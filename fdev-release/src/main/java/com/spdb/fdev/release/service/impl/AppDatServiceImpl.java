package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.release.service.IAppDatService;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RefreshScope
public class AppDatServiceImpl implements IAppDatService {
    @Autowired
    private RestTransport restTransport;
    @Override
    public Map<String, Object> queryLastAppJson(String application_name_en, String branch) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.PROJECT_NAME, application_name_en);// 发app模块获取appliaction详细信息
        send_map.put(Dict.BRANCH, branch);
        send_map.put(Dict.REST_CODE, "queryLastAppJson");
        return (Map<String, Object>) restTransport.submit(send_map);
    }
}
