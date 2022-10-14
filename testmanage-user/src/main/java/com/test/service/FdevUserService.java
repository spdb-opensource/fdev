package com.test.service;

import java.util.List;
import java.util.Map;

public interface FdevUserService {
	
	Map<String,Object> queryUserByUserNameEn (String user_name_en) throws Exception;
	
	String getUserToken (String host,String name) throws Exception;
	
	List<Map<String,Object>> queryAllFdevUser(String status) throws Exception;
	
	List queryFdevGroups() throws Exception;

	void setLeaveByIds(List<String> nameEns) throws Exception;
}
