package com.spdb.fdev.spdb.service;

import java.util.List;
import java.util.Map;


public interface IRestService {

	/**
	 * 获取用户信息
	 * @param userId
	 * @return
	 * @throws Exception
	 */
//	Map<String,Object> getUserInfoById(String userId) throws Exception;
	


	/**
     * 查询小组信息
     *
     * @param groupId
     * @return
     * @throws Exception
     */
//	Map<String, Object>  getGroupInfo(String groupId)throws Exception;
	

	
	/**
     * 查询应用详情
     *
     * @param id
     * @return
     * @throws Exception
     */
	Map<String, Object>  queryApp(String id)throws Exception;



	/**
     * 查询应用列表
     *
     * @return
     * @throws Exception
     */
	 List<Map>  queryApps(Map map)throws Exception;


	
}