package com.github.dapeng.gateway.util;


/**
 * desc: 系统环境变量读取
 *
 * @author hz.lei
 * @since 2018年08月23日 下午4:39
 */
public class SysEnvUtil {
    private static final String KEY_SOA_ZOOKEEPER_HOST = "soa.zookeeper.host";
    private static final String KEY_OPEN_AUTH_ENABLE = "soa.open.auth.enable";


    public static final String SOA_ZOOKEEPER_HOST = get(KEY_SOA_ZOOKEEPER_HOST, "");
    /**
     * 默认开启open接口鉴权
     */
//    public static final String OPEN_AUTH_ENABLE = get(KEY_OPEN_AUTH_ENABLE, "true");
    public static final String OPEN_AUTH_ENABLE = get(KEY_OPEN_AUTH_ENABLE, "false");


    private static String get(String key, String defaultValue) {
        String envValue = System.getenv(key.replaceAll("\\.", "_"));

        if (envValue == null) {
            return System.getProperty(key, defaultValue);
        }

        return envValue;
    }

}
