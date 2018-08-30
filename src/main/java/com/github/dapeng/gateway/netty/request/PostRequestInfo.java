package com.github.dapeng.gateway.netty.request;


import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * desc: PostRequestInfo
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

    private String timestamp;
    private String secret;
    private String secret2;

    private String parameter;


    private Map<String, String> arguments = new HashMap<>();

    public PostRequestInfo(String prefix, String service, String version, String method, String apiKey, Map<String, String> arguments) {
        this.prefix = prefix;
        this.service = service;
        this.version = version;
        this.method = method;
        this.apiKey = apiKey;
        this.arguments.putAll(arguments);
    }

    public PostRequestInfo(String prefix, String service, String version, String method,
                           String apiKey, String timestamp, String secret, String secret2,
                           String parameter, Map<String, String> arguments) {
        this.prefix = prefix;
        this.service = service;
        this.version = version;
        this.method = method;
        this.apiKey = apiKey;
        this.timestamp = timestamp;
        this.secret = secret;
        this.secret2 = secret2;
        this.parameter = parameter;
        this.arguments = arguments;
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

    public Map<String, String> getArguments() {
        return arguments;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSecret() {
        return secret;
    }

    public String getSecret2() {
        return secret2;
    }

    public String getParameter() {
        return parameter;
    }

    public String getArgumentString() {
        return arguments.entrySet().stream()
                .map(argument -> "KV:[" + argument.getKey() + " -> " + argument.getValue() + "]")
                .collect(Collectors.joining(","));
    }
}
