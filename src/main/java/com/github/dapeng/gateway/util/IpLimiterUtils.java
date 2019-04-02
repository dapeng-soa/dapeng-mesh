package com.github.dapeng.gateway.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * ip 限流
 *
 * @author huyj
 * @Created 2019-04-02 14:08
 */
public class IpLimiterUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(IpLimiterUtils.class);

    private static Long REQUEST_COUNT_PERIOD = 500L;
    private static LoadingCache<String, RateLimiter> ipLimiterCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            // 根据IP分不同的令牌桶, 每天自动清理缓存
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build(new CacheLoader<String, RateLimiter>() {
                @Override
                public RateLimiter load(String key) throws Exception {
                    // 新的IP初始化 (限流每秒两个令牌响应)
                    return RateLimiter.create(REQUEST_COUNT_PERIOD);
                }
            });


    public static boolean checkIpLimiter(String ip) {
        try {
            RateLimiter limiter = IpLimiterUtils.ipLimiterCache.get(ip);
            return limiter.tryAcquire();
        } catch (ExecutionException e) {
              LOGGER.error(e.getMessage(), e);
        }
        return false;
    }


    public static void main(String[] arg0) {
        for (int i = 0; i < 1000; i++) {
            if (!checkIpLimiter("127.0.0.1")) {
                System.out.println("*-*-*-*-*-*-* 被限流 *-*-*-*-*-*-");
            }
        }
    }

}
