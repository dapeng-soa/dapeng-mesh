package com.github.dapeng.gateway.netty.match;

import com.github.dapeng.gateway.netty.request.PostRequestInfo;
import com.github.dapeng.gateway.netty.request.RequestParser;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * desc: UrlMappingResolver
 *
 * @author hz.lei
 * @since 2018年08月24日 下午1:23
 */
public class UrlMappingResolver {
    private static Logger logger = LoggerFactory.getLogger(UrlMappingResolver.class);

    /**
     * Default path separator: "/"
     */
    public static final String DEFAULT_PATH_SEPARATOR = "/";

    private static final Pattern POST_GATEWAY_PATTERN = Pattern.compile("/([^\\s|^/]*)/([^\\s|^/]*)/([^\\s|^/]*)/([^\\s|^/]*)(?:/([^\\s|^/]*))?");


    private static final Pattern POST_GATEWAY_PATTERN_1 = Pattern.compile("/([^\\s|^/]*)(?:/([^\\s|^/]*))?");

    private static final String[] WILDCARD_CHARS = {"/", "?", "&", "="};

    public static PostRequestInfo handlerMappingUrl(String url) {
        Matcher matcher = POST_GATEWAY_PATTERN.matcher(url);
        if (matcher.matches()) {
            String prefix = matcher.group(1);
            String serviceName = matcher.group(2);
            String versionName = matcher.group(3);
            String methodName = matcher.group(4);
            String apiKey = matcher.group(5);

            Map<String, String> argumentMap;

            if (apiKey != null) {
                UrlArgumentHolder holder = resolveArgument(apiKey);
                argumentMap = holder.getArgumentMap();
                apiKey = holder.getLastPath();
            } else {
                UrlArgumentHolder holder = resolveArgument(methodName);
                argumentMap = holder.getArgumentMap();
                methodName = holder.getLastPath();
            }
            return new PostRequestInfo(prefix, serviceName, versionName, methodName, apiKey, argumentMap);
        }
        return null;
    }

    /**
     * 得到 param参数
     *
     * @param url
     * @param request
     * @return
     */
    public static PostRequestInfo handlerRequestParam(String url, FullHttpRequest request) {
        Matcher matcher = POST_GATEWAY_PATTERN_1.matcher(url);
        if (matcher.matches()) {
            String prefix = matcher.group(1);
            String apiKey = matcher.group(2);

            Map<String, String> arguments;
            if (apiKey != null) {
                UrlArgumentHolder holder = resolveArgument(apiKey);
                arguments = holder.getArgumentMap();
                apiKey = holder.getLastPath();
            } else {
                UrlArgumentHolder holder = resolveArgument(prefix);
                arguments = holder.getArgumentMap();
                prefix = holder.getLastPath();
            }
            return RequestParser.fastParse(prefix, apiKey, request, arguments);
        }
        return null;
    }



    private static UrlArgumentHolder resolveArgument(String parameter) {
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
}
