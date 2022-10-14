package com.test.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public interface UserService {

    Map update(String user_id,String  level, List<String> roleIds) throws Exception;

	void saveMantisToken(String mantis_token) throws Exception;

	Map loginSaveUser(Map user)throws Exception;

	void fdevSyncUser() throws Exception;
	
	Integer updateWorkConfig(String group_id, String manager, List<String> groupLeaders, String uatContact, List<String> securityLeader) throws Exception;

	Map query(List<String> search, String groupId, Integer currentPage, Integer pageSize) throws Exception;

	Map queryUserDetailByNameEn(String userNameEn) throws Exception;

	void exportUser(List<String> search, String groupId, HttpServletResponse resp) throws Exception;

	boolean vaildateAdmin() throws Exception;

	Map login(String userNameEn, String password) throws Exception;

	List<Map<String, String>> queryChildGroupById(String groupId) throws Exception;

	Integer queryGroupCountTester(String groupId) throws Exception;

	List<Map> queryGroupTester(String groupIds, boolean flag) throws Exception;

	List<Map> queryTester() throws Exception;

	Map<String, Object> queryGroupDetailById(String s) throws Exception;

    List<Map<String, Object>> queryAlreadyAllocated() throws Exception;

    Map queryUserCoreDataById(String id) throws Exception;

    Map syncLoginFtms(String token) throws Exception;

	Map<String, Map> queryUserByNameEns(List<String> nameEns);

	Map<String, String> queryGroupNameByIds(List<String> groupIds) throws Exception;

	/**
	 * 批量查询小组
	 * @param groupIds
	 * @return
	 */
	List<Map> queryGroupByIds(List<String> groupIds) throws Exception;
}
