package com.spdb.fdev.fdevapp.spdb.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.spdb.service.IUserService;
import com.spdb.fdev.transport.RestTransport;


@Service
@RefreshScope
public class UserServiceImpl implements IUserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印
    
    @Autowired
    private RestTransport restTransport;

	@Override
	public Map<String, Object> getThreeLevelGroup(String group_id) throws Exception {
		Map<String, String> map = new HashMap<>();
		map.put(Dict.ID, group_id);
		map.put(Dict.REST_CODE, "getThreeLevelGroup");
		return (Map<String, Object>) restTransport.submit(map);
	}



}
