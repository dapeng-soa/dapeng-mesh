package com.github.dapeng.gateway.netty.handler;

import com.github.dapeng.core.SoaException;
import com.github.dapeng.gateway.http.HttpGetHeadProcessor;
import com.github.dapeng.gateway.http.HttpPostProcessor;
import com.github.dapeng.gateway.http.HttpResponseEntity;
import com.github.dapeng.gateway.netty.request.RequestContext;
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
public class ServerProcessHandler extends SimpleChannelInboundHandler<RequestContext> {
    private static Logger logger = LoggerFactory.getLogger(ServerProcessHandler.class);

    private final HttpGetHeadProcessor getHandler = new HttpGetHeadProcessor();
    private final HttpPostProcessor postHandler = new HttpPostProcessor();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestContext context) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("current request context: {}", context.toString());
        }
        try {
            dispatchRequest(context, ctx);
        } catch (SoaException e) {
            logger.error("网关请求SoaException：" + e.getMessage());
            HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapExCodeResponse(e), null, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.error("网关处理请求失败: " + e.getMessage(), e);
            HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.ProcessReqFailed), null, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void dispatchRequest(RequestContext context, ChannelHandlerContext ctx) throws SoaException {

        HttpMethod httpMethod = context.httpMethod();

        if (HttpMethod.POST.equals(httpMethod)) {
            postHandler.handlerPostRequest(context, ctx);
            return;
        }
        boolean isGet = HttpMethod.GET.equals(httpMethod);
        if (isGet || HttpMethod.HEAD.equals(httpMethod)) {
            handlerGetAndHead(context, ctx);
            return;
        }
        HttpProcessorUtils.sendHttpResponse(ctx, HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.RequestTypeNotSupport), context.request(), HttpResponseStatus.OK);
    }


    /**
     * handler get 和 head 请求
     */
    private void handlerGetAndHead(RequestContext context, ChannelHandlerContext ctx) {
        HttpResponseEntity entity = getHandler.handlerRequest(context);
        HttpProcessorUtils.sendHttpResponse(ctx, entity, context);
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
