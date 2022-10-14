package com.spdb.fdev.release.service;

import java.util.List;
import java.util.Map;

import com.spdb.fdev.release.entity.BatchTaskInfo;


public interface IBatchTaskService {


	/**
	 * 新建批量任务
	 * @return
	 * @throws Exception
	 */
	void createBatchTask(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 修改批量任务
	 * @return
	 * @throws Exception
	 */
	void updateBatchTask(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 删除批量任务
	 * @return
	 * @throws Exception
	 */
	void deteleBatchTask(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 获取批量任务
	 * @return
	 * @throws Exception
	 */
	List<Map<String, Object>> queryBatchTask(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 添加批量任务
	 * @return
	 * @throws Exception
	 */
	void addBatchTask(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 查询批量任务列表
	 * @return
	 * @throws Exception
	 */
	List<Map<String, Object>> queryBatchTaskList(Map<String, Object> requestParam) throws Exception;

	/**
	 * 查询变更管理批量任务列表
	 * @return
	 * @throws Exception
	 */
	List<Map<String, Object>> queryBatchTaskByProdId(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 更新批量任务序号
	 * @return
	 * @throws Exception
	 */
	Map<String, Object> updateNoteBatchNo(Map<String, Object> requestParam) throws Exception;
	
	/**
	 * 查询已添加批量任务的应用
	 * @return
	 * @throws Exception
	 */
	List<String> queryBatchAppIdByNoteId(Map<String, Object> requestParam) throws Exception;
}
