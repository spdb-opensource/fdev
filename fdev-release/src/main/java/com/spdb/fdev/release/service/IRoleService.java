package com.spdb.fdev.release.service;

public interface IRoleService {
	/**
	 * 查询当前用户是否为该小组的投产管理员
	 * @param group_id 组id
	 * @return
	 * @throws Exception
	 */
	boolean isGroupReleaseManager(String group_id) throws Exception;

	/**
	 * 查询当前用户是否为该小组的投产管理员
	 * @return
	 * @throws Exception
	 */
	boolean isReleaseManager() throws Exception;
	/**
	 * 查询当前应用是否为该任务的行内负责人
	 * @param task_id 任务id
	 * @return
	 * @throws Exception
	 */
	boolean isTaskSpdbManager(String task_id) throws Exception ;
	/**
	 * 查询当前用户是否为该任务的任务负责人
	 * @param task_id 任务id
	 * @return
	 * @throws Exception
	 */
	boolean isTaskManager(String task_id) throws Exception;
	/**
	 * 判断当前用户是否为该应用的应用负责人
	 * @param application_id 应用id
	 * @return
	 * @throws Exception
	 */
	boolean isApplicationManager(String application_id) throws Exception;
	/**
	 * 判断当前用户是否为该应用的行内应用负责人
	 * @param application_id
	 * @return
	 * @throws Exception
	 */
	boolean isAppSpdbManager(String application_id) throws Exception;
	/**
	 * 判断a 组为b组的同组或子组
	 * @param group_id a组组id
	 * @param target_group_id  b组组id
	 * @return
	 * @throws Exception
	 */
	boolean isChildGroup(String group_id, String target_group_id)throws Exception;
	
	/**
	 * 判断a 组为b组的同组或父组
	 * @param group_id a组组id
	 * @param target_group_id  b组组id
	 * @return
	 * @throws Exception
	 */
	boolean isParentGroup(String group_id, String target_group_id)throws Exception;
	/**
	 * 通过变更id判断用户是否为该组的投产管理员
	 * @param prod_id 变更id
	 * @return
	 * @throws Exception
	 */
	public void isGroupReleaseManagerByProd(String prod_id)throws Exception;
	
	/**
	 * 通过变更id判断用户是否为该组的投产管理员
	 * @param prod_id 变更id
	 * @return
	 * @throws Exception
	 */
	boolean isUserOperatorProd(String prod_id) throws Exception;

    boolean isReleaseContact(String releaseNodeName) throws Exception;
}
