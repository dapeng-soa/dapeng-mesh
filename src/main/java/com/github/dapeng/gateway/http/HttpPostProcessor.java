package com.github.dapeng.gateway.http;

import com.github.dapeng.core.InvocationContextImpl;
import com.github.dapeng.core.SoaException;
import com.github.dapeng.core.helper.SoaSystemEnvProperties;
import com.github.dapeng.gateway.auth.WhiteListHandler;
import com.github.dapeng.gateway.http.match.UrlMappingResolver;
import com.github.dapeng.gateway.netty.request.PostRequestInfo;
import com.github.dapeng.gateway.netty.request.RequestParser;
import com.github.dapeng.gateway.util.*;
import com.github.dapeng.util.DumpUtil;
import com.today.api.admin.OpenAdminServiceClient;
import com.today.api.admin.request.CheckGateWayAuthRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
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
     * @param request netty request
     * @param ctx     netty ctx
     */
    public void handlerPostRequest(FullHttpRequest request, ChannelHandlerContext ctx) {
        String uri = request.uri();
        PostRequestInfo info;
        try {
            info = UrlMappingResolver.handlerMappingUrl(uri, request);
        } catch (SoaException e) {
            logger.error(e.getCode(), e.getMsg());
            HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.OpenAuthEnableError), request, HttpResponseStatus.OK);
            return;
        }

        if (info == null) {
            try {
                info = UrlMappingResolver.handlerRequestParam(uri, request);
            } catch (SoaException e) {
                logger.error(e.getCode(), e.getMsg());
                HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.OpenAuthEnableError), request, HttpResponseStatus.OK);
                return;
            }
        }


        if (info != null) {
            try {
                authSecret(info, request, ctx);
            } catch (SoaException e) {
                HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapExCodeResponse(uri, e), request, HttpResponseStatus.OK);
                return;
            } catch (Exception e) {
                HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.AuthSecretError), request, HttpResponseStatus.OK);
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("请求参数: {} ", info.getArgumentString());
            }
            // fill invocationContext
            fillInvocationProxy(request, info, ctx);

            String parameter = RequestParser.fastParseParam(request, "parameter");

            CompletableFuture<String> jsonResponse = (CompletableFuture<String>) PostUtil.postAsync(info.getService(), info.getVersion(), info.getMethod(), parameter, request, InvokeUtil.getCookiesFromParameter(info));
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
                    HttpProcessorUtils.sendHttpResponse(ctx, resp, request, HttpResponseStatus.OK);

                } else {
                    InvocationContextImpl invocationContext = (InvocationContextImpl) InvocationContextImpl.Factory.currentInstance();
                    //判断返回结果是否为 0000
                    if (!SoaSystemEnvProperties.SOA_NORMAL_RESP_CODE.equals(invocationContext.lastInvocationInfo()
                            .responseCode())) {
                        logger.info("soa-response: " + DumpUtil.formatToString(result) + " cost:" + (System.currentTimeMillis() - beginTime) + "ms");
                        HttpProcessorUtils.sendHttpResponse(ctx, result, request, HttpResponseStatus.OK);
                        return;
                    }
                    String response = "{}".equals(result) ? "{\"status\":1}" : result.substring(0, result.lastIndexOf('}')) + ",\"status\":1}";

                    logger.info("soa-response: " + DumpUtil.formatToString(response) + " cost:" + (System.currentTimeMillis() - beginTime) + "ms");
                    HttpProcessorUtils.sendHttpResponse(ctx, response, request, HttpResponseStatus.OK);
                }
            });
        } else {
            HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.IllegalRequest), request, HttpResponseStatus.OK);
        }
    }

    /**
     * fillInvocationProxy
     */
    private void fillInvocationProxy(FullHttpRequest request, PostRequestInfo info, ChannelHandlerContext ctx) {
        SoaInvocationProxy invocationProxy = new SoaInvocationProxy(request, info, ctx);
        InvocationContextImpl.Factory.setInvocationContextProxy(invocationProxy);
    }

    /**
     * 鉴权 ..
     *
     * @param info post参数
     */
    private void authSecret(PostRequestInfo info, FullHttpRequest request, ChannelHandlerContext ctx) throws SoaException {
        String serviceName = info.getService();
        String apiKey = info.getApiKey();
        String secret = info.getSecret();
        String timestamp = info.getTimestamp();
        String parameter = info.getParameter();
        String secret2 = "".equals(info.getSecret2()) ? null : info.getSecret2();
        String remoteIp = InvokeUtil.getIpAddress(request, ctx);

//        setCallInvocationTimeOut();

        if (logger.isDebugEnabled()) {
            logger.debug("apiKey: {}, secret: {} , timestamp: {}, secret2: {} , parameter: {} ", apiKey, secret, timestamp, secret2, parameter);
        }
        try {
            checkSecret(serviceName, apiKey, secret, timestamp, parameter, secret2, remoteIp);
        } catch (SoaException e) {
            logger.error("request failed:: Invoke ip [ {} ] apiKey:[ {} ] call timestamp:[{}] call[ {}:{}:{} ] cookies:[{}] -> ", remoteIp, apiKey, timestamp, serviceName, info.getVersion(), info.getMethod(), InvokeUtil.getCookiesFromParameter(info));
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
