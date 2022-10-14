package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ReleaseRqrmntInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 发送 task模块接口类
 *
 */
public interface ITaskService {
	/**
	 * 通过taskid查询task所在的应用id
	 * @param taskId 任务id
	 * @return taskProject
	 * @throws Exception
	 */
	Map<String,Object> queryTaskInfo(String taskId)throws Exception;
	/**
	 * 根据任务id查询任务关联项审核最终结果
	 * @param idlist
	 * @return
	 * @throws Exception
	 */
	Map queryTaskReview(List<String> idlist)throws Exception;
	/**
	 * 根据系统id查询中文名
	 * @param id
	 * @return
	 * @throws Exception
	 */
	String querySystemName(String id)throws Exception;

	/**
	 * 修改任务UAT阶段
	 * @return
	 * @throws Exception
	 */
	Map updateTaskInner(Map<String, Object> map) throws Exception;

	/**
	 * 修改任务阶段为已投产
	 * @param task_id 任务id
	 * @throws Exception
	 */
	void updateTaskProduction(String task_id) throws Exception;

	/**
	 * 查询系统信息
	 * @return
	 * @throws Exception
	 */
	List<Map> querySystem()throws Exception;
	/**
	 * 通过id列表查询task信息
	 * @return
	 * @throws Exception
	 */
	Map<String,Object> queryTasksByIds(Set<String> list)throws Exception;

	/**
	 * 修改任务REL阶段
	 * @param id
	 * @param stage
	 * @param date
	 * @return
	 * @throws Exception
	 */
	Map updateTaskForRel(String id, String stage, String date) throws Exception;

	/**
	 * 回退任务状态
	 * @param id
	 * @param stage
	 * @param date
	 * @param uat_testObject
	 * @return
	 * @throws Exception
	 */
	Map rollbackTaskStatus(String id, String stage, String uat_testObject, String date) throws Exception;

	Map<String, Object> queryTaskDetail(String task_id) throws Exception;

	Map<String, Object> queryTestcaseByTaskId(String task_id) throws Exception;

	Map<String, Object> querySystemById(String id) throws Exception;

	/**
	 * 查询任务需求信息
	 * @param id
	 * @return
	 * @throws Exception
	 */
	Map<String, Object> queryTaskRqrmnt(String id) throws Exception;

	boolean queryTaskReleaseConfirmDoc(ReleaseRqrmntInfo releaseRqrmntInfo, Map<String, Map> tasks) throws  Exception;

	Map<String, Map> queryRqrmntInfoTasks(List<ReleaseRqrmntInfo> releaseRqrmntInfos) throws Exception;

	Map queryTaskConfirmRecord(String task_id) throws Exception;

	String queryTaskConfirmRecord(ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception;

	String queryRqrmntInfoConfirmRecord(ReleaseRqrmntInfo releaseRqrmntInfo) throws Exception;

	Map<String, String> queryRqrmntInfoApp(ReleaseRqrmntInfo releaseRqrmntInfo)throws Exception;

	List<Map> queryNotConfirmDocTasks(ReleaseRqrmntInfo releaseRqrmntInfo)throws Exception;

	Map queryTaskTestInfo(String task_id) throws Exception;

	Map queryInfoById(String id) throws Exception;
	
	Map<String, Object> querySecurityTestResult(Set<String> ids)throws Exception;
}
