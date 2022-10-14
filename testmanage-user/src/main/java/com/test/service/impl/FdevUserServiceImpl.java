package com.test.service.impl;

import com.test.dict.Dict;
import com.test.service.FdevUserService;
import com.test.testmanagecommon.cache.LazyInitProperty;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.utils.MyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class FdevUserServiceImpl implements FdevUserService{

	private static Logger logger = LoggerFactory.getLogger(FdevUserServiceImpl.class);
	@Autowired
	private RestTransport restTransport;
	
	@LazyInitProperty(redisKeyExpression = "tuser.fdev.user.{user_name_en}")
	@Override
	public Map<String, Object> queryUserByUserNameEn(String user_name_en) throws Exception {
		Map<String,String> map = new HashMap<String,String>();
		map.put(Dict.USER_NAME_EN, user_name_en);
		map.put(Dict.REST_CODE, "fdev.user.core.query");
		List list = (List) restTransport.submit(map);
		if(MyUtils.isEmpty(list)) {
			return null;
		}
		return (Map<String, Object>)list.get(0);
	}

	@Override
	public String getUserToken(String host,String name) throws Exception {
		Map<String,String> map = new HashMap<String,String>();
		map.put(Dict.HOST, host);
		map.put(Dict.NAME, name);
		map.put(Dict.REST_CODE, "get.token.url");
		return (String)restTransport.submit(map);
	}

	@Override
	public List<Map<String, Object>> queryAllFdevUser(String status) throws Exception {
		Map<String,String> map = new HashMap<String,String>();
		map.put(Dict.STATUS, status);
		map.put(Dict.REST_CODE, "fdev.user.core.query");
		List list = (List) restTransport.submit(map);
		if(MyUtils.isEmpty(list)) {
			return null;
		}
		return list;
	}

	@Override
	public List queryFdevGroups() throws Exception {
		return (List) restTransport.submit(restTransport.getUrl("fdev.group.url"), new HashMap<>());
	}

	@Override
	public void setLeaveByIds(List<String> nameEns) throws Exception {
		Map send = new HashMap();
		send.put(Dict.NAME, nameEns);
		send.put(Dict.REST_CODE, "fdev.user.setLeaveByNames");
		try {
			restTransport.submitSourceBack(send);
		} catch (Exception e) {
			logger.error("fail to set leave");
		}
	}

}
