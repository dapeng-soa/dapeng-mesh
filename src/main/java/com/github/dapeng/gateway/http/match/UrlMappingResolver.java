package com.github.dapeng.gateway.http.match;

import com.github.dapeng.core.SoaException;
import com.github.dapeng.gateway.netty.request.PostRequestInfo;
import com.github.dapeng.gateway.netty.request.RequestParser;
import com.github.dapeng.gateway.util.Constants;
import com.github.dapeng.gateway.util.DapengMeshCode;
import com.github.dapeng.gateway.util.SysEnvUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author maple 2018.09.03 17:02  UrlMappingResolver
 */
public class UrlMappingResolver {
    private static Logger logger = LoggerFactory.getLogger(UrlMappingResolver.class);

    private static final Pattern POST_GATEWAY_PATTERN = Pattern.compile("/([^\\s|^/]*)/([^\\s|^/]*)/([^\\s|^/]*)/([^\\s|^/]*)(?:/([^\\s|^/]*))?");

    private static final Pattern POST_GATEWAY_PATTERN_1 = Pattern.compile("/([^\\s|^/]*)(?:/([^\\s|^/]*))?");

    private static final Pattern ECHO_PATTERN = Pattern.compile("/([^\\s|^/]*)/([^\\s|^/]*)/([^\\s|^/]*)/([^\\s|^/]*)");


    private static final String[] WILDCARD_CHARS = {"/", "?", "&", "="};

    private static final String DEFAULT_URL_PREFIX = "api";


    /**
     * 解析 rest风格的请求,包括apiKey 和 没有 apiKey 的 请求
     * etc. /api/com.today.soa.idgen.service.IDService/1.0.0/genId/{apiKey}?cookie=234&user=maple
     * etc. /api/com.today.soa.idgen.service.IDService/1.0.0/genId
     */
    public static PostRequestInfo handlerMappingUrl(String url, FullHttpRequest request) throws SoaException {
        Matcher matcher = POST_GATEWAY_PATTERN.matcher(url);
        if (matcher.matches()) {
            String prefix = matcher.group(1);
            String serviceName = matcher.group(2);
            String versionName = matcher.group(3);
            String methodName = matcher.group(4);
            String apiKey = matcher.group(5);

            if (apiKey == null) {
                return handlerRestNoAuthArgument(prefix, serviceName, versionName, methodName, request);
            }
            UrlArgumentHolder holder = doResolveArgument(apiKey);

            String timestamp = RequestParser.fastParseParam(request, "timestamp");
            String secret = RequestParser.fastParseParam(request, "secret");
            String secret2 = RequestParser.fastParseParam(request, "secret2");
            String parameter = RequestParser.fastParseParam(request, "parameter");


            return new PostRequestInfo(prefix, serviceName, versionName, methodName, holder.getLastPath(), timestamp, secret, secret2, parameter, holder.getArgumentMap());
        }
        return null;
    }


    /**
     * 处理 rest 风格url 不带 apiKey (无鉴权模式)
     * 根据系统变量 {@link SysEnvUtil#KEY_OPEN_AUTH_ENABLE} 判断是否支持此模式请求.
     */
    private static PostRequestInfo handlerRestNoAuthArgument(String prefix, String serviceName, String versionName, String methodName, FullHttpRequest request) throws SoaException {
        boolean authEnable = Boolean.valueOf(SysEnvUtil.OPEN_AUTH_ENABLE);
        if (authEnable) {
            throw new SoaException(DapengMeshCode.OpenAuthEnableError);
        }
        UrlArgumentHolder holder = doResolveArgument(methodName);


        return new PostRequestInfo(prefix, serviceName, versionName, holder.getLastPath(), null, holder.getArgumentMap());
    }


    /**
     * 解析 requestParam 风格的请求,包括apiKey 和 没有 apiKey 的 请求
     * etc. /api/{apiKey}?cookie=234&user=maple
     * etc. /api?cookie=234&user=maple
     */
    public static PostRequestInfo handlerRequestParam(String url, FullHttpRequest request) throws SoaException {
        Matcher matcher = POST_GATEWAY_PATTERN_1.matcher(url);
        if (matcher.matches()) {
            String prefix = matcher.group(1);
            String apiKey = matcher.group(2);

            // prefix 必须以 api开头，否则为非法请求
            if (!prefix.equals(DEFAULT_URL_PREFIX)) {
                return null;
            }

            if (apiKey == null) {
                return handlerParamNoAuthArgument(prefix, request);
            }

            UrlArgumentHolder holder = doResolveArgument(apiKey);
            Map<String, String> arguments = holder.getArgumentMap();
            apiKey = holder.getLastPath();
            return RequestParser.fastParse(prefix, apiKey, request, arguments);
        }
        return null;
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
                String prefix = matcher.group(1) + matcher.group(2);

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
