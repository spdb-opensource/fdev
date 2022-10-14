package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.spdb.service.IAppService;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppServiceImpl implements IAppService {

    @Autowired
    private RestTransport restTransport;

    @Override
    @LazyInitProperty(redisKeyExpression = "fsonar.appinfo.{id}")
    public Map<String, Object> findById(String id) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.ID, id);// 发app模块获取appliaction详细信息
        send_map.put(Dict.REST_CODE, "findById");
        return (Map<String, Object>) restTransport.submit(send_map);
    }

    @Override
    public Map<String, Object> queryByGitId(Integer gitlab_project_id) throws Exception {
        Map<String, Object> send = new HashMap<>();
        send.put(Dict.ID, gitlab_project_id);
        send.put(Dict.REST_CODE, "getAppByGitId");
        Map<String, Object> result = (Map<String, Object>) restTransport.submit(send);
        if (CommonUtil.isNullOrEmpty(result)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[] {"通过gitid查询应用信息不存在"});
        }
        return result;
    }

    @Override
    public Map<String, Object> queryByNameEn(String name_en) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.NAME_EN, name_en);// 发app模块获取appliaction详细信息
        send_map.put(Dict.REST_CODE, "search");
        Object submit = restTransport.submit(send_map);
        if (CommonUtil.isNullOrEmpty(submit)) {
            return null;
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) submit;
        Map<String, Object> map = list.get(0);
        return map;
    }

}
