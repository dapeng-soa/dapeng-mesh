package com.github.dapeng.gateway.http;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author maple 2018.08.28 下午4:23
 */
public class HttpResponseEntity {
    private String content;
    private HttpResponseStatus status;

    public HttpResponseEntity(String content, HttpResponseStatus status) {
        this.content = content;
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }
}
