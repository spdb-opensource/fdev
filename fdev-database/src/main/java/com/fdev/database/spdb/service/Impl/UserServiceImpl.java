package com.fdev.database.spdb.service.Impl;

import com.fdev.database.dict.Dict;
import com.fdev.database.spdb.service.UserService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private RestTransport restTransport;

    @Override
    public List<Map> queryUserInfo(String user_name_en) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put(Dict.USER_NAME_EN, user_name_en);
        map.put(Dict.REST_CODE, "queryUser");
        return (ArrayList) restTransport.submit(map);
    }

    @Override
    public List<Map> queryRoleInfo(String roleName) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put(Dict.NAME, roleName);
        map.put(Dict.REST_CODE, "queryRole");
        return (ArrayList) restTransport.submit(map);
    }
}
