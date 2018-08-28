package com.github.dapeng.gateway.netty.handler;

import com.github.dapeng.core.SoaCode;
import com.github.dapeng.gateway.config.ContainerStatus;
import com.github.dapeng.gateway.netty.match.UrlMappingResolver;
import com.github.dapeng.gateway.netty.request.PostRequestInfo;
import com.github.dapeng.gateway.netty.request.RequestParser;
import com.github.dapeng.gateway.util.Constants;
import com.github.dapeng.gateway.util.DapengMeshCode;
import com.github.dapeng.gateway.util.PostUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author hz.lei 2018.08.23 上午10:01
 */
@ChannelHandler.Sharable
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static Logger logger = LoggerFactory.getLogger(NettyHttpServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) throws Exception {
        try {
            doService(httpRequest, ctx);
        } catch (Exception e) {
            logger.error("网关处理请求失败: " + e.getMessage(), e);
            sendHttpResponse(ctx, wrapErrorResponse(DapengMeshCode.ProcessReqFailed), null, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void doService(FullHttpRequest request, ChannelHandlerContext ctx) throws Exception {

        dispatchRequest(request, ctx);
    }

    private void dispatchRequest(FullHttpRequest request, ChannelHandlerContext ctx) {
        HttpMethod method = request.method();
        boolean isGet = HttpMethod.GET.equals(method);
        if (isGet || HttpMethod.HEAD.equals(method)) {
            handlerGetAndHead(request, ctx);
            return;
        }
        boolean isPost = HttpMethod.POST.equals(method);
        if (isPost) {
            handlerPostRequest(request, ctx);
            return;
        }
        sendHttpResponse(ctx, wrapErrorResponse(DapengMeshCode.IllegalRequest), request, HttpResponseStatus.OK);
    }

    private void handlerPostRequest(FullHttpRequest request, ChannelHandlerContext ctx) {
        String uri = request.uri();
        PostRequestInfo info = UrlMappingResolver.handlerMappingUrl(uri);

        if (info == null) {
            info = UrlMappingResolver.handlerRequestParam(uri, request);
        }

        if (info != null) {
            logger.info("请求参数: {} ", info.getArgumentString());

            String parameter = RequestParser.fastParseParam(request, "parameter");

            CompletableFuture<String> jsonResponse = (CompletableFuture<String>) PostUtil.postAsync(info.getService(), info.getVersion(), info.getMethod(), parameter, request);
            jsonResponse.whenComplete((result, ex) -> {
                if (ex != null) {
                    String resp = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", SoaCode.ServerUnKnown.getCode(), ex.getMessage(), "{}");
                    sendHttpResponse(ctx, resp, request, HttpResponseStatus.OK);
                } else {
                    if (result.contains("status")) {
                        sendHttpResponse(ctx, result, request, HttpResponseStatus.OK);
                        return;
                    }
                    String response = "{}".equals(result) ? "{\"status\":1}" : result.substring(0, result.lastIndexOf('}')) + ",\"status\":1}";
                    sendHttpResponse(ctx, response, request, HttpResponseStatus.OK);
                }
            });
        } else {
            sendHttpResponse(ctx, wrapErrorResponse(DapengMeshCode.IllegalRequest), request, HttpResponseStatus.OK);
        }
    }

    private void handlerGetAndHead(FullHttpRequest request, ChannelHandlerContext ctx) {
        String uri = request.uri();
        if (Constants.GET_HEALTH_CHECK_URL.equals(uri)) {
            logger.debug("health check,container status: " + ContainerStatus.GREEN);
            sendHttpResponse(ctx, "GateWay is running", request, HttpResponseStatus.OK);
        } else {
            logger.debug("not support url request, uri: {}", uri);
            sendHttpResponse(ctx, wrapErrorResponse(DapengMeshCode.RequestTypeNotSupport), request, HttpResponseStatus.OK);
        }
    }


    /**
     * wrap message response for json format.
     *
     * @param code
     * @return
     */
    private String wrapErrorResponse(DapengMeshCode code) {
        String resp = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", code.getCode(), code.getMsg(), "{}");
        logger.info("mesh-response: {}", resp);
        return resp;
    }

    /**
     * 返回信息给前端 http
     *
     * @param ctx     handler's context
     * @param content msg's info
     * @param request msg's request
     * @param status  http status
     * @link 不使用 Unpooled.copiedBuffer(content, CharsetUtil.UTF_8)
     * @link Unpooled.wrappedBuffer
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, String content, FullHttpRequest request, HttpResponseStatus status) {
        ByteBuf wrapBuf = ctx.alloc().buffer(content.length());
        wrapBuf.writeBytes(content.getBytes(CharsetUtil.UTF_8));

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, wrapBuf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        if (request == null) {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            boolean isKeepAlive = HttpUtil.isKeepAlive(request);
            if (isKeepAlive) {
//                response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                ctx.writeAndFlush(response);
            } else {
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        }
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
        sendHttpResponse(ctx, wrapErrorResponse(DapengMeshCode.GateWayUnknownError), null, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        ctx.close();
    }
}
