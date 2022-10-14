package com.spdb.fdev.fdevtask.spdb.dao;

import java.util.List;
import java.util.Map;

import com.spdb.fdev.fdevtask.spdb.entity.AppDeployReview;

public interface IAppDeployReviewDao {

	/**
     * 保存申请部署信息
     * @param applayDeploy
     */
    public AppDeployReview applayDeploy(AppDeployReview applayDeploy) throws Exception;
    
    /**
     * 查询已审批信息
     * @param queryApproved
     */
	public Map<String,Object> queryApproved(Map<String, Object> param) throws Exception;
	
	/**
     * 查询未审批信息
     * @param queryNotApproved
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
     * 根据应用名称、镜像类型查询部署信息
     * @param appName
     */
	public List<AppDeployReview> queryAppImage(String appNames,String queryType) throws Exception;
	
	/**
     * 保存流水线信息
     * @param applayDeploy
     */
	public AppDeployReview savePiplineAppInfo(AppDeployReview applayDeploy) throws Exception;
	
	/**
     * 修改部署信息
     * @param applayDeploy
     */
	public void update(AppDeployReview applayDeploy) throws Exception;
	
	/**
	 * 修改部署状态
	 * */
	public void modifyDeployStatus(Map<String, Object> param);
	
	/**
     * 根据应用名称、部署状态，查询部署信息
     * @param appName
     */
	public List<AppDeployReview> queryDeployTask(String appNames,String queryType) throws Exception;
	
}
