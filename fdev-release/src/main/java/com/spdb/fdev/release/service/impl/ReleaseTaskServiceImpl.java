package com.spdb.fdev.release.service.impl;


import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IReleaseApplicationDao;
import com.spdb.fdev.release.dao.IReleaseTaskDao;
import com.spdb.fdev.release.entity.AuditRecord;
import com.spdb.fdev.release.entity.ReleaseApplication;
import com.spdb.fdev.release.entity.ReleaseNode;
import com.spdb.fdev.release.entity.ReleaseTask;
import com.spdb.fdev.release.service.*;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 投产任务业务实现类
 */
@Service
@RefreshScope
public class ReleaseTaskServiceImpl implements IReleaseTaskService {
	@Autowired
	private ISendEmailService sendEmailService;

	@Autowired
	private ICacheService cacheService;

	private static Logger logger = LoggerFactory.getLogger(ReleaseTaskServiceImpl.class);

    private static Logger logger_batch = LoggerFactory.getLogger(Dict.LOGGER_BATCH);

	@Value("${gitlab.manager.token}")
	private String token;
	@Autowired
	private IAppService appService;

	@Autowired
	private IAuditRecordService auditRecordService;

	@Autowired
	private ITaskService taskService;

	@Autowired
	private IReleaseTaskDao releaseTaskDao;

	@Autowired
	private IReleaseApplicationService releaseApplicationService;

	@Autowired
	private RestTransport restTransport;

	@Autowired
	private IReleaseApplicationDao releaseApplicationdao;

	@Autowired
	private ICommissionEventService commissionEventService;

	@Autowired
	private IProdApplicationService prodApplicationService;

	@Autowired
	private IRelDevopsRecordService relDevopsRecordService;

	@Autowired
	private IGitlabService gitlabService;

	@Autowired
	private IReleaseNodeService releaseNodeService;

	@Lazy
	@Autowired
	private IReleaseRqrmntInfoService releaseRqrmntInfoService;

	@Autowired
	private IReleaseRqrmntService releaseRqrmntService;
	
	@Autowired
	private IComponenService componenService;
	
	@Autowired
    private ITaskService iTaskService;

	@Override
	public Map<String, Object> queryDetailByTaskId(String taskid, String type) throws Exception {
		return releaseTaskDao.queryDetailByTaskId(taskid, type);
	}

	@Override
	public List<Map<String, Object>> queryBatchTasks(Map<String, String> requestParam) throws Exception {
		List<ReleaseTask> releaseTasks = releaseTaskDao.queryTasks(requestParam);
		List<Map<String, Object>> list = new ArrayList<>();
		List<String> ids = new ArrayList<>();
		for (ReleaseTask task : releaseTasks) {// 遍历任务列表
			String task_id = task.getTask_id();
			ids.add(task_id);
		}
		Map<String, Object> map = new HashMap<>();
		map.put(Dict.IDS, ids);// 发task模块获取task详细信息
		map.put(Dict.REST_CODE, "queryTaskDetailByIds");
		try {
			Map<String, Object> allTaskMap = (Map<String, Object>) restTransport.submit(map);
			for (ReleaseTask task : releaseTasks) {// 遍历任务列表
				Map beanMap = CommonUtils.beanToMap(task);
				Map<String, Object> taskmap = (Map<String, Object>) allTaskMap.get(task.getTask_id());
				if (CommonUtils.isNullOrEmpty(taskmap)) {
					continue;
				}
				beanMap.put(Dict.FEATURE_BRANCH, taskmap.get(Dict.FEATURE_BRANCH));
				list.add(beanMap);
			}
		} catch (Exception e) {
			logger.error("queryTaskDetail error with:" + e);
		}
		return list;
	}

