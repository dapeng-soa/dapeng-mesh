package com.github.dapeng.gateway.auth;

import org.simpleframework.xml.core.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huyj
 * @Created 2019-04-02 10:31
 */
public class AuthWhiteUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(AuthWhiteUtils.class);
    private static List<String> authWhiteListConfig;

    /**
     * 加载鉴权白名单
     */
    public static void loadAuthWhiteConfig() {
        authWhiteListConfig = null;
        Persister persister = new Persister();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream("/dapeng-mesh/auth-whitelist.xml");
            AuthWhiteList authWhiteList = persister.read(AuthWhiteList.class, inputStream);
            authWhiteListConfig = initWhiteList(authWhiteList);
            LOGGER.info("load auth-whitelist.xml on [/gateway-conf] current whitelist [{}]", authWhiteListConfig.size());
        } catch (FileNotFoundException e) {
            LOGGER.warn("read file system NotFound [/dapeng-mesh/auth-whitelist.xml],found conf file [auth-whitelist.xml] on classpath");
            try {
                //==develop==//
                AuthWhiteList authWhiteList = persister.read(AuthWhiteList.class, ResourceUtils.getFile("classpath:auth-whitelist.xml"));
                authWhiteListConfig = initWhiteList(authWhiteList);
                LOGGER.info("load service-whitelist.xml on [classpath] current whitelist [{}]", authWhiteListConfig.size());
            } catch (FileNotFoundException e1) {
                LOGGER.error("auth-whitelist.xml in [classpath] and [/gateway-conf/] NotFound, please Settings", e);
                throw new RuntimeException("auth-whitelist.xml in [classpath] and [/gateway-conf/] NotFound, please Settings");
            } catch (Exception e1) {
                LOGGER.error("初始化白名单错误!", e1);
            }
        } catch (Exception e) {
            LOGGER.error("初始化白名单错误!", e);
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        LOGGER.info("加载白名单数据成功！");
    }


    private static List<String> initWhiteList(AuthWhiteList authWhiteList) {
        List<String> whiteList = new ArrayList<>();
        if (authWhiteList != null && !authWhiteList.getServices().isEmpty()) {
            authWhiteList.getServices().forEach(service -> {
                if (service.getMethods() != null && !service.getMethods().isEmpty()) {
                    service.getMethods().forEach(method -> whiteList.add(service.getServiceName() + "." + method.getMethodName()));
                } else {
                    whiteList.add(service.getServiceName() + ".*");
                }
            });
        }
        return whiteList;
    }


    public static boolean isNeedAuth(String serviceName, String methodName) {
        if (authWhiteListConfig != null && !authWhiteListConfig.isEmpty()) {
            return !(authWhiteListConfig.contains(serviceName + ".*") || authWhiteListConfig.contains(serviceName + "." + methodName));
        }
        return true;
    }
}
