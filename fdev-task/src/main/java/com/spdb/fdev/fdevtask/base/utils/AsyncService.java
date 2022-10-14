package com.spdb.fdev.fdevtask.base.utils;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.SpringContextUtil;
import com.spdb.fdev.fdevtask.base.dict.Constants;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.unit.DealTaskUnit;
import com.spdb.fdev.fdevtask.spdb.dao.IFdevTaskDao;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTaskCollection;
import com.spdb.fdev.fdevtask.spdb.entity.TaskReviewChild;
import com.spdb.fdev.fdevtask.spdb.entity.ToDoList;
import com.spdb.fdev.fdevtask.spdb.service.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("asyncService")
@RefreshScope
public class AsyncService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${fdev.taskdetail:xxx%s/fdev/#/job/list/%s}")
    private String taskdetailurl;

    @Autowired
    private IFdevTaskService fdevService;

    @Autowired
    private IFdevTaskDao fdevTaskDao;

    @Autowired
    private TodoListApi todoListApi;

    @Autowired
    private IAppApi iappApi;

    @Autowired
    private IUserApi iUserApi;

    @Autowired
    private ReviewRecordService reviewRecordService;

    @Autowired
    private IRedmineApi iRedmineApi;

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private IReleaseTaskApi ReleaseTaskApi;

    @Autowired
    private IEnvConfigApi iEnvConfigApi;

    @Autowired
    private MailUtil mailUtil;

    @Value("${isUpdateRedmineDate}")
    private boolean isUpdateRedmineDate;

    @Autowired
    private RequirementApi requirementApi;

    @Autowired
    private InterfaceApi interfaceApi;

    @Autowired
    private DealTaskUnit dealTaskUnit;

    @Autowired
    private DemandService demandService;

    @Autowired
    private KafkaMsgProducer kafkaProducer;

    @Autowired
    private JiraIssuesService jiraIssuesService;


    @Value("${stageEmailSwitch}")
    private boolean stageEmailSwitch;

    @Value("${updateEmailSwitch}")
    private boolean updateEmailSwitch;

    @Async()
    public void syncSubTask(FdevTask task) throws Exception {
        List listtmp = new ArrayList();
        listtmp.add(task.getId());
        FdevTaskCollection tc = new FdevTaskCollection("", "", listtmp);
        List<Map> list = fdevService.queryBySubTask(tc);
        if (!list.isEmpty()) {
            for (Map tcollection : list) {
                String task_id = (String) tcollection.get(Dict.TASK_ID);
                //主任务更新所有子任务
                if (task.getId().equals(task_id)) {
                    this.syncSubTaskTester(task.getId(), (List<String>) tcollection.get(Dict.TASKCOLLECTION));
                } else {
                    //子任务更新成主任务
                    task.setTester(fdevService.query(new FdevTask.FdevTaskBuilder().id(task_id).init()).get(0).getTester());
                    fdevTaskDao.update(task);
                }
            }
        }
    }

    public void syncSubTaskTester(String tc_id, List<String> tcollection) throws Exception {
        List<FdevTask> list = fdevService.query(
                new FdevTask.FdevTaskBuilder().id(tc_id).init());
        if (!CommonUtils.isNullOrEmpty(list)) {
            String[] tester = list.get(0).getTester();
            for (String id : tcollection) {
                fdevTaskDao.update(new FdevTask.FdevTaskBuilder().id(id).tester(tester).init());
            }
        }

    }


