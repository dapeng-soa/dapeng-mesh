package com.github.dapeng;

/**
 * desc: NettyStartServer
 *
 * @author hz.lei
 * @since 2018年08月23日 上午10:21
 */
public class NettyStartServer {

    public static void main(String[] args) {
        NettyHttpServer server = new NettyHttpServer(9000);
        server.start();
    }

}
