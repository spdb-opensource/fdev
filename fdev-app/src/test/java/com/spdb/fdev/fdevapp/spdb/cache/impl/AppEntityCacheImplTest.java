package com.spdb.fdev.fdevapp.spdb.cache.impl;

import com.spdb.fdev.fdevapp.spdb.cache.IAppEntityCache;
import com.spdb.fdev.fdevapp.spdb.service.IAppEntityService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class AppEntityCacheImplTest {

    @Autowired
    private IAppEntityService appEntityService;

    @Autowired
    private IAppEntityCache appEntityCache;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.profiles.active}")
    private String env;

    @Test
    public void setCache() throws Exception {
        //String id = "5eddfecf0fe74a0012f2cf74";
        String id = "5ea4fccb3132150012ad3509";
        Object object = appEntityService.findById(id);
        String cacheKey = "frelease.appinfo." + id;
        redisTemplate.opsForValue().set(cacheKey, object, 10, TimeUnit.MINUTES);
        //redisTemplate.delete(cacheKey);
        Set keys = redisTemplate.keys("*" + id + "*");
        Iterator var9 = keys.iterator();

        while(var9.hasNext()) {
            Object o = var9.next();
            String key = (String)o;
            key = key.substring(this.env.length() + 1);
            if (!key.contains("user.login.token")) {
                this.redisTemplate.delete(key);
            }
        }
    }

}