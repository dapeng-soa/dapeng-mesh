package com.github.dapeng.gateway.http;

/**
 * MeshHealthStatus 网关 健康状况
 *
 * @author maple 2018.08.28 下午3:21
 */
public enum MeshHealthStatus {
    /**
     * 不健康的运行状态
     */
    YELLOW(500),
    /**
     * 正常运行状态
     */
    GREEN(200);

    private int status;

    MeshHealthStatus(int status) {
        this.status = status;
    }

    public static MeshHealthStatus findByValue(int value) {
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
