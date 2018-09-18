package com.github.dapeng.gateway.util;

import com.github.dapeng.core.InvocationContext;
import com.github.dapeng.core.InvocationContextImpl;
import com.github.dapeng.core.helper.DapengUtil;
import com.github.dapeng.core.helper.IPUtils;
import com.github.dapeng.gateway.netty.request.PostRequestInfo;
import com.github.dapeng.gateway.netty.request.RequestContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Map;
import java.util.Optional;

/**
 * @author maple 2018.09.04 下午3:11
 */
public class SoaInvocationProxy implements InvocationContext.InvocationContextProxy {
    private final RequestContext context;
    private final ChannelHandlerContext ctx;


    public SoaInvocationProxy(RequestContext context, ChannelHandlerContext ctx) {
        this.context = context;
        this.ctx = ctx;
    }

    @Override
    public Optional<Long> sessionTid() {
        return Optional.of(InvocationContextImpl.Factory.currentInstance().sessionTid().orElse(DapengUtil.generateTid()));
    }

    @Override
    public Optional<Integer> userIp() {

        if (context.request() == null) {
            return Optional.of(IPUtils.localIpAsInt());
        }
        String ip = InvokeUtil.getIpAddress(context.request(), ctx);

        return Optional.of(IPUtils.transferIp(ip));
    }

    @Override
    public Optional<Long> userId() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> operatorId() {
        return Optional.empty();
    }

    @Override
    public Optional<String> callerMid() {
        if (context.request() == null) {
            return Optional.of("apiGateWay");
        }
        return Optional.ofNullable(context.requestUrl());
    }

    @Override
    public Map<String, String> cookies() {
        return InvokeUtil.getCookiesFromParameter(context);
    }
}
