package com.spdb.fdev.fdevtask.spdb.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.spdb.fdev.fdevtask.spdb.entity.AppDeployReview;

/**
 * 部署申请接口
 */
public interface IAppDeployService {

    /**
     * 保存申请部署信息
     * @param applayDeploy
     */
	public AppDeployReview applayDeploy(AppDeployReview applayDeploy) throws Exception;
	
	/**
     * 查询已审批信息
     */
	public Map<String,Object> queryApproved(Map<String, Object> param) throws Exception;
	
	/**
     * 查询未审批信息
     */
	public List<AppDeployReview> queryNotApproved() throws Exception;
	
	/**
     * 根据id集合查询
     * @param ids
     */
	public List<AppDeployReview> queryAppDeploy(List<String> ids) throws Exception;
	
	/**
     * 根据任务编号查询部署信息
     * @param taskId
     */
	public AppDeployReview queryAppDeployByTaskId(String taskId) throws Exception;
	
	/**
     * 根据应用名称查询部署信息
     * @param appName
     * @param queryType
     */
	public AppDeployReview queryAppImage(String appName,String queryType) throws Exception;
	
	/**
     * 根据应用名称查询已申请部署应用下其他未勾选任务
     * @param ids
     */
	public List<AppDeployReview> queryOtherTasks(String appName,String ids) throws Exception;
	
	/**
     * 查询应用关联任务
     * @param appName
     * @param queryType
     */
	public List<AppDeployReview> queryDeployTask(String appName,String queryType) throws Exception;
	
	/**
     * 保存流水线信息
     * @param applayDeploy
     */
	public AppDeployReview savePiplineAppInfo(AppDeployReview applayDeploy) throws Exception;
	
	/**
     * 部署
     * @param applayDeploy
     */
	public void deploy(Map<String,Object> param) throws Exception;
	
	/**
     * 修改部署信息
     * @param applayDeploy
     */
	public void update(AppDeployReview applayDeploy) throws Exception;
	
	/**
	 * 导出excel
	 * */
	public void export(Map<String, Object> resultMap,HttpServletResponse resp) throws Exception;

	public void modifyDeloyStatus(Map<String, Object> param);
	

}
