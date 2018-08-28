package com.github.dapeng.gateway.http;

import com.github.dapeng.gateway.util.Constants;
import com.github.dapeng.gateway.util.DapengMeshCode;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author maple 2018.08.28 下午3:21
 */
public class HttpGetHeadProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HttpGetHeadProcessor.class);
    /***
     * netty mesh 容器状态,即将关闭时显示 GREEN
     */
    public static MeshHealthStatus status = MeshHealthStatus.GREEN;

    public HttpResponseEntity handlerRequest(FullHttpRequest request) {
        String url = request.uri();

        if (Constants.GET_HEALTH_CHECK_URL.equals(url)) {
            return handlerHealth(url);
        }
        if (Constants.GET_CHECK.equals(url)) {
            logger.debug("check support url request, uri: {}", url);
            return new HttpResponseEntity(HttpProcessorUtils.wrapResponse(url, "dapeng-mesh is running"), HttpResponseStatus.OK);
        }
        logger.debug("not support url request, uri: {}", url);
        return new HttpResponseEntity(HttpProcessorUtils.wrapErrorResponse(url, DapengMeshCode.RequestUrlNotSupport), HttpResponseStatus.OK);
    }


    private HttpResponseEntity handlerHealth(String url) {
        logger.debug("handlerHealth check,container status: " + status);
        if (status == MeshHealthStatus.YELLOW) {
            logger.info("handlerHealth check,container status: " + status);
            return new HttpResponseEntity(HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.MeshShutdownSoon), HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
        return new HttpResponseEntity(HttpProcessorUtils.wrapResponse(url, "health check container is running"), HttpResponseStatus.OK);
    }


}
