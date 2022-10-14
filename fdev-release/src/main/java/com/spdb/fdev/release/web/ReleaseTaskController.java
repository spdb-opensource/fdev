package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.ReleaseNode;
import com.spdb.fdev.release.entity.ReleaseRqrmntInfo;
import com.spdb.fdev.release.entity.ReleaseTask;
import com.spdb.fdev.release.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.*;

@Api(tags = "投产任务接口")
@RequestMapping("/api/task")
@RestController
public class ReleaseTaskController {
	@Autowired
	IReleaseTaskService releaseTaskService;
	@Autowired
	ITaskService taskService;
	@Autowired
	IReleaseNodeService releaseNodeService;
	@Autowired
	IRoleService roleService;
	@Autowired
	ICommissionEventService commissionEventService;
	@Autowired
	IAppService appService;
	@Autowired
	ISendEmailService sendEmailService;
	@Autowired
	IReleaseRqrmntService releaseRqrmntService;
    @Autowired
    ICacheService cacheService;
    @Autowired
    IGitlabService gitlabService;
	@Lazy
	@Autowired
	IReleaseRqrmntInfoService releaseRqrmntInfoService;
	@Autowired
	private IDbReviewService dbReviewService;
	@Autowired
	private IUserService userService;
	@Autowired
    private IComponenService componenService;

	private static Logger logger = LoggerFactory.getLogger(ReleaseTaskController.class);

	private static Logger logger_batch = LoggerFactory.getLogger(Dict.LOGGER_BATCH);

	@RequestValidate(NotEmptyFields = { Dict.TASK_ID })
	@PostMapping(value = "/queryDetailByTaskId")
	public JsonResult queryDetailByTaskId(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String task_id = requestParam.get(Dict.TASK_ID);
		String type = requestParam.get(Dict.TYPE);
		// 通过应用id返回该投产任务的投产窗口信息、所在投产应用信息及任务信息
		Map<String, Object> map = releaseTaskService.queryDetailByTaskId(task_id, type);
		return JsonResultUtil.buildSuccess(map);
	}

	@RequestValidate(NotEmptyFields = { Dict.RELEASE_NODE_NAME, Dict.TASK_ID })
	@PostMapping(value = "/add")
	public JsonResult addTask(
			@RequestBody @ApiParam(name = "参数", value = "示例{ \"task_id\":\"任务id\",\"release_node_name\":\"投产窗口名称\" }") Map<String, String> requestParam)
			throws Exception {
		ReleaseTask releaseTask = new ReleaseTask();
		String task_id = requestParam.get(Dict.TASK_ID);
		if (!roleService.isTaskManager(task_id)) {
			logger.error("the user must be this task manager! @@@@@taskid={}", task_id);
			throw new FdevException(ErrorConstants.ROLE_ERROR);
		}
		String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);

		// 查询其release_nodes窗口对象
		ReleaseNode releaseNode = releaseNodeService.queryDetail(release_node_name);

