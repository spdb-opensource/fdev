package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.release.service.ICacheService;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xxx on 下午7:45.
 */
@Service
public class CacheServiceImpl implements ICacheService{

    @Autowired
    private RestTransport transport;

    @Override
    @LazyInitProperty(redisKeyExpression = "frelease.usergitlabtoken.{username}")
    public String queryUserGitlabToken(String username) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put(Dict.USER_NAME_EN, username);
        map.put(Dict.REST_CODE, "queryUser");
        List<Map<String, Object>> result = (List<Map<String, Object>>) transport.submit(map);
        String token = null;
        if (result != null) {
            token = (String) result.get(0).get(Dict.GIT_TOKEN);
        }
        return String.valueOf(token);
    }
}
