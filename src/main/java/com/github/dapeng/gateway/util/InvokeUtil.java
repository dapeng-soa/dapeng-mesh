package com.github.dapeng.gateway.util;

import com.github.dapeng.gateway.netty.request.PostRequestInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author with struy.
 * Create by 2018/5/9 15:10
 * email :yq1724555319@gmail.com
 */
public class InvokeUtil {
    /**
     * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
     * <p>
     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
     * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
     * <p>
     * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130,
     * 192.168.1.100
     * <p>
     * 用户真实IP为： 192.168.1.110
     *
     * @return
     */
    public static String getIpAddress(FullHttpRequest request, ChannelHandlerContext ctx) {
        HttpHeaders headers = request.headers();

        String ip = headers.get("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.get("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.get("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.get("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.get("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getHostString();
        }
        return !ip.contains(",") ? ip : ip.split(",")[0];
    }


    public static Map<String, String> getCookies(PostRequestInfo info) {
        Map<String, String> cookies = new HashMap<>(16);
        if (info == null) {
            return cookies;
        }
        String cookieStoreId = info.getArguments().get("cookie_storeId");
        if (cookieStoreId != null) {
            cookies.put("storeId", cookieStoreId);
        }
        String cookiePosId = info.getArguments().get("cookie_posId");
        if (cookiePosId != null) {
            cookies.put("posId", cookiePosId);
        }
        String cookieOperatorId = info.getArguments().get("cookie_operatorId");
        if (cookieOperatorId != null) {
            cookies.put("operatorId", cookieOperatorId);
        }
        return cookies;
    }
}
