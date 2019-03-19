package com.github.dapeng.gateway.netty.handler;

import com.github.dapeng.core.InvocationContextImpl;
import com.github.dapeng.core.SoaCode;
import com.github.dapeng.core.SoaException;
import com.github.dapeng.core.helper.SoaSystemEnvProperties;
import com.github.dapeng.gateway.auth.WhiteListHandler;
import com.github.dapeng.gateway.http.HttpProcessorUtils;
import com.github.dapeng.gateway.netty.request.RequestContext;
import com.github.dapeng.gateway.util.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author maple 2018.08.23 上午10:01
 */
@ChannelHandler.Sharable
public class AuthenticationHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(AuthenticationHandler.class);
    private static final Pattern PATTERN = Pattern.compile("\"");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RequestContext context = (RequestContext) msg;
        try {
            // POST FIRST
            if (HttpMethod.POST.equals(context.httpMethod())) {
                //鉴权
                try {
                    authSecret(context, ctx);
                } catch (SoaException e) {
                    HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapExCodeResponse(context.requestUrl(), e), context.request(), HttpResponseStatus.OK);
                    return;
                } catch (Exception e) {
                    HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.AuthSecretError), context.request(), HttpResponseStatus.OK);
                    return;
                }
            }
            super.channelRead(ctx, context);
        } catch (Exception e) {
            logger.error("网关处理请求失败: " + e.getMessage(), e);
            HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.ProcessReqFailed), null, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 鉴权 ..
     *
     * @param context request 请求上下文
     */
    private void authSecret(RequestContext context, ChannelHandlerContext ctx) throws Exception {
        if (Boolean.valueOf(SysEnvUtil.OPEN_AUTH_ENABLE)) {
            Set<String> list = WhiteListHandler.getServiceWhiteList();
            if (!list.contains(context.service().get())) {
                throw new SoaException("Err-GateWay-006", "非法请求,请联系管理员!");
            }
        }
        Optional<String> serviceName = context.service();
        Optional<String> apiKey = context.apiKey();
        Optional<String> secret = context.secret();
        Optional<String> timestamp = context.timestamp();
        Optional<String> parameter = context.parameter();
        Optional<String> secret2 = context.secret2();

        failIfNotPresent(serviceName, apiKey, timestamp);

        if (!secret.isPresent() && !secret2.isPresent()) {
            throw new SoaException(DapengMeshCode.AuthSecretEx);
        }

        fillInvocationProxy(context, ctx);
        String remoteIp = InvokeUtil.getIpAddress(context.request(), ctx);
        String requestJson = buildRequestJson(context, ctx);

        if (logger.isDebugEnabled()) {
            logger.debug("apiKey: {}, secret: {} , timestamp: {}, secret2: {} , parameter: {} ", apiKey, secret, timestamp, secret2, parameter);
        }

        try {
            String responseCode = PostUtil.postSync(Constants.ADMIN_SERVICE_NAME, Constants.ADMIN_VERSION_NAME, Constants.ADMIN_METHOD_NAME, requestJson, context.request(), InvokeUtil.getCookiesFromParameter(context));

            //没有成功的错误
            if (!SoaSystemEnvProperties.SOA_NORMAL_RESP_CODE.equals(responseCode)) {
                if (responseCode == null) {
                    throw new SoaException(SoaCode.ServerUnKnown);
                }
                throw buildExceptionByCode(responseCode);
            }
        } catch (SoaException e) {
            logger.error("request failed:: Invoke ip [ {} ] apiKey:[ {} ] call timestamp:[{}] call[ {}:{}:{} ] cookies:[{}]", remoteIp, apiKey, timestamp, serviceName, context.version().get(), context.method().get(), InvokeUtil.getCookiesFromParameter(context));
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * build request json
     *
     * @param context request context
     * @param ctx     netty channel ctx
     * @return request json
     */
    private String buildRequestJson(RequestContext context, ChannelHandlerContext ctx) {
        String apiKey = context.apiKey().get();
        String timestamp = context.timestamp().get();
        String parameter = context.parameter().get();
        String remoteIp = InvokeUtil.getIpAddress(context.request(), ctx);

        String secret = context.secret().orElse(null);
        String secret2 = context.secret2().orElse(null);

        StringBuilder jsonBuilder = new StringBuilder();

        if (secret != null && secret.length() > 0 && secret.trim().length() > 0) {
            jsonBuilder.append("{\"body\": {\"request\": {\"apiKey\": \"").append(apiKey).append("\"")
                    .append(",\"timestamp\": \"").append(timestamp).append("\"")
                    .append(",\"secret\": \"").append(secret).append("\"")
                    .append(",\"invokeIp\": \"").append(remoteIp).append("\"");

            //防止 secret2 空串
            if (secret2 != null && secret2.length() > 0 && secret2.trim().length() > 0) {
                String parameter2 = PATTERN.matcher(parameter).replaceAll("\\\\\"");

                jsonBuilder.append(",\"parameter\":\"").append(parameter2).append("\"")
                        .append(",\"secret2\": \"").append(secret2).append("\"");

                logger.info("parameter2: {}", parameter2);
            }
            jsonBuilder.append("}}}");
        } else {
            String parameter2 = PATTERN.matcher(parameter).replaceAll("\\\\\"");
            jsonBuilder.append("{\"body\": {\"request\": {\"apiKey\": \"").append(apiKey).append("\"")
                    .append(",\"timestamp\": \"").append(timestamp).append("\"")
                    .append(",\"invokeIp\": \"").append(remoteIp).append("\"")
                    .append(",\"parameter\":\"").append(parameter2).append("\"")
                    .append(",\"secret2\": \"").append(secret2).append("\"");
            jsonBuilder.append("}}}");
        }
        String json = jsonBuilder.toString();
        if (logger.isDebugEnabled()) {
            logger.debug("call authentication json: {}", json);
        }
        return json;
    }


    /**
     * fillInvocationProxy
     */
    private void fillInvocationProxy(RequestContext context, ChannelHandlerContext ctx) {
        SoaInvocationProxy invocationProxy = new SoaInvocationProxy(context, ctx);
        InvocationContextImpl.Factory.setInvocationContextProxy(invocationProxy);
    }

    /**
     * build error exception from  rpc response code
     *
     * @param errorCode rpc response code
     * @return exception
     */
    private SoaException buildExceptionByCode(String errorCode) {
        switch (errorCode) {
            case "Err-GateWay-001":
            case "Err-GateWay-002":
            case "Err-GateWay-003":
            case "Err-GateWay-005":
                return new SoaException(errorCode, "认证失败，非法请求");
            case "Err-GateWay-004":
                return new SoaException(errorCode, "Api网关请求超时");
            default:
                return new SoaException(errorCode, "调用admin鉴权未知错误");
        }
    }

    /**
     * optionals both must isPresent , otherwise will throw exception
     *
     * @param optionals parameter optionals
     * @throws SoaException
     * @see java.util.Optional
     */
    @SafeVarargs
    private final void failIfNotPresent(Optional<String>... optionals) throws SoaException {
        for (Optional<String> optional : optionals) {

            if (!optional.isPresent()) {
                throw new SoaException(DapengMeshCode.AuthParameterEx);
            }
        }
    }
}
