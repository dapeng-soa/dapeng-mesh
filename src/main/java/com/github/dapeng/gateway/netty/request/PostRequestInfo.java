package com.github.dapeng.gateway.netty.request;

import com.github.dapeng.gateway.netty.match.UrlArgumentHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * desc: TODO
 *
 * @author hz.lei
 * @since 2018年08月24日 下午3:27
 */
public class PostRequestInfo {
    private String prefix;
    private String service;
    private String version;
    private String method;
    private String apiKey;
    private List<UrlArgumentHolder.KV> arguments = new ArrayList<>();

    public PostRequestInfo(String prefix, String service, String version, String method, String apiKey, List<UrlArgumentHolder.KV> arguments) {
        this.prefix = prefix;
        this.service = service;
        this.version = version;
        this.method = method;
        this.apiKey = apiKey;
        this.arguments.addAll(arguments);
    }

    public String getPrefix() {
        return prefix;
    }

    public String getService() {
        return service;
    }

    public String getVersion() {
        return version;
    }

    public String getMethod() {
        return method;
    }

    public String getApiKey() {
        return apiKey;
    }

    public List<UrlArgumentHolder.KV> getArguments() {
        return arguments;
    }

    public String getArgumentString() {
        return arguments.stream()
                .map(argument -> "KV:[" + argument.argKey + " -> " + argument.argValue + "]")
                .collect(Collectors.joining(","));
    }
}
