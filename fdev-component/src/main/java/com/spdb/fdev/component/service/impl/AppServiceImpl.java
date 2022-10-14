package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.component.service.IAppService;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppServiceImpl implements IAppService {

    @Autowired
    private RestTransport restTransport;

    @Override
    public void createProjectPipeline(Map map) throws Exception {
        // 发app模块创建新pipeline
        map.put(Dict.REST_CODE, Dict.CREATE_PIPELINE);
        restTransport.submit(map);
    }

    @Override
    public Map createTags(String id, String name, String ref, String token) throws Exception {
        // 发app模块创建新tag
        Map<String, String> mapMergeReq = new HashMap<>();
        mapMergeReq.put(Dict.ID, id);
        mapMergeReq.put(Dict.NAME, name);
        mapMergeReq.put(Dict.REF, ref);
        mapMergeReq.put(Dict.PRIVATE_TOKEN_L, token);
        mapMergeReq.put(Dict.REST_CODE, Dict.CREATE_TAGS);
        return (Map) restTransport.submit(mapMergeReq);
    }

    /**
     * 根据应用id查询应用信息
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    @LazyInitProperty(redisKeyExpression = "fcomponent.appinfo.{id}")
    public Map<String, Object> queryAPPbyid(String id) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.REST_CODE, Dict.FINDBYID);
        send_map.put(Dict.ID, id);// 发app模块获取appliaction详细信息
        return (Map<String, Object>) restTransport.submit(send_map);
    }

    /**
     * 根据gitlabid查询应用信息
     *
     * @param gitlabid
     * @return
     * @throws Exception
     */
    @Override
    @LazyInitProperty(redisKeyExpression = "fcomponent.appinfo.{gitlabid}")
    public Map<String, Object> getAppByGitId(String gitlabid) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.REST_CODE, Dict.GETAPPBYGITID);
        send_map.put(Dict.ID, gitlabid);// 发app模块获取appliaction详细信息
        return (Map<String, Object>) restTransport.submit(send_map);
    }

    @Override
    public List<Map<String, Object>> getAppList() throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.REST_CODE, Dict.QUERYAPPS);
        return (ArrayList) restTransport.submit(send_map);
    }
}
