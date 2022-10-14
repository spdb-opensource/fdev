package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.dao.IReleaseTaskDao;
import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;

@Api(tags = "投产管理应用接口")
@RestController
public class ReleaseApplicationController {

    @Autowired
    IReleaseApplicationService releaseApplicationService;
    @Autowired
    ISendEmailService sendEmailService;
    @Autowired
    IReleaseTaskService releaseTaskService;
    @Autowired
    ITaskService taskService;
    @Autowired
    IAppService appService;
    @Autowired
    IGitlabService gitlabService;
    @Autowired
    IRelDevopsRecordService relDevopsRecordService;
    @Autowired
    IRoleService roleService;
    @Autowired
    ICommissionEventService commissionEventService;
    @Autowired
    IAuditRecordService auditRecordService;
    @Autowired
    IUserService userService;
    @Autowired
    IProdApplicationService prodApplicationService;
    @Autowired
    IReleaseNodeService releaseNodeService;
    @Autowired
	private IComponenService componenService;
    @Autowired
	private IReleaseTaskDao releaseTaskDao;

    private Logger logger = LoggerFactory.getLogger(ReleaseApplicationController.class);

    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID})
    @PostMapping(value = "/api/application/queryReleaseNodesByAppId")
    public JsonResult queryReleaseNodesByAppId(@RequestBody @ApiParam Map<String, String> requestParam)
            throws Exception {
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        //`通过应用id查询含有该应用的投产窗口
        List<ReleaseNode> list = releaseApplicationService
                .queryReleaseNodesByAppId(requestParam.get(Dict.APPLICATION_ID), requestParam.get(Dict.NODE_STATUS));
        return JsonResultUtil.buildSuccess(list);
    }

    @PostMapping(value = "/api/releasenode/deleteApplication")
    //TODO 逻辑不完善
    public JsonResult deleteApplication(
            @RequestBody @ApiParam(name = "参数", value = "示例{ \"userId\":\"用户id\",\"token\":\"验证码\" }") JSONObject requestParam)
            throws Exception {
        releaseApplicationService.deleteApplication(requestParam.getString(Dict.APPLICATION_ID),
                requestParam.getString(Dict.RELEASE_NODE_NAME));
        return JsonResultUtil.buildSuccess(null);
    }

    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/api/application/queryApplications")
    public JsonResult queryApplications(
            @RequestBody @ApiParam(name = "参数", value = "示例{ \"release_node_name\":\"用户id\"}") Map<String, String> requestParam)
            throws Exception {
        //参数为投产窗口名， 查询投产应用列表
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        List<Map<String, Object>> result = releaseApplicationService.queryApplications(release_node_name);
    	for (Map map : result) {
            Map appMap = appService.queryAPPbyid((String) map.get(Dict.APPLICATION_ID));
            map.put(Dict.GROUP, CommonUtils.isNullOrEmpty(appMap) ? "" : appMap.get(Dict.GROUP));
        }
        return JsonResultUtil.buildSuccess(result);
    }

    @OperateRecord(operateDiscribe = "投产模块-审核")
    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/api/application/confirmConfigChanges")
    public JsonResult confirmConfigChanges(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        if (!roleService.isAppSpdbManager(application_id) && !roleService.isApplicationManager(application_id)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"此操作为应用级别,没有操作权限"});
        }
        ReleaseApplication releaseApplication = new ReleaseApplication(release_node_name, application_id);
        releaseApplication.setFdev_config_confirm("1");
        releaseApplication = releaseApplicationService.updateConfigConfirm(releaseApplication);
        RelDevopsRecord relDevopsRecord = relDevopsRecordService.findAppByTagAndAppid(application_id,
                releaseApplication.getDevops_tag());
        User user = CommonUtils.getSessionUser();
        // 修改待办事项为DONE
        try {
            commissionEventService.updateCommissionEvent(relDevopsRecord.getId(),
                    user.getId(), Dict.RELEASE_APPLICATION_CONFIG_CHANGED);
        } catch (Exception e) {
            logger.error("修改配置文件比对待办事项异常", e);
        }
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setOperation_type("1");
        // 投产应用配置文件审核
        auditRecord.setOperation(Dict.RELEASE_APPLICATION_CONFIG_CHANGED);
        auditRecord.setRelease_node_name(release_node_name);
        auditRecord.setApplication_id(application_id);
        auditRecord.setOperation_time(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
        auditRecord.setOperator_name_en(user.getUser_name_en());
        auditRecord.setOperator_name_cn(user.getUser_name_cn());
        auditRecord.setOperator_email(user.getEmail());
        try {
            auditRecordService.save(auditRecord);
        } catch (Exception e) {
            logger.error("投产应用配置文件审核记录保存异常", e);
        }
        return JsonResultUtil.buildSuccess(releaseApplication);
    }

    @RequestValidate(NotEmptyFields = {Dict.RELEASE_BRANCH})
    @PostMapping(value = "/api/releasenode/queryUatEnv")
    public JsonResult queryUatEnv(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
        Integer gitlab_project_id = null;
        if (!CommonUtils.isNullOrEmpty(requestParam.get(Dict.GITLAB_PROJECT_ID)))
            gitlab_project_id = (Integer) requestParam.get(Dict.GITLAB_PROJECT_ID);
        String release_branch = (String) requestParam.get(Dict.RELEASE_BRANCH);
        //若无应用id，则用git项目id查询其应用id
        application_id = releaseApplicationService.queryApplicationId(application_id, gitlab_project_id);
        //通过应用id 和 rel分支名查询 投产应用的 UAT环境
        String uat_env_name;
        ReleaseApplication releaseApplication = releaseApplicationService.queryAppByIdAndBranch(application_id, release_branch);
        if (!CommonUtils.isNullOrEmpty(releaseApplication) && !CommonUtils.isNullOrEmpty(releaseApplication.getUat_env_name())) {
            uat_env_name = releaseApplication.getUat_env_name();
        } else {
            Map<String, Object> application = appService.queryAPPbyid(application_id);
            uat_env_name = appService.queryByLabelsFuzzy(Dict.UAT_LOWER,
                    (CommonUtils.isNullOrEmpty(application.get(Dict.NETWORK))
                            || ((String) application.get(Dict.NETWORK)).indexOf(",") != -1)
                            ? "dmz" : (String) application.get(Dict.NETWORK));
            if (!CommonUtils.isNullOrEmpty(releaseApplication)) {
                releaseApplication.setUat_env_name(uat_env_name);
                releaseApplicationService.updateReleaseApplication(releaseApplication);
            }
        }
        return JsonResultUtil.buildSuccess(uat_env_name);
    }

    @RequestValidate(NotEmptyFields = {Dict.RELEASE_BRANCH})
    @PostMapping(value = "/api/releasenode/queryRelEnv")
    public JsonResult queryRelEnv(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
        Integer gitlab_project_id = null;
        if (!CommonUtils.isNullOrEmpty(requestParam.get(Dict.GITLAB_PROJECT_ID)))
            gitlab_project_id = (Integer) requestParam.get(Dict.GITLAB_PROJECT_ID);
        String product_tag = (String) requestParam.get(Dict.RELEASE_BRANCH);
        //若无应用id，则用git项目id查询其应用id
        application_id = releaseApplicationService.queryApplicationId(application_id, gitlab_project_id);
        //新增判断逻辑,接口中分支名传进来的是master时,将查询当前日期之前的一次的tag作为分支名查询环境信息
        if(Dict.MASTER.equals(product_tag)){
            String tag = prodApplicationService.findLatestTag(application_id,Dict.CAAS);
            if(CommonUtils.isNullOrEmpty(tag)) {
                throw new FdevException(ErrorConstants.RELEASE_LACK_MIRROR_URI, new String[]{""});
            }
            product_tag = tag.split(":")[1];
        }
        //通过应用id 和 rel分支名查询 投产应用的 rel环境
        ReleaseApplication releaseApplication = releaseApplicationService.queryAppByIdAndTag(application_id, product_tag);
        String rel_env_name;
        if (!CommonUtils.isNullOrEmpty(releaseApplication) && !CommonUtils.isNullOrEmpty(releaseApplication.getRel_env_name())) {
            rel_env_name = releaseApplication.getRel_env_name();
        } else {
            Map<String, Object> application = appService.queryAPPbyid(application_id);
            rel_env_name = appService.queryByLabelsFuzzy(Dict.REL.toLowerCase(),
                    (CommonUtils.isNullOrEmpty(application.get(Dict.NETWORK))
                            || ((String) application.get(Dict.NETWORK)).indexOf(",") != -1)
                            ? "dmz" : (String) application.get(Dict.NETWORK));
            if (!CommonUtils.isNullOrEmpty(releaseApplication)) {
                releaseApplication.setRel_env_name(rel_env_name);
                releaseApplicationService.updateReleaseApplication(releaseApplication);
            }
        }
        return JsonResultUtil.buildSuccess(rel_env_name);
    }

    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME, Dict.APPLICATION_ID})
    @PostMapping(value = "/api/application/queryTasksReviews")
    public JsonResult queryTasksReviews(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
        String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
        List<ReleaseTask> releaseTasks = releaseTaskService.queryReleaseTaskByAppid(application_id, release_node_name);
        List<String> list = new ArrayList<String>();
        for (ReleaseTask releaseTask : releaseTasks) {
            list.add(releaseTask.getTask_id());
        }
        List taskReviews = releaseApplicationService.queryTasksReviews(list);
        return JsonResultUtil.buildSuccess(taskReviews);
    }

    @RequestValidate(NotEmptyFields = {Dict.RELEASE_TASKS})
    @PostMapping(value = "/api/application/sendEmailForTaskManagers")
    public JsonResult sendEmailForTaskManagers(@RequestBody @ApiParam Map requestParam) throws Exception {
        List<Map> tasks = (List<Map>) requestParam.get(Dict.RELEASE_TASKS);
        List<HashMap> list = new ArrayList<>();
        for (Map reqMap : tasks) {
            String taskId = (String) reqMap.get(Dict.TASK_ID);
            HashMap taskDetail = (HashMap) taskService.queryTaskInfo(taskId);
            String applicationType = (String) taskDetail.get("applicationType");
            Map relaseTask = new HashMap<String, Object>();
            if ("componentWeb".equals(applicationType) || "componentServer".equals(applicationType)) {
            	relaseTask = releaseTaskService.queryDetailByTaskId(taskId, "4");
			} else if ("archetypeWeb".equals(applicationType) || "archetypeServer".equals(applicationType)) {
				relaseTask = releaseTaskService.queryDetailByTaskId(taskId, "5");
			} else if ("image".equals(applicationType)) {
				relaseTask = releaseTaskService.queryDetailByTaskId(taskId, "6");
			} else {
				relaseTask = releaseTaskService.queryDetailByTaskId(taskId, null);
			}
            if (CommonUtils.isNullOrEmpty(relaseTask.get(Dict.DESC))) {
                relaseTask.put(Dict.DESC, "无");
            }
            taskDetail.putAll(relaseTask);
            list.add(taskDetail);
        }
        sendEmailService.sendEmailReviews(list);
        return JsonResultUtil.buildSuccess(null);
    }

    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/api/application/queryApplicationTags")
    public JsonResult queryApplicationTags(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        List tagList = releaseApplicationService.queryApplicationTags(release_node_name, application_id);
        return JsonResultUtil.buildSuccess(tagList);
    }

    @OperateRecord(operateDiscribe = "投产模块-测试环境")
    @RequestValidate(
            NotEmptyFields = {Dict.APPLICATION_ID, Dict.RELEASE_NODE_NAME}
    )
    @PostMapping(value = "/api/application/setTestEnvs")
    public JsonResult setTestEnvs(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        String uat_env_name = requestParam.get(Dict.UAT_ENV_NAME);
        String rel_env_name = requestParam.get(Dict.REL_ENV_NAME);
        if (CommonUtils.isNullOrEmpty(uat_env_name) && CommonUtils.isNullOrEmpty(rel_env_name)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_BOTH_EMPTY,
                    new String[]{Dict.UAT_ENV_NAME + "|" + Dict.REL_ENV_NAME});
        }
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        ReleaseApplication releaseApplication = releaseApplicationService.findOneReleaseApplication(application_id, release_node_name);
        if (CommonUtils.isNullOrEmpty(releaseApplication)) {
            throw new FdevException(ErrorConstants.APP_NOT_EXIST);
        }
        if (!CommonUtils.isNullOrEmpty(uat_env_name)) {
            releaseApplication.setUat_env_name(uat_env_name);
        }
        if (!CommonUtils.isNullOrEmpty(rel_env_name)) {
            releaseApplication.setRel_env_name(rel_env_name);
        }
        ReleaseApplication result = releaseApplicationService.updateReleaseApplication(releaseApplication);
        return JsonResultUtil.buildSuccess(result);
    }

    @OperateRecord(operateDiscribe = "投产模块-CI/CD")
    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/api/application/queryAppTagPiplines")
    public JsonResult queryAppTagPiplines(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        String pages = requestParam.get(Dict.PAGES);
        ReleaseNode releaseNode = releaseNodeService.queryDetail(release_node_name);
        String project_id = null;
        List<ReleaseTask> tasks = releaseTaskDao.querySameAppTask(application_id,release_node_name);
    	String taskId = tasks.get(0).getTask_id();
    	String applicationType = (String) taskService.queryTaskDetail(taskId).get("applicationType");
        if ("4".equals(releaseNode.getType())) {
        	Map<String, Object> component = new HashMap<String, Object>();
			//查询骨架
        	if ("componentWeb".equals(applicationType)) {
        		component = componenService.queryMpassComponentDetail(application_id);
			} else {
				component = componenService.queryComponenbyid(application_id);
			}
        	if (CommonUtils.isNullOrEmpty(component)) {
				throw new FdevException(ErrorConstants.GITLAB_PROJECT_NOT_EXIST);
			}
			project_id = String.valueOf(component.get("gitlab_id"));
		} else if ("5".equals(releaseNode.getType())) {
			Map<String, Object> archetype = new HashMap<String, Object>();
			//查询骨架
        	if ("archetypeWeb".equals(applicationType)) {
        		archetype = componenService.queryMpassArchetypeDetail(application_id);
			} else {
				archetype = componenService.queryArchetypeDetail(application_id);
			}
			if (CommonUtils.isNullOrEmpty(archetype)) {
				throw new FdevException(ErrorConstants.GITLAB_PROJECT_NOT_EXIST);
			}
			project_id = String.valueOf(archetype.get("gitlab_id"));
		} else if ("6".equals(releaseNode.getType())) {
			Map<String, Object> image = componenService.queryBaseImageDetail(application_id);
			if (CommonUtils.isNullOrEmpty(image)) {
				throw new FdevException(ErrorConstants.GITLAB_PROJECT_NOT_EXIST);
			}
			project_id = String.valueOf(image.get("gitlab_id"));
		} else {
			Map<String, Object> application = appService.queryAPPbyid(application_id);
			if (CommonUtils.isNullOrEmpty(application)) {
				throw new FdevException(ErrorConstants.GITLAB_PROJECT_NOT_EXIST);
			}
			project_id = String.valueOf(application.get(Dict.GITLAB_PROJECT_ID));
		}
        List<String> tagList = new ArrayList<String>();
        if ("4".equals(releaseNode.getType()) || "5".equals(releaseNode.getType()) || "6".equals(releaseNode.getType())) {
			tagList = componenService.queryTagList(release_node_name, application_id);
		} else {
			tagList = releaseApplicationService.queryApplicationTags(release_node_name, application_id);
		}
        List<Map> piplines = new ArrayList<>();
        for (String tag_name : tagList) {
            piplines.addAll(gitlabService.queryPiplineWithJobs(project_id, tag_name, pages));
        }
        piplines.sort((a, b) -> {
            if (Integer.parseInt(String.valueOf(a.get(Dict.ID)))
                    >= Integer.parseInt(String.valueOf(b.get(Dict.ID)))) {
                return -1;
            }
            return 1;
        });
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.PROJECT_ID, project_id);
        map.put(Dict.PIPELINES, piplines);
        return JsonResultUtil.buildSuccess(map);
    }

    //批量服务使用接口
    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME})
    @RequestMapping(value = "/api/application/queryBatchApplications", method = RequestMethod.POST)
    public JsonResult queryBatchApplications(
            @RequestBody @ApiParam(name = "参数", value = "示例{ \"release_node_name\":\"用户id\"}") Map<String, String> requestParam)
            throws Exception {
        //参数为投产窗口名， 查询投产应用列表
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        return JsonResultUtil.buildSuccess(releaseApplicationService.queryBatchApplications(release_node_name));
    }

    /**
     * 查询传入窗口下该应用是否所有任务均已进入rel阶段
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, Dict.RELEASE_NODE_NAME})
    @RequestMapping(value = "/api/application/tasksInSitStage", method = RequestMethod.POST)
    public JsonResult tasksInSitStage(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        //根据应用id与名称查询所有任务
        List<ReleaseTask> releaseTasks = releaseTaskService.queryReleaseTaskByAppid(application_id, release_node_name);
        Set<String> ids = new HashSet<>();
        for (ReleaseTask releaseTask : releaseTasks) {
            ids.add(releaseTask.getTask_id());
        }
        Map<String, Object> map = new HashMap<>();
        String status = Constants.RELEASE_CONFIRM_NORMAL;
        Map<String,String> stage_task_name = new HashMap<>();
        Map<String,String> confirm_task_name = new HashMap<>();
        Map<String,String> safety_task_name = new HashMap<>();
        Map<String,String> report_task_name = new HashMap<>();
        //根据任务id查询所有任务详情
        Map<String, Object> allTaskMap = taskService.queryTasksByIds(ids);
        // 任务不满足放入集合
        for (String taskId : ids) {
            Map<String, Object> taskmap = (Map<String, Object>) allTaskMap.get(taskId);
            //当前任务未在rel阶段且确认书未到达
            String groupId = String.valueOf(((Map)taskmap.get(Dict.GROUP)).get(Dict.ID));
            Map  groupInfo = userService.queryGroupDetail(groupId);
            boolean belongToInternet = String.valueOf(groupInfo.get(Dict.FULLNAME)).contains(Constants.INTERNET_GROUP);
            if (!(Dict.UAT_LOWER.equals(taskmap.get(Dict.STAGE)) || Dict.REL_LOWERCASE.equals(taskmap.get(Dict.STAGE))
                    || Dict.TASK_STAGE_PRODUCTION.equals(taskmap.get(Dict.STAGE))
                    || Dict.FILE.equals(taskmap.get(Dict.STAGE)) || Dict.ABORT.equals(taskmap.get(Dict.STAGE)))) {
                status = Constants.RELEASE_CONFIRM_ERROR;
                stage_task_name.put((String) taskmap.get(Dict.ID), (String) taskmap.get(Dict.NAME));
            }
            if(!(Boolean) taskmap.get(Dict.PRODUCTIONSTATUS) && belongToInternet){
                status = Constants.RELEASE_CONFIRM_ERROR;
                confirm_task_name.put((String) taskmap.get(Dict.ID), (String) taskmap.get(Dict.NAME));
            }
            if (!(Boolean)taskmap.get(Dict.BUSINESSTESTREPORTFLAG)) {
                status = Constants.RELEASE_CONFIRM_ERROR;
                report_task_name.put((String) taskmap.get(Dict.ID), (String) taskmap.get(Dict.NAME));
            }
        }
        map.put(Dict.STAGE_TASK_NAME, stage_task_name);
        map.put(Dict.CONFIRM_TASK_NAME,confirm_task_name);
        map.put(Dict.REPORT_TASK_NAME,report_task_name);
        ReleaseNode releaseNode = releaseNodeService.queryDetail(release_node_name);
        List<ReleaseTask> tasks = releaseTaskDao.querySameAppTask(application_id,release_node_name);
    	String taskId = tasks.get(0).getTask_id();
    	String applicationType = (String) taskService.queryTaskDetail(taskId).get("applicationType");
        if ("4".equals(releaseNode.getType())) {
        	Map<String, Object> component = new HashMap<String, Object>();
			//查询骨架
        	if ("componentWeb".equals(applicationType)) {
        		component = componenService.queryMpassComponentDetail(application_id);
			} else {
				component = componenService.queryComponenbyid(application_id);
			}
        	if (CommonUtils.isNullOrEmpty(component)) {
				throw new FdevException(ErrorConstants.GITLAB_PROJECT_NOT_EXIST);
			}
        	if(CommonUtils.isNullOrEmpty(component)) {
	            status = Constants.RELEASE_CONFIRM_ERROR;
	            map.put(Dict.APPLICATION_ID, application_id);
	            map.put(Dict.SYSTEM, "");
	        }
		} else if ("5".equals(releaseNode.getType())) {
			Map<String, Object> archetype = new HashMap<String, Object>();
			//查询骨架
        	if ("archetypeWeb".equals(applicationType)) {
        		archetype = componenService.queryMpassArchetypeDetail(application_id);
			} else {
				archetype = componenService.queryArchetypeDetail(application_id);
			}
			if(CommonUtils.isNullOrEmpty(archetype)) {
	            status = Constants.RELEASE_CONFIRM_ERROR;
	            map.put(Dict.APPLICATION_ID, application_id);
	            map.put(Dict.SYSTEM, "");
	        }
		} else if ("6".equals(releaseNode.getType())) {
			Map<String, Object> image = componenService.queryBaseImageDetail(application_id);
			if(CommonUtils.isNullOrEmpty(image)) {
	            status = Constants.RELEASE_CONFIRM_ERROR;
	            map.put(Dict.APPLICATION_ID, application_id);
	            map.put(Dict.SYSTEM, "");
	        }
		} else {
	        // 应用未绑定系统
			Map<String, Object> application = appService.queryAPPbyid(application_id);
	        if(CommonUtils.isNullOrEmpty(application) || CommonUtils.isNullOrEmpty(application.get(Dict.SYSTEM))) {
	            status = Constants.RELEASE_CONFIRM_ERROR;
	            map.put(Dict.APPLICATION_ID, application_id);
	            map.put(Dict.SYSTEM, taskService.querySystem());
	        }
		}
        Map<String, Object> data = taskService.querySecurityTestResult(ids);
        List<Map<String, Object>> allTaskList = (List<Map<String, Object>>) data.get("noPassTaskList");
        if (!CommonUtils.isNullOrEmpty(allTaskList)) {
        	for (Map<String, Object> allTask : allTaskList) {
        		status = Constants.RELEASE_CONFIRM_ERROR;
        		safety_task_name.put((String) allTask.get("taskId"), (String) allTask.get("taskName"));
			}
		}
        map.put("safety_task_name", safety_task_name);
        map.put(Dict.STATUS, status);
        return JsonResultUtil.buildSuccess(map);
    }

    /**
     * 修改fake相关信息
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @OperateRecord(operateDiscribe = "投产模块-更改faketime状态")
    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, Dict.RELEASE_NODE_NAME, Dict.FAKE_FLAG})
    @RequestMapping(value = "/api/application/editFakeInfo", method = RequestMethod.POST)
    public JsonResult editFakeInfo(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        if (!roleService.isApplicationManager(application_id)
                && !roleService.isAppSpdbManager(application_id)) {//判断当前用户为该任务的应用负责人
            logger.error("the user must be this app manager!");
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"操作用户必须为该应用的负责人或行内负责人"});
        }
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        String fake_flag = requestParam.get(Dict.FAKE_FLAG);
        String fake_image_name = requestParam.get(Dict.FAKE_IMAGE_NAME);
        String fake_image_version = requestParam.get(Dict.FAKE_IMAGE_VERSION);
        String tips = "设置成功";
        if (Constants.PULL_FAKE_TAG.equals(fake_flag)) {
            if (CommonUtils.isNullOrEmpty(fake_image_name) || CommonUtils.isNullOrEmpty(fake_image_version)) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY,
                        new String[]{Dict.FAKE_IMAGE_NAME + "或" + Dict.FAKE_IMAGE_VERSION});
            }
            List<String> tags = relDevopsRecordService.queryNormalTags(release_node_name, application_id);
            logger.info("查询已出tag数量：{}", tags.size());
            if (!CommonUtils.isNullOrEmpty(tags)) {
                String last_tag = tags.get(0);
                logger.info("选择已出tag名称：{}", last_tag);
                String fake_tag = last_tag + "-" + Dict.FAKETIME;
                RelDevopsRecord fake_record = relDevopsRecordService.findAppByTagAndAppid(application_id, fake_tag);
                logger.info("查询是否已出fake_tag：{}", !CommonUtils.isNullOrEmpty(fake_record));
                if (CommonUtils.isNullOrEmpty(fake_record)) {
                    User user = CommonUtils.getSessionUser();
                    this.pullFakeTag(last_tag, release_node_name, application_id, fake_image_name, fake_image_version,
                            user.getGit_token());
                    tips = "拉取" + fake_tag + "tag成功";
                }
            }
        } else {
            tips = "faketime关闭成功";
        }
        ReleaseApplication releaseApplication = new ReleaseApplication(release_node_name, application_id);
        releaseApplication.setFake_flag(fake_flag);
        releaseApplication.setFake_image_name(fake_image_name);
        releaseApplication.setFake_image_version(fake_image_version);
        releaseApplicationService.updateReleaseApplication(releaseApplication);
        return JsonResultUtil.buildSuccess(tips);
    }

    private void pullFakeTag(String pro_tag, String release_node_name, String application_id, String fake_image_name,
                             String fake_image_version, String token) throws Exception {
        String branch_name = pro_tag.replace("pro", Dict.FAKETIME);
        String fake_tag = pro_tag + "-" + Dict.FAKETIME;
        logger.info("{}投产窗口下已拉取tag{}faketime分支名称：{}", release_node_name, pro_tag, branch_name);
        Map<String, Object> application = appService.queryAPPbyid(application_id);
        // 从master创建分支
        gitlabService.createBranch((Integer) application.get(Dict.GITLAB_PROJECT_ID), branch_name, Dict.MASTER);
        logger.info("创建分支成功：{}", branch_name);
        InputStream is = null;
        try {
            // 获取要修改的文件内容
            String gitFileContent = appService.getGitFileContentById(String.valueOf(application.get(Dict.GITLAB_PROJECT_ID)),
                    URLEncoder.encode(Dict.GITLAB_CI + "/" + Dict.DOCKERFILE, "UTF-8"), branch_name);
            byte[] content = Base64.decodeBase64(gitFileContent);
            byte[] buff = new byte[1024];
            is = new ByteArrayInputStream(content);
            StringBuilder sb = new StringBuilder();
            int len;
            while (-1 != (len = is.read(buff))) {
                String file_content = new String(buff, 0, len);
                logger.info("读取文件内容：{}", file_content);
                // 若换行符为\r\n，则剔除\r
                if (file_content.contains("\r")) {
                    file_content.replace("\r", "");
                }
                String[] content_array = file_content.split("\n");
                int line = 1;
                for (String line_contant : content_array) {
                    if (line == 1) {
                        sb.append(line_contant, 0, line_contant.lastIndexOf("/") + 1).append(fake_image_name)
                                .append(":").append(fake_image_version);
                    } else {
                        sb.append("\n").append(line_contant);
                    }
                    line++;
                }
            }
            is.close();
            appService.updateGitFile(String.valueOf(application.get(Dict.GITLAB_PROJECT_ID)),
                    URLEncoder.encode(Dict.GITLAB_CI + "/" + Dict.DOCKERFILE, "UTF-8"), branch_name, sb.toString(),
                    "修改基础镜像名称与版本号", token);
            logger.info("修改文件并推送成功，文件内容：{}", sb.toString());
            // 以新拉取分支创建tag
            gitlabService.createTag(String.valueOf(application.get(Dict.GITLAB_PROJECT_ID)), fake_tag, branch_name, token);
            logger.info("拉取{}tag成功", fake_tag);
        } catch (Exception e) {
            if (is != null) {
                is.close();
            }
            // 出现异常，删除临时分支，抛出错误
            gitlabService.deleteBranch((Integer) application.get(Dict.GITLAB_PROJECT_ID), branch_name);
            logger.info("出现异常，删除分支成功，抛出异常，分支名称：{}", branch_name);
            throw e;
        }
        // 删除临时分支
        gitlabService.deleteBranch((Integer) application.get(Dict.GITLAB_PROJECT_ID), branch_name);
        logger.info("{}分支删除成功", branch_name);
        RelDevopsRecord fake_record = new RelDevopsRecord();
        fake_record.setApplication_id(application_id);
        fake_record.setRelease_node_name(release_node_name);
        fake_record.setProduct_tag(fake_tag);
        fake_record.setDevops_type("3");
        fake_record.setDevops_status(Constants.DEVOPS_PRODTAG_CREATED);
        relDevopsRecordService.save(fake_record);
    }

    @OperateRecord(operateDiscribe = "投产模块-试运行CI/CD")
    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/api/application/queryTestRunPiplines")
    public JsonResult queryTestRunPiplines(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        Map<String, Object> application = appService.queryAPPbyid(application_id);
        if (CommonUtils.isNullOrEmpty(application)) {
            throw new FdevException(ErrorConstants.GITLAB_PROJECT_NOT_EXIST);
        }
        String project_id = String.valueOf(application.get(Dict.GITLAB_PROJECT_ID));
        //
        ReleaseApplication releaseApplication = releaseApplicationService.findOneReleaseApplication(application_id, release_node_name);
        List<Map> piplines = new ArrayList<>();

        piplines.addAll(gitlabService.queryPiplineWithJobs(project_id, releaseApplication.getRelease_branch(), "1"));

        piplines.sort((a, b) -> {
            if (Integer.parseInt(String.valueOf(a.get(Dict.ID)))
                    >= Integer.parseInt(String.valueOf(b.get(Dict.ID)))) {
                return -1;
            }
            return 1;
        });
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.PROJECT_ID, project_id);
        map.put(Dict.PIPELINES, piplines);
        return JsonResultUtil.buildSuccess(map);
    }
    
    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/api/application/queryComponent")
    public JsonResult queryComponent(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        //参数为投产窗口名和组件类型， 查询投产组件列表
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        String type = requestParam.get(Dict.TYPE);
        List<Map<String, Object>> result = releaseApplicationService.queryComponent(release_node_name,type);
        return JsonResultUtil.buildSuccess(result);
    }
    
    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/api/application/queryTargetVersion")
    public JsonResult queryTargetVersion(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        List<String> result = releaseApplicationService.queryTargetVersion(requestParam);
        return JsonResultUtil.buildSuccess(result);
    }

}
