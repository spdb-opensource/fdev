package com.spdb.fdev.fdevtask.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdevtask.base.dict.Constants;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.unit.AsyncTaskUnit;
import com.spdb.fdev.fdevtask.base.utils.*;
import com.spdb.fdev.fdevtask.spdb.dao.IReleaseApproveDao;
import com.spdb.fdev.fdevtask.spdb.entity.*;
import com.spdb.fdev.fdevtask.spdb.service.*;
import com.spdb.fdev.transport.RestTransport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Api(tags = "任务接口")
@RequestMapping("/api/task")
@RestController
@RefreshScope
public class FdevTaskController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志

    @Autowired
    private IFdevTaskService fdevTaskService;
    @Autowired
    private IAppApi iAppApi;
    @Autowired
    private MailUtil mailUtil;
    @Autowired
    private IUserApi iUserApi;
    @Autowired
    private ITestApi iTestApi;
    @Autowired
    private INotifyApi iNotifyApi;
    @Autowired
    private IReleaseTaskApi ReleaseTaskApi;
    @Autowired
    private ExcelUtils excelUtils;
    @Autowired
    private IEnvConfigApi iEnvConfigApi;
    @Autowired
    private ISetProtectedBranches iSetProtectedBranches;
    @Autowired
    private OperateRecordService operateRecordService;
    @Autowired
    private MateDataService mateDataService;
    @Autowired
    private DemandService demandService;
    @Autowired
    private GitApiService gitApiService;
    @Autowired
    private ServiceApi serviceApi;
    @Autowired
    private IComponentService componentService;
    @Autowired
    private IReleaseApproveDao releaseApproveDao;
    @Autowired
    private UserUtils userUtils;
    @Autowired
    private UserVerifyUtil userVerifyUtil;
    @Value("${git.home}")
    private String gitHome;
    @Value("${isSendMail}")
    private boolean isSendMail;
    @Value("${git.token}")
    private String gitToken;
    @Value("${queryPostponeTask.history:false}")
    private boolean history;
    @Value("${ui.review.link}")
    private String baseLink;
    @Value("${ui.send.review.email}")
    private boolean uiSendReviewEmail;
    @Value("${updateEmailSwitch}")
    private boolean updateEmailSwitch;
    @Value("${add-order-topic}")
    private String addOrderTopic;
    @Value("${delete-order-topic}")
    private String deleteOrderTopic;
    @Value("${release.merge.team}")
    private List<String> releaseMergeTeam;//须走release合并审核流程的三级组

    @Autowired
    private TodoListApi todoListApi;

    @Autowired
    private AsyncService asyncService;

    @Autowired
    private ReviewRecordService reviewRecordService;
    @Resource
    private AsyncTaskUnit asyncTaskUnit;

    @Autowired
    private IFdevTaskService iFdevTaskService;

    @Autowired
    private TaskDiffModifyRecordService modifyRecordService;

    @Autowired
    private GroupTreeUtil groupTreeUtil;

    @Autowired
    private FdevTaskUtils fdevTaskUtils;

    @Autowired
    private JiraIssuesService jiraIssuesService;

    @Autowired
    private RestTransport restTransport;
    //投产点任务审核、任务变更，审核项审核、任务更新

    @ApiOperation(value = "查询设计还原审核是否完成")
    @RequestMapping(value = "/getReviewStatus", method = RequestMethod.POST)
    public JsonResult getReviewStatus(@RequestBody @ApiParam(name = "参数", value = " 例如：{\r\n"
            + " \"gitlabProjectId\":\"3452\",\r\n"
            + " \"featureBranch\":\"feature-test20200526\" \r\n}") Map request) throws Exception {
        String projectId = (String) request.get("gitlabProjectId");
        if (CommonUtils.isNullOrEmpty(projectId)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"gitlab项目id不能为空！"});
        }
        String featureBranch = (String) request.get("featureBranch");
        if (CommonUtils.isNullOrEmpty(featureBranch)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"feature分支名不能为空！"});
        }
        String result = Dict.IRRELEVANT;
        Map appInfo = iAppApi.queryAppByGitlabIds(projectId);
        if (!CommonUtils.isNullOrEmpty(appInfo)) {
            String id = (String) appInfo.get(Dict.ID);
            List<FdevTask> list = fdevTaskService.query(new FdevTask.FdevTaskBuilder().project_id(id).feature_branch(featureBranch).init());
            if (!CommonUtils.isNullOrEmpty(list)) {
                for (FdevTask s : list) {
                    if (!CommonUtils.isNullOrEmpty(s.getReview_status())) {
                        //需求UI开关
                        Boolean ui_verify = false;
                        /*需求信息*/
                        Map map = demandService.queryByFdevNo(s.getRqrmnt_no(), s.getFdev_implement_unit_no());
                        if (map != null) {
                            Map implement_unit_info = (Map) map.get(Dict.IMPLEMENT_UNIT_INFO);
                            if (implement_unit_info != null)
                                ui_verify = Optional.ofNullable((Boolean) map.get("ui_verify")).orElse(false);
                        }
                        String reviewStatus = (String) s.getReview_status();
                        if (!Dict.IRRELEVANT.equals(reviewStatus) && !Dict.FINISHED.equals(reviewStatus) && ui_verify) {
                            result = reviewStatus;
                        }
                    }
                }
            }
        }
        return JsonResultUtil.buildSuccess(result);
    }


    @OperateRecord(operateDiscribe = "任务模块-更新设计还原审核状态")
    @ApiOperation(value = "更新设计还原审核状态")
    @RequestMapping(value = "/updateState", method = RequestMethod.POST)
    public JsonResult updateState(@RequestBody @ApiParam(name = "参数", value = " 例如：{\r\n"
            + " \"taskId\":\"5c87a3bc550d6900066b1901\",\r\n"
            + " \"newStatus\":\"uploaded\" \r\n}") Map request) throws Exception {
        //必填参数为：taskId：任务id，newStatus：新任务状态
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        String creator = user.getId();
        String taskId = (String) request.get("taskId");
        if (CommonUtils.isNullOrEmpty(taskId)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"任务id不能为空！"});
        }
        FdevTask taskParam = null;
        List<FdevTask> list = fdevTaskService.query(new FdevTask.FdevTaskBuilder().id(taskId).init());
        if (CommonUtils.isNullOrEmpty(list)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"根据任务ID查询任务返回为空！"});
        }
        taskParam = list.get(0);
        /*需求信息*/
        String rqrmnt_no = taskParam.getRqrmnt_no();
        Map demand = new HashMap();
        if (CommonUtils.isNotNullOrEmpty(rqrmnt_no)) {
            demand = demandService.queryDemandInfoDetail(rqrmnt_no);
        }
        Map group = fdevTaskService.group(taskParam.getGroup());
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("【" + (String) group.get("name") + "-");
        if ("tech".equals(demand.get("demand_type"))) {
            stringBuffer.append("科技需求");
        } else if ("business".equals(demand.get("demand_type"))) {
            stringBuffer.append("业务需求");
        }
        stringBuffer.append("-" + (String) demand.get("oa_contact_no") + "】");
        String title = stringBuffer.toString();
        String designRemark = (String) request.get("designRemark");
        if (designRemark != null) {
            FdevTask fdevTask = new FdevTask.FdevTaskBuilder().id(taskId).init();
            fdevTask.setDesignRemark(designRemark);
            fdevTaskService.updateDesignRemark(fdevTask);
            logger.info("更新设计还原备注成功designRemark={}", designRemark);
            String creator1 = taskParam.getCreator();
            String[] developer = taskParam.getDeveloper();
            asyncTaskUnit.sendNotify(taskParam, developer.length > 0 ? developer[0] : creator1, taskParam.getName() + "--设计还原审核修改建议已提交请重新上传");
            asyncTaskUnit.sendToList(taskParam, developer.length > 0 ? developer[0] : creator1, taskParam.getName() + "--设计还原审核修改建议已提交请重新上传", "no_pass");
            //删除审核代办
            asyncTaskUnit.deleteTodolist(taskId, "notify_reviewer", creator);
            return JsonResultUtil.buildSuccess();
        }

        String review_status = taskParam.getReview_status();
        String newStatus = (String) request.get("newStatus");
        if (CommonUtils.isNullOrEmpty(newStatus)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"新状态不能为空！"});
        }
        Map param = new HashMap();
        if (!CommonUtils.isNullOrEmpty(request.get("deleteFile")) && (boolean) request.get("deleteFile")) {
            if (CommonUtils.isNullOrEmpty(request.get("fileId"))) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"上传文件id不能为空！"});
            }
            Map<String, List<String>> map = Optional.ofNullable(taskParam.getDoc_uploader()).orElse(new HashMap<String, List<String>>());
            String fileId = (String) request.get("fileId");
            Map<String, List<String>> map1 = new HashMap<String, List<String>>();
            if (!map.isEmpty()) {
                for (String key : map.keySet()) {
                    List<String> list2 = map.get(key);
                    List<String> tem = new ArrayList<String>();
                    list2.forEach(r -> {
                        if (!fileId.equals(r)) {
                            tem.add(r);
                        }
                    });
                    if (CollectionUtils.isNotEmpty(tem)) {
                        map1.put(key, tem);
                    }
                }
            }
            taskParam.setDoc_uploader(map1);
            fdevTaskService.update(taskParam);
            return JsonResultUtil.buildSuccess(param);
        }

        String taskName = taskParam.getName();
        String link = baseLink + taskId + "/design";
        String isInvolved = (String) request.get("isInvolved");
        if (Dict.UNINVOLVED.equals(newStatus) || Dict.UNINVOLVED.equals(isInvolved)
                || Dict.REFUSEUNINVOLVED.equals(newStatus)) {
            //不涉及,开发人员点击上送参数newStatus=UNINVOLVED,负责人点击完成参数newStatus=finished,isInvolved=UNINVOLVED
            String name = (String) request.get("name");
            String time = (String) request.get("time");
            String remark = (String) request.get("remark");
            Map<String, List<Map<String, String>>> designMap = Optional
                    .ofNullable(taskParam.getDesignMap())
                    .orElse(new HashMap<String, List<Map<String, String>>>());
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("name", name);
            dataMap.put("time", time);
            dataMap.put("remark", remark);
            List<String> uiManager = new ArrayList<>();
            //根据id查询UI团队管理人员id
            List<Map> uiManagerList = getRoleInfo(Constants.UI_TEAM_MANAGER);
            List<String> emailList = new ArrayList<>();
            List<String> uiManagerNameEnList = new ArrayList<>();
            uiManagerList.forEach(s -> {
                uiManager.add((String) s.get(Dict.ID));
                if (!CommonUtils.isNullOrEmpty(s.get(Dict.EMAIL))) emailList.add((String) s.get(Dict.EMAIL));
                uiManagerNameEnList.add((String) s.get(Dict.USER_NAME_EN));
            });
            String[] emails;
            String confKey;
            if (CommonUtils.isNullOrEmpty(newStatus)) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"newStatus参数为空"});
            }
            if (Dict.UNINVOLVED.equals(newStatus)) {
                //开发人员点击，给ui团队负责人发通知邮件
                emails = emailList.toArray(new String[emailList.size()]);
                List<Map<String, String>> uninvolvedList = designMap.get(Dict.UNINVOLVED);
                if (!CommonUtils.isNullOrEmpty(uninvolvedList)) {
                    uninvolvedList.addAll(Arrays.asList(dataMap));
                    designMap.put(Dict.UNINVOLVED, uninvolvedList);
                } else {
                    designMap.put(Dict.UNINVOLVED, Arrays.asList(dataMap));
                }
                confKey = Constants.EMAIL_NOTIFYREVIEWER;
                //添加代办事项
                String desc = taskName + "--设计还原审核任务审核";
                HashMap todoMap = new HashMap<>();
                todoMap.put(Dict.USER_IDS, uiManager);
                todoMap.put(Dict.MODULE, Dict.TASK);
                todoMap.put(Dict.DESCRIPTION, desc);
                todoMap.put(Dict.LINK, link);
                todoMap.put(Dict.TYPE, "notify_reviewer");
                todoMap.put(Dict.CREATE_USER_ID, creator);
                todoMap.put(Dict.TARGET_ID, taskId);
                ToDoList toDoList = CommonUtils.mapToBean(todoMap, ToDoList.class);
                toDoList.setCreate_date(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN));
                toDoList.setProject_id(taskParam.getProject_id());
                toDoList.setModule(Constants.TASK_MODULE);
                toDoList.setTask_id(taskId);
                //给本地的todo表也添加待办记录，方便后面删除任务的时候，连带待办一起删除
                this.todoListApi.save(toDoList);
                iUserApi.addTodoList(todoMap);

                //发送通知
                iNotifyApi.sendFdevNotify(desc, "0", uiManagerNameEnList.toArray(new String[uiManagerNameEnList.size()]), link, "设计还原审核任务审核");
            } else {
                //ui负责人点击
                //删除代办
                asyncTaskUnit.deleteTodolist(taskId, "notify_reviewer", creator);
                //给开发人员发邮件，流程结束
                if (!uiManager.contains(creator) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
                    throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"权限不足，请联系UI团队负责人！"});
                }
                //拒绝跳过审核
                if (Dict.REFUSEUNINVOLVED.equals(newStatus)) {
                    List<Map<String, String>> refuseUninvolvedList = designMap.get(Dict.REFUSEUNINVOLVED);
                    if (!CommonUtils.isNullOrEmpty(refuseUninvolvedList)) {
                        refuseUninvolvedList.addAll(Arrays.asList(dataMap));
                        designMap.put(Dict.REFUSEUNINVOLVED, refuseUninvolvedList);
                    } else {
                        designMap.put(Dict.REFUSEUNINVOLVED, Arrays.asList(dataMap));
                    }
                    confKey = "email.task.feedbacknopassinform";
                } else {
                    //同意跳过审核
                    designMap.put(Dict.FINISHED, Arrays.asList(dataMap));
                    confKey = "email.task.feedbackpassinform";
                }
                String[] developer = taskParam.getDeveloper();
                emails = new String[developer.length];
                for (int i = 0; i < developer.length; i++) {
                    Map userParam = new HashMap<>();
                    userParam.put(Dict.ID, developer[i]);
                    Map queryUser = iUserApi.queryUser(userParam);
                    emails[i] = (String) queryUser.get(Dict.EMAIL);
                }
            }
            taskParam.setDesignMap(designMap);
            taskParam.setReview_status(newStatus);
            fdevTaskService.update(taskParam);
            if (uiSendReviewEmail) {
                HashMap map = new HashMap<>();
                map.put(Dict.TASKNAME, taskName);
                map.put(Dict.LINK, link);
                mailUtil.sendTaskReviewMail(confKey, map, title, taskName, emails);
            }
        } else if (Dict.UPLOADED.equals(newStatus)) {//uploaded  即准备从wait_upload更新为uploaded  需要携带folderId
            if (CommonUtils.isNullOrEmpty(request.get("folderId"))) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"上传文件夹id不能为空！"});
            }
            if (CommonUtils.isNullOrEmpty(request.get("fileId"))) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"上传文件id不能为空！"});
            }
            String folderId = (String) request.get("folderId");
            //文档上传人员记录
            String fileId = (String) request.get("fileId");
            Map<String, List<String>> map = Optional.ofNullable(taskParam.getDoc_uploader()).orElse(new HashMap<String, List<String>>());
            if (CollectionUtils.isEmpty((map.get(creator)))) {
                map.put(creator, Arrays.asList(fileId));
            } else {
                List<String> list2 = map.get(creator);
                if (!list2.contains(fileId)) {
                    list2.add(fileId);
                }
                map.put(creator, list2);
            }
            // 文档信息存库
            //---------新还原设计流程额外参数
            String name = (String) request.get("name");
            String time = (String) request.get("time");
            String remark = (String) request.get("remark");
            Map<String, List<Map<String, String>>> designMap = Optional
                    .ofNullable(taskParam.getDesignMap())
                    .orElse(new HashMap<String, List<Map<String, String>>>());
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("name", name);
            dataMap.put("time", time);
            dataMap.put("remark", remark);
            designMap.put(Dict.UPLOADED, Arrays.asList(dataMap));
            taskParam.setDesignMap(designMap);
            logger.info("新流程获取到的参数name{}，time{},remark{},designMap{}", name, time, remark, designMap);
            //---------新还原设计流程额外参数
            taskParam.setDoc_uploader(map);
            taskParam.setFolder_id(folderId);
            taskParam.setReview_status(newStatus);
            fdevTaskService.update(taskParam);
        } else if (Dict.FIXING.equals(newStatus)) {//fixing  即准备从wait_allot更新为fixing  需要携带审核人信息
            String reviewer = (String) request.get("reviewer");
            if (CommonUtils.isNullOrEmpty(reviewer)) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"审核人不能为空！"});
            }
            //根据id查询UI团队管理人员id
            List<String> uiManager = new ArrayList<>();
            List<Map> uiManagerList = getRoleInfo(Constants.UI_TEAM_MANAGER);
            uiManagerList.forEach(s -> {
                uiManager.add((String) s.get(Dict.ID));
            });
            //增加卡点权限
            if (!uiManager.contains(creator) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"权限不足，请联系UI团队负责人分配审核人！"});
            }
            List<Map> uiDesigner = getRoleInfo(Constants.UI_TEAM_DESIGNER);
            List<String> uiDesignerId = new ArrayList<>();
            uiDesigner.forEach(s -> {
                uiDesignerId.add((String) s.get(Dict.ID));
            });
            //增加卡点权限
            if (!uiDesignerId.contains(reviewer) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"审核人信息错误，审核人必须是UI团队设计师角色！"});
            }
            //---------新还原设计流程额外参数
            String name = (String) request.get("name");
            String time = (String) request.get("time");
            String remark = (String) request.get("remark");
            Map<String, List<Map<String, String>>> designMap = Optional
                    .ofNullable(taskParam.getDesignMap())
                    .orElse(new HashMap<String, List<Map<String, String>>>());
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("name", name);
            dataMap.put("time", time);
            dataMap.put("remark", remark);
            designMap.put(Dict.FIXING, Arrays.asList(dataMap));
            taskParam.setDesignMap(designMap);
            logger.info("新流程获取到的参数name{}，time{},remark{},designMap{}", name, time, remark, designMap);
            //---------新还原设计流程额外参数


            //审核人信息存库
            taskParam.setReviewer(reviewer);
            taskParam.setReview_status(newStatus);
            fdevTaskService.update(taskParam);

            //发送邮件
            Map userParam = new HashMap<>();
            userParam.put(Dict.ID, reviewer);
            Map queryUser = iUserApi.queryUser(userParam);
            String reviewerEamil = (String) queryUser.get(Dict.EMAIL);
            String reviewerNameEn = (String) queryUser.get(Dict.USER_NAME_EN);

            String desc = taskName + "--设计还原审核任务审核";

            if (uiSendReviewEmail) {
                HashMap map = new HashMap<>();
                map.put(Dict.TASKNAME, taskName);
                map.put(Dict.LINK, link);
                mailUtil.sendTaskReviewMail(Constants.EMAIL_NOTIFYREVIEWER, map, title, taskName, new String[]{reviewerEamil});
            }
            //添加代办事项
            HashMap todoMap = new HashMap<>();
            todoMap.put(Dict.USER_IDS, new String[]{reviewer});
            todoMap.put(Dict.MODULE, Dict.TASK);
            todoMap.put(Dict.DESCRIPTION, desc);
            todoMap.put(Dict.LINK, link);
            todoMap.put(Dict.TYPE, "notify_reviewer");
            todoMap.put(Dict.TARGET_ID, taskId);
            todoMap.put(Dict.CREATE_USER_ID, creator);
            ToDoList toDoList = CommonUtils.mapToBean(todoMap, ToDoList.class);
            toDoList.setCreate_date(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN));
            toDoList.setProject_id(taskParam.getProject_id());
            toDoList.setModule(Constants.TASK_MODULE);
            toDoList.setTask_id(taskId);
            this.todoListApi.save(toDoList);
            iUserApi.addTodoList(todoMap);

            //发送通知
            iNotifyApi.sendFdevNotify(desc, "0", new String[]{reviewerNameEn}, link, "设计还原审核任务审核");
            asyncTaskUnit.deleteTodolist(taskId, "choose_reviewer", creator);
        } else if (Dict.NOPASS.equals(newStatus)) {
            //---------新还原设计流程额外参数
            //存在多个
            String name = (String) request.get("name");
            String time = (String) request.get("time");
            String remark = (String) request.get("remark");
            String stage = (String) request.get("stage");
            Map<String, List<Map<String, String>>> designMap = Optional
                    .ofNullable(taskParam.getDesignMap())
                    .orElse(new HashMap<String, List<Map<String, String>>>());
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("name", name);
            dataMap.put("time", time);
            dataMap.put("remark", remark);
            dataMap.put("stage", stage);
            List<Map<String, String>> nopass = Optional
                    .ofNullable(designMap.get("nopass")).orElse(new ArrayList<Map<String, String>>());
            nopass.add(dataMap);
            designMap.put(Dict.NOPASS, nopass);
            taskParam.setDesignMap(designMap);
            logger.info("新流程获取到的参数name{}，time{},remark{},designMap{},stage{}", name, time, remark, designMap, stage);
            //---------新还原设计流程额外参数
            taskParam.setReview_status(newStatus);
            //发邮件
            String[] developer = taskParam.getDeveloper();
            String[] feedbackreviewerEamil = new String[developer.length];
            for (int i = 0; i < developer.length; i++) {
                Map userParam = new HashMap<>();
                userParam.put(Dict.ID, developer[i]);
                Map queryUser = iUserApi.queryUser(userParam);
                feedbackreviewerEamil[i] = (String) queryUser.get(Dict.EMAIL);
            }
            if (uiSendReviewEmail && Constants.STAGE_LOADNOPASS.equals(stage)) {
                HashMap map = new HashMap<>();
                map.put(Dict.TASKNAME, taskName);
                map.put(Dict.LINK, link);
                mailUtil.sendTaskReviewMail("email.task.feedbacknopassinform", map, title, taskName, feedbackreviewerEamil);
            }
            fdevTaskService.update(taskParam);

            String targetId = "";
            if ("load_upload".equals(stage)) {
                String desc = taskName + "--设计还原审核已重新上传";
                List<Map<String, String>> uploaded = designMap.get("uploaded");
                targetId = taskParam.getReviewer();
                //发送代办
                asyncTaskUnit.sendToList(taskParam, targetId, desc, "notify_reviewer");
                //发送通知
                asyncTaskUnit.sendNotify(taskParam, targetId, desc);
                asyncTaskUnit.deleteTodolist(taskId, "no_pass", user.getId());
            }
        } else if (Dict.FINISHED.equals(newStatus) || Dict.FIXED.equals(newStatus)) {//分配审核人之后的步骤仅审核人可操作
            String reviewer = taskParam.getReviewer();
            // 增加卡点权限
            if (!creator.equals(reviewer) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"权限不足，请联系审核人修改审核状态！"});
            }
            //---------新还原设计流程额外参数
            String name = (String) request.get("name");
            String time = (String) request.get("time");
            String remark = (String) request.get("remark");
            Map<String, List<Map<String, String>>> designMap = Optional
                    .ofNullable(taskParam.getDesignMap())
                    .orElse(new HashMap<String, List<Map<String, String>>>());
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("name", name);
            dataMap.put("time", time);
            dataMap.put("remark", remark);
            designMap.put(Dict.FINISHED, Arrays.asList(dataMap));
            taskParam.setDesignMap(designMap);
            logger.info("新流程获取到的参数name{}，time{},remark{},designMap{}", name, time, remark, designMap);
            //---------新还原设计流程额外参数
            //更新数据库中状态
            taskParam.setReview_status(newStatus);
            if (Dict.FINISHED.equals(newStatus)) {
                //发邮件
                String[] developer = taskParam.getDeveloper();
                String[] feedbackreviewerEamil = new String[developer.length];
                for (int i = 0; i < developer.length; i++) {
                    Map userParam = new HashMap<>();
                    userParam.put(Dict.ID, developer[i]);
                    Map queryUser = iUserApi.queryUser(userParam);
                    feedbackreviewerEamil[i] = (String) queryUser.get(Dict.EMAIL);
                }
                if (uiSendReviewEmail) {
                    HashMap map = new HashMap<>();
                    map.put(Dict.TASKNAME, taskName);
                    map.put(Dict.LINK, link);
                    mailUtil.sendTaskReviewMail("email.task.feedbackpassinform", map, title, taskName, feedbackreviewerEamil);
                }
            }
            fdevTaskService.update(taskParam);
            for (String s : taskParam.getDeveloper()) {
                asyncTaskUnit.deleteTodolist(taskParam.getId(), "no_pass", s);
            }
            asyncTaskUnit.deleteTodolist(taskId, "notify_reviewer", user.getId());
        } else if (Dict.WAIT_ALLOT.equals(newStatus)) {//点击下一步
            //---------新还原设计流程额外参数
            String name = (String) request.get("name");
            String time = (String) request.get("time");
            String remark = (String) request.get("remark");
            Map<String, List<Map<String, String>>> designMap = Optional
                    .ofNullable(taskParam.getDesignMap())
                    .orElse(new HashMap<String, List<Map<String, String>>>());
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("name", name);
            dataMap.put("time", time);
            dataMap.put("remark", remark);
            designMap.put(Dict.WAIT_ALLOT, Arrays.asList(dataMap));
            taskParam.setDesignMap(designMap);
            logger.info("新流程获取到的参数name{}，time{},remark{},designMap{}", name, time, remark, designMap);
            //---------新还原设计流程额外参数


            //更新数据库中状态
            taskParam.setReview_status(newStatus);
            taskParam.setUiVerifyReporter(user.getId());
            fdevTaskService.update(taskParam);

            //根据id查询UI团队管理人员id和邮箱和英文名
            List<Map> uiManagerList = getRoleInfo(Constants.UI_TEAM_MANAGER);
            if (CommonUtils.isNullOrEmpty(uiManagerList)) {
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"UI团队负责人为空，请联系UI团队添加负责人角色！"});
            }
            List<String> uiManager = new ArrayList<>();
            List<String> uiManagerEmailList = new ArrayList<>();
            List<String> uiManagerNameEnList = new ArrayList<>();
            uiManagerList.forEach(s -> {
                uiManager.add((String) s.get(Dict.ID));
                uiManagerEmailList.add((String) s.get(Dict.EMAIL));
                uiManagerNameEnList.add((String) s.get(Dict.USER_NAME_EN));
            });
            String desc = taskName + "--设计还原审核指派审核人";

            //发送邮件
            if (uiSendReviewEmail) {
                HashMap map = new HashMap<>();
                map.put(Dict.TASKNAME, taskName);
                map.put(Dict.LINK, link);
                mailUtil.sendTaskReviewMail(Constants.EMAIL_CHOOSEREVIEWER, map, title, taskName, uiManagerEmailList.toArray(new String[uiManagerEmailList.size()]));
            }

            //添加代办事项
            HashMap todoMap = new HashMap<>();
            todoMap.put(Dict.USER_IDS, uiManager);
            todoMap.put(Dict.MODULE, Dict.TASK);
            todoMap.put(Dict.DESCRIPTION, desc);
            todoMap.put(Dict.LINK, link);
            todoMap.put(Dict.TYPE, "choose_reviewer");
            todoMap.put(Dict.CREATE_USER_ID, creator);
            todoMap.put(Dict.TARGET_ID, taskId);
            ToDoList toDoList = CommonUtils.mapToBean(todoMap, ToDoList.class);
            toDoList.setCreate_date(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN));
            toDoList.setProject_id(taskParam.getProject_id());
            toDoList.setModule(Constants.TASK_MODULE);
            toDoList.setTask_id(taskId);
            //给本地的todo表也添加待办记录，方便后面删除任务的时候，连带待办一起删除
            this.todoListApi.save(toDoList);
            iUserApi.addTodoList(todoMap);

            //发送通知
            iNotifyApi.sendFdevNotify(desc, "0", uiManagerNameEnList.toArray(new String[uiManagerNameEnList.size()]), link, "设计还原审核指派审核人");
        }
        return JsonResultUtil.buildSuccess(param);
    }

    /**
     * 根据组名称查询ui团队负责人信息
     *
     * @return
     */
    private List getRoleInfo(String roleName) {
        List uiList = new ArrayList();
        try {
            Map param = new HashMap();
            List roleId = new ArrayList();
            List roles = iUserApi.queryRole();
            for (Object role : roles) {
                Map tmp = (Map) role;
                if (roleName.equals(tmp.get(Dict.NAME))) {
                    roleId.add(tmp.get(Dict.ID));
                    break;
                }
            }
            if (roleId.isEmpty()) {
                return new ArrayList();
            }
            param.put(Dict.ROLE_ID, roleId);
            uiList = (List) iUserApi.queryUserAll(param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (CommonUtils.isNullOrEmpty(uiList)) {
            logger.info("没有查询到" + roleName + "信息！");
        }
        return uiList;
    }

    @OperateRecord(operateDiscribe = "任务模块-新增任务")
    @ApiOperation(value = "新增任务")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public JsonResult addTask(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        String creator = user.getId();
        //将关联项数据转成json格式
        if (request.containsKey(Dict.REVIEW)) {
            Map review = (Map) request.get(Dict.REVIEW);
            String[] reviewStr = {Dict.OTHER_SYSTEM, Dict.DATA_BASE_ALTER, Dict.EBANK_COMMON_ALTER, Dict.FIRE_WALL_OPEN,
                    Dict.INTERFACE_ALTER, Dict.SCRIPT_ALTER, Dict.STATIC_RESOURCE};
            for (String key : reviewStr) {
                if (review.containsKey(key)) {
                    ArrayList other_system = (ArrayList) review.get(key);
                    review.put(key, format(other_system));
                }
            }
        }
        FdevTask task = CommonUtils.mapToBean(request, FdevTask.class);
        Map<String, Object> demandAndUnitInfo = (Map) demandService.queryByFdevNo("", task.getFdev_implement_unit_no());
        Map implement_unit_info = (Map) demandAndUnitInfo.get(Dict.IMPLEMENT_UNIT_INFO);
        if (CommonUtils.isNullOrEmpty(implement_unit_info)) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"研发单元不存在"});
        }
        //实施单元拆分，将uat完成日期和rel启动日期字段合并，确保2个字段的赋值一致
        task.setPlan_uat_test_stop_time(task.getPlan_rel_test_time());
        String demandType = (String) implement_unit_info.get(Dict.DEMAND_TYPE);
        //如果是建的日常任务，不需要sit、uat和rel的计划时间
        if ("2".equals(String.valueOf(task.getTaskType()))) {
            task.setPlan_inner_test_time("");
            task.setPlan_uat_test_start_time("");
            task.setPlan_uat_test_stop_time("");
            task.setPlan_rel_test_time("");
        }
        //如果建的是无代码任务，不需要应用和分支
        if("1".equals(String.valueOf(task.getTaskType()))) {
            task.setProject_id("");
            task.setFeature_branch("");
        }
        //校验需求类型和任务类型，如果是新增日常任务，选择的需求是开发需求，同一研发单元下必须有一个开发任务
        //如果选择的需求是日常需求，只能新增日常任务
        checkTaskType(task, demandType);
        //业务需求,验证任务计划时间是否小于研发单元计划时间
        if (Constants.DEMANDTYPE_BUSINESS.equals(demandType)) {
            fdevTaskService.checkPlanTime(request, implement_unit_info);
        }
        //如果研发单元为评估中，不可以创建任务
        if ((Integer) implement_unit_info.get(Dict.IMPLEMENT_UNIT_STATUS_NORMAL) == 1) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"任务创建失败，当前研发单元为评估中"});
        }
        //如果研发单元为暂缓或者恢复中，不可以创建任务
        Integer specialStatus = (Integer) implement_unit_info.get(Dict.IMPLEMENT_UNIT_STATUS_SPECIAL);
        if (specialStatus != null && (specialStatus == 1 || specialStatus == 2)) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"任务创建失败，当前研发单元尚未恢复"});
        }
        //校验任务名是否重复
        String taskName = (String) request.get(Dict.NAME);
        FdevTask task1 = new FdevTask();
        task1.setName(taskName);
        List<FdevTask> all_tasks = fdevTaskService.query(task1);
        List<String> allTasks_names = new ArrayList<>();
        if (all_tasks != null && all_tasks.size() >= 1) {
            for (FdevTask all_task : all_tasks) {
                allTasks_names.add(all_task.getName());
            }
        }
        if (allTasks_names.contains(taskName)) {
            throw new FdevException(ErrorConstants.TASK_NAME_EROOR);
        }
        //开发任务
        if (null == task.getTaskType()) {
            if (CommonUtils.isNullOrEmpty(task.getProject_id())) {
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"请关联应用"});
            }
            if (CommonUtils.isNullOrEmpty(task.getFeature_branch())) {
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"请输入分支名"});
            }
            //校验各项时间是否正常
            checkTime(task);
        }
        //输入了分支就创建分支
        String isTest = "";//是否涉及内测
        if (!CommonUtils.isNullOrEmpty(task.getFeature_branch())) {
            if (CommonUtils.isNullOrEmpty(task.getProject_id())) {
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"请关联应用"});
            }
            if(CommonUtils.isNullOrEmpty(task.getApplicationType())) {
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"应用类型不能为空"});
            }
            String appType = task.getApplicationType();//应用类型
            //如果是组件，校验输入的预设版本号
            if(Constants.COMPONENT_WEB.equals(appType)) {
                componentService.judgePredictVersion(task.getProject_id(), task.getVersionNum(), Constants.MPASS);
            }
            if(Constants.COMPONENT_SERVER.equals(appType)) {
                componentService.judgePredictVersion(task.getProject_id(), task.getVersionNum(), Constants.BACK);
            }
            //如果是后端骨架，校验输入的目标版本号
            if(Constants.ARCHETYPE_SERVER.equals(appType)) {
                componentService.judgeTargetVersion(task.getProject_id(), task.getVersionNum());
            }
            //获取关联项目信息
            Map<String, Object> projectInfo = fdevTaskService.getProjectInfo(task.getProject_id(), appType);
            String gitlabId = (String) projectInfo.get(Dict.GITLAB_ID);//应用git id
            task.setProject_name((String) projectInfo.get(Dict.PROJECTNAME));
            isTest = (String) projectInfo.get(Dict.ISTEST);
            //去除分支名中可能存在的不可见字符和空格
            String featureBranch = task.getFeature_branch().replace("\u200b", "").replaceAll(" ", "");

            task.setFeature_branch(featureBranch);
            //创建分支或关联分支
            if("1".equals(request.get("branchType"))){
                //校验分支名是否重复
                checkFeatureBranch(featureBranch, appType, gitlabId);
                //创建分支
                createFeatureBranch(task, appType, gitlabId);
            }
            //设置设计还原审核状态初始值，需求中选择涉及前端且vue项目为待上传截图，其他任务为不涉及
            if (Constants.APPTYPE_VUE.equals(appType)) {
                task.setReview_status(Dict.WAIT_UPLOAD);
            } else {
                task.setReview_status(Dict.IRRELEVANT);
            }
            // 添加git的权限
            HashSet members = fdevTaskUtils.getMembers(null, task);
            //现在项目管理员权限不动
            members.removeAll((List<String>) projectInfo.get(Dict.MANAGER));
            fdevTaskService.addMemberGit(gitlabId, members, appType, task.getProject_id());
        }
        if (!CommonUtils.isNullOrEmpty(task.getStoryId())) {
            jiraIssuesService.queryJiraStoryByKey(task.getStoryId());
        }

        if (request.containsKey(Dict.TAG)) {
            try {
                List<String> tmp = (ArrayList) request.get(Dict.TAG);
                task.setTag(CommonUtils.distinctArray(tmp.toArray(new String[tmp.size()])));
            } catch (Exception e) {
                logger.error("解析任务Tag失败");
            }
        }
        // 新建任务，把任务id给下面
        String date = CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1);
        task.setStart_time(date);// 任务启动时间
        task.setCreator(creator);
        ObjectId id = new ObjectId();
        task.set_id(id);
        task.setId(id.toString());
        task.setConfirmBtn("0");
        task.setRedmine_id(task.getFdev_implement_unit_no());
        task.setStage(Dict.TASK_STAGE_DEVELOP);
        String simpleFlag = (String) request.get("simpleDemand"); // 是否简单需求任务 0-是（不走内测） 1-否(走内测)
        task.setSimpleDemand(CommonUtils.isNullOrEmpty(simpleFlag)? "1": simpleFlag);
        task.setSkipFlag("0");
        //日常任务和无代码任务不创建工单，不涉及内测不创建工单
        if("1".equals(isTest) && CommonUtils.isNullOrEmpty(task.getTaskType()) && "1".equals(simpleFlag)) {
            createWorkOrder(demandType, (String) implement_unit_info.get(Dict.IPMP_IMPLEMENT_UNIT_NO), (boolean)implement_unit_info.get(Dict.IS_NEW), task);
        }
        //创建jira子任务并更新故事状态
        if (!CommonUtils.isNullOrEmpty(task.getStoryId())) {
            String jiraSubTaskKey = jiraIssuesService.createJiraSubTask(task);
            asyncService.updateJiraStoryStatus(task);
            asyncService.updateJiraAssignee(jiraSubTaskKey, CommonUtils.isNullOrEmpty(task.getDeveloper()) ? "" : task.getDeveloper()[0]);
            task.setJiraKey(jiraSubTaskKey);
        }
        if("1".equals(String.valueOf(task.getTaskType()))) {
            Map<String, NoCodeInfo> nocodeInfoMap = new HashMap<>(4);
            NoCodeInfo noCodeInfo = new NoCodeInfo();
            String desc = task.getDesc();
            if (null == desc || "".equals(desc)) {
                desc = "无";
            }
            noCodeInfo.setTaskDesc(desc);
            noCodeInfo.setRelators(new Relator[]{});
            noCodeInfo.setFinishTime("");
            nocodeInfoMap.put(Dict.TASK_STAGE_SIT, noCodeInfo);
            nocodeInfoMap.put(Dict.TASK_STAGE_UAT, noCodeInfo);
            nocodeInfoMap.put(Dict.TASK_STAGE_REL, noCodeInfo);
            task.setNocodeInfoMap(nocodeInfoMap);
            task.setStage(Dict.TASK_STAGE_SIT);
            //无代码变更直接进入sit  给定sit开始时间
            task.setStart_inner_test_time(CommonUtils.dateFormat(new Date(),CommonUtils.DATE_PATTERN_F1));
        }
        FdevTask rTask = fdevTaskService.save(task);
        //发送邮件
        if (isSendMail) {
            asyncService.sendMail(null, rTask, user.getUser_name_cn(), "");
        }
        //创建审核
        reviewRecordService.saveSqlReview(rTask);
        if(CommonUtils.isNullOrEmpty(task.getTaskType())) {
            Map<String, Object> releaseTaskApiParam = new HashMap<>();
            releaseTaskApiParam.put(Dict.TASK_ID, rTask.getId());
            ReleaseTaskApi.updateRqrmntInfoReview(releaseTaskApiParam);
        }
        // 更新需求状态
        asyncService.updateRqrmntWhenStageChanged(task, false);
        return JsonResultUtil.buildSuccess(rTask);
    }

    /**
     * 创建玉衡工单
     *
     * @param demandType
     * @param unitNo
     * @param isNew
     * @param task
     * @throws Exception
     */
    private void createWorkOrder(String demandType, String unitNo, boolean isNew, FdevTask task) throws Exception {
        if (Constants.DEMANDTYPE_TECH.equals(demandType)
                || CommonUtils.isNullOrEmpty(isNew) || !isNew) {
            //如果是科技需求，根据研发单元编号创建玉衡工单
            createWorkOrder(task.getFdev_implement_unit_no(), task.getId(), task.getName(), task.getDesc());
        } else if ((Constants.DEMANDTYPE_BUSINESS.equals(demandType)
                && CommonUtils.isNotNullOrEmpty(unitNo))) {
            //如果是业务需求，且挂载了实施单元，根据实施单元id创建玉衡工单
            Map<String, String> sendMap = new HashMap<>();
            sendMap.put(Dict.IMPL_UNIT_NUM, unitNo);
            sendMap.put(Dict.REST_CODE, "queryIpmpUnitById");
            //先根据实施单元编号查询详情，获取id
            Map<String, Object> implUnitInfo = (Map<String, Object>) restTransport.submit(sendMap);
            if (!CommonUtils.isNullOrEmpty(implUnitInfo)) {
                createWorkOrder((String) implUnitInfo.get(Dict.ID), task.getId(), task.getName(), task.getDesc());
            }
        }
    }

    //调用玉衡创建工单接口，传入实施单元id或者研发单元编号
    public void createWorkOrder(String unitNo, String taskNo, String taskName, String requireRemark) {
        Map test_param = new HashMap();
        test_param.put(Dict.UNITNO, unitNo);
        test_param.put(Dict.TASKNO, taskNo);
        test_param.put(Dict.TASKNAME, taskName);
        test_param.put(Dict.REQUIREREMARK, requireRemark);
        test_param.put("kafkaTopic", addOrderTopic);
        test_param.put(Dict.REST_CODE, "addRequireImplementNo");
        asyncService.sendMessage(test_param);
    }

    /**
     * 创建任务时校验各计划时间是否正常
     *
     * @param task
     * @throws ParseException
     */
    public void checkTime(FdevTask task) throws ParseException {
        String regex = "^[0-9]{4}[/]{1}[0-9]{2}[/]{1}[0-9]{2}$";
        String planStartTime = task.getPlan_start_time();//计划启动日期
        if (CommonUtils.isNotNullOrEmpty(planStartTime) && !Pattern.matches(regex, planStartTime)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"计划启动日期", "格式错误"});
        }
        String planInnerTestTime = task.getPlan_inner_test_time();//计划提交内测日期
        if (CommonUtils.isNotNullOrEmpty(planInnerTestTime) && !Pattern.matches(regex, planInnerTestTime)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"计划提交内测日期", "格式错误"});
        }
        //校验计划提交用户测试日期和计划用户测试完成日期是否正常
        String planUatTestStartTime = task.getPlan_uat_test_start_time();//计划提交用户测试日期
        if (CommonUtils.isNotNullOrEmpty(planUatTestStartTime) && !Pattern.matches(regex, planUatTestStartTime)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"计划提交用户测试日期", "格式错误"});
        }
        String planRelTestTime = task.getPlan_rel_test_time();//计划用户测试完成日期
        if (CommonUtils.isNotNullOrEmpty(planRelTestTime) && !Pattern.matches(regex, planRelTestTime)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"计划用户测试完成日期", "格式错误"});
        }
        String planFireTime = task.getPlan_fire_time();//计划投产日期
        if (CommonUtils.isNotNullOrEmpty(planFireTime) && !Pattern.matches(regex, planFireTime)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"计划投产日期", "格式错误"});
        }
        String proWantWindow = task.getProWantWindow();//投产意向窗口
        if (CommonUtils.isNotNullOrEmpty(proWantWindow) && !Pattern.matches(regex, proWantWindow)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"投产意向窗口", "格式错误"});
        }
        SimpleDateFormat sdf = new SimpleDateFormat(CommonUtils.DATE_PATTERN_F1);
        if (CommonUtils.isNotNullOrEmpty(planStartTime, planInnerTestTime)
                && sdf.parse(planStartTime).after(sdf.parse(planInnerTestTime))) {
            throw new FdevException(ErrorConstants.DATE_AFTER_ERROR, new String[]{"计划启动日期", "计划提交内测日期" + planInnerTestTime});
        }
        if (CommonUtils.isNotNullOrEmpty(planInnerTestTime, planUatTestStartTime)
                && sdf.parse(planInnerTestTime).after(sdf.parse(planUatTestStartTime))) {
            throw new FdevException(ErrorConstants.DATE_AFTER_ERROR, new String[]{"计划提交内测日期", "计划提交用户测试日期" + planUatTestStartTime});
        }
        if (CommonUtils.isNotNullOrEmpty(planUatTestStartTime, planRelTestTime)
                && sdf.parse(planUatTestStartTime).after(sdf.parse(planRelTestTime))) {
            throw new FdevException(ErrorConstants.DATE_AFTER_ERROR, new String[]{"计划提交用户测试日期", "计划用户测试完成日期" + planRelTestTime});
        }
        if (CommonUtils.isNotNullOrEmpty(planRelTestTime, planFireTime)
                && sdf.parse(planRelTestTime).after(sdf.parse(planFireTime))) {
            throw new FdevException(ErrorConstants.DATE_AFTER_ERROR, new String[]{"计划用户测试完成日期", "计划投产日期" + planFireTime});
        }
        if (CommonUtils.isNotNullOrEmpty(planFireTime, proWantWindow)
                && sdf.parse(planFireTime).after(sdf.parse(proWantWindow))) {
            throw new FdevException(ErrorConstants.DATE_AFTER_ERROR, new String[]{"计划投产日期", "投产意向窗口" + proWantWindow});
        }
    }

    /**
     * @param task
     * @param demandType
     */
    public void checkTaskType(FdevTask task, String demandType) {
        if ("2".equals(String.valueOf(task.getTaskType()))
                && !Constants.DEMANDTYPE_DAILY.equals(demandType)){
            //查询研发单元下任务
            List<FdevTask> taskList = fdevTaskService.queryTaskByFdevUnitNo(task.getFdev_implement_unit_no());
            if(CommonUtils.isNullOrEmpty(taskList)
                    || CommonUtils.isNullOrEmpty(taskList.stream().filter(fdevTask -> (!"2".equals(String.valueOf(fdevTask.getTaskType())))).collect(Collectors.toList()))) {
                throw new FdevException(ErrorConstants.TASK_ERROR,
                        new String[] {"在开发需求的研发单元下新建日常任务前需要先新建一个开发任务或无代码任务，请修改任务类型"});
            }
        }
        if(!"2".equals(String.valueOf(task.getTaskType())) && Constants.DEMANDTYPE_DAILY.equals(demandType)) {
            throw new FdevException(ErrorConstants.TASK_ERROR,
                    new String[] {"在日常需求下不能新建开发任务，请修改任务类型"});
        }
    }

    /**
     * 校验分支是否存在并创建
     * @param featureBranch
     * @param appTypeName
     * @param gitlabProjectId
     * @throws Exception
     */
    public void checkFeatureBranch(String featureBranch, String appTypeName, String gitlabProjectId) throws Exception {
        if(CommonUtils.isNullOrEmpty(featureBranch)) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"请输入分支名"});
        }
        //查询同一应用下是否有相同分支
        //ios、android类型应用自动生成feature下与dev下的
        if (Constants.APPLICATION_ANDROID.equals(appTypeName) || Constants.APPLICATION_IOS.equals(appTypeName)) {
            Map<String,Object> branchInfo = gitApiService.queryBranch(gitlabProjectId, "feature/" + featureBranch);
            if(!CommonUtils.isNullOrEmpty(branchInfo)) {
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"该应用下已有相同分支"});
            }
            branchInfo = gitApiService.queryBranch(gitlabProjectId, "dev/" + featureBranch);
            if(!CommonUtils.isNullOrEmpty(branchInfo)) {
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"该应用下已有相同分支"});
            }
        }
        Map<String,Object> branchInfo = gitApiService.queryBranch(gitlabProjectId, featureBranch);
        if(!CommonUtils.isNullOrEmpty(branchInfo)) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"该应用下已有相同分支"});
        }
    }

    /**
     * 创建分支
     * @param task
     * @param appTypeName
     * @param gitlabProjectId
     * @throws Exception
     */
    private void createFeatureBranch(FdevTask task, String appTypeName, String gitlabProjectId) throws Exception {
        //创建分支
        String featureBranch = task.getFeature_branch();
        if (Constants.APPLICATION_ANDROID.equals(appTypeName) || Constants.APPLICATION_IOS.equals(appTypeName)) {
            gitApiService.createBranch(gitlabProjectId, "feature/" + featureBranch, Constants.BRANCH_MASTER);
            gitApiService.createBranch(gitlabProjectId, "dev/" + featureBranch, Constants.BRANCH_MASTER);
            iSetProtectedBranches.setProtectedBranches(gitlabProjectId, "dev/" + featureBranch, gitToken);
            task.setFeature_branch("feature/" + featureBranch);
        } else {
            gitApiService.createBranch(gitlabProjectId, featureBranch, Constants.BRANCH_MASTER);
            task.setFeature_branch(featureBranch);
        }
    }

    /**
     * @param request
     * @return JsonResult
     * @throws Exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @OperateRecord(operateDiscribe = "任务模块-删除任务")
    @RequestMapping(value = "/deleteTask", method = RequestMethod.POST)
    @ApiOperation(value = "删除任务")
    public JsonResult deleteTask(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        Map param = new HashMap();
        param.put(Dict.ID, user.getId());
        ArrayList roleList = (ArrayList) iUserApi.queryUser(param).get(Dict.ROLE_ID);
        // 厂商项目负责人 行内项目负责人 拥有权限 新增卡点管理员权限
        if (!userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            if (CommonUtils.isNullOrEmpty(roleList)
                    || !(roleList.contains(Dict.ROLE_CSII) || roleList.contains(Dict.ROLE_SPDB))) {
                return JsonResultUtil.buildError(ErrorConstants.ROLE_ERROR, "用户权限不足");
            }
        }
        ArrayList idslist = new ArrayList();
        String id = (String) request.get(Dict.ID);
        if (!CommonUtils.isNotNullOrEmpty(id)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID, "任务id不能为空!"});
        }
        String new_id= null;
        String job_id = null;
        //查询相关信息
        List<Map> subtaskList = queryMainTask(id);
        if (!CommonUtils.isNullOrEmpty(subtaskList)){
            Map subtask = subtaskList.get(0);
            //查询jobid
            if (null !=  subtask) {
                job_id = (String) subtask.get("id");
            }
            if (((List) subtask.get("taskcollection")).size()>0) {
                Map taskcollection = (Map) ((List) subtask.get("taskcollection")).get(0);
                if (null != taskcollection) {
                    new_id = (String) taskcollection.get("id");
                }
            }
        }
        idslist.add(id);
        List<FdevTask> tasklist = fdevTaskService.queryTasksByIds(idslist);
        if (CommonUtils.isNullOrEmpty(tasklist)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{id});
        }
        FdevTask task = tasklist.get(0);
        //查看是否是暂缓任务
        this.fdevTaskUtils.checkIsWaitTask(task.getTaskSpectialStatus());
        //如果是开发需求，一个研发单元的任务，不能删得只剩日常任务
        Map<String,Object> demandAndUnitInfo = (Map) demandService.queryByFdevNo("", task.getFdev_implement_unit_no());
        Map implement_unit_info = (Map) demandAndUnitInfo.get(Dict.IMPLEMENT_UNIT_INFO);
        if(!Constants.DEMANDTYPE_DAILY.equals((String)implement_unit_info.get(Dict.DEMAND_TYPE))) {
            //查询研发单元下任务
            List<FdevTask> taskList = fdevTaskService.queryTaskByFdevUnitNo(task.getFdev_implement_unit_no());
            //如果包含了日常任务
            if(taskList.stream().anyMatch(t -> "2".equals(String.valueOf(t.getTaskType())))) {
                //去除当前任务和日常任务
                taskList = taskList.stream().filter(t -> !task.getId().equals(t.getId()) && !"2".equals(String.valueOf(t.getTaskType())))
                        .collect(Collectors.toList());
                if(taskList.size() == 0) {
                    throw new FdevException(ErrorConstants.TASK_ERROR, new String[] {"开发需求下的研发单元不能只建日常任务，请先删除该研发单元下其它日常任务"});
                }
            }
        }
        //如果是无代码变更任务。直接删除
        if (task.getTaskType() != null && task.getTaskType() == 1) {
            if (task.getStage().equals(Dict.SIT)) {
                FdevTask result = fdevTaskService.deleteTask(id);
                Map<String, NoCodeInfo> nocodeInfoMap = result.getNocodeInfoMap();
                NoCodeInfo sit = nocodeInfoMap.get("sit");
                Relator[] relators = sit.getRelators();
                if (relators.length>0){
                    for (Relator relator : relators) {
                        Map deleteMap = new HashMap<>();
                        deleteMap.put(Dict.MODULE, Dict.TASK);
                        deleteMap.put(Dict.TYPE, relator.getRid());
                        deleteMap.put(Dict.TARGET_ID, relator.getRelatorId());
                        iUserApi.deleteTodoList(deleteMap);
                    }
                }
                if (isSendMail) {
                    asyncService.sendMail(result, result, user.getUser_name_cn(), "");
                }
            } else throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"当前阶段不可以删除该任务"});
            return JsonResultUtil.buildSuccess();
        }
        //如果挂载了投产窗口，需要先解除挂载
        Map releaseWindowInfo = ReleaseTaskApi.queryDetailByTaskId(task.getId(), CommonUtils.getReleaseType(task.getApplicationType()));
        if (!CommonUtils.isNullOrEmpty(releaseWindowInfo)) {
            throw new FdevException(ErrorConstants.DELETE_TASK_ERROR, new String[]{"请先解除挂载投产窗口"});
        }
        // 投产模块
        ReleaseTaskApi.deleteTask(id);
        //新任务删除
        if (task.getFdev_implement_unit_no() != null){
            Map newParam = new HashMap();
            newParam.put(Dict.TASKNOONLY, id);
            newParam.put("kafkaTopic",deleteOrderTopic);
            newParam.put(Dict.REST_CODE,"deleteOrder");
            asyncService.sendMessage(newParam);
        }
        //老任务删除主任务关联
        if ((CommonUtils.pareStage(task.getStage()) > CommonUtils.pareStage("create-feature"))&& task.getFdev_implement_unit_no() == null) {
            this.deleteSubTask(id, new_id, job_id);
        }
        //删除任务
        FdevTask result = fdevTaskService.deleteTask(id);

        //判断该任务的 代码审核编号 是否均已投产
        Set<String> code_order_no = result.getCode_order_no();
        if( !CommonUtils.isNullOrEmpty( code_order_no ) ){
            for (String no : code_order_no) {
                Map<String,Object>  map = fdevTaskService.queryTaskByCodeOrderNo(no);
                if( !CommonUtils.isNullOrEmpty(map) ){
                    //流程工具接口
                    map.put(Dict.ORDERNO,no);
                    serviceApi.updateByTask(map);
                }
            }
        }
        //删除任务的代码合并请求
        if(releaseApproveDao.getCountByTaskId(result.getId()) > 0){
            releaseApproveDao.deleteByTaskId(result.getId());
        }
        //删除对应的审核项
        reviewRecordService.deleteByTaskId(id);
        reviewRecordService.deleteByTask_Id(id);
        //删除相关待办
        ToDoList removeEntity = new ToDoList();
        removeEntity.setTask_id(id);
        try {
            this.todoListApi.remove(removeEntity);
        } catch (Exception e) {
            logger.error("删除任务时，删除相关待办失败");
        }
        Map<String, Object> releaseTaskApiParam = new HashMap<>();
        releaseTaskApiParam.put(Dict.TASK_ID, result.getId());
        ReleaseTaskApi.deleteRqrmntInfoTask(releaseTaskApiParam);
        if (isSendMail) {
            asyncService.sendMail(result, result, user.getUser_name_cn(), "");
        }
        return JsonResultUtil.buildSuccess();
    }

    private void deleteSubTask(String id, String new_id, String job_id) throws Exception {
        Map param = new HashMap();
        FdevTaskCollection tc = new FdevTaskCollection(job_id, "", new ArrayList<String>());
        FdevTaskCollection tc2 = fdevTaskService.queryByJobId(tc);
        if (!CommonUtils.isNullOrEmpty(tc2)) {//删除没有子任务的主任务
            // 子任务以下线也视为没有子任务
            if (!CommonUtils.isNotNullOrEmpty(new_id) && CommonUtils.isNullOrEmpty(fdevTaskService.queryTasksByIds((ArrayList) tc2.getTaskcollection()))) {
                this.fdevTaskService.deleteMainTask(job_id);
                param.put(Dict.JOBNO, job_id);
            } else if (CommonUtils.isNotNullOrEmpty(new_id)) {
                tc2.setTask_id(new_id);
                List<String> taskcollection = tc2.getTaskcollection();
                taskcollection.remove(taskcollection.indexOf(new_id));
                tc2.setTaskcollection(taskcollection);
                param.put(Dict.JOBNO, job_id);
                param.put(Dict.TASKNO, new_id);
                List tmp = new ArrayList();
                tmp.add(new_id);
                List<FdevTask> list = fdevTaskService.queryTasksByIds((ArrayList) tmp);
                if (CommonUtils.isNullOrEmpty(list))
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{Dict.TASK_ID});
                FdevTask task = list.get(0);
                param.put(Dict.TASKNAME, task.getName());
                param.put(Dict.INTERNALTESTSTART, task.getStart_inner_test_time()); // 测试开始时间
                param.put(Dict.INTERNALTESTEND, task.getPlan_uat_test_start_time()); // uat 开始就是sit测试结束时间
                param.put(Dict.REQUIREREMARK, task.getDesc());
            } else {
                param.put(Dict.TASKNOONLY, id);
                param.put(Dict.JOBNO, job_id);
                List<String> taskcollection = tc2.getTaskcollection();
                taskcollection.remove(taskcollection.indexOf(id));
                tc2.setTaskcollection(taskcollection);
            }
            param.put("kafkaTopic",deleteOrderTopic);
            param.put(Dict.REST_CODE,"deleteOrder");
            asyncService.sendMessage(param);
            this.fdevTaskService.updateTaskCollection(tc2);
        }
    }

    /**
     * @param request
     * @return JsonResult
     * @throws Exception
     */
    @RequestMapping(value = "/queryDeleteTaskDetail", method = RequestMethod.POST)
    @ApiOperation(value = "查询删除任务详情")
    public JsonResult queryDeleteTaskDetail(@RequestBody Map request) throws Exception {
        ArrayList idslist = new ArrayList();
        String id = (String) request.get(Dict.ID);
        idslist.add(id);
        List<FdevTask> tasklist = fdevTaskService.queryTasksByIds(idslist);
        if (CommonUtils.isNullOrEmpty(tasklist)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{id});
        }
        FdevTask task = tasklist.get(0);
        Map returnMap = new HashMap();
        String project_name = "";
        String feature_branch = "";
        String release_branch = "";
        String release_node_name = "";
        List product_tag = new ArrayList();
        List pro_image_uri = new ArrayList();
        project_name = task.getProject_name();
        feature_branch = task.getFeature_branch();
        if (CommonUtils.pareStage(task.getStage()) >= CommonUtils.pareStage(Dict.UAT)) {
            Map result = ReleaseTaskApi.queryDetailByTaskId(id, CommonUtils.getReleaseType(task.getApplicationType()));
            if (!CommonUtils.isNullOrEmpty(result)) {
                release_node_name = (String) result.get(Dict.RELEASE_NODE_NAME);
                release_branch = (String) ((Map) result.get(Dict.RELEASE_APPLICATION)).get(Dict.RELEASE_BRANCH);
                product_tag = (List) ((Map) result.get(Dict.RELEASE_APPLICATION)).get(Dict.PRODUCT_TAG);
                pro_image_uri = (List) ((Map) result.get(Dict.RELEASE_APPLICATION)).get(Dict.PRO_IMAGE_URI);
            }
        }
        List<Map> tclist = queryMainTask(id);
        List<Map> tcollections = new ArrayList<Map>();
        String job_id = "";
        Map tc = new HashMap();
        if (tclist.size() == 0) {
            logger.info("该任务没有关联的主任务");
        }
        if (tclist.size() > 1) {
            logger.info("该任务关联多个主任务");
            throw new FdevException(ErrorConstants.MAIN_TASK_ERROR, new String[]{});
        }
        if (tclist.size() == 1) {
            tc = tclist.get(0);
            job_id = (String) tc.get(Dict.ID);
            Map main_task = (Map) tc.get("main_task");
            tcollections = id.equals((String) main_task.get(Dict.ID)) ? (List<Map>) tc.get(Dict.TASKCOLLECTION) : tcollections;
        }
        returnMap.put(Dict.PROJECT_NAME, project_name);
        returnMap.put(Dict.FEATURE_BRANCH, feature_branch);
        returnMap.put(Dict.RELEASE_BRANCH, release_branch);
        returnMap.put(Dict.RELEASE_NODE_NAME, release_node_name);
