package com.spdb.fdev.cache;

import com.spdb.fdev.common.annoation.RemoveCachedProperty;
import org.springframework.stereotype.Service;

/**
 * redis缓存公用方法
 */
@Service
public class RedisCache {
    /**
     * 根据 环境+"*"+key+"*" 来清除缓存
     *
     * @param key
     */
    @RemoveCachedProperty(redisKeyExpression = "{key}")
    public void removeCache(String key) {
    }


}
