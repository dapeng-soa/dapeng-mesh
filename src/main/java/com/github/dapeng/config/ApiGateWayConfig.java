package com.github.dapeng.config;

import com.github.dapeng.openapi.cache.ZkBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc: zk metadata fetch init
 *
 * @author hz.lei
 * @since 2018年06月21日 上午10:40
 */
public class ApiGateWayConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiGateWayConfig.class);
    public static final String ENV_SOA_ZOOKEEPER_HOST = "soa_zookeeper_host";

    public static final String PROP_SOA_ZOOKEEPER_HOST = "soa.zookeeper.host";


    public void afterPropertiesSet() throws Exception {
        if (System.getenv(ENV_SOA_ZOOKEEPER_HOST) != null
                || System.getProperty(PROP_SOA_ZOOKEEPER_HOST) != null) {
            LOGGER.info("zk host in the environment is already setter...");
        } else {
            String defaultHost = "127.0.0.1:2181";
            System.setProperty(PROP_SOA_ZOOKEEPER_HOST, defaultHost);
            LOGGER.info("zk host in the environment is not found,setting it with spring boot application, host is {}", defaultHost);
        }
        new ZkBootstrap().init();
    }
}
