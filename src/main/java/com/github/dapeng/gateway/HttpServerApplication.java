package com.github.dapeng.gateway;

import com.github.dapeng.gateway.config.ApiGateWayConfig;
import com.github.dapeng.gateway.netty.NettyHttpServer;

/**
 * desc: HttpServerApplication
 *
 * @author hz.lei
 * @since 2018年08月23日 下午2:46
 */
public class HttpServerApplication {

    public static void main(String[] args) throws Exception {
        new ApiGateWayConfig().afterPropertiesSet();
        NettyHttpServer server = new NettyHttpServer(9000);
        server.start();


    }
}
