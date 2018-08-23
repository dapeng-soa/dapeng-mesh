package com.github.dapeng.gateway.util;


/**
 * desc: 系统环境变量读取
 *
 * @author hz.lei
 * @since 2018年08月23日 下午4:39
 */
public class SysEnvUtil {

    public static final String KEY_SOA_ZOOKEEPER_HOST = "soa.zookeeper.host";


    public static final String SOA_ZOOKEEPER_HOST = get(KEY_SOA_ZOOKEEPER_HOST, "");


    public static String get(String key, String defaultValue) {
        String envValue = System.getenv(key.replaceAll("\\.", "_"));

        if (envValue == null) {
            return System.getProperty(key, defaultValue);
        }

        return envValue;
    }

}
