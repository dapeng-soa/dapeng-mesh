package com.github.dapeng.gateway.netty.handler;

import com.github.dapeng.gateway.http.HttpGetHeadProcessor;
import com.github.dapeng.gateway.http.HttpPostProcessor;
import com.github.dapeng.gateway.http.HttpResponseEntity;
import com.github.dapeng.gateway.util.DapengMeshCode;
import com.github.dapeng.gateway.http.HttpProcessorUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author maple 2018.08.23 上午10:01
 */
@ChannelHandler.Sharable
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static Logger logger = LoggerFactory.getLogger(NettyHttpServerHandler.class);

    private final HttpGetHeadProcessor getHandler = new HttpGetHeadProcessor();
    private final HttpPostProcessor postHandler = new HttpPostProcessor();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) throws Exception {
        try {
            dispatchRequest(httpRequest, ctx);
        } catch (Exception e) {
            logger.error("网关处理请求失败: " + e.getMessage(), e);
            HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.ProcessReqFailed), null, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void dispatchRequest(FullHttpRequest request, ChannelHandlerContext ctx) {
        HttpMethod method = request.method();
        boolean isPost = HttpMethod.POST.equals(method);
        if (isPost) {
            postHandler.handlerPostRequest(request, ctx);
            return;
        }
        boolean isGet = HttpMethod.GET.equals(method);
        if (isGet || HttpMethod.HEAD.equals(method)) {
            handlerGetAndHead(request, ctx);
            return;
        }
        HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.RequestTypeNotSupport), request, HttpResponseStatus.OK);
    }


    /**
     * handler get 和 head 请求
     */
    private void handlerGetAndHead(FullHttpRequest request, ChannelHandlerContext ctx) {
        HttpResponseEntity entity = getHandler.handlerRequest(request);
        HttpProcessorUtils.sendHttpResponse(ctx, entity, request);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("连接的客户端地址:{}", ctx.channel().remoteAddress());
        }
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("网关handler exceptionCaught未知异常: " + cause.getMessage(), cause);
        HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.MeshUnknownError), null, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        ctx.close();
    }
}
