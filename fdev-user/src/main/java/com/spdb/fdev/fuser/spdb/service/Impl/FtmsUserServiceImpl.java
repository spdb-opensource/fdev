package com.spdb.fdev.fuser.spdb.service.Impl;

import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.spdb.service.FtmsUserService;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
@EnableAsync
public class FtmsUserServiceImpl implements FtmsUserService {

    
    @Resource
    private RestTransport restTransport;

    @Async
    @Override
    public void isLeave(String user_name_en,String status) throws Exception {      
        Map<String, String> map = new HashMap<>();
        map.put(Dict.USER_NAME_EN, user_name_en);
        map.put(Dict.STATUS, status);
        map.put(Dict.REST_CODE, "setUserLeave");
        restTransport.submit(map);
    }
}
