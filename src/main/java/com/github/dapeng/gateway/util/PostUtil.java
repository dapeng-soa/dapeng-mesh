package com.github.dapeng.gateway.util;

import com.github.dapeng.core.InvocationContext;
import com.github.dapeng.core.InvocationContextImpl;
import com.github.dapeng.core.SoaCode;
import com.github.dapeng.core.SoaException;
import com.github.dapeng.core.enums.CodecProtocol;
import com.github.dapeng.core.helper.DapengUtil;
import com.github.dapeng.core.helper.SoaSystemEnvProperties;
import com.github.dapeng.json.OptimizedMetadata;
import com.github.dapeng.openapi.cache.ServiceCache;
import com.github.dapeng.openapi.utils.AsyncJsonPost;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * desc: PostUtil
 *
 * @author hz.lei
 * @since 2018年08月23日 下午2:29
 */
public class PostUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.github.dapeng.openapi.utils.PostUtil.class);


    public static Future<String> postAsync(String service, String version, String method, String parameter, FullHttpRequest req) {
        return postAsync(service, version, method, parameter, req, true);
    }



    private static Future<String> postAsync(String service,
                                            String version,
                                            String method,
                                            String parameter,
                                            FullHttpRequest req,
                                            boolean clearInvocationContext) {
        InvocationContextImpl invocationCtx = (InvocationContextImpl) createInvocationCtx(service, version, method, req);

        OptimizedMetadata.OptimizedService bizService = ServiceCache.getService(service, version);

        if (bizService == null) {
            LOGGER.error("bizService not found[service:" + service + ", version:" + version + "]");
            return CompletableFuture.completedFuture(String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", SoaCode.NoMatchedService.getCode(), SoaCode.NoMatchedService.getMsg(), "{}"));
        }
//        fillInvocationCtx(invocationCtx, req);

        AsyncJsonPost jsonPost = new AsyncJsonPost(service, version, method, true);

        try {
            return jsonPost.callServiceMethodAsync(parameter, bizService);
        } catch (SoaException e) {

            LOGGER.error(e.getMsg(), e);
            return CompletableFuture.completedFuture(String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", e.getCode(), e.getMsg(), "{}"));

        } catch (Exception e) {

            LOGGER.error(e.getMessage(), e);
            return CompletableFuture.completedFuture(String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", "9999", "系统繁忙，请稍后再试[9999]！", "{}"));
        } finally {
            if (clearInvocationContext) {
                InvocationContextImpl.Factory.removeCurrentInstance();
            }
        }
    }

    private static InvocationContext createInvocationCtx(String service, String version, String method, FullHttpRequest req) {
        InvocationContextImpl invocationCtx = (InvocationContextImpl) InvocationContextImpl.Factory.currentInstance();
        invocationCtx.serviceName(service);
        invocationCtx.versionName(version);
        invocationCtx.methodName(method);
        invocationCtx.callerMid(req.uri());
        if (!invocationCtx.sessionTid().isPresent()) {
            invocationCtx.sessionTid(DapengUtil.generateTid());
        }

        if (!invocationCtx.timeout().isPresent()) {
            int timeOut = getEnvTimeOut();
            if (timeOut > 0) {
                invocationCtx.timeout(timeOut);
            }
        }

        invocationCtx.codecProtocol(CodecProtocol.CompressedBinary);
        return invocationCtx;
    }

    /*private static void fillInvocationCtx(InvocationContext invocationCtx, FullHttpRequest req) {

        Set<String> parameters = req.getParameterMap().keySet();
        if (parameters.contains("calleeIp")) {
            invocationCtx.calleeIp(IPUtils.transferIp(req.getParameter("calleeIp")));
        }

        if (parameters.contains("calleePort")) {
            invocationCtx.calleePort(Integer.valueOf(req.getParameter("calleePort")));
        }

        if (parameters.contains("callerMid")) {
            invocationCtx.callerMid(req.getParameter("callerMid"));
        }

        if (parameters.contains("userId")) {
            invocationCtx.userId(Long.valueOf(req.getParameter("userId")));
        }

        if (parameters.contains("operatorId")) {
            invocationCtx.operatorId(Long.valueOf(req.getParameter("operatorId")));
        }

        InvocationContext.InvocationContextProxy invocationCtxProxy = InvocationContextImpl.Factory.getInvocationContextProxy();
        invocationCtx.cookies(invocationCtxProxy.cookies());
    }*/

    private static int getEnvTimeOut() {
        return SoaSystemEnvProperties.SOA_SERVICE_TIMEOUT.intValue();
    }
}

