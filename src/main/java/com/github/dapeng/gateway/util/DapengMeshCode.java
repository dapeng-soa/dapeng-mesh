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
    AuthSecretError("Err-Mesh-507", "网关鉴权失败,可能原因是不正确的apiKey或加密格式"),
    OpenAuthEnableError("Err-Mesh-508", "网关接口需要鉴权,未开放无需鉴权功能"),
    EchoUnknowEx("Err-Mesh-509", "Echo接口请求url格式不正确"),
    MeshUnknowEx("Err-Mesh-510", "ApiMesh未知异常"),
    AuthParameterEx("Err-Mesh-511", "请求校验参数为空,请检查 api-key,timestamp"),
    AuthSecretEx("Err-Mesh-512", "请求校验参数secret,secret2 至少有一个不为空"),
    ParameterError("Err-Mesh-513", "请求参数 Request 部分参数不能为空"),
    IpLimiterError("Err-Mesh-514", "[被限流]请求过于频繁");


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
