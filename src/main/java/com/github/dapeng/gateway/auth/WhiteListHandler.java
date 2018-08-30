package com.github.dapeng.gateway.auth;

import com.github.dapeng.openapi.cache.ZookeeperClient;
import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

/**
 * @author with struy.
 * Create by 2018/5/12 00:07
 * email :yq1724555319@gmail.com
 */

public class WhiteListHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(WhiteListHandler.class);
    private static Persister persister = null;

    public static Set<String> getServiceWhiteList() {
        return ZookeeperClient.getWhitelist();
    }

    /**
     * 初始化白名单
     *
     * @return
     */
    public static Set<String> initWhiteList() {
        if (persister == null) {
            persister = new Persister();
        }
        FileInputStream inputStream = null;
        try {
            //==images==//
            inputStream = new FileInputStream("/gateway-conf/service-whitelist.xml");
            Set<String> services = persister.read(
                    ServiceWhiteList.class, inputStream)
                    .getService();
            LOGGER.info("load service-whitelist.xml on [/gateway-conf] current whitelist [{}]", services.size());
            return services;
        } catch (FileNotFoundException e) {
            LOGGER.warn("read file system NotFound [/gateway-conf/service-whitelist.xml],found conf file [service-whitelist.xml] on classpath");
            try {
                //==develop==//
                Set<String> services = persister.read(
                        ServiceWhiteList.class,
                        ResourceUtils.getFile("classpath:service-whitelist.xml"))
                        .getService();
                LOGGER.info("load service-whitelist.xml on [classpath] current whitelist [{}]", services.size());
                return services;
            } catch (FileNotFoundException e1) {
                LOGGER.error("service-whitelist.xml in [classpath] and [/gateway-conf/] NotFound, please Settings", e);
                throw new RuntimeException("service-whitelist.xml in [classpath] and [/gateway-conf/] NotFound, please Settings");
            } catch (Exception e1) {
                LOGGER.error("初始化白名单错误!", e1);
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("初始化白名单错误!", e);
            return null;
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }
}
