package com.github.dapeng.gateway.http.match;

import com.github.dapeng.core.SoaException;
import com.github.dapeng.gateway.netty.request.PostRequestInfo;
import com.github.dapeng.gateway.netty.request.RequestContext;
import com.github.dapeng.gateway.netty.request.RequestParser;
import com.github.dapeng.gateway.util.Constants;
import com.github.dapeng.gateway.util.DapengMeshCode;
import com.github.dapeng.gateway.util.SysEnvUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author maple 2018.09.03 17:02  UrlMappingResolver
 */
public class UrlMappingResolverNew {
    private static Logger logger = LoggerFactory.getLogger(UrlMappingResolverNew.class);

    private static final Pattern POST_GATEWAY_PATTERN = Pattern.compile("/([^\\s|^/]*)/([^\\s|^/]*)/([^\\s|^/]*)/([^\\s|^/]*)(?:/([^\\s|^/]*))?");

    private static final Pattern POST_GATEWAY_PATTERN_1 = Pattern.compile("/([^\\s|^/]*)(?:/([^\\s|^/]*))?");

    private static final Pattern ECHO_PATTERN = Pattern.compile("/([^\\s|^/]*)/([^\\s|^/]*)/([^\\s|^/]*)/([^\\s|^/]*)");


    private static final String[] WILDCARD_CHARS = {"/", "?", "&", "="};

    private static final String DEFAULT_URL_PREFIX = "api";


    public static void handlerPostUrl(FullHttpRequest request, RequestContext context) {
        Matcher matcherFirst = POST_GATEWAY_PATTERN.matcher(request.uri());

        if (matcherFirst.matches()) {
            handlerMappingUrl(matcherFirst, request, context);
            return;
        }
        Matcher matcherSecond = POST_GATEWAY_PATTERN_1.matcher(request.uri());

        if (matcherSecond.matches()) {
            handlerRequestParam(matcherSecond, request, context);
            return;
        }

        context.isLegal(false);
        context.cause("no match is available");
    }


    /**
     * 解析 rest风格的请求,包括apiKey 和 没有 apiKey 的 请求
     * etc. /api/com.today.soa.idgen.service.IDService/1.0.0/genId/{apiKey}?cookie=234&user=maple
     * etc. /api/com.today.soa.idgen.service.IDService/1.0.0/genId
     *
     * @param request
     * @param context
     */
    private static void handlerMappingUrl(Matcher matcher, FullHttpRequest request, RequestContext context) {
        String prefix = matcher.group(1);
        String serviceName = matcher.group(2);
        String versionName = matcher.group(3);
        String methodName = matcher.group(4);
        String apiKey = matcher.group(5);

        if (apiKey == null) {
            UrlArgumentHolder holder = doResolveArgument(methodName);
            context.urlPrefix(prefix);
            context.service(serviceName);
            context.version(versionName);
            context.method(holder.getLastPath());

            context.arguments(holder.getArgumentMap());
            return;
        }
        UrlArgumentHolder holder = doResolveArgument(apiKey);

        String timestamp = RequestParser.fastParseParam(request, "timestamp");
        String secret = RequestParser.fastParseParam(request, "secret");
        String secret2 = RequestParser.fastParseParam(request, "secret2");
        String parameter = RequestParser.fastParseParam(request, "parameter");

        context.urlPrefix(prefix);
        context.service(serviceName);
        context.version(versionName);
        context.method(methodName);
        context.apiKey(holder.getLastPath());
        context.timestamp(timestamp);
        context.secret(secret);
        context.secret2(secret2);
        context.parameter(parameter);
        context.arguments(holder.getArgumentMap());
    }

    /**
     * 解析 requestParam 风格的请求,包括apiKey 和 没有 apiKey 的 请求
     * etc. /api/{apiKey}?cookie=234&user=maple
     * etc. /api?cookie=234&user=maple
     */
    private static void handlerRequestParam(Matcher matcher, FullHttpRequest request, RequestContext context) {
        String prefix = matcher.group(1);
        String apiKey = matcher.group(2);

        // prefix 必须以 api开头，否则为非法请求
        if (!prefix.equals(DEFAULT_URL_PREFIX)) {
            context.isLegal(false);
            context.cause("prefix 必须以 api开头");
            return;
        }

        if (apiKey == null) {
            UrlArgumentHolder holder = doResolveArgument(prefix);
            context.urlPrefix(holder.getLastPath());
            context.arguments(holder.getArgumentMap());
            RequestParser.fastParse(request, context);
            return;
        }
        UrlArgumentHolder holder = doResolveArgument(apiKey);

        context.urlPrefix(prefix);
        context.apiKey(holder.getLastPath());
        context.arguments(holder.getArgumentMap());

        RequestParser.fastParse(request, context);
    }

    /**
     * 处理 requestParam 风格url 不带 apiKey (无鉴权模式)
     * 根据系统变量 {@link SysEnvUtil#KEY_OPEN_AUTH_ENABLE} 判断是否支持此模式请求.
     */
    private static PostRequestInfo handlerParamNoAuthArgument(String prefix, FullHttpRequest request) throws SoaException {
        boolean authEnable = Boolean.valueOf(SysEnvUtil.OPEN_AUTH_ENABLE);
        if (authEnable) {
            throw new SoaException(DapengMeshCode.OpenAuthEnableError);
        }
        UrlArgumentHolder holder = doResolveArgument(prefix);
        return RequestParser.fastParse(holder.getLastPath(), null, request, holder.getArgumentMap());
    }


    /**
     * 解析url后携带参数,封装为 Map
     */
    private static UrlArgumentHolder doResolveArgument(String parameter) {
        try {
            // container ?
            int pos = parameter.lastIndexOf(WILDCARD_CHARS[1]);
            if (pos != -1) {
                String arguments = parameter.substring(pos + 1);
                if (arguments.contains(WILDCARD_CHARS[0])) {
                    return UrlArgumentHolder.onlyPathCreator(parameter);
                }

                UrlArgumentHolder holder = UrlArgumentHolder.nonPropertyCreator();
                Arrays.stream(arguments.split(WILDCARD_CHARS[2])).forEach(argument -> {
                    String[] arg = argument.split(WILDCARD_CHARS[3]);
                    holder.setArgument(arg[0], arg[1]);
                });
                holder.setLastPath(parameter.substring(0, pos));
                return holder;
            }
            return UrlArgumentHolder.onlyPathCreator(parameter);
        } catch (RuntimeException e) {
            logger.error("解析url参数错误, exception: {}, cause:" + e.getMessage(), e.getClass().getName());
            return UrlArgumentHolder.onlyPathCreator(parameter);
        }
    }

    /**
     * 解析 rest风格的 echo get 请求  /api/echo/{service}/{version}
     */
    public static Pair<String, String> handlerEchoUrl(String url) {
        Matcher matcher = ECHO_PATTERN.matcher(url);

        try {
            if (matcher.matches()) {
                String prefix = "/" + matcher.group(1) + "/" + matcher.group(2);

                if (!prefix.equals(Constants.ECHO_PREFIX)) {
                    return null;
                }
                String serviceName = matcher.group(3);
                String versionName = matcher.group(4);

                return new Pair<>(serviceName, versionName);
            }
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;

        }
    }

}