	@Override
	public ReleaseTask addTask(String application_id, String task_id, String release_node_name, String type, String rqrmntNo) throws Exception {
		ReleaseTask releaseTask = releaseTaskDao.addTask(application_id, task_id, release_node_name, type, rqrmntNo);// 更新投产应用下任务列表
		// 增加审核记录
		AuditRecord auditRecord = new AuditRecord();
		auditRecord.setTask_id(task_id);
		auditRecord.setRelease_node_name(release_node_name);
		auditRecord.setOperation_type(Constants.OPERATION_TYPR_BEGIN);// 操作类型为0 发起
		auditRecordService.auditRecord(auditRecord);
		return releaseTask;
	}

	@Override
	public ReleaseTask deleteTask(String task_id, String type) throws Exception {
		ReleaseTask releaseTask = releaseTaskDao.findOneTask(task_id, type);
		if(CommonUtils.isNullOrEmpty(releaseTask)){
			logger.error("releaseTask does not exist");
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"任务id：" + task_id});
		}
		String application_id = releaseTask.getApplication_id();
		String release_node_name = releaseTask.getRelease_node_name();
		List<ReleaseTask> releaseTasks = releaseTaskDao.querySameAppTask(application_id, release_node_name);
		try {
			//任务阶段及uat rel时间回退
			taskService.rollbackTaskStatus(task_id, Dict.SIT, "", "");
			try {
				//删除待办事项
				commissionEventService.deleteCommissionEvent(task_id);
			} catch (Exception e) {
				logger.error("delete Commission event error" + e.getMessage());
				throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{"删除代办项异常，请稍后重试"});
			}
		} catch (Exception e) {
			logger.error("delete task update task stage to sit error");
			throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{"任务回退sit阶段异常，请稍后重试"});
		}
		if (releaseTasks.size() > 1) {//若该任务挂载的投产应用下只有一个任务 则删除该应用
			return releaseTaskDao.deleteTask(task_id, releaseTask.getType());
		} else {
			//类型为0 代表删除任务  及直接删除投产应用及变更应用
			//删除投产应用
			releaseApplicationdao.deleteApplication(application_id, release_node_name);
			//删除变更应用
			prodApplicationService.deleteApplicationByNode(release_node_name, application_id);
		}
		return releaseTaskDao.deleteTask(task_id, releaseTask.getType());
	}

	@Override
	public ReleaseTask deleteSitTask(String task_id) throws Exception {
		ReleaseTask releaseTask = releaseTaskDao.findOneTask(task_id, null);
		if (CommonUtils.isNullOrEmpty(releaseTask)) {
			return null;
		}
		String application_id = releaseTask.getApplication_id();
		String release_node_name = releaseTask.getRelease_node_name();
		List<ReleaseTask> releaseTasks = releaseTaskDao.querySameAppTask(application_id, release_node_name);
		if (releaseTasks.size() > 1) {//若该任务挂载的投产应用下只有一个任务 则删除该应用
			return releaseTaskDao.deleteTask(task_id, "1");
		} else {
			//类型为0 代表删除任务  及直接删除投产应用及变更应用
			//删除投产应用
			releaseApplicationdao.deleteApplication(application_id, release_node_name);
			//删除变更应用
			prodApplicationService.deleteApplicationByNode(release_node_name, application_id);
		}
		return releaseTaskDao.deleteTask(task_id, "1");
	}

	@Override
	public List<Map<String, Object>> queryTasks(Map<String, String> requestParam) throws Exception {
		List<ReleaseTask> releaseTasks = releaseTaskDao.queryTasks(requestParam);
		List<Map<String, Object>> list = new ArrayList<>();
		List<String> ids = new ArrayList<>();
		for (ReleaseTask task : releaseTasks) {// 遍历任务列表
			String task_id = task.getTask_id();
			ids.add(task_id);
		}
		Map<String, Object> map = new HashMap<>();
		map.put(Dict.IDS, ids);// 发task模块获取task详细信息
		map.put(Dict.REST_CODE, "queryTaskDetailByIds");
		try {
			Map<String, Object> allTaskMap = (Map<String, Object>) restTransport.submit(map);
			for (ReleaseTask task : releaseTasks) {// 遍历任务列表
				Map<String, Object> result = new HashMap<>();
				Map<String, Object> taskmap = (Map<String, Object>) allTaskMap.get(task.getTask_id());
				if (CommonUtils.isNullOrEmpty(taskmap)) {
					continue;
				}
				Map<String, String> group = (Map) taskmap.get(Dict.GROUP);
				result.put(Dict.TASK_GROUP, group.get(Dict.NAME));
				result.put(Dict.TASK_GROUP_ID, group.get(Dict.ID));
				List<Map<String, String>> spdbMaster = (List<Map<String, String>>) taskmap.get(Dict.SPDB_MASTER);
				result.put(Dict.BANK_MASTER, spdbMaster);
				List<Map<String, String>> devManager = (List<Map<String, String>>) taskmap.get(Dict.MASTER);
				result.put(Dict.DEV_MANAGERS, devManager);
				result.put(Dict.TASK_NAME, taskmap.get(Dict.NAME));
				result.put(Dict.TASK_STAGE, taskmap.get(Dict.STAGE));
				result.put(Dict.TASK_PROJECT, taskmap.get(Dict.PROJECT_NAME));
				result.put(Dict.REJECT_REASON, task.getReject_reason());
				result.put(Dict.TASK_ID, task.getTask_id());
				result.put(Dict.APPLICATION_ID, task.getApplication_id());
				result.put(Dict.TASK_STATUS, task.getTask_status());
				result.put(Dict.MERGE_RELEASE_FLAG, !CommonUtils.isNullOrEmpty(taskmap.get(Dict.UAT_MERGE_TIME)));
				result.put(Dict.MERGE_RELEASE_TIME, taskmap.get(Dict.UAT_MERGE_TIME));
				result.put("applicationType", taskmap.get("applicationType"));
				list.add(result);// 将详细的task放入list
			}
		} catch (Exception e) {
			logger.error("queryTaskDetail error with:{}", e);
		}
		//排序，没合并release分支的任务放在上面
		Collections.sort(list, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				return (boolean)o1.get(Dict.MERGE_RELEASE_FLAG) ? 0 : 1;
			}
		});
		//按是否合并release过滤
		String merge_release_flag = requestParam.get(Dict.MERGE_RELEASE_FLAG);
		if(!CommonUtils.isNullOrEmpty(merge_release_flag)) {
			list = list.stream().filter(task -> "true".equals(merge_release_flag) ? (boolean)task.get(Dict.MERGE_RELEASE_FLAG)
					: !(boolean)task.get(Dict.MERGE_RELEASE_FLAG)).collect(Collectors.toList());
		}
		return list;
	}


	@Override
	public ReleaseTask auditAdd(String task_id, String application_id, String release_node_name, String operation_type,
								String operateUser, String source_branch, String reject_reason) throws Exception {
		ReleaseNode releaseNode = releaseNodeService.queryDetail(release_node_name);
		if (operation_type.equals(Constants.OPERATION_TYPR_ACCESS)) {// 若操作类型为1，则代表同意
			ReleaseApplication releaseApplication = releaseApplicationService.findOneReleaseApplication(application_id,
					release_node_name);
			Map<String, Object> application = new HashMap<String, Object>();
			String new_add_sign = null;
			String uat_env_name = null;
			String rel_env_name = null;
			String type = releaseNode.getType();
			if (!"4".equals(type) && !"5".equals(type) && !"6".equals(type)) {
				application = appService.queryAPPbyid(application_id);
				String last_tag = prodApplicationService.queryLastTagByGitlabId(application_id, null, null,null);
				new_add_sign = "0".equals(application.get(Dict.NEW_ADD_SIGN)) || CommonUtils.isNullOrEmpty(last_tag) ? "1" : "0";
				uat_env_name = appService.queryByLabelsFuzzy(Dict.UAT_LOWER,
						(CommonUtils.isNullOrEmpty(application.get(Dict.NETWORK))
								|| ((String)application.get(Dict.NETWORK)).indexOf(",") != -1)
						? "dmz" : (String)application.get(Dict.NETWORK));
				rel_env_name = appService.queryByLabelsFuzzy(Dict.REL.toLowerCase(),
						(CommonUtils.isNullOrEmpty(application.get(Dict.NETWORK))
								|| ((String)application.get(Dict.NETWORK)).indexOf(",") != -1)
						? "dmz" : (String)application.get(Dict.NETWORK));
			}
			if (CommonUtils.isNullOrEmpty(releaseApplication)) {
				// 若该任务的投产应用为空，则插入应用信息并 拉取新的release分支 并入库
				releaseApplication = new ReleaseApplication();
				String branchName;
				if("3".equals(releaseNode.getType())) {
					branchName = "testrun-" + release_node_name;
				} else {
					branchName = "release-" + release_node_name;
				}
				AuditRecord auditRecord = new AuditRecord();
				auditRecord.setRelease_node_name(release_node_name);
				auditRecord.setTask_id(task_id);
				auditRecord.setOperation_type(Constants.OPERATION_TYPR_BEGIN);// 通过发起记录 查询发起人的gittoken
				String user_name_en = auditRecordService.queryRecordOperation(auditRecord);// 查询审查记录中的发起人英文名
				String token = cacheService.queryUserGitlabToken(user_name_en);
				// 则从master上拉取新的release分支
				if (CommonUtils.isNullOrEmpty(source_branch)) {
					source_branch = Dict.MASTER;
				}
				try {
					Map<String, Object> task = iTaskService.queryTaskDetail(task_id);
					String applicationType = (String) task.get("applicationType");
					if (!CommonUtils.isNullOrEmpty(applicationType) && applicationType.contains("app")) {
						appService.createReleaseBranch(application_id, branchName, source_branch, token);
					} else {
						componenService.createReleaseBranch(application_id, branchName, source_branch, applicationType);
					}
				} catch (FdevException e) {
					if (e.getMessage().contains(Dict.BRANCH_U)) {
						throw new FdevException(ErrorConstants.NEED_DELETE_SAME_BRANCH, new String[]{branchName});
					}
					throw e;
				}
				releaseApplication.setApplication_id(application_id);
				releaseApplication.setRelease_node_name(release_node_name);
				releaseApplication.setRelease_branch(branchName);
				releaseApplication.setUat_env_name(uat_env_name);
				releaseApplication.setRel_env_name(rel_env_name);
				releaseApplication.setNew_add_sign(new_add_sign);
				releaseApplicationService.saveReleaseApplication(releaseApplication);
			} else {
				boolean flag = false;
				if (CommonUtils.isNullOrEmpty(releaseApplication.getUat_env_name())) {
					releaseApplication.setUat_env_name(uat_env_name);
					flag = true;
				}
				if (CommonUtils.isNullOrEmpty(releaseApplication.getRel_env_name())) {
					releaseApplication.setRel_env_name(rel_env_name);
					flag = true;
				}
				if (flag) {
					releaseApplicationService.updateReleaseApplication(releaseApplication);
				}
			}
			if(!"3".equals(releaseNode.getType())) {
				try {
					//修改待办事项为已处理
					commissionEventService.updateAuditTaskCommissionEvent(task_id);
				} catch (Exception e) {
					logger.error("audit task update commissionEvent error,e:" + e.getMessage());
				}
			}
		} else {
			try {
				commissionEventService.deleteCommissionEvent(task_id);
			} catch (Exception e) {
				logger.error("audit task delete commissionEvent error,e:" + e.getMessage());
			}
		}
		ReleaseTask releaseTask = releaseTaskDao.auditAdd(task_id, release_node_name, operation_type, reject_reason);
		// 记录操作流水
		AuditRecord auditRecord = new AuditRecord();
		auditRecord.setTask_id(task_id);
		auditRecord.setRelease_node_name(release_node_name);
		auditRecord.setOperation_type(operation_type);
		auditRecordService.auditRecord(auditRecord);
		return releaseTask;
	}

	@Override
	public ReleaseTask findOneTask(String task_id) throws Exception {
		// 通过投产任务id 查询唯一的投产任务对象
		return releaseTaskDao.findOneTask(task_id, null);
	}

	@Override
	public ReleaseTask findOneTask(String task_id, String type) throws Exception {
		// 通过投产任务id 查询唯一的投产任务对象
		return releaseTaskDao.findOneTask(task_id, type);
	}

	@Override
	public ReleaseTask findConfirmTask(String task_id) throws Exception {
		return releaseTaskDao.findConfirmTask(task_id);
	}

	@Override
	public ReleaseTask updateReleaseTask(ReleaseTask releaseTask) throws Exception {
		AuditRecord auditRecord = new AuditRecord();
		auditRecord.setTask_id(releaseTask.getTask_id());
		auditRecord.setRelease_node_name(releaseTask.getRelease_node_name());
		auditRecord.setOperation_type(Constants.OPERATION_TYPR_BEGIN);// 操作类型为0 发起
		auditRecordService.auditRecord(auditRecord);
		releaseTask = releaseTaskDao.updateReleaseTask(releaseTask);
		return releaseTask;
	}

	@Override
	public List<ReleaseTask> queryTaskStatusOfApp(String release_node_name, String application_id) throws Exception {
		return releaseTaskDao.queryTaskStatusOfApp(release_node_name, application_id);
	}

	@Override
	public List<ReleaseTask> queryReleaseTaskByAppid(String appid, String release_node_name) throws Exception {
		return releaseTaskDao.queryReleaseTaskByAppid(appid, release_node_name);

	}


	@Override
	public List<String> releaseTaskByNodeName(List<String> release_node_names) throws Exception {
		return releaseTaskDao.releaseTaskByNodeName(release_node_names);
	}

	@Override
	public void updateTaskProduction(String task_id) throws Exception {
		releaseTaskDao.updateTaskProduction(task_id);
	}

	@Override
	public List<String> queryTaskByNodeDate(String archivedDay) throws Exception {
		return releaseTaskDao.queryTaskByNodeDate(archivedDay);
	}

	@Override
	public void changeReleaseNode(String task_id, String release_node_name_new, ReleaseTask releaseTask) throws Exception {
		String application_id = releaseTask.getApplication_id();
		String release_node_name = releaseTask.getRelease_node_name();
		ReleaseNode releaseNode = releaseNodeService.queryDetail(release_node_name);
		List<ReleaseTask> releaseTasks = releaseTaskDao.querySameAppTask(application_id, release_node_name);
		ReleaseApplication releaseApplication = new ReleaseApplication();
		releaseApplication.setApplication_id(application_id);
		releaseApplication.setRelease_node_name(release_node_name_new);
		releaseApplication = releaseApplicationService.queryApplicationDetail(releaseApplication);
		//若该任务挂载的投产应用下不只有一个任务 则只删除任务
		if (releaseTasks.size() > 1) {
			rollbackTask(task_id, releaseNode.getType());
			//若新窗口包含此应用则 只删除任务
		} else if (!CommonUtils.isNullOrEmpty(releaseApplication)) {
			releaseApplicationService.deleteApplication(application_id, release_node_name);
			prodApplicationService.deleteApplicationByNode(release_node_name, application_id);
			rollbackTask(task_id, releaseNode.getType());
		} else {
		    ReleaseApplication releaseApplication_old = new ReleaseApplication();
            releaseApplication_old.setApplication_id(application_id);
            releaseApplication_old.setRelease_node_name(release_node_name);
            releaseApplication_old = releaseApplicationService.queryApplicationDetail(releaseApplication_old);
			String branchHead = "3".equals(releaseTask.getType()) ? "testrun" : "release";
			String newBranchName = new StringBuilder(branchHead).append("-").append(release_node_name_new).toString();
			Map<String, Object> app = new HashMap<String, Object>();
			Integer gitlabId = null;
			Map<String, Object> taskInfo = taskService.queryTaskInfo(task_id);
			String taskType  = (String) taskInfo.get("applicationType");
			if ("componentWeb".equals(taskType)) {//前端组件
				app = componenService.queryMpassComponentDetail(application_id);
				String gitLabId = (String) app.get("gitlab_id");
				gitlabId = Integer.parseInt(gitLabId);
			} else if ("componentServer".equals(taskType)) {//后端组件
				app = componenService.queryComponenbyid(application_id);
				String gitLabId = (String) app.get("gitlab_id");
				gitlabId = Integer.parseInt(gitLabId);
			} else if ("archetypeWeb".equals(taskType)) {//前端骨架
				app = componenService.queryMpassArchetypeDetail(application_id);
				String gitLabId = (String) app.get("gitlab_id");
				gitlabId = Integer.parseInt(gitLabId);
			} else if ("archetypeServer".equals(taskType)) {//后端骨架
				app = componenService.queryArchetypeDetail(application_id);
				String gitLabId = (String) app.get("gitlab_id");
				gitlabId = Integer.parseInt(gitLabId);
			} else if ("image".equals(taskType)) {//镜像
				app = componenService.queryBaseImageDetail(application_id);
				String gitLabId = (String) app.get("gitlab_id");
				gitlabId = Integer.parseInt(gitLabId);
			} else {
				app = appService.queryAPPbyid(application_id);
				gitlabId = (Integer) app.get(Dict.GITLAB_PROJECT_ID);
			}
			// 创建新分支
			gitlabService.createBranch(gitlabId, newBranchName, releaseApplication_old.getRelease_branch());
			// 将新分支设置为受保护
			gitlabService.setProtectedBranches(gitlabId, newBranchName);
			// 删除原release分支
			gitlabService.deleteBranch(gitlabId, releaseApplication_old.getRelease_branch());
			//否则为修改任务投产窗口  逻辑为将原窗口的应用及相信息迁移到新的投产窗口中
			releaseTaskDao.updateTaskNode(releaseTask, release_node_name_new);
			releaseApplicationService.changeReleaseNodeName(release_node_name, release_node_name_new, application_id, newBranchName);
			relDevopsRecordService.changeReleaseNodeName(release_node_name, release_node_name_new, application_id);
			prodApplicationService.deleteApplicationByNode(release_node_name, application_id);
		}

	}

	@Override
	public Set<String> queryTaskByInterval(Map<String, String> requestParam) throws Exception {
		String owner_groupId = requestParam.get(Dict.OWNER_GROUPID);
		Set<String> list = releaseTaskDao.queryTaskByInterval(requestParam);
		if (CommonUtils.isNullOrEmpty(list)) {
			return list;
		}
		Map<String, Object> task = taskService.queryTasksByIds(list);
		Iterator<String> it = list.iterator();
		while (it.hasNext()) {
			Map map = (Map) task.get(it.next());
			if (CommonUtils.isNullOrEmpty(map)) {
				it.remove();
				continue;
			}
			Map group = (Map) map.get(Dict.GROUP);
			String group_id = String.valueOf(group.get(Dict.ID));
			if (!group_id.equals(owner_groupId)) {
				it.remove();
			}
		}
		return list;
	}

	public void rollbackTask(String task_id, String type) throws Exception {
		//任务阶段及uat rel时间回退
		taskService.rollbackTaskStatus(task_id, Dict.SIT,"", "");
		//删除待办事项
		commissionEventService.deleteCommissionEvent(task_id);
		releaseTaskDao.deleteTask(task_id, type);
	}
	@Override
	public void updateReleaseNodeName(String old_release_node_name, String release_node_name) throws Exception {
		releaseTaskDao.updateReleaseNodeName(old_release_node_name,release_node_name);
	}

	@Override
	public List<ReleaseTask> queryTasksByExecutor(Map<String, String> param) throws Exception {
		//投产点名称
		String release_node_name = param.get(Dict.RELEASE_NODE_NAME);
		//应用id
		String application_id = param.get(Dict.APPLICATION_ID);
		return releaseTaskDao.queryTasksByExecutor(application_id, release_node_name);
	}

	@Override
	public void updateTaskArchivedBatch(Map<String, String> param) throws Exception {
        MDC.put(Dict.METHOD_NAME, "updateTaskArchivedBatch");
		String start_date = param.get(Dict.START_DATE);
		String end_date = param.get(Dict.END_DATE);
		//1.返回需要归档的窗口并对窗口归档
		List<ReleaseNode> releaseNodes = releaseNodeService.updateReleaseNodeBatch(start_date, end_date);
		//2.针对窗口返回所有的投产任务
		if (CommonUtils.isNullOrEmpty(releaseNodes)) {
			return;
		}
		List<String> release_node_names = new ArrayList<>();
		releaseNodes.forEach(releaseNode -> release_node_names.add(releaseNode.getRelease_node_name()));
		StringBuilder nodeNames = new StringBuilder();
		for(String nodeName: release_node_names) {
		    nodeNames.append("|").append(nodeName);
        }
		logger_batch.info("{}至{}时间内查到的投产窗口数为：{}，窗口名分别为：{}", start_date, end_date, release_node_names.size(), nodeNames);
		List<ReleaseTask> releaseTasks = releaseTaskDao.queryReleaseTaskByNames(release_node_names);
		if (CommonUtils.isNullOrEmpty(releaseTasks)) {
			return;
		}
		//3.发送任务模块归档任务
		List<String> task_ids = this.updateTaskProductions(releaseTasks);
		StringBuilder taskIds = new StringBuilder();
		for(String taskId : task_ids) {
            taskIds.append("|").append(taskId);
        }
		logger_batch.info("任务模块返回的任务id数量为：{}，id分别为：{}", task_ids.size(), taskIds.toString());
		//4.根据任务id更新投产任务状态
		this.updateReleaseTaskProductions(task_ids);
		MDC.clear();
	}

	/**
	 * 发送任务模块接口进行任务状态的变更
 	 * @param releaseTasks
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<String> updateTaskProductions(List<ReleaseTask> releaseTasks) throws Exception {
		Map<String, Object> param = new HashMap<>();
		StringBuilder taskIds = new StringBuilder();
		for (ReleaseTask releaseTask : releaseTasks){
            taskIds.append("|").append(releaseTask.getTask_id());
			String fire_time = releaseTask.getRelease_node_name().split("_")[0];
			StringBuilder sb = new StringBuilder();
			String release_date = sb.append(fire_time.substring(0, 4)).append("/").append(fire_time.substring(4, 6)).append("/").append(fire_time.substring(6,8)).toString();
			Map<String, Object> map =new HashMap<>();
			map.put(Dict.ID, releaseTask.getTask_id());
			map.put(Dict.STAGE, Dict.TASK_STAGE_PRODUCTION);
			map.put(Dict.FIRE_TIME, release_date);
			param.put(releaseTask.getTask_id(), map);
		}
		logger_batch.info("投产模块查到的任务数：{}，任务id为：{}", releaseTasks.size(), taskIds.toString());
		param.put(Dict.REST_CODE, "updateBulkTask");
		return (List<String>)restTransport.submit(param);
	}

	/***
	 * 通过任务id进行投产任务的更新
	 * @param task_ids
	 * @throws Exception
	 */
	@Override
	public void updateReleaseTaskProductions(List<String> task_ids) throws Exception {
		//不为空进行更新
		if(!CommonUtils.isNullOrEmpty(task_ids)){
			releaseTaskDao.updateReleaseTaskProductions(task_ids);
		}

	}


	public List<ReleaseTask> queryReleaseTaskByAppIdNoTaskId(String appId, String release_time, String task_id) throws Exception {
		return releaseTaskDao.queryReleaseTaskByAppIdNoTaskId(appId, release_time, task_id);
	}

	@Override
	public List<ReleaseTask> queryReleaseByNodeName(String date) {
		return releaseTaskDao.queryReleaseByNodeName(date);
	}

	@Override
	public List<ReleaseTask> queryCurrentTaskByNodeName(String release_node_name, List<String> status, List<String> ids) {
		return releaseTaskDao.queryCurrentTaskByNodeName(release_node_name, status, ids);
	}

	@Override
	public void deleteReleaseProdApplication(String task_id, String release_node_name) throws Exception {
		ReleaseTask releaseTask = releaseTaskDao.queryByTaskIdNode(task_id, release_node_name);
		if(!CommonUtils.isNullOrEmpty(releaseTask)) {
			List<ReleaseTask> releaseTasks = releaseTaskDao.querySameAppTask(releaseTask.getApplication_id(),
					release_node_name);
			if(releaseTasks.size() == 1) {
				releaseApplicationdao.deleteApplication(releaseTask.getApplication_id(), release_node_name);
				//删除变更应用
				prodApplicationService.deleteApplicationByNode(release_node_name, releaseTask.getApplication_id());
			}
		}
	}

	@Override
	public ReleaseTask queryByTaskIdNode(String task_id, String release_node_name) {
		return releaseTaskDao.queryByTaskIdNode(task_id, release_node_name);
	}

	@Override
	public List<String> findReleaseTaskByReleaseNodeName(String release_node_name) {
		return releaseTaskDao.findReleaseTaskByReleaseNodeName(release_node_name);
	}

    @Override
    public ReleaseTask deleteOnlyTask(String task_id, String type) throws Exception {
		ReleaseTask releaseTask = releaseTaskDao.findOneTask(task_id, type);
		if(CommonUtils.isNullOrEmpty(releaseTask)){
			logger.error("releaseTask does not exist");
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"任务id：" + task_id});
		}
		String application_id = releaseTask.getApplication_id();
		String release_node_name = releaseTask.getRelease_node_name();
		List<ReleaseTask> releaseTasks = releaseTaskDao.querySameAppTask(application_id, release_node_name);
		if(releaseTasks.size() == 1) {
			//删除投产应用
			releaseApplicationdao.deleteApplication(application_id, release_node_name);
		}
        return releaseTaskDao.deleteTask(task_id, type);
    }

	@Override
	public List<ReleaseTask> queryByReleaseNodeName(String release_node_name) {
		return releaseTaskDao.queryByReleaseNodeName(release_node_name);
	}

	@Override
	public List<Map<String, Object>> proWantWindow(String time, String ownerGroupId) {
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> map = new HashMap<>();
		map.put(Dict.PROWANTWINDOW,time);
		map.put(Dict.GROUP, ownerGroupId);
		map.put(Dict.REST_CODE, "proWantWindow");
		try {
			list = (List<Map<String, Object>>) restTransport.submit(map);
		} catch (Exception e) {
			logger.error("proWantWindow error with:" + e);
		}
		return list;
	}

	@Override
	public void updateReleaseTaskRqrmntId(String task_id, String rqrmntId) throws Exception {
		releaseTaskDao.updateReleaseTaskRqrmntId(task_id, rqrmntId);
	}

	@Override
	public List<ReleaseTask> queryNecessaryTask(String release_node_name) {
		return releaseTaskDao.queryNecessaryTask(release_node_name);
	}

	@Override
	public List<ReleaseTask> queryRefusedTask(String release_node_name) {
		return releaseTaskDao.queryRefusedTask(release_node_name);
	}
	
	@Override
	public void updateTaskByComponen(String task_id, String type) throws Exception {
		releaseTaskDao.updateTaskByComponen(task_id,type);
	}
}
