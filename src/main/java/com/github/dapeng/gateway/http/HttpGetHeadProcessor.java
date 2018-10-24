package com.github.dapeng.gateway.http;

import com.github.dapeng.gateway.netty.request.RequestContext;
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

    private GetUrlController controller = new GetUrlController();

    public HttpResponseEntity handlerRequest(RequestContext context) {
        String url = processUrl(context.requestUrl());

        switch (url) {
            case Constants.GET_HEALTH_CHECK_URL:

                return controller.handlerHealth(url);
            case Constants.GET_CHECK:

                return controller.getCheck(url);
            case Constants.ECHO_PREFIX:

                return controller.echo(url);
            case Constants.SERVICE_LIST:

                return controller.serviceList(url);
            case Constants.SYS_TIME_SYNC:

                return controller.syncSysTime(url);
            default:
                if (url.contains(Constants.ECHO_PREFIX)) {
                    return controller.echo(url);
                }
                break;
        }
        logger.info("not support url request, uri: {}", url);
        return new HttpResponseEntity(HttpProcessorUtils.wrapErrorResponse(url, DapengMeshCode.RequestUrlNotSupport), HttpResponseStatus.OK);
    }

    /**
     * 如果 Get Url 带有 "?" , 去问号之前的内容
     *
     * @param url input url
     * @return
     */
    private String processUrl(String url) {
        int i = url.lastIndexOf("?");
        if (i > 0) {
            return url.substring(0, i);
        }
        return url;
    }
}
