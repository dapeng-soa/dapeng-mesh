package com.github.dapeng.gateway;

import com.github.dapeng.gateway.config.ApiGateWayConfig;
import com.github.dapeng.gateway.netty.NettyHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc: HttpServerApplication
 *
 * @author hz.lei
 * @since 2018年08月23日 下午2:46
 */
public class HttpServerApplication {
    private static Logger logger = LoggerFactory.getLogger(HttpServerApplication.class);

    public static void main(String[] args) throws Exception {
        new ApiGateWayConfig().afterPropertiesSet();
        NettyHttpServer server = new NettyHttpServer(9000);
        logLogBanner();
        server.start();
    }


    private static void logLogBanner() {
        String builder =
                "\n\n ____                                                   __  __                _     " +
                "\n|  _ \\    __ _   _ __     ___   _ __     __ _          |  \\/  |   ___   ___  | |__  " +
                "\n| | | |  / _` | | '_ \\   / _ \\ | '_ \\   / _` |  _____  | |\\/| |  / _ \\ / __| | '_ \\ " +
                "\n| |_| | | (_| | | |_) | |  __/ | | | | | (_| | |_____| | |  | | |  __/ \\__ \\ | | | |" +
                "\n|____/   \\__,_| | .__/   \\___| |_| |_|  \\__, |         |_|  |_|  \\___| |___/ |_| |_|" +
                "\n                |_|                     |___/                                        " +
                "\n\n ";
        logger.info(builder);
    }
}
