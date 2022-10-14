package com.spdb.fdev.release.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.spdb.fdev.release.entity.ReleaseTask;

/**
 * ReleaseTask接口
 *
 */
public interface IReleaseTaskService {
	/**
	 * 添加任务
	 * @param release_node_name 应用对象
	 * @param task_id 任务id
	 * @return 所添加结果
	 * @throws Exception
	 */
	ReleaseTask addTask(String application_id, String task_id, String release_node_name, String type, String rqrmntNo) throws Exception;
	/**
	 * 删除任务
	 * @param task_id 任务对象
	 * @return 影响结果行数
	 * @throws Exception
	 */
	ReleaseTask deleteTask(String task_id, String type) throws Exception;
	/**
	 * 通过任务id查询任务所在投产应用信息
	 * @param taskid 投产应用
	 * @return 投产应用
	 * @throws Exception
	 */
	Map<String, Object> queryDetailByTaskId(String taskid, String type) throws Exception;
	/**
	 * 通过投产应用id和投产窗口名称查询投产应用下所以任务列表
	 * @param requestParam 投产应用对象
	 * @return 投产应用对象含任务列表
	 * @throws Exception
	 */
	 List<Map<String, Object>> queryTasks(Map<String, String> requestParam) throws Exception;
	/**
	 * 添加投产任务-确认
	 * @param release_node_name "task_id": "任务id","release_node_name": "string","operation_type"
	 * @return "task_id" "release_node_name" "application_id" "status"
	 * @throws Exception
	 */
	ReleaseTask auditAdd(String task_id, String application_id, String release_node_name, String operation_type, String operateUser, String source_branch, String reject_reason) throws Exception;
	/**
	 * 通过任务id 查询唯一的投产任务对象
	 * @param task_id 任务id
	 * @return ReleaseTask
	 * @throws Exception
	 */
	ReleaseTask findOneTask(String task_id) throws Exception;

	/**
	 * 通过任务id 与投产窗口类型查询唯一的投产任务对象
	 * @param task_id 任务id
	 * @param type 投产窗口类型
	 * @return
	 * @throws Exception
	 */
	ReleaseTask findOneTask(String task_id, String type) throws Exception;

	/**
	 * 通过任务id 查询确认投产的投产任务
	 * @param task_id 任务id
	 * @return
	 * @throws Exception
	 */
	ReleaseTask findConfirmTask(String task_id) throws Exception;
	
	/**
	 * 修改任务审核状态
	 * @param releaseTask 任务id 审核状态  拒绝理由（非必须）
	 * @return 投产任务实体
	 * @throws Exception
	 */
	ReleaseTask updateReleaseTask(ReleaseTask releaseTask) throws Exception;
	/**
	 * 查询该投产点下该投产应用中 所有任务的状态是否有0 （未审核）
	 * @param release_node_name 投产点名称
	 * @param application_id 应用id
	 * @return
	 * @throws Exception
	 */
	List<ReleaseTask> queryTaskStatusOfApp(String release_node_name, String application_id) throws Exception;
	/**
	 * 查询该投产窗口下同一应用的所有任务
	 * @param appid 应用id
	 * @param release_node_name 投产窗口名
	 * @return 任务列表
	 * @throws Exception
	 */
	List<ReleaseTask> queryReleaseTaskByAppid(String appid, String release_node_name) throws Exception;
	/**
	 * 任务特定日期所有投产窗口下任务列表
	 * @param release_node_names 投产日期
	 * @return 任务列表
	 * @throws Exception
	 */
	List<String> releaseTaskByNodeName(List<String> release_node_names) throws Exception;
	/**
	 * 修改任务状态为6 已投产
	 * @param task_id 任务id
	 * @throws Exception
	 */
	void updateTaskProduction(String task_id)throws Exception;

	/**
	 * 查询前三天的所有任务列表
	 * @param archivedDay
	 * @return
	 * @throws Exception
	 */
	List<String> queryTaskByNodeDate(String archivedDay) throws Exception;
	
	/**
	 * 修改投产窗口
	 * @param task_id
	 * @param release_node_name
	 * @throws Exception
	 */
	void changeReleaseNode(String task_id, String release_node_name, ReleaseTask releaseTask) throws Exception;

	/**
	 * 根据时间间隔和小组集合查询出投产任务的数量和投产任务的id集合
	 * @param requestParam
	 * @return
	 * @throws Exception
	 */
	Set<String> queryTaskByInterval(Map<String, String> requestParam) throws Exception;


	List<Map<String, Object>> queryBatchTasks(Map<String, String> requestParam) throws Exception;

//	void rollbackTask(String task_id) throws Exception;
	
	void updateReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception;

	/**
	 * 删除投产窗口待审核的任务  回退任务阶段
	 * @param task_id
	 * @throws Exception
	 */
	ReleaseTask deleteSitTask(String task_id) throws Exception;

	List<ReleaseTask> queryTasksByExecutor(Map<String, String> param) throws Exception;

	void updateTaskArchivedBatch(Map<String, String> param) throws Exception;

	List<String> updateTaskProductions(List<ReleaseTask> releaseTasks) throws Exception;

	void updateReleaseTaskProductions(List<String> task_ids) throws Exception;

	/**
	 * 通过应用id和投产日期以及任务id查询
	 * @param appId
	 * @param release_time
	 * 馹el@param task_id
	 * @return
	 */
	List<ReleaseTask> queryReleaseTaskByAppIdNoTaskId(String appId, String release_time, String task_id) throws Exception;

	List<ReleaseTask> queryReleaseByNodeName(String date);

	List<ReleaseTask> queryCurrentTaskByNodeName(String release_node_name, List<String> status, List<String> ids);

	void deleteReleaseProdApplication(String task_id, String release_node_name) throws Exception;

	ReleaseTask queryByTaskIdNode(String task_id, String release_node_name);

	List<String> findReleaseTaskByReleaseNodeName(String release_node_name);

    ReleaseTask deleteOnlyTask(String task_id, String type) throws Exception;

	List<ReleaseTask> queryByReleaseNodeName(String release_node_name);

	List<Map<String,Object>> proWantWindow(String time,String ownerGroupId);

	void updateReleaseTaskRqrmntId(String task_id, String rqrmntId) throws  Exception;

	List<ReleaseTask> queryNecessaryTask(String release_node_name);

    List<ReleaseTask> queryRefusedTask(String release_node_name);
    /**
	 * 修改任务状态为6 已投产
	 * @param task_id 任务id
	 * @throws Exception
	 */
	void updateTaskByComponen(String task_id, String type)throws Exception;
}

