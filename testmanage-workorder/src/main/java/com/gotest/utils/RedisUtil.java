package com.gotest.utils;

import com.test.testmanagecommon.cache.RemoveCachedProperty;
import org.springframework.stereotype.Service;

@Service
public class RedisUtil {

    /**
     * 根据 环境+"*"+key+"*" 来清除缓存
     * @param key
     */
    @RemoveCachedProperty(redisKeyExpression = "{key}")
    public void removeCache(String key) {
    }
}


