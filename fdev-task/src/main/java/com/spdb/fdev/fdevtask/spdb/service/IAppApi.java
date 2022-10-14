package com.spdb.fdev.fdevtask.spdb.service;
/**
 * 调用APP模块接口 
 * */

import com.spdb.fdev.common.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IAppApi {

	/** 4新建应用 */
	public Map createApp(Map param) throws Exception;

	/** 2新建gitlab分支 */
	public Map createGitLabBranch(Map param) throws Exception;

	/** 合并请求 */
	public Map requestMerge(Map param) throws Exception;

	/** 查看merge状态 */
	// {"id":"5c877a02e43ba200067c8c14","iid":"1","token":"qNMxg-bnRsBievnGFM1h"}
	public Map queryMergeInfo(Map param) throws Exception;


	/** 为应用添加相关人员 */
	// {"id":"5c877a02e43ba200067c8c14","userName":["xxx","xxx"],"role":"30","token":"qNMxg-bnRsBievnGFM1h"}
	public ArrayList<Map> addMember(Map param) throws Exception;

	// 向应用中添加任务
	public Map saveTask(Map param) throws Exception;

	// 保存文档
	public void saveDoc(Map saveDocParam) throws Exception;

	// 查询文档列表
	public ArrayList queryDoc(Map queryDoc) throws Exception;

	// 通过应用ID查信息
	public Map queryAppById(Map query) throws Exception;

	// 查询sit 分支名称
	public List<String> querySitBranch(Map map) throws Exception;

	//更新应用状态
	public Map update(Map map)throws Exception;

	List queryAppByIds(List ids) throws Exception;
	
	//根据gitlabid查询应用
	Map queryAppByGitlabIds(String id) throws Exception;

	/**
	 * 根据应用名字查询应用相关信息
	 * @param name_en 应用英文名字
	 * @return
	 * @throws Exception
	 */
	Map queryAppInfoByAppName(String name_en);

	public void uploadFilesByPaths(String task_id, String type, List<String>paths, User user);

	//根据应用类型id查找应用类型

	Map queryAppTypeById(String type_id);


	Map queryTestMergeInfo(Map param)throws Exception;

	/**
	 * 获取sonar扫描信息 v01
	 * @param projectId
	 * @param featur
	 * @param sonarId
	 * @return
	 */
	Map getSonarScanInfo(String projectId,String featur,String sonarId);

	String QueryTestSwitch(String projectId);

    Map<String, Object> queryAppByIdCache(String appId) throws Exception;
}
