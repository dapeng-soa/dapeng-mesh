package com.github.dapeng.gateway.util;

import com.github.dapeng.core.InvocationContext;
import com.github.dapeng.core.InvocationContextImpl;
import com.github.dapeng.core.helper.DapengUtil;
import com.github.dapeng.core.helper.IPUtils;
import com.github.dapeng.gateway.netty.request.PostRequestInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.Map;
import java.util.Optional;

/**
 * @author maple 2018.09.04 下午3:11
 */
public class SoaInvocationProxy implements InvocationContext.InvocationContextProxy {
    private final FullHttpRequest request;
    private final PostRequestInfo info;
    private final ChannelHandlerContext ctx;


    public SoaInvocationProxy(FullHttpRequest request, PostRequestInfo info, ChannelHandlerContext ctx) {
        this.request = request;
        this.info = info;
        this.ctx = ctx;
    }

    @Override
    public Optional<Long> sessionTid() {
        return Optional.of(InvocationContextImpl.Factory.currentInstance().sessionTid().orElse(DapengUtil.generateTid()));
    }

    @Override
    public Optional<Integer> userIp() {

        if (request == null) {
            return Optional.of(IPUtils.localIpAsInt());
        }
        String ip = InvokeUtil.getIpAddress(request, ctx);

        return Optional.ofNullable(IPUtils.transferIp(ip));
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
        if (request == null) {
            return Optional.of("apiGateWay");
        }
        return Optional.ofNullable(request.uri());
    }

    @Override
    public Map<String, String> cookies() {
        return InvokeUtil.getCookiesFromParameter(info);
    }
}
