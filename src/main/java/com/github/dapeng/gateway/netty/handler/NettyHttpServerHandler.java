package com.github.dapeng.gateway.netty.handler;

import com.github.dapeng.core.SoaCode;
import com.github.dapeng.gateway.config.ContainerStatus;
import com.github.dapeng.gateway.netty.match.AntPathMatcher;
import com.github.dapeng.gateway.netty.match.PathMatcher;
import com.github.dapeng.gateway.netty.request.RequestParser;
import com.github.dapeng.gateway.util.PostUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * desc: NettyHttpServerHandler
 *
 * @author hz.lei
 * @since 2018年08月23日 上午10:01
 */
public class NettyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private PathMatcher pathMatcher = new AntPathMatcher();


    private final String DEFAULT_MATCH = "/api/{serviceName:[\\s\\S]*}/{version:[\\s\\S]*}/{methodName:[\\s\\S]*}";


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) throws Exception {
        // doService
        try {
            doService(httpRequest, ctx);
        } catch (Exception e) {
            send(ctx, "处理请求失败!", HttpResponseStatus.INTERNAL_SERVER_ERROR);
            logger.error("处理请求失败!" + e.getMessage(), e);
        }
    }

    protected void doService(FullHttpRequest request, ChannelHandlerContext ctx) throws Exception {

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
        send(ctx, "不符合的请求", HttpResponseStatus.OK);
    }

    private void handlerPostRequest(FullHttpRequest request, ChannelHandlerContext ctx) {
        String uri = request.uri();
        if (pathMatcher.match(DEFAULT_MATCH, uri)) {
            Map<String, String> pathVariableMap = pathMatcher.extractUriTemplateVariables(DEFAULT_MATCH, request.uri());
            String serviceName = pathVariableMap.get("serviceName");
            String version = pathVariableMap.get("version");
            String methodName = pathVariableMap.get("methodName");
            String parameter = RequestParser.fastParseParam(request, "parameter");
            logger.info("parameter info: {}", parameter);

            CompletableFuture<String> jsonResponse = (CompletableFuture<String>) PostUtil.postAsync(serviceName, version, methodName, parameter, request);

            jsonResponse.whenComplete((result, ex) -> {
                if (ex != null) {
                    String resp = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", SoaCode.ServerUnKnown.getCode(), ex.getMessage(), "{}");
                    send(ctx, resp, HttpResponseStatus.OK);
                } else {
                    if (result.contains("status")) {
                        send(ctx, result, HttpResponseStatus.OK);
                        return;
                    }
                    String response = "{}".equals(result) ? "{\"status\":1}" : result.substring(0, result.lastIndexOf('}')) + ",\"status\":1}";
                    send(ctx, response, HttpResponseStatus.OK);
                }
            });
        } else {
            send(ctx, "不合法的请求", HttpResponseStatus.OK);

        }

    }


    private void handlerGetAndHead(FullHttpRequest request, ChannelHandlerContext ctx) {
        String uri = request.uri();
        if ("/health/check".equals(uri)) {
            logger.debug("health check,container status: " + ContainerStatus.GREEN);
            send(ctx, "GateWay is running", HttpResponseStatus.OK);
        } else {
            logger.debug("not support url request, uri: {}", uri);
            send(ctx, "不支持的请求类型", HttpResponseStatus.OK);
        }
    }


    private void send(ChannelHandlerContext ctx, String context, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(context, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("连接的客户端地址:{}", ctx.channel().remoteAddress());
        ctx.writeAndFlush("客户端" + InetAddress.getLocalHost().getHostName() + "成功与服务端建立连接！ ");
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(), cause);
        send(ctx, cause.getMessage(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
}