//        returnMap.put("task_collections", tcollections);
        returnMap.put(Dict.STAGE, task.getStage());
        //工单编号
        // returnMap.put(Dict.TASK_JOB_ID, job_id);
        returnMap.put(Dict.TASK_ID, id);
        returnMap.put(Dict.PRO_IMAGE_URI, pro_image_uri);
        returnMap.put(Dict.PRODUCT_TAG, product_tag);
        for (Object key : returnMap.keySet()) {
            if (returnMap.get(key) == null) {
                returnMap.put(key, "");
            }
        }
        return JsonResultUtil.buildSuccess(returnMap);
    }


    /**
     * @param request
     * @return JsonResult
     * @throws Exception
     */
    @ApiOperation(value = "新建分支")
    @RequestMapping(value = "/newFeature", method = RequestMethod.POST)
    public JsonResult newFeature(
            @RequestBody @ApiParam(name = "分支参数", value = "例如:{\"fearute_branch\":value,\"id\":\"5cc570aea8dd6e456c26d669\"}") Map
                    request)
            throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        Map param = new HashMap();
        param.put(Dict.ID, user.getId());
        ArrayList roleList = (ArrayList) iUserApi.queryUser(param).get(Dict.ROLE_ID);
        // 厂商项目负责人 行内项目负责人 拥有权限 新增卡店管理员权限
        if (!userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            if (CommonUtils.isNullOrEmpty(roleList)
                    || !(roleList.contains(Dict.ROLE_CSII) || roleList.contains(Dict.ROLE_SPDB))) {
                return JsonResultUtil.buildError(ErrorConstants.ROLE_ERROR, "用户权限不足");
            }
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        ArrayList idslist = new ArrayList();
        idslist.add(request.get(Dict.ID));
        List<FdevTask> tasklist = fdevTaskService.queryTasksByIds(idslist);
        FdevTask task = tasklist.get(0);
        //去除分支名中可能存在的不可见字符
        String feature = ((String) request.get(Dict.FEATURE_BRANCH)).replace("\u200b","");
        Map queryTerm = new HashMap();
        String appid = "";
        if (Dict.TASK_STAGE_CREATE_APP.equals(task.getStage())) {
            appid = task.getProject_id();
            queryTerm.put(Dict.ID, appid);
        } else if (Dict.TASK_STAGE_CREATE_INFO.equals(task.getStage())) {
            Map app = (Map) request.get(Dict.APP);
            if (CommonUtils.isNullOrEmpty(app)) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - app", "应用为空"});
            }
            appid = (String) app.get(Dict.ID);
            queryTerm.put(Dict.ID, appid);
        } else {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - STAGE", "任务状态异常"});
        }
        Map rApp = iAppApi.queryAppById(queryTerm);
        String gitlab_project_id = String.valueOf(rApp.get(Dict.GITLAB_PROJECT_ID));
        if (CommonUtils.isNullOrEmpty(rApp)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - app", "应用ID不存在"});
        }
        // 查询同一应用下是否有相同分支
        FdevTask queryRepeatTaskParam = new FdevTask();
        queryRepeatTaskParam.setProject_id(rApp.get(Dict.ID).toString());
        //ios、android类型应用自动生成feature下与dev下的
        String type_id = (String) rApp.get(Dict.TYPE_ID);
        Map appType = iAppApi.queryAppTypeById(type_id);
        if (null != appType && (appType.get(Dict.NAME).equals("Android应用") || appType.get(Dict.NAME).equals("IOS应用"))) {
            queryRepeatTaskParam.setFeature_branch("feature/" + feature);
            List<FdevTask> queryRepeatTasks1 = fdevTaskService.query(queryRepeatTaskParam);
            queryRepeatTaskParam.setFeature_branch("dev/" + feature);
            List<FdevTask> queryRepeatTasks2 = fdevTaskService.query(queryRepeatTaskParam);
            if ((queryRepeatTasks1 != null && queryRepeatTasks1.size() >= 1) ||
                    (queryRepeatTasks2 != null && queryRepeatTasks2.size() >= 1)) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - feature_branch", "该应用下已有相同分支"});
            }
        } else {
            queryRepeatTaskParam.setFeature_branch(feature);
            List<FdevTask> queryRepeatTasks = fdevTaskService.query(queryRepeatTaskParam);
            if (queryRepeatTasks != null && queryRepeatTasks.size() >= 1) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - feature_branch", "该应用下已有相同分支"});
            }
        }
        // 创建分支
        if (appType != null && (appType.get(Dict.NAME).equals("Android应用") || appType.get(Dict.NAME).equals("IOS应用"))) {
            createGitLabBranch("feature/" + feature, gitToken, appid);
            createGitLabBranch("dev/" + feature, gitToken, appid);
            iSetProtectedBranches.setProtectedBranches(gitlab_project_id, "dev/" + feature, gitToken);
            task.setFeature_branch("feature/" + feature);
        } else {
            createGitLabBranch(feature, gitToken, appid);
            task.setFeature_branch(feature);
        }

        //设置设计还原审核状态初始值，需求中选择涉及前端且vue项目为待上传截图，其他任务为不涉及
        if ("Vue应用".equals(rApp.get("type_name"))) {
            task.setReview_status(Dict.WAIT_UPLOAD);
        } else {
            task.setReview_status(Dict.IRRELEVANT);
        }

        task.setStage(Dict.TASK_STAGE_DEVELOP);
        task.setProject_id(rApp.get(Dict.ID).toString());
        task.setProject_name(rApp.get(Dict.NAME_EN).toString());
        FdevTask rTask = fdevTaskService.update(task);
        if (isSendMail) {
            asyncService.sendMail(null, rTask, user.getUser_name_cn(), "");
        }
        
        //判断是否涉及内测，不涉及则删除创建工单
        String id = (String) request.get(Dict.ID);
        String projectId = task.getProject_id();
        String isTest = iAppApi.QueryTestSwitch(projectId);
        if (!CommonUtils.isNullOrEmpty(isTest)) {
        	if ("0".equalsIgnoreCase(isTest)) { 
        		Map deleteWorkOrder = new HashMap();
        		deleteWorkOrder.put(Dict.TASKNOONLY, id);
        		try {
        			Map result = iTestApi.deleteOrder(deleteWorkOrder);
                } catch (Exception e) {
                    logger.error("删除工单失败"); 
                }
        	}
		}
        
        //创建审核
        reviewRecordService.saveSqlReview(rTask);
        Map<String, Object> releaseTaskApiParam = new HashMap<>();
        releaseTaskApiParam.put(Dict.TASK_ID, rTask.getId());
        ReleaseTaskApi.updateRqrmntInfoReview(releaseTaskApiParam);
        HashSet members = fdevTaskUtils.getMembers(null, rTask);
        if (!CommonUtils.isNullOrEmpty(members))
            addMember(rTask.getProject_id(), members);// 添加git的权限
        return JsonResultUtil.buildSuccess(rTask);
    }

    private void createGitLabBranch(String feature, String token, String appId) throws Exception {
        // 创建分支
        if (null != feature && !"".equals(feature.trim())) {
            Map branchparam = new HashMap();
            branchparam.put(Dict.NAME, feature);
            branchparam.put(Dict.REF, Dict.MASTER);
            branchparam.put(Dict.TOKEN, token);
            branchparam.put(Dict.ID, appId);
            if (!CommonUtils.isNotNullOrEmpty(feature, appId)) {
                throw new FdevException(ErrorConstants.PARAM_ERROR,
                        new String[]{"TASK - id,feature_branch", "创建分支应用ID，分支名称不允许为空"});
            }
            iAppApi.createGitLabBranch(branchparam);
        }
    }

    /**
     * @param request
     * @return JsonResult
     * @throws Exception
     */
    @OperateRecord(operateDiscribe = "任务模块-修改任务主从状态")
    @ApiOperation(value = "修改任务主从状态")
    @RequestMapping(value = "/updateTaskStatus", method = RequestMethod.POST)
    public JsonResult updateTaskStatus(
            @RequestBody @ApiParam(name = "做修改的任务id", value = "例如:{\"id\":\"5cc570aea8dd6e456c26d669\",\"job_id\":\"关联主任务的job_id\"}") Map
                    request)
            throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        Map param = new HashMap();
        param.put(Dict.ID, user.getId());
        ArrayList roleList = (ArrayList) iUserApi.queryUser(param).get(Dict.ROLE_ID);
        // 厂商项目负责人 行内项目负责人 拥有权限 新增卡点管理员权限
        if (!userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            if (CommonUtils.isNullOrEmpty(roleList)
                    || !(roleList.contains(Dict.ROLE_CSII) || roleList.contains(Dict.ROLE_SPDB))) {
                return JsonResultUtil.buildError(ErrorConstants.ROLE_ERROR, "用户权限不足");
            }
        }
        ArrayList idslist = new ArrayList();
        idslist.add(request.get(Dict.ID));
        List<FdevTask> tasklist = fdevTaskService.queryTasksByIds(idslist);
        if (CommonUtils.isNullOrEmpty(tasklist)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{(String) request.get(Dict.ID)});
        }
        FdevTask task = tasklist.get(0);
        //校验该task是否处于暂缓状态
        this.fdevTaskUtils.checkIsWaitTask(task.getTaskSpectialStatus());
        String jobId = "";
        //新建关系传boolean  修改关系不传。
        boolean mtask = false;
        if (request.containsKey(Dict.TASK_MAINTASK)) {
            mtask = (Boolean) request.get(Dict.TASK_MAINTASK);
        }
        if (mtask) {// 内测主任务 发送测试接口,获得jobId
            jobId = iTestApi.getJobId(task, jobId);
            fdevTaskService.saveFdevTaskCollection(new FdevTaskCollection(jobId, task.getId(), new ArrayList()));
        } else {// 或者关联到主任务。
            jobId = (String) request.get(Dict.TASK_JOB_ID);
            iTestApi.getJobId(task, jobId);
            FdevTaskCollection fdevTaskCollection = fdevTaskService
                    .queryByJobId(new FdevTaskCollection(jobId, "", new ArrayList()));
            if (CommonUtils.isNullOrEmpty(fdevTaskCollection)) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.TASK_JOB_ID, "参数job_id异常"});
            }
            fdevTaskCollection.setTaskcollection(fdevTaskCollection.addToTaskcollection(task.getId()));
            fdevTaskService.updateTaskCollection(fdevTaskCollection);
        }
        task.setStage(Dict.TASK_STAGE_DEVELOP);
        FdevTask newTask = fdevTaskService.update(task);
        if (isSendMail) {
            asyncService.sendMail(null, newTask, user.getUser_name_cn(), "");
        }
        return JsonResultUtil.buildSuccess(newTask);

    }

    /**
     * @param task
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "查询任务")
    @RequestMapping(value = "/query", method = RequestMethod.POST)
    public JsonResult queryTask(
            @RequestBody @ApiParam(name = "任务参数", value = "例如:{\"id\":\"\"5c7e166dd3e2a1ad40a0a06b\"\"}") FdevTask
                    task)
            throws Exception {
        List<Map> result = fdevTaskService.queryWithName(task);
        return JsonResultUtil.buildSuccess(result);
    }

    @OperateRecord(operateDiscribe = "任务模块-根据id修改任务")
    @ApiOperation(value = "根据id修改任务")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public JsonResult updateTask(
            @RequestBody @ApiParam(name = "任务id参数", value = "例如:{\"id\":\"\"5c7e166dd3e2a1ad40a0a06b\"\"}") Map
                    jsonObject)
            throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        if (jsonObject.containsKey(Dict.REVIEW)) {
            Map review = (Map) jsonObject.get(Dict.REVIEW);
            String[] reviewStr = {Dict.OTHER_SYSTEM, Dict.DATA_BASE_ALTER};
            for (String key : reviewStr) {
                if (review.containsKey(key)) {
                    if (key.equals(Dict.DATA_BASE_ALTER) || key.equals(Dict.OTHER_SYSTEM)) {
                        ArrayList other_system = (ArrayList) review.get(key);
                        review.put(key, formats(other_system));
                    }
                }
            }
        }
        // 修改任务，包含task基本信息(行内、任务负责人，参与人员，任务描述，分支名称，任务关联项)，测试信息,文件补传
        FdevTask task = CommonUtils.mapToBean(jsonObject, FdevTask.class);
        if (jsonObject.containsKey(Dict.TAG)) {
            try {
                List<String> tmp = (ArrayList) jsonObject.get(Dict.TAG);
                task.setTag(CommonUtils.distinctArray(tmp.toArray(new String[tmp.size()])));
            } catch (Exception e) {
                logger.error("解析任务Tag失败");
            }
        }
        String task_id = task.getId();
        if (CommonUtils.isNullOrEmpty(task_id)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"任务ID不允许为空"});
        }
        fdevTaskService.updateTaskInfo(task);
        return JsonResultUtil.buildSuccess();
    }

    @ApiOperation(value = "根据id修改任务,其他模块调用")
    @RequestMapping(value = "/updateTaskInner", method = RequestMethod.POST)
    public JsonResult updateTaskInner(
            @RequestBody @ApiParam(name = "任务id参数", value = "例如:{\"id\":\"\"5c7e166dd3e2a1ad40a0a06b\"\"}") Map
                    jsonObject)
            throws Exception {
        FdevTask result = new FdevTask();
        FdevTask old = new FdevTask();
        // 修改任务，包含task基本信息(行内、任务负责人，参与人员，任务描述，分支名称，任务关联项)，测试信息,文件补传
        FdevTask task = CommonUtils.mapToBean(jsonObject, FdevTask.class);
        String task_id = task.getId();
        if (CommonUtils.isNullOrEmpty(task_id)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID, "任务ID不允许为空"});
        }
        old.setId(task_id);
        old = fdevTaskService.queryTaskAll(old);
        if (CommonUtils.isNullOrEmpty(old)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"根据ID查询的任务不存在!"});
        }
        //校验任务是否处于暂缓状态
        this.fdevTaskUtils.checkIsWaitTask(old.getTaskSpectialStatus());
        //  归档 任务 , 直接跳过
        if (Dict.FILE.equals(old.getStage())) {
            throw new FdevException(ErrorConstants.TASK_STAGE_ERROR, new String[]{"任务已归档，不予修改!"});
        }
        //主任务 回退阶段 同步测试平台工单状态
        if (Dict.TASK_STAGE_SIT.equals(task.getStage()) && CommonUtils.pareStage(old.getStage()) > CommonUtils.pareStage(task.getStage())
                && CommonUtils.pareStage(old.getStage()) < CommonUtils.pareStage(Dict.TASK_STAGE_PRODUCTION)) {
//            List<FdevTaskCollection> ftcList = fdevTaskService.queryByTaskId(task_id);
//            if (!CommonUtils.isNullOrEmpty(ftcList)) {
//                if (ftcList.size() == 1) {
//                    FdevTaskCollection fdevTaskCollection = ftcList.get(0);
//                    if (CommonUtils.isNullOrEmpty(fdevTaskCollection.getTaskcollection())) {
//                        Map param = new HashMap();
//                        param.put("workOrderNo", fdevTaskCollection.getId());
//                        param.put(Dict.STAGE, "0");
//                        iTestApi.updateOrder(param);
//                    }
//                } else {
//                    throw new FdevException(ErrorConstants.MAIN_TASK_ERROR);
//                }
//            }
            //任务回退merge_id清空
            task.setUat_merge_id("");
        }
        //  tester 5c41796ca3178a3eb4b52006
        List<String> realTesters = new ArrayList();
        String[] tmp = task.getTester();
        if (!CommonUtils.isNullOrEmpty(tmp)) {
            try {
                for (int i = 0; i < tmp.length; i++) {
                    Map require = new HashMap();
                    require.put(Dict.ID, tmp[i]);
                    try {
                        Map user = iUserApi.queryUser(require);
                        List roles = (List) user.get(Dict.ROLE_ID);
                        if (roles.contains(Dict.ROLE_TESTER)) {
                            realTesters.add(tmp[i]);
                        }
                    } catch (FdevException e) {
                        logger.error("检测测试人员失败:code--" + e.getCode() + "msg--" + e.getMessage());
                        if (ErrorConstants.DATA_NOT_EXIST.equals(e.getMessage()))
                            continue;
                    }
                }
            } catch (Exception e) {
                logger.error("检测测试人员失败" + e.getMessage());
            } finally {
                task.setTester(realTesters.toArray(new String[realTesters.size()]));
            }
        }
        if (jsonObject.containsKey(Dict.TAG)) {
            try {
                List<String> tmpTag = (ArrayList) jsonObject.get(Dict.TAG);
                task.setTag(CommonUtils.distinctArray(tmpTag.toArray(new String[tmpTag.size()])));
            } catch (Exception e) {
                logger.error("解析任务Tag失败");
            }
        }
        //提交发布合并代码到master后，不再更改任务状态为rel，只记录合并时间
        if(Dict.TASK_STAGE_REL.equals(task.getStage()) && !Dict.TASK_STAGE_REL.equals(old.getStage())) {
            task.setStage(old.getStage());
            task.setStart_rel_test_time(old.getStart_rel_test_time());
            task.setRel_merge_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1));
        }
        //取消投产后，不再更改任务状态为sit，也不清空uat时间，只清空uat测试对象(确认挂载窗口时选择的数据)
        if(Dict.TASK_STAGE_SIT.equals(task.getStage())) {
            task.setStage(old.getStage());
            task.setStart_uat_test_time(old.getStart_uat_test_time());
            task.setStart_rel_test_time(old.getStart_rel_test_time());
            task.setStop_uat_test_time(old.getStart_rel_test_time());
            //取消窗口，清空合并release记录
            task.setUat_merge_id("");
            task.setUat_merge_time("");
        }
        result = fdevTaskService.update(task);
        HashSet members = fdevTaskUtils.getMembers(old, result);
        if (!Dict.TASK_STAGE_CREATE_INFO.equals(result.getStage()) && !CommonUtils.isNullOrEmpty(members)) {
            addMember(result.getProject_id(), members);
        }
        //判断状态是否为已投产 判断是否 请求流程工具接口
        if(Dict.TASK_STAGE_PRODUCTION.equals(result.getStage())){
            Set<String> code_order_no = result.getCode_order_no();
            if( !CommonUtils.isNullOrEmpty(code_order_no) ){
                for (String no : code_order_no) {
                    Map<String, Object> map = fdevTaskService.queryTaskByCodeOrderNo(no);
                    if( !CommonUtils.isNullOrEmpty(map) ){
                        //流程工具接口
                        map.put(Dict.ORDERNO,no);
                        serviceApi.updateByTask(map);
                    }
                }
            }
        }

        //组装邮件信息
        //判断任务阶段是否变化
        if (isSendMail) {
            if (Dict.TASK_STAGE_SIT.equals(task.getStage()) && CommonUtils.pareStage(old.getStage()) > CommonUtils.pareStage(task.getStage())
                    && CommonUtils.pareStage(old.getStage()) < CommonUtils.pareStage(Dict.TASK_STAGE_PRODUCTION)) {
                asyncService.sendMail(old, result, "三方调用", "任务取消投产");
            } else {
                asyncService.sendMail(old, result, "三方调用", "");
            }
        }
        return JsonResultUtil.buildSuccess(result);
    }


    //更新为废弃任务(第三方调用)
    @RequestMapping(value = "/updateTaskToDiscardInner", method = RequestMethod.POST)
    public JsonResult updateTaskToDiscardInner(@RequestBody Map jsonObject) throws Exception {
        FdevTask old = new FdevTask();
        FdevTask result = new FdevTask();
        FdevTask task = CommonUtils.mapToBean(jsonObject, FdevTask.class);
        String task_id = task.getId();
        if (CommonUtils.isNullOrEmpty(task_id)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID, "任务ID不允许为空"});
        }
        old.setId(task_id);
        old = fdevTaskService.queryTaskAll(old);
        if (CommonUtils.isNullOrEmpty(old)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"根据ID查询的任务不存在!"});
        }
        if (Dict.FILE.equals(old.getStage())) {
            throw new FdevException(ErrorConstants.TASK_STAGE_ERROR, new String[]{"任务已归档，不予修改!"});
        }

        Map map = ReleaseTaskApi.queryDetailByTaskId(old.getId(), CommonUtils.getReleaseType(old.getApplicationType()));
        //  查询任务是否有投产窗口
        if (CommonUtils.isNullOrEmpty(map)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"根据ID查询任务的投产窗口不存在，不能进行任务更新为废弃状态"});
        }
        result.setId(old.getId());
        result.setStage(Dict.TASK_STAGE_DISCARD);
        String discard_reason = "废弃原因：" + jsonObject.get("discard_reason");
        result.setDesc(CommonUtils.isNullOrEmpty(old.getDesc()) ? discard_reason : old.getDesc() + "\n" + discard_reason);
        FdevTask update = fdevTaskService.update(result);
        //删除任务之前有的审核历史记录
        reviewRecordService.deleteByTask_Id(task_id);
        //删除任务之前有的审核项
        reviewRecordService.deleteByTaskId(task_id);
        //删除任务的代码合并请求
        if(releaseApproveDao.getCountByTaskId(result.getId()) > 0){
            releaseApproveDao.deleteByTaskId(result.getId());
        }
        //组装邮件信息
        if (isSendMail) {
            asyncService.sendMail(old, update, "三方调用", "");
        }
        return JsonResultUtil.buildSuccess(update);
    }

    @ApiOperation(value = "研发单元编号，任务名称，所属应用，模糊匹配")
    @RequestMapping(value = "/queryByVague", method = RequestMethod.POST)
    public JsonResult queryTaskByTerm(
            @RequestBody @ApiParam(name = "任务名称参数", value = "例如:{\"term\":\"String\"}") Map requestParam)
            throws Exception {
        String taskName = (String) requestParam.get(Dict.TERM);
        String stage = (String) requestParam.get(Dict.STAGE);
        if (CommonUtils.isNullOrEmpty(taskName)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID, "任务名不能为空！"});
        }
        List<Map> result = fdevTaskService.queryTaskByTerm(taskName, stage);
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "匹配小组，阶段，状态")
    @RequestMapping(value = "/queryByTerms", method = RequestMethod.POST)
    public JsonResult queryByTerms(@RequestBody @ApiParam(name = "参数", value = " {\r\n"
            + " \"group\":[\"5c87a3bc550d6900066b1901\",\"5c87a3bc550d6900066b190f\"],\r\n"
            + " \"stage\":[\"sit\",\"rel\"],\r\n" + " }") Map requestParam) throws Exception {
        ArrayList group = (ArrayList) requestParam.get(Dict.GROUP);
        ArrayList stage = (ArrayList) requestParam.get(Dict.STAGE);
        Integer status = (Integer) requestParam.get("status");
        boolean isIncludeChildren = true;
        if (requestParam.containsKey("isIncludeChildren")) {
            isIncludeChildren = (boolean) requestParam.get("isIncludeChildren");
        }
        if (null == group || null == stage) {// 不为null 大小可以为0
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.GROUP + "," + Dict.STAGE, "小组和阶段不能为空！"});
        }
        int page = CommonUtils.isNullOrEmpty(requestParam.get("page")) ? 0 : (int)requestParam.get("page");
        int perPage = CommonUtils.isNullOrEmpty(requestParam.get("per_page")) ? 0 : (int) requestParam.get("per_page");
        return JsonResultUtil.buildSuccess(fdevTaskService.queryByTermsAndPage(group, stage, isIncludeChildren, status, page, perPage));
    }

    @ApiOperation(value = "获取当前人员参与的任务列表")
    @RequestMapping(value = "/queryUserTask", method = RequestMethod.POST)
    public JsonResult queryUserTask(
            @RequestBody @ApiParam(name = "参数", value = "示例{ 'id' : '123'  }") Map requestParam)
            throws Exception {
        String userId = (String) requestParam.get(Dict.ID);// 库里现在存的是用户ID
        String nameKey = (String) requestParam.get(Dict.NAMEKEY);// 任务名称模糊搜索
        Integer pageNum = (Integer) requestParam.get(Dict.PAGE_NUM);// 页码
        Integer pageSize = (Integer) requestParam.get(Dict.PAGE_SIZE);// 每页行数
        Boolean incloudFile = (Boolean) requestParam.getOrDefault(Dict.INCLOUDFILE, false);// 是否包含归档任务
        HashSet<String> delayStage = CommonUtils.isNullOrEmpty(requestParam.get("delayStage")) ? new HashSet<>() : new HashSet<>((List<String>) requestParam.get("delayStage"));//延期选项
        Map<String, Object> map = fdevTaskService.queryUserTask(userId, nameKey, pageNum, pageSize, incloudFile, delayStage);
        return JsonResultUtil.buildSuccess(map);
    }

    @ApiOperation(value = "根据任务id集合查询任务列表")
    @RequestMapping(value = "/queryTasksByIds", method = RequestMethod.POST)
    public JsonResult queryTasksByIds(
            @RequestBody @ApiParam(name = "参数", value = "示例{ 'ids' : ['1','2']  }") Map requestParam) throws
            Exception {
        ArrayList ids = (ArrayList) requestParam.get(Dict.IDS);
        if (CommonUtils.isNullOrEmpty(ids)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.IDS, "IDS不允许为空!"});
        }
        List<FdevTask> fdevTasks = fdevTaskService.queryTasksByIdsNoAbort(ids);
        List<Map> result = new ArrayList<Map>();
        if (!CommonUtils.isNullOrEmpty(fdevTasks)) {
            for (int i = 0; i < fdevTasks.size(); i++) {
                Map temp = CommonUtils.beanToMap(fdevTasks.get(i));
                temp.putAll(fdevTaskService.reBuildUser(fdevTasks.get(i)));
                result.add(temp);
            }
        }
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "根据任务id集合查询简单任务列表")
    @RequestMapping(value = "/queryTaskSimpleByIds", method = RequestMethod.POST)
        public JsonResult queryTaskSimpleByIds(@RequestBody Map<String, List<String>> param) throws Exception {
        return JsonResultUtil.buildSuccess(fdevTaskService.queryTaskSimpleByIds(param.get(Dict.IDS)));
    }


    @ApiOperation(value = "根据任务id集合查询任务列表,不会过滤任务状态")
    @RequestMapping(value = "/queryAllTasksByIds", method = RequestMethod.POST)
    public JsonResult queryAllTasksByIds(
            @RequestBody @ApiParam(name = "参数", value = "示例{ 'ids' : ['1','2']  }") Map requestParam) throws
            Exception {
        ArrayList ids = (ArrayList) requestParam.get(Dict.IDS);
        if (CommonUtils.isNullOrEmpty(ids)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.IDS, "IDS不允许为空!"});
        }
        List<FdevTask> fdevTasks = fdevTaskService.queryAllTasksByIds(ids);
        List<Map> result = new ArrayList<Map>();
        if (!CommonUtils.isNullOrEmpty(fdevTasks)) {
            for (int i = 0; i < fdevTasks.size(); i++) {
                Map temp = CommonUtils.beanToMap(fdevTasks.get(i));
                temp.putAll(fdevTaskService.reBuildUser(fdevTasks.get(i)));
                result.add(temp);
            }
        }
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = " 根据任务id查询任务关联项审核最终结果")
    @RequestMapping(value = "/queryTaskReview", method = RequestMethod.POST)
    public JsonResult queryTaskReview(
            @RequestBody @ApiParam(name = "参数", value = "示例{ 'ids' :[ '1','2']  }") Map requestParam) throws
            Exception {
        ArrayList ids = (ArrayList) requestParam.get(Dict.IDS);
        if (CommonUtils.isNullOrEmpty(ids)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID, "任务id不能为空!"});
        }
        List<FdevTask> tasks = fdevTaskService.queryTasksByIds(ids);
        List<Object> toDoTasks = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        boolean allPass = true;
        for (FdevTask task : tasks) {
            TaskReview view = task.getReview();
            Map<String, Object> rMap = new HashMap<>();
            rMap.put(Dict.TASK_ID, task.getId());
            rMap.put(Dict.DATA_BASE_ALTER, view.getData_base_alter());
            if (!view.allChecked()) {
                allPass = false;
                rMap.put(Dict.AUDITRESULT, false);
            } else {
                rMap.put(Dict.AUDITRESULT, true);
            }
            toDoTasks.add(rMap);
        }
        if (allPass) {
            result.put(Dict.ALLAUDITRESULT, true);
        } else {
            result.put(Dict.ALLAUDITRESULT, false);
        }
        result.put(Dict.TASKLIST, toDoTasks);
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = " 根据任务关联项id修改关联项审核结果")
    @RequestMapping(value = "/updateTaskReview", method = RequestMethod.POST)
    public JsonResult updateTaskReview(@RequestBody @ApiParam(name = "参数", value = "") Map requestParam)
            throws Exception {
        // 任务任务关联项 ID
        String viewId = requestParam.get(Dict.ID).toString();
        Boolean audit = (Boolean) requestParam.get(Dict.AUDIT);
        String scope = (String) requestParam.get(Dict.SCOPE);
        if (!CommonUtils.isNotNullOrEmpty(viewId, scope)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID + "," + Dict.SCOPE, "任务id和任务关联项的名称不能为空!"});
        }
        if (null == audit) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.AUDIT, "审核结果不能为空!"});
        }
        // String name = requestParam.getString(Dict.NAME) ;
        FdevTask result = fdevTaskService.updateTaskView(scope, viewId, null, audit);
        return JsonResultUtil.buildSuccess(result);
    }

    @OperateRecord(operateDiscribe = "任务模块-提交UAT测试")
    @ApiOperation(value = "提交UAT测试")
    @RequestMapping(value = "/putUatTest", method = RequestMethod.POST)
    public JsonResult putUatTest(@RequestBody @ApiParam(name = "参数", value = "") Map requestParam) throws
            Exception {
        // { \"id\":\"5c87135cde2ea00007171e10\" , \"feature\":\"feature1\" ,
        // \"relase\":\"relase1\" ,\"time\":\"2019-03-12\"
        // ,\"token\":\"qNMxg-bnRsBievnGFM1h\"}
        // 用户登录，获取token
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败!"});
        }
        String token = user.getGit_token();
        if (CommonUtils.isNullOrEmpty(token)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.GIT_TOKEN, "用户git_token不能为空!"});
        }
        // 判断任务参数，任务ID，应用ID，任务DE分支，对应应用的分支
        if (!requestParam.containsKey(Dict.ID) || CommonUtils.isNullOrEmpty(requestParam.get(Dict.ID).toString())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "任务ID不允许为空"});
        }
        String task_id = requestParam.get(Dict.ID).toString();
        FdevTask queryTask = new FdevTask();
        queryTask.setId(task_id);
        List<FdevTask> list = fdevTaskService.query(queryTask);
        if (CommonUtils.isNullOrEmpty(list)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK - 根据ID查询任务为空"});
        }
        queryTask = list.get(0);
        //校验任务是否处于暂缓状态
        this.fdevTaskUtils.checkIsWaitTask(queryTask.getTaskSpectialStatus());
        if (CommonUtils.isNullOrEmpty(queryTask.getFeature_branch())) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK - 该任务Feature分支为空"});
        }
        String project_id = queryTask.getProject_id();
        String feature = queryTask.getFeature_branch();
        if (!CommonUtils.isNotNullOrEmpty(feature, project_id)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK - 该任务Feature分支,应用ID为空"});
        }
        // 查询 uat 分支
        Map appParam = new HashMap();
        Map releaseInfo = ReleaseTaskApi.queryDetailByTaskId(task_id, CommonUtils.getReleaseType(queryTask.getApplicationType()));
        Map releaseApp = (Map) releaseInfo.get(Dict.RELEASE_APPLICATION);
        appParam.put(Dict.ID, releaseApp.get(Dict.APPLICATION_ID));
        if (!releaseApp.containsKey(Dict.RELEASE_BRANCH)
                || CommonUtils.isNullOrEmpty((String) releaseApp.get(Dict.RELEASE_BRANCH))) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK->RELEASE 查询该任务UAT分支返回为空"});
        }
        String[] developer = queryTask.getDeveloper();
        String[] tester = queryTask.getTester();
        String[] spdb_master = queryTask.getSpdb_master();
        String[] master = queryTask.getMaster();
        String sum[] = CommonUtils.concatAll(developer, tester, spdb_master, master);
        //增加卡点权限判断
        if (!Arrays.asList(sum).contains(user.getId()) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"你不是该任务下的参与人员 无法操作!"});
        }
        /*需求信息*/
        String unitNo = queryTask.getFdev_implement_unit_no();
        Map map = new HashMap();
        if (CommonUtils.isNotNullOrEmpty(unitNo)) {
            map = (Map) (demandService.queryByFdevNo("",unitNo)).get(Dict.IMPLEMENT_UNIT_INFO);
        }
        //需求UI开关
        Boolean ui_verify = Optional.ofNullable((Boolean) map.get("ui_verify")).orElse(false);
        String reviewStatus = queryTask.getReview_status();
        if (null != reviewStatus && ui_verify && !Dict.IRRELEVANT.equals(reviewStatus)
                && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            if (!Dict.FINISHED.equals(reviewStatus)) {
                throw new FdevException(ErrorConstants.REVIEW_STATUS_ERROR, new String[]{"此任务涉及还原审核，请执行设计还原审核流程！"});
            } else if (Dict.NOPASS.equals(reviewStatus)) {
                throw new FdevException(ErrorConstants.REVIEW_STATUS_ERROR, new String[]{"此任务设计还原审核未通过，请执行设计还原审核流程！"});
            }
        }

        //判断是否初次提交uat合并且为武汉研发A1-A6的任务，若是，则直接创建合并请求，否则走审批
        String group = queryTask.getGroup();
        Map threeLevelGroup = userUtils.getThreeLevelGroup(group);
        if(releaseMergeTeam.contains(threeLevelGroup.get(Dict.ID))){
            if(releaseApproveDao.getCountByTaskId(queryTask.getId()) > 0){
                throw new FdevException("不是初次提交release合并，须走审核流程！");//卡点
            }
        }
        //增加一次代码合并申请记录
        ReleaseApprove releaseApprove = new ReleaseApprove();
        releaseApprove.setTask_id(task_id);
        releaseApprove.setApplicant(user.getId());//申请人
        releaseApprove.setTarget_branch((String) releaseApp.get(Dict.RELEASE_BRANCH));//目标分支
        releaseApprove.setSource_branch(queryTask.getFeature_branch());//源分支
        releaseApprove.setAuditor(user.getId());//首次合并，审核人为自己
        releaseApprove.setAudit_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN));//首次合并，审核时间=申请日期
        releaseApprove.setStatus(1);//首次合并，直接通过
        releaseApprove.setEnv( "uat" );//环境
        releaseApproveDao.save(releaseApprove);
        Map redisFuser = userVerifyUtil.getRedisFuser();
        return JsonResultUtil.buildSuccess(fdevTaskService.createReleaseMergeRequest(queryTask,redisFuser,releaseApp));//直接发起合并
    }

    @OperateRecord(operateDiscribe = "任务模块-提交SIT测试")
    @ApiOperation(value = "提交SIT测试")
    @RequestMapping(value = "/putSitTest", method = RequestMethod.POST)
    public JsonResult putSitTest(
            @RequestBody @ApiParam(name = "参数", value = "示例{ \"id\":\"5c87135cde2ea00007171e10\" }") Map
                    requestMerge)
            throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        String token = user.getGit_token();// "qNMxg-bnRsBievnGFM1h"
        if (CommonUtils.isNullOrEmpty(token)) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户git_token不能为空!"});
        }
        String taskId = requestMerge.get(Dict.ID).toString();// 任务ID
        if (CommonUtils.isNullOrEmpty(taskId)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "任务ID不允许为空！"});
        }
        FdevTask taskParam = null;
        List<FdevTask> list = fdevTaskService.query(new FdevTask.FdevTaskBuilder().id(taskId).init());
        if (CommonUtils.isNullOrEmpty(list)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"根据任务ID查询任务返回为空"});
        }
        taskParam = list.get(0);
        //校验该task是否处于暂缓状态
        this.fdevTaskUtils.checkIsWaitTask(taskParam.getTaskSpectialStatus());
        String id = taskParam.getProject_id();
        // 根据应用ID查询，应用的sit分支信息
        if (CommonUtils.isNullOrEmpty(taskParam.getFeature_branch())) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"该任务无Feature分支无法提交测试"});
        }
        String[] developer = taskParam.getDeveloper();
        String[] tester = taskParam.getTester();
        String[] spdb_master = taskParam.getSpdb_master();
        String[] master = taskParam.getMaster();
        String sum[] = CommonUtils.concatAll(developer, tester, spdb_master, master);
        //增加卡点权限控制
        if (!Arrays.asList(sum).contains(user.getId()) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"你不是该任务下的参与人员 无法操作"});
        }
        //vue项目任务进行设计还原审核合并sit卡点
        /*需求信息*/
        String fdev_implement_unit_no = taskParam.getFdev_implement_unit_no();
        Map map = demandService.queryByFdevNo(taskParam.getRqrmnt_no(), fdev_implement_unit_no);
        Map implement_unit_info = (Map) map.get(Dict.IMPLEMENT_UNIT_INFO);
        //需求UI开关
        Boolean ui_verify = false;
        //研发单元信息不为空时取ui开关
        if (implement_unit_info!= null){
            ui_verify = Optional.ofNullable((Boolean) implement_unit_info.get("ui_verify")).orElse(false);
        }
        String reviewStatus = taskParam.getReview_status();
        if (null != reviewStatus && ui_verify && !Dict.IRRELEVANT.equals(reviewStatus)
                && (Dict.WAIT_UPLOAD.equals(reviewStatus) || Dict.UPLOADED.equals(reviewStatus))
                && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            throw new FdevException(ErrorConstants.REVIEW_STATUS_ERROR, new String[]{"此任务涉及UI审核，合并分支前，请先上传UI审核稿"});
        }
        //发起合并请求
        String applicationType = taskParam.getApplicationType();
        String featureBranch = taskParam.getFeature_branch();
        String targetBranch = Constants.BRANCH_SIT;
        if(CommonUtils.isApp(applicationType)) {
            Map param = new HashMap();
            param.put(Dict.ID, id);
            List<String> sitList = iAppApi.querySitBranch(param);
            if(!CommonUtils.isNullOrEmpty(sitList)) {
                targetBranch = sitList.get(0);
            }
        }
        if (Constants.APPLICATION_ANDROID.equals(applicationType)
                || Constants.APPLICATION_IOS.equals(applicationType)) {
            if(Constants.BRANCH_SIT.equals(targetBranch)) {
                targetBranch = featureBranch.replaceFirst("feature", "dev");
            }
        }
        String mergeRequestId = fdevTaskService.createMergeRequest(taskParam.getApplicationType(), taskParam.getName()+ "--SIT环境分支合并",
                taskParam.getProject_id(), (String) requestMerge.get(Dict.DESCRIPTION), featureBranch, targetBranch, taskParam.getVersionNum(), null);

        FdevTask task = new FdevTask.FdevTaskBuilder().id((String) requestMerge.get(Dict.ID)).sit_merge_id(mergeRequestId).init();
        FdevTask uptask = fdevTaskService.update(task);

        asyncService.addTodoList(id, uptask.getName() + "--SIT环境分支合并", user.getId(), uptask.getApplicationType(), "sitMergeRequest", mergeRequestId, taskId);
        return JsonResultUtil.buildSuccess(uptask);
    }

    /*@ApiOperation(value = "根据研发单元编号查询信息")
    @RequestMapping(value = "/queryRedmineInfoByRedmineId", method = RequestMethod.POST)
    public JsonResult queryRedmineInfoByRedmineId(
            @RequestBody @ApiParam(name = "参数", value = "示例{ 'redmine_id' : '研发单元2017运营224-022'  }") Map
                    requestParam)
            throws Exception {
        {
            ArrayList jsonArray = new ArrayList();
            String id = (String) requestParam.get(Dict.REDMINE_ID);
            if (CommonUtils.isNullOrEmpty(id)) {
                return JsonResultUtil.buildSuccess(jsonArray);
            }
            // requriement_no=研发单元2017运营224-022
            Map result = redmineService.queryRedmineInfoByRedmineId(requestParam);
            if (null == result) {
                return JsonResultUtil.buildSuccess(jsonArray);
            }
            jsonArray.add(result);
//            logger.info("根据研发单元编号查询信息  返回 ： " + jsonArray);
            return JsonResultUtil.buildSuccess(jsonArray);
        }
    }*/

    @ApiOperation(value = "查询任务明细")
    // @LazyInitProperty(redisKeyExpression = "nickname.{paramMap.MasterId}")
    @RequestMapping(value = "/queryTaskDetail", method = RequestMethod.POST)
    public JsonResult queryTaskDetail(@RequestBody @ApiParam FdevTask requestParam) throws Exception {
        String id = requestParam.getId();
        if (CommonUtils.isNullOrEmpty(id)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID, "任务ID不允许为空!"});
        }
        Map result = fdevTaskService.queryTaskDetail(requestParam);

        if (CommonUtils.isNullOrEmpty(result)) {
            return null;
        }
        if (CommonUtils.isNullOrEmpty(result.get("system_remould"))) {
            result.put("system_remould", "否");
        }
        if (CommonUtils.isNullOrEmpty(result.get("impl_data"))) {
            result.put("impl_data", "否");
        }
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "查询测试任务明细")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @RequestMapping(value = "/queryTestDetail", method = RequestMethod.POST)
    public JsonResult queryTestDetail(@RequestBody @ApiParam Map request) throws Exception {
        Map result = fdevTaskService.queryTestDetail(new FdevTask.FdevTaskBuilder().id((String) request.get(Dict.ID)).init(),  (Integer) request.get(Dict.PAGE_NUM ), (Integer) request.get(Dict.PAGE_SIZE));
        if (CommonUtils.isNullOrEmpty(result)) {
            return JsonResultUtil.buildSuccess(null);
        }
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "查询测试任务明细")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @RequestMapping(value = "/queryDocDetail", method = RequestMethod.POST)
    public JsonResult queryDocDetail(@RequestBody @ApiParam Map request) throws Exception {
        Map result = fdevTaskService.queryDocDetail(new FdevTask.FdevTaskBuilder().id((String) request.get(Dict.ID)).init(),
                (Integer) request.get(Dict.PAGE_NUM),(Integer)request.get(Dict.PAGE_SIZE));
        if (CommonUtils.isNullOrEmpty(result)) {
            return JsonResultUtil.buildSuccess(null);
        }
        return JsonResultUtil.buildSuccess( result );
    }

    @ApiOperation(value = "查询任务明细")
    @RequestMapping(value = "/queryTaskDetailByIds", method = RequestMethod.POST)
    public JsonResult queryTaskDetailByIds(@RequestBody @ApiParam Map requestParam) throws Exception {
        ArrayList<String> ids = (ArrayList<String>) requestParam.get(Dict.IDS);
        if (CommonUtils.isNullOrEmpty(ids)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.IDS, "IDS不能为空！"});
        }
        Map<String, Map> map = new HashMap<>();
        for (String id : ids) {
            FdevTask task = new FdevTask();
            task.setId(id);
            try {
                Map result = fdevTaskService.queryTaskDetailForProductions(task);
                if (result == null) {
                    continue;
                }
                map.put(task.getId(), result);
            } catch (Exception e) {
                logger.error("query task detail with error:", e);
            }
        }
        return JsonResultUtil.buildSuccess(map);
    }

    @OperateRecord(operateDiscribe = "任务模块-通知测试平台")
    @ApiOperation(value = "通知测试平台")
    @RequestMapping(value = "/interactTest", method = RequestMethod.POST)
    public JsonResult interactTest(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        // 任务ID
        String taskId = request.get(Dict.ID).toString();
        if (CommonUtils.isNullOrEmpty(taskId)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK-id", "任务ID不允许为空！"});
        }

        // 查询任务
        FdevTask task = null;
        List<FdevTask> list = fdevTaskService.query(new FdevTask.FdevTaskBuilder().id(taskId).init());
        if (CommonUtils.isNullOrEmpty(list)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"根据任务ID查询任务返回为空"});
        }
        task = list.get(0);
        // 查询应用的sit分支信息
        if (CommonUtils.isNullOrEmpty(task.getFeature_branch())) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"该任务无Feature分支无法提交测试"});
        }
        //查询任务是否有开发人员
        if (task.getDeveloper().length < 1) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"该任务无开发人员无法提交测试"});
        }

        // 按钮点击人员权限为行内或任务负责人
        String[] spdb_master = task.getSpdb_master();
        String[] master = task.getMaster();
        String[] developer = task.getDeveloper();
        List devsName_en = new ArrayList();
        for (String s : developer) {
            Map map = new HashMap();
            map.put(Dict.ID, s);
            Map devs = iUserApi.queryUser(map);
            if (null != devs) {
                devsName_en.add(devs.get(Dict.USER_NAME_EN));
            }
        }
        String sum[] = CommonUtils.concatAll(spdb_master, master, developer);
        //增加卡点权限判断
        if (!Arrays.asList(sum).contains(user.getId()) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"你不是任务相关人员 无法操作"});
        }
        //紧急需求的开发任务在提测前创建工单，同步任务到玉衡
        String unitNo = task.getFdev_implement_unit_no();
        //查询研发单元信息
        Map<String,Object> demandAndUnitInfo = demandService.queryByFdevNo(task.getRqrmnt_no(), unitNo);
        Map<String,Object> demand_baseinfo = (Map)demandAndUnitInfo.get(Dict.DEMAND_BASEINFO);
        Map<String,Object> fdevUnitInfo = (Map)demandAndUnitInfo.get(Dict.IMPLEMENT_UNIT_INFO);
        if(!"2".equals(String.valueOf(task.getTaskType()))
                && Constants.DEMANDTYPE_BUSINESS.equals((String)demand_baseinfo.get(Dict.DEMAND_TYPE))
                && CommonUtils.isNotNullOrEmpty((String) fdevUnitInfo.get(Dict.IPMP_IMPLEMENT_UNIT_NO))
                && "0".equals(String.valueOf(demand_baseinfo.get(Dict.DEMAND_FLAG)))) {
            //如果是业务需求，且挂载了实施单元，根据实施单元id创建玉衡工单
            Map<String,String> sendMap = new HashMap<>();
            sendMap.put(Dict.IMPL_UNIT_NUM, (String) fdevUnitInfo.get(Dict.IPMP_IMPLEMENT_UNIT_NO));
            sendMap.put(Dict.REST_CODE,"queryIpmpUnitById");
            //先根据实施单元编号查询详情，获取id
            Map<String,Object> implUnitInfo = (Map<String, Object>) restTransport.submit(sendMap);
            if(!CommonUtils.isNullOrEmpty(implUnitInfo)) {
                createWorkOrder((String) implUnitInfo.get(Dict.ID),task.getId(),task.getName(),task.getDesc());
            }
        }
        // 前端提交测试原因、JIRA编号、修复描述,输入框移到查询任务明细页面，增加通知测试平台按钮
        String testReason = request.get(Dict.TEST_REASON).toString();
        // 测试人员列表
        List<String> testorsParam = new ArrayList<>();
        Map testParam = new HashMap();
        int i = 0;
        if (!CommonUtils.isNullOrEmpty(task.getTester())) {
            for (String s : task.getTester()) {
                testParam.put(Dict.ID, s);
                Map map = iUserApi.queryUser(testParam);
                if (!CommonUtils.isNullOrEmpty(map)) {
                    testorsParam.add(i, map.get(Dict.USER_NAME_EN).toString());
                }
                i++;
            }
        }
        String Desc = request.get(Dict.REPAIR_DESC).toString();
        String regressionTestScope = (String) request.get("regressionTestScope");
        String clientVersion = (String) request.get("clientVersion");
        List copyTo = (List) request.get("copyTo");
        String testEnv = (String) request.get("testEnv");
        TaskReview review = task.getReview();
        String data_base = judgeChang(review.getData_base_alter());
        //接口变动内容
        String inter =  (String) request.get("interfaceChange");
        String system = judgeChang(review.getOther_system());

        try {// 通知测试任务进入SIT
            Map testSIT = new HashMap();
            testSIT.put(Dict.JOBNO, taskId);
            //  testSIT.put(Dict.JOBID, jobId);
            testSIT.put(Dict.TASKNAME, task.getName());
            testSIT.put(Dict.TASKSTAGE, task.getStage());
            testSIT.put(Dict.TESTERS, testorsParam);
            testSIT.put(Dict.TASKREASON, testReason);
            testSIT.put(Dict.TASKDESC, Desc);
            testSIT.put(Dict.DEVELOPER, devsName_en);
            testSIT.put("appName", task.getProject_name());
            testSIT.put("regressionTestScope", regressionTestScope);
            testSIT.put("clientVersion", clientVersion);
            testSIT.put("copyTo", copyTo);
            testSIT.put("testEnv", testEnv);
            testSIT.put("interfaceChange", inter);
            testSIT.put("databaseChange", data_base);
            testSIT.put("otherSystemChange", system);
            testSIT.put("rqrNo", (String)request.get("rqrNo"));
            testSIT.put("groupId",task.getGroup());
            //通知玉衡相关人员
            iTestApi.interactTest(testSIT);
            //通知fdev相关人员
            String desc = "提交SIT测试";
            String content = task.getName() + "--" + desc;
            Set set = new HashSet();
            //发送 给行内项目负责人
            if (!CommonUtils.isNullOrEmpty(spdb_master))
                set.addAll(Arrays.asList(spdb_master));

            if (!CommonUtils.isNullOrEmpty(master))
                //发送 给任务负责人
                set.addAll(Arrays.asList(master));

            // 发送给 开发人员
            if (!CommonUtils.isNullOrEmpty(task.getDeveloper()))
                set.addAll(Arrays.asList(task.getDeveloper()));

            List userList = null;
            if (!CommonUtils.isNullOrEmpty(set)) {
                List<String> userIds = new ArrayList<String>();
                userIds.addAll(set);
                Map map = new HashMap();
                map.put("ids", userIds);
                userList = iUserApi.queryUserList(map);
            }
            if (!CommonUtils.isNullOrEmpty(userList)) {
                Set<String> target = new HashSet<>();
                if (null != copyTo) {
                    target.addAll(copyTo);
                }
                for (Object item : userList) {
                    target.add((String) ((Map) item).get(Dict.USER_NAME_EN));
                }
                iNotifyApi.sendUserNotify(content, new ArrayList<>(target), "2", "", desc);
            }
        } catch (Exception e) {
            logger.info(task.getName() + "通知测试失败：" + e);
            throw new FdevException(ErrorConstants.TEST_NOTIFY_ERROR);
        }
        String date = CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1);
        String[] tag = task.getTag();
        tag = CommonUtils.concatAll(new String[]{"已提内测"}, tag);
        tag = Arrays.stream(tag).filter(t -> (!t.equals("提测打回"))).toArray(String[]::new);
        FdevTask tmpTask = new FdevTask.FdevTaskBuilder().id(taskId).tag(CommonUtils.distinctArray(tag)).init();
        if (Dict.TASK_STAGE_DEVELOP.equals(task.getStage())) {
            tmpTask.setStage(Dict.TASK_STAGE_SIT);
            tmpTask.setStart_inner_test_time(date);
        }
        fdevTaskService.update(tmpTask);
        return JsonResultUtil.buildSuccess();
    }

    private String judgeChang(TaskReviewChild[] taskReview) {
        String result = null;
        if (taskReview.length < 1) {
            result = "否";
            return result;
        }
        List list = new ArrayList();
        Arrays.asList(taskReview).forEach(e -> list.add(e.getName()));
        result = StringUtils.collectionToDelimitedString(list, ",");
        return result;
    }

    @ApiOperation(value = "查询主任务信息")
    //只返回develop阶段的主任务
    @RequestMapping(value = "/queryMainTask", method = RequestMethod.POST)
    public JsonResult queryFdevTaskCollection(@RequestBody @ApiParam Map request) {
        ArrayList<String> ids = (ArrayList<String>) request.get("id");
        return JsonResultUtil.buildSuccess(fdevTaskService.queryFdevTaskCollection(ids));
    }


    @ApiOperation(value = "查询主任务信息")
    @RequestMapping(value = "/queryBySubTask", method = RequestMethod.POST)
    public JsonResult queryFdevTaskCollectionBySubTask(@RequestBody @ApiParam Map request) throws Exception {
        String id = (String) request.get(Dict.ID);
        if (!CommonUtils.isNotNullOrEmpty(id)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "任务ID不允许为空！"});
        }
        List<Map> newResult = new ArrayList<Map>();
        newResult = queryMainTask(id);
        return JsonResultUtil.buildSuccess(newResult);
    }

    private List<Map> queryMainTask(String id) throws Exception {
        List list = new ArrayList<>();
        list.add(id);
        FdevTaskCollection tc = new FdevTaskCollection("", "", list);
        List<Map> result = fdevTaskService.queryBySubTask(tc);
        List<Map> newResult = new ArrayList<Map>();
        List<Map> taskcollectionList = new ArrayList<Map>();
        Map newTc = new HashMap();
        if (!CommonUtils.isNullOrEmpty(result)) {
            for (Map map : result) {
                taskcollectionList.clear();
                newTc.clear();
                List taskcollection = new ArrayList();
                taskcollection = (List) map.get(Dict.TASKCOLLECTION);
                if (!CommonUtils.isNullOrEmpty(taskcollection)) {
                    for (Object task_id : taskcollection) {
                        ArrayList ids = new ArrayList();
                        ids.add(task_id);
                        Map entity = new HashMap();
                        List tmp = fdevTaskService.queryTasksByIds(ids);
                        if (tmp.size() != 0) {
                            entity.put(Dict.NAME, ((FdevTask) tmp.get(0)).getName());
                            entity.put(Dict.ID, (String) task_id);
                            taskcollectionList.add(entity);
                        }
                    }
                }
                ArrayList param = new ArrayList<>();
                param.add((String) map.get(Dict.TASK_ID));
                FdevTask task = new FdevTask.FdevTaskBuilder().id((String) map.get(Dict.TASK_ID)).init();
                if (CommonUtils.isNullOrEmpty(fdevTaskService.queryTaskAll(task)))
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"主任务不存在"});
                String name = fdevTaskService.queryTaskAll(task).getName();
                newTc.put(Dict.ID, (String) map.get(Dict.ID));
                Map para = new HashMap();
                para.put(Dict.ID, (String) map.get(Dict.TASK_ID));
                para.put(Dict.NAME, name);
                newTc.put(Dict.MAIN_TASK, para);
                newTc.put(Dict.TASKCOLLECTION, taskcollectionList);
                newResult.add((Map) ((HashMap) newTc).clone());
            }
        }
        return newResult;
    }

