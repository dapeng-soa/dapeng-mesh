package com.github.dapeng.gateway.util;

import com.github.dapeng.core.SoaBaseCodeInterface;

/**
 * desc: DapengMeshCode
 *
 * @author hz.lei
 * @since 2018年08月27日 下午11:20
 */
public enum DapengMeshCode implements SoaBaseCodeInterface {

    // 客户端
    IllegalRequest("Err-Mesh-501", "请求url不合法"),

    ProcessReqFailed("Err-Mesh-502", "dapengMesh处理请求失败"),
    RequestTypeNotSupport("Err-Mesh-503", "网关不支持该请求类型"),
    GateWayUnknownError("Err-Mesh-504", "网关服务器出现未知错误");

    private String code;
    private String msg;

    DapengMeshCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return code + ":" + msg;
    }

}
