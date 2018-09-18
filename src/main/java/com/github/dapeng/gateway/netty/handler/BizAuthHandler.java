package com.github.dapeng.gateway.netty.handler;

import com.github.dapeng.core.InvocationContextImpl;
import com.github.dapeng.core.SoaException;
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

import java.util.Set;

/**
 * @author maple 2018.08.23 上午10:01
 */
@ChannelHandler.Sharable
public class BizAuthHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(BizAuthHandler.class);

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
        Set<String> list = WhiteListHandler.getServiceWhiteList();
        if (!list.contains(context.service().get())) {
            throw new SoaException("Err-GateWay-006", "非法请求,请联系管理员!");
        }

        fillInvocationProxy(context, ctx);

        String serviceName = context.service().get();
        String apiKey = context.apiKey().get();
        String secret = context.secret().get();
        String timestamp = context.timestamp().get();
        String parameter = context.parameter().get();
        String secret2 = context.secret2().orElse(null);

        String remoteIp = InvokeUtil.getIpAddress(context.request(), ctx);

        String requestJson = buildRequestJson(context, ctx);

        if (logger.isDebugEnabled()) {
            logger.debug("apiKey: {}, secret: {} , timestamp: {}, secret2: {} , parameter: {} ", apiKey, secret, timestamp, secret2, parameter);
        }

        try {
            String s = PostUtil.postSync(Constants.ADMIN_SERVICE_NAME, Constants.ADMIN_VERSION_NAME, Constants.ADMIN_METHOD_NAME, requestJson, context.request(), InvokeUtil.getCookiesFromParameter(context));
            logger.info(s);
        } catch (SoaException e) {
            logger.error("request failed:: Invoke ip [ {} ] apiKey:[ {} ] call timestamp:[{}] call[ {}:{}:{} ] cookies:[{}] -> ", remoteIp, apiKey, timestamp, serviceName, context.version().get(), context.method().get(), InvokeUtil.getCookiesFromParameter(context));
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    private String buildRequestJson(RequestContext context, ChannelHandlerContext ctx) {
        String apiKey = context.apiKey().get();
        String secret = context.secret().get();
        String timestamp = context.timestamp().get();
        String parameter = context.parameter().get();
        String secret2 = context.secret2().orElse(null);
        String remoteIp = InvokeUtil.getIpAddress(context.request(), ctx);

        StringBuilder jsonBuilder = new StringBuilder();

        jsonBuilder.append("{\"body\": {\"request\": {\"apiKey\": \"").append(apiKey).append("\"")
                .append(",\"timestamp\": \"").append(timestamp).append("\"")
                .append(",\"secret\": \"").append(secret).append("\"")
                .append(",\"invokeIp\": \"").append(remoteIp).append("\"")
                .append(",\"parameter\": \"").append(parameter).append("\"");
        if (secret2 != null) {
            jsonBuilder.append(",\"secret2\": \"").append(parameter).append("\"");

        }
        jsonBuilder.append("}}}");
        String json = jsonBuilder.toString();
        logger.info("request auth json : {}", json);
        return json;
    }

    /**
     * fillInvocationProxy
     */
    private void fillInvocationProxy(RequestContext context, ChannelHandlerContext ctx) {
        SoaInvocationProxy invocationProxy = new SoaInvocationProxy(context, ctx);
        InvocationContextImpl.Factory.setInvocationContextProxy(invocationProxy);
    }

}
