package com.spdb.fdev.base.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutionException;

@EnableCaching
@Configuration
@RefreshScope
public class GuavaCache {
    private Logger logger = LoggerFactory.getLogger(com.spdb.fdev.base.cache.GuavaCache.class);// 日志打印

    @Value("${spring.profiles.active}")
    private String prex;

    private LoadingCache<String, Object> loadingCache =
            CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .concurrencyLevel(8)
                    .build(new CacheLoader<String, Object>() {
                        @Override
                        public Object load(String s) {
                            return "";
                        }
                    });

    @Bean
    public LoadingCache<String, Object> loadingCache() {
        return loadingCache;
    }

    public void setCache(String key, Object object) {
        loadingCache.put(prex + "-" + key, object);
    }

    public Object getCache(String key) {
        Object object = null;
        try {
            object = loadingCache.get(prex + "-" + key);
        } catch (ExecutionException e) {
            logger.error("获取缓存失败", e);
        }
        return object;
    }

    public void flushCache(String key) {
        loadingCache.invalidate(prex + "-" + key);
    }
}
