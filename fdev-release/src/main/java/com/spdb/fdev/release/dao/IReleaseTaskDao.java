package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.ReleaseTask;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 投产任务数据库接口层
 *
 */
public interface IReleaseTaskDao {
	/**
	 * 通过taskid查询
	 *
	 * @param taskid
	 *            任务id
	 * @return ReleaseApplication
	 * @throws Exception
	 */
	Map<String, Object> queryDetailByTaskId(String taskid, String type) throws Exception;

	/**
	 * 新增任务
	 *
	 * @param release_node_name
	 *            releasenodename
	 * @param taskid
	 *            任务id
	 * @return 添加结果
	 * @throws Exception
	 */
	ReleaseTask addTask(String application_id, String taskid, String release_node_name, String type, String rqrmntNo) throws Exception;

	/**
	 * 删除任务
	 *
	 * @param task_id
	 *            任务id
	 * @return 删除结果
	 * @throws Exception
	 */
	ReleaseTask deleteTask(String task_id, String type) throws Exception;

	/**
	 * 查询任务列表
	 *
	 * @param requestParam releaseNode
	 *            releasenodename appid选填
	 * @return 任务列表
	 * @throws Exception
	 */
	List<ReleaseTask> queryTasks(Map<String, String> requestParam) throws Exception;

	/**
	 * 添加投产任务-确认
	 *
	 * @param
	 *            "task_id": "任务id","release_node_name": "string","operation_type"
	 * @return "task_id" "release_node_name" "application_id" "status"
	 * @throws Exception
	 */
	ReleaseTask auditAdd(String task_id, String release_node_name, String operation_type, String reject_reason) throws Exception;

	ReleaseTask findOneTask(String task_id, String type) throws Exception;

	ReleaseTask updateReleaseTask(ReleaseTask releaseTask)throws Exception;

	List<ReleaseTask> queryTaskStatusOfApp(String release_node_name, String application_id)throws Exception;

	List<ReleaseTask> queryReleaseTaskByAppid(String appid, String release_node_name)throws Exception;

	List<ReleaseTask> querySameAppTask(String application_id, String release_node_name)throws Exception;

	List<ReleaseTask> queryTaskByYestDayNode()throws Exception;

	List<String> releaseTaskByNodeName(List<String> release_node_names) throws Exception;

	void updateTaskProduction(String task_id) throws Exception;

	void updateTaskNode(ReleaseTask releaseTask, String release_node_name) throws Exception;

	List<String>  queryTaskByNodeDate(String archivedDay)throws Exception;

	Set<String> queryTaskByInterval(Map<String, String> requestParam) throws Exception;

	void updateReleaseNodeName(String release_node_name, String old_release_node_name) throws Exception;

	List<ReleaseTask> queryTasksByExecutor(String application_id, String release_node_name) throws Exception;

	List<ReleaseTask> queryReleaseTaskByNames(List<String> release_node_names) throws Exception;

	void updateReleaseTaskProductions(List<String> task_ids) throws Exception;

	List<ReleaseTask> queryReleaseTaskByAppIdNoTaskId(String appId, String release_time, String task_id) throws Exception;

	List<ReleaseTask> queryReleaseByNodeName(String date);

	List<ReleaseTask> queryCurrentTaskByNodeName(String release_node_name, List<String> status, List<String> ids);

    ReleaseTask queryByTaskIdNode(String task_id, String release_node_name);

	List<String> findReleaseTaskByReleaseNodeName(String release_node_name);

    List<ReleaseTask> queryByReleaseNodeName(String release_node_name);

    List<String> queryCounldBindAppIds(String rqrmntNo, String release_node_name);

    void updateReleaseTaskRqrmntId(String task_id, String rqrmntId) throws Exception;

    String queryRqrmntNoByAppIdReleaseNode(String appId, String releaseNodeName);

	ReleaseTask findConfirmTask(String task_id) throws Exception;

	List<ReleaseTask> queryNecessaryTask(String release_node_name);

    List<ReleaseTask> queryRefusedTask(String release_node_name);
    
    void updateTaskByComponen(String task_id, String type) throws Exception;

}