		if (CommonUtils.isNullOrEmpty(releaseNode)) {
			// 窗口对象若为空则报错
			logger.error("releasenode is empty!");
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "投产窗口不存在" });
		}
		// 查询该任务所在的应用晚于当前日期的所有该应用的投产点 若不存在或等于该投产点 则允许挂载
		Map<String, Object> taskInfo = taskService.queryTaskInfo(task_id);
		if (CommonUtils.isNullOrEmpty(taskInfo)) {
			logger.error("cani't find this task info !@@@@@ taskid={}" , task_id);
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "未查询到任务详情信息" });
		}
		String appid = (String) taskInfo.get(Dict.PROJECT_ID);
		String taskType = (String) taskInfo.get("applicationType");
		// 判断应用与投产窗口类型是否匹配
		String type = null;
		if (!CommonUtils.isNullOrEmpty(taskType) && taskType.contains("app")) {
			// 判断应用与投产窗口类型是否匹配
			valReleaseNodeAppType(releaseNode, appid);
	        type = "3".equals(releaseNode.getType()) ? releaseNode.getType() : "";
		} else if ("componentWeb".equals(taskType) || "componentServer".equals(taskType)) {
			type = "4".equals(releaseNode.getType()) ? releaseNode.getType() : "";
			if (!"4".equals(releaseNode.getType())) {
				throw new FdevException(ErrorConstants.RELEASE_NODE_TYPE_UNNORMAL, new String[]{"该应用不能挂载在组件窗口"});
			}
		} else if ("archetypeWeb".equals(taskType) || "archetypeServer".equals(taskType)) {
			type = "5".equals(releaseNode.getType()) ? releaseNode.getType() : "";
			if (!"5".equals(releaseNode.getType())) {
				throw new FdevException(ErrorConstants.RELEASE_NODE_TYPE_UNNORMAL, new String[]{"该应用不能挂载在骨架窗口"});
			}
		} else if ("image".equals(taskType)) {
			type = "6".equals(releaseNode.getType()) ? releaseNode.getType() : "";
			if (!"6".equals(releaseNode.getType())) {
				throw new FdevException(ErrorConstants.RELEASE_NODE_TYPE_UNNORMAL, new String[]{"该应用不能挂载在镜像窗口"});
			}
		}
		//判断此投产窗口所属小组是否与该应用当天投产选择的投产窗口小组一致
        valReleaseNodeGroupId(appid, release_node_name, task_id, releaseNode, taskType);
		ReleaseTask findOneTask = releaseTaskService.findOneTask(task_id, type);
		String rqrmntNo = String.valueOf(taskInfo.get(Dict.RQRMNT_NO));
		if (!CommonUtils.isNullOrEmpty(findOneTask)) {
			if (!Constants.TASK_RLS_REJECTED.equals(findOneTask.getTask_status())) { // 查询投产任务 若已存在且任务状态不为2则报错
				logger.error("this task already exist! @@@@@taskid={}" , task_id);
				throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[] { "已存在投产任务" });
			}
			// 查询投产任务 若已存在任务状态为2则更新任务状态为0
			releaseTask.setTask_id(task_id);
			releaseTask.setTask_status(Constants.TASK_RLS_NEED_AUDIT);
			releaseTask.setRelease_node_name(release_node_name);
			releaseTask.setReject_reason("");
			releaseTask.setType(releaseNode.getType());
			releaseTask.setUpdate_time(CommonUtils.formatDate("yyyy-MM-dd"));
			releaseTask = releaseTaskService.updateReleaseTask(releaseTask);
		} else {
			releaseTask = releaseTaskService.addTask(appid, task_id, release_node_name, releaseNode.getType(), rqrmntNo);
		}
		try {
			// 添加待办与推送消息
			commissionEventService.addAuditTaskCommissionEvent(task_id, releaseNode.getType());
		} catch (Exception e) {
			logger.error("addTask add commissionEvent error, e",e);
		}

		return JsonResultUtil.buildSuccess(releaseTask);
	}

	private void valReleaseNodeAppType(ReleaseNode release_node, String appId) throws Exception {
		Map<String, Object> application = appService.queryAPPbyid(appId);
		if("IOS应用".equals(application.get(Dict.TYPE_NAME)) || "Android应用".equals(application.get(Dict.TYPE_NAME))) {
			if("1".equals(release_node.getType())) {
				throw new FdevException(ErrorConstants.RELEASE_NODE_TYPE_UNNORMAL, new String[]{"Android或IOS应用不能挂载在微服务窗口"});
			}
		} else {
			if("2".equals(release_node.getType()) || "3".equals(release_node.getType())) {
				throw new FdevException(ErrorConstants.RELEASE_NODE_TYPE_UNNORMAL, new String[]{"此应用只能挂载在微服务窗口"});
			}
		}
	}

	@OperateRecord(operateDiscribe="投产模块-取消投产")
	@RequestValidate(NotEmptyFields = { Dict.TASK_ID })
	@PostMapping(value = "/deleteTask")
	public JsonResult deleteTask(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String task_id = requestParam.get(Dict.TASK_ID);
		String type = requestParam.get(Dict.TYPE);
		if(!roleService.isTaskSpdbManager(task_id)) {
			throw new FdevException(ErrorConstants.ROLE_ERROR,new String [] {"请联系任务的行内负责人"});
		}
		ReleaseTask releaseTask;
		if(!CommonUtils.isNullOrEmpty(type) && "3".equals(type)) {
			releaseTask = releaseTaskService.deleteOnlyTask(task_id, type);
		} else {
			releaseTask = releaseTaskService.deleteTask(task_id, type);
			releaseRqrmntService.deleteRqrmntTask(task_id, releaseTask.getRelease_node_name());
			releaseRqrmntInfoService.cancelRelease(releaseTask);
		}
		return JsonResultUtil.buildSuccess(releaseTask);
	}

	/**
	 * 批量给即将投产任务的相关人员发邮件
	 * 以下方法名与参数名（英文名称）错误，备注意思清楚，故不做修改
	 * @param requestParam 当前日期
	 * @return
	 * @throws Exception
	 */
    @RequestValidate(NotEmptyFields = { Dict.RELEASE_DATE })
	@PostMapping(value = "/cleanDelayedTasks")
	public JsonResult cleanDelayedTasks(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		MDC.put(Dict.METHOD_NAME, "cleanDelayedTasks");
	    String release_date = requestParam.get(Dict.RELEASE_DATE);
        String date;
		// T+1日
        try {
            date = TimeUtils.getDaysAgoStringDate(release_date, Constants.DATE_ONE_DAYS);
        } catch (ParseException e) {
            throw new FdevException(ErrorConstants.DATE_FROMAT_ERROR);
        }
		handleTask(date, Constants.DATE_ONE_DAYS);
		// T+2日
		date = TimeUtils.getDaysAgoStringDate(release_date, Constants.DATE_TWO_DAYS);
		handleTask(date, Constants.DATE_TWO_DAYS);
		// T+3日
		date = TimeUtils.getDaysAgoStringDate(release_date, Constants.DATE_THREE_DAYS);
		handleTask(date, Constants.DATE_THREE_DAYS);
		MDC.clear();
		return JsonResultUtil.buildSuccess(null);
	}

	private Set<String> getReleaseTaskIds(List<ReleaseTask> releaseTasks) {
		Set<String> set = new HashSet<>();
		for(ReleaseTask releaseTask : releaseTasks) {
			set.add(releaseTask.getTask_id());
		}
		return set;
	}

	private void handleTask(String date, int days_after) throws Exception {
		logger_batch.info("当前处理{}的任务", date);
		List<ReleaseTask> releaseTasks = releaseTaskService.queryReleaseByNodeName(date);
		logger_batch.info("{}是否包含状态为0或1的任务：{}，任务数量：{}", date, !CommonUtils.isNullOrEmpty(releaseTasks), releaseTasks.size());
		if(!CommonUtils.isNullOrEmpty(releaseTasks)) {
			Set<String> ids = getReleaseTaskIds(releaseTasks);
			// 查询所有任务当前的状态
			Map<String, Object> allTaskMap = taskService.queryTasksByIds(ids);
			for(ReleaseTask releaseTask : releaseTasks) {
			    if(!"3".equals(releaseTask.getType())) {
                    Map<String, Object> taskmap = (Map<String, Object>) allTaskMap.get(releaseTask.getTask_id());
                    //判断此任务是否为空
                    boolean taskMapIsNull = CommonUtils.isNullOrEmpty(taskmap);
                    logger_batch.info("任务：【{}】所属阶段{}",
                            !taskMapIsNull ? taskmap.get(Dict.NAME) : releaseTask.getTask_id(),
                            !taskMapIsNull ? taskmap.get(Dict.STAGE) : "未查到此任务");
                    // 若任务仍处于sit或uat阶段，则发送告警通知给所有此任务开发人员、任务负责人、任务行内负责人
                    if(!taskMapIsNull &&
                            (Dict.SIT.equals(taskmap.get(Dict.STAGE)) || Dict.UAT_LOWER.equals(taskmap.get(Dict.STAGE)))) {
                        taskmap.put(Dict.TASK_ID, releaseTask.getTask_id());//任务id
                        taskmap.put(Dict.RELEASE_NODE_NAME, releaseTask.getRelease_node_name());//投产窗口名称
                        taskmap.put(Dict.DATE, days_after);//n天后将被清理
                        sendEmailService.sendEmailCleanDelayedTasks(new HashMap(taskmap));
                    }
                }
			}
		}
	}

	@RequestValidate(NotEmptyFields = { Dict.TASK_ID })
	@PostMapping(value = "/deleteSitTask")
	public JsonResult deleteSitTask(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String task_id = requestParam.get(Dict.TASK_ID);
		ReleaseTask releaseTask = releaseTaskService.deleteSitTask(task_id);
		return JsonResultUtil.buildSuccess(releaseTask);
	}

	@RequestValidate(NotEmptyFields = { Dict.RELEASE_NODE_NAME })
	@PostMapping(value = "/queryTasks")
	public JsonResult queryTasks(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		List<Map<String, Object>> queryTasks;
		// 使用 release_node_name： 必传 和 application_id： 可不传 查询任务详情
		queryTasks = releaseTaskService.queryTasks(requestParam);
		return JsonResultUtil.buildSuccess(queryTasks);
	}

	@OperateRecord(operateDiscribe="投产模块-确认投产")
	@RequestValidate(NotEmptyFields = { Dict.TASK_ID, Dict.RELEASE_NODE_NAME })
	@PostMapping(value = "/auditAdd")
	public JsonResult auditAdd(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		// 当前操作人员operate_user（session中获取“_USER”）必须为该投产任务的行内负责人
		String task_id = requestParam.get(Dict.TASK_ID);
		String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
		String operation_type = requestParam.get(Dict.OPERATION_TYPE);
		String reject_reason = requestParam.get(Dict.REJECT_REASON);
		String source_branch = requestParam.get(Dict.SOURCE_BRANCH);
		String uat_testobject = requestParam.get(Dict.UAT_TESTOBJECT);
		// 拒绝投产必须填写拒绝理由
		if (Constants.OPERATION_TYPR_REFUSE.equals(operation_type) && CommonUtils.isNullOrEmpty(reject_reason)) {
			throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[] { Dict.REJECT_REASON });
		}
		if (!roleService.isTaskSpdbManager(task_id)) {
			logger.error("the user must be bank master!");
			throw new FdevException(ErrorConstants.ROLE_ERROR, new String[] { "非行内负责人无法审核任务" });
		}
		// 发任务模块查询该任务对应的应用id
		String appid = (String) taskService.queryTaskInfo(task_id).get(Dict.PROJECT_ID);
		if (CommonUtils.isNullOrEmpty(appid)) {
			logger.error("can't find task info! @@@@@taskid={}" , task_id);
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "未查询到任务相关应用" });
		}
		if(Constants.OPERATION_TYPR_REFUSE.equals(operation_type)) {//若拒绝投产则拼接拒绝理由
			String user_name_cn = CommonUtils.getSessionUser().getUser_name_cn();
			reject_reason=reject_reason+"--"+user_name_cn;
		}
		ReleaseTask releaseTask = releaseTaskService.auditAdd(task_id, appid, release_node_name, operation_type,
				CommonUtils.getSessionUser().getUser_name_en(), source_branch, reject_reason);
		// 行内负责人确认投产 保存任务uat承接方 保存任务投产类文档
		if(Constants.OPERATION_TYPR_ACCESS.equals(operation_type) && !"3".equals(releaseTask.getType())) {
			releaseRqrmntInfoService.auditAddRqrmntInfo(releaseTask, uat_testobject);
		}
		//存储数据库审核文件
		dbReviewService.dbReviewUpload(task_id);
		return JsonResultUtil.buildSuccess(releaseTask);
	}

	@OperateRecord(operateDiscribe="投产模块-确认已投产")
	@RequestValidate(NotEmptyFields = { Dict.TASK_ID })
	@PostMapping(value = "/updateTaskArchived")
	public JsonResult updateTaskArchived(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String task_id = requestParam.get(Dict.TASK_ID);
		String type = requestParam.get(Dict.TYPE);
		if(!roleService.isTaskManager(task_id)) {
			throw new FdevException(ErrorConstants.ROLE_ERROR);
		}
		try {
			//修改任务模块任务阶段
			taskService.updateTaskProduction(task_id);
			//修改投产表中任务状态
			if ("4".equals(type) || "5".equals(type) || "6".equals(type)) {
				releaseTaskService.updateTaskByComponen(task_id,type);
			} else {
				releaseTaskService.updateTaskProduction(task_id);
			}
		} catch (Exception e) {
			throw new FdevException(ErrorConstants.UPDATE_TASKSTATUS_FAILED);
		}
		return JsonResultUtil.buildSuccess(task_id);
	}

    @RequestValidate(NotEmptyFields = {Dict.TASK_ID, Dict.RELEASE_NODE_NAME, Dict.RELEASE_BRANCH})
    @PostMapping(value = "/pullGrayBranch")
    public JsonResult pullGrayBranch(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String task_id = requestParam.get(Dict.TASK_ID);
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        String release_branch = requestParam.get(Dict.RELEASE_BRANCH);
        Map<String, Object> taskInfo = taskService.queryTaskInfo(task_id);
        String appId = (String) taskInfo.get(Dict.PROJECT_ID);
        Map<String, Object> application = appService.queryAPPbyid(appId);
        Integer gitlab_id = (Integer)application.get(Dict.GITLAB_PROJECT_ID);
        String grayBranch = "gray-" + release_node_name;
        User user = CommonUtils.getSessionUser();
        String token = cacheService.queryUserGitlabToken(user.getUser_name_en());
        if(!gitlabService.checkBranchExists(gitlab_id, release_branch)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{release_branch + "分支不存在"});
		}
        String tips;
        if(gitlabService.checkBranchExists(gitlab_id, grayBranch)) {
        	try {
				gitlabService.createMergeRequest(gitlab_id, release_branch, grayBranch,
						(String) taskInfo.get(Dict.NAME), "合并灰度分支");
				tips = "已发起" + release_branch + "分支向" + grayBranch + "分支的合并请求";
			} catch (Exception e) {
				tips = release_branch + "分支向" + grayBranch + "分支的合并请求已存在，请点击合并";
			}
		} else {
			appService.createReleaseBranch(appId, grayBranch, release_branch, token);
			tips = grayBranch + "分支拉取成功";
		}
        return JsonResultUtil.buildSuccess(tips);
    }

	@OperateRecord(operateDiscribe="投产模块-更换投产窗口")
	@RequestValidate(NotEmptyFields = { Dict.TASK_ID ,Dict.RELEASE_NODE_NAME})
	@PostMapping(value = "/changeReleaseNode")
	public JsonResult changeReleaseNode(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String task_id = requestParam.get(Dict.TASK_ID);
		String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        // 查询其release_nodes窗口对象
        ReleaseNode releaseNode = releaseNodeService.queryDetail(release_node_name);
        if (CommonUtils.isNullOrEmpty(releaseNode)) {
            // 窗口对象若为空则报错
            logger.error("releasenode is empty!");
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[] { "投产窗口不存在" });
        }
        //投产窗口日期大于当前日期
        if(TimeUtils.compareDate(releaseNode.getRelease_date())) {
			throw new FdevException(ErrorConstants.RELEASE_NODE_EXPIRED, new String[] { release_node_name });
		}
		if(!roleService.isTaskSpdbManager(task_id)) {
			throw new FdevException(ErrorConstants.ROLE_ERROR,new String [] {"请联系任务的行内负责人"});
		}
		String applicationType = releaseNode.getType();
		String type = null;
		if ("4".equals(applicationType) || "5".equals(applicationType) || "6".equals(applicationType)) {
			type = applicationType;
		} else {
			type = "3".equals(releaseNode.getType()) ? releaseNode.getType() : "";
		}
		ReleaseTask releaseTask = releaseTaskService.findOneTask(task_id, type);
		if(CommonUtils.isNullOrEmpty(releaseTask)) {
			if ("4".equals(type)) {
				throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String [] {"组件窗口与其他窗口下的任务不能互相更换"});
			} else if ("5".equals(type)) {
				throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String [] {"骨架窗口与其他窗口下的任务不能互相更换"});
			} else if ("6".equals(type)) {
				throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String [] {"镜像窗口与其他窗口下的任务不能互相更换"});
			} else {
				throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String [] {"试运行窗口与其他窗口下的任务不能互相更换"});
			}
		}
		if(!releaseTask.getTask_status().equals(Constants.TASK_RLS_CONFIRMED)) {
			throw new FdevException(ErrorConstants.TASK_NOT_AUDIT);
		}
		if (!CommonUtils.isNullOrEmpty(releaseTask)
				&&release_node_name.equals(releaseTask.getRelease_node_name())) {
			throw new FdevException(ErrorConstants.CAN_NOT_CHANGE_NODE_SAME_AS_BEFORE);
		}
		// 查询该任务所在的应用晚于当前日期的所有该应用的投产点 若不存在或等于该投产点 则允许挂载
		Map<String, Object> taskInfo = taskService.queryTaskInfo(task_id);
		String taskType  = (String) taskInfo.get("applicationType");
		String appid = (String) taskInfo.get(Dict.PROJECT_ID);
		if ("componentWeb".equals(taskType) || "componentServer".equals(taskType)) {
			type = "4".equals(releaseNode.getType()) ? releaseNode.getType() : "";
			if (!"4".equals(releaseNode.getType())) {
				throw new FdevException(ErrorConstants.RELEASE_NODE_TYPE_UNNORMAL, new String[]{"该应用不能挂载在组件窗口"});
			}
		} else if ("archetypeWeb".equals(taskType) || "archetypeServer".equals(taskType)) {
			type = "5".equals(releaseNode.getType()) ? releaseNode.getType() : "";
			if (!"5".equals(releaseNode.getType())) {
				throw new FdevException(ErrorConstants.RELEASE_NODE_TYPE_UNNORMAL, new String[]{"该应用不能挂载在骨架窗口"});
			}
		} else if ("image".equals(taskType)) {
			type = "6".equals(releaseNode.getType()) ? releaseNode.getType() : "";
			if (!"6".equals(releaseNode.getType())) {
				throw new FdevException(ErrorConstants.RELEASE_NODE_TYPE_UNNORMAL, new String[]{"该应用不能挂载在镜像窗口"});
			}
		}else {
			// 判断应用与投产窗口类型是否匹配
			valReleaseNodeAppType(releaseNode, appid);
		}
        //通过应用id和投产窗口时间去投产应用中查询
        valReleaseNodeGroupId(releaseTask.getApplication_id(), release_node_name, task_id, releaseNode, taskType);
		releaseTaskService.changeReleaseNode(task_id, release_node_name, releaseTask);
		// 删除原投产需求
		releaseRqrmntService.deleteRqrmntTask(task_id, releaseTask.getRelease_node_name());
		// 添加新投产需求
		releaseRqrmntService.addOrEditRqrmntTaskAsync(task_id, release_node_name);

		releaseRqrmntInfoService.changeReleaseNode(releaseTask,releaseNode);

		return JsonResultUtil.buildSuccess(null);
	}

	@RequestValidate(NotEmptyFields = { Dict.OWNER_GROUPID })
	@PostMapping(value = "/queryTaskByInterval")
	public JsonResult queryTaskByInterval(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		Set<String> list = releaseTaskService.queryTaskByInterval(requestParam);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(Dict.COUNT, list.size());
		map.put(Dict.RELEASE_TASKS, list);
		return JsonResultUtil.buildSuccess(map);
	}

	 //  批量服务使用
	@RequestValidate(NotEmptyFields = { Dict.RELEASE_NODE_NAME })
	@RequestMapping(value = "/queryBatchTasks", method = RequestMethod.POST)
	public JsonResult queryBatchTasks(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		// 使用 release_node_name： 必传 和 application_id： 可不传 查询任务详情
		return JsonResultUtil.buildSuccess(releaseTaskService.queryBatchTasks(requestParam));
	}

	/**
	 * 批量模块使用，通过投产点名称和应用id进行查询
	 * @param param
	 * @return
	 */
	@RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME})
	@PostMapping("/queryTasksByExecutor")
	public JsonResult queryTasksByExecutor(@RequestBody @ApiParam Map<String, String> param) throws Exception{
		return JsonResultUtil.buildSuccess(releaseTaskService.queryTasksByExecutor(param));
	}

	/**
	 * 对传过来时间内的投产窗口以及投产窗口下的投产任务状态变更
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@RequestValidate(NotEmptyFields = {Dict.END_DATE})
    @PostMapping("/updateTaskArchivedBatch")
	public JsonResult updateTaskArchivedBatch(@RequestBody @ApiParam Map<String, String> param) throws Exception{
        releaseTaskService.updateTaskArchivedBatch(param);
		return JsonResultUtil.buildSuccess();
	}

    /**
     * 判断此投产窗口所属小组是否与该应用当天投产选择的投产窗口小组一致
     * @param appId 应用id
     * @param release_node_name 本次选择的投产窗口
     * @param releaseNode   本次投产窗口对象
     * @throws Exception
     */
	private void valReleaseNodeGroupId(String appId, String release_node_name, String task_id, ReleaseNode releaseNode, String taskType) throws Exception{
		//获取当天日期下投产窗口中同一个应用的任务
		String release_time = release_node_name.split("_")[0];
		List<ReleaseTask> releaseTasks = releaseTaskService.queryReleaseTaskByAppIdNoTaskId(appId, release_time, task_id);
		if(!CommonUtils.isNullOrEmpty(releaseTasks)){
			ReleaseTask releaseTask = releaseTasks.get(0);
			String old_release_node_date = releaseTask.getRelease_node_name();
			String old_release_date = old_release_node_date.split("_")[0];
			//3.通过投产窗口名称去查询此窗口所属小组
			ReleaseNode oldReleaseNode = releaseNodeService.queryDetail(old_release_node_date);
			//两投产窗口属于同一组下
			if(!releaseNode.getOwner_groupId().equals(oldReleaseNode.getOwner_groupId())){
				String owner_group_name = userService.queryGroupNameById(oldReleaseNode.getOwner_groupId());
				Map<String, Object> map = new HashMap<String, Object>();
				if ("componentWeb".equals(taskType)) {//前端组件
					map = componenService.queryMpassComponentDetail(appId);
					throw new FdevException(ErrorConstants.RELEASE_NODE_NOT_EQUALS, new String[]{(String) map.get(Dict.NAME_CN), old_release_date, owner_group_name});
				} else if ("componentServer".equals(taskType)) {//后端组件
					map = componenService.queryComponenbyid(appId);
					throw new FdevException(ErrorConstants.RELEASE_NODE_NOT_EQUALS, new String[]{(String) map.get(Dict.NAME_CN), old_release_date, owner_group_name});
				} else if ("archetypeWeb".equals(taskType)) {//前端骨架
					map = componenService.queryMpassArchetypeDetail(appId);
					throw new FdevException(ErrorConstants.RELEASE_NODE_NOT_EQUALS, new String[]{(String) map.get(Dict.NAME_CN), old_release_date, owner_group_name});
				} else if ("archetypeServer".equals(taskType)) {//后端骨架
					map = componenService.queryArchetypeDetail(appId);
					throw new FdevException(ErrorConstants.RELEASE_NODE_NOT_EQUALS, new String[]{(String) map.get(Dict.NAME_CN), old_release_date, owner_group_name});
				} else if ("image".equals(taskType)) {//镜像
					map = componenService.queryBaseImageDetail(appId);
					throw new FdevException(ErrorConstants.RELEASE_NODE_NOT_EQUALS, new String[]{(String) map.get(Dict.NAME_CN), old_release_date, owner_group_name});
				} else {
					//通过appId查询应用中文名
					map = appService.queryAPPbyid(appId);
					throw new FdevException(ErrorConstants.RELEASE_NODE_NOT_EQUALS, new String[]{(String) map.get(Dict.NAME_ZH), old_release_date, owner_group_name});
				}
			}
		}
    }

	@RequestValidate(NotEmptyFields = {Dict.TASK_ID, Dict.RELEASE_NODE_NAME})
	@PostMapping(value = "/queryByTaskIdNode")
	public JsonResult queryByTaskIdNode(@RequestBody @ApiParam Map<String, String> requestParam) {
		String task_id = requestParam.get(Dict.TASK_ID);
		String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
		// 通过应用id返回该投产任务的投产窗口信息、所在投产应用信息及任务信息
		ReleaseTask releaseTask = releaseTaskService.queryByTaskIdNode(task_id, release_node_name);
		boolean flag = false;
		if(!CommonUtils.isNullOrEmpty(releaseTask)
				&& (Constants.TASK_RLS_CONFIRMED.equals(releaseTask.getTask_status())
				|| Constants.TASK_RLS_PRODUCTION.equals(releaseTask.getTask_status()))) {
			// 投产任务不为空,并且投产任务状态为已确认投产或已投产
			flag = true;
		}
		return JsonResultUtil.buildSuccess(flag);
	}

	@RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME})
	@PostMapping(value = "/queryProWantTasks")
	public JsonResult queryProWantTasks(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
		String releaseNodeName = requestParam.get(Dict.RELEASE_NODE_NAME);
		String type = requestParam.get(Dict.TYPE);
		String releaseDateStr=releaseNodeName.split("_")[0];
		String releaseDate=releaseDateStr.substring(0,4)+"/"+releaseDateStr.substring(4,6)+"/"+releaseDateStr.substring(6,8);
		//获取所属小组id
        ReleaseNode releaseNode = releaseNodeService.queryDetail(releaseNodeName);
        if(CommonUtils.isNullOrEmpty(releaseNode)){
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[]{"投产窗口不存在"});
        }
        String ownerGroupId = releaseNode.getOwner_groupId();
        //调用任务模块接口
		List<Map<String, Object>> taskList = releaseTaskService.proWantWindow(releaseDate, ownerGroupId);
		List<Map<String,Object>> result=new ArrayList<>();
		for (Map<String, Object> task : taskList) {
			String taskId = (String) task.get(Dict.ID);
			String applicationType = (String) task.get("applicationType");
			//查询未挂载的任务
			ReleaseTask releaseTask = new ReleaseTask();
			if ("4".equals(type)) {
				releaseTask = releaseTaskService.findOneTask(taskId,type);
				if(CommonUtils.isNullOrEmpty(releaseTask)){
					if ("componentWeb".equals(applicationType)) {
						result.add(buildMap(task, taskId));
					} else if ("componentServer".equals(applicationType)) {
						result.add(buildMap(task, taskId));
					}
				} else {
					switch (releaseTask.getTask_status()){
                    case Constants.OPERATION_TYPR_BEGIN:
                        break;
                    case Constants.OPERATION_TYPR_ACCESS:
                        break;
                    case Constants.TASK_RLS_PRODUCTION:
                        break;
                    case Constants.OPERATION_TYPR_REFUSE:
                        result.add(buildMap(task, taskId));
                        break;
                    default:
                        break;
					}
				}
			} else if ("5".equals(type)) {
				releaseTask = releaseTaskService.findOneTask(taskId,type);
				if(CommonUtils.isNullOrEmpty(releaseTask)){
					if ("archetypeWeb".equals(applicationType)) {
						result.add(buildMap(task, taskId));
					} else if ("archetypeServer".equals(applicationType)) {
						result.add(buildMap(task, taskId));
					}
				} else {
					switch (releaseTask.getTask_status()){
                    case Constants.OPERATION_TYPR_BEGIN:
                        break;
                    case Constants.OPERATION_TYPR_ACCESS:
                        break;
                    case Constants.TASK_RLS_PRODUCTION:
                        break;
                    case Constants.OPERATION_TYPR_REFUSE:
                        result.add(buildMap(task, taskId));
                        break;
                    default:
                        break;
					}
				}
			} else if ("6".equals(type)) {
				releaseTask = releaseTaskService.findOneTask(taskId,type);
				if(CommonUtils.isNullOrEmpty(releaseTask)){
					if ("image".equals(applicationType)) {
						result.add(buildMap(task, taskId));
					}
				} else {
					switch (releaseTask.getTask_status()){
                    case Constants.OPERATION_TYPR_BEGIN:
                        break;
                    case Constants.OPERATION_TYPR_ACCESS:
                        break;
                    case Constants.TASK_RLS_PRODUCTION:
                        break;
                    case Constants.OPERATION_TYPR_REFUSE:
                        result.add(buildMap(task, taskId));
                        break;
                    default:
                        break;
					}
				}
			} else {
				releaseTask = releaseTaskService.findOneTask(taskId);
				if(CommonUtils.isNullOrEmpty(releaseTask) && !CommonUtils.isNullOrEmpty(applicationType)){
					if (applicationType.contains("app")) {
						result.add(buildMap(task, taskId));
					}
				} else if(!CommonUtils.isNullOrEmpty(releaseTask)){
					switch (releaseTask.getTask_status()){
                    case Constants.OPERATION_TYPR_BEGIN:
                        break;
                    case Constants.OPERATION_TYPR_ACCESS:
                        break;
                    case Constants.TASK_RLS_PRODUCTION:
                        break;
                    case Constants.OPERATION_TYPR_REFUSE:
                        result.add(buildMap(task, taskId));
                        break;
                    default:
                        break;
					}
				}
			}
		}
		return JsonResultUtil.buildSuccess(result);
	}

    private Map<String, Object> buildMap(Map<String, Object> task, String taskId) {
        Map<String,Object> map=new HashMap<>();
        Map<String, String> group = (Map<String, String>) task.get(Dict.GROUP);
        map.put(Dict.TASK_GROUP,group.get(Dict.NAME));
        map.put(Dict.TASK_GROUP_ID,group.get(Dict.ID));
        map.put(Dict.TASK_ID,taskId);
        map.put(Dict.TASK_NAME, task.get(Dict.NAME));
        Map<String,String> demand = (Map<String, String>) task.get(Dict.DEMAND);
        map.put(Dict.RQRMNT_NO, demand.get(Dict.OA_CONTACT_NO));
        map.put(Dict.RQRMNT_NAME_L, demand.get(Dict.OA_CONTACT_NAME));
        map.put(Dict.DEV_MANAGERS,task.get(Dict.MASTER));
        map.put(Dict.BANK_MASTER,task.get(Dict.SPDB_MASTER));
        map.put(Dict.APPLICATION_ID,task.get(Dict.PROJECT_ID));
        map.put(Dict.TASK_PROJECT,task.get(Dict.PROJECT_NAME));
        map.put(Dict.TASK_STAGE,task.get(Dict.STAGE));
        map.put("applicationType",task.get("applicationType"));
        return map;
    }

}


