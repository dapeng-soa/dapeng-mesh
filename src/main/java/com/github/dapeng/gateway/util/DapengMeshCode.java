package com.github.dapeng.gateway.util;

import com.github.dapeng.core.SoaBaseCodeInterface;

/**
 * desc: DapengMeshCode
 *
 * @author hz.lei
 * @since 2018年08月27日 下午11:20
 */
public enum DapengMeshCode implements SoaBaseCodeInterface {
    /**
     * IllegalRequest
     */
    IllegalRequest("Err-Mesh-501", "请求url不合法"),
    ProcessReqFailed("Err-Mesh-502", "网关错误,处理请求异常"),
    RequestUrlNotSupport("Err-Mesh-503", "网关不支持该请求Url"),
    RequestTypeNotSupport("Err-Mesh-504", "网关不支持该请求类型"),
    MeshUnknownError("Err-Mesh-505", "网关服务器出现未知错误"),
    MeshShutdownSoon("Err-Mesh-506", "health check is yellow,container will shutdown soon"),
    AuthSecretError("Err-Mesh-507", "网关鉴权失败,可能原因是不正确的apiKey或加密格式");


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
