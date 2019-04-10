package com.github.dapeng.gateway.config;

import com.github.dapeng.openapi.cache.ZkBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author maple 2018.06.21 10:40 zk metadata fetch init
 */
public class ApiGateWayConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiGateWayConfig.class);
    private static final String ENV_SOA_ZOOKEEPER_HOST = "soa_zookeeper_host";

    private static final String PROP_SOA_ZOOKEEPER_HOST = "soa.zookeeper.host";

    /**
     * new ZkBootstrap().init();
     *
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        if (System.getenv(ENV_SOA_ZOOKEEPER_HOST) != null
                || System.getProperty(PROP_SOA_ZOOKEEPER_HOST) != null) {
            LOGGER.info("zk host in the environment is already setter...");
        } else {
            String defaultHost = "127.0.0.1:2181";
            System.setProperty(PROP_SOA_ZOOKEEPER_HOST, defaultHost);
            LOGGER.info("zk host in the environment is not found,setting it with spring boot application, host is {}", defaultHost);
        }

       // new ZkBootstrap().filterInitWhiteList(WhiteListHandler.initWhiteList());
        new ZkBootstrap().init();
    }
}
