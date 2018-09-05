package com.github.dapeng.gateway.http;

import com.github.dapeng.core.SoaException;
import com.github.dapeng.echo.EchoClient;
import com.github.dapeng.gateway.http.match.UrlMappingResolver;
import com.github.dapeng.gateway.util.DapengMeshCode;
import com.github.dapeng.json.OptimizedMetadata;
import com.github.dapeng.openapi.cache.ServiceCache;
import io.netty.handler.codec.http.HttpResponseStatus;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Get 请求的 url 映射 <-> 处理器
 *
 * @author maple 2018.09.05 下午4:05
 */
public class GetUrlController {
    private Logger logger = LoggerFactory.getLogger(GetUrlController.class);
    /***
     * netty mesh 容器状态,即将关闭时显示 GREEN
     */
    public static MeshHealthStatus status = MeshHealthStatus.GREEN;

    /**
     * 将康检查
     *
     * @param url {@link com.github.dapeng.gateway.util.Constants#GET_HEALTH_CHECK_URL}
     * @return resp
     */
    public HttpResponseEntity handlerHealth(String url) {
        logger.debug("handlerHealth check,container status: " + status);
        if (status == MeshHealthStatus.YELLOW) {
            logger.info("handlerHealth check,container status: " + status);
            return new HttpResponseEntity(HttpProcessorUtils.wrapErrorResponse(DapengMeshCode.MeshShutdownSoon), HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
        return new HttpResponseEntity(HttpProcessorUtils.wrapResponse(url, "health check container is running"), HttpResponseStatus.OK);
    }

    /**
     * 将康检查
     *
     * @param url {@link com.github.dapeng.gateway.util.Constants#GET_CHECK}
     * @return resp
     */
    public HttpResponseEntity getCheck(String url) {
        logger.debug("check support url request, uri: {}", url);
        return new HttpResponseEntity(HttpProcessorUtils.wrapResponse(url, "dapeng-mesh is running"), HttpResponseStatus.OK);
    }

    /**
     * 调用指定服务的echo方法，判断服务是否健康
     */
    public HttpResponseEntity echo(String url) {
        Pair<String, String> pair = UrlMappingResolver.handlerEchoUrl(url);
        if (pair != null) {
            try {
                String echoResp = new EchoClient(pair.getKey(), pair.getValue()).echo();
                return new HttpResponseEntity(HttpProcessorUtils.wrapResponse(url, echoResp), HttpResponseStatus.OK);
            } catch (SoaException ex) {
                logger.error(ex.getMessage(), ex);
                return new HttpResponseEntity(HttpProcessorUtils.wrapResponse(url, ex), HttpResponseStatus.OK);
            }
        }
        return new HttpResponseEntity(HttpProcessorUtils.wrapResponse(url, DapengMeshCode.EchoUnknowEx), HttpResponseStatus.OK);
    }

    /**
     * 服务列表
     *
     * @return
     */
    public HttpResponseEntity serviceList(String url) {
        Map<String, OptimizedMetadata.OptimizedService> services = ServiceCache.getServices();
        List<String> list = new ArrayList<>(16);
        services.forEach((k, v) -> list.add(v.getService().namespace + "." + v.getService().name + ":" + v.getService().meta.version));
        return new HttpResponseEntity(HttpProcessorUtils.wrapResponse(url, list.toString()), HttpResponseStatus.OK);
    }

    /**
     * 客户端同步时间接口
     *
     * @param url {@link com.github.dapeng.gateway.util.Constants#SYS_TIME_SYNC}
     * @return 服务端系统当前时间
     */
    public HttpResponseEntity syncSysTime(String url) {
        return new HttpResponseEntity(HttpProcessorUtils.wrapResponse(url, System.currentTimeMillis()), HttpResponseStatus.OK);
    }

}
