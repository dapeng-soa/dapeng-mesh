package com.github.dapeng.gateway.http;

import com.github.dapeng.core.SoaException;
import com.github.dapeng.gateway.netty.request.RequestContext;
import com.github.dapeng.gateway.util.DapengMeshCode;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author maple 2018.08.28 下午4:53
 */
public class HttpProcessorUtils {
    private static Logger logger = LoggerFactory.getLogger(HttpProcessorUtils.class);

    //未完成请求计数
    private static AtomicInteger requestCounter = new AtomicInteger(0);

    public static void sendHttpResponse(ChannelHandlerContext ctx, HttpResponseEntity entity, RequestContext context) {
        sendHttpResponse(ctx, entity.getContent(), context.request(), entity.getStatus());
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
    public static void sendHttpResponse(ChannelHandlerContext ctx, String content, FullHttpRequest request, HttpResponseStatus status) {
        try {
            ByteBuf wrapBuf = ctx.alloc().buffer(content.length());
            wrapBuf.writeBytes(content.getBytes(CharsetUtil.UTF_8));

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, wrapBuf);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

            /*设置跨域*/
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS,"Origin, X-Requested-With, Content-Type, Accept");
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN,"*");


            if (request == null) {
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                boolean isKeepAlive = HttpUtil.isKeepAlive(request);
                if (isKeepAlive) {
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    ctx.writeAndFlush(response);
                } else {
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                }
            }
        } finally {
            //请求返回，计数 -1
            requestCounter.decrementAndGet();
        }
    }


    public static String wrapErrorResponse(DapengMeshCode code) {
        return wrapErrorResponse(null, code);
    }


    public static String wrapExCodeResponse(SoaException ex) {
        return wrapExCodeResponse(null, ex);
    }

    /**
     * wrap message response for json format.
     *
     * @param code
     * @return
     */
    public static String wrapErrorResponse(String url, DapengMeshCode code) {
        String resp = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", code.getCode(), code.getMsg(), "{}");
        logger.info("mesh-response: url: {}, info: {}", url, resp);
        return resp;
    }

    /**
     * wrap message response for json format.
     *
     * @param ex
     * @return
     */
    public static String wrapExCodeResponse(String url, SoaException ex) {
        String resp = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", ex.getCode(), ex.getMsg(), "{}");
        logger.info("mesh-response: url: {}, info: {}", url, resp);
        return resp;
    }

    /**
     * wrap message response for json format.
     */
    public static String wrapResponse(String url, Object msg) {
        String resp = String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":1}", "0000", msg, "{}");
        logger.debug("mesh-response: url: {}, info: {}", url, resp);
        return resp;
    }

    /**
     * wrap message response for json format.
     */
    public static String logResponse(String url, Object msg) {
        logger.debug("mesh-log-response: url: {}, info: {}", url, msg);
        return msg.toString();
    }

    public static AtomicInteger getRequestCounter() {
        return requestCounter;
    }

}
