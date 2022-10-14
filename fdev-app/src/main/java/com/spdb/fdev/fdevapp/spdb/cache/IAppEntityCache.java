package com.spdb.fdev.fdevapp.spdb.cache;

import com.spdb.fdev.fdevapp.spdb.entity.ServiceSystem;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAppEntityCache {
	
	Map<String,Map> getUsersByIdsOrUserNameEN(String key,Map param) throws Exception;
	
	void resetAppEntityByNameEN(String key)throws Exception;

	Map<String, Object> queryByGroupId(String id) throws Exception;

	/**
	 * 查询所有组信息
	 * @return
	 * @throws Exception
	 */
	List<Map> findGroups() throws Exception;

	/**
	 * 查询应用所属系统
	 * @return
	 * @throws Exception
	 */
	List<ServiceSystem> querySystem() throws Exception;

	List<String> getAllGroup(String groupId) throws Exception;
	
	/**
	 * 查询父子组
	 * @param id
	 * @return
	 * @throws Exception
	 */
	List<Map> queryChildGroupById(String id) throws Exception;

    void updateManager(String id, Set<Map<String, String>> spdb_managers);
}
