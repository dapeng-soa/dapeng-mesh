package com.github.dapeng.gateway.netty.handler;

import com.github.dapeng.core.SoaException;
import com.github.dapeng.gateway.auth.WhiteListHandler;
import com.github.dapeng.gateway.http.HttpProcessorUtils;
import com.github.dapeng.gateway.http.match.UrlMappingResolverNew;
import com.github.dapeng.gateway.netty.request.RequestContext;
import com.github.dapeng.gateway.util.DapengMeshCode;
import com.github.dapeng.gateway.util.InvokeUtil;
import com.today.api.admin.OpenAdminServiceClient;
import com.today.api.admin.request.CheckGateWayAuthRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

/**
 * @author maple 2018.08.23 上午10:01
 */
@ChannelHandler.Sharable
public class HttpAuthHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = LoggerFactory.getLogger(HttpAuthHandler.class);

    private final OpenAdminServiceClient adminService = new OpenAdminServiceClient();


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
