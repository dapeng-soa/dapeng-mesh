package com.github.dapeng.gateway.util;

/**
 * desc: Constants
 *
 * @author hz.lei
 * @since 2018年08月27日 下午10:37
 */
public class Constants {

    public static final int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() * 2, 32);


    public static final String GET_HEALTH_CHECK_URL = "/handlerHealth/check";


    public static final String GET_CHECK = "/";

    public static final String RESP_STATUS = "status";




}
