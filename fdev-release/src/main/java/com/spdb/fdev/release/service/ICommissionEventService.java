package com.spdb.fdev.release.service;

import java.util.List;
import java.util.Map;

/**
 * 待办事项
 * 2019年8月15日
 */
public interface ICommissionEventService {
	/**
	 * 添加任务投产审核待办事项
	 * @param task_id
	 * @param type
	 * @throws Exception
	 */
	void addAuditTaskCommissionEvent(String task_id, String type) throws Exception;
	
	/**
	 * 修改待办事项状态为已处理
	 * @param task_id
	 * @throws Exception
	 */
	void updateAuditTaskCommissionEvent(String task_id) throws Exception;
	
	/**
	 * 修改待办事项状态为已处理
	 * @param task_id
	 * @throws Exception
	 */
	void updateTaskArchivedCommissionEvent(String task_id) throws Exception;
	/**
	 * 删除待办事项
	 * @param task_id 任务id
	 * @throws Exception
	 */
	public void deleteCommissionEvent(String task_id)throws Exception;

	/**
	 * 将提交发布的添加到待办事项中
	 * @param application 应用信息
	 * @param saveId 持续集成记录ID
	 * @param id Merge_request_id
	 */
    void addRelDevopsCommissionEvent(Map<String, Object> application, String saveId, String id) throws Exception;

	/**
	 * 修改待办事项状态
	 * @param targetId 目标id
	 * @param userId 用户id
     * @param type 待办事项类型
	 * @throws Exception
	 */
	void updateCommissionEvent(String targetId, String userId, String type) throws Exception;

    void addAuditConfigChangedCommission(List<Map<String, String>> managerList,
                                         String targetId, String release_node_name) throws Exception;
}