/*    @ApiOperation(value = "查询研发单元编号下的主任务")
    @RequestMapping(value = "/queryTaskCByUnitNo", method = RequestMethod.POST)
    public JsonResult queryTaskCWithUnitNo(@RequestBody @ApiParam Map request) throws
            Exception {
        String unitno = (String) request.get("unitno");
        if (!CommonUtils.isNotNullOrEmpty(unitno)) {
            logger.info("TASK - unitno" + "研发单元编号不允许为空！");
            return JsonResultUtil.buildSuccess(new ArrayList<>());
        }
        return JsonResultUtil.buildSuccess(fdevTaskService.queryTaskCByUnitNo(unitno));
    }

    // 任务关联项最终结果，处理
    public Map<String, Boolean> mergeTaskView
    (Map<String, Boolean> param1, Map<String, Boolean> param2) {
        if (null == param2 || param2.size() <= 0) {
            return param1;
        }
        if (null == param1 || param1.size() <= 0) {
            return param2;
        }
        Iterator<String> it = param1.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            Boolean value = param1.get(key) && param2.get(key);
            param1.replace(key, value);
        }
        return param1;
    }*/

    // rewu
    public ArrayList format(ArrayList list) {
        ArrayList array = new ArrayList();
        for (Object sys : list) {

            Map temp = new HashMap();
            temp.put(Dict.NAME, sys);
            temp.put(Dict.AUDIT, false);
            array.add(temp);
        }
        return array;
    }

    public ArrayList formats(ArrayList list) {
        ArrayList array = new ArrayList();
        for (Object sys : list) {
            Map<String, Object> map = (Map<String, Object>) sys;
            Map temp = new HashMap();
            temp.put(Dict.NAME, map.get(Dict.NAME));
            temp.put(Dict.AUDIT, false);
            array.add(temp);
        }
        return array;
    }

    /**
     * @param project_id
     * @param members
     * @throws Exception
     */
    public void addMember(String project_id, HashSet members) throws Exception {
        //查询任务对应应用的应用负责人
        Map params = new HashMap();
        params.put(Dict.ID, project_id);
        Map app = iAppApi.queryAppById(params);
        ArrayList<Map> managers = (ArrayList<Map>) app.get(Dict.SPDB_MANAGERS);
        ArrayList<Map> dev_managers = (ArrayList<Map>) app.get(Dict.DEV_MANAGERS);
        managers.addAll(dev_managers);
        for (Map userMap : managers) {
            if (members.contains(userMap.get(Dict.ID))) {
                members.remove(userMap.get(Dict.ID));
            }
        }

        try {
            if (!CommonUtils.isNullOrEmpty(members)) {
                Map developer = new HashMap();
                developer.put(Dict.ID, project_id);
                developer.put(Dict.USER_NAME_EN, new ArrayList<>(members));
                developer.put(Dict.ROLE, "30");
                developer.put(Dict.TOKEN, gitToken);
                iAppApi.addMember(developer);
            }
        } catch (Exception e) {
            throw e;
        }
    }


    @ApiOperation(value = "查询环境信息")
    @RequestMapping(value = "/queryEnvDetail", method = RequestMethod.POST)
    public JsonResult queryEvnDetail(@RequestBody @ApiParam Map requestParam) throws
            Exception {
        if (!requestParam.containsKey(Dict.ID) && !requestParam.containsKey(Dict.TYPE)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR,
                    new String[]{"TASK - id，type", "任务ID，环境类型（sit,uat）不允许为空！"});
        }
        String id = requestParam.get(Dict.ID).toString();
        String type = (String) requestParam.get(Dict.TYPE);
        if (CommonUtils.isNullOrEmpty(id) || CommonUtils.isNullOrEmpty(type)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR,
                    new String[]{"TASK - id，type", "任务ID，环境类型（sit,uat）不允许为空！"});
        }
        FdevTask task = new FdevTask();
        task.setId(id);
        List<FdevTask> list = fdevTaskService.query(task);
        if (CommonUtils.isNullOrEmpty(list)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"查询任务返回为空!"});
        }
        task = list.get(0);
        String appId = task.getProject_id();
        ArrayList result = new ArrayList();
        if (CommonUtils.isNullOrEmpty(appId)) {
            return JsonResultUtil.buildSuccess();
        }
        Map<String,Object> projectInfo = fdevTaskService.getProjectInfo(appId, task.getApplicationType());
        Integer projectId = Integer.parseInt((String) projectInfo.get(Dict.GITLAB_ID));
        switch (type) {
            case Dict.REL_LOWERCASE:
                Map rel_releaseInfo = ReleaseTaskApi.queryDetailByTaskId(id, CommonUtils.getReleaseType(task.getApplicationType()));
                if (CommonUtils.isNullOrEmpty(rel_releaseInfo)) {
                    return JsonResultUtil.buildSuccess();
                }
                String rel_env_name = (String) rel_releaseInfo.get(Dict.REL_ENV_NAME);
                Map rel_releaseApp = (Map) rel_releaseInfo.get(Dict.RELEASE_APPLICATION);
                if (!rel_releaseApp.containsKey(Dict.RELEASE_BRANCH)
                        || CommonUtils.isNullOrEmpty((String) rel_releaseApp.get(Dict.RELEASE_BRANCH))) {
//                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK->RELEASE 查询该任务rel分支返回为空"});
                    return JsonResultUtil.buildSuccess();
                }
                String name = (String) rel_releaseApp.get(Dict.RELEASE_BRANCH);
                if (CommonUtils.isNotNullOrEmpty(rel_env_name)) {
                    Map tmp = iEnvConfigApi.queryEnvByNameEn(rel_env_name, projectId);
                    result.add(iEnvConfigApi.mapFilter(tmp));
                    if (!CommonUtils.isNullOrEmpty(result)) {
                        for (Object templ : result) {
                            ((Map) templ).put(Dict.BRANCH_NAME, name);
                        }
                    } else {
                        logger.info("询该任务REL环境信息返回为空");
                    }
                }
                break;
            case Dict.UAT:
                Map releaseInfo = ReleaseTaskApi.queryDetailByTaskId(id, CommonUtils.getReleaseType(task.getApplicationType()));
                if (CommonUtils.isNullOrEmpty(releaseInfo)) {
                    return JsonResultUtil.buildSuccess();
                }
                String uat_evn_name = (String) releaseInfo.get(Dict.UAT_ENV_NAME);
                Map releaseApp = (Map) releaseInfo.get(Dict.RELEASE_APPLICATION);
                if (!releaseApp.containsKey(Dict.RELEASE_BRANCH)
                        || CommonUtils.isNullOrEmpty((String) releaseApp.get(Dict.RELEASE_BRANCH))) {
//                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK->RELEASE 查询该任务UAT分支返回为空"});
                    return JsonResultUtil.buildSuccess();
                }
                String name0 = (String) releaseApp.get(Dict.RELEASE_BRANCH);
                if (CommonUtils.isNotNullOrEmpty(uat_evn_name)) {
                    Map tmp = iEnvConfigApi.queryEnvByNameEn(uat_evn_name, projectId);
                    result.add(iEnvConfigApi.mapFilter(tmp));
                    if (!CommonUtils.isNullOrEmpty(result)) {
                        for (Object templ : result) {
                            ((Map) templ).put(Dict.BRANCH_NAME, name0);
                        }
                    } else {
                        logger.info("询该任务UAT环境信息返回为空");
                    }
                }
                break;
            case Dict.SIT:
                if(CommonUtils.isApp(task.getApplicationType())) {
                    Map param = new HashMap();
                    param.put(Dict.ID, appId);
                    List<String> brnames = iAppApi.querySitBranch(param);
                    if (null == brnames || brnames.size() <= 0) {
                        return JsonResultUtil.buildSuccess(brnames);
                    }
                    String name1 = brnames.get(0);// 目前只有一个
                    Map resultApp = iAppApi.queryAppById(param);
                    if (CommonUtils.isNullOrEmpty(resultApp)) {
                        throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"APP 数据异常"});
                    }
                    List sit = (List) resultApp.get(Dict.SIT);
                    if (CommonUtils.isNullOrEmpty(resultApp)) {
                        throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"APP SIT为空"});
                    }
                    String auto_id = (String) ((Map) sit.get(0)).get(Dict.AUTO_ID);
                    String schedule_id = (String) ((Map) sit.get(0)).get(Dict.SCHEDULE_ID);
                    Map<String, String> envNames = new HashMap<>();
                    envNames.put(Dict.AUTO_ENV_NAME.split("_")[0], auto_id);
                    envNames.put(Dict.SCHEDULE_ENV_NAME.split("_")[0], schedule_id);
                    for (String env : envNames.keySet()) {
                        if (CommonUtils.isNotNullOrEmpty(envNames.get(env))) {
                            Map envMap = iEnvConfigApi.queryEnv(envNames.get(env));
                            String sitName = (String) envMap.get(Dict.NAME_EN);
                            Map tmp = iEnvConfigApi.queryEnvByNameEn(sitName, projectId);
                            tmp.put(Dict.NAME, env);
                            result.add(iEnvConfigApi.mapFilter(tmp));
                            if (!CommonUtils.isNullOrEmpty(result)) {
                                for (Object templ : result) {
                                    ((Map) templ).put(Dict.BRANCH_NAME, name1);
                                }
                            } else {
                                logger.info("查询环境模块sit环境信息返回为空");
                            }
                        } else {
                            logger.info("询该应用模块sit环境信息返回为空");
                        }
                    }
                }
                break;
            default:
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.TYPE, "环境变量参数错误！"});
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 根据应用返回的scc_status和caas_status字段判断应用是否绑定scc部署和caas部署
     * @param requestParam
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "校验是否绑定scc部署或caas部署，提交内测时校验按钮是否可点击时使用")
    @RequestMapping(value = "/checkSccOrCaas", method = RequestMethod.POST)
    public JsonResult checkSccOrCaas(@RequestBody @ApiParam Map requestParam) throws
            Exception {
        if (!requestParam.containsKey(Dict.ID) && !requestParam.containsKey(Dict.TYPE)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR,
                    new String[]{"TASK - id，type", "任务ID，环境类型（sit,uat）不允许为空！"});
        }
        String id = requestParam.get(Dict.ID).toString();
        String type = (String) requestParam.get(Dict.TYPE);
        if (CommonUtils.isNullOrEmpty(id) || CommonUtils.isNullOrEmpty(type)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR,
                    new String[]{"TASK - id，type", "任务ID，环境类型（sit,uat）不允许为空！"});
        }
        FdevTask task = new FdevTask();
        task.setId(id);
        List<FdevTask> list = fdevTaskService.query(task);
        if (CommonUtils.isNullOrEmpty(list)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"查询任务返回为空!"});
        }
        task = list.get(0);
        //非应用类型，直接返回不涉及
        if(!CommonUtils.isApp(task.getApplicationType())) {
            return JsonResultUtil.buildSuccess(new HashMap<String, Boolean>(){{
                put("flag", false);
            }});
        }
        String appId = task.getProject_id();
        Map result = new HashMap();
        result.put("flag",true);
        if (CommonUtils.isNullOrEmpty(appId)) {
            return JsonResultUtil.buildSuccess();
        }
        switch (type) {
            case Dict.SIT:
                Map param = new HashMap();
                param.put(Dict.ID, appId);
                List<String> brnames = iAppApi.querySitBranch(param);
                if (null == brnames || brnames.size() <= 0) {
                    return JsonResultUtil.buildSuccess(brnames);
                }
                Map resultApp = iAppApi.queryAppById(param);
                if (CommonUtils.isNullOrEmpty(resultApp)) {
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"APP 数据异常"});
                }
                if (CommonUtils.isNullOrEmpty(resultApp)) {
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"APP SIT为空"});
                }
                if(!((!CommonUtils.isNullOrEmpty(resultApp.get("scc_status")) && "1".equals(resultApp.get("scc_status").toString()))
                || (!CommonUtils.isNullOrEmpty(resultApp.get("caas_status")) && "1".equals(resultApp.get("caas_status").toString())))){
                    result.put("flag",false);
                }
                break;
            default:
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.TYPE, "环境变量参数错误！"});
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 查询各组近6周的任务数量
     *
     * @param requestParam 二级组对应的id集合
     * @return 小组维度的list，map中是日期为key，数量为value
     * @throws Exception
     */
    @ApiOperation(value = "查询各组近6周的任务数量")
    @RequestMapping(value = "/queryTaskNum", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.IDS, "isIncludeChildren"})
    public JsonResult queryTaskNum(@RequestBody @ApiParam Map requestParam) throws
            Exception {
        List params = (List) requestParam.get(Dict.IDS);
        boolean includeChild = (boolean) requestParam.get("isIncludeChildren");
        if (CommonUtils.isNullOrEmpty(params)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR,
                    new String[]{Dict.ID, "二级组id不能为空！"});
        }
        Map<String, Map> result = fdevTaskService.queryTaskNum(params, includeChild);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 查询各组各阶段任务数量
     *
     * @param requestParam 组id集合
     * @return 小组维度的list，map中是阶段为key，数量为value
     * @throws Exception
     */
    @ApiOperation(value = "根据任务属性查询各阶段任务数量")
    @RequestMapping(value = "/queryTaskNumByGroup", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.IDS, "isIncludeChildren"})
    public JsonResult queryTaskNumByGroup(@RequestBody @ApiParam Map requestParam) throws
            Exception {
        List params = (List) requestParam.get(Dict.IDS);
        boolean includeChild = (boolean) requestParam.get("isIncludeChildren");
        if (CommonUtils.isNullOrEmpty(params)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR,
                    new String[]{Dict.IDS, "组id集合不能为空！"});
        }
        Map<String, Map> result = fdevTaskService.queryTaskNumByGroup(params, includeChild);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 查询各组时间段任务数量
     *
     * @param requestParam 组id集合，开始日期，截止日期
     * @return 小组维度的list，map中是阶段为key，数量为value
     * @throws Exception
     */
    @ApiOperation(value = "查询各组时间段任务数量")
    @RequestMapping(value = "/queryTaskNumByGroupDate", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.IDS, "isIncludeChildren"})
    public JsonResult queryTaskNumByGroupDate(@RequestBody @ApiParam Map requestParam) throws
            Exception {
        List params = (List) requestParam.get(Dict.IDS);
        boolean includeChild = (boolean) requestParam.get("isIncludeChildren");
        if (CommonUtils.isNullOrEmpty(params)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR,
                    new String[]{Dict.ID, "组id不能为空！"});
        }
        Map<String, Map> result = fdevTaskService.queryTaskNumByGroupDate(params, (String) requestParam.get(Dict.START_DATE), (String) requestParam.get(Dict.END_DATE), includeChild);
        return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation(value = "查询人员关联的任务数量")            //已废弃
    @RequestMapping(value = "/queryTaskNumByMember", method = RequestMethod.POST)
    public JsonResult queryTaskNumByMember(@RequestBody @ApiParam Map requestParam) throws
            Exception {
        if (CommonUtils.isNullOrEmpty(requestParam)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR,
                    new String[]{Dict.ID, "参数不能为空！"});
        }
        Map<String, Map> result = fdevTaskService.queryTaskNumByMember(requestParam);
        return JsonResultUtil.buildSuccess(result);
    }


    @ApiOperation(value = "电子看板任务展示")
    @RequestMapping(value = "/taskCardDisplay", method = RequestMethod.POST)
    public JsonResult getTaskCardInfo(@RequestBody Map requestParam) throws
            Exception {
        return JsonResultUtil.buildSuccess(fdevTaskService.queryTaskCardByMember(requestParam));
    }

    @ApiOperation(value = "查询应用关联的任务数量")
    @RequestValidate(NotEmptyFields = {Dict.IDS})
    @RequestMapping(value = "/queryTaskNumByApp", method = RequestMethod.POST)
    public JsonResult queryTaskNumByApp(@RequestBody @ApiParam Map requestParam) throws
            Exception {
        List params = (List) requestParam.get(Dict.IDS);
        if (CommonUtils.isNullOrEmpty(params)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR,
                    new String[]{Dict.ID, "应用id不能为空！"});
        }
        Map<String, Map> result = fdevTaskService.queryTaskNumByApp(params);
        return JsonResultUtil.buildSuccess(result);
    }




    @RequestMapping(value = "/exportExcel", method = RequestMethod.POST)
    public void exportExcel(@RequestBody @ApiParam Map requestParam, HttpServletResponse resp) throws Exception {
        final String[] sheets = {Dict.INALL_CN, Dict.TODO_CN, Dict.DEVELOP_CN, Dict.SIT_UPPER, Dict.UAT_UPPER, Dict.REL_UPPER, Dict.PRO_CN};
        List sheetList = new ArrayList();
        for (String sheet : sheets) {
            sheetList.add(sheet);
        }
        //收集数据
        List ids = (ArrayList) requestParam.get(Dict.IDS);
        Object includeChild = requestParam.get("isIncludeChildren");
        boolean include = false;
        if (!CommonUtils.isNullOrEmpty(includeChild)) {
            include = (boolean) includeChild;
        }
        //任务数
        Map<String, Map> result = fdevTaskService.queryTaskNumByGroup(ids, include);
        //组名
        if (CommonUtils.isNullOrEmpty(ids)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID, "组id不能为空！"});
        }
        try {
            List indexDataList = new ArrayList();
            List childDataList = new ArrayList();
            //task数目
            List taskNum = new ArrayList();
            List taskIdsNum = new ArrayList();
            for (Object id : ids) {
                Map paramTmp = new HashMap();
                paramTmp.put(Dict.ID, id);
                Map resultTmp = iUserApi.queryGroup(paramTmp);
                if (CommonUtils.isNullOrEmpty(resultTmp)) {
                    throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"小组不存在" + (String) id});
                }
                Map taskNumTmp = result.get(id);
                List keySetList = new ArrayList(taskNumTmp.keySet());
                keySetList.sort((o1, o2) -> {
                    String[] stages = {Dict.TASK_STAGE_TODO,
                            Dict.TASK_STAGE_DEVELOP,
                            Dict.TASK_STAGE_SIT,
                            Dict.TASK_STAGE_UAT,
                            Dict.TASK_STAGE_REL,
                            Dict.TASK_STAGE_PRODUCTION,
                            Dict.TASK_STAGE_TODO_TASKIDS,
                            Dict.TASK_STAGE_DEVELOP_TASKIDS,
                            Dict.TASK_STAGE_SIT_TASKIDS,
                            Dict.TASK_STAGE_UAT_TASKIDS,
                            Dict.TASK_STAGE_REL_TASKIDS,
                            Dict.TASK_STAGE_PRODUCTION_TASKIDS};
                    List listTmp = Arrays.asList(stages);
                    return listTmp.indexOf(o1) - listTmp.indexOf(o2);
                });
                for (Object key : keySetList) {
                    if (((String) key).contains(Dict.IDS)) {
                        taskIdsNum.add(taskNumTmp.get(key));
                    } else {
                        taskNum.add(taskNumTmp.get(key));
                    }
                }
                taskNum.add(0, resultTmp.get(Dict.NAME));
                taskIdsNum.add(0, resultTmp.get(Dict.NAME));
                indexDataList.add(((ArrayList) taskNum).clone());
                childDataList.add(((ArrayList) taskIdsNum).clone());
                taskNum.clear();
                taskIdsNum.clear();
            }
            //workbook
            excelUtils.createWorkbook(sheetList);
            //汇总页
            List indexTitle = new ArrayList();
            sheetList.forEach(indexTitle::add);
            indexTitle.remove(0);
            indexTitle.add(0, Dict.ORDER_CN);
            indexTitle.add(1, Dict.GROUP_CN);
            indexTitle.add(2, "任务总数");
            logger.info("name:" + (String) sheetList.get(0));
            logger.info("title:" + Arrays.toString(indexTitle.toArray()));
            logger.info("data:" + Arrays.toString(indexDataList.toArray()));
            excelUtils.fillData((String) sheetList.get(0), indexTitle, indexDataList);
            //阶段页
            List stageList = (List) ((ArrayList) sheetList).clone();
            List childTitle = new ArrayList();
            List<List> childData = new ArrayList();
            //去掉汇总
            stageList.remove(0);
            for (int i = 0; i < stageList.size(); i++) {
                //标题Map,对应中文字符串
                List childTitleTmp = new ArrayList();
                switch (i) {
                    case 0:
                    case 1:
                        childTitleTmp = (List) excelUtils.childTitleTodoAndDevelop(excelUtils.childTitle());
                        break;
                    case 2:
                        childTitleTmp = (List) excelUtils.childTitleSit(excelUtils.childTitle());
                        break;
                    case 3:
                        childTitleTmp = (List) excelUtils.childTitleUat(excelUtils.childTitle());
                        break;
                    case 4:
                        childTitleTmp = (List) excelUtils.childTitleRel(excelUtils.childTitle());
                        break;
                    case 5:
                        childTitleTmp = (List) excelUtils.childTitlePro(excelUtils.childTitle());
                        break;
                    default:
                        break;
                }
                childData = excelUtils.generatechildData(childTitleTmp, childDataList, i);
                childTitle.add(0, Dict.ORDER_CN);
                childTitle.add(1, Dict.GROUP_CN);
                childTitle.addAll(excelUtils.translateTitle(childTitleTmp));
                childTitleTmp.clear();
                logger.info("sheet name:" + (String) stageList.get(i));
                logger.info("child title:" + Arrays.toString(childTitle.toArray()));
                logger.info("child data:" + Arrays.toString(childData.toArray()));
                excelUtils.fillData((String) stageList.get(i), childTitle, childData);
                childTitle.clear();
            }

            resp.reset();
            resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("utf-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Content-Disposition", "attachment;filename=" +
                    CommonUtils.dateFormat(new Date(), "yyyyMMddHHmmss") + ".xls");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            excelUtils.write(resp);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FdevException(ErrorConstants.GENERATE_EXCEL_ERROR);
        }
    }

    @Autowired
    private SchduleService schduleService;

    @RequestMapping(value = "/cacheRedmine", method = RequestMethod.POST)
    public JsonResult cacheRedmine() throws Exception {
        schduleService.cacheRedmine();
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/updateBulkTask", method = RequestMethod.POST)
    public JsonResult updateBulkTaskForRelease(@RequestBody Map param) throws Exception {
        List ids = new ArrayList();
        ids.addAll(param.keySet());
        Map<String, FdevTask> oldMap = new HashMap<>();
        for (Object id : ids) {
            List tmp = new ArrayList();
            tmp.add(id);
            oldMap.put((String) id, fdevTaskService.queryAllTasksByIds((ArrayList) tmp).get(0));
        }
        List result = fdevTaskService.updateBulkTaskForRelease(param);
        for (Object id : ids) {
            List tmp = new ArrayList();
            tmp.add(id);
            asyncService.sendMail(oldMap.get(id), fdevTaskService.queryAllTasksByIds((ArrayList) tmp).get(0), "批量", "");
        }
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestMapping(value = "/updateOldDataForFile", method = RequestMethod.POST)
    public JsonResult updateOldData(@RequestBody Map requestParam) throws Exception {
        return JsonResultUtil.buildError("9999999", "更新存量数据失败！");
    }

    @RequestMapping(value = "/queryTaskByDemandId", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.DEMAND_ID})
    public JsonResult queryTaskByDemandId(@RequestBody Map requestParam) throws Exception {
        String demand = (String) requestParam.get(Dict.DEMAND_ID);
        return JsonResultUtil.buildSuccess(fdevTaskService.queryTaskByRqrmnt(demand));
    }

    @RequestMapping(value = "/queryPostponeTask", method = RequestMethod.POST)
    public JsonResult queryPostponeTask(@RequestBody Map requestParam) throws Exception {
        //“需求编号/需求名称”，“研发单元编号”，“所属小组”，“参与人”，“阶段”
        if (!requestParam.containsKey("history")) {
            requestParam.put("history", history);
        }
        return JsonResultUtil.buildSuccess(fdevTaskService.queryPostponeTask(requestParam));
    }


    @RequestMapping(value = "/queryTaskNumByMemberRQR", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.IDS, "priority"})
    public JsonResult queryTaskNumByMemberRQR(@RequestBody Map requestParam) throws Exception {
        List ids = (List) requestParam.get(Dict.IDS);
        List roles = (List) requestParam.get(Dict.ROLES);
        boolean priority = (boolean) requestParam.get("priority");
        return JsonResultUtil.buildSuccess(fdevTaskService.queryTaskByRqrmntRQR(ids, roles, priority));
    }

    @RequestMapping(value = "/queryTaskNumByGroupinAll", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.IDS, "priority", "isIncludeChildren"})
    public JsonResult queryTaskNumByGroupinAll(@RequestBody Map requestParam) throws Exception {
        List ids = (List) requestParam.get(Dict.IDS);
        boolean priority = (boolean) requestParam.get("priority");
        boolean includeChild = (boolean) requestParam.get("isIncludeChildren");
        return JsonResultUtil.buildSuccess(fdevTaskService.queryTaskNumByGroupinAll(ids, priority, includeChild));
    }

    @RequestMapping(value = "/queryGroupRqrmnt", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {"groupIds", "isParent"})
    public JsonResult queryGroupRqrmnt(@RequestBody Map requestParam) throws Exception {
        List ids = (List) requestParam.get("groupIds");
        String priority = "";
        if (!CommonUtils.isNullOrEmpty(requestParam.get("priority")))
            priority = (String) requestParam.get("priority");
        boolean isParent = (boolean) requestParam.get("isParent");
        return JsonResultUtil.buildSuccess(fdevTaskService.queryGroupRqrmnt(ids, priority, isParent));
    }

    //    根据应用id查询该应用下所有未上线的任务
    @RequestMapping(value = "/queryNotinlineTasksByAppId", method = RequestMethod.POST)
    public JsonResult queryNotinlineTasksByAppId(@RequestBody Map requestParam) throws Exception {
        if (CommonUtils.isNullOrEmpty(requestParam)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID, "应用id不能为空！"});
        }
        List<FdevTask> NotinlineTasks = fdevTaskService.queryNotinlineTasksByAppId(requestParam);
        List<Map> result = new ArrayList<Map>();
        if (!CommonUtils.isNullOrEmpty(NotinlineTasks)) {
            for (int i = 0; i < NotinlineTasks.size(); i++) {
                Map temp = CommonUtils.beanToMap(NotinlineTasks.get(i));
                temp.putAll(fdevTaskService.reBuildUser(NotinlineTasks.get(i)));
                result.add(temp);
            }
        }
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestMapping(value = "/bulkaddtask", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.NAME, Dict.MASTER, Dict.APP_IDS, Dict.FEATURE_BRANCH, Dict.RQRMNT_NO})
    public JsonResult bulkaddtask(@RequestBody Map requestParam) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        fdevTaskService.bulkAddTask(requestParam, user);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询人员时间段任务数量
     *
     * @param requestParam 人员id集合，开始日期，截止日期
     * @return 人员维度的list，map中是阶段为key，数量为value
     * @throws Exception
     */
    @ApiOperation(value = "查询人员时间段任务产出数量")
    @RequestMapping(value = "/queryTaskNumByUserIdsDate", method = RequestMethod.POST)
    public JsonResult queryTaskNumByUserIdsDate(@RequestBody @ApiParam Map requestParam) throws
            Exception {
        List params = (List) requestParam.get(Dict.USER_IDS);
        if (CommonUtils.isNullOrEmpty(params)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR,
                    new String[]{Dict.ID, "用户id不能为空！"});
        }
        List<String> demandTypeList = CommonUtils.isNullOrEmpty(requestParam.get("demandType")) ? new ArrayList<>() : (List<String>) requestParam.get("demandType");
        List<Integer> taskTypelIst = CommonUtils.isNullOrEmpty(requestParam.get("taskType")) ? new ArrayList<>() : (List<Integer>) requestParam.get("taskType");
        Map<String, Map> result = fdevTaskService.queryTaskNumByUserIdsDate(params, (List) requestParam.get(Dict.ROLES), (String) requestParam.get(Dict.START_DATE), (String) requestParam.get(Dict.END_DATE), demandTypeList, taskTypelIst);
        return JsonResultUtil.buildSuccess(result);
    }


    @RequestMapping(value = "/taskNameJudge", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.TASKNAME})
    public JsonResult taskNameJudge(@RequestBody Map requestParam) throws Exception {
        Map result = fdevTaskService.taskNameJudge(requestParam);
        return JsonResultUtil.buildSuccess(result);
    }

    @RequestMapping(value = "/queryByJobId", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.JOBID})
    public JsonResult queryByJobId(@RequestBody Map requestParam) throws Exception {
        FdevTaskCollection fdevTaskCollection = fdevTaskService.queryByJobId(new FdevTaskCollection((String) requestParam.get(Dict.JOBID), "", new ArrayList()));
        return JsonResultUtil.buildSuccess(fdevTaskCollection);
    }

    @RequestMapping(value = "/queryRqrmntNameAndReinforceMsg", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.MERGE_ID, Dict.GITLAB_PROJECT_ID})
    public JsonResult queryRqrmntNameAndReinforceMsg(@RequestBody Map requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.fdevTaskService.queryRqrmntNameAndReinforceMsg(requestParam));
    }


    @RequestMapping(value = "/createTestRunMerge", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult createTestRunMerge(@RequestBody Map requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(this.fdevTaskService.createTestRunMerge(requestParam));
    }

    @ApiOperation(value = "创建无代码变更")
    @RequestMapping(value = "/create/nocode/task", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.TASK_TYPE})
    public JsonResult createNoCodeTask(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        ArrayList idslist = new ArrayList();
        idslist.add(request.get(Dict.ID));
        List<FdevTask> tasklist = fdevTaskService.queryTasksByIds(idslist);
        //通过id拿到任务
        FdevTask task = tasklist.get(0);
        if (task == null) {
            return JsonResultUtil.buildError(ErrorConstants.TASK_NAME_EROOR, "非法任务！！");
        }
        //查看是否是暂缓任务
        this.fdevTaskUtils.checkIsWaitTask(task.getTaskSpectialStatus());
        List userlist = new ArrayList();
        userlist.addAll(Arrays.asList(task.getMaster()));
        userlist.addAll(Arrays.asList(task.getSpdb_master()));
        if (CommonUtils.isNotNullOrEmpty(task.getDeveloper())) {
            userlist.add(task.getDeveloper());
        }
        if (!userlist.contains(user.getId()) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"您没有权限!"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        /*Map<String, Object> releaseTaskApiParam = new HashMap<>();
        releaseTaskApiParam.put(Dict.TASK_ID,task.getId());
        ReleaseTaskApi.updateRqrmntInfoReview(releaseTaskApiParam);*/
        return JsonResultUtil.buildSuccess(this.fdevTaskService.createNoCodeTask(task, request));
    }

    @ApiOperation(value = "添加相关人员")
    @RequestMapping(value = "/add/noCode/relator", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.ID, "relatorId", "relatorName", "currentId", "currentName"})
    public JsonResult addNocodeRelator(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        Map param = new HashMap();
        param.put(Dict.ID, user.getId());
        request.put(Dict.USER_ID, user.getId());
        ArrayList roleList = (ArrayList) iUserApi.queryUser(param).get(Dict.ROLE_ID);
        // 厂商项目负责人 行内项目负责人 拥有权限 新增卡店管理员权限
        if (!userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            if (CommonUtils.isNullOrEmpty(roleList)
                    || !(roleList.contains(Dict.ROLE_CSII) || roleList.contains(Dict.ROLE_SPDB))) {
                return JsonResultUtil.buildError(ErrorConstants.ROLE_ERROR, "用户权限不足");
            }
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        ArrayList idslist = new ArrayList();
        idslist.add(request.get(Dict.ID));
        List<FdevTask> tasklist = fdevTaskService.queryTasksByIds(idslist);
        //通过id拿到任务
        FdevTask task = tasklist.get(0);
        //查看是否是暂缓任务
        this.fdevTaskUtils.checkIsWaitTask(task.getTaskSpectialStatus());
        List userlist = new ArrayList();
        userlist.addAll(Arrays.asList(task.getMaster()));
        userlist.addAll(Arrays.asList(task.getSpdb_master()));
        if (CommonUtils.isNotNullOrEmpty(task.getDeveloper())) {
            userlist.add(task.getDeveloper());
        }
        if (!userlist.contains(user.getId()) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"您没有权限!"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        String link = baseLink + task.getId() + "/manage";
        request.put(Dict.LINK, link);
        return JsonResultUtil.buildSuccess(this.fdevTaskService.addNocodeRelator(task, request));
    }

    @ApiOperation(value = "删除相关人员")
    @RequestMapping(value = "/delete/noCode/relator", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.ID, "rid"})
    public JsonResult deleteNocodeRelator(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        ArrayList idslist = new ArrayList();
        idslist.add(request.get(Dict.ID));
        List<FdevTask> tasklist = fdevTaskService.queryTasksByIds(idslist);
        //通过id拿到任务
        FdevTask task = tasklist.get(0);
        //查看是否是暂缓任务
        this.fdevTaskUtils.checkIsWaitTask(task.getTaskSpectialStatus());
        List userlist = new ArrayList();
        userlist.addAll(Arrays.asList(task.getMaster()));
        userlist.addAll(Arrays.asList(task.getSpdb_master()));
        if (CommonUtils.isNotNullOrEmpty(task.getDeveloper())) {
            userlist.add(task.getDeveloper());
        }
        if (!userlist.contains(user.getId()) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"您没有权限!"});
        }
        return JsonResultUtil.buildSuccess(this.fdevTaskService.deleteNocodeRelator(task, request));
    }


    @ApiOperation(value = "相关人员完成操作")
    @RequestMapping(value = "/finish/noCode/relator", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.ID, "rid"})
    public JsonResult finishNocodeRelator(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        //userid传给service
        request.put("userId", user.getId());
        if (!CommonUtils.isNotNullOrEmpty((String) request.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        ArrayList idslist = new ArrayList();
        idslist.add(request.get(Dict.ID));
        List<FdevTask> tasklist = fdevTaskService.queryTasksByIds(idslist);
        //通过id拿到任务
        FdevTask task = tasklist.get(0);
        //查看是否是暂缓任务
        this.fdevTaskUtils.checkIsWaitTask(task.getTaskSpectialStatus());
        String result = this.fdevTaskService.finishNocodeRelator(task, request);
        if (result == null) {
            return JsonResultUtil.buildError(ErrorConstants.DATA_NOT_EXIST, Dict.ID_NOT_EXIST);
        } else
            return JsonResultUtil.buildSuccess(result);
    }

    @ApiOperation("非代码变更相关人员确认修改哦")
    @RequestMapping(value = "/confirm/update/nocodeInfo", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.ID, "finishTime"})
    public JsonResult confirmUpdateNocodeInfo(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        ArrayList idslist = new ArrayList();
        idslist.add(request.get(Dict.ID));
        List<FdevTask> tasklist = fdevTaskService.queryTasksByIds(idslist);
        //通过id拿到任务
        FdevTask task = tasklist.get(0);
        //查看是否是暂缓任务
        this.fdevTaskUtils.checkIsWaitTask(task.getTaskSpectialStatus());
        List userlist = new ArrayList();
        userlist.addAll(Arrays.asList(task.getMaster()));
        userlist.addAll(Arrays.asList(task.getSpdb_master()));
        if (CommonUtils.isNotNullOrEmpty(task.getDeveloper())) {
            userlist.add(task.getDeveloper());
        }
        if (!userlist.contains(user.getId()) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"您没有权限!"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        return JsonResultUtil.buildSuccess(this.fdevTaskService.confirmUpdateNocodeInfo(task, request));
    }


    @ApiOperation("非代码变更相关人员文件上传")
    @RequestMapping(value = "/upload/files/rid", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.ID, "rid", "fileName", "path"})
    public JsonResult uploadFileByRid(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get("rid"))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - rid", "相关信息id不允许为空"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get("fileName"))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - fileName", "文件名称不允许为空"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get("path"))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - path", "文件路径不允许为空"});
        }
        ArrayList idslist = new ArrayList();
        idslist.add(request.get(Dict.ID));
        List<FdevTask> tasklist = fdevTaskService.queryTasksByIds(idslist);
        //通过id拿到任务
        FdevTask task = tasklist.get(0);
        //查看是否是暂缓任务
        this.fdevTaskUtils.checkIsWaitTask(task.getTaskSpectialStatus());
        //userid传给service
        request.put("userId", user.getId());
        return JsonResultUtil.buildSuccess(this.fdevTaskService.uploadFileByRid(task, request));
    }


    @ApiOperation("非代码变更相关人员文件删除")
    @RequestMapping(value = "/delete/file/rid", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.ID, "rid", "fileName"})
    public JsonResult deleteFileByRid(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get("rid"))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - rid", "相关信息id不允许为空"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get("fileName"))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - fileName", "文件名称不允许为空"});
        }
        ArrayList idslist = new ArrayList();
        idslist.add(request.get(Dict.ID));
        List<FdevTask> tasklist = fdevTaskService.queryTasksByIds(idslist);
        //通过id拿到任务
        FdevTask task = tasklist.get(0);
        //查看是否是暂缓任务
        this.fdevTaskUtils.checkIsWaitTask(task.getTaskSpectialStatus());
        //userid传给service
        request.put("userId", user.getId());
        return JsonResultUtil.buildSuccess(this.fdevTaskService.deleteFileByRid(task, request));
    }

    @ApiOperation("非代码变更下一阶段")
    @RequestMapping(value = "/nextStage", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult nocodeNextStage(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        ArrayList idslist = new ArrayList();
        idslist.add(request.get(Dict.ID));
        List<FdevTask> tasklist = fdevTaskService.queryTasksByIds(idslist);
        //通过id拿到任务
        FdevTask task = tasklist.get(0);
        //查看是否是暂缓任务
        this.fdevTaskUtils.checkIsWaitTask(task.getTaskSpectialStatus());
        List userlist = new ArrayList();
        userlist.addAll(Arrays.asList(task.getMaster()));
        userlist.addAll(Arrays.asList(task.getSpdb_master()));
        if (CommonUtils.isNotNullOrEmpty(task.getDeveloper())) {
            userlist.add(task.getDeveloper());
        }
        if (!userlist.contains(user.getId()) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"您没有权限!"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        return JsonResultUtil.buildSuccess(this.fdevTaskService.nocodeNextStage(task));
    }


    @ApiOperation("非代码变更相关人员文件查看")
    @RequestMapping(value = "/query/files/rid", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.ID, "rid"})
    public JsonResult queryFilesByRid(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get("rid"))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - rid", "相关信息id不允许为空"});
        }
        ArrayList idslist = new ArrayList();
        idslist.add(request.get(Dict.ID));
        List<FdevTask> tasklist = fdevTaskService.queryTasksByIds(idslist);
        //通过id拿到任务
        FdevTask task = tasklist.get(0);
        //userid传给service
        request.put("userId", user.getId());
        return JsonResultUtil.buildSuccess(this.fdevTaskService.queryFilesByRid(task, request));
    }

    @ApiOperation("更新上线确认书按钮开关同时记录操作信息")
    @RequestMapping(value = "/update/confirmBtn", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {"taskId", "confirmBtn"})
    public JsonResult updateConfirmBtn(@RequestBody Map params) throws Exception {
        String taskId = (String) params.get("taskId");
        String confirmBtn = (String) params.get("confirmBtn");
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        Map param = new HashMap();
        param.put(Dict.ID, user.getId());
        ArrayList roleList = (ArrayList) iUserApi.queryUser(param).get(Dict.ROLE_ID);
        // 厂商项目负责人 行内项目负责人 拥有权限 新增卡店管理员权限
        if (!userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            if (CommonUtils.isNullOrEmpty(roleList)
                    || !(roleList.contains(Dict.ROLE_CSII) || roleList.contains(Dict.ROLE_SPDB))) {
                return JsonResultUtil.buildError(ErrorConstants.ROLE_ERROR, "用户权限不足");
            }
        }
        if (!CommonUtils.isNotNullOrEmpty(taskId)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        //通过id拿到任务
        FdevTask task = fdevTaskService.queryTaskById(taskId);
        if (task.getConfirmBtn() != null && task.getConfirmBtn().equals("1")) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "不允许修改"});
        }
        if (task.getConfirmBtn() != null && task.getConfirmBtn().equals("0")) {
            String[] tag = task.getTag();
            String[] tagTemp = null;
            if (tag == null || tag.length == 0) {
                tagTemp = new String[1];
                tagTemp[0] = "确认书已到达";
            } else {
                tagTemp = new String[tag.length + 1];
                System.arraycopy(tag, 0, tagTemp, 0, tag.length);
                tagTemp[tag.length] = "确认书已到达";
            }
            task.setTag(tagTemp);
            task.setConfirmBtn(confirmBtn);
            /*
            *新增逻辑针对处对处、总队总标签，进行核对一下上线确认书实际到达时间，并重新同步标签
             */
            if(CommonUtils.isNotNullOrEmpty((String) params.get("confirmFileDate"))){
                String confirmFileDate = (String) params.get("confirmFileDate");
                task.setConfirmFileDate(confirmFileDate);
                ReleaseTaskApi.confirmRqrmntInfoTag(confirmFileDate,task.getId());
            }
            OperateRecords operateRecord = new OperateRecords();
            operateRecord.setOperateUserId(user.getId());
            operateRecord.setId(taskId);
            operateRecord.setOperateName(user.getUser_name_cn());
            operateRecordService.save(operateRecord);
        }
        return JsonResultUtil.buildSuccess(fdevTaskService.updateConfirmBtn(task));
    }

    @ApiOperation("更新提测试重点")
    @RequestMapping(value = "/update/testKeyNote", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.ID, "testKeyNote"})
    public JsonResult updateTestKeyNote(@RequestBody Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        Map param = new HashMap();
        param.put(Dict.ID, user.getId());
        ArrayList roleList = (ArrayList) iUserApi.queryUser(param).get(Dict.ROLE_ID);
        // 厂商项目负责人 行内项目负责人 拥有权限 新增卡店管理员权限
        if (!userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            if (CommonUtils.isNullOrEmpty(roleList)
                    || !(roleList.contains(Dict.ROLE_CSII) || roleList.contains(Dict.ROLE_SPDB))) {
                return JsonResultUtil.buildError(ErrorConstants.ROLE_ERROR, "用户权限不足");
            }
        }
        if (!CommonUtils.isNotNullOrEmpty((String) request.get(Dict.ID))) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "应用ID不允许为空"});
        }
        ArrayList idslist = new ArrayList();
        idslist.add((String) request.get(Dict.ID));
        List<FdevTask> tasklist = fdevTaskService.queryTasksByIds(idslist);
        //通过id拿到任务
        FdevTask task = tasklist.get(0);
        List userlist = new ArrayList();
        userlist.addAll(Arrays.asList(task.getMaster()));
        userlist.addAll(Arrays.asList(task.getSpdb_master()));
        if (CommonUtils.isNotNullOrEmpty(task.getDeveloper())) {
            userlist.add(task.getDeveloper());
        }
        if (!userlist.contains(user.getId()) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"您没有权限!"});
        }
        task.setTestKeyNote((String) request.get("testKeyNote"));
        return JsonResultUtil.buildSuccess(fdevTaskService.updateTestKeyNote(task));
    }

    @ApiOperation("获取确认书记录")
    @RequestMapping(value = "/record/getConfirmRecord", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {"id"})
    public JsonResult getConfirmRecordByTaskId(@RequestBody Map param) {
        return JsonResultUtil.buildSuccess(operateRecordService.queryConfirmRecordByTaskId((String) param.get("id")));
    }

    @ApiOperation("获取所有确认书记录")
    @RequestMapping(value = "/record/getAllConfirmRecord", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {"id"})
    public JsonResult getAllConfirmRecordByTaskIdContainHistory(@RequestBody Map param) {
        return JsonResultUtil.buildSuccess(operateRecordService.getAllConfirmRecordByTaskIdContainHistory((String) param.get("id")));
    }

    @ApiOperation("通过组id和投产意向窗口获取任务列表")
    @RequestMapping(value = "/list/task/groupId/proWantWindow", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.GROUP, Dict.PRO_WANT_WINDOW})
    public JsonResult listTaskByGroupIdAndProWantWindow(@RequestBody Map params) throws Exception {
        return JsonResultUtil.buildSuccess(fdevTaskService.listTaskByGroupIdAndProWantWindow((String) params.get(Dict.GROUP), (String) params.get(Dict.PRO_WANT_WINDOW)));
    }

    @ApiOperation(value = "查询元数据")
    @RequestMapping(value = "/queryMateDataByType", method = RequestMethod.POST)
    public JsonResult queryMateDataByType(@RequestBody @ApiParam Map requestParam) {
        return JsonResultUtil.buildSuccess(mateDataService.queryMateDataByType(requestParam));
    }

    @ApiOperation(value = "添加元数据")
    @RequestMapping(value = "/addMateDataByType", method = RequestMethod.POST)
    public JsonResult addMateDataByType(@RequestBody @ApiParam MateData mateData) {
        return JsonResultUtil.buildSuccess(mateDataService.addMateDataByType(mateData));
    }

    @ApiOperation(value = "更新元数据")
    @RequestMapping(value = "/updateMateDataByType", method = RequestMethod.POST)
    public JsonResult updateMateDataByType(@RequestBody @ApiParam MateData mateData) {
        return JsonResultUtil.buildSuccess(mateDataService.updateMateDataByType(mateData));
    }

    @ApiOperation(value = "查询是否存在需求文档")
    @RequestMapping(value = "/queryRqrDocInfo", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {"taskId"})
    public JsonResult deleteTaskDoc(@RequestBody Map params) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        String taskId = (String) params.get("taskId");
        if (userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            return JsonResultUtil.buildSuccess(new HashMap() {{
                put("result", true);
            }});
        }
        return JsonResultUtil.buildSuccess(fdevTaskService.getDocInfoForRqr(taskId));
    }

    @ApiOperation("通过时间段、审核人、和组查询设计还原审核记录")
    @RequestMapping(value = "/queryReviewList",method = RequestMethod.POST)
    public JsonResult queryReviewList(@RequestBody Map params) throws Exception {
        return JsonResultUtil.buildSuccess(fdevTaskService.queryReviewDetailList((String)params.get(Dict.REVIEWER),
                (List<String>)params.get(Dict.GROUP),(String)params.get(Dict.STARTDATE),(String)params.get(Dict.ENDDATE),
                (String)params.get(Dict.LOGICALOPERATOR),(List<Map>)params.get(Dict.SEARCHLIST),(String)params.get(Dict.INTERNETCHILDGROUPID)));
    }

    @ApiOperation("下载设计还原审核记录")
    @PostMapping("/downLoadReviewList")
    public void downLoadReviewList(@RequestBody Map params, HttpServletResponse response) throws Exception {
        fdevTaskService.downLoadReviewList(response,(String)params.get(Dict.REVIEWER),(List<String>)params.get(Dict.GROUP),
                (String)params.get(Dict.STARTDATE),(String)params.get(Dict.ENDDATE),(String)params.get(Dict.LOGICALOPERATOR),
                (List<Map>)params.get(Dict.SEARCHLIST),(Map<String,String>)params.get(Dict.COLUMNMAP),(String)params.get(Dict.INTERNETCHILDGROUPID));
    }

    @ApiOperation(value = "任务的暂缓恢复")
    @RequestMapping(value = "/defer/or/recover", method = RequestMethod.POST)
    public JsonResult deferOrRecoverTask(@RequestBody Map params) {
        List<String> ids = (List<String>) params.get(Dict.IDS);
        Integer taskSpecialStatus = (Integer) params.get(Dict.TASK_SPECIAL_STATUS);
        if (CommonUtils.isNullOrEmpty(ids) || CommonUtils.isNullOrEmpty(taskSpecialStatus)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"ids或者taskSpectialStatus不能为空"});
        }
        return JsonResultUtil.buildSuccess(fdevTaskService.deferOrRecoverTask(ids,taskSpecialStatus));
    }

    @ApiOperation(value = "通过研发单元编号查询任务")
    @RequestMapping(value = "/queryDetailByUnitNo", method = RequestMethod.POST)
    public JsonResult queryTaskDetailByfdevImplementUnitNo(@RequestBody Map params) throws Exception {
        List<String> ids = (List<String>) params.get(Dict.IDS);
        if (CommonUtils.isNullOrEmpty(ids)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"研发单元编号不能为空！"});
        }
        return JsonResultUtil.buildSuccess(fdevTaskService.queryTaskDetailByfdevImplementUnitNo(ids));
    }

    @ApiOperation(value = "通过研发单元编号集合查询非删除取消的任务")
    @RequestMapping(value = "/queryTaskByUnitNos", method = RequestMethod.POST)
    public JsonResult queryTaskByUnitNos(@RequestBody Map params) throws Exception {
        List<String> ids = (List<String>) params.get(Dict.IDS);
        if (CommonUtils.isNullOrEmpty(ids)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"研发单元编号不能为空！"});
        }
        return JsonResultUtil.buildSuccess(fdevTaskService.queryTaskByUnitNos(ids));
    }

    @ApiOperation(value = "通过研发单元编号判断是否可以暂缓或者撤销")
    @RequestMapping(value = "/judge/deferOrDelete", method = RequestMethod.POST)
    public JsonResult judgeByfdevImplementUnitNo(@RequestBody Map params) throws Exception {
        List<String> ids = (List<String>) params.get(Dict.IDS);
        String demand_id = (String) params.get("demand_id");
        return JsonResultUtil.buildSuccess(fdevTaskService.judgeByfdevImplementUnitNo(demand_id, ids));
    }

    @ApiOperation(value = "根据组获取任务数量")
    @RequestMapping(value = "/queryTaskNumByGroups", method = RequestMethod.POST)
    public JsonResult queryTaskNumByGroups(@RequestBody Map params) throws Exception {
        String group = (String) params.get(Dict.GROUP);
        Boolean isParent = (Boolean) params.get("isParent");
        List stages = (List) params.get(Dict.STAGES);
        GroupTree groupTree = null;
        if (isParent) {
            groupTree = groupTreeUtil.getGroupTree();
        }
        return JsonResultUtil.buildSuccess(fdevTaskService.queryTaskNumByGroupinAll(group, groupTree, stages));
    }


    @RequestMapping(value = "/queryTaskByreadmine", method = RequestMethod.POST)
    public JsonResult queryTaskByreadmine(@RequestBody Map params) throws Exception {
        String group = (String) params.get("redmine_id");

        return JsonResultUtil.buildSuccess(fdevTaskService.queryTaskByreadmine(group));
    }

    @RequestMapping(value = "/queryTaskIdsByProjectId", method = RequestMethod.POST)
    public JsonResult queryTaskIdsByProjectId(@RequestBody Map params) throws Exception {
        String project_id = (String) params.get("project_id");
        return JsonResultUtil.buildSuccess(fdevTaskService.queryTaskIdsByProjectId(project_id));
    }

    @RequestMapping(value = "/queryInfoById", method = RequestMethod.POST)
    public JsonResult queryInfoById(@RequestBody Map params) throws Exception {
        String task_id = (String) params.get("id");
        return JsonResultUtil.buildSuccess(fdevTaskService.queryInfoById(task_id));
    }


    @RequestMapping(value = "/queryNotDiscarddnum", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {"fdev_implement_unit_no"})
    @ApiOperation(value = "根据研发单元查询创建的任务状态为非删除废弃的数量")
    public JsonResult queryNotDiscarddnum(@RequestBody Map params) throws Exception {
        String fdev_implement_unit_no = (String) params.get("fdev_implement_unit_no");
        return JsonResultUtil.buildSuccess(fdevTaskService.queryNotDiscarddnum(fdev_implement_unit_no));
    }

    @RequestMapping(value = "/queryWaitFileTask", method = RequestMethod.POST)
    @ApiOperation(value = "查询用户可操作的已投产待归档任务列表")
    public JsonResult queryWaitFileTask() throws Exception {
        return JsonResultUtil.buildSuccess(fdevTaskService.queryWaitFileTask());
    }

    @RequestMapping(value = "/updateTaskStateToFile", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.IDS})
    @ApiOperation(value = "批量归档任务")
    public JsonResult updateTaskStateToFile(@RequestBody Map<String,List<String>> params) throws Exception {
        fdevTaskService.updateTaskStateToFile(params.get(Dict.IDS));
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/checkMountUnit", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @ApiOperation(value = "校验任务的研发单元是否已挂载实施单元")
    public JsonResult checkMountUnit(@RequestBody Map<String,String> params) throws Exception {
        return JsonResultUtil.buildSuccess(fdevTaskService.checkMountUnit(params.get(Dict.ID)));
    }

    @RequestMapping(value = "/updateTaskSpectialStatus", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.UNITNO})
    @ApiOperation(value = "更改任务状态为暂缓或取消")
    public JsonResult updateTaskSpectialStatus(@RequestBody Map<String,String> params) throws Exception {
        fdevTaskService.updateTaskSpectialStatus(params.get(Dict.UNITNO),params.get(Dict.STAGE),params.get(Dict.TASKSPECTIALSTATUS));
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/checkUatAndRelTestTime", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.TASKID})
    @ApiOperation(value = "校验用户编辑的uat测试时间是否早于意向投产窗口时间")
    public JsonResult checkUatTestTime(@RequestBody Map<String,String> params) throws Exception {
        return JsonResultUtil.buildSuccess(fdevTaskService.checkUatAndRelTestTime(params.get(Dict.TASKID)));
    }

    @RequestMapping(value = "/queryAddTaskType", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.DEMAND_ID, Dict.UNITNO})
    @ApiOperation(value = "查询当前需求下的研发单元可以新建任务的类型")
    public JsonResult queryAddTaskType(@RequestBody Map<String,String> params) {
        return JsonResultUtil.buildSuccess(fdevTaskService.queryAddTaskType(params.get(Dict.DEMAND_ID), params.get(Dict.UNITNO)));
    }


    @RequestMapping(value = "/queryGroupById", method = RequestMethod.POST)
    @ApiOperation(value = "根据小组id查询子组,默认查互联网条线下的组信息")
    public JsonResult queryGroupById(@RequestBody Map<String,String> params) throws Exception {
        return JsonResultUtil.buildSuccess(fdevTaskService.queryGroupById(params.get(Dict.ID)));
    }

    @RequestMapping(value = "/updateTaskCodeOrderNo", method = RequestMethod.POST)
    @ApiOperation(value = "修改代码审核工单编号")
    @RequestValidate(NotEmptyFields = {Dict.CODE_ORDER_NO})
    public JsonResult updateTaskCodeOrderNo(@RequestBody Map<String,Object> params) throws Exception {
        fdevTaskService.updateTaskCodeOrderNo(params);
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/queryTaskByCodeOrderNo", method = RequestMethod.POST)
    @ApiOperation(value = "根据代码审核编号查询任务")
    @RequestValidate(NotEmptyFields = {Dict.CODE_ORDER_NO})
    public JsonResult queryTaskByCodeOrderNo(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(fdevTaskService.queryTaskByCodeOrderNo((String) params.get(Dict.CODE_ORDER_NO)));
    }

    @RequestMapping(value = "/putSecurityTest", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.TASKID, Dict.TRANSFILEPATH})
    @ApiOperation(value = "提交安全测试")
    public JsonResult putSecurityTest(@RequestBody Map<String,Object> params) throws Exception {
        String taskId = (String) params.get(Dict.TASKID);
        String correlationSystem = (String) params.get(Dict.CORRELATIONSYSTEM);
        String correlationInterface = (String) params.get(Dict.CORRELATIONINTERFACE);
        String interfaceFilePath = (String) params.get(Dict.INTERFACEFILEPATH);
        String transFilePath = (String) params.get(Dict.TRANSFILEPATH);
        List<Map> transList = (List<Map>) params.get(Dict.TRANSLIST);
        String remark = (String) params.get(Dict.REMARK);
        fdevTaskService.putSecurityTest(taskId, correlationSystem,
                correlationInterface, interfaceFilePath, transFilePath, transList, remark);
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/syncTaskUIReportUser", method = RequestMethod.POST)
    @ApiOperation(value = "同步提交UI审核人员")
    public JsonResult syncTaskUIReportUser(@RequestBody Map<String,Object> params) throws Exception {
        fdevTaskService.syncTaskUIReportUser();
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/syncTaskApplicationType", method = RequestMethod.POST)
    @ApiOperation(value = "同步应用类型")
    public JsonResult syncTaskApplicationType(@RequestBody Map<String,Object> params) throws Exception {
        fdevTaskService.syncTaskApplicationType();
        return JsonResultUtil.buildSuccess();
    }

    @ApiOperation(value = "批量查询任务基础信息")
    @RequestMapping(value = "/queryTaskBaseInfoByIds", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.IDS})
    public JsonResult queryTaskBaseInfoByIds(@RequestBody @ApiParam Map<String, List> param) throws Exception {
        return JsonResultUtil.buildSuccess(fdevTaskService.queryTaskBaseInfoByIds(param.get(Dict.IDS),
                param.get(Dict.FIELDS), param.get(Dict.RESPONSEFIELDS)));
    }

    @ApiOperation(value = "实施单元调用修改任务实际提交用户测试时间并反推给研发单元")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @RequestMapping(value = "/updateStartUatTime", method = RequestMethod.POST)
    public JsonResult updateStartUatTime(@RequestBody Map<String, String> param)throws Exception {
        fdevTaskService.updateStartUatTime(param.get(Dict.ID), param.get(Dict.START_UAT_TEST_TIME));
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping(value = "/queryProjectBranchList", method = RequestMethod.POST)
    @ApiOperation(value = "查询git项目所有分支")
    @RequestValidate(NotEmptyFields = {Dict.GITLABPROJECTID})
    public JsonResult queryProjectBranchList(@RequestBody Map<String,Object> params) throws Exception {
        String gitlabProjectId = (String) params.get(Dict.GITLABPROJECTID);
        String applicationType = "";
        if(!CommonUtils.isNullOrEmpty(params.get(Dict.APPLICATIONTYPE))){
            applicationType = (String) params.get(Dict.APPLICATIONTYPE);
        }

        return JsonResultUtil.buildSuccess(gitApiService.queryProjectBranchList(gitlabProjectId, applicationType));
    }

    @ApiOperation(value = "查询任务信息")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @RequestMapping(value = "/queryTaskInfoById", method = RequestMethod.POST)
    public JsonResult queryTaskInfoById(@RequestBody Map<String, Object> param)throws Exception {
        return JsonResultUtil.buildSuccess(fdevTaskService.queryTaskInfoById((String) param.get(Dict.ID), (List<String>) param.get(Dict.FIELDS)));
    }

    @ApiOperation(value = "简单需求任务跳过功能测试")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @RequestMapping(value = "/skipInnerTest", method = RequestMethod.POST)
    public JsonResult skipInnerTest(@RequestBody Map<String, Object> param)throws Exception {
        fdevTaskService.skipInnerTest(param);
        return JsonResultUtil.buildSuccess();
    }

    @RequestMapping("/exportAppTask")
    public void exportAppTask(HttpServletResponse response) throws IOException {
        fdevTaskService.exportAppTask(response);
    }
}
