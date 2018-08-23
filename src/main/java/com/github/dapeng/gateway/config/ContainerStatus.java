package com.github.dapeng.gateway.config;

/**
 * desc: tomcat容器状态
 *
 * @author hz.lei
 * @since 2018年06月30日 下午5:26
 */
public enum ContainerStatus {

    YELLOW(500),

    GREEN(200);

    private int status;

    ContainerStatus(int status) {
        this.status = status;
    }

    public static ContainerStatus findByValue(int value) {
        switch (value) {
            case 500:
                return YELLOW;

            case 200:
                return GREEN;
            default:
                return null;
        }
    }
}
