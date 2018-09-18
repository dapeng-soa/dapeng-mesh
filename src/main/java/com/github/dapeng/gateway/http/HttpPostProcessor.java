package com.github.dapeng.gateway.http;

import com.github.dapeng.core.InvocationContextImpl;
import com.github.dapeng.core.SoaException;
import com.github.dapeng.core.helper.SoaSystemEnvProperties;
import com.github.dapeng.gateway.auth.WhiteListHandler;
import com.github.dapeng.gateway.netty.request.RequestContext;
import com.github.dapeng.gateway.util.*;
import com.github.dapeng.util.DumpUtil;
import com.today.api.admin.OpenAdminServiceClient;
import com.today.api.admin.request.CheckGateWayAuthRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author maple 2018.08.28 下午3:21
 */
public class HttpPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(HttpPostProcessor.class);

    private final OpenAdminServiceClient adminService = new OpenAdminServiceClient();

    /**
     * {@link SoaSystemEnvProperties#SOA_NORMAL_RESP_CODE}
     *
     * @param context 封装的每一次请求上下文
     * @param ctx     netty ctx
     */
    public void handlerPostRequest(RequestContext context, ChannelHandlerContext ctx) {

        if (context.isLegal()) {
            try {
                authSecret(context, ctx);
            } catch (SoaException e) {
                HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapExCodeResponse(context.requestUrl(), e), context.request(), HttpResponseStatus.OK);
                return;
            } catch (Exception e) {
                HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.AuthSecretError), context.request(), HttpResponseStatus.OK);
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Http:{}, 请求参数: {} ", context.requestUrl(), context.argumentToString());
            }
            // fill invocationContext
            fillInvocationProxy(context, ctx);

//            String parameter = RequestParser.fastParseParam(context, "parameter");

            String parameter = context.parameter().get();
            //todo 修改传入参数 ...
            CompletableFuture<String> jsonResponse = (CompletableFuture<String>) PostUtil.postAsync(context.service().get(), context.version().get(), context.method().get(), parameter, context.request(), InvokeUtil.getCookiesFromParameter(context));
            long beginTime = System.currentTimeMillis();
            jsonResponse.whenComplete((result, ex) -> {
                if (ex != null) {
                    String resp;
                    if (ex instanceof SoaException) {
                        resp = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", ((SoaException) ex).getCode(), ((SoaException) ex).getMsg(), "{}");
                        logger.info("soa-response: " + resp + " cost:" + (System.currentTimeMillis() - beginTime) + "ms");
                    } else {
                        resp = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", DapengMeshCode.MeshUnknowEx.getCode(), ex.getMessage(), "{}");
                        logger.info("soa-response: " + resp + " cost:" + (System.currentTimeMillis() - beginTime) + "ms");
                    }
                    HttpProcessorUtils.sendHttpResponse(ctx, resp, context.request(), HttpResponseStatus.OK);

                } else {
                    InvocationContextImpl invocationContext = (InvocationContextImpl) InvocationContextImpl.Factory.currentInstance();
                    //判断返回结果是否为 0000
                    if (!SoaSystemEnvProperties.SOA_NORMAL_RESP_CODE.equals(invocationContext.lastInvocationInfo()
                            .responseCode())) {
                        logger.info("soa-response: " + DumpUtil.formatToString(result) + " cost:" + (System.currentTimeMillis() - beginTime) + "ms");
                        HttpProcessorUtils.sendHttpResponse(ctx, result, context.request(), HttpResponseStatus.OK);
                        return;
                    }
                    String response = "{}".equals(result) ? "{\"status\":1}" : result.substring(0, result.lastIndexOf('}')) + ",\"status\":1}";

                    logger.info("soa-response: " + DumpUtil.formatToString(response) + " cost:" + (System.currentTimeMillis() - beginTime) + "ms");
                    HttpProcessorUtils.sendHttpResponse(ctx, response, context.request(), HttpResponseStatus.OK);
                }
            });
        } else {
            HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.IllegalRequest), context.request(), HttpResponseStatus.OK);
        }
    }

    /**
     * fillInvocationProxy
     */
    private void fillInvocationProxy(RequestContext context, ChannelHandlerContext ctx) {
        SoaInvocationProxy invocationProxy = new SoaInvocationProxy(context, ctx);
        InvocationContextImpl.Factory.setInvocationContextProxy(invocationProxy);
    }

    /**
     * 鉴权 ..
     *
     * @param context request 请求上下文
     */
    private void authSecret(RequestContext context, ChannelHandlerContext ctx) throws SoaException {
        String serviceName = context.service().get();
        String apiKey = context.apiKey().get();
        String secret = context.secret().get();
        String timestamp = context.timestamp().get();
        String parameter = context.parameter().get();
        String secret2 = "".equals(context.secret2().get()) ? null : context.secret2().get();
        String remoteIp = InvokeUtil.getIpAddress(context.request(), ctx);

//        setCallInvocationTimeOut();

        if (logger.isDebugEnabled()) {
            logger.debug("apiKey: {}, secret: {} , timestamp: {}, secret2: {} , parameter: {} ", apiKey, secret, timestamp, secret2, parameter);
        }
        try {
            checkSecret(serviceName, apiKey, secret, timestamp, parameter, secret2, remoteIp);
        } catch (SoaException e) {
            //todo
            logger.error("request failed:: Invoke ip [ {} ] apiKey:[ {} ] call timestamp:[{}] call[ {}:{}:{} ] cookies:[{}] -> ", remoteIp, apiKey, timestamp, serviceName, context.version().get(), context.method().get(), InvokeUtil.getCookiesFromParameter(context));
            throw e;
        }
    }

    private void checkSecret(String serviceName, String apiKey, String secret, String timestamp, String parameter, String secret2, String ip) throws SoaException {
        Set<String> list = WhiteListHandler.getServiceWhiteList();
        if (null == list || !list.contains(serviceName)) {
            throw new SoaException("Err-GateWay-006", "非法请求,请联系管理员!");
        }
        CheckGateWayAuthRequest checkGateWayAuthRequest = new CheckGateWayAuthRequest();
        checkGateWayAuthRequest.setApiKey(apiKey);
        checkGateWayAuthRequest.setSecret(Optional.ofNullable(secret));
        checkGateWayAuthRequest.setTimestamp(timestamp);
        checkGateWayAuthRequest.setInvokeIp(ip);
        checkGateWayAuthRequest.setParameter(Optional.ofNullable(parameter));
        checkGateWayAuthRequest.setSecret2(Optional.ofNullable(secret2));

        adminService.checkGateWayAuth(checkGateWayAuthRequest);
    }

    /*private void setCallInvocationTimeOut() {
        InvocationContextImpl invocationCtx = (InvocationContextImpl) InvocationContextImpl.Factory.currentInstance();
        invocationCtx.timeout(10000);
    }*/
}
