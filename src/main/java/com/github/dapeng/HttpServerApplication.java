package com.github.dapeng;

import com.github.dapeng.config.ApiGateWayConfig;

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
