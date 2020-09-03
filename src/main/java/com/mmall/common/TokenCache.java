package com.mmall.common;


import com.google.common.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TokenCache {
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public static final String TOKEN_PREFIX = "token_";
    /**
     * https://blog.csdn.net/abc86319253/article/details/53020432
     * https://blog.csdn.net/lhc1105/article/details/79797205
     * guava本地缓存：refreshAfterWrite刷新只有在发生查询的时候才会对比是否超时去真正刷新（懒加载），而且只允许一个线程刷新，其他线程暂时读到的是旧值
     * 配合expireAfterWrite可以达到限制短期内有效的缓存值的作用；主要是因为refreshAfterWrite导致get内的加锁范围变成了insertLoadingValueReference，这个提前进入loading状态，
     * 加锁范围比正常的load要小，所以可以提高性能。
     * 但是这俩方法是同步的，也就是说当长时间后才来新的查询请求，那么就会短时间内发生大量的load调用
     * 其实可以用refresh方法（异步刷新）配合ScheduledExecutorService线程池定期刷新缓存，那么就可以减少这种大量load的情况，但是这会使得缓存维护开销变大；
     * */
    //LRU替换策略
    private static LoadingCache<String, String> localCache = CacheBuilder.newBuilder()
            .initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //默认的数据加载实现，当调用get没有找到key时，调用此方法
                @Override
                public String load(String s) throws Exception {
                    //避免在其他地方调用.equals报空指针异常
                    return "null";
                }
            });

    public static void setKey(String key, String value)
    {
        localCache.put(key, value);
    }

    public static String getKey(String key)
    {
        String value = null;
        try {
            value = localCache.get(key);
            if ("null".equals(value))   return null;
            return value;
        }catch (Exception e){
            logger.error("localCache get error", e);
        }
        return null;
    }
}
