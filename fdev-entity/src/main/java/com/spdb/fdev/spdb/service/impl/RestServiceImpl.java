package com.spdb.fdev.spdb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.spdb.service.IRestService;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RefreshScope
public class RestServiceImpl implements IRestService {

	@Autowired
    private RestTransport restTransport;
	@Autowired
	private UserVerifyUtil userVerifyUtil;



	@Override
	@LazyInitProperty(redisKeyExpression = "fentity.service.{id}")
	public Map<String, Object> queryApp(String id) throws Exception {
		Map<String, Object> sendMap = new HashMap<>();
		sendMap.put(Dict.REST_CODE, Dict.QUERYAPP);
		sendMap.put(Dict.ID, id);// 查询应用详情
        Map<String, Object> map = (Map<String, Object>) restTransport.submit(sendMap);
		return map;
	}


	

	
	@Override
	public List<Map> queryApps(Map map) throws Exception {
		Map<String, Object> sendMap = new HashMap<>();
		sendMap.put(Dict.REST_CODE, Dict.QUERYAPPS);
		sendMap.put("ids", map.get(Dict.GITLABIDS));// 根据应用gitIds查询应用列表
		sendMap.put("type","gitlab_project_id" );// 查询使用中应用

		List<Map> submitMap = (List<Map>) restTransport.submit(sendMap);
		return submitMap;
	}
	
	

}
