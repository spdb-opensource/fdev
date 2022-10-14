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
import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Api(tags = "投产管理接口")
@RestController
@RefreshScope
public class DevopsController {

    @Value("${link.port}")
    private String port;

    @Value("${fdev.caas.registry.pro}")
    private String ips;

    @Autowired
    IAppService appService;
    @Autowired
    IReleaseTaskService releaseTask;
    @Autowired
    IReleaseApplicationService releaseApplicationService;
    @Autowired
    IRoleService roleService;
    @Autowired
    ISendEmailService sendEmailService;
    @Autowired
    IReleaseTaskService releaseTaskService;
    @Autowired
    ITaskService taskService;
    @Autowired
    IRelDevopsRecordService relDevopsRecordService;
    @Autowired
    IAsyncAutoReleaseService asyncAutoReleaseService;
    @Autowired
    ICommissionEventService commissionEventService;
    @Autowired
    IProdApplicationService prodApplicationService;
    @Autowired
    IProdRecordService prodRecordService;
    @Autowired
    IGitlabService gitlabService;
    @Autowired
    IUserService userService;
    @Autowired
    IAsyncService asyncService;

    private Logger logger = LoggerFactory.getLogger(DevopsController.class);
    //生产发布的接口
    @OperateRecord(operateDiscribe="投产模块-提交发布")
    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/api/devops/relDevops")
    public JsonResult relDevops(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        String is_reinforce = requestParam.get(Dict.IS_REINFORCE);
        if (!roleService.isApplicationManager(application_id)
                && !roleService.isAppSpdbManager(application_id)) {//判断当前用户为该任务的应用负责人
            logger.error("the user must be this app manager!");
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"操作用户必须为该应用的负责人或行内负责人"});
        }
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        //判断该应用下所有任务 的 任务关联项审核是否通过 //且过滤已拒绝的任务
        List<ReleaseTask> releaseTasks = releaseTask.queryReleaseTaskByAppid(application_id, release_node_name);
        List<String> ids = new ArrayList<>();
        for (ReleaseTask releaseTask : releaseTasks) {
            ids.add(releaseTask.getTask_id());
        }
        
        Map<String, Object> allTaskMap = taskService.queryTasksByIds(new HashSet<>(ids));
		for(String taskId : ids) {
			Map<String, Object> taskmap = (Map<String, Object>) allTaskMap.get(taskId);
            String groupId = String.valueOf(((Map)taskmap.get(Dict.GROUP)).get(Dict.ID));
            Map  groupInfo = userService.queryGroupDetail(groupId);
            boolean belongToInternet = String.valueOf(groupInfo.get(Dict.FULLNAME)).contains(Constants.INTERNET_GROUP);
            //如果任务属于互联网应用且提交发布卡点状态为false
            if(belongToInternet&&!(Boolean) taskmap.get(Dict.PRODUCTIONSTATUS)){
                throw new FdevException(ErrorConstants.REALEASE_NOT_IN_UAT_REL, new String[]{(String)taskmap.get(Dict.NAME)});
            }
            if(!(Dict.UAT_LOWER.equals(taskmap.get(Dict.STAGE)) || Dict.REL_LOWERCASE.equals(taskmap.get(Dict.STAGE))
                    || Dict.TASK_STAGE_PRODUCTION.equals(taskmap.get(Dict.STAGE))
                    || Dict.FILE.equals(taskmap.get(Dict.STAGE)) || Dict.ABORT.equals(taskmap.get(Dict.STAGE)))) {
				throw new FdevException(ErrorConstants.REALEASE_NOT_IN_UAT_REL, new String[]{(String)taskmap.get(Dict.NAME)});
			}
		}
        
        List tasksReviews = releaseApplicationService.queryTasksReviews(ids);
        if (!CommonUtils.isNullOrEmpty(tasksReviews)) {
            throw new FdevException(ErrorConstants.EXIST_TASK_REVIEWS_NOT_AUDIT);
        }
        String desc = requestParam.get(Dict.DESCRIPTION);
        User user = CommonUtils.getSessionUser();
        ReleaseApplication releaseApplication = releaseApplicationService.findOneReleaseApplication(application_id,
                release_node_name);
        //查询该应用下是否还有未审核的任务，有则不允许合并
        List<ReleaseTask> tasklist = releaseTaskService.queryTaskStatusOfApp(release_node_name, application_id);
        if (!CommonUtils.isNullOrEmpty(tasklist)) {
            throw new FdevException(ErrorConstants.EXIST_TASK_NOT_AUDITED);
        }

        if (CommonUtils.isNullOrEmpty(releaseApplication)) {
            logger.error("can't find this app info! @@@@@appid={}", application_id);
            throw new FdevException(ErrorConstants.APP_NOT_EXIST);
        }
        // 提交release合并master请求，成功则devops状态更新为1，失败更新为2
        RelDevopsRecord relDevopsRecord = new RelDevopsRecord();
        Map<String, Object> application = appService.queryAPPbyid(application_id);
        RelDevopsRecord save;
        try {
            if(!CommonUtils.isNullOrEmpty(application.get(Dict.NEW_ADD_SIGN))
                    && "0".equals(application.get(Dict.NEW_ADD_SIGN))) {
                Map<String, Object> app = new HashMap<>();
                app.put(Dict.ID, application.get(Dict.ID));
                app.put(Dict.NEW_ADD_SIGN, "1");
                appService.updateApplication(app);
            }
            String mergeRequest_id = appService.commitMergeRequest(releaseApplication.getApplication_id(),
                    user.getGit_token(), releaseApplication.getRelease_branch(), desc);
            relDevopsRecord.setMerge_request_id(mergeRequest_id);
            relDevopsRecord.setDevops_status(Constants.DEVOPS_MERGEREQ_CREATED);
            relDevopsRecord.setApplication_id(application_id);
            relDevopsRecord.setRelease_node_name(release_node_name);
            relDevopsRecord.setDevops_type("1");
            if(!CommonUtils.isNullOrEmpty(is_reinforce)) {
                relDevopsRecord.setIs_reinforce(is_reinforce);
            }
            String tag_version = requestParam.get(Dict.TAG_VERSION);
            if(!CommonUtils.isNullOrEmpty(tag_version)) {
                relDevopsRecord.setTag_version(tag_version);
            }
            save = relDevopsRecordService.save(relDevopsRecord);

        } catch (Exception ex) {
            logger.error("create merge request failed with error! devops_status will be set to '2' ");
            throw ex;
        }
        //添加代办事项
        commissionEventService.addRelDevopsCommissionEvent(application, save.getId(), save.getMerge_request_id());
        return JsonResultUtil.buildSuccess(null);
    }

    @RequestValidate(NotEmptyFields = {Dict.PRODUCT_TAG})
    @PostMapping(value = "/api/releasenode/application/saveProductImage")
    public JsonResult saveProductImage(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
        Integer gitlab_project_id = (Integer) (requestParam.get(Dict.GITLAB_PROJECT_ID));
        String pro_image_uri = (String) requestParam.get(Dict.PRO_IMAGE_URI);
        String pro_scc_image_uri = (String) requestParam.get(Dict.PRO_SCC_IMAGE_URI);
        String product_tag = (String) requestParam.get(Dict.PRODUCT_TAG);
        String env = (String) requestParam.get(Dict.ENV);
        boolean caasFlag = true;
        boolean sccFlag = true;
        if(!CommonUtils.isNullOrEmpty(pro_image_uri)){
            caasFlag = verifyImageIp(pro_image_uri.split("/")[0]);
        }else if (!CommonUtils.isNullOrEmpty(pro_scc_image_uri)){
            sccFlag = verifyImageIp(pro_scc_image_uri.split("/")[0]);
        }
        RelDevopsRecord relDevopsRecord = new RelDevopsRecord();
        if(caasFlag && sccFlag) {
            logger.info("镜像标签{}所属ip不符合投产要求，不做处理", pro_image_uri);
            return JsonResultUtil.buildSuccess(relDevopsRecord);
        }
        application_id = releaseApplicationService.queryApplicationId(application_id, gitlab_project_id);
        relDevopsRecord.setApplication_id(application_id);
        relDevopsRecord.setProduct_tag(product_tag);
        relDevopsRecord.setDevops_status(Constants.DEVOPS_PRODTAG_PACKAGED);
        if(!CommonUtils.isNullOrEmpty(pro_image_uri)){
            relDevopsRecord.setCaas_env(env);
            relDevopsRecord.setPro_image_uri(pro_image_uri);
        }
        if(!CommonUtils.isNullOrEmpty(pro_scc_image_uri)){
            relDevopsRecord.setScc_env(env);
            relDevopsRecord.setPro_scc_image_uri(pro_scc_image_uri);
        }
        relDevopsRecord = relDevopsRecordService.setUri(relDevopsRecord);
        return JsonResultUtil.buildSuccess(relDevopsRecord);
    }

    /**
     * 校验IP是否在配置列表中
     * @param imageIp 镜像uriIP
     * @return 在返回false，不在返回true
     */
    private boolean verifyImageIp(String imageIp){
        String[] pro_images = this.ips.split(",");
        for(String pro_image : pro_images) {
            if(pro_image.equals(imageIp)) {
                return false;
            }
        }
        return true;
    }

    @RequestValidate(NotEmptyFields = {Dict.PATH, Dict.PRODUCT_TAG,Dict.GITLAB_PROJECT_ID})
    @PostMapping(value = "/api/releasenode/application/saveApplicationPath")
    public JsonResult saveApplicationPath(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String product_tag = (String) requestParam.get(Dict.PRODUCT_TAG);
        Integer gitlab_project_id = (Integer) (requestParam.get(Dict.GITLAB_PROJECT_ID));
        List<String> path = (List<String>) requestParam.get(Dict.PATH);
        //通过gitlab_project_id查询应用id
        String application_id = releaseApplicationService.queryApplicationId("", gitlab_project_id);
        //根据应用id和tag名称查询投产窗口
        RelDevopsRecord relDevopsRecord = relDevopsRecordService.findAppByTagAndAppid(application_id, product_tag);
        if(CommonUtils.isNullOrEmpty(relDevopsRecord)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{product_tag + "不是fdev拉取，不做处理"});
        }
        ReleaseApplication application = releaseApplicationService.findOneReleaseApplication(application_id, relDevopsRecord.getRelease_node_name());
        String minioPath = releaseApplicationService.sourceMapTar(application_id, product_tag, relDevopsRecord.getRelease_node_name(), path, application.getRel_env_name());
        if(!CommonUtils.isNullOrEmpty(application) && !CommonUtils.isNullOrEmpty(minioPath)) {
            if(!CommonUtils.isNullOrEmpty(application.getPath())) {
                application.getPath().put(product_tag, minioPath);
            } else {
                application.setPath(new HashMap<String, String>(){{put(product_tag, minioPath);}});
            }
            // 保存生成的tar包路径到投产应用表中
            releaseApplicationService.updateApplicationPath(application);
        }
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.PRODUCT_TAG})
    @PostMapping(value = "/api/releasenode/application/saveFdevConfigChanged")
    public JsonResult saveFdevConfigChanged(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
        Integer gitlab_project_id = (Integer) (requestParam.get(Dict.GITLAB_PROJECT_ID));
        String product_tag = (String) requestParam.get(Dict.PRODUCT_TAG);
        application_id = releaseApplicationService.queryApplicationId(application_id, gitlab_project_id);
        RelDevopsRecord relDevopsRecord = relDevopsRecordService.findAppByTagAndAppid(application_id, product_tag);
        if(CommonUtils.isNullOrEmpty(relDevopsRecord)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{product_tag + "不是fdev拉取，不做处理"});
        }
        if(Dict.FAKETIME.equals(product_tag.substring(product_tag.lastIndexOf("-") + 1))) {
            return JsonResultUtil.buildSuccess("faketime镜像tag不进行配置文件比对");
        }
        Map<String, Object> appDetail = appService.queryAPPbyid(application_id);
        // 判断应用关联的gitlab的id是否为空
        if(!CommonUtils.isNullOrEmpty(appDetail) && !CommonUtils.isNullOrEmpty(appDetail.get(Dict.GITLAB_PROJECT_ID))) {
            // 根据应用gitlab的id查询应用绑定部署实体配置
            Map<String, Object> configGitlab = prodApplicationService.findConfigByGitlab(String.valueOf(appDetail.get(Dict.GITLAB_PROJECT_ID)));
            // 实体配置的gitlab的id
            String configGitlabId = prodApplicationService.checkEnvConfig(configGitlab, product_tag);
            // 部署实体配置是否为空
            if (!CommonUtils.isNullOrEmpty(configGitlabId)) {
                String last_tag = prodApplicationService.findLastTagByApplicationId(application_id);
                Map<String, Object> differs = new HashMap<>();
                //判断是否有上一次投产的uri
                if(!CommonUtils.isNullOrEmpty(last_tag)) {
                    // 比对两个分支差异
                    differs = gitlabService.compareBranches(configGitlabId, last_tag, product_tag);
                }
                ReleaseApplication releaseApplication = new ReleaseApplication(relDevopsRecord.getRelease_node_name(), application_id);
                String compare_url = "";
                boolean fdev_config_changed = false;
                String webUrl = "";
                if(CommonUtils.isNullOrEmpty(last_tag) || !CommonUtils.isNullOrEmpty(differs.get(Dict.DIFFS))) {
                    fdev_config_changed = true;
                    webUrl = gitlabService.findWebUrlByGitlabId(configGitlabId);

                    if(!CommonUtils.isNullOrEmpty(differs)) {
                        compare_url = webUrl + differs.get(Dict.COMPAREBRANCHES_URL);
                    } else {
                        compare_url = webUrl + "/tree/" + product_tag;
                    }
                }
                releaseApplication.setFdev_config_changed(fdev_config_changed);
                releaseApplication.setLast_release_tag(last_tag);
                releaseApplication.setDevops_tag(product_tag);
                releaseApplication.setCompare_url(compare_url);
                releaseApplication.setFdev_config_confirm(fdev_config_changed ? "0" : "1");
                releaseApplicationService.updateReleaseApplicationConfig(releaseApplication);
                // 若有差异则发邮件
                if(fdev_config_changed) {
                    List<Map<String, String>> appDevManager = userService.queryAppDevManager(application_id);
                    List<Map<String, String>> appSpdbManager = userService.queryAppSpdbManager(application_id);
                    Map<String, Object> map = new HashMap<>();
                    map.put(Dict.DEV_MANAGERS, appDevManager);
                    map.put(Dict.SPDB_MANAGERS, appSpdbManager);
                    map.put(Dict.NAME_EN, appDetail.get(Dict.NAME_EN));
                    map.put(Dict.DEVOPS_TAG, product_tag);
                    map.put(Dict.LAST_RELEASE_TAG, last_tag);
                    map.put(Dict.URL, webUrl);
                    map.put(Dict.RELEASE_NODE_NAME, relDevopsRecord.getRelease_node_name());
                    String hyperlink ="xxx"+port+"/fdev/#/release/list/"+relDevopsRecord.getRelease_node_name()+"/applist";
                    map.put(Dict.HYPERLINK, hyperlink);
                    logger.info("配置文件比对项目地址:{},当前分支:{},上次投产分支{}", webUrl + "/tree/master",
                            product_tag, last_tag);

                    // 给应用负责人发送通知提醒审核
                    sendEmailService.sendConfigCheckNotify(new HashMap<>(map));
                    List<Map<String, String>> managerList = new ArrayList<>();
                    managerList.addAll(appDevManager);
                    managerList.addAll(appSpdbManager);
                    // 给负责人添加待办事项
                    commissionEventService.addAuditConfigChangedCommission(managerList,
                            relDevopsRecord.getId(), relDevopsRecord.getRelease_node_name());
                }
            }
        }
        return JsonResultUtil.buildSuccess();
    }

    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID})
    @PostMapping(value = "/api/releasenode/application/findUriByApplicationId")
    public JsonResult findUriByApplicationId(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
        List<ProdApplication> list = prodApplicationService.findUriByApplicationId(application_id);
        List<String> s = new ArrayList<>();
        for (ProdApplication prodApplication : list) {
            ProdRecord prodRecord = prodRecordService.queryDetail(prodApplication.getProd_id());
            StringBuilder sb = new StringBuilder();
            sb.append("镜像：").append(prodApplication.getPro_image_uri())
                    .append("，date：").append(prodRecord.getDate())
                    .append("，plan_time：").append(prodRecord.getPlan_time());
            s.add(sb.toString());
        }
        return JsonResultUtil.buildSuccess(s);
    }

    @OperateRecord(operateDiscribe="投产模块-重新打包")
    @RequestValidate(NotEmptyFields = {Dict.APPLICATION_ID, Dict.PRODUCT_TAG, Dict.RELEASE_NODE_NAME})
    @PostMapping(value = "/api/devops/packageFromTag")
    public JsonResult packageFromTag(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
        String product_tag = (String) requestParam.get(Dict.PRODUCT_TAG);
        String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
        RelDevopsRecord relDevopsRecord = relDevopsRecordService.findAppByTagAndAppid(application_id, product_tag);
        if (CommonUtils.isNullOrEmpty(relDevopsRecord)) {
            relDevopsRecord = new RelDevopsRecord();
            relDevopsRecord.setRelease_node_name(release_node_name);
            relDevopsRecord.setDevops_type("2");
            relDevopsRecord.setApplication_id(application_id);
            relDevopsRecord.setProduct_tag(product_tag);
            relDevopsRecordService.save(relDevopsRecord);
        }

        //判断该应用下所有任务 的 任务关联项审核是否通过
        List<ReleaseTask> releaseTasks = releaseTask.queryReleaseTaskByAppid(application_id, release_node_name);
        List<String> ids = new ArrayList<>();
        for (ReleaseTask releaseTask : releaseTasks) {
            ids.add(releaseTask.getTask_id());
        }
        List tasksReviews = releaseApplicationService.queryTasksReviews(ids);
        if (!CommonUtils.isNullOrEmpty(tasksReviews)) {
            throw new FdevException(ErrorConstants.EXIST_TASK_REVIEWS_NOT_AUDIT);
        }
        if (!roleService.isApplicationManager(application_id)
                && !roleService.isAppSpdbManager(application_id)) {//判断当前用户为该任务的应用负责人或行内负责人
            logger.error("the user must be this app manager!");
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"操作用户必须为该应用的负责人或行内负责人"});
        }
        appService.createProjectPipeline(application_id, product_tag);
        return JsonResultUtil.buildSuccess(null);
    }

    @RequestValidate(NotEmptyFields = {Dict.PRO_PACKAGE_URI, Dict.PRODUCT_TAG})
    @PostMapping(value = "/api/releasenode/application/saveProductWar")
    public JsonResult saveProductWar(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        RelDevopsRecord relDevopsRecord = relDevopsRecordService.saveProductWar(requestParam);
        return JsonResultUtil.buildSuccess(relDevopsRecord);
    }

    @RequestValidate(NotEmptyFields = {Dict.PRODUCT_TAG,Dict.GITLAB_PROJECT_ID})
    @PostMapping(value = "/api/releasenode/application/saveApplicationTag", consumes = {"multipart/form-data"})
    public JsonResult saveApplicationTag(@RequestParam(Dict.PRODUCT_TAG) String product_tag,
                                         @RequestParam("RevertSqlPro") String revertsqlpro,
                                         @RequestParam("RevertSqlGray") String revertsqlgray,
                                         @RequestParam(Dict.GITLAB_PROJECT_ID) Integer gitlab_project_id,
                                         @RequestParam(Dict.FILE) MultipartFile[] files) throws Exception {
        releaseApplicationService.saveApplicationTag(product_tag, revertsqlpro, revertsqlgray, gitlab_project_id, files);
        return JsonResultUtil.buildSuccess();
    }
    
    @PostMapping(value = "/api/releasenode/application/deploy")
    public JsonResult deploy(@RequestBody @ApiParam Map<String, Object> requestParam) {
    	try {
			asyncAutoReleaseService.deploy(requestParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return JsonResultUtil.buildSuccess();
    }
}