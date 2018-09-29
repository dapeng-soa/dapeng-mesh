package com.github.dapeng.gateway.util;

import com.github.dapeng.gateway.netty.request.RequestContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author with struy.
 * Create by 2018/5/9 15:10
 * email :yq1724555319@gmail.com
 */
public class InvokeUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.github.dapeng.openapi.utils.PostUtil.class);

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

    /**
     * 处理 url 后缀携带的 cookie 参数
     * 处理http 传递过来的 也没 cookie 值
     *
     * @param context request 上下文
     */
    public static Map<String, String> getCookiesFromParameter(RequestContext context) {
        Map<String, String> cookies = new HashMap<>(16);
        if (context == null) {
            return cookies;
        }
        String cookieStoreId = context.arguments().get("cookie_storeId");
        if (cookieStoreId != null) {
            cookies.put("storeId", cookieStoreId);
        }
        String cookiePosId = context.arguments().get("cookie_posId");
        if (cookiePosId != null) {
            cookies.put("posId", cookiePosId);
        }
        String cookieOperatorId = context.arguments().get("cookie_operatorId");
        if (cookieOperatorId != null) {
            cookies.put("operatorId", cookieOperatorId);
        }

        //process http Cookies
        Set<Cookie> httpCookies = context.cookies();
        if (httpCookies != null && httpCookies.size() > 0) {
            //设置通过 http 传入的 cookies ,需要前缀为 COOKIES_PREFIX
            httpCookies.forEach(httpCookie -> {
                String key = httpCookie.name();
                if (key.startsWith(Constants.COOKIES_PREFIX)) {
                    cookies.put(key.substring(Constants.COOKIES_PREFIX.length()), httpCookie.value());
                }
            });
        }
        return cookies;
    }

    /**
     * 获取 Http 的 Cookies 值
     */
    public static Map<String, String> getHttpCookies(Set<Cookie> cookies) {


        Map<String, String> cookieMap = new HashMap<>();
        if (cookies == null || cookies.size() == 0) {
            return cookieMap;
        }
        StringBuilder builder = new StringBuilder();
        cookies.forEach(cookie -> builder.append("cookie-> ").append(cookie.name()).append(":").append(cookie.value()));
        LOGGER.info("httpCookies: {}", builder.toString());

        //设置通过 http 传入的 cookies ,需要前缀为 COOKIES_PREFIX
        cookies.forEach(cookie -> {
            String key = cookie.name();
            if (key.startsWith(Constants.COOKIES_PREFIX)) {
                cookieMap.put(key.substring(key.indexOf("_") + 1), cookie.value());
            }
        });
        return cookieMap;
    }
}
