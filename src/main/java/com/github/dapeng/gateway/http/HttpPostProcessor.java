package com.github.dapeng.gateway.http;

import com.github.dapeng.core.InvocationContextImpl;
import com.github.dapeng.core.SoaException;
import com.github.dapeng.core.helper.SoaSystemEnvProperties;
import com.github.dapeng.gateway.netty.request.RequestContext;
import com.github.dapeng.gateway.util.*;
import com.github.dapeng.util.DumpUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author maple 2018.08.28 下午3:21
 */
public class HttpPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(HttpPostProcessor.class);


    /**
     * {@link SoaSystemEnvProperties#SOA_NORMAL_RESP_CODE}
     *
     * @param context 封装的每一次请求上下文
     * @param ctx     netty ctx
     */
    public void handlerPostRequest(RequestContext context, ChannelHandlerContext ctx) {

        if (context.isLegal()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Http:{}, 请求参数: {} ", context.requestUrl(), context.argumentToString());
            }
            // fill invocationContext
            fillInvocationProxy(context, ctx);

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


}
