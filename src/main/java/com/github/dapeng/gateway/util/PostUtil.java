package com.github.dapeng.gateway.util;

import com.github.dapeng.client.netty.JsonPost;
import com.github.dapeng.core.InvocationContext;
import com.github.dapeng.core.InvocationContextImpl;
import com.github.dapeng.core.SoaCode;
import com.github.dapeng.core.SoaException;
import com.github.dapeng.core.enums.CodecProtocol;
import com.github.dapeng.core.helper.DapengUtil;
import com.github.dapeng.core.helper.IPUtils;
import com.github.dapeng.core.helper.SoaSystemEnvProperties;
import com.github.dapeng.gateway.netty.request.RequestContext;
import com.github.dapeng.gateway.netty.request.RequestParser;
import com.github.dapeng.json.OptimizedMetadata;
import com.github.dapeng.openapi.cache.ServiceCache;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static com.github.dapeng.gateway.util.InvokeUtil.*;

/**
 * desc: PostUtil
 *
 * @author hz.lei
 * @since 2018年08月23日 下午2:29
 */
public class PostUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.github.dapeng.openapi.utils.PostUtil.class);

    public static Future<String> postAsync(RequestContext context) {
        preCheck(context);

        String parameter = context.parameter().get();
        String service = context.service().get();
        String version = context.version().get();
        String method = context.method().get();
        FullHttpRequest request = context.request();
        return doPostAsync(service, version, method, parameter, request, getCookiesFromParameter(context));
    }


    private static Future<String> doPostAsync(String service,
                                              String version,
                                              String method,
                                              String parameter,
                                              FullHttpRequest req,
                                              Map<String, String> cookies) {

        InvocationContextImpl invocationCtx = (InvocationContextImpl) createInvocationCtx(service, version, method, req, cookies);

        OptimizedMetadata.OptimizedService bizService = ServiceCache.getService(service, version);

        if (bizService == null) {
            LOGGER.error("bizService not found[service:" + service + ", version:" + version + "]");
            return CompletableFuture.completedFuture(String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", SoaCode.NoMatchedService.getCode(), SoaCode.NoMatchedService.getMsg(), "{}"));
        }
        fillInvocationCtx(invocationCtx, req);

        JsonPost jsonPost = new JsonPost(service, version, method, true);

        try {
            return jsonPost.callServiceMethodAsync(parameter, bizService);
        } catch (SoaException e) {
            LOGGER.error(e.getMsg(), e);
            return CompletableFuture.completedFuture(String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", e.getCode(), e.getMsg(), "{}"));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return CompletableFuture.completedFuture(String.format("{\"responseCode\":\"%s\", \"responseMsg\":\"%s\", \"success\":\"%s\", \"status\":0}", "9999", "系统繁忙，请稍后再试[9999]！", "{}"));
        } finally {
            InvocationContextImpl.Factory.removeCurrentInstance();
        }
    }

    /**
     * 同步 postSync
     *
     * @throws Exception throw exception
     */
    public static String postSync(String service,
                                  String version,
                                  String method,
                                  String parameter,
                                  FullHttpRequest req,
                                  Map<String, String> cookies) throws Exception {
        InvocationContextImpl invocationCtx = (InvocationContextImpl) createInvocationCtx(service, version, method, req, cookies);

        OptimizedMetadata.OptimizedService bizService = ServiceCache.getService(service, version);

        if (bizService == null) {
            LOGGER.error("bizService not found[service:" + service + ", version:" + version + "]");
            throw new SoaException(SoaCode.NoMatchedService);
        }

        fillInvocationCtx(invocationCtx, req);

        JsonPost jsonPost = new JsonPost(service, version, method, true);
        try {
            jsonPost.callServiceMethod(parameter, bizService);
            return invocationCtx.lastInvocationInfo().responseCode();
        } finally {
            InvocationContextImpl.Factory.removeCurrentInstance();
        }
    }

    private static InvocationContext createInvocationCtx(String service,
                                                         String version,
                                                         String method,
                                                         FullHttpRequest req,
                                                         Map<String, String> cookies) {
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

        if (!cookies.isEmpty()) {
            invocationCtx.cookies(cookies);
        }
        invocationCtx.codecProtocol(CodecProtocol.CompressedBinary);
        return invocationCtx;
    }

    private static void fillInvocationCtx(InvocationContext invocationCtx, FullHttpRequest req) {
        Map<String, List<String>> parameters = RequestParser.fastParseToMap(req);
        if (parameters.containsKey("calleeIp")) {
            invocationCtx.calleeIp(IPUtils.transferIp(parameters.get("calleeIp").get(0)));
        }

        if (parameters.containsKey("calleePort")) {
            invocationCtx.calleePort(Integer.valueOf(parameters.get("calleePort").get(0)));
        }

        if (parameters.containsKey("callerMid")) {
            invocationCtx.callerMid(parameters.get("callerMid").get(0));
        }

        if (parameters.containsKey("userId")) {
            invocationCtx.userId(Long.valueOf(parameters.get("userId").get(0)));
        }

        if (parameters.containsKey("operatorId")) {
            invocationCtx.operatorId(Long.valueOf(parameters.get("operatorId").get(0)));
        }

        InvocationContext.InvocationContextProxy invocationCtxProxy = InvocationContextImpl.Factory.getInvocationContextProxy();
        invocationCtx.cookies(invocationCtxProxy.cookies());
    }

    private static int getEnvTimeOut() {
        return (int) SoaSystemEnvProperties.SOA_SERVICE_TIMEOUT;
    }


    private static void preCheck(RequestContext context) {
        // parameter 会空
        asserts(context.parameter(), "parameter");
        asserts(context.service(), "service");
        asserts(context.version(), "version");
        asserts(context.method(), "method");
    }

    private static <T> void asserts(Optional<T> value, String message) {
        if (!value.isPresent()) {
            throw new IllegalArgumentException("请求参数 (" + message + ") 不能为空!");
        }
    }
}