    /**
     * @param projectId
     * @param description    描述
     * @param create_user_id 代办发起人
     * @param appType      目标id(各模块事项唯一标识)
     * @param type           代办类型(如: task_archived)
     * @param merge_id
     * @param task_id
     * @throws Exception
     */
    @Async()
    public void addTodoList(String projectId, String description, String create_user_id, String appType,
                            String type, String merge_id, String task_id) throws Exception {
        Map<String, Object> projectInfo = fdevService.getProjectInfo(projectId,appType);
        String gitUrl = (String) projectInfo.get(Dict.GITLABURL);
        List<String> managerIds = (List<String>) projectInfo.get(Dict.MANAGER);
        if (CommonUtils.isNullOrEmpty(gitUrl))
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"项目gitURL"});
        if (CommonUtils.isNullOrEmpty(managerIds))
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"应用负责人"});
        StringBuffer stringBuffer = new StringBuffer(gitUrl.replaceAll(".git", ""));
        stringBuffer.append("/merge_requests/").append(merge_id);
        Map param = new HashMap();
        param.put(Dict.MODULE, applicationName.replace("f", ""));
        param.put(Dict.LINK, stringBuffer.toString());
        param.put(Dict.DESCRIPTION, description);
        param.put(Dict.TARGET_ID, UUID.randomUUID().toString());
        param.put(Dict.TYPE, type);
        param.put(Dict.CREATE_USER_ID, create_user_id);
        param.put(Dict.USER_IDS, managerIds);
        ToDoList todoList = CommonUtils.mapToBean(param, ToDoList.class);
        todoList.setCreate_date(CommonUtils.dateFormat(new Date(), Dict.FORMATE_DATE));
        todoList.setProject_id(String.valueOf(projectInfo.get(Dict.GITLAB_ID)));
        todoList.setMerge_id(merge_id);
        todoList.setTask_id(task_id);
        logger.info("代办事项保存");
        todoListApi.save(todoList);
        logger.info("代办事项发送");
        iUserApi.addTodoList(param);
    }

    private String[] userList2String(List userList) throws Exception {
        List<String> result = new ArrayList();
        userList.forEach(n -> result.add((String) ((Map) n).get(Dict.ID)));
        return result.toArray(new String[userList.size()]);
    }

    private void demandCount(Map model, FdevTask old, FdevTask task) {
        StringBuffer sb = new StringBuffer();
        StringBuffer oldSb = new StringBuffer();
        if (task.getReview().getCommonProfile() == null){
            model.put("commonProfile", "不涉及");
        }else {
            model.put("commonProfile", task.getReview().getCommonProfile()?"涉及":"不涉及");
        }
        if (CommonUtils.isNullOrEmpty(task.getReview().getSecurityTest())){
            model.put("securityTest", "不涉及");
        }else {
            model.put("securityTest", task.getReview().getSecurityTest());
        }
        if (CommonUtils.isNullOrEmpty(task.getReview().getSpecialCase())){
            model.put("specialCase", "不涉及");
        }else {
            for (String s:task.getReview().getSpecialCase())
            sb.append(s).append(",");
            model.put("specialCase",sb.toString());
            sb.delete(0,sb.length());
        }
        if (task.getReview().getOther_system() == null || task.getReview().getOther_system().length <= 0) {
            model.put("Other_system", "无");
        } else {
            for (TaskReviewChild t : old.getReview().getOther_system()) {
                oldSb.append(t.getName() + ",");
            }
            for (TaskReviewChild t : task.getReview().getOther_system()) {
                sb.append(t.getName() + ",");
            }
            if (oldSb.toString().equals(sb.toString())) {
                model.put("Other_system", (CommonUtils.isNullOrEmpty(sb.toString()) ? "无" : sb.toString()));
            } else {
                model.put("Other_system", (CommonUtils.isNullOrEmpty(oldSb.toString()) ? "无" : oldSb.toString()) + "-------更新为------>" + sb.toString());
            }
            sb.delete(0, sb.length());
            oldSb.delete(0, oldSb.length());
        }
        if (task.getReview().getData_base_alter() == null || task.getReview().getData_base_alter().length <= 0) {
            model.put("Data_base_alter", "无");
        } else {
            for (TaskReviewChild t : old.getReview().getData_base_alter()) {
                oldSb.append(t.getName() + ",");
            }
            for (TaskReviewChild t : task.getReview().getData_base_alter()) {
                sb.append(t.getName() + ",");
            }
            if (oldSb.toString().equals(sb.toString())) {
                model.put("Data_base_alter", (CommonUtils.isNullOrEmpty(sb.toString()) ? "无" : sb.toString()));
            } else {
                model.put("Data_base_alter", (CommonUtils.isNullOrEmpty(oldSb.toString()) ? "无" : oldSb.toString()) + "-------更新为------>" + sb.toString());
            }
            sb.delete(0, sb.length());
            oldSb.delete(0, oldSb.length());
        }

    }

    public void sendFastMail(HashMap model, String person[], String key, String taskName) throws
            Exception {
        Map userparam = new HashMap();
        Set<String> emails = new HashSet();
        for (String s : person) {
            userparam.put(Dict.ID, s);
            String email = (String) iUserApi.queryUser(userparam).get(Dict.EMAIL);
            if (!CommonUtils.isNullOrEmpty(email)) {
                emails.add(email);
            }
        }
        String[] to = emails.toArray(new String[emails.size()]);
        mailUtil.sendTaskMail(key, model, taskName, to);
    }

    private void CountPersonEmail(HashMap model, FdevTask old, FdevTask task) throws
            Exception {
        StringBuffer sb = new StringBuffer();
        StringBuffer oldSb = new StringBuffer();
        Map userParam = new HashMap();
        Map groupParam = new HashMap();
        model.put(Dict.TASK, task);
        if ((CommonUtils.isNullOrEmpty(old.getRedmine_id()) && CommonUtils.isNullOrEmpty(task.getRedmine_id())) || task.getRedmine_id().equals(old.getRedmine_id())) {
            model.put(Dict.REDMINE_ID, CommonUtils.isNullOrEmpty(old.getRedmine_id()) ? "无" : old.getRedmine_id());
        } else {
            model.put(Dict.REDMINE_ID, (CommonUtils.isNullOrEmpty(old.getRedmine_id()) ? "无" : old.getRedmine_id()) + "-------更新为------>" + task.getRedmine_id());
        }
        if ((CommonUtils.isNullOrEmpty(old.getDesc()) && CommonUtils.isNullOrEmpty(task.getDesc())) || task.getDesc().equals(old.getDesc())) {
            model.put(Dict.DESC, CommonUtils.isNotNullOrEmpty(old.getDesc()) ? old.getDesc() : "无");
        } else {
            model.put(Dict.DESC, (CommonUtils.isNotNullOrEmpty(old.getDesc()) ? old.getDesc() : "无") + "------------更新为-----------" + task.getDesc());
        }
        groupParam.put(Dict.ID, task.getGroup());
        Map gmap = iUserApi.queryGroup(groupParam);
        model.put(Dict.GROUPNAME, gmap.get(Dict.NAME));
        oldSb = new StringBuffer(conactUserNames(old.getDeveloper()));
        sb = new StringBuffer(conactUserNames(task.getDeveloper()));
        if (oldSb.toString().equals(sb.toString())) {
            model.put(Dict.DEVELOPER, CommonUtils.isNullOrEmpty(sb.toString()) ? "无" : sb.toString());
        } else {
            model.put(Dict.DEVELOPER, (CommonUtils.isNullOrEmpty(oldSb.toString()) ? "无" : oldSb.toString()) + "-------更新为------>" + sb.toString());
        }
        sb.delete(0, sb.length());
        oldSb.delete(0, oldSb.length());
        oldSb = new StringBuffer(conactUserNames(old.getTester()));
        sb = new StringBuffer(conactUserNames(task.getTester()));
        if (oldSb.toString().equals(sb.toString())) {
            model.put(Dict.TESTER, (CommonUtils.isNullOrEmpty(sb.toString()) ? "无" : sb.toString()));
        } else {
            model.put(Dict.TESTER, (CommonUtils.isNullOrEmpty(oldSb.toString()) ? "无" : oldSb.toString()) + "-------更新为------>" + sb.toString());
        }
        sb.delete(0, sb.length());
        oldSb.delete(0, oldSb.length());
        oldSb = new StringBuffer(conactUserNames(old.getSpdb_master()));
        sb = new StringBuffer(conactUserNames(task.getSpdb_master()));
        if (oldSb.toString().equals(sb.toString())) {
            model.put(Dict.SPDB_MASTER, (CommonUtils.isNullOrEmpty(sb.toString()) ? "无" : sb.toString()));
        } else {
            model.put(Dict.SPDB_MASTER, (CommonUtils.isNullOrEmpty(oldSb.toString()) ? "无" : oldSb.toString()) + "-------更新为------>" + sb.toString());
        }
        sb.delete(0, sb.length());
        oldSb.delete(0, oldSb.length());
        oldSb = new StringBuffer(conactUserNames(old.getMaster()));
        sb = new StringBuffer(conactUserNames(task.getMaster()));
        if (oldSb.toString().equals(sb.toString())) {
            model.put(Dict.MASTER, (CommonUtils.isNullOrEmpty(sb.toString()) ? "无" : sb.toString()));
        } else {
            model.put(Dict.MASTER, (CommonUtils.isNullOrEmpty(oldSb.toString()) ? "无" : oldSb.toString()) + "-------更新为------>" + sb.toString());
        }
        if (CommonUtils.isNullOrEmpty(task.getCreator())) {
            model.put(Dict.CREATOR, "无");
        } else {
            Map param = new HashMap();
            param.put(Dict.ID, task.getCreator());
            Map map = iUserApi.queryUser(param);
            model.put(Dict.CREATOR, map.get(Dict.USER_NAME_CN).toString());

        }
    }

    public String conactUserNames(String[] ids) {
        List names = new ArrayList();
        for (int i = 0; i < ids.length; i++) {
            Map param = new HashMap();
            param.put(Dict.ID, ids[i]);
            try {
                Map map = iUserApi.queryUser(param);
                names.add(map.get(Dict.USER_NAME_CN).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return StringUtils.join(names, "、");
    }

    private void compareTime(HashMap model, FdevTask old, FdevTask task) {
        String oldTime = old.getPlan_inner_test_time();
        String newTime = task.getPlan_inner_test_time();
        if ((CommonUtils.isNullOrEmpty(oldTime) && CommonUtils.isNullOrEmpty(newTime)) || newTime.equals(oldTime)) {
            model.put(Dict.PLAN_INNER_TEST_TIME, CommonUtils.isNullOrEmpty(oldTime) ? "无" : oldTime);
        } else {
            model.put(Dict.PLAN_INNER_TEST_TIME, (CommonUtils.isNullOrEmpty(oldTime) ? "无" : oldTime) + "-------更新为------>" + newTime);
        }
        oldTime = old.getPlan_uat_test_start_time();
        newTime = task.getPlan_uat_test_start_time();
        if ((CommonUtils.isNullOrEmpty(oldTime) && CommonUtils.isNullOrEmpty(newTime)) || newTime.equals(oldTime)) {
            model.put(Dict.PLAN_UAT_TEST_START_TIME, CommonUtils.isNullOrEmpty(oldTime) ? "无" : oldTime);
        } else {
            model.put(Dict.PLAN_UAT_TEST_START_TIME, (CommonUtils.isNullOrEmpty(oldTime) ? "无" : oldTime) + "-------更新为------>" + newTime);
        }
        oldTime = old.getPlan_start_time();
        newTime = task.getPlan_start_time();
        if ((CommonUtils.isNullOrEmpty(oldTime) && CommonUtils.isNullOrEmpty(newTime)) || newTime.equals(oldTime)) {
            model.put(Dict.PLAN_START_TIME, CommonUtils.isNullOrEmpty(oldTime) ? "无" : oldTime);
        } else {
            model.put(Dict.PLAN_START_TIME, (CommonUtils.isNullOrEmpty(oldTime) ? "无" : oldTime) + "-------更新为------>" + newTime);
        }
        oldTime = old.getPlan_rel_test_time();
        newTime = task.getPlan_rel_test_time();
        if ((CommonUtils.isNullOrEmpty(oldTime) && CommonUtils.isNullOrEmpty(newTime)) || newTime.equals(oldTime)) {
            model.put(Dict.PLAN_REL_TEST_TIME, CommonUtils.isNullOrEmpty(oldTime) ? "无" : oldTime);
        } else {
            model.put(Dict.PLAN_REL_TEST_TIME, (CommonUtils.isNullOrEmpty(oldTime) ? "无" : oldTime) + "-------更新为------>" + newTime);
        }
        oldTime = old.getPlan_fire_time();
        newTime = task.getPlan_fire_time();
        if ((CommonUtils.isNullOrEmpty(oldTime) && CommonUtils.isNullOrEmpty(newTime)) || newTime.equals(oldTime)) {
            model.put(Dict.PLAN_FIRE_TIME, CommonUtils.isNullOrEmpty(oldTime) ? "无" : oldTime);
        } else {
            model.put(Dict.PLAN_FIRE_TIME, (CommonUtils.isNullOrEmpty(oldTime) ? "无" : oldTime) + "-------更新为------>" + newTime);
        }
        oldTime = old.getPlan_uat_test_stop_time();
        newTime = task.getPlan_uat_test_stop_time();
        if ((CommonUtils.isNullOrEmpty(oldTime) && CommonUtils.isNullOrEmpty(newTime)) || newTime.equals(oldTime)) {
            model.put(Dict.PLAN_UAT_TEST_STOP_TIME, CommonUtils.isNullOrEmpty(oldTime) ? "无" : oldTime);
        } else {
            model.put(Dict.PLAN_UAT_TEST_STOP_TIME, (CommonUtils.isNullOrEmpty(oldTime) ? "无" : oldTime) + "-------更新为------>" + newTime);
        }
    }

    private Map envInfo(String envName, Integer gitlabId) {
        HashMap model = new HashMap();
        if (CommonUtils.isNotNullOrEmpty(envName)) {
            Map tmp = null;
            try {

                tmp = iEnvConfigApi.queryEnvByNameEn(envName, gitlabId);
                Map a = iEnvConfigApi.mapFilter(tmp);
                Map b = new HashMap();
                List c = (List) a.get(Dict.VAR_LIST);
                c.forEach(n -> b.put(((Map) n).get(Dict.KEY), ((Map) n).get(Dict.VALUE)));
                model.putAll(b);
            } catch (Exception e) {
                logger.error("查询环境信息失败:" + e.getMessage());
            }
        }
        return model;
    }

    private Map sitEnvInfo(FdevTask task) {
        Map param = new HashMap();
        Map result = new HashMap();
        param.put(Dict.ID, task.getProject_id());
        Map resultApp = null;
        try {
            resultApp = iappApi.queryAppById(param);
        } catch (Exception e) {
            logger.info("查询该应用异常");
        }
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
        Map appParam = new HashMap();
        appParam.put(Dict.ID, task.getProject_id());
        Integer gitlabId = 0;
        try {
            Map app = iappApi.queryAppById(appParam);
            gitlabId = (Integer) app.get(Dict.GITLAB_PROJECT_ID);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        for (String env : envNames.keySet()) {
            if (CommonUtils.isNotNullOrEmpty(envNames.get(env))) {
                Map envMap = null;
                try {
                    envMap = iEnvConfigApi.queryEnv(envNames.get(env));
                } catch (Exception e) {
                    logger.info("查询sit环境信息异常");
                }
                String sitName = (String) envMap.get(Dict.NAME_EN);
                result.put(env, envInfo(sitName, gitlabId));
            } else {
                logger.info("查询该应用模块sit环境信息返回为空");
            }
        }
        return result;
    }

    @Async()
    public void sendMail(FdevTask old, FdevTask result, String userName, String rollback_reason) {
        HashMap model = new HashMap();
        String task_id = result.getId();
        String[] creator = new String[1];
        creator[0] = result.getCreator();
        String person[] = CommonUtils.concatAll(result.getMaster(), result.getTester(), result.getDeveloper(),
                result.getSpdb_master(), creator);
        Map appParams = new HashMap();
        appParams.put(Dict.ID, result.getProject_id());
        Integer gitlabId = 0;
        try {
            //add
            if (old == null) {
                model.put(Dict.TASK_CREATETASKUSER, userName);
                model.put(Dict.STAGE, result.getStage());
                CountPersonEmail(model, result, result);
                demandCount(model, result, result);
                compareTime(model, result, result);
                sendFastMail(model, person, Constants.EMAIL_ADDTASK, result.getName());
            }
            //abort
            else if (Dict.TASK_STAGE_ABORT.equals(result.getStage())) {
                model.put(Dict.TASK_CREATETASKUSER, userName);
                model.put(Dict.STAGE, result.getStage());
                CountPersonEmail(model, old, result);
                demandCount(model, old, result);
                compareTime(model, old, result);
                sendFastMail(model, person, Constants.EMAIL_INTOABORT, result.getName());
            }
            //sit
            else if (Dict.TASK_STAGE_DEVELOP.equals(old.getStage()) && Dict.TASK_STAGE_SIT.equals(result.getStage()) && stageEmailSwitch) {
                String name1 = Constants.BRANCH_SIT;
                if(CommonUtils.isApp(result.getApplicationType())) {
                    Map param = new HashMap();
                    param.put(Dict.ID, result.getProject_id());
                    List<String> brnames = iappApi.querySitBranch(param);
                    if (null == brnames || brnames.size() <= 0) {
                        throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK->APP 查询该任务SIT分支返回为空"});
                    }
                    name1 = brnames.get(0);// 目前只有一个
                }
                model.put(Dict.BRANCH_NAME, name1);
                model.put(Dict.TASK_NAME, result.getName());
                CountPersonEmail(model, result, result);
                demandCount(model, old, result);
                model.putAll(sitEnvInfo(result));
                sendFastMail(model, person, Constants.EMAIL_INTOSIT, result.getName());
            }
            //uat
            else if (Dict.TASK_STAGE_SIT.equals(old.getStage()) && Dict.TASK_STAGE_UAT.equals(result.getStage()) && stageEmailSwitch) {
                Map releaseInfo = ReleaseTaskApi.queryDetailByTaskId(task_id, CommonUtils.getReleaseType(old.getApplicationType()));
                Map releaseApp = (Map) releaseInfo.get(Dict.RELEASE_APPLICATION);
                String uat_evn_name = (String) releaseInfo.get(Dict.UAT_ENV_NAME);
                model.put(Dict.UAT_EVN_NAME, uat_evn_name);
                model.put(Dict.RELEASE_BRANCH, releaseApp.get(Dict.RELEASE_BRANCH));
                model.put(Dict.TASK_NAME, result.getName());
                model.put(Dict.RELEASE_NODE_NAME, releaseInfo.get(Dict.RELEASE_NODE_NAME));
                CountPersonEmail(model, result, result);
                demandCount(model, old, result);
                try {
                    Map app = iappApi.queryAppById(appParams);
                    gitlabId = (Integer) app.get(Dict.GITLAB_PROJECT_ID);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
                model.putAll(envInfo(uat_evn_name, gitlabId));
                sendFastMail(model, person, Constants.EMAIL_INTOUAT, result.getName());
            }
            //rel
            else if (Dict.TASK_STAGE_UAT.equals(old.getStage()) && Dict.TASK_STAGE_REL.equals(result.getStage()) && stageEmailSwitch) {
                Map releaseApp = new HashMap();
                Map appParam = new HashMap();
                Map releaseInfo = ReleaseTaskApi.queryDetailByTaskId(task_id, CommonUtils.getReleaseType(old.getApplicationType()));
                releaseApp = (Map) releaseInfo.get(Dict.RELEASE_APPLICATION);
                String rel_evn_name = (String) releaseInfo.get(Dict.REL_ENV_NAME);
                appParam.put(Dict.ID, releaseApp.get(Dict.APPLICATION_ID));
                model.put(Dict.REL_EVN_NAME, rel_evn_name);
                model.put(Dict.PRODUCT_TAG, releaseApp.get(Dict.PRODUCT_TAG) == null ? "无" : releaseApp.get(Dict.PRODUCT_TAG));
                model.put(Dict.PRO_IMAGE_URI, releaseApp.get(Dict.PRO_IMAGE_URI) == null ? "无" : releaseApp.get(Dict.PRO_IMAGE_URI));
                model.put(Dict.TASK_NAME, result.getName());
                demandCount(model, old, result);
                CountPersonEmail(model, result, result);
                try {
                    Map app = iappApi.queryAppById(appParams);
                    gitlabId = (Integer) app.get(Dict.GITLAB_PROJECT_ID);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
                model.putAll(envInfo(rel_evn_name, gitlabId));
                sendFastMail(model, person, Constants.EMAIL_INTOREL, result.getName());
            }
            //pro
            else if (Dict.TASK_STAGE_REL.equals(old.getStage()) && Dict.TASK_STAGE_PRODUCTION.equals(result.getStage()) && stageEmailSwitch) {
                model.put(Dict.FIRE_TIME, result.getFire_time());
                model.put(Dict.TASK, result);
                sendFastMail(model, person, Constants.EMAIL_INTOPRO, result.getName());
            }
            //file
            else if (Dict.TASK_STAGE_PRODUCTION.equals(old.getStage()) && Dict.TASK_STAGE_FILE.equals(result.getStage()) && stageEmailSwitch) {
                demandCount(model, old, result);
                CountPersonEmail(model, result, result);
                compareTime(model, old, result);
                sendFastMail(model, person, Constants.EMAIL_INTOFILE, result.getName());
            }
            //task update
            else if (!CommonUtils.taskEquals(old, result) && CommonUtils.isNullOrEmpty(rollback_reason) && Dict.TASK_STAGE_DISCARD.equals(result.getStage())) {
                model.put(Dict.TASK_CREATETASKUSER, "第三方调用");
                model.put(Dict.STAGE, result.getStage());
                CountPersonEmail(model, old, result);
                demandCount(model, old, result);
                compareTime(model, old, result);
                sendFastMail(model, person, Constants.EMAIL_UPDATETASK, result.getName());
            }
            //task rollback
            else if (!CommonUtils.isNullOrEmpty(rollback_reason)) {
                String port = "";
                String url;
                //判断当前部署环境
                String activeProfile = SpringContextUtil.getActiveProfile();
                if (Dict.SIT.equalsIgnoreCase(activeProfile)) {
                    port = "9093";
                } else if (Dict.REL.equalsIgnoreCase(activeProfile)) {
                    port = "9091";
                } else if ("pro".equalsIgnoreCase(activeProfile)) {
                    port = "8080";
                }
                url = String.format(taskdetailurl, port, result.getId());
                model.put(Dict.TASK_URL, url);
                model.put(Dict.ROLLBACK_REASON, rollback_reason);
                model.put(Dict.TASK, old);
                sendFastMail(model, person, Constants.EMAIL_ROLLBACKTASK, result.getName());
            }

        } catch (Exception e) {
            logger.error("发送邮件失败", e);
        }
    }

    // 同步实际时间  到 redmine
    @Async
    public void updateRedMineDate(FdevTask task) {
        if (isUpdateRedmineDate) {
            StringBuffer query = new StringBuffer();
            StringBuffer update = new StringBuffer();
            String redmineId = task.getRedmine_id();
            String startTime = task.getStart_time();
            String uatStartTime = task.getStart_uat_test_time();
            String uatStopTime = task.getStop_uat_test_time();
            String fireTime = task.getFire_time();
            if (CommonUtils.isNullOrEmpty(startTime) && CommonUtils.isNullOrEmpty(uatStartTime) &&
                    CommonUtils.isNullOrEmpty(uatStopTime) && CommonUtils.isNullOrEmpty(fireTime)) {
                return;
            }
            if (!CommonUtils.isNullOrEmpty(redmineId)) {
                query.append(iRedmineApi.getRedmineHost());
                query.append("?requriement_no=");
                query.append(redmineId);
                Map redmineInfo = iRedmineApi.redMineRest(query.toString());
                // 研发单元 5 还是日常任务
                if (CommonUtils.isNullOrEmpty(redmineInfo)) return;
                Map fs = (Map) ((Map) redmineInfo.get(Dict.ISSUE)).get(Dict.TRACKER);
                boolean flag = "5".equals(fs.get(Dict.ID).toString());
                if (flag) {
                    if (!CommonUtils.isNullOrEmpty(startTime)) {
                        update.append("&ssdy_ks_date=");
                        update.append(CommonUtils.dateParse2(startTime));
                    }
                    if (!CommonUtils.isNullOrEmpty(uatStartTime)) {
                        update.append("&ssdy_uat_date=");
                        update.append(CommonUtils.dateParse2(uatStartTime));
                    }
                    if (!CommonUtils.isNullOrEmpty(uatStopTime)) {
                        update.append("&ssdy_uatjs_date=");
                        update.append(CommonUtils.dateParse2(uatStopTime));
                    }
                    if (!CommonUtils.isNullOrEmpty(fireTime)) {
                        update.append("&ssdy_tc_date=");
                        update.append(CommonUtils.dateParse2(fireTime));
                    }
                } else {
                    if (!CommonUtils.isNullOrEmpty(startTime)) {
                        update.append("&rc_ks_date=");
                        update.append(CommonUtils.dateParse2(startTime));
                    }
                    if (!CommonUtils.isNullOrEmpty(fireTime)) {
                        update.append("&rc_js_date=");
                        update.append(CommonUtils.dateParse2(fireTime));
                    }
                }
                if (!StringUtils.isEmpty(String.valueOf(update))) {
                    query.append(update);
                    logger.info(query.toString());
                    iRedmineApi.updateRedMineDate(query.toString());
                }
            }
        }
    }

    @Async
    public void updateRqrmntWhenStageChanged_tmp(FdevTask task) throws Exception {
        List stageList = new ArrayList();
        stageList.add(Dict.TASK_STAGE_DEVELOP);
        stageList.add(Dict.TASK_STAGE_SIT);
        stageList.add(Dict.TASK_STAGE_UAT);
        stageList.add(Dict.TASK_STAGE_REL);
        stageList.add(Dict.TASK_STAGE_PRODUCTION);
        stageList.add(Dict.TASK_STAGE_ABORT);
        stageList.add(Dict.TASK_STAGE_DISCARD);
        final Map mapping = new HashMap();
        mapping.put(Dict.TASK_STAGE_CREATE, "待实施");
        mapping.put(Dict.TASK_STAGE_DEVELOP, "开发中");
        mapping.put(Dict.TASK_STAGE_SIT, "SIT");
        mapping.put(Dict.TASK_STAGE_UAT, "UAT");
        mapping.put(Dict.TASK_STAGE_REL, "REL");
        mapping.put(Dict.TASK_STAGE_PRODUCTION, "已投产");
        String stage = task.getStage();
        if (stageList.contains(stage)) {
            if (!CommonUtils.isNullOrEmpty(task)) {
                String rqrmntId = task.getRqrmnt_no();
                List<FdevTask> list1 = fdevTaskDao.queryTaskGroupByRqrmnt(rqrmntId);
                List devStartTime = new ArrayList();
                List uatTime = new ArrayList();
                List proTime = new ArrayList();
                if (list1 != null && list1.size() >= 1) {
                    for (FdevTask fdevTask : list1) {
                        String start_time = fdevTask.getStart_time();
                        if (CommonUtils.isNotNullOrEmpty(start_time)) {
                            devStartTime.add(start_time);
                        }
                        String start_uat_test_time = fdevTask.getStart_uat_test_time();
                        if (CommonUtils.isNotNullOrEmpty(start_uat_test_time)) {
                            uatTime.add(start_uat_test_time);
                        }
                        String uat_test_time = fdevTask.getUat_test_time();
                        if (CommonUtils.isNotNullOrEmpty(uat_test_time)) {
                            uatTime.add(uat_test_time);
                        }
                        String fire_time = fdevTask.getFire_time();
                        if (CommonUtils.isNotNullOrEmpty(fire_time)) {
                            proTime.add(fire_time);
                        }
                    }
                }
                String devTimeFirst = "";
                String uatTimeLast = "";
                String proTimeLast = "";
                if (devStartTime != null && devStartTime.size() >= 1) {
                    Collections.sort(devStartTime);
                    devTimeFirst = CommonUtils.dateParse2((String) devStartTime.get(0));
                }
                if (uatTime != null && uatTime.size() >= 1) {
                    Collections.sort(uatTime);
                    uatTimeLast = CommonUtils.dateParse2((String) uatTime.get(uatTime.size() - 1));
                }
                if (proTime != null && proTime.size() >= 1) {
                    Collections.sort(proTime);
                    proTimeLast = CommonUtils.dateParse2((String) proTime.get(proTime.size() - 1));
                }
                if (!CommonUtils.isNullOrEmpty(rqrmntId)) {
                    if (Dict.TASK_STAGE_DEVELOP.equalsIgnoreCase(stage)) {
                        stage = Dict.TASK_STAGE_DEVELOP;
                    } else {
                        try {
                            List<String> stages = new ArrayList<>();
                            List list = fdevTaskDao.queryTaskGroupByRqrmnt(rqrmntId);
                            if (CommonUtils.isNullOrEmpty(list)) {
                                FdevTask fdevTask = new FdevTask();
                                fdevTask.setRqrmnt_no(rqrmntId);
                                fdevTask.setStage(Dict.TASK_STAGE_FILE);
                                List<FdevTask> fdevTasks = fdevTaskDao.queryAll(fdevTask);
                                if (CommonUtils.isNullOrEmpty(fdevTasks)) {
                                    stages.add(Dict.TASK_STAGE_CREATE);
                                } else {
                                    stages.add(Dict.TASK_STAGE_PRODUCTION);
                                }
                            }
                            for (Object item : list) {
                                Map tmp = (Map) item;
                                if (CommonUtils.isNotNullOrEmpty((String) tmp.get(Dict.UAT_TEST_TIME)) && tmp.get(Dict.STAGE).equals(Dict.TASK_STAGE_SIT)) {
                                    tmp.put(Dict.STAGE, Dict.TASK_STAGE_UAT);
                                }
                                stages.add((String) tmp.get(Dict.STAGE));
                            }
                            if (stages.stream().allMatch(e -> e.equalsIgnoreCase(Dict.TASK_STAGE_ABORT) || e.equalsIgnoreCase(Dict.TASK_STAGE_DISCARD))) {
                                stage = Dict.TASK_STAGE_CREATE;
                            } else {
                                stage = CommonUtils.minStage(stages);
                            }

                        } catch (Exception e) {
                            logger.error("" + e.getMessage());
                        }
                    }
                    String stageZH = (String) mapping.get(stage);
                    if (CommonUtils.isNullOrEmpty(stageZH)) {
                        return;
                    }
                    switch (stageZH) {
                        case "开发中":
                            //添加最早启动时间
                            Map map = new HashMap();
                            map.put("estInitRealDate", devTimeFirst);
                            requirementApi.updateRqrmntState(rqrmntId, stageZH, map);
                            break;
                        case "SIT":
                            //添加最早启动时间
                            Map map5 = new HashMap();
                            map5.put("estInitRealDate", devTimeFirst);
                            requirementApi.updateRqrmntState(rqrmntId, stageZH, map5);
                            break;
                        case "UAT":
                            //添加最晚进入业测时间与最早启动时间
                            Map map1 = new HashMap();
                            map1.put("estInitRealDate", devTimeFirst);
                            map1.put("estUatRealDate", uatTimeLast);
                            requirementApi.updateRqrmntState(rqrmntId, stageZH, map1);
                            break;
                        case "REL":
                            //添加最晚进入业测时间与最早启动时间
                            Map map4 = new HashMap();
                            map4.put("estInitRealDate", devTimeFirst);
                            map4.put("estUatRealDate", uatTimeLast);
                            requirementApi.updateRqrmntState(rqrmntId, stageZH, map4);
                            break;
                        case "已投产":
                            //添加进入最晚业测时间与最早启动时间与最晚投产时间
                            Map map2 = new HashMap();
                            map2.put("estInitRealDate", devTimeFirst);
                            map2.put("estUatRealDate", uatTimeLast);
                            map2.put("estRelComplRealDate", proTimeLast);
                            requirementApi.updateRqrmntState(rqrmntId, stageZH, map2);
                            break;
                        default:
                            Map map3 = new HashMap();
                            requirementApi.updateRqrmntState(rqrmntId, stageZH, map3);
                            break;
                    }

                }
            }
        }
    }

    public void deleteForInterface(FdevTask newly, FdevTask older) {
        List unnormal = new ArrayList();
        unnormal.add(Dict.TASK_STAGE_DISCARD);
        unnormal.add(Dict.TASK_STAGE_FILE);
        unnormal.add(Dict.TASK_STAGE_ABORT);
        String newlyStage = newly.getStage();
        String oldStage = older.getStage();
        if (!CommonUtils.isNullOrEmpty(newlyStage) && unnormal.contains(newlyStage) && !unnormal.contains(oldStage)) {
            String appname = older.getProject_name();
            String branch = older.getFeature_branch();
            if (CommonUtils.isNotNullOrEmpty(appname, branch)) {
                String taskname = older.getName();
                interfaceApi.delete(appname, branch);
                logger.info("调用接口删除成功 任务名：{}", taskname);
            }
        }
    }

    /**
     * 任务下线或者投产的时候 将审核记录归档
     *
     * @param
     */
    @Async
    public void deleteReviewRecordCaseByTaskStageChange(FdevTask ntask, FdevTask otask) {
        reviewRecordService.deleteTaskRecord(ntask, otask);
    }

    /**
     * 当任务状态发生变化后、要重新计算当前需求的所处阶段和实际投产日期
     * 调用场景: 新增任务、删除废弃任务、更新任务(状态变更、kafka)
     * @param task 变更的当前任务
     * @param isSameDemand 是否是同一个需求
     */
    @Async
    public void updateRqrmntWhenStageChanged(FdevTask task, Boolean isSameDemand) {
        logger.info("通知需求模块变更状态{}", task.getId());
        // 1、获取当前任务所属需求的所有任务集
        String rqrmntNo = task.getRqrmnt_no();
        String unitNo = task.getFdev_implement_unit_no();
        List<FdevTask> fdevTasks = null;
        //待实施任务集合
        List<FdevTask> todoTasks = null;
        if (isSameDemand || !CommonUtils.isNullOrEmpty(unitNo)) {
            fdevTasks = fdevTaskDao.queryAllTaskByUnitNo(unitNo, false);
            //查询是否存在有待实施的任务
            todoTasks = fdevTaskDao.queryAllTaskByUnitNo(unitNo, true);
        }else {
            fdevTasks = fdevTaskDao.queryAllTaskByDemandNo(rqrmntNo, false);
            todoTasks = fdevTaskDao.queryAllTaskByDemandNo(rqrmntNo, true);
        }
        //当开发阶段的删除或归档完，待实施的任务仍然存在，便修改研发单元为待实施
        if(fdevTasks.size() == 0) {
            logger.info("所有任务状态为废弃或者删除");
            Map param = new HashMap();
            param.put(Dict.REAL_START_DATE, "");
            param.put(Dict.REAL_INNER_TEST_DATE, "");
            param.put(Dict.REAL_TEST_DATE, "");
            param.put(Dict.REAL_TEST_FINISH_DATE, "");
            param.put(Dict.REAL_PRODUCT_DATE, "");
            //置空
            if (CommonUtils.isNullOrEmpty(unitNo)) {
                logger.error("研发单元编号为null，调用老需求修改接口");
                requirementApi.updateDemandState(rqrmntNo, "create-info", param);
            } else
                requirementApi.updateImplUnitState(unitNo, "create-info", param);
            return;
        }
        // 2、获取当前需求应该处于的阶段,返回当前正确的需求阶段和需要更新的实际时间。
        Tuple.Tuple2<String, HashMap<String, String>> tuple = dealTaskUnit.getStageAndDate(fdevTasks,todoTasks);
        //查询是否存在有待实施的任务，若存在，研发单元更新为开发中，若不存在，研发单元更新为待实施
        if (CommonUtils.isNullOrEmpty(tuple) && todoTasks.size() != 0)
            tuple = dealTaskUnit.changeToDevelop(tuple, todoTasks);
        if (null != tuple) {
            // 3、调用需求模块更新时间
            //如果研发单元不存在（即老任务），便修改需求状态
            if (CommonUtils.isNullOrEmpty(unitNo)){
                logger.error("研发单元编号为null，调用老需求修改接口");
                requirementApi.updateDemandState(rqrmntNo, tuple.first(), tuple.second());
            }else
                requirementApi.updateImplUnitState(unitNo, tuple.first(), tuple.second());
        }
    }

    @Async
    public void sendMessage(Map params){
        kafkaProducer.sendMessage(params);
    }

    @Async
    public void updateJiraStoryStatus(FdevTask fdevTask){
        String storyId = fdevTask.getStoryId();
        List<FdevTask> taskList = fdevTaskDao.queryTaskByStoryId(storyId);
        List<Integer> status = taskList.stream().map(FdevTask::getStage).distinct().map(CommonUtils::pareStage).sorted().collect(Collectors.toList());
        Integer mixStatus = CommonUtils.isNullOrEmpty(status)?-1:status.get(0);
        //最小的状态小于develop，但有其他任务的状态在[develop,file],故事状态取处理中
        List<Integer> started = status.stream().filter(p -> p > 3 && p < 10).collect(Collectors.toList());
        if(!CommonUtils.isNullOrEmpty(started) && mixStatus < 4){
            mixStatus = 4;
        }
        jiraIssuesService.updateJiraTaskStatus(storyId,CommonUtils.getJiraTaskStatus(mixStatus));
    }

    @Async
    public void updateJiraSubTaskStatus(FdevTask fdevTask){
        jiraIssuesService.updateJiraTaskStatus(fdevTask.getJiraKey(),CommonUtils.getJiraTaskStatus(CommonUtils.pareStage(fdevTask.getStage())));
    }

    @Async
    public void updateJiraAssignee(String key,String developerId){
        jiraIssuesService.updateJiraSubTask(key,developerId);
    }

    @Async
    public void deleteJiraSubTask(String key){
        jiraIssuesService.deleteJiraSubTask(key);
    }
}
