package com.github.dapeng.gateway.netty.match;

import com.github.dapeng.gateway.netty.request.PostRequestInfo;
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

    private static final String[] WILDCARD_CHARS = {"/", "?", "&", "="};

    public static PostRequestInfo handlerMappingUrl(String url) {
        Matcher matcher = POST_GATEWAY_PATTERN.matcher(url);
        if (matcher.matches()) {
            String prefix = matcher.group(1);
            String serviceName = matcher.group(2);
            String versionName = matcher.group(3);
            String methodName = matcher.group(4);
            String apiKey = matcher.group(5);

            List<UrlArgumentHolder.KV> arguments;
            if (apiKey != null) {
                UrlArgumentHolder holder = resolveArgument(apiKey);
                arguments = holder.getArguments();
                apiKey = holder.getLastPath();
            } else {
                UrlArgumentHolder holder = resolveArgument(methodName);
                arguments = holder.getArguments();
                methodName = holder.getLastPath();
            }
            return new PostRequestInfo(prefix, serviceName, versionName, methodName, apiKey, arguments);
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
                    holder.setArgument(new UrlArgumentHolder.KV(arg[0], arg[1]));
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
