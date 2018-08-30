package com.github.dapeng.gateway.netty.request;

import com.github.dapeng.gateway.netty.match.UrlArgumentHolder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * desc: 解析 httpRequest请求参数.
 *
 * @author hz.lei
 * @since 2018年08月23日 上午10:01
 */
public final class RequestParser {

    private RequestParser() {
    }

    /**
     * 解析请求参数
     */
    public static Map<String, String> parse(FullHttpRequest req) {
        Map<String, String> params = new HashMap<>();
        // 是POST请求
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), req);
        List<InterfaceHttpData> postList = decoder.getBodyHttpDatas();
        for (InterfaceHttpData data : postList) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                params.put(attribute.getName(), attribute.getValue());
            }
        }
        // resolve memory leak
        decoder.destroy();
        return params;
    }

    /**
     * parse http params
     */
    public static PostRequestInfo fastParse(String prefix, String apiKey, FullHttpRequest httpRequest, Map<String, String> arguments) {
        String content = httpRequest.content().toString(StandardCharsets.UTF_8);
        QueryStringDecoder qs = new QueryStringDecoder(content, StandardCharsets.UTF_8, false);
        Map<String, List<String>> parameters = qs.parameters();

        List<String> defaultStr = new ArrayList<>();

        defaultStr.add("");
        String serviceName = parameters.getOrDefault("serviceName", defaultStr).get(0);
        String version = parameters.getOrDefault("version", defaultStr).get(0);
        String methodName = parameters.getOrDefault("methodName", defaultStr).get(0);

        String parameter = parameters.getOrDefault("parameter", defaultStr).get(0);

        String timestamp = parameters.getOrDefault("timestamp", defaultStr).get(0);

        String secret = parameters.getOrDefault("secret", defaultStr).get(0);
        String secret2 = parameters.getOrDefault("secret2", defaultStr).get(0);

        return new PostRequestInfo(prefix, serviceName, version, methodName, apiKey, timestamp, secret, secret2, parameter, arguments);
    }


    /**
     * 解析 http 请求携带参数
     *
     * @param httpRequest
     * @param condition
     * @return
     */
    public static String fastParseParam(FullHttpRequest httpRequest, String condition) {
        String content = httpRequest.content().toString(StandardCharsets.UTF_8);
        QueryStringDecoder qs = new QueryStringDecoder(content, StandardCharsets.UTF_8, false);
        Map<String, List<String>> parameters = qs.parameters();
        String value = parameters.get(condition).get(0);

        return value;
    }


    /**
     * parse http params
     */
    public static Map<String, List<String>> fastParseToMap(FullHttpRequest httpRequest) {
        String content = httpRequest.content().toString(StandardCharsets.UTF_8);
        QueryStringDecoder qs = new QueryStringDecoder(content, StandardCharsets.UTF_8, false);
        return qs.parameters();
    }


}