package com.spdb.fdev.fdevapp.spdb.service;

import java.util.Map;

public interface IGitlabVariablesService {

	/**
	 * 查询项目参数列表
	 * @param id 应用id
	 * @param token 验证码
	 * @return 参数列表
	 * @throws Exception
	 */
	public Object queryVariables(String id,String token)throws Exception;
	/**
	 * 查询参数详情
	 * @param id 应用id
	 * @param key 参数key
	 * @param token 验证码
	 * @return 参数详情
	 * @throws Exception
	 */
	public Object queryVariableDetails(String id,String key,String token)throws Exception;
	/**
	 * 创建参数
	 * @param id 应用id
	 * @param key 参数key
	 * @param value 参数value
	 * @param token 验证码
	 * @return 参数详情
	 * @throws Exception
	 */
	public Object createVariable(String id,String key,String value,String token)throws Exception;
	/**
	 * 更新参数
	 * @param id 应用id
	 * @param key 参数key
	 * @param value 参数value
	 * @param token 验证码
	 * @return 参数详情
	 * @throws Exception
	 */
	public Object updateVariable(String id,String key,String value,String token)throws Exception;
	/**
	 * 删除参数
	 * @param id 应用id
	 * @param key 参数key
	 * @param token 验证码
	 * @return 
	 * @throws Exception
	 */
	public Object deleteVariable(String id,String key,String token)throws Exception;
	/**
	 * Create a new pipeline schedule of a project.
	 * @param id 项目id 
	 * @param description 描述
	 * @param ref 目标名
	 * @param cron 定义触发时间规则
	 * @param key 参数key
	 * @param value 参数value
	 * @param token 验证码
	 * @return PipelineSchedule信息
	 * @throws Exception
	 */
	public Map<String,Object> createPipelineSchedule(String id,String description,String ref,String cron, String key,String value,String token) throws Exception;
	
	/**
	 * 查询管道计划详情
	 * @param id 项目id
	 * @param key 参数key
	 * @param token 验证码
	 * @return PipelineSchedule信息
	 * @throws Exception
	 */
	public Object   queryPipelineSchedule(String id,String token) throws Exception;
	/**
	 * 删除管道计划详情
	 * @param id 项目id
	 * @param key 参数key
	 * @param pipelineScheduleId
	 * @param token 验证码
	 * @return 
	 * @throws Exception
	 */
	public Object   deletePipelineSchedule(String id,String pipelineScheduleId,String token) throws Exception;


}
