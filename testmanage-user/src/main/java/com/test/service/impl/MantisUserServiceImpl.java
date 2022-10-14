package com.test.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.test.dict.Dict;
import com.test.entity.User;
import com.test.service.MantisUserService;
import com.test.service.UserService;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.utils.MantisRestTemplate;
import com.test.utils.MyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class MantisUserServiceImpl implements MantisUserService {
	
	private static final Logger logger = LoggerFactory.getLogger(MantisUserServiceImpl.class);
	@Value("${manits.add.user.url}")
	private String manits_user_url;
	@Value("${manits.admin.token}")
	private String manits_token;
	@Value("${mantis.init.password}")
	private String mantisInitPassword;
	@Autowired
	private UserService userService;
	@Autowired
	private MantisRestTemplate mantisUserService;
	@Autowired
	private RestTransport restTransport;

	@Override
	public void addMantisUser(User user) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(Dict.USERNAME, user.getUser_en_name());
		map.put(Dict.PASSWORD, mantisInitPassword);
		map.put(Dict.REAL_NAME, user.getUser_name());
		map.put(Dict.EMAIL, user.getEmail());
		Map<String, String> levelMap = new HashMap<String, String>();
		levelMap.put(Dict.NAME, Dict.DEVELOPER);
		map.put(Dict.ACCESS_LEVEL, levelMap);
		map.put(Dict.ENABLED, true);
		map.put(Dict.PROTECTED, false);
		try {
			mantisUserService.sendPost(manits_user_url, manits_token, map);
		} catch (Exception e) {
			logger.error("add mantis user error,user ="+ user.getUser_en_name());
			logger.error(""+e);
		}

	}

	@Override
	public void branchAddMantisUser() throws Exception {
		/*List<User> users = userService.queryAllUser();
		for (User user : users) {
			 addMantisUser(user);
		}*/
	}

	@Override
	public Map<String,Integer> countReporterSum(String startDate, String endDate, List<String> collect) throws Exception {
		Map<String, Object> sendMap = new HashMap<>();
		sendMap.put(Dict.STARTDATE, startDate);
		sendMap.put(Dict.ENDDATE, endDate);
		sendMap.put(Dict.USERLIST, collect);
		sendMap.put(Dict.REST_CODE, "countReporterSumNew");
		try {
			return (Map<String, Integer>) restTransport.submitSourceBack(sendMap);
		} catch (Exception e) {
			logger.info(">>>>>>countReporterSum fail");
			return new HashMap<>();
		}
	}
}
