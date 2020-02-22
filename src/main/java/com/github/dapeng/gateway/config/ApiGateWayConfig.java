package com.github.dapeng.gateway.config;

import com.github.dapeng.gateway.auth.WhiteListHandler;
import com.github.dapeng.openapi.cache.ZkBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.dapeng.gateway.util.Constants.DEFAULT_ZOOKEEPER_HOST;
import static com.github.dapeng.gateway.util.SysEnvUtil.*;


/**
 * @author maple 2018.06.21 10:40 zk metadata fetch init
 */
public class ApiGateWayConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiGateWayConfig.class);


    /**
     * new ZkBootstrap().init();
     */
    public void afterPropertiesSet() {
        if (System.getenv(KEY_ENV_SOA_ZOOKEEPER_HOST) != null
                || System.getProperty(KEY_SOA_ZOOKEEPER_HOST) != null) {
            LOGGER.info("Zookeeper host in the environment is already setter...");
        } else {
            String defaultHost = DEFAULT_ZOOKEEPER_HOST;
            System.setProperty(KEY_SOA_ZOOKEEPER_HOST, defaultHost);
            LOGGER.info("zk host in the environment is not found,setting it with spring boot application, host is {}", defaultHost);
        }

        if (Boolean.parseBoolean(WHITE_LIST_ENABLE)) {
            new ZkBootstrap().filterInitWhiteList(WhiteListHandler.initWhiteList());
        } else {
            new ZkBootstrap().init();
        }


    }
}
