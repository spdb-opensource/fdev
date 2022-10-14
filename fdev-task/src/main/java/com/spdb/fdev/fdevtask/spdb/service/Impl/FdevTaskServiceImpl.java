package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevtask.base.dict.Constants;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.unit.AsyncTaskUnit;
import com.spdb.fdev.fdevtask.base.utils.*;
import com.spdb.fdev.fdevtask.spdb.dao.IFdevTaskDao;
import com.spdb.fdev.fdevtask.spdb.dao.IReleaseApproveDao;
import com.spdb.fdev.fdevtask.spdb.dao.ReviewRecordDao;
import com.spdb.fdev.fdevtask.spdb.entity.*;
import com.spdb.fdev.fdevtask.spdb.service.*;
import com.spdb.fdev.transport.RestTransport;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


@Service
@RefreshScope
public class FdevTaskServiceImpl implements IFdevTaskService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志
    @Autowired
    @Lazy
    private IFdevTaskDao fdevTaskDao;

    @Autowired
    private UserApiImpl userApi;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ITestApi iTestApi;

    @Autowired
    private IAppApi appApi;

    @Autowired
    private IReleaseTaskApi ReleaseTaskApi;

    @Autowired
    private IReleaseApproveDao releaseApproveDao;
    @Autowired
    @Lazy
    private AsyncService asyncService;

    @Autowired
    private INotifyApi iNotifyApi;

    @Autowired
    private GroupTreeUtil groupTreeUtil;

    @Autowired
    private IUserApi iUserApi;

    @Autowired
    private FdevTaskUtils fdevTaskUtils;

    @Autowired
    private GitApiService gitApiService;

    @Autowired
    private IComponentService componentService;

    @Value("${git.token}")
    private String token;

    @Value("${minio.docResource}")
    private String docResource;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Resource
    private AsyncTaskUnit asyncTaskUnit;

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private DemandService demandService;

    @Autowired
    private ReviewRecordDao reviewRecordDao;

    @Autowired
    private FdocmanageService fdocmanageService;

    @Value("${isSendMail}")
    private boolean isSendMail;

    @Value("${internet.group.id}")
    private String internetGroupId;

    @Value("${release.merge.team}")
    private List<String> releaseMergeTeam;//须走release合并审核流程的三级组

    @Autowired
    private UserUtils userUtils;


    @Autowired
    private ReviewRecordService reviewRecordService;

    @Autowired
    private IDocService docService;

    @Override
    public FdevTask save(FdevTask task) throws Exception {
        FdevTask ftask = fdevTaskDao.save(task);
        if (Constants.COMPONENT_SERVER.equals(ftask.getApplicationType())) {
            try {
                componentService.savePredictVersion(ftask.getProject_id(), ftask.getVersionNum(), Constants.BACK);
            } catch (Exception e) {
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"任务创建成功保存预设版本号失败:" + e.getMessage()});
            }
        }
        if (Constants.COMPONENT_WEB.equals(ftask.getApplicationType())) {
            try {
                componentService.savePredictVersion(ftask.getProject_id(), ftask.getVersionNum(), Constants.MPASS);
            } catch (Exception e) {
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"任务创建成功保存预设版本号失败:" + e.getMessage()});
            }

        }
        if (Constants.ARCHETYPE_SERVER.equals(ftask.getApplicationType())) {
            try {
                componentService.saveTargetVersion(ftask.getProject_id(), ftask.getVersionNum());
            } catch (Exception e) {
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"任务创建成功保存目标版本号失败:" + e.getMessage()});
            }
        }
        return ftask;
    }

    @Override
    public List<Map> queryWithName(FdevTask task) throws Exception {
        List<FdevTask> taskList = fdevTaskDao.query(task);
        List<Map> result = new ArrayList<Map>();
        if (!CommonUtils.isNullOrEmpty(taskList) && !taskList.isEmpty()) {
            for (int i = 0; i < taskList.size(); i++) {
                Map temp = CommonUtils.beanToMap(taskList.get(i));
                temp.putAll(this.reBuildUser(taskList.get(i)));
                result.add(temp);
            }
        }
        return result;
    }

    @Override
    public FdevTask update(FdevTask task) throws Exception {
        //查询修改前的任务信息
        List<FdevTask> list = query(new FdevTask.FdevTaskBuilder().id(task.getId()).init());
        if (CommonUtils.isNullOrEmpty(list)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{task.getId()});
        }
        FdevTask oldTask = list.get(0);
        //判断是否编辑了实际开始UAT日期和实际开始REL日期
        String start_uat_test_time_old = oldTask.getStart_uat_test_time();//修改前开始uat测试时间
        String start_rel_test_time_old = oldTask.getStart_rel_test_time();//修改前开始rel测试时间
        String start_uat_test_time = task.getStart_uat_test_time();//修改后开始uat测试时间
        String start_rel_test_time = task.getStart_rel_test_time();//修改后开始rel测试时间
        boolean updateUatTime = false;//修改了实际提交用户测试日期、实际用户测试完成日期标记
        if ((CommonUtils.isNullOrEmpty(start_uat_test_time_old) && CommonUtils.isNotNullOrEmpty(start_uat_test_time))
                || (CommonUtils.isNotNullOrEmpty(start_rel_test_time_old) && "".equals(start_rel_test_time))) {
            //新增了uat测试启动日期,或者清除了rel测试启动日期将状态推到uat
            task.setStage(Dict.TASK_STAGE_UAT);
            updateUatTime = true;
        }
        if (CommonUtils.isNullOrEmpty(start_rel_test_time_old) && !CommonUtils.isNullOrEmpty(start_rel_test_time)) {
            //新增了rel测试启动日期将状态推到rel
            task.setStage(Dict.TASK_STAGE_REL);
            updateUatTime = true;
        }
        String new_stage = task.getStage();
        String old_stage = oldTask.getStage();
        //只修改时间不推阶段
        if (((CommonUtils.isNotNullOrEmpty(start_uat_test_time) && !start_uat_test_time.equals(start_uat_test_time_old))
                || (CommonUtils.isNotNullOrEmpty(start_rel_test_time) && !start_rel_test_time.equals(start_rel_test_time_old)))
                && old_stage.equals(new_stage)) {
            updateUatTime = true;
        }
        //更新的任务状态要是连续的
        if (CommonUtils.isNotNullOrEmpty(new_stage, old_stage)) {
            if ((old_stage.equals(Dict.TASK_STAGE_SIT) && new_stage.equals(Dict.TASK_STAGE_PRODUCTION))
                    || (old_stage.equals(Dict.TASK_STAGE_UAT) && new_stage.equals(Dict.TASK_STAGE_PRODUCTION))) {
                throw new FdevException(ErrorConstants.CHANGE_TASK_STAGE_ERROR, new String[]{old_stage, new_stage});
            }
        }
        //日常任务点已完成按钮
        if ("2".equals(String.valueOf(oldTask.getTaskType()))
                && CommonUtils.isNotNullOrEmpty(new_stage, old_stage)
                && Dict.TASK_STAGE_DEVELOP.equals(old_stage)
                && Dict.TASK_STAGE_PRODUCTION.equals(new_stage)) {
            task.setFire_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1));
        }
        //不再主动设置实际开始rel日期，由任务编辑页面用户手动设置
//        if (Dict.TASK_STAGE_UAT.equalsIgnoreCase(old_stage) && Dict.TASK_STAGE_REL.equalsIgnoreCase(new_stage)) {
//            task.setStart_rel_test_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1));
//        }
        FdevTask ftask = fdevTaskDao.update(task);
        asyncService.syncSubTask(ftask);
        //归档方法很多，统一在service过滤
        if (!CommonUtils.isNullOrEmpty(ftask.getRedmine_id())) {
            task.setRedmine_id(ftask.getRedmine_id());
        }
        asyncService.updateRedMineDate(task);
        //删除 接口调用关系
        asyncService.deleteForInterface(task, oldTask);
        asyncService.deleteReviewRecordCaseByTaskStageChange(task, ftask);
        String rqrno = ftask.getRqrmnt_no();
        String oldRqrno = oldTask.getRqrmnt_no();
        //新的研发单元编号
        String newUnitNo = ftask.getFdev_implement_unit_no();
        //旧的研发单元编号
        String oldUnitNo = oldTask.getFdev_implement_unit_no();
        //需求编号 新增 修改 （不考虑删除）
        if (!Dict.TASK_STAGE_FILE.equals(new_stage)) {//任务归档不推送状态
            if (updateUatTime || CommonUtils.isNotNullOrEmpty(new_stage, old_stage) && !new_stage.equals(old_stage) ||
                    CommonUtils.isNullOrEmpty(oldRqrno) && !CommonUtils.isNullOrEmpty(rqrno)) {
                asyncService.updateRqrmntWhenStageChanged(ftask, false);
            }
            if (CommonUtils.isNotNullOrEmpty(rqrno, oldRqrno) && !rqrno.equals(oldRqrno)) {
                asyncService.updateRqrmntWhenStageChanged(ftask, false);
                asyncService.updateRqrmntWhenStageChanged(new FdevTask.FdevTaskBuilder().stage(Dict.TASK_STAGE_ABORT).rqrmnt_no(oldRqrno).unitNo(oldUnitNo).init(), false);
            } else {
                //同一个需求下的不同研发单元,
                //根据是否更新了研发单元来更新相应的研发单元
                if (!CommonUtils.isNullOrEmpty(newUnitNo) && !newUnitNo.equals(oldUnitNo)) {
                    asyncService.updateRqrmntWhenStageChanged(ftask, true);
                    asyncService.updateRqrmntWhenStageChanged(new FdevTask.FdevTaskBuilder().stage(Dict.TASK_STAGE_ABORT).unitNo(oldUnitNo).init(), true);
                }
            }
        }

        //和jira关联的需要更新故事和子任务状态
        if (!CommonUtils.isNullOrEmpty(oldTask.getStoryId()) && CommonUtils.isNotNullOrEmpty(new_stage, old_stage) && !new_stage.equals(old_stage)) {
            //更新故事状态
            logger.info("更新故事状态:" + oldTask.getStoryId());
            asyncService.updateJiraStoryStatus(oldTask);
            if (Dict.TASK_STAGE_ABORT.equals(new_stage) || Dict.TASK_STAGE_DISCARD.equals(new_stage)) {
                //删除子任务
                logger.info("删除子任务:" + oldTask.getJiraKey());
                asyncService.deleteJiraSubTask(oldTask.getJiraKey());
            } else {
                logger.info("更新子任务状态" + ftask.getJiraKey());
                asyncService.updateJiraSubTaskStatus(ftask);
            }
        }
        //开发人员变更需要更新jira经办人
        if (!CommonUtils.isNullOrEmpty(oldTask.getStoryId()) && null != task.getDeveloper()) {
            asyncService.updateJiraAssignee(oldTask.getJiraKey(), 0 == task.getDeveloper().length ? "" : task.getDeveloper()[0]);
        }
        return ftask;
    }

    @Override
    public List<FdevTask> query(FdevTask task) throws Exception {
        return fdevTaskDao.query(task);
    }

    @Override
    public Map<String, Object> queryUserTask(String userId, String nameKey, Integer pageNum, Integer pageSize, Boolean incloudFile, Set<String> delayStage) throws Exception {
        if (CommonUtils.isNullOrEmpty(userId)) {
            User user = userVerifyUtil.getRedisUser();
            if (CommonUtils.isNullOrEmpty(user)) {
                throw new FdevException(ErrorConstants.USR_AUTH_FAIL);
            }
            userId = user.getId();
        }
        Map<String, Object> map = fdevTaskDao.queryUserTask(userId, nameKey, pageNum, pageSize, incloudFile);
        List<Map> result = new ArrayList<>();
        List<FdevTask> taskList = (List<FdevTask>) map.get(Dict.DATA);
        if (!CommonUtils.isNullOrEmpty(taskList) && !taskList.isEmpty()) {
            //根据延期选项做结果筛选
            List<FdevTask> taskListResult = new ArrayList<>();
            HashSet<String> temp = new HashSet<>();
            if (!CommonUtils.isNullOrEmpty(delayStage)) {
                for (FdevTask fdevTask : taskList) {
                    temp.clear();
                    temp.addAll(delayStage);
                    temp.retainAll(isDelay(fdevTask));
                    if (temp.size() > 0) {
                        taskListResult.add(fdevTask);
                    }
                }
            } else {
                taskListResult = taskList;
            }
            //批量查询所有相关人员、小组和需求信息
            Set<String> taskUserIds = new HashSet<>();
            Set<String> taskGroupIds = new HashSet<>();
            Set<String> taskDemandIds = new HashSet<>();
            Set<String> delayTaskIds = new HashSet<>();
            for (FdevTask task : taskListResult) {
                FdevTaskUtils.setIds(task, taskUserIds, taskGroupIds, taskDemandIds);
                //判断任务是否延期，并返回延期任务的id
                if (!CommonUtils.isNullOrEmpty(isDelay(task))) {
                    delayTaskIds.add(task.getId());
                }
            }
            //批量查询人员信息
            Map<String, Map> userMap = userUtils.getUserByIds(taskUserIds);
            //批量查询小组信息
            List<Map> groupList = userApi.queryGroupByIds(new ArrayList<>(taskGroupIds));
            //将小组信息和id对应起来
            Map<String, Map> groupMap = new HashMap<>();
            if (!CommonUtils.isNullOrEmpty(groupList)) {
                for (Map group : groupList) {
                    groupMap.put((String) group.get(Dict.ID), group);
                }
            }
            //批量查询需求信息
            List<Map<String, String>> demandInfo = demandService.queryDemandByIds(taskDemandIds);
            //将需求信息和id对应起来
            Map<String, Map> demandMap = new HashMap<>();
            if (!CommonUtils.isNullOrEmpty(demandInfo)) {
                for (Map<String, String> demand : demandInfo) {
                    demandMap.put(demand.get(Dict.ID), demand);
                }
            }
            //字段组装
            result = setTaskInfo(taskListResult, userMap, groupMap, new HashMap<>(), null, delayTaskIds);
        }
        map.put(Dict.DATA, result);
        return map;
    }

    //判断任务是否延期
    private Set<String> isDelay(FdevTask task) {
        return new HashSet<String>() {{
            if (CommonUtils.taskDateCompare(task.getPlan_start_time(), task.getStart_time()) > 0) {
                //启动延期
                add("develop");
            }
            if ((CommonUtils.isNullOrEmpty(task.getTaskType()) || task.getTaskType() != 2)
                    && CommonUtils.taskDateCompare(task.getPlan_inner_test_time(), task.getStart_inner_test_time()) > 0) {
                //内测延期
                add("sit");
            }
            if ((CommonUtils.isNullOrEmpty(task.getTaskType()) || task.getTaskType() != 2)
                    && CommonUtils.taskDateCompare(task.getPlan_uat_test_start_time(), task.getStart_uat_test_time()) > 0) {
                //提交用户测试延期
                add("uat");
            }
            if ((CommonUtils.isNullOrEmpty(task.getTaskType()) || task.getTaskType() != 2)
                    && CommonUtils.taskDateCompare(task.getPlan_rel_test_time(), task.getStart_rel_test_time()) > 0) {
                //用户测试完成延期
                add("rel");
            }
            if (CommonUtils.taskDateCompare(task.getPlan_fire_time(), task.getFire_time()) > 0) {
                //投产延期
                add("production");
            }
        }};

    }

    /**
     * DESC : 完善任务 用户信息和小组信息
     *
     * @param fdevTask
     * @return
     * @throws Exception 2019年4月2日
     */
    public Map queryUserTaskBuildUser(FdevTask fdevTask) throws Exception {
        Map result = CommonUtils.beanToMap(fdevTask);
        //任务负责人
        String group = fdevTask.getGroup();
        result.put(Dict.GROUP, group(group));
        result.put("ipmp_implement_unit_no", fdevTask.getRedmine_id());
        Map<String, List<String>> doc_uploader = fdevTask.getDoc_uploader();
        if (null != doc_uploader) {
            result.put("doc_uploader", doc_uploader.keySet().toArray(new String[]{}));
        }
        /*测试信息*/
        String testSwitch = "";
        if (CommonUtils.isApp(fdevTask.getApplicationType())) {
            testSwitch = appApi.QueryTestSwitch(fdevTask.getProject_id());
        }

        if ("0".equals(testSwitch)) {
            //不涉及内测
            result.put("test_info", new HashMap<>());
        } else {
            //涉及内测
            Map testInfo = reBuildTestInfo(fdevTask, null, null);
            List<Map> testsers = new ArrayList<>();
            result.put(Dict.TESTER, testsers);
            if (testInfo != null) {
                testsers = (List<Map>) testInfo.remove("testers");
                result.put(Dict.TESTER, testsers);
            }
            result.put("test_info", testInfo);
        }
        return result;
    }

    /**
     * 获取任务集合中的所有人员信息
     *
     * @param taskList
     * @return
     */
    public Map getAllUserInfo(List<FdevTask> taskList) throws Exception {
        Map users = new HashMap();
        if (!CommonUtils.isNullOrEmpty(taskList) && !taskList.isEmpty()) {
            //封装用户信息
            Set<String> userIds = new HashSet<>();
            for (int i = 0; i < taskList.size(); i++) {
                //任务负责人
                FdevTask fdevTask = taskList.get(i);
                String[] master = fdevTask.getMaster();
                String[] spdbMaster = fdevTask.getSpdb_master();
                String[] developer = fdevTask.getDeveloper();
                String[] concern = fdevTask.getConcern();
                String creator = fdevTask.getCreator();
                String reviewer = fdevTask.getReviewer();
                //批量查询人员信息
                if (!CommonUtils.isNullOrEmpty(master)) {
                    userIds.addAll(Arrays.asList(master));
                }
                if (!CommonUtils.isNullOrEmpty(spdbMaster)) {
                    userIds.addAll(Arrays.asList(spdbMaster));
                }
                if (!CommonUtils.isNullOrEmpty(developer)) {
                    userIds.addAll(Arrays.asList(developer));
                }
                if (!CommonUtils.isNullOrEmpty(concern)) {
                    userIds.addAll(Arrays.asList(concern));
                }
                if (!CommonUtils.isNullOrEmpty(creator)) {
                    userIds.add(creator);
                }
                if (!CommonUtils.isNullOrEmpty(reviewer)) {
                    userIds.add(reviewer);
                }
            }
            users = userUtils.getUserByIds(userIds);
        }
        return users;
    }

    /**
     * 从批量任务信息中，返回所选任务的人员信息
     *
     * @return
     * @throws Exception
     */
    public Map rebuildAllUserInfo(Map users, FdevTask fdevTask) {
        Map result = new HashMap();
        result.put(Dict.MASTER, getUsersInfo(fdevTask.getMaster(), users));
        result.put(Dict.SPDB_MASTER, getUsersInfo(fdevTask.getSpdb_master(), users));
        result.put(Dict.DEVELOPER, getUsersInfo(fdevTask.getDeveloper(), users));
        result.put(Dict.CONCERN, getUsersInfo(fdevTask.getConcern(), users));
        result.put(Dict.CREATOR, getUsersInfo(fdevTask.getCreator(), users));
        result.put(Dict.REVIEWER, getUsersInfo(fdevTask.getReviewer(), users));

        return result;
    }

    private Map<String, String> getUsersInfo(String userId, Map users) {
        Map<String, String> result = new HashMap<>();
        if (CommonUtils.isNullOrEmpty(userId)) {
            return result;
        }
        if (!CommonUtils.isNullOrEmpty(users.get(userId))) {
            Map<String, Object> userInfo = (Map<String, Object>) users.get(userId);
            result.put(Dict.USER_NAME_EN, (String) userInfo.get(Dict.USER_NAME_EN));
            result.put(Dict.USER_NAME_CN, (String) userInfo.get(Dict.USER_NAME_CN));
            result.put(Dict.ID, (String) userInfo.get(Dict.ID));
        }
        return result;
    }

    private List<Map<String, String>> getUsersInfo(String[] userIds, Map users) {
        List<Map<String, String>> result = new ArrayList<>();
        if (CommonUtils.isNullOrEmpty(userIds) || userIds.length == 0) {
            return result;
        }
        for (String userId : userIds) {
            if (!CommonUtils.isNullOrEmpty(users.get(userId))) {
                Map<String, Object> userInfo = (Map<String, Object>) users.get(userId);
                Map<String, String> map = new HashMap<>();
                map.put(Dict.USER_NAME_EN, (String) userInfo.get(Dict.USER_NAME_EN));
                map.put(Dict.USER_NAME_CN, (String) userInfo.get(Dict.USER_NAME_CN));
                map.put(Dict.ID, (String) userInfo.get(Dict.ID));
                result.add(map);
            }
        }
        return result;
    }

    @Override
    public List<Map> queryTaskByTerm(String taskName, String stage) throws Exception {
        return fdevTaskDao.queryTaskByTerm(taskName, stage);
    }


    public List<FdevTask> queryTasksByIds(ArrayList ids) throws Exception {
        return fdevTaskDao.queryTasksByIds(ids);
    }

    @Override
    public List<FdevTask> queryTasksByIdsNoAbort(ArrayList ids) throws Exception {
        return fdevTaskDao.queryTasksByIdsNoAbort(ids);
    }

    @Override
    public List<Map<String, Object>> queryTaskSimpleByIds(List<String> ids) throws Exception {
        List<FdevTask> tasks = fdevTaskDao.queryTasksByIdsNoAbort(ids);
        Set<String> userIds = new HashSet<String>() {{
            tasks.forEach(task -> {
                if (!CommonUtils.isNullOrEmpty(task.getMaster())) addAll(Arrays.asList(task.getMaster()));
                if (!CommonUtils.isNullOrEmpty(task.getSpdb_master())) addAll(Arrays.asList(task.getSpdb_master()));
            });
        }};
        Map<String, Object> userMap = getUserInfoByIds(new ArrayList() {{
            addAll(userIds);
        }});
        //Map<String, String> appIdNameMap = (Map<String, String>) appApi.queryAppByIds(tasks.stream().map(FdevTask::getProject_id).collect(Collectors.toList())).stream().collect(Collectors.toMap(x -> ((Map<String, String>) x).get("id"), x -> ((Map<String, String>) x).get("name_zh")));
        return new ArrayList<Map<String, Object>>() {{
            tasks.forEach(task -> add(new HashMap<String, Object>() {{
                put(Dict.ID, task.getId());
                put(Dict.NAME, task.getName());
                put(Dict.FDEV_IMPLEMENT_UNIT_NO, task.getFdev_implement_unit_no());
                put(Dict.MASTER, null != task.getMaster() ? Arrays.stream(task.getMaster()).map(userId -> new HashMap<String, String>() {{
                    if (!CommonUtils.isNullOrEmpty(userMap.get(userId))) {
                        Map<String, String> temp = (Map<String, String>) userMap.get(userId);
                        put(Dict.ID, temp.get(Dict.ID));
                        put(Dict.USER_NAME_CN, temp.get(Dict.USER_NAME_CN));
                    }
                }}).collect(Collectors.toSet()) : new HashSet<>());
                put(Dict.SPDB_MASTER, null != task.getSpdb_master() ? Arrays.stream(task.getSpdb_master()).map(userId -> new HashMap<String, String>() {{
                    if (!CommonUtils.isNullOrEmpty(userMap.get(userId))) {
                        Map<String, String> temp = (Map<String, String>) userMap.get(userId);
                        put(Dict.ID, temp.get(Dict.ID));
                        put(Dict.USER_NAME_CN, temp.get(Dict.USER_NAME_CN));
                    }
                }}).collect(Collectors.toSet()) : new HashSet<>());
                put(Dict.PROJECT_ID, task.getProject_id());
                put(Dict.PROJECT_NAME, task.getProject_name());
                put(Dict.STAGE, task.getStage());
            }}));
        }};
    }


    @Override
    public List<FdevTask> queryAllTasksByIds(ArrayList ids) throws Exception {
        return fdevTaskDao.queryAllTasksByIds(ids);
    }

    @Override
    public FdevTask updateTaskView(String scope, String viewId, String name, Boolean audit) throws Exception {
        return fdevTaskDao.updateTaskView(scope, viewId, name, audit);
    }

    @Override
    public List<FdevTask> queryByTerms(ArrayList group, ArrayList stage, boolean isIncludeChildren, Integer isDefered) throws Exception {
        Set<String> groupIds = new HashSet<>();
        if (isIncludeChildren && !CommonUtils.isNullOrEmpty(group)) {
            Map<String, List<Map>> groupMap = userApi.queryChildGroupByIds(group);
            if (!CommonUtils.isNullOrEmpty(groupMap)) {
                for (String groupId : groupMap.keySet()) {
                    List<Map> groupList = groupMap.get(groupId);
                    if (!CommonUtils.isNullOrEmpty(groupId)) {
                        groupIds.addAll(groupList.stream().map(m -> (String) m.get(Dict.ID)).collect(Collectors.toSet()));
                    }
                }
            }
        } else {
            groupIds = new HashSet<>(group);
        }
        List<FdevTask> fdevTasks = fdevTaskDao.queryByTerms(groupIds, stage, isIncludeChildren, isDefered);
        Set<String> demandIds = fdevTasks.stream().map(task -> task.getRqrmnt_no()).collect(Collectors.toSet());
        if (!CommonUtils.isNullOrEmpty(demandIds)) {
            List<Map<String, String>> demands = demandService.queryDemandByIds(demandIds);
            Map<String, String> demandIdNoMap = demands.stream().collect(Collectors.toMap(demand -> demand.get(Dict.ID), demand -> demand.get(Dict.OA_CONTACT_NO)));
            fdevTasks.forEach(task -> task.setDemandNo(demandIdNoMap.get(task.getRqrmnt_no())));
        }
        return fdevTasks;
    }

    /**
     * @param requestParam
     * @return
     * @throws Exception 2019年4月2日
     * @Desc 查询任务明细
     */
    @Override
    public Map queryTaskDetail(FdevTask requestParam) throws Exception {
        //查询任务明细
        //1，集成user模块，用户中英文名称，小组id和名称
        List<FdevTask> list = fdevTaskDao.queryAll(requestParam);
        if (CommonUtils.isNullOrEmpty(list)) {
            return null;
        }
        String projectId = list.get(0).getProject_id();
        //获取应用类型
        Map result = reBuildTask(list.get(0));
        FdevTask fdevTask = list.get(0);
        if (fdevTask.getNocodeInfoMap() != null) {
            Map<String, NoCodeInfo> nocodeInfoMap = fdevTask.getNocodeInfoMap();
            result.put("nocodeInfoMap", reBuildNocodeInfoMap(nocodeInfoMap));
        }

        if (CommonUtils.isNotNullOrEmpty(projectId)) {
            Map<String, Object> projectInfo = getProjectInfo(projectId, fdevTask.getApplicationType());
            result.put(Dict.ISTEST, CommonUtils.isNullOrEmpty(projectInfo.get(Dict.ISTEST)) ? "1" : projectInfo.get(Dict.ISTEST));
            result.put(Dict.GITLAB_ID, projectInfo.get(Dict.GITLAB_ID));
            result.put(Dict.GITLABURL, projectInfo.get(Dict.GITLABURL));
            result.put(Dict.APPCITYPE, CommonUtils.isNullOrEmpty(projectInfo.get(Dict.APPCITYPE)) ? "" : projectInfo.get(Dict.APPCITYPE));

        }
        //获取该任务是否需要release合并卡点
        if (releaseMergeFlag(fdevTask)) {
            result.put("releaseMergeFlag", "1");//需要卡点
        } else {
            result.put("releaseMergeFlag", "0");//直接过
        }
        return result;
    }

    /**
     * @param task
     * @return
     * @throws Exception
     */
    @Override
    public Map queryTestDetail(FdevTask task, Integer pageNum, Integer pageSize) throws Exception {
        List<FdevTask> list = fdevTaskDao.queryAll(task);
        if (CommonUtils.isNullOrEmpty(list)) {
            return null;
        }
        return reBuildTestInfo(list.get(0), pageNum, pageSize);
    }

    /**
     * @param task
     * @return
     * @throws Exception
     */
    @Override
    public Map queryDocDetail(FdevTask task, Integer pageNum, Integer pageSize) throws Exception {
        List<FdevTask> list = fdevTaskDao.queryAll(task);
        if (CommonUtils.isNullOrEmpty(list)) {
            return null;
        }
        Map result = CommonUtils.beanToMap(list.get(0));
        result.put(Dict.GROUP, group((String) result.get(Dict.GROUP)));
        Map tmp = new HashMap();
        List<Map<String, String>> newFileList = getNewFileList(result);
        tmp.put(Dict.COUNT, newFileList.size());
        //均不为空  且长度大于分页长度 分页
        if (!CommonUtils.isNullOrEmpty(newFileList) && !CommonUtils.isNullOrEmpty(pageNum) &&
                !CommonUtils.isNullOrEmpty(pageSize) && newFileList.size() > pageSize) {
            int start = Math.min((pageNum - 1) * pageSize, newFileList.size());
            int end = Math.min(start + pageSize, newFileList.size());
            newFileList = newFileList.subList(start, end);
            for (Map<String, String> newFile : newFileList) {
                if (CommonUtils.isNullOrEmpty(newFile.get(Dict.UPDATE_TIME))) newFile.put(Dict.UPDATE_TIME, "");
                if (CommonUtils.isNullOrEmpty(newFile.get(Dict.USER_ID))) newFile.put(Dict.USER_ID, "");
                if (CommonUtils.isNullOrEmpty(newFile.get(Dict.USER_NAME_EN))) newFile.put(Dict.USER_NAME_EN, "");
                if (CommonUtils.isNullOrEmpty(newFile.get(Dict.USER_NAME_CN))) newFile.put(Dict.USER_NAME_CN, "");
            }
        }

        tmp.put(Dict.DOC, newFileList);

        List resList = new ArrayList();
        ReviewRecord r = new ReviewRecord();
        r.setTaskId(task.getId());
        List<ReviewRecord> listr = reviewRecordDao.query(r);
        listr.forEach(n -> resList.add(n.getReviewStatus()));
        if (CommonUtils.isNullOrEmpty(resList)) {
            tmp.put(Dict.STATUS, "");
        } else {
            tmp.put(Dict.STATUS, resList.get(0));
        }
        return tmp;
    }


    /**
     * @param task
     * @return 不对任务阶段进行过滤
     * @throws Exception
     */
    @Override
    public FdevTask queryTaskAll(FdevTask task) throws Exception {
        //查询任务明细
        List<FdevTask> list = fdevTaskDao.queryAll(task);
        return list.get(0);
    }

    @Override
    public FdevTaskCollection saveFdevTaskCollection(FdevTaskCollection fdevTaskCollection) throws Exception {
        return fdevTaskDao.saveFdevTaskCollection(fdevTaskCollection);
    }

    @Override
    public List<Map> queryFdevTaskCollection(ArrayList ids) {
        return fdevTaskDao.queryFdevTaskCollection(ids);
    }

    @Override
    public FdevTaskCollection queryByJobId(FdevTaskCollection fdevTaskCollection) throws Exception {
        return fdevTaskDao.queryByJobId(fdevTaskCollection);
    }

    @Override
    public List<FdevTaskCollection> queryByTaskId(String task_id) throws Exception {
        return fdevTaskDao.queryByTaskId(task_id);
    }

    @Override
    public List<Map> queryBySubTask(FdevTaskCollection fdevTaskCollection) throws Exception {
        return fdevTaskDao.queryBySubTask(fdevTaskCollection);
    }


    @Override
    public FdevTaskCollection updateTaskCollection(FdevTaskCollection fdevTaskCollection) throws Exception {
        FdevTaskCollection ftc = fdevTaskDao.updateTaskCollection(fdevTaskCollection);
        //同步子任务

        String tc_id = fdevTaskCollection.getTask_id();
        if (!CommonUtils.isNotNullOrEmpty(tc_id)) {
            asyncService.syncSubTaskTester(tc_id, fdevTaskCollection.getTaskcollection());
        }
        return ftc;
    }

    /**
     * DESC : 集成各个平台信息汇总
     *
     * @param
     * @param li
     * @return
     * @throws Exception 2019年4月2日
     */
    public Map reBuildTask(FdevTask li) throws Exception {
        /*用户模块*/
        ArrayList<FdevTask> taskList = new ArrayList();
        taskList.add(li);
        Map users = this.getAllUserInfo(taskList);
        Map model = this.buildTaskInfo(li);
        model.putAll(this.rebuildAllUserInfo(users, li));

        /*sit对应信息
         *   merge
         *   *环境
         * */
        model.put("sit_merge_status", reBuildMergeInfo(li, Dict.SIT));
        /*uat对应信息
         *   merge   reBuildMergeInfo()
         *   *环境
         * */
        model.put("uat_merge_status", reBuildMergeInfo(li, Dict.UAT));

//        /*文档信息*/ 迁移到queryDocDetail接口中了
//        model.put("doc", getFileList(model));
        model.remove("doc");
        //兼容旧的设计还原审核。构造数据
        if (CommonUtils.isNullOrEmpty(model.get("designMap"))) {
            Map<String, List<Map<String, String>>> stringListMap = buildDesign(model);
            model.put("designMap", stringListMap);
            fdevTaskDao.updateDesign(stringListMap, li.getId());
            logger.info("更新了构造的数据参数如下:designMap{}", JSON.toJSONString(stringListMap, true));
        }

        return model;

    }

    private Map buildTaskInfo(FdevTask li) throws Exception {
        String group = li.getGroup();
        Map<String, List<String>> doc_uploader = li.getDoc_uploader();
        Map result = CommonUtils.beanToMap(li);
        //构造需求信息
        result.put("productionStatus", true);
        result.put("confirmShow", false);
        result.put("confirmDateShow", true);//所有任务打开上线确认书开关时都要选择日期
        Map demand = new HashMap();
        demand.put("oa_contact_no", "");
        demand.put("oa_contact_name", "");
        demand.put("ui_verify", false);
        result.put("demand", demand);
        /*查询需求信息*/
        Map map = new HashMap();
        if (CommonUtils.isNotNullOrEmpty(li.getRqrmnt_no())
                || CommonUtils.isNotNullOrEmpty(li.getFdev_implement_unit_no())) {
            map = demandService.queryByFdevNo(li.getRqrmnt_no(), li.getFdev_implement_unit_no());
        }
        if (!CommonUtils.isNullOrEmpty(map)) {
            Map demand_baseinfo = (Map) map.get(Dict.DEMAND_BASEINFO);
            Map implement_unit_info = (Map) map.get(Dict.IMPLEMENT_UNIT_INFO);
            if (null != demand_baseinfo) {
                demand.put("oa_contact_no", demand_baseinfo.get("oa_contact_no"));
                demand.put("oa_contact_name", demand_baseinfo.get("oa_contact_name"));
                demand.put("demand_type", demand_baseinfo.get("demand_type"));
            }
            String ipmpUnitNo = "";
            String otherDemandTaskNum = "";
            if (null != implement_unit_info) {
                demand.put("ui_verify", implement_unit_info.get("ui_verify"));
                ipmpUnitNo = implement_unit_info.get("ipmp_implement_unit_no") == null ? ipmpUnitNo : (String) implement_unit_info.get("ipmp_implement_unit_no");
                otherDemandTaskNum = implement_unit_info.get("other_demand_task_num") == null ? otherDemandTaskNum : (String) implement_unit_info.get("other_demand_task_num");
            }
            result.put("ipmp_implement_unit_no", ipmpUnitNo);
            result.put("other_demand_task_num", otherDemandTaskNum);
            result.put("demand", demand);
            result.put("implement_unit_info", implement_unit_info);
            if (null != demand_baseinfo && "business".equals(demand_baseinfo.get("demand_type"))) {
                String proWantWindow = (String) result.get("proWantWindow");
                if (proWantWindow != null && !"".equals(proWantWindow)) {
                    Calendar instance = Calendar.getInstance();
                    instance.setTime(CommonUtils.dateParse(proWantWindow, CommonUtils.DATE_PATTERN_F1));
                    String dateTime = PatrickKit.getDatePresentSomeWorkDay(instance, 12);
                    Date date = CommonUtils.dateParse(dateTime, CommonUtils.DATE_PATTERN_F1);
                    Date currentDate = new Date();
                    //用户测试完成日期不能为空
                    if (currentDate.getTime() > date.getTime() && !CommonUtils.isNullOrEmpty(li.getStart_rel_test_time())) {
                        result.put("confirmShow", true);
                    }
                    if (li.getTaskType() != null && li.getTaskType() == 1) {
                        result.put("confirmShow", false);
                    }
                    if ("0".equals(result.get("confirmBtn"))) {
                        result.put("productionStatus", false);
                    }
                }
            }
        }
        result.put(Dict.GROUP, group(group));
        if (null != doc_uploader) {
            result.put("doc_uploader", doc_uploader.keySet().toArray(new String[]{}));
        }
        /*测试信息*/
        String testSwitch = "";
        if (CommonUtils.isApp(li.getApplicationType())) {
            testSwitch = appApi.QueryTestSwitch(li.getProject_id());
        }

        if ("0".equals(testSwitch)) {
            //不涉及内测
            result.put("test_info", new HashMap<>());
        } else {
            //涉及内测
            Map testInfo = reBuildTestInfo(li, null, null);
            List<Map> testsers = new ArrayList<>();
            result.put(Dict.TESTER, testsers);
            if (testInfo != null) {
                testsers = (List<Map>) testInfo.remove("testers");
                result.put(Dict.TESTER, testsers);
            }
            result.put("test_info", testInfo);
        }
        return result;
    }

    private Map<String, NoCodeInfo> reBuildNocodeInfoMap(Map<String, NoCodeInfo> nocodeInfoMap) {
        for (String stage : nocodeInfoMap.keySet()) {
            NoCodeInfo noCodeInfo = nocodeInfoMap.get(stage);
            noCodeInfo.setNextStageStatus(true);
            Relator[] relators = noCodeInfo.getRelators();
            if (relators != null && relators.length > 0) {
                for (Relator r : relators) {
                    StringBuffer fileNameTemp = new StringBuffer();
                    if (r.getTaskStatus().equals(Dict.NOCODEINFO_NOT_FINISHED)) {
                        noCodeInfo.setNextStageStatus(false);
                    }
                    if (r.getFiles() != null && r.getFiles().size() > 0) {
                        List<Map<String, String>> rFiles = r.getFiles();
                        for (int i = 0; i < rFiles.size(); i++) {
                            if (i == rFiles.size() - 1) {
                                fileNameTemp.append(rFiles.get(i).get("fileName"));
                            } else
                                fileNameTemp.append(rFiles.get(i).get("fileName")).append(",");
                        }
                    }
                    r.setFileName(fileNameTemp.toString());
                }
            }
            noCodeInfo.setRelators(relators);
            nocodeInfoMap.put(stage, noCodeInfo);
        }
        return nocodeInfoMap;
    }


    /**
     * @param li
     * @return
     * @throws Exception 2019年4月2日
     * @Desc 测试信息
     */
    public Map reBuildTestInfo(FdevTask li, Integer pageNum, Integer pageSize) throws Exception {
        Map param = new HashMap();
        param.put("taskNo", li.getId());
        param.put("page", pageNum);
        param.put("pageSize", pageSize);
        //测试平台返回结果
        Map testResult = new HashMap();
        testResult.put("stageStatus", "-1");
        testResult.put("testPlan", new ArrayList());
        testResult.put("error", "工单不存在!!!");
        try {
            testResult = iTestApi.queryTest(param);
        } catch (Exception e) {
            testResult.put("stageStatus", "-1");
            testResult.put("testPlan", new ArrayList());
            testResult.put("error", e.getMessage());
        }
        return testResult;
    }


    /**
     * 获取文档列表
     *
     * @param task
     * @return
     */
    public List getFileList(Map task) throws Exception {
        String groupName = (String) ((Map) task.get(Dict.GROUP)).get(Dict.NAME);//组名
        String taskName = (String) task.get(Dict.NAME);//任务名字
        String task_id = task.get(Dict.ID).toString();//任务id
        String encoderTaskName = URLEncoder.encode(taskName, "utf-8").replaceAll("\\+", "%20").replaceAll("%", "a");
        // 第一种 第一种为原始项目名，用于兼容历史任务附件。/taskName(项目名)-taskId(项目id)/文件类型/文件
        String pathFirst = docResource + "/" + groupName + "/" + taskName + "-" + task_id;
        Future<List<Map<String, String>>> futureFirst = asyncTaskUnit.dealTaskDocList("fdev-task", pathFirst);
        //第二种为对项目名做URLEncoder编码
        String pathSecond = docResource + "/" + groupName + "/" + encoderTaskName + "-" + task_id;
        Future<List<Map<String, String>>> futureSecond = null;
        if (!pathFirst.equals(pathSecond)) {
            futureSecond = asyncTaskUnit.dealTaskDocList("fdev-task", pathSecond);
        }
        //第三种为在第二种的基础上取前10位
        Future<List<Map<String, String>>> futureThird = null;
        if (encoderTaskName.length() > 10) {
            String pathThird = docResource + "/" + groupName + "/" + encoderTaskName.substring(0, 10) + "-" + task_id;
            futureThird = asyncTaskUnit.dealTaskDocList("fdev-task", pathThird);
        }
        List<Map<String, String>> list = new ArrayList<>();
        list.addAll(futureFirst.get());
        if (futureSecond != null)
            list.addAll(futureSecond.get());
        if (futureThird != null)
            list.addAll(futureThird.get());
        list = list.stream().filter((map) -> !map.isEmpty()).collect(Collectors.toList());
        return list;
    }

    /**
     * 新获取文档列表
     *
     * @param task
     * @return
     */
    public List getNewFileList(Map task) throws Exception {
        FdevTask fdevTask = new FdevTask();
        fdevTask.setId((String) task.get(Dict.ID));
        List<FdevTask> fdevTasks = fdevTaskDao.queryAll(fdevTask);
        FdevTask fdevTask1 = fdevTasks.get(0);
        return Optional.ofNullable(fdevTask1.getNewDoc()).orElse(Lists.newArrayList());
    }


    /**
     * 查询文件信息
     *
     * @param task
     * @return
     * @throws Exception 2019年4月2日
     * @Desc
     */
    public ArrayList reBuildFileInfo(Map task) throws Exception {
        try {
            Map param = new HashMap();
            Map param1 = new HashMap();
            Map param2 = new HashMap();
            String group_name = (String) ((Map) task.get(Dict.GROUP)).get(Dict.NAME);
            String task_name = (String) task.get(Dict.NAME);
            String encode = URLEncoder.encode(task_name, "utf-8");
            String filePath = encode.replaceAll("%", "a");
            String filePath1 = "";
            if (filePath.length() > 10) {
                filePath1 = filePath.substring(0, 10);
            }
            String task_id = task.get(Dict.ID).toString();
            String path = group_name + "/" + task_name + "-" + task_id;
            String path1 = group_name + "/" + filePath + "-" + task_id;
            param.put(Dict.PATH, path);
            param.put(Dict.TOKEN, token);
            ArrayList arrayList = appApi.queryDoc(param);
            if (!task_name.equals(encode)) {
                param1.put(Dict.PATH, path1);
                param1.put(Dict.TOKEN, token);
                ArrayList arrayList1 = appApi.queryDoc(param1);
                arrayList.addAll(arrayList1);
            }
            if (CommonUtils.isNotNullOrEmpty(filePath1)) {
                String path2 = group_name + "/" + filePath1 + "-" + task_id;
                param2.put(Dict.PATH, path2);
                param2.put(Dict.TOKEN, token);
                ArrayList arrayList2 = appApi.queryDoc(param2);
                arrayList.addAll(arrayList2);
            }
            return arrayList;
        } catch (Exception e) {
            return new ArrayList();
        }
    }


    /**
     * @param li
     * @param type 查询sit 或 uat
     * @return
     * @throws Exception 2019年4月2日
     * @Desc 查 merge状态
     */
    public Map reBuildMergeInfo(FdevTask li, String type) throws Exception {
        Map mergeStatus = new HashMap();
        try {
            String sitMergeId = li.getSit_merge_id();
            String uatMergeId = li.getUat_merge_id();

            String id = li.getProject_id();
            Map param = new HashMap();
            param.put(Dict.ID, id);//应用ID
            switch (type) {
                case Dict.SIT:
                    if (StringUtils.isEmpty(sitMergeId)) {
                        mergeStatus.put(Dict.STATUS_CODE, "0");
                        mergeStatus.put(Dict.STATUS_MEAN, "未发起合并");
                        return mergeStatus;
                    }
                    param.put(Dict.IID, sitMergeId);
                    break;
                case Dict.UAT:
                    if (StringUtils.isEmpty(uatMergeId)) {
                        mergeStatus.put(Dict.STATUS_CODE, "0");
                        mergeStatus.put(Dict.STATUS_MEAN, "未发起合并");
                        return mergeStatus;
                    }
                    param.put(Dict.IID, uatMergeId);
                    break;
                default:
                    throw new FdevException(ErrorConstants.PARAM_ERROR);
            }
            param.put(Dict.TOKEN, token);
            Map<String, Object> projectInfo = getProjectInfo(id, li.getApplicationType());
            mergeStatus = queryMergeInfo((String) projectInfo.get(Dict.GITLAB_ID), (String) param.get(Dict.IID));
        } catch (FdevException e1) {
            throw e1;
        } catch (Exception e2) {
            throw e2;
        }
        return mergeStatus;
    }

    /**
     * 查询合并请求状态和流水线执行信息
     *
     * @param gitlabProjectId
     * @param mergeId
     * @return
     */
    @Override
    public Map<String, String> queryMergeInfo(String gitlabProjectId, String mergeId) {
        Map<String, Object> mergeInfo = gitApiService.queryMergeInfo(gitlabProjectId, mergeId);
        String status = (String) mergeInfo.get(Dict.STATE);
        Object merged_by = mergeInfo.get(Dict.MERGED_BY);
        Object closed_by = mergeInfo.get(Dict.CLOSED_BY);
        Object pipeline = mergeInfo.get(Dict.PIPELINE);
        Map<String, String> mergeStatus = new HashMap();
        // 待合并
        if ("opened".equals(status) && CommonUtils.isNullOrEmpty(closed_by) && CommonUtils.isNullOrEmpty(merged_by)) {
            mergeStatus.put(Dict.STATUS_CODE, "1");
            mergeStatus.put(Dict.STATUS_MEAN, "待合并");
        }
        // 合并请求被关闭，可重新发起合并请求
        if (Dict.CLOSED.equals(status) && !CommonUtils.isNullOrEmpty(closed_by)
                && CommonUtils.isNullOrEmpty(merged_by)) {
            mergeStatus.put(Dict.STATUS_CODE, "5");
            mergeStatus.put(Dict.STATUS_MEAN, "合并请求被关闭，可重新发起合并请求");
        }
        if (Dict.MERGED.equals(status)) {
            if (Dict.RUNNING.equals(pipeline)) {
                mergeStatus.put(Dict.STATUS_CODE, "2");
                mergeStatus.put(Dict.STATUS_MEAN, "已合并，pipline进行中");
            } else if (Dict.SUCCESS.equals(pipeline)) {
                mergeStatus.put(Dict.STATUS_CODE, "3");
                mergeStatus.put(Dict.STATUS_MEAN, "已合并，pipline构建成功");
            } else {
                mergeStatus.put(Dict.STATUS_CODE, "4");
                mergeStatus.put(Dict.STATUS_MEAN, "已合并，pipline构建失败");
            }
        }
        return mergeStatus;
    }

    /**
     * DESC : 完善用户信息和小组信息
     *
     * @param li
     * @return
     * @throws Exception 2019年4月2日
     */
    @Override
    public Map reBuildUser(FdevTask li) throws Exception {
        //任务负责人
        String[] master = li.getMaster();
        String[] spdbMaster = li.getSpdb_master();
        String[] developer = li.getDeveloper();
        String[] concern = li.getConcern();
        String creator = li.getCreator();
        String group = li.getGroup();
        String reviewer = li.getReviewer();
        Map<String, List<String>> doc_uploader = li.getDoc_uploader();
        Map result = CommonUtils.beanToMap(li);
        //构造需求信息
        result.put("productionStatus", true);
        result.put("businessTestReportFlag", true);//是否已有业测报告标志，只卡业务需求下的任务
        result.put("confirmShow", false);
        Map demand = new HashMap();
        demand.put("oa_contact_no", "");
        demand.put("oa_contact_name", "");
        demand.put("ui_verify", false);
        result.put("demand", demand);
        /*查询需求信息*/
        Map map = new HashMap();
        if (CommonUtils.isNotNullOrEmpty(li.getRqrmnt_no())
                || CommonUtils.isNotNullOrEmpty(li.getFdev_implement_unit_no())) {
            map = demandService.queryByFdevNo(li.getRqrmnt_no(), li.getFdev_implement_unit_no());
        }
        if (!CommonUtils.isNullOrEmpty(map)) {
            Map demand_baseinfo = (Map) map.get(Dict.DEMAND_BASEINFO);
            Map implement_unit_info = (Map) map.get(Dict.IMPLEMENT_UNIT_INFO);
            if (null != demand_baseinfo) {
                demand.put("oa_contact_no", demand_baseinfo.get("oa_contact_no"));
                demand.put("oa_contact_name", demand_baseinfo.get("oa_contact_name"));
                demand.put("demand_type", demand_baseinfo.get("demand_type"));
            }
            String ipmpUnitNo = "";
            if (null != implement_unit_info) {
                demand.put("ui_verify", implement_unit_info.get("ui_verify"));
                ipmpUnitNo = implement_unit_info.get("ipmp_implement_unit_no") == null ? ipmpUnitNo : (String) implement_unit_info.get("ipmp_implement_unit_no");
            }
            result.put("ipmp_implement_unit_no", ipmpUnitNo);
            result.put("demand", demand);
            if (null != demand_baseinfo && "business".equals(demand_baseinfo.get("demand_type"))) {
                //查询需求关联文档
                List<Map<String, String>> docInfo = demandService.showDemandDoc(li.getRqrmnt_no());
                if (CommonUtils.isNullOrEmpty(docInfo) || !docInfo.stream().anyMatch(doc -> "businessTestReport".equals(doc.get("doc_type")))) {
                    result.put("businessTestReportFlag", false);
                }
                String proWantWindow = (String) result.get("proWantWindow");
                if (proWantWindow != null && !"".equals(proWantWindow)) {
                    Calendar instance = Calendar.getInstance();
                    instance.setTime(CommonUtils.dateParse(proWantWindow, CommonUtils.DATE_PATTERN_F1));
                    String dateTime = PatrickKit.getDatePresentSomeWorkDay(instance, 12);
                    Date date = CommonUtils.dateParse(dateTime, CommonUtils.DATE_PATTERN_F1);
                    Date currentDate = new Date();
                    //用户测试完成日期不能为空
                    if (currentDate.getTime() > date.getTime() && !CommonUtils.isNullOrEmpty(li.getStart_rel_test_time())) {
                        result.put("confirmShow", true);
                    }
                    if (li.getTaskType() != null && li.getTaskType() == 1) {
                        result.put("confirmShow", false);
                    }
                    if ("0".equals(result.get("confirmBtn")) || CommonUtils.isNullOrEmpty(docInfo)
                            || !docInfo.stream().anyMatch(doc -> "launchConfirm".equals(doc.get("doc_type")))) {
                        result.put("productionStatus", false);
                    }
                    Map RqrmntInfo = ReleaseTaskApi.queryRqrmntInfoByTaskNo(li.getId());
                    if (!CommonUtils.isNullOrEmpty(RqrmntInfo)) {
                        String tag = (String) RqrmntInfo.get(Dict.TAG);
                        if (Constants.RQRMNT_TAG_CHIEF.equals(tag) || Constants.RQRMNT_TAG_LENGTH.equals(tag)) {
                            result.put("confirmDateShow", true);
                        }
                    }
                }
            }
        }
        result.put(Dict.MASTER, name(master));
        result.put(Dict.SPDB_MASTER, name(spdbMaster));
        result.put(Dict.DEVELOPER, name(developer));
        result.put(Dict.CONCERN, name(concern));
        result.put(Dict.GROUP, group(group));
        result.put(Dict.CREATOR, name(creator));
        result.put("reviewer", name(reviewer));
        if (null != doc_uploader) {
            result.put("doc_uploader", doc_uploader.keySet().toArray(new String[]{}));
        }
        /*测试信息*/
        String testSwitch = "";
        if (CommonUtils.isApp(li.getApplicationType())) {
            testSwitch = appApi.QueryTestSwitch(li.getProject_id());
        }

        if ("0".equals(testSwitch)) {
            //不涉及内测
            result.put("test_info", new HashMap<>());
        } else {
            //涉及内测
            Map testInfo = reBuildTestInfo(li, null, null);
            List<Map> testsers = new ArrayList<>();
            result.put(Dict.TESTER, testsers);
            if (testInfo != null) {
                testsers = (List<Map>) testInfo.remove("testers");
                result.put(Dict.TESTER, testsers);
            }
            result.put("test_info", testInfo);
        }
        return result;
    }

    /**
     * @param name
     * @return
     * @throws Exception 2019年4月2日
     * @Desc 完善用户信息方法
     */

    public Map name(String name) throws Exception {
        Map user = new HashMap();
        if (null == name || "".equals(name)) {
            return user;
        }
        Map param = new HashMap();
        param.put(Dict.ID, name);
        Map rUser = userApi.queryTaskDetailUser(param);
        user.put(Dict.ID, name);
        user.put(Dict.USER_NAME_EN, rUser.get(Dict.USER_NAME_EN) == null ? "" : rUser.get(Dict.USER_NAME_EN));
        user.put(Dict.USER_NAME_CN, rUser.get(Dict.USER_NAME_CN) == null ? "" : rUser.get(Dict.USER_NAME_CN));
        return user;
    }

    public ArrayList name(String[] names) throws Exception {
        ArrayList result = new ArrayList();
        if (null == names || names.length <= 0) {
            return result;
        }
        for (String name : names) {
            Map user = name(name);
            result.add(user);
        }
        return result;
    }

    /**
     * @param group
     * @return
     * @throws Exception 2019年4月2日
     * @Desc 完善小组信息方法
     */

    public Map group(String group) throws Exception {
        if (null == group || "".equals(group)) {
            return null;
        }
        Map result = new HashMap();
        Map param = new HashMap();
        param.put(Dict.ID, group);
        Map rGroup = (Map) userApi.queryGroup(param);
        result.put(Dict.ID, rGroup.get(Dict.ID));
        result.put(Dict.NAME, rGroup.get(Dict.NAME));
        result.put("fullName", rGroup.get("fullName"));
        return result;
    }

    @Override
    public Map queryTaskDetailForProductions(FdevTask requestParam) throws Exception {
        List<FdevTask> list = fdevTaskDao.queryAll(requestParam);
        if (CommonUtils.isNullOrEmpty(list)) {
            return null;
        }
        return reBuildUser(list.get(0));
    }


    public List<FdevTask> queryTaskCByUnitNo(String unitno) throws Exception {
        return fdevTaskDao.queryTaskCByUnitNo(unitno);
    }


    @Override
    public FdevTask deleteTask(String id) throws Exception {
        FdevTask task = update(new FdevTask.FdevTaskBuilder().stage(Dict.TASK_STAGE_ABORT).id(id).init());
        iTestApi.deleteTaskIssue(id);
        return task;
    }

    @Override
    public void removeTask(String id) throws Exception {
        fdevTaskDao.removeTask(id);
    }

    /**
     * 根据小组查询各阶段任务数量
     *
     * @param ids 各组id的集合
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Map> queryTaskNumByGroup(List<String> ids, boolean includeChild) throws Exception {
        Map<String, Map> result = new HashMap<>();
        if (includeChild) {
            GroupTree groupTree = groupTreeUtil.getGroupTree();
            for (String id : ids) {
                Map<String, Object> stageNums;
                stageNums = fdevTaskDao.queryTaskNum(id, groupTree);
                result.put(id, stageNums);
            }
        } else {
            for (String id : ids) {
                Map<String, Object> stageNums;
                stageNums = fdevTaskDao.queryTaskNum(id, null);
                result.put(id, stageNums);
            }
        }
        return result;
    }

    /**
     * 查询各组时间段任务数量
     *
     * @param ids 各组id的集合
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Map> queryTaskNumByGroupDate(List<String> ids, String startDate, String end_date, boolean includeChild) throws Exception {
        Map<String, Map> result = new HashMap<>();
        if (!CommonUtils.isNullOrEmpty(end_date)) {
            Date endDate = CommonUtils.dateParse(end_date, CommonUtils.DATE_PATTERN_F1);
            Date today = new Date();
            if (endDate.after(today)) {
                end_date = CommonUtils.dateFormat(today, CommonUtils.DATE_PATTERN_F1);
            }
        }
        if (!includeChild) {
            for (String id : ids) {
                Map<String, Object> stageNums = fdevTaskDao.queryOutputTaskbyGroup(null, id, startDate, end_date);
                result.put(id, stageNums);
            }
        } else {
            GroupTree groupTree = groupTreeUtil.getGroupTree();
            for (String id : ids) {
                Map<String, Object> stageNums = fdevTaskDao.queryOutputTaskbyGroup(groupTree, id, startDate, end_date);
                result.put(id, stageNums);
            }
        }
        return result;
    }

    /**
     * 根据人员查询各阶段任务数量,已废弃
     *
     * @param requestParams 查询的条件，人员查询
     * @return 返回以人员的list，每个人员的任务卡片信息（例如有多少个sit、uat等）
     */
    @Override
    public Map<String, Map> queryTaskNumByMember(Map requestParams) {
        if (CommonUtils.isNullOrEmpty(requestParams))
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"参数为空"});
        Map<String, Map> result = new HashMap<>();
        List ids = (List) requestParams.get(Dict.IDS);
        List roles = (List) requestParams.get(Dict.ROLES);
        for (Object id : ids) {
            Map<String, Object> stageNums = fdevTaskDao.queryTaskNumByMember((String) id, roles);
            result.put((String) id, stageNums);
        }
        return result;
    }

    /**
     * 根据人员查询各阶段任务数量
     *
     * @param requestParams 查询的条件，人员查询
     * @return 返回以人员的list，每个人员的任务卡片信息（例如有多少个sit、uat等）
     */
    @Override
    public List queryTaskCardByMember(Map requestParams) {
        if (CommonUtils.isNullOrEmpty(requestParams))
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"参数为空"});
        List<Map> result = new ArrayList<>();
        List ids = (List) requestParams.get(Dict.IDS);
        Map<String, Object> userInfo = getUserInfoByIds(ids);
        List roles = (List) requestParams.get(Dict.ROLES);
        //总需求id
        List<String> rqrmntList = new ArrayList<>();
        for (Object id : ids) {
            Map<String, Object> stageNums = fdevTaskDao.queryTaskCardByMember((String) id, roles);
            Map<String, Object> user = (Map<String, Object>) userInfo.get(id);
            handlerTackCardInfo(stageNums, user);
            result.add(stageNums);
            if (!CommonUtils.isNullOrEmpty(stageNums.get("rqrmnNumList"))) {
                rqrmntList.addAll((Collection<? extends String>) stageNums.get("rqrmnNumList"));
            }
        }
        rqrmntList = rqrmntList.stream().distinct().collect(Collectors.toList());

        if (!CommonUtils.isNullOrEmpty(result)) {
            result.get(0).put("rqrmnNum", rqrmntList.size());
        }
        return result;
    }

    /**
     * 任务暂缓恢复
     *
     * @param ids
     * @param taskSpecialStatus
     * @return
     */
    @Override
    public Long deferOrRecoverTask(List<String> ids, Integer taskSpecialStatus) {
        if (taskSpecialStatus == 3) {
            for (String id : ids) {
                Map map = demandService.queryByFdevNo(null, id);
                Map implement_unit_info = (Map) map.get(Dict.IMPLEMENT_UNIT_INFO);
                fdevTaskDao.updateByFdevImplementUnitNo(id, implement_unit_info);

                FdevTask task = new FdevTask();
                task.setFdev_implement_unit_no(id);
                asyncService.updateRqrmntWhenStageChanged(task, false);
            }
        }
        return fdevTaskDao.deferOrRecoverTask(ids, taskSpecialStatus);
    }

    @Override
    public List<Map> queryTaskDetailByfdevImplementUnitNo(List<String> fdev_implement_unit_no_list) throws Exception {
        //查询任务明细
        //1，集成user模块，用户中英文名称，小组id和名称
        List<FdevTask> list = fdevTaskDao.queryTaskDetailByfdevImplementUnitNo(fdev_implement_unit_no_list);
        List<Map> resultList = new ArrayList<>();
        if (CommonUtils.isNullOrEmpty(list)) {
            return resultList;
        }
        for (int i = 0; i < list.size(); i++) {
            FdevTask fdevTask = list.get(i);
            Map result = CommonUtils.beanToMap(fdevTask);
            if (fdevTask.getNocodeInfoMap() != null) {
                Map<String, NoCodeInfo> nocodeInfoMap = fdevTask.getNocodeInfoMap();
                result.put("nocodeInfoMap", reBuildNocodeInfoMap(nocodeInfoMap));
            }
            resultList.add(result);
        }
        return resultList;
    }

    @Override
    public List<Map> queryTaskByUnitNos(List<String> fdev_implement_unit_no_list) throws Exception {
        //查询任务明细
        //1，集成user模块，用户中英文名称，小组id和名称
        List<FdevTask> taskList = fdevTaskDao.queryNotDiscardTask(fdev_implement_unit_no_list);
        return rebuildUserAndGroup(taskList);
    }

    @Override
    public List<FdevTask> queryTaskNumByGroupinAll(String group, GroupTree groupTree, List stages) {
        return fdevTaskDao.queryTaskNumByGroupinAll(group, groupTree, stages);
    }

    @Override
    public List<FdevTask> queryTaskByreadmine(String group) {
        List<FdevTask> fdevTasks = fdevTaskDao.queryTaskByreadmine(group);
        return fdevTasks.stream().filter(r -> (r.getRedmine_id() != null && r.getPlan_inner_test_time() != null && r.getStart_inner_test_time() != null)).collect(Collectors.toList());
    }

    @Override
    public Map judgeByfdevImplementUnitNo(String demand_id, List<String> ids) throws Exception {
        List<FdevTask> list = fdevTaskDao.queryTaskGroupByRqrmnt(demand_id);
        Map<String, Boolean> result = new HashMap<>();
        if (CommonUtils.isNullOrEmpty(list)) {
            result.put("isDelete", true);
        } else {
            result.put("isDelete", false);
        }
        result.put("isDefer", true);
        for (FdevTask fdevTask : list) {
            Map params = new HashMap<>();
            params.put("task_id", fdevTask.getId());
            Map map = ReleaseTaskApi.queryDetailByTaskId(params);
            if (!CommonUtils.isNullOrEmpty(map)) {
                String task_status = (String) map.get("task_status");
                if (null != task_status && (task_status.equals("0") || task_status.equals("1") || task_status.equals("6"))) {
                    result.put("isDefer", false);
                    break;
                }
            }
            params.put("type", "3");
            map = ReleaseTaskApi.queryDetailByTaskId(params);
            if (!CommonUtils.isNullOrEmpty(map)) {
                String task_status = (String) map.get("task_status");
                if (null != task_status && (task_status.equals("0") || task_status.equals("1") || task_status.equals("6"))) {
                    result.put("isDefer", false);
                    break;
                }
            }
        }
        return result;
    }


    /**
     * 调用用户模块的queryByUserCoreData来获取用户信息
     *
     * @param ids
     * @return
     */
    private Map<String, Object> getUserInfoByIds(List ids) {
        LinkedHashMap result = null;
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("ids", ids);
            param.put(Dict.REST_CODE, "queryByUserCoreData");
            result = (LinkedHashMap) restTransport.submit(param);
        } catch (Exception e) {
            logger.error("task error>>>get user info failed>>>>调用用户模块失败，通过ids获取批量用户信息失败");
        }
        return result;
    }

    /**
     * 处理组装返回的map
     *
     * @param stageNums taskCard信息
     * @param user      id对应的用户信息
     */
    private void handlerTackCardInfo(Map stageNums, Map user) {
        String user_name_en = (String) user.get("user_name_en");
        String user_name_cn = (String) user.get("user_name_cn");
        stageNums.put("name_en", user_name_en);
        stageNums.put("name_cn", user_name_cn);
    }


    /**
     * 根据应用查询各阶段任务数量
     *
     * @param appIds 应用id的集合
     * @return
     * @throws Exception
     */
    @Override

    public Map<String, Map> queryTaskNumByApp(List<String> appIds) throws Exception {
        Map<String, Map> result = new HashMap<>();
        for (String id : appIds) {
            Map<String, Object> stageNums = fdevTaskDao.queryTaskNumByApp(id);
            result.put(id, stageNums);
        }
        return result;
    }


    /**
     * 查询各组近6周的任务数量
     *
     * @param ids 各组id的集合
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Map> queryTaskNum(List<String> ids, boolean includeChild) throws Exception {
        Map<String, Map> result = new HashMap<>();
        List<String> lastSixWeek = CommonUtils.getLastSixWeek(new Date());
        if (!includeChild) {
            for (String groupid : ids) {
                int num = 0;
                Map<String, Integer> nums = new HashedMap();
                for (int i = 0; i <= 5; i++) {
                    num = fdevTaskDao.queryTaskNum(groupid, "", lastSixWeek.get(i), null);
                    nums.put(lastSixWeek.get(i), num);
                }
                result.put(groupid, nums);
            }
        } else {
            GroupTree groupTree = groupTreeUtil.getGroupTree();
            for (String groupid : ids) {
                int num = 0;
                Map<String, Integer> nums = new HashedMap();
                for (int i = 0; i <= 5; i++) {
                    num = fdevTaskDao.queryTaskNum(groupid, "", lastSixWeek.get(i), groupTree);
                    nums.put(lastSixWeek.get(i), num);
                }
                result.put(groupid, nums);
            }
        }
        return result;
    }

    @Override
    public FdevTaskCollection deleteMainTask(String id) throws Exception {
        return fdevTaskDao.deleteMainTask(id);
    }

    @Override
    public Map queryTaskAndRedmineNo() throws Exception {
        return fdevTaskDao.queryTaskAndRedmineNo();
    }

    @Override
    public List updateBulkTaskForRelease(Map data) {
        List<String> result = new ArrayList<>();
        try {
            for (Object key : data.keySet()) {
                List<FdevTask> tmp = this.query(new FdevTask.FdevTaskBuilder().id((String) key).init());
                if (!CommonUtils.isNullOrEmpty(tmp)) {
                    FdevTask task = tmp.get(0);
                    Map tmpMap = (Map) data.get(key);
                    if (task != null && (Dict.TASK_STAGE_REL.equals(task.getStage()))) {
                        result.add((String) key);
                        fdevTaskDao.update(new FdevTask.FdevTaskBuilder().id((String) key).stage(Dict.TASK_STAGE_PRODUCTION).
                                fire_time((tmpMap == null ? null : (String) tmpMap.get(Dict.FIRE_TIME))).init());
                    }
                }
            }
        } finally {
            return result;
        }
    }

    @Override
    public List queryTaskByRqrmnt(String rqrmnt) throws Exception {
        List<FdevTask> taskList = fdevTaskDao.queryTaskGroupByRqrmnt(rqrmnt);
        //组合人员和小组信息
        return rebuildUserAndGroup(taskList);
    }

    /**
     * 组合人员和小组信息
     *
     * @param taskList
     * @return
     * @throws Exception
     */
    public List<Map> rebuildUserAndGroup(List<FdevTask> taskList) throws Exception {
        if (CommonUtils.isNullOrEmpty(taskList)) {
            return new ArrayList<>();
        }
        //批量查询所有相关人员的信息
        Set<String> taskUserIds = new HashSet<>();
        Set<String> taskGroupIds = new HashSet<>();
        List<Map> resultList = new ArrayList<>();
        for (FdevTask task : taskList) {
            if (!CommonUtils.isNullOrEmpty(task.getCreator())) {
                taskUserIds.add(task.getCreator());
            }
            if (!CommonUtils.isNullOrEmpty(task.getDeveloper())) {
                taskUserIds.addAll(Arrays.asList(task.getDeveloper()));
            }
            if (!CommonUtils.isNullOrEmpty(task.getMaster())) {
                taskUserIds.addAll(Arrays.asList(task.getMaster()));
            }
            if (!CommonUtils.isNullOrEmpty(task.getSpdb_master())) {
                taskUserIds.addAll(Arrays.asList(task.getSpdb_master()));
            }
            if (!CommonUtils.isNullOrEmpty(task.getTester())) {
                taskUserIds.addAll(Arrays.asList(task.getTester()));
            }
            if (!CommonUtils.isNullOrEmpty(task.getReviewer())) {
                taskUserIds.add(task.getReviewer());
            }
            if (!CommonUtils.isNullOrEmpty(task.getConcern())) {
                taskUserIds.addAll(Arrays.asList(task.getConcern()));
            }
            if (!CommonUtils.isNullOrEmpty(task.getGroup())) {
                taskGroupIds.add(task.getGroup());
            }
        }
        //批量查询人员信息
        Map<String, Map> userMap = userUtils.getUserByIds(taskUserIds);
        //批量查询小组信息
        List<Map> groupList = userApi.queryGroupByIds(new ArrayList<>(taskGroupIds));
        //将小组信息和id对应起来
        Map<String, Map> groupMap = new HashMap<>();
        for (Map group : groupList) {
            groupMap.put((String) group.get(Dict.ID), group);
        }
        //设置人员姓名和小组名称
        for (FdevTask task : taskList) {
            Map<String, Object> taskMap = CommonUtils.beanToMap(task);
            taskMap.put(Dict.MASTER, fdevTaskUtils.getUserName(task.getMaster(), userMap));
            taskMap.put(Dict.SPDB_MASTER, fdevTaskUtils.getUserName(task.getSpdb_master(), userMap));
            taskMap.put(Dict.DEVELOPER, fdevTaskUtils.getUserName(task.getDeveloper(), userMap));
            taskMap.put(Dict.CONCERN, fdevTaskUtils.getUserName(task.getConcern(), userMap));
            taskMap.put(Dict.CREATOR, fdevTaskUtils.getUserName(task.getCreator(), userMap));
            taskMap.put(Dict.REVIEWER, fdevTaskUtils.getUserName(task.getReviewer(), userMap));
            taskMap.put(Dict.TESTER, fdevTaskUtils.getUserName(task.getTester(), userMap));
            taskMap.put(Dict.GROUP, fdevTaskUtils.getGroupInfo(task.getGroup(), groupMap));
            if (!CommonUtils.isNullOrEmpty(task.getDoc_uploader())) {
                taskMap.put(Dict.DOCUPLOADER, task.getDoc_uploader().keySet().toArray(new String[]{}));
            }
            //添加延期标识
            if (!CommonUtils.isNullOrEmpty(isDelay(task))) {
                taskMap.put(Dict.DELAYFLAG, true);
            }
            resultList.add(taskMap);
        }
        return resultList;
    }


    @Override
    public List queryTaskByRqrmntRQR(List ids, List roles, boolean priority) throws Exception {
        List<Map> reList = new ArrayList<>();
        Map rqrType = (Map) rqrTypeMapping(priority).get("rqrType");
        for (Object id : ids) {
            List<FdevTask> list = fdevTaskDao.queryTaskNumRQR((String) id, roles);
            Map param = new HashMap();
            param.put(Dict.ID, id);
            Map user = userApi.queryUser(param);
            Map result = new HashMap();
            List<Integer> num = new ArrayList();
            num.add(0, 0);
            num.add(1, 0);
            num.add(2, 0);
            result.put(Dict.TASK_STAGE_TODO, ((ArrayList<Integer>) num).clone());
            result.put(Dict.TASK_STAGE_DEVELOP, ((ArrayList<Integer>) num).clone());
            result.put(Dict.TASK_STAGE_SIT, ((ArrayList<Integer>) num).clone());
            result.put(Dict.TASK_STAGE_UAT, ((ArrayList<Integer>) num).clone());
            result.put(Dict.TASK_STAGE_REL, ((ArrayList<Integer>) num).clone());
            result.put(Dict.DEFERTASK, ((ArrayList<Integer>) num).clone());

            for (FdevTask fdevTask : list) {
                if (CommonUtils.isNullOrEmpty(fdevTask.getTaskSpectialStatus()) || fdevTask.getTaskSpectialStatus().equals(3)) {
                    switch (fdevTask.getStage()) {
                        case Dict.TASK_STAGE_CREATE_INFO:
                        case Dict.TASK_STAGE_CREATE_APP:
                        case Dict.TASK_STAGE_CREATE_FEATURE:
                            result = accumulate(fdevTask, rqrType, result, Dict.TASK_STAGE_TODO);
                            break;
                        case Dict.TASK_STAGE_PRODUCTION:
                            break;
                        default:
                            result = accumulate(fdevTask, rqrType, result, fdevTask.getStage());
                            break;
                    }
                } else {
                    result = accumulate(fdevTask, rqrType, result, Dict.DEFERTASK);
                }
            }
            int yw = 0;
            int xq = 0;
            int rc = 0;
            for (Object key : result.keySet()) {
                List<Integer> tmp = (List<Integer>) result.get(key);
                result.put(key, tmp.get(0) + "+" + tmp.get(1) + "+" + tmp.get(2) + "=" + (tmp.get(0) + tmp.get(1) + tmp.get(2)));
                yw += tmp.get(0);
                xq += tmp.get(1);
                rc += tmp.get(2);
            }
            int sum = yw + xq + rc;
            result.put("sum", yw + "+" + xq + "+" + rc + "=" + sum);
            result.put("company", ((Map) (user.get("company"))).get(Dict.NAME));
            result.put("group", ((Map) (user.get("group"))).get(Dict.NAME));
            result.put(Dict.NAME, user.get(Dict.USER_NAME_CN));
            result.put("account", user.get(Dict.USER_NAME_EN));
            reList.add(result);
        }
        return reList;
    }

    private Map<String, String> accumulate(FdevTask fdevTask, Map<String, String> record) {
        String taskStage = fdevTask.getStage();
        String uat_test_time = fdevTask.getUat_test_time();
        if (Dict.TASK_STAGE_SIT.equals(taskStage) && !CommonUtils.isNullOrEmpty(uat_test_time)) {
            taskStage = Dict.TASK_STAGE_UAT;
        }
        if (record.keySet().contains(fdevTask.getRqrmnt_no())) {
            // 已记录
            String recordStage = CommonUtils.minStage(record.get(fdevTask.getRqrmnt_no()), taskStage);
            if (recordStage.equals(record.get(fdevTask.getRqrmnt_no()))) {
                return record;
            } else {
                record.put(fdevTask.getRqrmnt_no(), taskStage);
            }
        }
        record.put(fdevTask.getRqrmnt_no(), taskStage);
        return record;
    }

    private Map accumulate(FdevTask task, Map rqrType, Map result, String key) {
        List<Integer> detail = (List) result.get(key);
        String rqrid = task.getRqrmnt_no();
        if (!CommonUtils.isNullOrEmpty(rqrid)) {
            String type = (String) rqrType.get(rqrid);
            if (!CommonUtils.isNullOrEmpty(type)) {
                if (type.equals("business")) {
                    detail.set(0, detail.get(0) + 1);
                }
                if (type.equals("tech")) {
                    detail.set(1, detail.get(1) + 1);
                }
                if (type.equals("daily")) {
                    detail.set(2, detail.get(2) + 1);
                }
            }
        }
        result.put(key, detail);
        return result;
    }

    private boolean isInnerRqrmnt(String rqrmntNo, Map rqrType) {
        if (!CommonUtils.isNullOrEmpty(rqrmntNo)) {
            String type = (String) rqrType.get(rqrmntNo);
            if (!CommonUtils.isNullOrEmpty(type)) {
                if (type.equals("business")) {
                    return false;
                }
                if (type.equals("tech")) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map rqrTypeMapping(boolean priority) {
        //true 查询优先级高的，false查询全部
        List<Map> rqrResult = demandService.queryDemandList();
        if (priority) {
            rqrResult = rqrResult.stream()
                    .filter(r -> ("0".equals(r.get("priority"))))
                    .collect(Collectors.toList());
        }
        Map<String, Map> result = new HashedMap();
        Map rqrType = new HashMap();
        Map rqrGroup = new HashMap();
        if (CommonUtils.isNullOrEmpty(rqrResult)) {
            throw new FdevException(ErrorConstants.RQRMNT_ERROR, new String[]{"查询需求数据为空"});
        }
        rqrResult.forEach(o -> {
            rqrType.put(o.get("id"), o.get("demand_type"));
            rqrGroup.put(o.get("id"), o.get("demand_leader_group"));
        });
        result.put("rqrType", rqrType);
        result.put("rqrGroup", rqrGroup);
        return result;
    }


    @Override
    public List queryTaskNumByGroupinAll(List groups, boolean priority, boolean includeChild) {
        List<Map> groupResult = userApi.queryDevResource(groups);
        Map groupName = groupResult.stream().collect(Collectors.toMap(k -> k.get(Dict.ID), v -> v.get(Dict.NAME)));
        Map groupDevNum = groupResult.stream().collect(Collectors.toMap(k -> k.get(Dict.ID), v -> v.get(Dict.COUNT)));
        List returnList = new ArrayList();
//        List<Map> dataReturnList = new ArrayList();
        Map rqrType = (Map) rqrTypeMapping(priority).get("rqrType");
        GroupTree groupTree = null;
//        int countdev = 0;
        if (includeChild) {
            groupTree = groupTreeUtil.getGroupTree();
        }
        for (Object group : groups) {
//            countdev += (int) groupDevNum.get(group);
            List<FdevTask> taskList = fdevTaskDao.queryTaskNumByGroupinAll((String) group, groupTree, CommonUtils.getNoAbortAndNoFile());
            Map result = new HashMap();
            List<Integer> num = new ArrayList();
            num.add(0, 0);
            num.add(1, 0);
            num.add(2, 0);
            result.put(Dict.TASK_STAGE_TODO, ((ArrayList<Integer>) num).clone());
            result.put(Dict.TASK_STAGE_DEVELOP, ((ArrayList<Integer>) num).clone());
            result.put(Dict.TASK_STAGE_SIT, ((ArrayList<Integer>) num).clone());
            result.put(Dict.TASK_STAGE_UAT, ((ArrayList<Integer>) num).clone());
            result.put(Dict.TASK_STAGE_REL, ((ArrayList<Integer>) num).clone());
            result.put(Dict.TASK_STAGE_PRODUCTION, ((ArrayList<Integer>) num).clone());
            result.put("dev_delay", ((ArrayList<Integer>) num).clone());
            result.put("test_delay", ((ArrayList<Integer>) num).clone());
            result.put("pro_delay", ((ArrayList<Integer>) num).clone());
            result.put("wait_in_line", ((ArrayList<Integer>) num).clone());
            result.put(Dict.DEFERTASK, ((ArrayList<Integer>) num).clone());
            // 先计算单项数值
            for (FdevTask fdevTask : taskList) {
                String now = CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1);
                String planStart = fdevTask.getPlan_start_time();
                String planPro = fdevTask.getPlan_fire_time();
                String planTest = fdevTask.getPlan_uat_test_start_time();
                String uatTestTime = fdevTask.getUat_test_time();
                if (CommonUtils.isNullOrEmpty(fdevTask.getTaskSpectialStatus()) || fdevTask.getTaskSpectialStatus().equals(3)) {
                    switch (fdevTask.getStage()) {
                        case Dict.TASK_STAGE_CREATE_INFO:
                        case Dict.TASK_STAGE_CREATE_APP:
                        case Dict.TASK_STAGE_CREATE_FEATURE:
                            if (!CommonUtils.isNullOrEmpty(CommonUtils.dateCompare(planStart, now)))
                                result = accumulate(fdevTask, rqrType, result, "dev_delay");
                            try {
                                String lastweek = CommonUtils.dateFormat(CommonUtils.getLastWeek(
                                        CommonUtils.dateParse(planStart, CommonUtils.DATE_PATTERN_F1)), CommonUtils.DATE_PATTERN_F1);
                                if (!CommonUtils.isNullOrEmpty(CommonUtils.dateCompare(lastweek, now)))
                                    result = accumulate(fdevTask, rqrType, result, "wait_in_line");
                            } catch (Exception e) {
                                logger.error("日期转换异常");
                            }
                            if (!CommonUtils.isNullOrEmpty(uatTestTime)) {
                                fdevTask.setStage(Dict.TASK_STAGE_UAT);
                            } else if (!CommonUtils.isNullOrEmpty(CommonUtils.dateCompare(planTest, now))) {
                                result = accumulate(fdevTask, rqrType, result, "test_delay");
                            }
                            if (!CommonUtils.isNullOrEmpty(CommonUtils.dateCompare(planPro, now)))
                                result = accumulate(fdevTask, rqrType, result, "pro_delay");
                            result = accumulate(fdevTask, rqrType, result, Dict.TASK_STAGE_TODO);
                            break;
                        case Dict.TASK_STAGE_DEVELOP:
                        case Dict.TASK_STAGE_SIT:
                            if (Dict.TASK_STAGE_SIT.equals(fdevTask.getStage()) && !CommonUtils.isNullOrEmpty(uatTestTime)) {
                                fdevTask.setStage(Dict.TASK_STAGE_UAT);
                            } else if (!CommonUtils.isNullOrEmpty(CommonUtils.dateCompare(planTest, now))) {
                                result = accumulate(fdevTask, rqrType, result, "test_delay");
                            }
                            if (!CommonUtils.isNullOrEmpty(CommonUtils.dateCompare(planPro, now)))
                                result = accumulate(fdevTask, rqrType, result, "pro_delay");
                            String stage = fdevTask.getStage();
                            result = accumulate(fdevTask, rqrType, result, stage);
                            break;
                        case Dict.TASK_STAGE_REL:
                            if (!CommonUtils.isNullOrEmpty(CommonUtils.dateCompare(planPro, now)))
                                result = accumulate(fdevTask, rqrType, result, "pro_delay");
                            result = accumulate(fdevTask, rqrType, result, Dict.TASK_STAGE_REL);
                            break;
                        default:
                            result = accumulate(fdevTask, rqrType, result, fdevTask.getStage());
                            break;
                    }
                } else {
                    result = accumulate(fdevTask, rqrType, result, Dict.DEFERTASK);
                }
            }
            // 计算平均值和纵向合计
            String avg = "0.00";
            int yw = 0;
            int xq = 0;
            int rc = 0;
            Map dataResult = (Map) ((HashMap) result).clone();
            for (Object key : result.keySet()) {
                List<Integer> tmp = (List<Integer>) result.get(key);
                BigDecimal sum = new BigDecimal(tmp.get(0) + tmp.get(1) + tmp.get(2));
                BigDecimal devNum = new BigDecimal((int) groupDevNum.get(group));
                result.put(key, tmp.get(0) + "+" + tmp.get(1) + "+" + tmp.get(2) + "=" + sum);
                if (Dict.TASK_STAGE_DEVELOP.equals(key))
                    avg = devNum.equals(new BigDecimal(0)) ? "∞" : sum.divide(devNum, 2, BigDecimal.ROUND_HALF_UP).toString();
                if (Dict.TASK_STAGE_PRODUCTION.equals(key))
                    continue;
                if (!((String) key).contains("_")) {
                    yw += tmp.get(0);
                    xq += tmp.get(1);
                    rc += tmp.get(2);
                }
            }
            result.put("avg", avg);
            result.put(Dict.GROUP, groupName.get(group));
            result.put("sum", yw + "+" + xq + "+" + rc + "=" + (yw + xq + rc));
            String line = (String) result.remove("wait_in_line");
            String todo = (String) result.get(Dict.TASK_STAGE_TODO);
            result.put(Dict.TASK_STAGE_TODO, formateTodo(line, todo));
            returnList.add(result);
//            dataReturnList.add(dataResult);
        }
        // 计算 横向的合计
//        Map<String, String> sum = new HashMap();
//        int total0 = 0;
//        int total1 = 0;
//        int sumdev = 0;
//        for (Object key : dataReturnList.get(0).keySet()) {
//            List<Integer> tmp;
//            int num0 = 0;
//            int num1 = 0;
//            for (Map data : dataReturnList) {
//                tmp = (List<Integer>) data.get(key);
//                num0 += tmp.get(0);
//                num1 += tmp.get(1);
//            }
//            if (!((String) key).contains("_")) {
//                total0 += num0;
//                total1 += num1;
//            }
//            if (Dict.TASK_STAGE_DEVELOP.equals(key))
//                sumdev += num0 + num1;

//            sum.put((String) key, num0 + "+" + num1 + "=" + (num0 + num1));
//        }
//        sum.put(Dict.TASK_STAGE_TODO, formateTodo(sum.get("wait_in_line"), sum.get(Dict.TASK_STAGE_TODO)));
//        sum.remove("wait_in_line");
//        sum.put("sum", total0 + "+" + total1 + "=" + (total0 + total1));
//        BigDecimal bigCountDev = new BigDecimal(countdev);
//        BigDecimal bigSumDev = new BigDecimal(sumdev);
//        String sumAvg = bigCountDev.equals(new BigDecimal(0)) ? "∞" : bigSumDev.divide(bigCountDev, 2, BigDecimal.ROUND_HALF_UP).toString();
//        sum.put("avg", sumAvg);
//        sum.put(Dict.GROUP, "合计");
//        returnList.add(sum);
        return returnList;
    }

    private String formateTodo(String line, String todo) {
        StringBuilder sb = new StringBuilder();
        sb.append(todo, 0, todo.indexOf("+")).append("(").append(line, 0, line.indexOf("+")).append(")")
                .append("+")
                .append(todo, todo.indexOf("+") + 1, todo.lastIndexOf("+"))
                .append("(")
                .append(line, line.indexOf("+") + 1, line.lastIndexOf("+"))
                .append(")")
                .append("+")
                .append(todo,todo.lastIndexOf("+") + 1, todo.indexOf("="))
                .append("(")
                .append(line, line.lastIndexOf("+") + 1, line.indexOf("="))
                .append(")")
                .append("=")
                .append(todo.substring(todo.indexOf("=") + 1))
                .append("(")
                .append(line.substring(line.indexOf("=") + 1))
                .append(")");
        return sb.toString();
    }


    @Override
    public Map<String, Object> queryPostponeTask(Map<String, Object> params) throws Exception {
        List<Map> postponeTask = null;
        boolean flag = (boolean) params.get("history");
        Map<String, Object> taskPageMap = fdevTaskDao.queryPostponeTask(params, groupTreeUtil.getGroupTree());
        List<FdevTask> list = (List<FdevTask>) taskPageMap.get(Dict.TASKLIST);
        postponeTask = list.stream().map(n -> {
            return new HashMap() {{
                putAll(CommonUtils.obj2Map(n));
                put("postpone", isPostpone(n, flag));
            }};
        }).collect(Collectors.toList());
        List<Map> rebuildTemp = rebuild(postponeTask);
        //组装加入需求编号
        Set<String> demandIdSet = rebuildTemp.stream().filter(x -> !CommonUtils.isNullOrEmpty(x.get(Dict.RQRMNT_NO))).map(x -> (String) x.get(Dict.RQRMNT_NO)).collect(Collectors.toSet());
        if (!CommonUtils.isNullOrEmpty(demandIdSet)) {
            List<Map<String, String>> demandList = demandService.queryDemandByIds(demandIdSet);
            if (!CommonUtils.isNullOrEmpty(demandList)) {
                Map<String, String> demandIdNoMap = demandList.stream().collect(Collectors.toMap(x -> x.get(Dict.ID), x -> x.get("oa_contact_no")));
                rebuildTemp.forEach(x -> x.put("oa_contact_no", demandIdNoMap.get(x.get(Dict.RQRMNT_NO))));
            }
        }
        taskPageMap.put(Dict.TASKLIST, rebuildTemp);
        return taskPageMap;
    }

    private Map isPostpone(FdevTask task, boolean flag) {
        if (CommonUtils.isNullOrEmpty(task))
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"task", "传参为空"});
        Map delayDays = new LinkedHashMap();
        try {
            String days = "";
            String stage = task.getStage();
            String realStart = task.getStart_time();
            String realDevEnd = task.getStart_inner_test_time();
            String realSitEnd = task.getStart_uat_test_time();
            //仅当任务状态是sit的时候存在这种情况
            if (CommonUtils.isNotNullOrEmpty(task.getUat_test_time()) && Dict.TASK_STAGE_SIT.equals(task.getStage())) {
                realSitEnd = task.getUat_test_time();
                stage = Dict.TASK_STAGE_UAT;
            }
            String realUatEnd = task.getStop_uat_test_time();
            String planStart = task.getPlan_start_time();
            String planDevEnd = task.getPlan_inner_test_time();
            String planSitEnd = task.getPlan_uat_test_start_time();
            String planUatEnd = task.getPlan_rel_test_time();
            String planRelEnd = task.getPlan_fire_time();//计划投产日期
            String fire_time = task.getFire_time();//实际投产日期
            String now = CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1);
            //当前阶段延期。
            switch (stage) {
                case Dict.TASK_STAGE_CREATE_INFO:
                case Dict.TASK_STAGE_CREATE_APP:
                case Dict.TASK_STAGE_CREATE_FEATURE:
                case Dict.TASK_STAGE_DEVELOP:
                    if (CommonUtils.isNotNullOrEmpty(now, planDevEnd)) {
                        days = CommonUtils.dateCompare(planDevEnd, now);
                        if (!CommonUtils.isNullOrEmpty(days)) {
                            delayDays.put(Dict.TASK_STAGE_SIT, days);
                            if (CommonUtils.isNotNullOrEmpty(now, planSitEnd)) {
                                String uatDays = CommonUtils.dateCompare(planSitEnd, now);
                                if (!CommonUtils.isNullOrEmpty(uatDays)) {
                                    delayDays.put(Dict.TASK_STAGE_UAT, uatDays);
                                }
                            }
                            if (CommonUtils.isNotNullOrEmpty(now, planUatEnd)) {
                                String relDays = CommonUtils.dateCompare(planUatEnd, now);
                                if (!CommonUtils.isNullOrEmpty(relDays)) {
                                    delayDays.put(Dict.TASK_STAGE_REL, relDays);
                                }
                            }
                            if (CommonUtils.isNotNullOrEmpty(now, planRelEnd)) {
                                String proDays = CommonUtils.dateCompare(planRelEnd, now);
                                if (!CommonUtils.isNullOrEmpty(proDays)) {
                                    delayDays.put(Dict.TASK_STAGE_PRODUCTION, proDays);
                                }
                            }
                        }
                    }
                    break;
                case Dict.TASK_STAGE_SIT:
                    if (CommonUtils.isNotNullOrEmpty(now, planSitEnd)) {
                        days = CommonUtils.dateCompare(planSitEnd, now);
                        if (!CommonUtils.isNullOrEmpty(days)) {
                            delayDays.put(Dict.TASK_STAGE_UAT, days);
                            if (CommonUtils.isNotNullOrEmpty(now, planUatEnd)) {
                                String relDays = CommonUtils.dateCompare(planUatEnd, now);
                                if (!CommonUtils.isNullOrEmpty(relDays)) {
                                    delayDays.put(Dict.TASK_STAGE_REL, relDays);
                                }
                            }
                            if (CommonUtils.isNotNullOrEmpty(now, planRelEnd)) {
                                String proDays = CommonUtils.dateCompare(planRelEnd, now);
                                if (!CommonUtils.isNullOrEmpty(proDays)) {
                                    delayDays.put(Dict.TASK_STAGE_PRODUCTION, proDays);
                                }
                            }
                        }
                    }
                    break;
                case Dict.TASK_STAGE_UAT:
                    if (CommonUtils.isNotNullOrEmpty(now, planUatEnd)) {
                        days = CommonUtils.dateCompare(planUatEnd, now);
                        if (!CommonUtils.isNullOrEmpty(days)) {
                            delayDays.put(Dict.TASK_STAGE_REL, days);
                            if (CommonUtils.isNotNullOrEmpty(now, planRelEnd)) {
                                String proDays = CommonUtils.dateCompare(planRelEnd, now);
                                if (!CommonUtils.isNullOrEmpty(proDays)) {
                                    delayDays.put(Dict.TASK_STAGE_PRODUCTION, proDays);
                                }
                            }
                        }
                    }
                    break;
                case Dict.TASK_STAGE_REL:
                    if (CommonUtils.isNotNullOrEmpty(now, planRelEnd)) {
                        days = CommonUtils.dateCompare(planRelEnd, now);
                        if (!CommonUtils.isNullOrEmpty(days)) {
                            delayDays.put(Dict.TASK_STAGE_PRODUCTION, days);
                        }
                    }
                    break;
                default:
                    break;
            }
            if (flag) {
                List beforeStage = new ArrayList();
                if (CommonUtils.isNotNullOrEmpty(planStart, realStart)) {
                    days = CommonUtils.dateCompare(planStart, realStart);
                    if (!CommonUtils.isNullOrEmpty(days)) {
                        delayDays.put(Dict.TASK_STAGE_DEVELOP, days);
                    }
                }
                if (CommonUtils.isNotNullOrEmpty(planDevEnd, realDevEnd)) {
                    days = CommonUtils.dateCompare(planDevEnd, realDevEnd);
                    if (!CommonUtils.isNullOrEmpty(days)) {
                        delayDays.put(Dict.TASK_STAGE_SIT, days);
                    }
                }
                if (CommonUtils.isNotNullOrEmpty(planSitEnd, realSitEnd)) {
                    days = CommonUtils.dateCompare(planSitEnd, realSitEnd);
                    if (!CommonUtils.isNullOrEmpty(days)) {
                        delayDays.put(Dict.TASK_STAGE_UAT, days);
                    }
                }
                if (CommonUtils.isNotNullOrEmpty(planUatEnd, realUatEnd)) {
                    days = CommonUtils.dateCompare(planUatEnd, realUatEnd);
                    if (!CommonUtils.isNullOrEmpty(days)) {
                        delayDays.put(Dict.TASK_STAGE_REL, days);
                    }
                }
                if (CommonUtils.isNotNullOrEmpty(planRelEnd, fire_time)) {
                    days = CommonUtils.dateCompare(planRelEnd, fire_time);
                    if (!CommonUtils.isNullOrEmpty(days)) {
                        delayDays.put(Dict.TASK_STAGE_PRODUCTION, days);
                    }
                }
                if (!delayDays.isEmpty()) {
                    for (Object key : delayDays.keySet()) {
                        if (CommonUtils.pareStage(String.valueOf(key)) <= CommonUtils.pareStage(stage)) {
                            beforeStage.add(key);
                        }
                    }
                    delayDays.put("history", beforeStage);
                }
            }
        } catch (Exception e) {
            logger.error("计算延期失败" + e.getMessage());
        }
        return delayDays;
    }

    private List<Map> rebuild(List<Map> postponeTask) {
        List<Map> result = new ArrayList<>();
        postponeTask.forEach(n -> {
            Map tmp = new HashMap();
            for (Iterator<String> iterator = n.keySet().iterator(); iterator.hasNext(); ) {
                String key = iterator.next();
                Object value = n.get(key);
                try {
                    switch (key) {
                        case Dict.ID:
                        case Dict.NAME:
                        case "postpone":
                        case "rqrmnt_no":
                        case Dict.STAGE:
                            tmp.put(key, value);
                            break;
                        case Dict.SPDB_MASTER:
                        case Dict.MASTER:
                            if (value instanceof String) {
                                value = name((String) value);
                            } else {
                                value = name((String[]) value);
                            }
                            tmp.put(key, value);
                            break;
                        case Dict.GROUP:
                            tmp.put(key, group((String) value));
                        default:
                            break;
                    }

                } catch (Exception e) {
                    logger.error("格式化返回数据异常" + e.getMessage());
                    continue;
                }
            }
            result.add(tmp);
        });
        return result;
    }

    @Override
    public List<FdevTask> queryNotinlineTasksByAppId(Map params) throws Exception {
        List<FdevTask> NotinlineTasks = null;
        try {
            Map map = appApi.queryAppById(params);
            ArrayList task_ids = (ArrayList) map.get("tasks_id");
            NotinlineTasks = fdevTaskDao.queryTasksByIdsNotinlineTasks(task_ids);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NotinlineTasks;
    }

    @Override
    public void bulkAddTask(Map requestParam, User user) throws Exception {
        String target = user.getUser_name_en() + "_addtaskbulk";
        String creator = user.getId();
        List appIds = (List) requestParam.get(Dict.APP_IDS);
        List<Map<String, Object>> apps = appApi.queryAppByIds(appIds);
        Map appNameMap = apps.stream().collect(Collectors.toMap(k -> k.get(Dict.ID), v -> v.get(Dict.NAME_ZH)));
        Map appNameEnMap = apps.stream().collect(Collectors.toMap(k -> k.get(Dict.ID), v -> v.get(Dict.NAME_EN)));
        Map appGroupMap = apps.stream().collect(Collectors.toMap(k -> k.get(Dict.ID), v -> {
            Map spdb = (Map) v.get(Dict.GROUP);
            if (CommonUtils.isNullOrEmpty(spdb))
                return new String();
            return spdb.get(Dict.ID);
        }));
        Map appSpdbMap = apps.stream().collect(Collectors.toMap(k -> k.get(Dict.ID), v -> {
            List spdb = (List) v.get(Dict.SPDB_MANAGERS);
            if (CommonUtils.isNullOrEmpty(spdb))
                return new ArrayList<>();
            return spdb.stream().map(n -> ((Map) n).get(Dict.ID)).collect(Collectors.toList());
        }));
        if (requestParam.containsKey(Dict.REVIEW)) {
            Map review = (Map) requestParam.get(Dict.REVIEW);
            String[] reviewStr = {Dict.OTHER_SYSTEM, Dict.DATA_BASE_ALTER, Dict.EBANK_COMMON_ALTER, Dict.FIRE_WALL_OPEN,
                    Dict.INTERFACE_ALTER, Dict.SCRIPT_ALTER, Dict.STATIC_RESOURCE};
            for (String key : reviewStr) {
                if (review.containsKey(key)) {
                    ArrayList other_system = (ArrayList) review.get(key);
                    review.put(key, format(other_system));
                }
            }
        }
        Map websMsg = new HashMap();
        Map request = new HashMap();
        request.putAll(requestParam);
        request.remove(Dict.APP_IDS);
        String feature = (String) requestParam.get(Dict.FEATURE_BRANCH);
        FdevTask task = CommonUtils.mapToBean(request, FdevTask.class);
        String taskName = task.getName();
        for (int i = 0; i < appIds.size(); i++) {
            String jobid = "";
            String errMsg = "";
            String appid = (String) appIds.get(i);
            ObjectId id = new ObjectId();
            boolean success = true;
            try {
                if (null != feature && !"".equals(feature.trim())) {
                    task.setProject_id(appid);
                    task.setStage(Dict.TASK_STAGE_DEVELOP);
                    task.setCreator(creator);
                    List spdbmanager = (List) appSpdbMap.get(appid);
                    task.setSpdb_master((String[]) spdbmanager.toArray(new String[spdbmanager.size()]));
                    task.setId(id.toString());
                    task.set_id(id);
                    //原任务名(应用名)
                    task.setName(taskName + "（" + appNameMap.get(appid) + "）");
                    task.setGroup((String) appGroupMap.get(appid));
                    task.setProject_name((String) appNameEnMap.get(appid));
                    task.setStart_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1));
                    FdevTask newTask = save(task);
                    asyncService.updateRqrmntWhenStageChanged(newTask, false);
                    jobid = iTestApi.getJobId(newTask, "");
                    saveFdevTaskCollection(new FdevTaskCollection(jobid, newTask.getId(), new ArrayList()));
                    Map branchparam = new HashMap();
                    branchparam.put(Dict.NAME, feature);
                    branchparam.put(Dict.REF, Dict.MASTER);
                    branchparam.put(Dict.TOKEN, token);
                    branchparam.put(Dict.ID, appid);
                    appApi.createGitLabBranch(branchparam);
                }
            } catch (Exception e) {
                success = false;
                errMsg = e.getMessage();
                removeTask(id.toString());
                logger.info("任务创建失败，删除失败任务:{}", id);
                if (!CommonUtils.isNullOrEmpty(jobid)) {
                    deleteMainTask(jobid);
                    Map param = new HashMap();
                    param.put(Dict.JOBNO, jobid);
                    try {
                        iTestApi.deleteOrder(param);
                    } catch (Exception e1) {
                        logger.error("删除工单失败");
                    }
                }
                logger.error("批量新建分支失败：{};errormsg:{}", appNameMap.get(appid), errMsg);
            }
            websMsg.put(Dict.SUCCESS, success);
            websMsg.put("errMsg", errMsg);
            websMsg.put(Dict.APP, appNameMap.get(appid));
            websMsg.put(Dict.NAME, target);
            try {
                iNotifyApi.sendWebsocketMsg(websMsg);
            } catch (Exception e) {
                logger.error("发送websocket失败:{}", e.getMessage());
            }
        }
    }

    @Override
    public Map<String, Map> queryTaskNumByUserIdsDate(List<String> ids, List roles, String startDate, String end_date, List<String> demandTypeList, List<Integer> taskTypeList) throws Exception {
        Map<String, Map> result = new HashMap<>();
        if (!CommonUtils.isNullOrEmpty(end_date)) {
            Date endDate = CommonUtils.dateParse(end_date, CommonUtils.DATE_PATTERN_F1);
            Date today = new Date();
            if (endDate.after(today)) {
                end_date = CommonUtils.dateFormat(today, CommonUtils.DATE_PATTERN_F1);
            }
        }
        for (String id : ids) {
            Map<String, Object> map = fdevTaskDao.queryOutputTaskbyUser(roles, id, startDate, end_date, demandTypeList, taskTypeList);
            result.put(id, map);
        }
        return result;
    }


    @Override
    public Map taskNameJudge(Map requestParam) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        FdevTask task = new FdevTask();
        task.setName((String) requestParam.get(Dict.TASKNAME));
        List<FdevTask> tasks = fdevTaskDao.query(task);
        Map result = new HashMap();
        if (tasks != null && tasks.size() >= 1) {
            result.put("result", false);
        } else {
            result.put("result", true);
        }
        return result;
    }

    @Override
    public Map<String, Object> queryByTermsAndPage(ArrayList parentGroupIds, ArrayList stage, boolean isIncludeChildren, Integer isDefered, int page, int per_page) throws Exception {
        Set<String> groupIds = new HashSet<>();
        if (isIncludeChildren && !CommonUtils.isNullOrEmpty(parentGroupIds)) {
            Map<String, List<Map>> groupMap = userApi.queryChildGroupByIds(parentGroupIds);
            if (!CommonUtils.isNullOrEmpty(groupMap)) {
                for (String groupId : groupMap.keySet()) {
                    List<Map> groupList = groupMap.get(groupId);
                    if (!CommonUtils.isNullOrEmpty(groupId)) {
                        groupIds.addAll(groupList.stream().map(m -> (String) m.get(Dict.ID)).collect(Collectors.toSet()));
                    }
                }
            }
        } else {
            groupIds = new HashSet<>(parentGroupIds);
        }
        //小组，开发人员，负责人，需求编号
        Map<String, Object> map = fdevTaskDao.queryByTermsAndPage(groupIds, stage, isIncludeChildren, isDefered, page, per_page);
        Map<String, Object> result = new HashMap<>();
        List<FdevTask> taskList = (List<FdevTask>) map.get(Dict.TASKLIST);
        //批量查询所有相关人员的信息
        Set<String> taskUserIds = new HashSet<>();
        Set<String> taskGroupIds = new HashSet<>();
        Set<String> taskDemandIds = new HashSet<>();
        Set<String> taskIds = new HashSet<>();
        Set<String> delayTaskIds = new HashSet<>();
        List<Map> resultList = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(taskList)) {
            for (FdevTask task : taskList) {
                if (!CommonUtils.isNullOrEmpty(task.getCreator())) {
                    taskUserIds.add(task.getCreator());
                }
                if (!CommonUtils.isNullOrEmpty(task.getDeveloper())) {
                    taskUserIds.addAll(Arrays.asList(task.getDeveloper()));
                }
                if (!CommonUtils.isNullOrEmpty(task.getMaster())) {
                    taskUserIds.addAll(Arrays.asList(task.getMaster()));
                }
                if (!CommonUtils.isNullOrEmpty(task.getSpdb_master())) {
                    taskUserIds.addAll(Arrays.asList(task.getSpdb_master()));
                }
                if (!CommonUtils.isNullOrEmpty(task.getTester())) {
                    taskUserIds.addAll(Arrays.asList(task.getTester()));
                }
                if (!CommonUtils.isNullOrEmpty(task.getReviewer())) {
                    taskUserIds.add(task.getReviewer());
                }
                if (!CommonUtils.isNullOrEmpty(task.getConcern())) {
                    taskUserIds.addAll(Arrays.asList(task.getConcern()));
                }
                if (!CommonUtils.isNullOrEmpty(task.getGroup())) {
                    taskGroupIds.add(task.getGroup());
                }
                if (!CommonUtils.isNullOrEmpty(task.getRqrmnt_no())) {
                    taskDemandIds.add(task.getRqrmnt_no());
                }
                taskIds.add(task.getId());
                if (!CommonUtils.isNullOrEmpty(isDelay(task))) {
                    delayTaskIds.add(task.getId());
                }
            }
            //批量查询人员信息
            Map<String, Map> userMap = userUtils.getUserByIds(taskUserIds);
            //批量查询小组信息
            List<Map> groupList = userApi.queryGroupByIds(new ArrayList<>(taskGroupIds));
            //将人员信息和id对应起来
            Map<String, Map> groupMap = new HashMap<>();
            if (!CommonUtils.isNullOrEmpty(groupList)) {
                for (Map group : groupList) {
                    groupMap.put((String) group.get(Dict.ID), group);
                }
            }

            //批量查询需求信息
            List<Map<String, String>> demandInfo = demandService.queryDemandByIds(taskDemandIds);
            //将需求信息和id对应起来
            Map<String, Map> demandMap = new HashMap<>();
            if (!CommonUtils.isNullOrEmpty(demandInfo)) {
                for (Map<String, String> demand : demandInfo) {
                    demandMap.put(demand.get(Dict.ID), demand);
                }
            }

            //批量查询合并申请次数并封装成map
            Map<String, Integer> releaseApproveNums = releaseApproveDao.getCountByTaskIds(taskIds).stream().collect(Collectors.toMap(x -> (String) x.get("_id"), x -> (Integer) x.get("num")));

            int sum = taskList.size();
            if (sum > 500) {
                //组装字段
                logger.info(">>>>Runtime.getRuntime().availableProcessors()" + Runtime.getRuntime().availableProcessors());
                ExecutorService es = Executors.newFixedThreadPool(5);

                logger.info(">>>>>queryByTermsAndPage>>{}", sum);
                int count = sum / 4;
                Future<List> future1 = es.submit(() -> setTaskInfo(taskList.subList(0, count), userMap, groupMap, demandMap, releaseApproveNums, delayTaskIds));
                Future<List> future2 = es.submit(() -> setTaskInfo(taskList.subList(count, 2 * count), userMap, groupMap, demandMap, releaseApproveNums, delayTaskIds));
                Future<List> future3 = es.submit(() -> setTaskInfo(taskList.subList(2 * count, 3 * count), userMap, groupMap, demandMap, releaseApproveNums, delayTaskIds));
                Future<List> future4 = es.submit(() -> setTaskInfo(taskList.subList(3 * count, 4 * count), userMap, groupMap, demandMap, releaseApproveNums, delayTaskIds));
                Future<List> future5 = es.submit(() -> setTaskInfo(taskList.subList(4 * count, taskList.size()), userMap, groupMap, demandMap, releaseApproveNums, delayTaskIds));
                resultList.addAll(future1.get());
                resultList.addAll(future2.get());
                resultList.addAll(future3.get());
                resultList.addAll(future4.get());
                resultList.addAll(future5.get());
                es.shutdown();
                //任务全部执行完毕后
                logger.info(">>>>>>queryByTermsAndPage执行完毕:{}", resultList.size());
            } else {
                resultList = setTaskInfo(taskList, userMap, groupMap, demandMap, releaseApproveNums, delayTaskIds);
            }

        }
        result.put(Dict.TOTAL, map.get(Dict.TOTAL));
        result.put("list", resultList);
        return result;
    }

    /**
     * 设置任务的字段信息
     *
     * @param taskList
     * @param userMap
     * @param groupMap
     * @param demandMap
     * @param releaseApproveNums
     * @return
     * @throws Exception
     */
    public List<Map> setTaskInfo(List<FdevTask> taskList, Map<String, Map> userMap, Map<String, Map> groupMap,
                                 Map<String, Map> demandMap, Map<String, Integer> releaseApproveNums, Set<String> delayTaskIds) throws Exception {
        List<Map> list = new ArrayList<>();
        logger.info(">>>>>queryByTermsAndPage.setTaskInfo开始");
        for (FdevTask task : taskList) {
            Map<String, Object> taskMap = CommonUtils.beanToMap(task);
            if (!CommonUtils.isNullOrEmpty(userMap)) {
                taskMap.put(Dict.MASTER, fdevTaskUtils.getUserName(task.getMaster(), userMap));
                taskMap.put(Dict.SPDB_MASTER, fdevTaskUtils.getUserName(task.getSpdb_master(), userMap));
                taskMap.put(Dict.DEVELOPER, fdevTaskUtils.getUserName(task.getDeveloper(), userMap));
                taskMap.put(Dict.CONCERN, fdevTaskUtils.getUserName(task.getConcern(), userMap));
                taskMap.put(Dict.CREATOR, fdevTaskUtils.getUserName(task.getCreator(), userMap));
                taskMap.put(Dict.REVIEWER, fdevTaskUtils.getUserName(task.getReviewer(), userMap));
                taskMap.put(Dict.TESTER, fdevTaskUtils.getUserName(task.getTester(), userMap));
            }
            if (!CommonUtils.isNullOrEmpty(groupMap)) {
                taskMap.put(Dict.GROUP, fdevTaskUtils.getGroupName(task.getGroup(), groupMap));
            }
            if (!CommonUtils.isNullOrEmpty(task.getDoc_uploader())) {
                taskMap.put(Dict.DOCUPLOADER, task.getDoc_uploader().keySet().toArray(new String[]{}));
            }
            if (!CommonUtils.isNullOrEmpty(demandMap)) {
                taskMap.put(Dict.DEMANDNO, fdevTaskUtils.getDemandNo(task.getRqrmnt_no(), demandMap));
            }
            if (!CommonUtils.isNullOrEmpty(releaseApproveNums)) {
                taskMap.put(Dict.APPLYAPPROVETIMES, releaseApproveNums.get(task.getId()));
            }
            if (!CommonUtils.isNullOrEmpty(delayTaskIds)) {
                if (delayTaskIds.contains(task.getId())) {
                    taskMap.put(Dict.DELAYFLAG, true);
                }
            }
            list.add(taskMap);
        }
        logger.info(">>>>>queryByTermsAndPage.setTaskInfo结束");
        return list;
    }

    @Override
    public Map queryRqrmntNameAndReinforceMsg(Map requestParam) throws Exception {
        Map result = new HashMap();
        String merge_id = (String) requestParam.get(Dict.MERGE_ID);
        if (CommonUtils.isNullOrEmpty(merge_id)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"请求参数merge_id不能为空"});
        }
        //查询应用id
        int gitlab_project_id = (int) requestParam.get(Dict.GITLAB_PROJECT_ID);
        if (CommonUtils.isNullOrEmpty(gitlab_project_id)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"请求参数gitlab_project_id不能为空"});
        }
        Map app = appApi.queryAppByGitlabIds(String.valueOf(gitlab_project_id));
        List<FdevTask> tasks = fdevTaskDao.queryBySitMergeId(merge_id, (String) app.get(Dict.ID));
        if (CommonUtils.isNullOrEmpty(tasks)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"任务不存在"});
        }

        Map rqrmnt = demandService.queryByFdevNo(tasks.get(0).getRqrmnt_no(), tasks.get(0).getFdev_implement_unit_no());
        if (null != rqrmnt) {
            Map demand_baseinfo = (Map) rqrmnt.get(Dict.DEMAND_BASEINFO);
            result.put("rqrmntName", demand_baseinfo.get("oa_contact_name"));
        }
        result.put("reinforce", tasks.get(0).getReinforce());
        return result;
    }

    @Override
    public FdevTask createTestRunMerge(Map requestParam) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        String token = user.getGit_token();// "qNMxg-bnRsBievnGFM1h"
        if (CommonUtils.isNullOrEmpty(token)) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户git_token不能为空!"});
        }
        String taskId = requestParam.get(Dict.ID).toString();// 任务ID
        if (CommonUtils.isNullOrEmpty(taskId)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"TASK - id", "任务ID不允许为空！"});
        }
        FdevTask taskParam = null;
        List<FdevTask> list = this.fdevTaskDao.query(new FdevTask.FdevTaskBuilder().id(taskId).init());
        if (CommonUtils.isNullOrEmpty(list)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"根据任务ID查询任务返回为空"});
        }
        taskParam = list.get(0);
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
        //新增卡点管理权限判断测试
        if (!Arrays.asList(sum).contains(user.getId()) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"你不是该任务下的参与人员 无法操作"});
        }
        Map release = ReleaseTaskApi.queryDetailByTaskId(taskId, "3");
        Map release_application = (Map) release.get(Dict.RELEASE_APPLICATION);
        String release_branch = (String) release_application.get(Dict.RELEASE_BRANCH);
        Map param = new HashMap();
        param.put(Dict.ID, id);
        String feature_branch = taskParam.getFeature_branch();
        String dev_branch = feature_branch.replaceFirst("feature", "dev");
        param.put(Dict.SOURCE_BRANCH, dev_branch);
        param.put(Dict.TARGET_BRANCH, release_branch);
        param.put(Dict.TOKEN, token);
        param.put(Dict.TITLE, taskParam.getName() + "--提交试运行测试");
        param.put(Dict.DESCRIPTION, requestParam.get(Dict.DESCRIPTION));
        Map result = appApi.requestMerge(param);
        String mid = result.get(Dict.IID).toString();
        FdevTask task = new FdevTask.FdevTaskBuilder().id((String) requestParam.get(Dict.ID)).test_merge_id(mid).init();
        FdevTask uptask = fdevTaskDao.update(task);
        return uptask;
    }

    @Override
    public List queryGroupRqrmnt(List groups, String priority, boolean isParent) {
        //获取小组名称及小组开发资源
        List<Map> groupResult = userApi.queryDevResource(groups);
        Map groupName = groupResult.stream().collect(Collectors.toMap(k -> k.get(Dict.ID), v -> v.get(Dict.NAME)));
        Map groupDevNum = groupResult.stream().collect(Collectors.toMap(k -> k.get(Dict.ID), v -> v.get(Dict.COUNT)));

        //根据优先级获取全量需求条目
        Map rqrType;
        Map rqrGroup;
        if ("高".equals(priority)) {
            Map rqrResult = rqrTypeMapping(true);
            rqrType = (Map) rqrResult.get("rqrType");
            rqrGroup = (Map) rqrResult.get("rqrGroup");
        } else {
            Map rqrResult = rqrTypeMapping(false);
            rqrType = (Map) rqrResult.get("rqrType");
            rqrGroup = (Map) rqrResult.get("rqrGroup");
        }

        //组装子组
        GroupTree groupTree = null;
        if (isParent) {
            groupTree = groupTreeUtil.getGroupTree();
        }

        List returnList = new ArrayList();
        DecimalFormat df = new DecimalFormat("0.00");
        for (Object group : groups) {
            Map result = new HashMap();
            int count = (int) groupDevNum.get(group);
            result.put(Dict.COUNT, count);
            result.put(Dict.GROUP_ID, group);
            result.put(Dict.GROUPNAME, groupName.get(group));
            int dsskj = 0, dssyw = 0, cssityw = 0, cssitkj = 0, nogdevelopkj = 0, nogdevelopyw = 0, kaifayw = 0, kaifakj = 0, csuatyw = 0, csuatkj = 0, csrelkj = 0, csprokj = 0, csrelyw = 0, csproyw = 0;

            Map<String, String> record = new HashMap<>();
            List<FdevTask> taskList = fdevTaskDao.queryTaskNumByGroupinAll((String) group, groupTree, CommonUtils.getNoAbortList());
            for (FdevTask fdevTask : taskList) {
                if (!CommonUtils.isNullOrEmpty(fdevTask.getRqrmnt_no()))
                    record = accumulate(fdevTask, record);
            }
            for (String rqrmntNo : record.keySet()) {
                if (isInnerRqrmnt(rqrmntNo, rqrType)) {
                    switch (record.get(rqrmntNo)) {
                        case Dict.TASK_STAGE_CREATE_INFO:
                        case Dict.TASK_STAGE_CREATE_APP:
                        case Dict.TASK_STAGE_CREATE_FEATURE:
                            dsskj++;
                            break;
                        case Dict.TASK_STAGE_DEVELOP:
                            String rqrmntGroup = (String) rqrGroup.get(rqrmntNo);
                            if (CommonUtils.isNullOrEmpty(rqrmntGroup)) {
                                nogdevelopkj++;
                            } else {
                                String rqrmntGroupId = rqrmntGroup;
                                if (group.equals(rqrmntGroupId)) {
                                    kaifakj++;
                                } else {
                                    if (isParent) {
                                        List<String> fullChildsById = groupTreeUtil.getFullChildsById((String) group);
                                        if (!CommonUtils.isNullOrEmpty(fullChildsById) && fullChildsById.contains(rqrmntGroup)) {
                                            kaifakj++;
                                        } else {
                                            nogdevelopkj++;
                                        }
                                    } else {
                                        nogdevelopkj++;
                                    }
                                }
                            }
                            break;
                        case Dict.TASK_STAGE_SIT:
                            cssitkj++;
                            break;
                        case Dict.TASK_STAGE_UAT:
                            csuatkj++;
                            break;
                        case Dict.TASK_STAGE_REL:
                            csrelkj++;
                            break;
                        case Dict.TASK_STAGE_PRODUCTION:
                        case Dict.TASK_STAGE_FILE:
                            csprokj++;
                            break;
                        default:
                            break;
                    }
                } else {
                    switch (record.get(rqrmntNo)) {
                        case Dict.TASK_STAGE_CREATE_INFO:
                        case Dict.TASK_STAGE_CREATE_APP:
                        case Dict.TASK_STAGE_CREATE_FEATURE:
                            dssyw++;
                            break;
                        case Dict.TASK_STAGE_DEVELOP:
                            String rqrmntGroup = (String) rqrGroup.get(rqrmntNo);
                            if (CommonUtils.isNullOrEmpty(rqrmntGroup)) {
                                nogdevelopyw++;
                            } else {
                                String rqrmntGroupId = rqrmntGroup;
                                if (group.equals(rqrmntGroupId)) {
                                    kaifayw++;
                                } else {
                                    if (isParent) {
                                        List<String> fullChildsById = groupTreeUtil.getFullChildsById((String) group);
                                        if (!CommonUtils.isNullOrEmpty(fullChildsById) && fullChildsById.contains(rqrmntGroup)) {
                                            kaifayw++;
                                        } else {
                                            nogdevelopyw++;
                                        }
                                    } else {
                                        nogdevelopyw++;
                                    }
                                }
                            }
                            break;
                        case Dict.TASK_STAGE_SIT:
                            cssityw++;
                            break;
                        case Dict.TASK_STAGE_UAT:
                            csuatyw++;
                            break;
                        case Dict.TASK_STAGE_REL:
                            csrelyw++;
                            break;
                        case Dict.TASK_STAGE_PRODUCTION:
                        case Dict.TASK_STAGE_FILE:
                            csproyw++;
                            break;
                        default:
                            break;
                    }
                }
            }
            result.put("dsskj", dsskj);
            result.put("kaifakj", kaifakj);
            result.put("nogdevelopkj", nogdevelopkj);
            result.put("cssitkj", cssitkj);
            result.put("csuatkj", csuatkj);
            result.put("csrelkj", csrelkj);
            result.put("csprokj", csprokj);
            result.put("dssyw", dssyw);
            result.put("kaifayw", kaifayw);
            result.put("nogdevelopyw", nogdevelopyw);
            result.put("cssityw", cssityw);
            result.put("csuatyw", csuatyw);
            result.put("csrelyw", csrelyw);
            result.put("csproyw", csproyw);
            int ywxq = dssyw + kaifayw + nogdevelopyw + cssityw + csuatyw + csrelyw;
            int kjxq = dsskj + kaifakj + nogdevelopkj + cssitkj + csuatkj + csrelkj;
            int kaifaxq = kaifakj + kaifayw + nogdevelopkj + nogdevelopyw;
            double ywxqrjfh = 0;
            double kaifarjfh = 0;
            if (count != 0) {
                kaifarjfh = kaifaxq / (double) count;
                ywxqrjfh = ywxq / (double) count;
                result.put("ywxqrjfh", df.format(ywxqrjfh));
                result.put("kaifarjfh", df.format(kaifarjfh));
            } else {
                result.put("ywxqrjfh", "∞");
                result.put("kaifarjfh", "∞");
            }
            result.put("totalkj", kjxq);
            result.put("totalyw", ywxq);
            returnList.add(result);
        }

        return returnList;
    }

    @Override
    public String createNoCodeTask(FdevTask task, Map request) throws Exception {
        if (!task.getStage().equals("create-info")) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"当前阶段不可以创建无代码变更任务"});
        }
        task.setTaskType((Integer) request.get(Dict.TASK_TYPE));
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
        task.setStart_inner_test_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1));
        FdevTask fdevTask = fdevTaskDao.update(task);
        Map param = new HashMap<>();
        param.put(Dict.TASKNOONLY, task.getId());
        Map result = iTestApi.deleteOrder(param);
        if ("删除任务失败".equals(result.get(Dict.STATUS))) logger.error((String) result.get(Dict.STATUS));
        if (fdevTask != null) {
            asyncService.updateRqrmntWhenStageChanged(task, false);
            if (!CommonUtils.isNullOrEmpty(fdevTask.getStoryId())) {
                asyncService.updateJiraSubTaskStatus(fdevTask);
                asyncService.updateJiraStoryStatus(fdevTask);
            }
            return Dict.SUCCESS;
        }
        return Dict.OPERATION_FAILD;
    }

    @Override
    public String addNocodeRelator(FdevTask task, Map request) throws Exception {
        String stage = task.getStage();
        String rid = new ObjectId().toString();
        Map<String, NoCodeInfo> nocodeInfoMap = task.getNocodeInfoMap();
        NoCodeInfo noCodeInfo = task.getNocodeInfoMap().get(task.getStage());
        Relator[] relatorList = noCodeInfo.getRelators();
        Relator relator = new Relator();
        relator.setRid(rid);
        relator.setCurrentId((String) request.get("currentId"));
        relator.setRelatorId((String) request.get("relatorId"));
        relator.setCurrentName((String) request.get("currentName"));
        relator.setRelatorName((String) request.get("relatorName"));
        relator.setTaskStatus(Dict.NOCODEINFO_NOT_FINISHED);
        relator.setTaskStartTime(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1));
        if (relatorList == null || relatorList.length == 0) {
            relatorList = new Relator[1];
            relatorList[0] = relator;
            noCodeInfo.setRelators(relatorList);
        } else {
            Relator[] resultList = new Relator[relatorList.length + 1];
            List<Relator> relators = new ArrayList<>(relatorList.length + 1);
            for (Relator r : relatorList) {
                relators.add(r);
            }
            relators.add(relator);
            relatorList = relators.toArray(resultList);
            noCodeInfo.setRelators(relatorList);
        }
        String reviewer = (String) request.get("relatorId");
        String link = (String) request.get(Dict.LINK);
        //发送邮件
        Map userParam = new HashMap<>();
        userParam.put(Dict.ID, reviewer);
        Map queryUser = iUserApi.queryUser(userParam);
        String reviewerEamil = (String) queryUser.get(Dict.EMAIL);
        String reviewerNameEn = (String) queryUser.get(Dict.USER_NAME_EN);
        String desc = task.getNocodeInfoMap().get(task.getStage()).getTaskDesc();
        HashMap map = new HashMap<>();
        map.put(Dict.TASKNAME, task.getName());
        map.put(Dict.LINK, link);
        map.put(Dict.DESC, desc);
        map.put("stage", task.getStage());
        map.put("reviewerNameEn", reviewerNameEn);
        mailUtil.sendNocodeTaskMail(Constants.EMAIL_ADDNOCODETASK, map, new String[]{reviewerEamil});
        //添加代办事项
        HashMap todoMap = new HashMap<>();
        todoMap.put(Dict.USER_IDS, new String[]{reviewer});
        todoMap.put(Dict.MODULE, Dict.TASK);
        todoMap.put(Dict.DESCRIPTION, desc);
        todoMap.put(Dict.LINK, link);
        todoMap.put(Dict.CREATE_USER_ID, request.get(Dict.USER_ID));
        todoMap.put(Dict.TYPE, rid);
        todoMap.put(Dict.TARGET_ID, reviewer);
        //新增用户模块的待办
        iUserApi.addTodoList(todoMap);
        //发送通知
        iNotifyApi.sendFdevNotify(desc, "0", new String[]{reviewerNameEn}, link, "无代码变更任务");
        nocodeInfoMap.put(task.getStage(), noCodeInfo);
        task.setNocodeInfoMap(nocodeInfoMap);
        FdevTask fdevTask = fdevTaskDao.update(task);
        if (fdevTask != null) {
            return Dict.SUCCESS;
        }
        return Dict.OPERATION_FAILD;
    }

    @Override
    public String deleteNocodeRelator(FdevTask task, Map request) throws Exception {
        Map<String, NoCodeInfo> nocodeInfoMap = task.getNocodeInfoMap();
        NoCodeInfo noCodeInfo = nocodeInfoMap.get(task.getStage());
        Relator[] relator = noCodeInfo.getRelators();
        List<Relator> relatorList = new ArrayList<>();
        boolean flag = false;
        String targetId = null;
        if (relator != null && relator.length > 0) {
            for (Relator r : relator) {
                if (r.getRid().equals((String) request.get("rid"))) {
                    flag = true;
                    targetId = r.getRelatorId();
                } else
                    relatorList.add(r);
            }
        }
        if (!flag) {
            return "不存在你要删除的信息";
        }
        if (relatorList.size() == 0 || relatorList == null) {
            noCodeInfo.setRelators(new Relator[]{});
        } else {
            Relator[] result = new Relator[relator.length - 1];
            result = relatorList.toArray(result);
            noCodeInfo.setRelators(result);
            nocodeInfoMap.put(task.getStage(), noCodeInfo);
            task.setNocodeInfoMap(nocodeInfoMap);
        }
        if (targetId != null) {
            Map deleteMap = new HashMap<>();
            deleteMap.put(Dict.MODULE, Dict.TASK);
            deleteMap.put(Dict.TYPE, request.get("rid"));
            deleteMap.put(Dict.TARGET_ID, targetId);
            iUserApi.deleteTodoList(deleteMap);
        }
        FdevTask fdevTask = fdevTaskDao.update(task);
        if (fdevTask != null && flag) {
            return Dict.SUCCESS;
        }
        return Dict.OPERATION_FAILD;
    }

    @Override
    public String finishNocodeRelator(FdevTask task, Map request) throws Exception {
        NoCodeInfo noCodeInfo = task.getNocodeInfoMap().get(task.getStage());
        Relator[] relator = noCodeInfo.getRelators();
        boolean flag = false;
        for (int i = 0; i < relator.length; i++) {
            Relator temp = relator[i];
            if (temp.getRid().equals(request.get("rid")) && temp.getRelatorId().equals(request.get("userId"))) {
                flag = true;
                temp.setTaskStatus(Dict.NOCODEINFO_FINISHED);
                temp.setTaskEndTime(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1));
                break;
            }
        }
        noCodeInfo.setRelators(relator);
        task.getNocodeInfoMap().put(task.getStage(), noCodeInfo);
        FdevTask fdevTask = fdevTaskDao.update(task);
        HashMap todoMap = new HashMap<>();
        todoMap.put("executor_id", request.get("userId"));
        todoMap.put(Dict.MODULE, Dict.TASK);
        todoMap.put(Dict.TYPE, request.get("rid"));
        todoMap.put(Dict.TARGET_ID, request.get("userId"));
        iUserApi.updateTodoList(todoMap);
        if (fdevTask != null && flag) {
            return Dict.SUCCESS;
        } else
            return Dict.OPERATION_FAILD;
    }

    @Override
    public String confirmUpdateNocodeInfo(FdevTask task, Map request) throws Exception {
        Map<String, NoCodeInfo> nocodeInfoMap = task.getNocodeInfoMap();
        NoCodeInfo noCodeInfo = task.getNocodeInfoMap().get(task.getStage());
        if (noCodeInfo == null) {
            return Dict.ID_NOT_EXIST;
        }
        String taskDesc = (String) request.get("taskDesc");
        if (!StringUtils.isEmpty(taskDesc)) {
            noCodeInfo.setTaskDesc(taskDesc);
        }
        noCodeInfo.setFinishTime((String) request.get("finishTime"));
        nocodeInfoMap.put(task.getStage(), noCodeInfo);
        task.setNocodeInfoMap(nocodeInfoMap);
        FdevTask fdevTask = fdevTaskDao.update(task);
        if (fdevTask != null) {
            return Dict.SUCCESS;
        }
        return Dict.OPERATION_FAILD;
    }

    @Override
    public List<Map<String, String>> uploadFileByRid(FdevTask task, Map request) throws Exception {
        Map<String, NoCodeInfo> nocodeInfoMap = task.getNocodeInfoMap();
        NoCodeInfo noCodeInfo = task.getNocodeInfoMap().get(task.getStage());
        Relator[] relators = noCodeInfo.getRelators();
        if (relators == null || relators.length == 0) {
            logger.error("非法请求！！！");
            return null;
        }
        List<Relator> relatorList = new ArrayList<>();
        boolean flag = false;
        for (Relator r : relators) {
            if (r.getRid().equals(request.get("rid")) && r.getRelatorId().equals(request.get("userId"))) {
                flag = true;
            }
            relatorList.add(r);
        }
        List<Map<String, String>> files = null;
        if (flag) {
            Iterator<Relator> iterator = relatorList.iterator();
            a:
            while (iterator.hasNext()) {
                Relator temp = iterator.next();
                if (temp.getRid().equals(request.get("rid")) && temp.getRelatorId().equals(request.get("userId"))) {
                    files = temp.getFiles();
                    Map<String, String> file = new HashMap<>(4);
                    file.put("fileName", (String) request.get("fileName"));
                    file.put("path", (String) request.get("path"));
                    file.put("createTime", CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1));
                    if (files != null && files.size() > 0) {
                        for (Map<String, String> f : files) {
                            if (f.get("fileName").equals(request.get("fileName"))) {
                                return null;
                            }
                        }
                    } else {
                        files = new ArrayList<>(1);
                    }
                    files.add(file);
                    temp.setFiles(files);
                    break a;
                }
            }
        } else {
            return null;
        }
        relatorList.toArray(relators);
        nocodeInfoMap.put(task.getStage(), noCodeInfo);
        task.setNocodeInfoMap(nocodeInfoMap);
        FdevTask fdevTask = fdevTaskDao.update(task);
        if (fdevTask != null) {
            return files;
        }
        return null;
    }

    @Override
    public List<Map<String, String>> deleteFileByRid(FdevTask task, Map request) throws Exception {
        Map<String, NoCodeInfo> nocodeInfoMap = task.getNocodeInfoMap();
        NoCodeInfo noCodeInfo = task.getNocodeInfoMap().get(task.getStage());
        Relator[] relators = noCodeInfo.getRelators();
        List<Relator> relatorList = Arrays.asList(relators);
        Iterator<Relator> iterator = relatorList.iterator();
        boolean flag = false;
        List<Map<String, String>> files = null;
        while (iterator.hasNext()) {
            Relator temp = iterator.next();
            if (temp.getRelatorId().equals(request.get("userId")) && temp.getRid().equals(request.get("rid"))) {
                files = temp.getFiles();
                Iterator<Map<String, String>> filesTemp = files.iterator();
                while (filesTemp.hasNext()) {
                    Map<String, String> fileTemp = filesTemp.next();
                    if (fileTemp.get("fileName").equals(request.get("fileName"))) {
                        filesTemp.remove();
                        flag = true;
                        break;
                    }
                }
                temp.setFiles(files);
                break;
            }
        }
        relatorList.toArray(relators);
        nocodeInfoMap.put(task.getStage(), noCodeInfo);
        task.setNocodeInfoMap(nocodeInfoMap);
        FdevTask fdevTask = fdevTaskDao.update(task);
        if (fdevTask != null && flag) {
            return files;
        }
        return null;
    }

    @Override
    public String nocodeNextStage(FdevTask task) throws Exception {
        String stage = task.getStage();
        switch (stage) {
            //不添加人员直接进入uat  给定uat开始时间
            case Dict.TASK_STAGE_SIT:
                task.setStart_uat_test_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1));
                task.setStage(Dict.TASK_STAGE_UAT);
                break;
            //不添加人员直接进入rel  给定rel开始时间
            case Dict.TASK_STAGE_UAT:
                task.setStart_rel_test_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1));
                //同时给定uat结束时间
                task.setStop_uat_test_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1));
                task.setStage(Dict.TASK_STAGE_REL);
                break;
            //不添加人员直接进入pro  给定fire时间
            case Dict.TASK_STAGE_REL:
                task.setStage(Dict.TASK_STAGE_PRODUCTION);
                task.setFire_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1));
                break;
            case Dict.TASK_STAGE_PRODUCTION:
                task.setStage(Dict.TASK_STAGE_FILE);
                break;
            default:
                break;
        }
        FdevTask fdevTask = fdevTaskDao.update(task);
        if (fdevTask != null) {
            asyncService.updateRqrmntWhenStageChanged(task, false);
            if (!CommonUtils.isNullOrEmpty(task.getStoryId())) {
                asyncService.updateJiraSubTaskStatus(task);
                asyncService.updateJiraStoryStatus(task);
            }
            return Dict.SUCCESS;
        }
        return Dict.OPERATION_FAILD;
    }

    @Override
    public List<Map<String, String>> queryFilesByRid(FdevTask task, Map request) {
        NoCodeInfo noCodeInfo = task.getNocodeInfoMap().get(task.getStage());
        Relator[] relators = noCodeInfo.getRelators();
        List<Relator> relatorList = Arrays.asList(relators);
        Iterator<Relator> iterator = relatorList.iterator();
        List<Map<String, String>> files = null;
        while (iterator.hasNext()) {
            Relator temp = iterator.next();
            if (temp.getRelatorId().equals(request.get("userId")) && temp.getRid().equals(request.get("rid"))) {
                files = temp.getFiles();
            }
        }
        return files;
    }


    @Override
    public void updateDesignRemark(FdevTask task) throws Exception {
        fdevTaskDao.update(task);
    }

    @Override
    public Boolean updateConfirmBtn(FdevTask task) throws Exception {
        FdevTask update = fdevTaskDao.update(task);
        if (update != null)
            return true;
        else return false;
    }

    @Override
    public Boolean updateTestKeyNote(FdevTask task) throws Exception {
        FdevTask update = fdevTaskDao.update(task);
        if (update != null)
            return true;
        else return false;
    }

    @Override
    public List<Map> listTaskByGroupIdAndProWantWindow(String group, String proWantWindow) throws Exception {
        FdevTask fdevTask = new FdevTask();
        fdevTask.setGroup(group);
        fdevTask.setProWantWindow(proWantWindow);
        List<FdevTask> list = fdevTaskDao.query(fdevTask);
        List<Map> result = new ArrayList<>();
        for (FdevTask task : list) {
            result.add(reBuildUser(task));
        }
        return result;
    }

    @Override
    public Map<String, Object> getDocInfoForRqr(String taskId) throws Exception {
        //查询task
        FdevTask fdevTask = new FdevTask();
        fdevTask.setId(taskId);
        List<FdevTask> fdevTasks = fdevTaskDao.queryAll(fdevTask);
        FdevTask task = fdevTasks.get(0);
        //根据需求编号查询文件是否存在
        String rqrmntNo = task.getRqrmnt_no();
        Map demandInfoDetail = demandService.queryDemandInfoDetail(rqrmntNo);
        Map<String, Object> result = new HashMap<>();
        if (CommonUtils.isNullOrEmpty(demandInfoDetail)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"需求模块异常"});
        }
        List<Map<String, String>> docInfo = demandService.showDemandDoc(rqrmntNo);
        //需归和需求都没有
        if (CommonUtils.isNullOrEmpty(docInfo)) {
            result.put("result", false);
            result.put("rqrNo", rqrmntNo);//需求编号
            result.put("missType", 2);//0 缺少需求 1缺少需归 2 都没上传
            return result; //返回结束
        }
        boolean docType1 = docInfo.stream()
                .anyMatch(map -> "demandInstruction".equals(map.get("doc_type")));
        boolean docType2 = docInfo.stream()
                .anyMatch(map -> "demandPlanInstruction".equals(map.get("doc_type")));
        if (docType1 && docType2) {
            result.put("result", true);
            return result; //返回结束
        }
        if (docType1) {
            result.put("result", false);
            result.put("rqrNo", rqrmntNo);//需求编号
            result.put("missType", 1);//缺少需归
            return result;//返回结束
        }
        if (docType2) {
            result.put("result", false);
            result.put("rqrNo", rqrmntNo);//需求编号
            result.put("missType", 0);//0 缺少需求 1缺少需归 2 都没上传
            return result;//返回结束
        }
        result.put("result", false);
        result.put("rqrNo", rqrmntNo);//需求编号
        logger.info("判断需求下的文件出错");
        return result;
    }

    private Map reBuildGroupName(Map group1) throws Exception {
        String groupName = "";
        String s = "-";
        if (group1 != null) {
            List<Map> list = userApi.queryParentGroupById((String) group1.get(Dict.ID));
            for (int i = list.size() - 1; i >= 0; i--) {
                Map map = list.get(i);
                String name = (String) map.get(Dict.NAME);
                if (!list.get(0).equals(map)) {
                    name += s;
                }
                groupName += name;
            }
            group1.put("groupFullName", groupName);
        }
        return group1;
    }

    private ArrayList format(ArrayList list) {
        ArrayList array = new ArrayList();
        for (Object sys : list) {
            Map temp = new HashMap();
            temp.put(Dict.NAME, sys);
            temp.put(Dict.AUDIT, false);
            array.add(temp);
        }
        return array;
    }

    private Map<String, List<Map<String, String>>> buildDesign(Map task) {
        String review_status = (String) task.get("review_status");
        if (Dict.IRRELEVANT.equals(review_status) || Dict.WAIT_UPLOAD.equals(review_status)) {
            return null;
        }
        if (Dict.UPLOADED.equals(review_status)) {
            return getStringListMap(task);
        }
        if (Dict.WAIT_ALLOT.equals(review_status)) {
            Map<String, List<Map<String, String>>> demo = getWaitAlot();
            demo.putAll(getStringListMap(task));
            return demo;
        }
        if (Dict.FIXING.equals(review_status)) {
            Map<String, List<Map<String, String>>> fxingListMap = getFxingListMap(task);
            fxingListMap.putAll(getStringListMap(task));
            fxingListMap.putAll(getUploadMap());
            fxingListMap.putAll(getWaitAlot());
            return fxingListMap;
        }
        if (Dict.FIXED.equals(review_status) || Dict.FINISHED.equals(review_status)) {
            Map<String, List<Map<String, String>>> uploadMap = getUploadMap();
            uploadMap.putAll(getStringListMap(task));
            uploadMap.putAll(getFxingListMap(task));
            uploadMap.putAll(getFinished());
            uploadMap.putAll(getWaitAlot());
            return uploadMap;
        }
        logger.info("没有符合的阶段");
        return null;
    }

    private Map<String, List<Map<String, String>>> getFxingListMap(Map task) {
        Map reviewer = (Map) task.get("reviewer");
        Map<String, List<Map<String, String>>> designMap = new HashMap<>();
        Map map = PatrickKit
                .buildMap(Arrays.asList("name", "time", "remark"),
                        Arrays.asList(reviewer.get("user_name_cn"), "", ""));
        designMap.put(Dict.FIXING, Arrays.asList(map));
        return designMap;
    }

    //wait_upload
    private Map<String, List<Map<String, String>>> getUploadMap() {
        Map<String, List<Map<String, String>>> designMap = new HashMap<>();
        Map map = PatrickKit
                .buildMap(Arrays.asList("name", "time", "remark"),
                        Arrays.asList("", "", ""));
        designMap.put(Dict.WAIT_UPLOAD, Arrays.asList(map));
        return designMap;
    }

    private Map<String, List<Map<String, String>>> getWaitAlot() {
        Map<String, List<Map<String, String>>> designMap = new HashMap<>();
        Map map = PatrickKit
                .buildMap(Arrays.asList("name", "time", "remark"),
                        Arrays.asList("", "", ""));
        designMap.put(Dict.WAIT_ALLOT, Arrays.asList(map));
        return designMap;
    }


    // uploaded状态
    private Map<String, List<Map<String, String>>> getStringListMap(Map task) {
        List developer = (ArrayList) task.get(Dict.DEVELOPER);
//        user_name_cn
        ArrayList<Object> devers = new ArrayList<>();
        developer.forEach((dev) -> {
            Map dever = (HashMap) dev;
            if (!CommonUtils.isNullOrEmpty(dever)) {
                Object user_name_cn = dever.get("user_name_cn");
                devers.add(user_name_cn);
            }
        });
        Map<String, List<Map<String, String>>> designMap = new HashMap<>();
        Map dataMap = PatrickKit
                .buildMap(Arrays.asList("name", "time", "remark"),
                        Arrays.asList("", "", ""));
        designMap.put(Dict.UPLOADED, Arrays.asList(dataMap));
        return designMap;
    }

    private Map<String, List<Map<String, String>>> getFinished() {
        Map<String, List<Map<String, String>>> designMap = new HashMap<>();
        Map map = PatrickKit
                .buildMap(Arrays.asList("name", "time", "remark"),
                        Arrays.asList("", "", ""));
        designMap.put(Dict.FINISHED, Arrays.asList(map));
        return designMap;
    }

    /**
     * 查询设计还原审核进度
     *
     * @param reviewer   审核人
     * @param group      板块
     * @param startDate  开始时间
     * @param endDate    结束时间
     * @param searchList 高级筛查条件集合
     * @return
     */
    @Override
    public List<Map<String, Object>> queryReviewDetailList(String reviewer, List<String> group, String startDate, String endDate,
                                                           String logicalOperator, List<Map> searchList, String internetChildGroupId) throws Exception {
        //未传时间就取当前月份起始和终止日期
        Map<String, String> initDate = getInitDate(startDate, endDate);
        String startDate2 = initDate.get(Dict.FIRSTDATE) + " 00:00:00";
        String endDate2 = initDate.get(Dict.LASTDATE) + " 23:59:59";
        //选择了互联网应用下的某个组的子组后，自动填充所选择的互联网应用子组
        List<String> groupIds = new ArrayList<>();
        groupIds.add(internetChildGroupId);
        if (!CommonUtils.isNullOrEmpty(group)) {
            for (String groupId : group) {
                List<Map> result = userApi.queryChildGroup(groupId);
                result = result.stream().filter(map -> "1".equals(map.get(Dict.STATUS))).collect(Collectors.toList());
                groupIds.addAll(result.stream().map(map -> (String) map.get(Dict.ID)).collect(Collectors.toList()));
            }
        }
        List<FdevTask> fdevTasks = fdevTaskDao.queryReviewDetailList(reviewer, groupIds, startDate2, endDate2);
        if (CommonUtils.isNullOrEmpty(fdevTasks)) {
            logger.info("设计还原审核查询为空");
            return null;
        }
        List<Map> fdevImplUnitList = demandService.queryFdevImplUnitByUnitNos(fdevTasks.stream()
                .map(task -> task.getFdev_implement_unit_no()).distinct().collect(Collectors.toList()));
        //批量查询研发单元信息
        Map<String, Boolean> uiVerifyMap = new HashMap<>();
        Map<String, List<String>> implenentLeaderMap = new HashMap<>();
        Map<String, String> demandNoMap = new HashMap<>();
        Map<String, String> ipmpUnitNo = new HashMap<>();
        Map<String, String> unitId = new HashMap<>();
        fdevImplUnitList.forEach(map -> {
            uiVerifyMap.put((String) map.get(Dict.FDEV_IMPLEMENT_UNIT_NO), (Boolean) map.get(Dict.UIVERIFY));
            implenentLeaderMap.put((String) map.get(Dict.FDEV_IMPLEMENT_UNIT_NO), (List<String>) map.get(Dict.IMPLEMENT_LEADER));
            demandNoMap.put((String) map.get(Dict.FDEV_IMPLEMENT_UNIT_NO), (String) ((Map) map.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NO));
            ipmpUnitNo.put((String) map.get(Dict.FDEV_IMPLEMENT_UNIT_NO), (String) map.get(Dict.IPMP_IMPLEMENT_UNIT_NO));
            unitId.put((String) map.get(Dict.FDEV_IMPLEMENT_UNIT_NO), (String) map.get(Dict.ID));
        });
        //获取审核次数、卡点状态、当前阶段及时间和完成情况数据，并设置到任务信息中
        List<Map> taskMaps = new ArrayList<>();
        Map<String, Object> taskMap = null;
        for (FdevTask task : fdevTasks) {
            taskMap = CommonUtils.beanToMap(task);
            taskMap.put(Dict.DEMANDNO, demandNoMap.get(task.getFdev_implement_unit_no()));
            taskMap.put(Dict.IPMPUNITNO, ipmpUnitNo.get(task.getFdev_implement_unit_no()));
            taskMap.put(Dict.FDEV_UNIT_ID, unitId.get(task.getFdev_implement_unit_no()));
            taskMap.put(Dict.CHECKCOUNT, getCheckCount(task));
            taskMap.put(Dict.POSITIONSTATUS, getPositionStatus(task.getFdev_implement_unit_no(), uiVerifyMap));
            setCurrenStageAndTime(taskMap, endDate2);
            taskMap.put(Dict.FINSHFLAG, getFinished(task, endDate2));
            taskMaps.add(taskMap);
        }
        //按高级筛查条件过滤
        if (!CommonUtils.isNullOrEmpty(searchList)) {
            List<Map> newFdevTasks = new ArrayList<>();
            List<String> taskIds = new ArrayList<>();//保存已添加到新集合中的任务id
            for (Map search : searchList) {
                String searchKey = (String) search.get(Dict.SEARCHKEY);//筛查条件关键字
                String relationalOperator = (String) search.get(Dict.RELATIONALOPERATOR);//关系运算符
                String searchValue = (String) search.get(Dict.SEARCHVALUE);//筛查条件值
                //如果逻辑运算符是且，就在原集合上一层层过滤
                //如果逻辑元算符是或，就将每一个符合条件的集合合并到一起
                switch (searchKey) {
                    case Constants.SEARCHKEY_CHECKCOUNT:
                        if (Constants.LOGICALOPERATOR_OR.equals(logicalOperator)) {
                            for (Map task : taskMaps.stream().filter(task ->
                                    getCheckCountCompareResult(Integer.parseInt((String) task.get(Dict.CHECKCOUNT)), Integer.parseInt(searchValue), relationalOperator)
                            ).collect(Collectors.toList())) {
                                if (!taskIds.contains(task.get(Dict.ID))) {
                                    newFdevTasks.add(task);
                                    taskIds.add((String) task.get(Dict.ID));
                                }
                            }
                        } else {
                            taskMaps = taskMaps.stream().filter(task ->
                                    getCheckCountCompareResult(Integer.parseInt((String) task.get(Dict.CHECKCOUNT)), Integer.parseInt(searchValue), relationalOperator)
                            ).collect(Collectors.toList());
                        }
                        break;
                    case Constants.SEARCHKEY_RETURNTOUPLOAD:
                        if (Constants.LOGICALOPERATOR_OR.equals(logicalOperator)) {
                            for (Map task : taskMaps.stream().filter(task ->
                                    getDaysCompareResult(getUpdateDays(task, endDate2), Integer.parseInt(searchValue), relationalOperator)
                            ).collect(Collectors.toList())) {
                                if (!taskIds.contains(task.get(Dict.ID))) {
                                    newFdevTasks.add(task);
                                    taskIds.add((String) task.get(Dict.ID));
                                }
                            }
                        } else {
                            taskMaps = taskMaps.stream().filter(task ->
                                    getDaysCompareResult(getUpdateDays(task, endDate2), Integer.parseInt(searchValue), relationalOperator)
                            ).collect(Collectors.toList());
                        }
                        break;
                    case Constants.SEARCHKEY_UPLOADTORETURN:
                        if (Constants.LOGICALOPERATOR_OR.equals(logicalOperator)) {
                            for (Map task : taskMaps.stream().filter(
                                    task -> getDaysCompareResult(getCheckDays(task, endDate2), Integer.parseInt(searchValue), relationalOperator)
                            ).collect(Collectors.toList())) {
                                if (!taskIds.contains(task.get(Dict.ID))) {
                                    newFdevTasks.add(task);
                                    taskIds.add((String) task.get(Dict.ID));
                                }
                            }
                        } else {
                            taskMaps = taskMaps.stream().filter(
                                    task -> getDaysCompareResult(getCheckDays(task, endDate2), Integer.parseInt(searchValue), relationalOperator)
                            ).collect(Collectors.toList());
                        }
                        break;
                    case Constants.SEARCHKEY_POSITIONSTATUS:
                        if (Constants.LOGICALOPERATOR_OR.equals(logicalOperator)) {
                            for (Map task : taskMaps.stream().filter(
                                    task -> searchValue.equals(task.get(Dict.POSITIONSTATUS))
                            ).collect(Collectors.toList())) {
                                if (!taskIds.contains(task.get(Dict.ID))) {
                                    newFdevTasks.add(task);
                                    taskIds.add((String) task.get(Dict.ID));
                                }
                            }
                        } else {
                            taskMaps = taskMaps.stream().filter(
                                    task -> searchValue.equals(task.get(Dict.POSITIONSTATUS))
                            ).collect(Collectors.toList());
                        }
                        break;
                    case Constants.SEARCHKEY_FINSHFLAG:
                        if (Constants.LOGICALOPERATOR_OR.equals(logicalOperator)) {
                            for (Map task : taskMaps.stream().filter(
                                    task -> searchValue.equals(task.get(Dict.FINSHFLAG))
                            ).collect(Collectors.toList())) {
                                if (!taskIds.contains(task.get(Dict.ID))) {
                                    newFdevTasks.add(task);
                                    taskIds.add((String) task.get(Dict.ID));
                                }
                            }
                        } else {
                            //完成情况，先看任务是否有finished，再看finished的时间是否晚于查询结束时间
                            taskMaps = taskMaps.stream().filter(
                                    task -> searchValue.equals(task.get(Dict.FINSHFLAG))
                            ).collect(Collectors.toList());
                        }
                        break;
                    case Constants.SEARCHKEY_CURRENTSTAGE:
                        if (Constants.LOGICALOPERATOR_OR.equals(logicalOperator)) {
                            for (Map task : taskMaps.stream().filter(
                                    task -> searchValue.equals(task.get(Dict.CURRENTSTAGE))
                            ).collect(Collectors.toList())) {
                                if (!taskIds.contains(task.get(Dict.ID))) {
                                    newFdevTasks.add(task);
                                    taskIds.add((String) task.get(Dict.ID));
                                }
                            }
                        } else {
                            taskMaps = taskMaps.stream().filter(
                                    task -> searchValue.equals(task.get(Dict.CURRENTSTAGE))
                            ).collect(Collectors.toList());
                        }
                        break;
                    default:
                        break;
                }
            }
            //如果查询关系是或，用新任务集合替换旧集合
            if (Constants.LOGICALOPERATOR_OR.equals(logicalOperator)) {
                taskMaps = newFdevTasks;
            }
        }
        //组装ui审核发起人和研发单元牵头人姓名
        Set<String> ids = new HashSet<>();
        taskMaps.forEach(task -> {
            if (!CommonUtils.isNullOrEmpty(task.get(Dict.UIVERIFYREPORTER))) {
                ids.add((String) task.get(Dict.UIVERIFYREPORTER));
            }
        });
        fdevImplUnitList.forEach(map -> {
            if (!CommonUtils.isNullOrEmpty(map.get(Dict.IMPLEMENT_LEADER))) {
                ids.addAll((List<String>) map.get(Dict.IMPLEMENT_LEADER));
            }
        });
        Map<String, Object> userInfoMap = getUserInfoByIds(Arrays.asList(ids.toArray()));
        if (!CommonUtils.isNullOrEmpty(userInfoMap)) {
            taskMaps.forEach(task -> {
                if (!CommonUtils.isNullOrEmpty(userInfoMap.get(task.get(Dict.UIVERIFYREPORTER)))) {
                    Map<String, Object> userInfo = (Map<String, Object>) userInfoMap.get(task.get(Dict.UIVERIFYREPORTER));
                    task.put(Dict.UIVERIFYREPORTER, userInfo.get(Dict.USER_NAME_CN));
                }
                List<String> implementLeaders = implenentLeaderMap.get(task.get(Dict.FDEV_IMPLEMENT_UNIT_NO));
                if (!CommonUtils.isNullOrEmpty(implementLeaders)) {
                    List<String> leaderNames = new ArrayList<>();
                    implementLeaders.forEach(str -> {
                        if (!CommonUtils.isNullOrEmpty(userInfoMap.get(str))) {
                            Map<String, Object> leaderInfo = (Map<String, Object>) userInfoMap.get(str);
                            leaderNames.add((String) leaderInfo.get(Dict.USER_NAME_CN));
                        }
                    });
                    task.put(Dict.IMPLEMENTLEADERNAMECN, leaderNames);
                }
            });
        }
        // 按group分组
        Map<String, List<Map>> collect = taskMaps.stream().collect(Collectors.groupingBy(map -> (String) map.get(Dict.GROUP)));
        if (CommonUtils.isNullOrEmpty(collect)) {
            logger.info("设计还原审核查询为空");
            return null;
        }
        //处理返回结果
        List<Map<String, Object>> result = new ArrayList<>();
        dealResult(result, collect, endDate2);
        //排序
        result.sort(Comparator.comparing(p -> (String) p.get("sortNum")));
        return result;
    }

    /**
     * 获取任务设计还原审核结束时间
     *
     * @return
     */
    private String getFinishedTime(FdevTask fdevTask) {
        if (CommonUtils.isNullOrEmpty(fdevTask.getDesignMap())) {
            return "";
        }
        List<Map<String, String>> finished = fdevTask.getDesignMap().get(Dict.FINISHED);
        //还在审核中，取当前时间。否则取审核结束时间
        if (CommonUtils.isNullOrEmpty(finished) || CommonUtils.isNullOrEmpty(finished.get(0))
                || CommonUtils.isNullOrEmpty(finished.get(0).get(Dict.TIME))) {
            return CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN);
        }
        return finished.get(0).get(Dict.TIME);
    }

    /**
     * 获取待分配时间
     *
     * @return
     */
    private String getWailAllotTime(FdevTask fdevTask) {
        Map<String, List<Map<String, String>>> designMap = fdevTask.getDesignMap();
        if (CommonUtils.isNullOrEmpty(designMap)) return "";
        List<Map<String, String>> wait_allot = designMap.get(Dict.WAIT_ALLOT);
        if (CommonUtils.isNullOrEmpty(wait_allot)) {
            wait_allot = designMap.get(Dict.UNINVOLVED);
            if (CommonUtils.isNullOrEmpty(wait_allot)) {
                return "";
            }
        }
        Map<String, String> waitAllotMap = wait_allot.get(0);
        if (CommonUtils.isNullOrEmpty(waitAllotMap)) return "";
        if (CommonUtils.isNullOrEmpty(waitAllotMap.get(Dict.TIME))) return "";
        return waitAllotMap.get(Dict.TIME);
    }

    /**
     * 设计还原审核按是否完成分类
     *
     * @param fdevTasks
     * @param finishedTask
     * @param unFinishedTask
     */
    private void divideByFinished(List<Map> fdevTasks, List<Map> finishedTask,
                                  List<Map> unFinishedTask) {
        fdevTasks.forEach(p -> {
            if (Constants.FINSHFLAG_PASS.equals(p.get(Dict.FINSHFLAG))) {
                finishedTask.add(p);
            } else {
                unFinishedTask.add(p);
            }
        });
    }

    /**
     * 获取筛选时间
     *
     * @return
     */
    private Map<String, String> getInitDate(String mixDate, String maxDate) {
        Map<String, String> result = new HashMap<>();
        if (CommonUtils.isNotNullOrEmpty(mixDate) && CommonUtils.isNotNullOrEmpty(maxDate)) {
            result.put("firstDate", mixDate);
            result.put("lastDate", maxDate);
            return result;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CommonUtils.DATE_PATTERN);
        Calendar cale = Calendar.getInstance();
        //
        cale.add(Calendar.MONTH, 0);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        result.put("firstDate", simpleDateFormat.format(cale.getTime()));
        cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 1);
        cale.set(Calendar.DAY_OF_MONTH, 0);
        result.put("lastDate", simpleDateFormat.format(cale.getTime()));
        return result;
    }

    /**
     * 处理返回结果
     */
    private void dealResult(List<Map<String, Object>> result, Map<String, List<Map>> collect, String endDate2) {
        Map<String, Object> groupParam = new HashMap<>();
        Iterator<Map.Entry<String, List<Map>>> iterator = collect.entrySet().iterator();
        while (iterator.hasNext()) {
            Map<String, Object> resultByGruop = new HashMap<>();
            List<Map> finishedTask = new ArrayList<>();
            List<Map> unFinishedTask = new ArrayList<>();
            Map.Entry<String, List<Map>> next = iterator.next();
            groupParam.put("id", next.getKey());
            Map queryGroup = null;
            try {
                queryGroup = iUserApi.queryGroup(groupParam);
            } catch (Exception e) {
                logger.error("查询小组信息异常,组id:" + next.getKey());
                throw new FdevException(ErrorConstants.APP_THIRD_SERVER_ERROR, new String[]{"查询小组信息异常"});
            }
            if (CommonUtils.isNullOrEmpty(queryGroup)) {
                logger.error("查询组信息为空,组id:" + next.getKey());
                throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"查询小组信息为空"});
            }
            //板块名称
            String groupName = (String) queryGroup.get("name");
            //排序字段
            String sortNum = (String) queryGroup.get("sortNum");
            resultByGruop.put("group", next.getKey());
            resultByGruop.put("groupName", groupName);
            resultByGruop.put("sortNum", sortNum);
            //按已完成、未完成分为两个list
            divideByFinished(next.getValue(), finishedTask, unFinishedTask);
            resultByGruop.put("finishedList", finishedTask);
            resultByGruop.put("unfinishedList", unFinishedTask);
            result.add(resultByGruop);
        }
    }

    /**
     * 获取审核轮数
     *
     * @param task
     * @return
     */
    public String getCheckCount(FdevTask task) {
        Map<String, List<Map<String, String>>> designMap = task.getDesignMap();
        int count = 0;
        //有finished(审核通过)代表一轮
        if (!CommonUtils.isNullOrEmpty(designMap.get(Dict.FINISHED))) {
            ++count;
        }
        //如果有nopass，nopass下的数据集合中，有几条数据的stage为load_nopass(审核未通过)，那就再加几轮
        List<Map<String, String>> nopassList = designMap.get(Dict.NOPASS);
        if (!CommonUtils.isNullOrEmpty(nopassList)) {
            for (Map<String, String> nopass : nopassList) {
                if (Constants.STAGE_LOADNOPASS.equals(nopass.get(Dict.STAGE))) {
                    ++count;
                }
            }
        }
        return String.valueOf(count);
    }

    /**
     * 获取审核未通过后开发修改耗时天数
     *
     * @param task
     * @param endDate2
     * @return
     */
    public List<Integer> getUpdateDays(Map task, String endDate2) {
        List<Integer> updateDays = new ArrayList<>();
        Map<String, List<Map<String, String>>> designMap = (Map<String, List<Map<String, String>>>) task.get(Dict.DESIGNMAP);
        List<Map<String, String>> nopassList = designMap.get(Dict.NOPASS);
        if (!CommonUtils.isNullOrEmpty(nopassList)) {
            Collections.sort(nopassList, new Comparator<Map<String, String>>() {
                @Override
                public int compare(Map<String, String> o1, Map<String, String> o2) {
                    String time1 = o1.get(Dict.TIME);
                    String time2 = o2.get(Dict.TIME);
                    return time1.compareTo(time2);
                }
            });
            //取每条load_nopass时间和紧跟着的开发上传截图load_upload时间的相差天数，
            //如果开发尚未上传，取和查询结束日期(如果结束日期晚于当前日期，取当前日期)的相差天数
            SimpleDateFormat sdf = new SimpleDateFormat(CommonUtils.DATE_PATTERN);
            String nowDate = sdf.format(new Date());
            for (int i = 0; i < nopassList.size(); i++) {
                Map<String, String> noPass = nopassList.get(i);
                if (Constants.STAGE_LOADNOPASS.equals(noPass.get(Dict.STAGE))) {
                    String time = noPass.get(Dict.TIME);
                    try {
                        //没有后续记录了，说明开发尚未上传
                        if (i == nopassList.size() - 1) {
                            updateDays.add(CommonUtils.getDiffDays(endDate2.compareTo(nowDate) > 0 ? nowDate : endDate2, time));
                        } else {
                            String uploadTime = nopassList.get(i + 1).get(Dict.TIME);
                            updateDays.add(CommonUtils.getDiffDays(uploadTime, time));
                        }
                    } catch (Exception e) {
                        logger.info("获取审核未通过后开发修改耗时天数异常,taskId=" + task.get(Dict.ID));
                    }
                }
            }
        }
        return updateDays;
    }

    /**
     * 获取开发上传截图后审核人员完成审核耗时天数
     *
     * @param task
     * @param endDate2
     * @return
     */
    public List<Integer> getCheckDays(Map task, String endDate2) {
        List<Integer> checkDays = new ArrayList<>();
        Map<String, List<Map<String, String>>> designMap = (Map<String, List<Map<String, String>>>) task.get(Dict.DESIGNMAP);
        List<Map<String, String>> nopassList = designMap.get(Dict.NOPASS);
        SimpleDateFormat sdf = new SimpleDateFormat(CommonUtils.DATE_PATTERN);
        String nowDate = sdf.format(new Date());
        //第一次审核时间，是分配了审核人员后到审核完成/第一次未通过时间的天数
        List<Map<String, String>> finishedList = designMap.get(Dict.FINISHED);
        List<Map<String, String>> fixingList = designMap.get(Dict.FIXING);
        //已分配了审核人员
        if (!CommonUtils.isNullOrEmpty(fixingList)) {
            //有nopass就取第一次未通过时间，和分配时间相隔天数就是第一次审核耗时
            String fixingTime = fixingList.get(0).get(Dict.TIME);
            if (!CommonUtils.isNullOrEmpty(nopassList)) {
                Collections.sort(nopassList, new Comparator<Map<String, String>>() {
                    @Override
                    public int compare(Map<String, String> o1, Map<String, String> o2) {
                        String time1 = o1.get(Dict.TIME);
                        String time2 = o2.get(Dict.TIME);
                        return time1.compareTo(time2);
                    }
                });
                String firstNoPassTime = nopassList.get(0).get(Dict.TIME);
                try {
                    checkDays.add(CommonUtils.getDiffDays(firstNoPassTime, fixingTime));
                } catch (Exception e) {
                    logger.info("获取开发上传截图后审核人员完成审核耗时天数异常,taskId=" + task.get(Dict.ID));
                }
                //取其中每次stage为load_upload(再次上传)时间和紧跟着的load_nopass或finished时间
                //如果审核人尚未审核，取和查询结束日期(如果结束日期晚于当前日期，取当前日期)的相差天数
                for (int i = 0; i < nopassList.size(); i++) {
                    Map<String, String> noPass = nopassList.get(i);
                    if (Constants.STAGE_LOADUPLOAD.equals(noPass.get(Dict.STAGE))) {
                        String uploadTime = noPass.get(Dict.TIME);
                        try {
                            //没有后续记录了，查看是否有finished
                            if (i == nopassList.size() - 1) {
                                if (CommonUtils.isNullOrEmpty(finishedList)) {
                                    //最后一次操作是开发上传
                                    checkDays.add(CommonUtils.getDiffDays(endDate2.compareTo(nowDate) > 0 ? nowDate : endDate2, uploadTime));
                                } else {
                                    //最后一次审核，审核通过了
                                    checkDays.add(CommonUtils.getDiffDays(finishedList.get(0).get(Dict.TIME), uploadTime));
                                }
                            } else {
                                //第一次和最后一次之间的审核
                                String checkTime = nopassList.get(i + 1).get(Dict.TIME);
                                checkDays.add(CommonUtils.getDiffDays(checkTime, uploadTime));
                            }
                        } catch (Exception e) {
                            logger.info("获取开发上传截图后审核人员完成审核耗时天数异常,taskId=" + task.get(Dict.ID));
                        }
                    }
                }
            } else {
                //审核记录中如果没有nopass且有finished，取完成审核时间和指定审核人员时间的相差天数
                if (!CommonUtils.isNullOrEmpty(finishedList)) {
                    try {
                        checkDays.add(CommonUtils.getDiffDays(finishedList.get(0).get(Dict.TIME), fixingList.get(0).get(Dict.TIME)));
                    } catch (Exception e) {
                        logger.info("获取开发上传截图后审核人员完成审核耗时天数异常,taskId=" + task.get(Dict.ID));
                    }
                } else {
                    //没有nopass也没有finished，说明一直未审核，取和查询结束日期(如果结束日期晚于当前日期，取当前日期)的相差天数
                    try {
                        checkDays.add(CommonUtils.getDiffDays(endDate2.compareTo(nowDate) > 0 ? nowDate : endDate2, fixingTime));
                    } catch (Exception e) {
                        logger.info("获取开发上传截图后审核人员完成审核耗时天数异常,taskId=" + task.get(Dict.ID));
                    }
                }
            }
        }
        return checkDays;
    }

    /**
     * 获取完成情况
     * 先看任务是否有finished，再看finished的时间是否晚于查询结束时间
     *
     * @param task
     * @param endDate2
     * @return
     */
    public String getFinished(FdevTask task, String endDate2) {
        Map<String, List<Map<String, String>>> designMap = task.getDesignMap();
        List<Map<String, String>> finishedList = designMap.get(Dict.FINISHED);
        if (CommonUtils.isNullOrEmpty(finishedList)) {
            return Constants.FINSHFLAG_NOPASS;
        } else {
            String finishedTime = finishedList.get(0).get(Dict.TIME);
            if (finishedTime.compareTo(endDate2) > 0) {
                return Constants.FINSHFLAG_NOPASS;
            }
        }
        return Constants.FINSHFLAG_PASS;
    }

    /**
     * 获取卡点状态
     *
     * @param fdevUnitNo
     * @param uiVerifyMap
     * @return
     */
    public String getPositionStatus(String fdevUnitNo, Map<String, Boolean> uiVerifyMap) {
        if (!CommonUtils.isNullOrEmpty(uiVerifyMap.get(fdevUnitNo))) {
            return uiVerifyMap.get(fdevUnitNo) ? Constants.POSITIONSTATUS_OK : Constants.POSITIONSTATUS_FAIL;
        } else {
            return Constants.POSITIONSTATUS_FAIL;
        }

    }

    /**
     * 获取当前审核阶段
     *
     * @param task
     * @return
     */
    public void setCurrenStageAndTime(Map<String, Object> task, String endDate2) {
        Map<String, List<Map<String, String>>> designMap = new HashMap<>();
        designMap.putAll((Map<? extends String, ? extends List<Map<String, String>>>) task.get(Dict.DESIGNMAP));
        //将审核记录中，每一个步骤的时间晚于查询结束时间的剔除
        List<Map<String, String>> finishedList = designMap.get(Dict.FINISHED);
        if (!CommonUtils.isNullOrEmpty(finishedList)) {
            Map<String, String> finished = finishedList.get(0);
            if (finished.get(Dict.TIME).compareTo(endDate2) > 0) {
                designMap.remove(Dict.FINISHED);
            }
        }
        List<Map<String, String>> nopassList = designMap.get(Dict.NOPASS);
        String lastStage = "";//审核记录中nopass不晚于查询结束时间的记录中最后一条数据的stage
        String lastStageTime = "";//审核记录中nopass不晚于查询结束时间的记录中最后一条数据的time
        if (!CommonUtils.isNullOrEmpty(nopassList)) {
            //将noPass记录中，时间晚于查询结束时间的剔除
            nopassList = nopassList.stream().filter(map -> endDate2.compareTo((String) map.get(Dict.TIME)) >= 0).collect(Collectors.toList());
            if (CommonUtils.isNullOrEmpty(nopassList)) {
                designMap.remove(Dict.NOPASS);
            } else {
                lastStage = nopassList.get(nopassList.size() - 1).get(Dict.STAGE);
                lastStageTime = nopassList.get(nopassList.size() - 1).get(Dict.TIME);
            }
        }
        List<Map<String, String>> fixingList = designMap.get(Dict.FIXING);
        if (!CommonUtils.isNullOrEmpty(fixingList)) {
            Map<String, String> fixing = fixingList.get(0);
            if (fixing.get(Dict.TIME).compareTo(endDate2) > 0) {
                designMap.remove(Dict.FIXING);
            }
        }
        List<Map<String, String>> waitAllotList = designMap.get(Dict.WAIT_ALLOT);
        if (!CommonUtils.isNullOrEmpty(waitAllotList)) {
            Map<String, String> waitAllot = waitAllotList.get(0);
            if (waitAllot.get(Dict.TIME).compareTo(endDate2) > 0) {
                designMap.remove(Dict.WAIT_ALLOT);
            }
        }
        List<Map<String, String>> uploadedList = designMap.get(Dict.UPLOADED);
        if (!CommonUtils.isNullOrEmpty(uploadedList)) {
            Map<String, String> uploaded = uploadedList.get(0);
            if (uploaded.get(Dict.TIME).compareTo(endDate2) > 0) {
                designMap.remove(Dict.UPLOADED);
            }
        }
        List<Map<String, String>> irrelevantList = designMap.get(Dict.UNINVOLVED);
        if (!CommonUtils.isNullOrEmpty(irrelevantList)) {
            irrelevantList = irrelevantList.stream().filter(map ->
                    endDate2.compareTo((String) map.get(Dict.TIME)) >= 0).collect(Collectors.toList());
            if (CommonUtils.isNullOrEmpty(irrelevantList)) {
                designMap.remove(Dict.UNINVOLVED);
            }
        }
        List<Map<String, String>> refuseUninvolvedList = designMap.get(Dict.REFUSEUNINVOLVED);
        if (!CommonUtils.isNullOrEmpty(refuseUninvolvedList)) {
            refuseUninvolvedList = refuseUninvolvedList.stream().filter(map ->
                    endDate2.compareTo((String) map.get(Dict.TIME)) >= 0).collect(Collectors.toList());
            if (CommonUtils.isNullOrEmpty(refuseUninvolvedList)) {
                designMap.remove(Dict.REFUSEUNINVOLVED);
            }
        }
        //当前审核状态，就是满足时间条件后的最后一个状态
        List<String> keys = new ArrayList<>(designMap.keySet());
        //因为前面按时间过滤过了，这里的数据，至少有一条是时间满足的
        String reviewStatus = "";
        if (keys.contains(Constants.REVIEWSTATUS_FINISHED)) {
            reviewStatus = Constants.REVIEWSTATUS_FINISHED;
        } else if (keys.contains(Constants.REVIEWSTATUS_NOPASS)) {
            reviewStatus = Constants.REVIEWSTATUS_NOPASS;
        } else if (keys.contains(Constants.REVIEWSTATUS_FIXING)) {
            reviewStatus = Constants.REVIEWSTATUS_FIXING;
        } else if (keys.contains(Constants.REVIEWSTATUS_WAITALLOT)) {
            reviewStatus = Constants.REVIEWSTATUS_WAITALLOT;
        } else {
            logger.info(">>>getCurrenStage数据异常" + task.get(Dict.ID));
        }
        //分配中
        ////开发发起ui还原性申请，未分配审核人员，审核状态标志为wait_allot；
        if (Constants.REVIEWSTATUS_WAITALLOT.equals(reviewStatus)) {
            task.put(Dict.CURRENTSTAGE, Constants.REVIEWSTAGE_ALLOTING);
            task.put(Dict.CURRENTSTAGETIME, designMap.get(reviewStatus).get(0).get(Dict.TIME));
        }
        //审核中
        ////分配审核人员后，但未审核完成，审核状态标志为fixing，且分配时间早于查询结束时间；
        if (Constants.REVIEWSTATUS_FIXING.equals(reviewStatus)) {
            task.put(Dict.CURRENTSTAGE, Constants.REVIEWSTAGE_CHECKING);
            task.put(Dict.CURRENTSTAGETIME, designMap.get(reviewStatus).get(0).get(Dict.TIME));
        }
        ////开发人员二次及多次（非第一次）上传文件后，未审核完成，审核状态标志为nopass，且审核记录中nopass最后一条数据的stage为load_upload
        if (Constants.REVIEWSTATUS_NOPASS.equals(reviewStatus)
                && Constants.STAGE_LOADUPLOAD.equals(lastStage)) {
            task.put(Dict.CURRENTSTAGE, Constants.REVIEWSTAGE_CHECKING);
            task.put(Dict.CURRENTSTAGETIME, lastStageTime);
        }
        //修改中
        ////ui审核拒绝后，开发人员未上传材料，审核状态标志为nopass，且审核记录中nopass最后一条数据的stage为load_nopass。
        if (Constants.REVIEWSTATUS_NOPASS.equals(reviewStatus)
                && Constants.STAGE_LOADNOPASS.equals(lastStage)) {
            task.put(Dict.CURRENTSTAGE, Constants.REVIEWSTAGE_UPDATEING);
            task.put(Dict.CURRENTSTAGETIME, lastStageTime);
        }
        //完成
        ////审核通过，审核状态标志为finished
        if (Constants.REVIEWSTATUS_FINISHED.equals(reviewStatus)) {
            task.put(Dict.CURRENTSTAGE, Constants.REVIEWSTAGE_FINISH);
            task.put(Dict.CURRENTSTAGETIME, designMap.get(reviewStatus).get(0).get(Dict.TIME));
        }
    }

    /**
     * 获取审核轮数根据关系运算符比较后的结果
     *
     * @param num1
     * @param num2
     * @param relationalOperator
     * @return
     */
    public boolean getCheckCountCompareResult(int num1, int num2, String relationalOperator) {
        switch (relationalOperator) {
            case Constants.RELATIONALOPERATOR_LT:
                return num1 < num2;
            case Constants.RELATIONALOPERATOR_EQ:
                return num1 == num2;
            case Constants.RELATIONALOPERATOR_GT:
                return num1 > num2;
            default:
                return false;
        }
    }

    /**
     * 获取审核天数或修改天数根据关系运算符比较后的结果
     *
     * @param numList
     * @param num2
     * @param relationalOperator
     * @return
     */
    public boolean getDaysCompareResult(List<Integer> numList, int num2, String relationalOperator) {
        if (CommonUtils.isNullOrEmpty(numList)) {
            return false;
        }
        switch (relationalOperator) {
            case Constants.RELATIONALOPERATOR_LT:
                //如果选择的关系是小于，需要每一个耗时天数都小于筛查条件值
                return numList.stream().allMatch(dayNum -> dayNum < num2);
            case Constants.RELATIONALOPERATOR_EQ:
                //如果是等于，其中一个耗时天数等于筛查条件值即可
                return numList.stream().anyMatch(dayNum -> dayNum == num2);
            case Constants.RELATIONALOPERATOR_GT:
                //如果是大于，其中一个耗时天数大于筛查条件值即可
                return numList.stream().anyMatch(dayNum -> dayNum > num2);
            default:
                return false;
        }
    }

    @Override
    public List<Map> getPostponeInfo(FdevTask fdevTask) {
        List<Map> list = new ArrayList<>();
        Map tmp = new HashMap();
        tmp.putAll(CommonUtils.obj2Map(fdevTask));
        tmp.put("postpone", isPostpone(fdevTask, false));
        list.add(tmp);
        return list;
    }

    /**
     * 查询应用id下未删除、废弃、归档的任务
     *
     * @param project_id
     * @return
     */
    @Override
    public List<String> queryTaskIdsByProjectId(String project_id) {
        List<FdevTask> taskList = fdevTaskDao.queryTaskIdsByProjectId(project_id);
        if (!CommonUtils.isNullOrEmpty(taskList)) {
            return taskList.stream().map(FdevTask::getId).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 新增任务时，验证任务计划时间是否小于研发单元计划时间
     *
     * @param fdevTask
     * @param implementUnitInfo
     */
    @Override
    public void checkPlanTime(Map<String, Object> fdevTask, Map<String, Object> implementUnitInfo) {
        if (CommonUtils.isNotNullOrEmpty((String) fdevTask.get("plan_start_time"), (String) implementUnitInfo.get("plan_start_date"))
                && ((String) fdevTask.get("plan_start_time")).compareTo(((String) implementUnitInfo.get("plan_start_date")).replace("-", "/")) > 0) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"任务计划开始时间大于研发单元计划开始时间"});
        }
        if (CommonUtils.isNotNullOrEmpty((String) fdevTask.get("plan_inner_test_time"), (String) implementUnitInfo.get("plan_inner_test_date"))
                && ((String) fdevTask.get("plan_inner_test_time")).compareTo(((String) implementUnitInfo.get("plan_inner_test_date")).replace("-", "/")) > 0) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"任务计划内测启动日期大于研发单元计划内测启动日期"});
        }
        if (CommonUtils.isNotNullOrEmpty((String) fdevTask.get("plan_uat_test_start_time"), (String) implementUnitInfo.get("plan_test_date"))
                && ((String) fdevTask.get("plan_uat_test_start_time")).compareTo(((String) implementUnitInfo.get("plan_test_date")).replace("-", "/")) > 0) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"任务计划提交用户测试日期大于研发单元计划提交用户测试日期"});
        }
        if (CommonUtils.isNotNullOrEmpty((String) fdevTask.get("plan_uat_test_stop_time"), (String) implementUnitInfo.get("plan_test_finish_date"))
                && ((String) fdevTask.get("plan_uat_test_stop_time")).compareTo(((String) implementUnitInfo.get("plan_test_finish_date")).replace("-", "/")) > 0) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"任务计划用户测试完成日期大于研发单元计划用户测试完成日期"});

        }
        //rel测试启动日期与uat测试完成日期相同
        if (CommonUtils.isNotNullOrEmpty((String) fdevTask.get("plan_rel_test_time"), (String) implementUnitInfo.get("plan_test_finish_date"))
                && ((String) fdevTask.get("plan_rel_test_time")).compareTo(((String) implementUnitInfo.get("plan_test_finish_date")).replace("-", "/")) > 0) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"任务计划用户测试完成日期大于研发单元计划用户测试完成日期"});

        }
        if (CommonUtils.isNotNullOrEmpty((String) fdevTask.get("plan_fire_time"), (String) implementUnitInfo.get("plan_product_date"))
                && ((String) fdevTask.get("plan_fire_time")).compareTo(((String) implementUnitInfo.get("plan_product_date")).replace("-", "/")) > 0) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"任务计划投产日期大于研发单元计划投产日期"});
        }
    }

    @Override
    public Map queryInfoById(String taskId) throws Exception {
        FdevTask fdevTasks = fdevTaskDao.queryById(taskId);
        if (CommonUtils.isNullOrEmpty(fdevTasks)) {
            return null;
        }
        Map result = CommonUtils.beanToMap(fdevTasks);
        ReviewRecord reviewRecord = reviewRecordDao.queryRecordByTaskId((String) result.get(Dict.ID));
        result.put("reviewRecord", reviewRecord);
        return result;
    }


    /**
     * 根据研发单元查询创建的任务状态为非删除废弃的数量
     *
     * @param fdev_implement_unit_no
     */
    @Override
    public Integer queryNotDiscarddnum(String fdev_implement_unit_no) {
        return fdevTaskDao.queryNotDiscarddnum(fdev_implement_unit_no);
    }

    @Override
    public FdevTask queryTaskById(String taskId) {
        return fdevTaskDao.queryById(taskId);
    }

    @Override
    public FdevTask updateReview(FdevTask fdevTask) throws Exception {
        return fdevTaskDao.update(fdevTask);
    }

    @Override
    public List<Map> queryWaitFileTask() throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        List<FdevTask> taskList = fdevTaskDao.queryUserProductionTask(user.getId());
        List<Map> result = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(taskList)) {
            Map<String, Object> taskInfo = null;
            for (FdevTask task : taskList) {
                taskInfo = new HashMap<>();
                taskInfo.put(Dict.ID, task.getId());
                taskInfo.put(Dict.NAME, task.getName());
                taskInfo.put(Dict.STAGE, task.getStage());
                taskInfo.put(Dict.TASKSPECTIALSTATUS, task.getTaskSpectialStatus());
                taskInfo.put(Dict.BRANCH, task.getFeature_branch());
                taskInfo.put(Dict.PROJECT_ID, task.getProject_id());
                taskInfo.put(Dict.APP_NAME_EN, task.getProject_name());
                Map param = new HashMap();
                param.put(Dict.ID, task.getGroup());
                Map groupInfo = userApi.queryGroup(param);
                if (!CommonUtils.isNullOrEmpty(groupInfo)) {
                    taskInfo.put(Dict.GROUP, groupInfo.get(Dict.NAME));
                } else {
                    taskInfo.put(Dict.GROUP, "");
                }
                result.add(taskInfo);
            }
        }
        return result;
    }

    @Override
    public void updateTaskStateToFile(List<String> ids) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        //查询任务信息
        List<FdevTask> fdevTaskList = fdevTaskDao.queryAllTasksByIds((ArrayList) ids);
        Map<String, FdevTask> oldTaskMap = new HashMap<>();
        //先校验用户是否每个任务都有权限操作
        for (FdevTask task : fdevTaskList) {
            if (!(task.getCreator().equals(user.getId())
                    || Arrays.asList(task.getMaster()).contains(user.getId())
                    || Arrays.asList(task.getSpdb_master()).contains(user.getId()))
                    && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"不是任务" + task.getName() + "创建人或负责人"});
            }
            //再判断任务是否暂缓中
            if (!CommonUtils.isNullOrEmpty(task.getTaskSpectialStatus()) && task.getTaskSpectialStatus() == 1) {
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{task.getName() + "任务状态处于暂缓状态，无法归档"});
            }
            oldTaskMap.put(task.getId(), task);
        }
        //更新数据表
        List<FdevTask> fdevTaskListNew = fdevTaskDao.updateTaskStateToFileBatch(ids);
        //更新其它关联信息
        for (FdevTask ftask : fdevTaskListNew) {
            FdevTask task = new FdevTask();
            task.setId(ftask.getId());
            task.setStage(Dict.TASK_STAGE_FILE);
            //任务更新前
            FdevTask oldTask = oldTaskMap.get(task.getId());
            //删除接口调用关系
            asyncService.deleteForInterface(task, oldTask);
            //审核记录归档
            asyncService.deleteReviewRecordCaseByTaskStageChange(task, ftask);
            //和jira关联的需要更新故事和子任务状态
            if (!CommonUtils.isNullOrEmpty(oldTask.getStoryId())) {
                //更新故事状态
                logger.info("更新故事状态:" + oldTask.getStoryId());
                asyncService.updateJiraStoryStatus(oldTask);
                logger.info("更新子任务状态" + ftask.getJiraKey());
                asyncService.updateJiraSubTaskStatus(ftask);
            }
            //更新投产模块的任务数据
            Map<String, Object> releaseTaskApiParam = new HashMap<>();
            releaseTaskApiParam.put(Dict.TASK_ID, ftask.getId());
            ReleaseTaskApi.updateRqrmntInfoReview(releaseTaskApiParam);
            //发送邮件
            if (isSendMail) {
                asyncService.sendMail(oldTask, ftask, user.getUser_name_cn(), "");
            }
        }
    }

    @Override
    public Map<String, Boolean> checkMountUnit(String taskId) {
        Map<String, Boolean> result = new HashMap<>();
        result.put(Dict.MOUNTUNITFLAG, true);
        //根据任务id查询任务信息
        FdevTask fdevTask = fdevTaskDao.queryById(taskId);
        if (CommonUtils.isNullOrEmpty(fdevTask)) {
            throw new FdevException(ErrorConstants.TASK_NOT_EXIST);
        }
        String fdevUnit = fdevTask.getFdev_implement_unit_no();
        //根据任务的研发单元编号查询研发单元信息
        Map implementUnitInfo = (Map) demandService.queryByFdevNo("", fdevUnit).get(Dict.IMPLEMENT_UNIT_INFO);
        //判断是否新版研发单元
        if (!CommonUtils.isNullOrEmpty(implementUnitInfo) && (Boolean) implementUnitInfo.get(Dict.IS_NEW)
                && CommonUtils.isNullOrEmpty(implementUnitInfo.get(Dict.IPMP_IMPLEMENT_UNIT_NO))) {
            result.put(Dict.MOUNTUNITFLAG, false);
        }
        return result;
    }

    @Override
    public void updateTaskSpectialStatus(String unitNo, String status, String spectialStatus) throws Exception {
        //根据研发单元查询任务
        List<FdevTask> fdevTaskList = fdevTaskDao.queryAllTaskByUnitNo(unitNo);
        for (FdevTask fdevTask : fdevTaskList) {
            //只支持修改特殊状态，不支持直接删除任务和取消删除
            updateSpectialStatus(fdevTask, spectialStatus);

            /*//如果是要恢复撤销,将原先删除的(有保留删除前状态)任务状态恢复
            if("cancelAbort".equals(status)
                    && Dict.TASK_STAGE_ABORT.equals(fdevTask.getStage())
                    && !CommonUtils.isNullOrEmpty(fdevTask.getOld_stage())) {
                fdevTask.setStage(fdevTask.getOld_stage());
                fdevTask.setOld_stage("");
                updateSpectialStatus(fdevTask, spectialStatus);
            }else if(Dict.TASK_STAGE_ABORT.equals(status)
                    && !Dict.TASK_STAGE_ABORT.equals(fdevTask.getStage())
                    && !Dict.TASK_STAGE_DISCARD.equals(fdevTask.getStage())
                    && !Dict.TASK_STAGE_PRODUCTION.equals(fdevTask.getStage())
                    && !Dict.TASK_STAGE_FILE.equals(fdevTask.getStage())) {
                //如果是要撤销,任务状态不能是已取消、已废弃、已投产和已归档
                fdevTask.setOld_stage(fdevTask.getStage());
                fdevTask.setStage(Dict.TASK_STAGE_ABORT);
                updateSpectialStatus(fdevTask, spectialStatus);
            }
            if(CommonUtils.isNullOrEmpty(status)) {
                updateSpectialStatus(fdevTask, spectialStatus);
            }*/
        }
    }

    //修改特殊状态
    public void updateSpectialStatus(FdevTask fdevTask, String spectialStatus) throws Exception {
        if (!CommonUtils.isNullOrEmpty(spectialStatus)
                && ("0".equals(spectialStatus) || "1".equals(spectialStatus))) {
            //如果是更改特殊状态为暂缓或者正常
            fdevTask.setTaskSpectialStatus(Integer.parseInt(spectialStatus));
        }
        fdevTaskDao.update(fdevTask);
    }


    @Override
    public Map<String, Boolean> checkUatAndRelTestTime(String taskId) throws Exception {
        FdevTask fdevTask = fdevTaskDao.queryById(taskId);
        if (CommonUtils.isNullOrEmpty(fdevTask)) {
            throw new FdevException(ErrorConstants.TASK_NOT_EXIST);
        }
        Map<String, Boolean> result = new HashedMap();
        result.put(Dict.UATTESTTIMEOKFLAG, true);
        result.put(Dict.RELTESTTIMEOKFLAG, true);
        String proWantWindow = fdevTask.getProWantWindow();
        if (CommonUtils.isNotNullOrEmpty(proWantWindow)) {
            String startUatTestTime = fdevTask.getStart_uat_test_time();
            if (CommonUtils.isNotNullOrEmpty(startUatTestTime)
                    && CommonUtils.dateParse(proWantWindow, CommonUtils.DATE_PATTERN_F1).before(CommonUtils.dateParse(startUatTestTime, CommonUtils.DATE_PATTERN_F1))) {
                result.put(Dict.UATTESTTIMEOKFLAG, false);
            }
            String startRelTestTime = fdevTask.getStart_rel_test_time();
            if (CommonUtils.isNotNullOrEmpty(startRelTestTime)
                    && CommonUtils.dateParse(proWantWindow, CommonUtils.DATE_PATTERN_F1).before(CommonUtils.dateParse(startRelTestTime, CommonUtils.DATE_PATTERN_F1))) {
                result.put(Dict.RELTESTTIMEOKFLAG, false);
            }
        }
        return result;
    }

    @Override
    public List<FdevTask> queryTaskByFdevUnitNo(String fdevUnitNo) {
        return fdevTaskDao.queryAllTaskByUnitNo(fdevUnitNo, false);
    }

    @Override
    public Map<String, Object> queryAddTaskType(String demandId, String unitNo) {
        Map<String, Object> demandAndUnitInfo = (Map) demandService.queryDemandInfoDetail(demandId);
        if (CommonUtils.isNullOrEmpty(demandAndUnitInfo)) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"需求信息不存在"});
        }
        String demandType = (String) demandAndUnitInfo.get(Dict.DEMAND_TYPE);
        //如果选择的需求是日常需求，只能新增日常任务
        List<String> taskType = new ArrayList<>();
        if (Constants.DEMANDTYPE_DAILY.equals(demandType)) {
            taskType.add(Constants.TASKTYPE_DAILY);
        } else {
            //如果选择的需求是开发需求，可以新增开发任务和无代码任务
            taskType.add(Constants.TASKTYPE_DEVELOP);
            taskType.add(Constants.TASKTYPE_NOCODE);
            //查询研发单元下任务
            List<FdevTask> taskList = fdevTaskDao.queryAllTaskByUnitNo(unitNo, false);
            //如果研发单元下已经有其他开发任务了，可以新增日常任务
            if (!CommonUtils.isNullOrEmpty(taskList)
                    && taskList.stream().filter(task -> !"2".equals(String.valueOf(task.getTaskType()))).count() > 0) {
                taskType.add(Constants.TASKTYPE_DAILY);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put(Dict.TASK_TYPE, taskType);
        return result;
    }

    @Override
    public List<Map> queryGroupById(String id) throws Exception {
        String groupId = CommonUtils.isNullOrEmpty(id) ? internetGroupId : id;
        List<Map> result = userApi.queryChildGroup(groupId);
        return result.stream().filter(map -> "1".equals(map.get(Dict.STATUS)) && !groupId.equals(map.get(Dict.ID))).collect(Collectors.toList());
    }

    @Override
    public void downLoadReviewList(HttpServletResponse response, String reviewer, List<String> group, String startDate, String endDate,
                                   String logicalOperator, List<Map> searchList, Map<String, String> columnMap, String internetChildGroupId) throws Exception {
        excel(response, queryReviewDetailList(reviewer, group, startDate, endDate, logicalOperator, searchList, internetChildGroupId), columnMap);
    }

    @Override
    public void updateTaskCodeOrderNo(Map<String, Object> params) throws Exception {
        String no = (String) params.get(Dict.CODE_ORDER_NO);
        List<FdevTask> fdevTaskList = fdevTaskDao.queryTaskByCodeOrderNo(no);
        if (!CommonUtils.isNullOrEmpty(fdevTaskList)) {
            //清空原该代码审核工单的任务
            for (FdevTask fdevTask : fdevTaskList) {
                Set<String> code_order_no = fdevTask.getCode_order_no();
                code_order_no.remove(no);
                fdevTask.setCode_order_no(code_order_no);
                fdevTaskDao.update(fdevTask);
            }
        }

        List<String> taskIds = (List<String>) params.get(Dict.TASK_ID);
        if (!CommonUtils.isNullOrEmpty(taskIds)) {
            //赋值现该代码审核工单的任务
            for (String taskId : taskIds) {
                FdevTask fdevTask = fdevTaskDao.selectTaskById(taskId);
                if (!CommonUtils.isNullOrEmpty(fdevTask)) {
                    Set<String> code_order_no = fdevTask.getCode_order_no();
                    if (CommonUtils.isNullOrEmpty(code_order_no)) code_order_no = new HashSet<>();
                    code_order_no.add(no);
                    fdevTask.setCode_order_no(code_order_no);
                    fdevTaskDao.update(fdevTask);
                }
            }
        }

    }

    @Override
    public Map<String, Object> queryTaskByCodeOrderNo(String code_order_no) throws Exception {
        List<FdevTask> fdevTaskList = fdevTaskDao.queryTaskByCodeOrderNo(code_order_no);
        List<String> taskList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (FdevTask fdevTask : fdevTaskList) {
            //包含进行中状态 没有都投产 返回空
            if (CommonUtils.getGoingList().contains(fdevTask.getStage())) {
                return map;
            }
            taskList.add(fdevTask.getId());
        }
        map.put("taskIds", taskList);//任务ID
        map.put(Dict.REALPRODUCTDATE, fdevTaskList.get(0).getFire_time());//投产时间
        return map;
    }

    /**
     * 创建Excel并插入数据
     *
     * @param response
     * @param dataList
     * @param columnMap
     * @throws Exception
     */
    public void excel(HttpServletResponse response, List<Map<String, Object>> dataList, Map<String, String> columnMap) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("sheet1");
        XSSFCellStyle cellStyle = wb.createCellStyle();
        cellStyle.setWrapText(true);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //合并单元格，第一列项目小组其实占了小组和完成情况及数量两列
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));
        //主体内容，按小组区分，一个小组一大块
        int rowNum = 1;
        if (dataList == null) {
            dataList = new ArrayList<>();
        }
        for (int i = 0; i < dataList.size(); i++) {
            //审核通过任务数量
            int finishedRowNum = ((List) dataList.get(i).get(Dict.FINISHEDLIST)).size();
            //未审核通过任务数量
            int unFinishedRowNum = ((List) dataList.get(i).get(Dict.UNFINISHEDLIST)).size();
            //小组占用的行数，是小组下所有任务的条数
            if (finishedRowNum + unFinishedRowNum > 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowNum, finishedRowNum + unFinishedRowNum + rowNum - 1, 0, 0));
            }
            if (finishedRowNum > 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowNum, finishedRowNum + rowNum - 1, 1, 1));
            }
            if (unFinishedRowNum > 1) {
                sheet.addMergedRegion(new CellRangeAddress(finishedRowNum + rowNum, finishedRowNum + unFinishedRowNum + rowNum - 1, 1, 1));
            }
            rowNum += finishedRowNum + unFinishedRowNum;
        }

        int excelRow = 0;
        Row titleRow = sheet.createRow(excelRow++);
        //表头
        List<String> titleList = new ArrayList<>(columnMap.values());
        for (int i = 0; i < titleList.size(); i++) {
            Cell cell = titleRow.createCell(i);
            cell.setCellValue(titleList.get(i));
        }
        List<String> keyList = new ArrayList<>(columnMap.keySet());
        //数据
        List<FdevTask> taskList = null;
        for (int i = 0; i < dataList.size(); i++) {
            taskList = new ArrayList<>();
            taskList.addAll((List<FdevTask>) dataList.get(i).get(Dict.FINISHEDLIST));
            taskList.addAll((List<FdevTask>) dataList.get(i).get(Dict.UNFINISHEDLIST));
            //审核通过任务数量
            int finishedRowNum = ((List) dataList.get(i).get(Dict.FINISHEDLIST)).size();
            //未审核通过任务数量
            int unFinishedRowNum = ((List) dataList.get(i).get(Dict.UNFINISHEDLIST)).size();
            for (int j = 0; j < taskList.size(); j++) {
                Map task = CommonUtils.beanToMap(taskList.get(j));
                //行
                Row dataRow = sheet.createRow(excelRow++);
                Cell cell = null;
                for (int k = 0; k < keyList.size(); k++) {
                    //单元格
                    cell = dataRow.createCell(k);
                    String key = keyList.get(k);
                    String text = "";
                    if (Dict.GROUPNAME.equals(key)) {
                        cell.setCellStyle(cellStyle);
                        text = (String) dataList.get(i).get(key);
                        cell.setCellValue(text + "\r\n总数\r\n" + (finishedRowNum + unFinishedRowNum));
                    } else if (Dict.NUM.equals(key)) {
                        cell.setCellStyle(cellStyle);
                        //项目小组下面还有一列，展示各完成情况的任务数量
                        if (j < finishedRowNum) {
                            cell.setCellValue("通过\r\n" + finishedRowNum);
                        } else {
                            cell.setCellValue("未通过\r\n" + unFinishedRowNum);
                        }
                    } else {
                        if (Dict.IMPLEMENTLEADERNAMECN.equals(key)) {
                            text = StringUtils.join((List<String>) task.get(key), ",");
                        } else {
                            text = (String) task.get(key);
                            if (Constants.SEARCHKEY_POSITIONSTATUS.equals(key)) {
                                text = Constants.POSITIONSTATUS_OK.equals(text) ? "正常" : "失败";
                            } else if (Constants.SEARCHKEY_FINSHFLAG.equals(key)) {
                                text = Constants.FINSHFLAG_PASS.equals(text) ? "通过" : "未通过";
                            } else if (Constants.SEARCHKEY_CURRENTSTAGE.equals(key)) {
                                if (Constants.REVIEWSTAGE_ALLOTING.equals(text)) {
                                    text = "分配中";
                                }
                                if (Constants.REVIEWSTAGE_CHECKING.equals(text)) {
                                    text = "审核中";
                                }
                                if (Constants.REVIEWSTAGE_UPDATEING.equals(text)) {
                                    text = "修改中";
                                }
                                if (Constants.REVIEWSTAGE_FINISH.equals(text)) {
                                    text = "完成";
                                }
                            } else if (Constants.SEARCHKEY_CHECKCOUNT.equals(key)) {
                                text = text + "轮";
                            }
                        }
                        cell.setCellValue(text);
                    }
                }
            }
        }

        excelRow++;
        Row dataRow = sheet.createRow(excelRow++);
        Cell cell = dataRow.createCell(0);
        cell.setCellValue("注：卡点状态反映评估研发单元是否涉及UI审核的准确性，如果研发单元涉及UI还原审核，状态为正常，如果不涉及UI还原审核，状态为失败。");
        XSSFFont font = wb.createFont();
        font.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
        XSSFCellStyle style = wb.createCellStyle();
        style.setFont(font);
        cell.setCellStyle(style);

        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("任务进度详情", "UTF-8") + ".xlsx");
        wb.write(response.getOutputStream());
    }

    @Override
    public void putSecurityTest(String taskId, String correlationSystem, String correlationInterface,
                                String interfaceFilePath, String transFilePath, List<Map> transList, String remark) throws Exception {
        FdevTask fdevTask = fdevTaskDao.queryById(taskId);
        if (CommonUtils.isNullOrEmpty(fdevTask)) {
            throw new FdevException(ErrorConstants.TASK_NOT_EXIST);
        }
        User user = userVerifyUtil.getRedisUser();
        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL);
        }
        if (Constants.TASKTYPE_DEVELOP.equals(fdevTask.getStage())) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"请先提交功能测试"});
        }
        String sum[] = CommonUtils.concatAll(fdevTask.getDeveloper(), fdevTask.getSpdb_master(), fdevTask.getMaster());
        //权限判断
        if (!Arrays.asList(sum).contains(user.getId()) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        if (transList != null) {
            //将更改后的交易清单列表重新写入excel文件
            inputTransToFile(transFilePath, transList, user);
        }
        //创建工单需要研发单元编号或者实施单元id
        Map demandInfo = demandService.queryDemandInfoDetail(fdevTask.getRqrmnt_no());
        String taskName = fdevTask.getName();
        String unitNo = fdevTask.getFdev_implement_unit_no();
        if (!CommonUtils.isNullOrEmpty(demandInfo) && Constants.DEMANDTYPE_BUSINESS.equals(demandInfo.get(Dict.DEMAND_TYPE))) {
            Map<String, Object> fdevImplUnit = demandService.queryFdevImplUnitDetail(unitNo);
            if (!CommonUtils.isNullOrEmpty(fdevImplUnit)) {
                if (!CommonUtils.isNullOrEmpty(fdevImplUnit.get(Dict.IPMP_IMPLEMENT_UNIT_NO))) {
                    Map<String, String> sendMap = new HashMap<>();
                    sendMap.put(Dict.IMPL_UNIT_NUM, (String) fdevImplUnit.get(Dict.IPMP_IMPLEMENT_UNIT_NO));
                    sendMap.put(Dict.REST_CODE, "queryIpmpUnitById");
                    //先根据实施单元编号查询详情，获取id
                    Map<String, Object> implUnitInfo = (Map<String, Object>) restTransport.submit(sendMap);
                    unitNo = (String) implUnitInfo.get(Dict.ID);
                }
            } else {
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"查询研发单元信息失败"});
            }
        }
        //获取开发人员英文名列表
        String[] developerArr = fdevTask.getDeveloper();
        Map<String, Object> userInfoMap = getUserInfoByIds(Arrays.asList(developerArr));
        List<String> devsNameEN = new ArrayList();
        for (String str : developerArr) {
            if (!CommonUtils.isNullOrEmpty(userInfoMap.get(str))) {
                Map<String, Object> userInfo = (Map<String, Object>) userInfoMap.get(str);
                devsNameEN.add((String) userInfo.get(Dict.USER_NAME_EN));
            }
        }
        String developers = String.join(",", devsNameEN);
        //创建安全测试工单
        iTestApi.createSecurityOrder(taskId, taskName, unitNo, correlationSystem, correlationInterface,
                interfaceFilePath, transFilePath, remark, fdevTask.getProject_name(), developers, fdevTask.getGroup(), transList);
    }

    /**
     * 将交易清单列表写入excel文件重新上传
     *
     * @param transFilePath
     * @param transList
     * @param user
     * @throws Exception
     */
    private void inputTransToFile(String transFilePath, List<Map> transList, User user) throws Exception {
        ClassPathResource resource = new ClassPathResource(Constants.TRANS_TEMPLATE_XLSX);
        XSSFWorkbook workbook = new XSSFWorkbook(resource.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        int i = 1;
        Row row = null;
        for (Map<String, String> trans : transList) {
            row = sheet.getRow(i);
            Cell indexCell = row.getCell(0);
            indexCell.setCellValue(trans.get(Dict.INDEX));
            Cell transNameCell = row.getCell(1);
            transNameCell.setCellValue(trans.get(Dict.TRANSNAME));
            Cell transDescCell = row.getCell(2);
            transDescCell.setCellValue(trans.get(Dict.TRANSDESC));
            Cell functionMenuCell = row.getCell(3);
            functionMenuCell.setCellValue(trans.get(Dict.FUNCTIONMENU));
            ++i;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
        MultipartFile multipartFile = new MockMultipartFile("file", "安全测试交易清单", "text/plain",
                IOUtils.toByteArray(inputStream));
        if (!fdocmanageService.uploadFiletoMinio("fdev-task", transFilePath, multipartFile, user)) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"更新交易清单列表文件失败"});
        }
    }

    @Override
    public void syncTaskUIReportUser() {
        List<FdevTask> taskList = fdevTaskDao.queryUITask();
        for (FdevTask task : taskList) {
            Map<String, List<Map<String, String>>> designMap = task.getDesignMap();
            if (!CommonUtils.isNullOrEmpty(designMap) && !CommonUtils.isNullOrEmpty(designMap.get(Dict.WAIT_ALLOT))) {
                List<Map<String, String>> waitAllot = designMap.get(Dict.WAIT_ALLOT);
                String userName = waitAllot.get(0).get(Dict.NAME);
                if (!CommonUtils.isNullOrEmpty(userName)) {
                    try {
                        Map userInfo = userApi.queryUser(userName);
                        task.setUiVerifyReporter((String) userInfo.get(Dict.ID));
                        fdevTaskDao.update(task);
                    } catch (Exception e) {
                        logger.info("查询用户信息异常{}", userName);
                    }
                }
            }
        }
    }

    @Override
    public String createMergeRequest(String applicationType, String title, String projectId, String description,
                                     String sourceBranch, String targetBranch, String versionNum, Map createUser) throws Exception {
        User user = userVerifyUtil.getRedisUser();
        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL);
        }
        if (CommonUtils.isNullOrEmpty(projectId)) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"未关联应用"});
        }
        String mergeRequestId = "";
        //获取关联项目信息
        Map<String, Object> projectInfo = getProjectInfo(projectId, applicationType);
        if (CommonUtils.isNullOrEmpty(projectInfo)) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"未查询到任务关联的项目信息"});
        }
        String gitlabId = (String) projectInfo.get(Dict.GITLAB_ID);
        //如果是往SIT合并判断是否已有SIT分支，没有就从master分支拉一个
        if (Constants.BRANCH_SIT.equals(targetBranch) &&
                CommonUtils.isNullOrEmpty(gitApiService.queryBranch(gitlabId, Constants.BRANCH_SIT))) {
            //创建SIT分支
            gitApiService.createBranch(gitlabId, Constants.BRANCH_SIT, Constants.BRANCH_MASTER);
        }
        if (CommonUtils.isNullOrEmpty(createUser)) {
            mergeRequestId = gitApiService.createMergeRequest(gitlabId, sourceBranch, targetBranch,
                    description, title, user.getGit_token());
        } else {
            mergeRequestId = gitApiService.createMergeRequest(gitlabId, sourceBranch, targetBranch,
                    description, title, (String) createUser.get("git_token"));
        }
        //合并请求信息传给组件模块
        if (CommonUtils.isComponent(applicationType)) {
            try {
                componentService.saveMergeRequest(projectId, "", gitlabId, mergeRequestId, versionNum);
            } catch (Exception e) {
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"提交合并请求成功,组件模块保存合并请求信息失败:" + e.getMessage()});
            }
        }
        if (CommonUtils.isArchetype(applicationType)) {
            try {
                componentService.saveMergeRequest("", projectId, gitlabId, mergeRequestId, versionNum);
            } catch (Exception e) {
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"提交合并请求成功,组件模块保存合并请求信息失败:" + e.getMessage()});
            }
        }
        return mergeRequestId;
    }

    @Override
    public void syncTaskApplicationType() {
        List<FdevTask> taskList = fdevTaskDao.queryAllTask();
        for (FdevTask task : taskList) {
            String appId = task.getProject_id();
            //查询应用类型
            try {
                if (!CommonUtils.isNullOrEmpty(appId)) {
                    Map<String, Object> appDetail = appApi.queryAppByIdCache(appId);
                    if (!CommonUtils.isNullOrEmpty(appDetail)) {
                        String typeName = (String) appDetail.get(Dict.TYPE_NAME);
                        try {
                            fdevTaskDao.updateApplicationType(task.getId(), CommonUtils.getTypeNameEN(typeName));
                        } catch (Exception e) {
                            logger.info(">>>>>>修改任务应用类型失败{}", task.getId());
                        }
                    } else {
                        fdevTaskDao.updateApplicationType(task.getId(), Constants.APPLICATION_JAVA);
                    }
                }
            } catch (Exception e) {
                logger.info(">>>>>>查询应用信息失败{}", appId);
            }
        }
    }

    @Override
    public void addMemberGit(String gitlabProjectId, HashSet members, String appType, String projectId) throws Exception {
        //查询用户的git id
        Map<String, List<String>> param = new HashMap<String, List<String>>() {{
            put(Dict.IDS, new ArrayList<>(members));
        }};
        List<Map> userList = userApi.queryUserList(param);
        List<String> gitlabUserIds = userList.stream().map(user -> (String) user.get(Dict.GITUSERID)).collect(Collectors.toList());
        for (String gitlabUserId : gitlabUserIds) {
            gitApiService.addMember(gitlabProjectId, 30, gitlabUserId);
        }
    }

    @Override
    public Map<String, Object> getProjectInfo(String projectId, String appType) throws Exception {
        if (Constants.COMPONENT_WEB.equals(appType)) {
            //查询前端组件信息
            Map<String, Object> componentInfo = componentService.queryComponentWebDetail(projectId);
            List<Map> managers = (List<Map>) componentInfo.get(Dict.MANAGER);
            List<String> managerIds = managers.stream().map(manager -> (String) manager.get(Dict.ID)).collect(Collectors.toList());
            return new HashMap<String, Object>() {{
                put(Dict.GITLAB_ID, String.valueOf(componentInfo.get(Dict.GITLABID)));
                put(Dict.GITLABURL, componentInfo.get(Dict.GITLAB_URL));
                put(Dict.PROJECTNAME, componentInfo.get(Dict.NAME_EN));
                put(Dict.ISTEST, componentInfo.get(Dict.ISTEST));
                put(Dict.SONARSCANSWITCH, "0");
                put(Dict.MANAGER, managerIds);
            }};
        } else if (Constants.COMPONENT_SERVER.equals(appType)) {
            //查询后端组件信息
            Map<String, Object> componentInfo = componentService.queryComponentServerDetail(projectId);
            List<Map> managers = (List<Map>) componentInfo.get(Dict.MANAGER_ID);
            List<String> managerIds = managers.stream().map(manager -> (String) manager.get(Dict.ID)).collect(Collectors.toList());
            return new HashMap<String, Object>() {{
                put(Dict.GITLAB_ID, String.valueOf(componentInfo.get(Dict.GITLABID)));
                put(Dict.GITLABURL, componentInfo.get(Dict.GITLAB_URL));
                put(Dict.PROJECTNAME, componentInfo.get(Dict.NAME_EN));
                put(Dict.ISTEST, componentInfo.get(Dict.ISTEST));
                put(Dict.SONARSCANSWITCH, componentInfo.get(Dict.SONARSCANSWITCH));
                put(Dict.MANAGER, managerIds);
            }};
        } else if (Constants.ARCHETYPE_WEB.equals(appType)) {
            //查询前端骨架信息
            Map<String, Object> archetypeInfo = componentService.queryArchetypeWebDetail(projectId);
            List<Map> managers = (List<Map>) archetypeInfo.get(Dict.MANAGER);
            List<String> managerIds = managers.stream().map(manager -> (String) manager.get(Dict.ID)).collect(Collectors.toList());
            return new HashMap<String, Object>() {{
                put(Dict.GITLAB_ID, String.valueOf(archetypeInfo.get(Dict.GITLABID)));
                put(Dict.GITLABURL, archetypeInfo.get(Dict.GITLAB_URL));
                put(Dict.PROJECTNAME, archetypeInfo.get(Dict.NAME_EN));
                put(Dict.ISTEST, archetypeInfo.get(Dict.ISTEST));
                put(Dict.SONARSCANSWITCH, "0");
                put(Dict.MANAGER, managerIds);
            }};
        } else if (Constants.ARCHETYPE_SERVER.equals(appType)) {
            //查询后端骨架信息
            Map<String, Object> archetypeInfo = componentService.queryArchetypeServerDetail(projectId);
            List<Map> managers = (List<Map>) archetypeInfo.get(Dict.MANAGER_ID);
            List<String> managerIds = managers.stream().map(manager -> (String) manager.get(Dict.ID)).collect(Collectors.toList());
            return new HashMap<String, Object>() {{
                put(Dict.GITLAB_ID, String.valueOf(archetypeInfo.get(Dict.GITLABID)));
                put(Dict.GITLABURL, archetypeInfo.get(Dict.GITLAB_URL));
                put(Dict.PROJECTNAME, archetypeInfo.get(Dict.NAME_EN));
                put(Dict.ISTEST, archetypeInfo.get(Dict.ISTEST));
                put(Dict.SONARSCANSWITCH, archetypeInfo.get(Dict.SONARSCANSWITCH));
                put(Dict.MANAGER, managerIds);
            }};
        } else if (Constants.IMAGE.equals(appType)) {
            //查询镜像信息
            Map<String, Object> imageInfo = componentService.queryImageDetail(projectId);
            List<Map> managers = (List<Map>) imageInfo.get(Dict.MANAGER);
            List<String> managerIds = managers.stream().map(manager -> (String) manager.get(Dict.ID)).collect(Collectors.toList());
            return new HashMap<String, Object>() {{
                put(Dict.GITLAB_ID, String.valueOf(imageInfo.get(Dict.GITLABID)));
                put(Dict.GITLABURL, imageInfo.get(Dict.GITLAB_URL));
                put(Dict.PROJECTNAME, imageInfo.get(Dict.NAME));
                put(Dict.ISTEST, imageInfo.get(Dict.ISTEST));
                put(Dict.SONARSCANSWITCH, "0");
                put(Dict.MANAGER, managerIds);
            }};
        } else {
            //查询应用信息
            Map queryTerm = new HashMap();
            queryTerm.put(Dict.ID, projectId);
            Map appInfo = appApi.queryAppById(queryTerm);
            if (CommonUtils.isNullOrEmpty(appInfo)) {
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"应用不存在"});
            }

            Map system = (Map) appInfo.get(Dict.SYSTEM);
            String issueSystem = CommonUtils.isNullOrEmpty(system) ? "个人手机银行系统" : (String) system.get(Dict.NAME);
            List<Map> managers = (List<Map>) appInfo.get(Dict.SPDB_MANAGERS);
            managers.addAll((List<Map>) appInfo.get(Dict.DEV_MANAGERS));
            List<String> managerIds = managers.stream().map(manager -> (String) manager.get(Dict.ID)).distinct().collect(Collectors.toList());
            return new HashMap<String, Object>() {{
                put(Dict.GITLAB_ID, String.valueOf(appInfo.get(Dict.GITLAB_PROJECT_ID)));
                put(Dict.GITLABURL, appInfo.get(Dict.GIT));
                put(Dict.PROJECTNAME, appInfo.get(Dict.NAME_EN));
                put(Dict.ISTEST, appInfo.get(Dict.ISTEST));
                put(Dict.SONARSCANSWITCH, appInfo.get(Dict.SONAR_SCAN_SWITCH));
                put(Dict.MANAGER, managerIds);
                put(Dict.APPCITYPE, appInfo.get(Dict.APPCITYPE));
                put(Dict.ISSUESYSTEM, issueSystem);
            }};
        }
    }

    @Override
    public void updateTaskInfo(FdevTask task) throws Exception {
        User user = userVerifyUtil.getRedisUser();
        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL);
        }
        //修改前的任务信息
        FdevTask oldTask = fdevTaskDao.queryById(task.getId());
        // 任务创建者，行内，任务负责人可以修改任务
        List userlist = new ArrayList();
        userlist.addAll(Arrays.asList(oldTask.getMaster()));
        userlist.addAll(Arrays.asList(oldTask.getSpdb_master()));
        if (CommonUtils.isNotNullOrEmpty(oldTask.getCreator())) {
            userlist.add(oldTask.getCreator());
        }
        if (CommonUtils.isNullOrEmpty(task)) {
            //增加卡点管理员判断
            if (!userlist.contains(user.getId()) && !userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"您没有权限修改该任务!"});
            }
        }
        String startUatTestTimeOld = oldTask.getStart_uat_test_time();//修改前开始uat测试时间
        String startRelTestTimeOld = oldTask.getStart_rel_test_time();//修改前开始rel测试时间
        String startUatTestTimeNew = task.getStart_uat_test_time();//修改后开始uat测试时间
        String startRelTestTimeNew = task.getStart_rel_test_time();//修改后开始rel测试时间
        //校验时间是否正常
        fdevTaskUtils.checkTaskTime(task.getId(), startUatTestTimeNew, startRelTestTimeNew, oldTask.getProWantWindow(),
                oldTask.getApplicationType(), startUatTestTimeOld, startRelTestTimeOld);
        //实施单元拆分，将uat完成日期和rel启动日期字段合并，确保2个字段的赋值一致
        task.setStop_uat_test_time(task.getStart_rel_test_time());
        //校验该task是否处于暂缓状态
        this.fdevTaskUtils.checkIsWaitTask(oldTask.getTaskSpectialStatus());
        //校验是否修改了上线确认书到达时间，确认书开关打开后才能修改
        String confirmFileDateNew = task.getConfirmFileDate();//修改后确认书到达时间
        if (!CommonUtils.isNullOrEmpty(confirmFileDateNew) && !"1".equals(oldTask.getConfirmBtn())) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"上线确认书开关未打开，无法修改确认书到达时间"});
        }
        if ("".equals(confirmFileDateNew) && !CommonUtils.isNullOrEmpty(oldTask.getConfirmFileDate())) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"确认书到达时间不能清空"});
        }
        //校验计划时间是否在研发单元计划时间范围内
        fdevTaskUtils.checkPlanTime(task);
        //如果更改了研发单元，同步更改工单信息
        task = fdevTaskUtils.updateTaskByImplUnit(task, oldTask);
        //如果修改了计划时间，修改工单时间
        Map changedDate = CommonUtils.taskDateChanged(task, oldTask);
        if (!CommonUtils.isNullOrEmpty(changedDate)) {
            Map param = iTestApi.generateRequestParam(task.getId(), changedDate);
            if (!CommonUtils.isNullOrEmpty(param)) {
                iTestApi.updateOrder(param);
            }
            logger.info("更新任务时间!" + JSONObject.fromObject(changedDate).toString());
        }
        //如果任务数据变更修改为否,删除任务历史的审核记录
        boolean changeDataBaseReviewFlag = fdevTaskUtils.changeDataBaseReviewFlag(task.getReview());
        if (!changeDataBaseReviewFlag) {
            fdevTaskUtils.setDataBaseReview(task, oldTask.getReview());
        }
        //任务难度描述改动
        fdevTaskUtils.updateDifficultuDesc(task, oldTask, user.getId());
        String newStage = task.getStage();
        String oldStage = oldTask.getStage();
        //日常任务点已完成按钮
        if ("2".equals(String.valueOf(oldTask.getTaskType()))
                && CommonUtils.isNotNullOrEmpty(newStage, oldStage)
                && Dict.TASK_STAGE_DEVELOP.equals(oldStage)
                && Dict.TASK_STAGE_PRODUCTION.equals(newStage)) {
            task.setFire_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1));
        }
        //判断是否修改了用户测试时间
        boolean updateTimeFlag = fdevTaskUtils.updateTimeFlag(startUatTestTimeNew, startRelTestTimeNew, startUatTestTimeOld, startRelTestTimeOld);
        //修改阶段
        //如果传了提交用户测试时间，且阶段是SIT(传了阶段字段用传入的，没传就用原数据的)，将阶段改成UAT
        //如果清空了用户测试完成时间，且阶段是REL(传了阶段字段用传入的，没传就用原数据的)，将阶段改成UAT
        if ((!CommonUtils.isNullOrEmpty(startUatTestTimeNew) && (Dict.TASK_STAGE_SIT.equals(task.getStage())
                || CommonUtils.isNullOrEmpty(task.getStage()) && Dict.TASK_STAGE_SIT.equals(oldTask.getStage())))
                || ("".equals(startRelTestTimeNew) && (Dict.TASK_STAGE_REL.equals(task.getStage())
                || CommonUtils.isNullOrEmpty(task.getStage()) && Dict.TASK_STAGE_REL.equals(oldTask.getStage())))) {
            task.setStage(Dict.TASK_STAGE_UAT);
            newStage = Dict.TASK_STAGE_UAT;
        }
        //如果传了用户测试完成时间，且阶段是UAT(传了阶段字段用传入的，没传就用原数据的)，将阶段改成REL
        if (!CommonUtils.isNullOrEmpty(startRelTestTimeNew) && (Dict.TASK_STAGE_UAT.equals(task.getStage())
                || CommonUtils.isNullOrEmpty(task.getStage()) && Dict.TASK_STAGE_UAT.equals(oldTask.getStage()))) {
            task.setStage(Dict.TASK_STAGE_REL);
            newStage = Dict.TASK_STAGE_REL;
        }
        //修改数据库
        FdevTask ftask = fdevTaskDao.update(task);
        //如果修改了上线确认书时间，重新调用投产模块接口通知他们
        if (!CommonUtils.isNullOrEmpty(confirmFileDateNew) && !confirmFileDateNew.equals(oldTask.getConfirmFileDate())) {
            ReleaseTaskApi.confirmRqrmntInfoTag(ftask.getConfirmFileDate(), ftask.getId());
        }
        asyncService.syncSubTask(ftask);
        //归档方法很多，统一在service过滤
        if (!CommonUtils.isNullOrEmpty(ftask.getRedmine_id())) {
            task.setRedmine_id(ftask.getRedmine_id());
        }
        asyncService.updateRedMineDate(task);
        //删除 接口调用关系
        asyncService.deleteForInterface(task, oldTask);
        asyncService.deleteReviewRecordCaseByTaskStageChange(task, ftask);
        //推送阶段和时间到研发单元
        fdevTaskUtils.sendStageAndTime(ftask, oldTask, updateTimeFlag, newStage);
        //和jira关联的需要更新故事和子任务状态
        if (!CommonUtils.isNullOrEmpty(oldTask.getStoryId()) && CommonUtils.isNotNullOrEmpty(newStage, oldStage) && !oldStage.equals(oldStage)) {
            //更新故事状态
            logger.info("更新故事状态:" + oldTask.getStoryId());
            asyncService.updateJiraStoryStatus(oldTask);
            if (Dict.TASK_STAGE_ABORT.equals(newStage) || Dict.TASK_STAGE_DISCARD.equals(newStage)) {
                //删除子任务
                logger.info("删除子任务:" + oldTask.getJiraKey());
                asyncService.deleteJiraSubTask(oldTask.getJiraKey());
            } else {
                logger.info("更新子任务状态" + ftask.getJiraKey());
                asyncService.updateJiraSubTaskStatus(ftask);
            }
        }
        //开发人员变更需要更新jira经办人
        if (!CommonUtils.isNullOrEmpty(oldTask.getStoryId()) && null != task.getDeveloper()) {
            asyncService.updateJiraAssignee(oldTask.getJiraKey(), 0 == task.getDeveloper().length ? "" : task.getDeveloper()[0]);
        }
        //
        Map<String, Object> releaseTaskApiParam = new HashMap<>();
        releaseTaskApiParam.put(Dict.TASK_ID, ftask.getId());
        ReleaseTaskApi.updateRqrmntInfoReview(releaseTaskApiParam);
        if (changeDataBaseReviewFlag) {
            //删除任务之前有的审核历史记录
            reviewRecordService.deleteByTask_Id(ftask.getId());
            //删除任务之前有的审核项
            reviewRecordService.deleteByTaskId(ftask.getId());
        } else {
            //变更为是时判断任务之前有没有审核项
            ReviewRecord reviewRecord = reviewRecordService.queryTaskReviewByTaskId(ftask.getId());
            if (reviewRecord == null) {
                //没有则创建审核
                reviewRecordService.saveSqlReview(ftask);
            }
        }
        HashSet members = fdevTaskUtils.getMembers(oldTask, ftask);
        // 判断无代码变更，不是无代码变更添加git 成员
        if (!CommonUtils.isNullOrEmpty(ftask.getProject_id())) {
            if (!Dict.TASK_STAGE_CREATE_INFO.equals(ftask.getStage()) && !CommonUtils.isNullOrEmpty(members)) {
                //获取关联项目信息
                String appType = ftask.getApplicationType();
                Map<String, Object> projectInfo = getProjectInfo(ftask.getProject_id(), appType);
                String gitlabId = (String) projectInfo.get(Dict.GITLAB_ID);//应用git id
                //现在项目管理员权限不动
                members.removeAll((List<String>) projectInfo.get(Dict.MANAGER));
                addMemberGit(gitlabId, members, appType, ftask.getProject_id());
            }
        }
        //组装邮件信息
        if (isSendMail) {
            asyncService.sendMail(oldTask, ftask, user.getUser_name_cn(), "");
        }
    }

    @Override
    public List queryTaskBaseInfoByIds(List<String> ids, List<String> fields, List<String> responseFields) throws Exception {
        List<FdevTask> fdevTaskList = fdevTaskDao.queryAllTasksByIds((ArrayList) ids);
        List result = new ArrayList();
        Map<String, Object> taskMap;
        if (!CommonUtils.isNullOrEmpty(fields)) {
            Set<String> taskUserIds = new HashSet<>();
            Set<String> taskGroupIds = new HashSet<>();
            for (FdevTask task : fdevTaskList) {
                if (fields.contains(Dict.CREATOR) && !CommonUtils.isNullOrEmpty(task.getCreator())) {
                    taskUserIds.add(task.getCreator());
                }
                if (fields.contains(Dict.DEVELOPER) && !CommonUtils.isNullOrEmpty(task.getDeveloper())) {
                    taskUserIds.addAll(Arrays.asList(task.getDeveloper()));
                }
                if (fields.contains(Dict.MASTER) && !CommonUtils.isNullOrEmpty(task.getMaster())) {
                    taskUserIds.addAll(Arrays.asList(task.getMaster()));
                }
                if (fields.contains(Dict.SPDB_MASTER) && !CommonUtils.isNullOrEmpty(task.getSpdb_master())) {
                    taskUserIds.addAll(Arrays.asList(task.getSpdb_master()));
                }
                if (fields.contains(Dict.TESTER) && !CommonUtils.isNullOrEmpty(task.getTester())) {
                    taskUserIds.addAll(Arrays.asList(task.getTester()));
                }
                if (fields.contains(Dict.REVIEWER) && !CommonUtils.isNullOrEmpty(task.getReviewer())) {
                    taskUserIds.add(task.getReviewer());
                }
                if (fields.contains(Dict.CONCERN) && !CommonUtils.isNullOrEmpty(task.getConcern())) {
                    taskUserIds.addAll(Arrays.asList(task.getConcern()));
                }
                if (fields.contains(Dict.GROUP) && !CommonUtils.isNullOrEmpty(task.getGroup())) {
                    taskGroupIds.add(task.getGroup());
                }
            }
            //批量查询人员信息
            Map<String, Map> userMap = new HashedMap();
            if (!CommonUtils.isNullOrEmpty(taskUserIds)) {
                userMap = userUtils.getUserByIds(taskUserIds);
            }
            //批量查询小组信息
            List<Map> groupList = new ArrayList<>();
            if (!CommonUtils.isNullOrEmpty(taskGroupIds)) {
                groupList = userApi.queryGroupByIds(new ArrayList<>(taskGroupIds));
            }
            //将小组信息和id对应起来
            Map<String, Map> groupMap = new HashMap<>();
            for (Map group : groupList) {
                groupMap.put((String) group.get(Dict.ID), group);
            }
            for (FdevTask task : fdevTaskList) {
                //只返回指定字段
                if (!CommonUtils.isNullOrEmpty(responseFields)) {
                    taskMap = new HashMap<>();
                    fdevTaskUtils.putRepFields(responseFields, taskMap, task, userMap, groupMap);
                } else {
                    taskMap = CommonUtils.beanToMap(task);
                    fdevTaskUtils.addFields(fields, taskMap, task, userMap, groupMap);
                }
                result.add(taskMap);
            }
            return result;
        }
        //不需要非任务表信息，只返回指定字段
        if (!CommonUtils.isNullOrEmpty(responseFields)) {
            Map<String, Object> taskNew = null;
            for (FdevTask task : fdevTaskList) {
                taskMap = CommonUtils.beanToMap(task);
                taskNew = new HashMap<>();
                for (String field : responseFields) {
                    taskNew.put(field, taskMap.get(field));
                }
                result.add(taskNew);
            }
            return result;
        }
        return fdevTaskList;
    }

    @Override
    public FdevTask createReleaseMergeRequest(FdevTask queryTask, Map user, Map releaseApp) throws Exception {
        // 创建合并请求
        String relase = (String) releaseApp.get(Dict.RELEASE_BRANCH);
        String title = queryTask.getName() + "--提交UAT测试";
        String feature = queryTask.getFeature_branch();
        String task_id = queryTask.getId();
        String project_id = queryTask.getProject_id();
        String applicationType = queryTask.getApplicationType();
        if (Constants.APPLICATION_ANDROID.equals(applicationType)
                || Constants.APPLICATION_IOS.equals(applicationType)) {
            feature = feature.replaceFirst("feature", "dev");
        }
        String mid = this.createMergeRequest(queryTask.getApplicationType(), title, project_id,
                "", feature, relase, queryTask.getVersionNum(), user);
        FdevTask rtask = new FdevTask.FdevTaskBuilder().id(task_id).uat_merge_id(mid).init();
        FdevTask uptask = this.update(rtask);
        try {
            asyncService.addTodoList(project_id, uptask.getName() + "--UAT环境分支合并", (String) user.get(Dict.ID),
                    uptask.getApplicationType(), "uatMergeRequest", mid, task_id);
        } catch (Exception e) {
            logger.info("UAT合并待办发送失败" + e.getMessage());
        }

        return uptask;
    }

    /**
     * 判断任务是否要走release合并审核流程，卡点条件为：
     * 1、不是首次合并release；2、任务所属小组的三级组为武汉研发A1-A6
     *
     * @param fdevTask
     * @return
     */
    public boolean releaseMergeFlag(FdevTask fdevTask) throws Exception {
        String group = fdevTask.getGroup();
        Map threeLevelGroup = userUtils.getThreeLevelGroup(group);
        if (releaseMergeTeam.contains(threeLevelGroup.get(Dict.ID))) {
            if (releaseApproveDao.getCountByTaskId(fdevTask.getId()) > 0) {
                return true;//卡点
            }
        }
        return false;
    }

    @Override
    public void updateStartUatTime(String id, String startUatTime) throws Exception {
        FdevTask task = fdevTaskDao.queryById(id);
        if (CommonUtils.isNullOrEmpty(task)) {
            throw new FdevException(ErrorConstants.TASK_NOT_EXIST);
        }
        //只能修改sit、uat和rel阶段的任务
        if (!CommonUtils.isNullOrEmpty(startUatTime) && (Dict.TASK_STAGE_SIT.equals(task.getStage())
                || Dict.TASK_STAGE_UAT.equals(task.getStage()) || Dict.TASK_STAGE_REL.equals(task.getStage()))) {
            //如果原先没有实际提交用户测试时间，将状态推至UAT
            if (CommonUtils.isNullOrEmpty(task.getStart_uat_test_time())) {
                task.setStage(Dict.TASK_STAGE_UAT);
            }
            task.setStart_uat_test_time(startUatTime);
            FdevTask ftask = fdevTaskDao.update(task);
            asyncService.updateRqrmntWhenStageChanged(ftask, false);
        }
    }

    @Override
    public Object queryTaskInfoById(String id, List<String> fields) throws Exception {
        FdevTask task = fdevTaskDao.queryById(id);
        if (!CommonUtils.isNullOrEmpty(fields)) {
            Set<String> taskUserIds = new HashSet<>();
            Set<String> taskGroupIds = new HashSet<>();
            if (fields.contains(Dict.CREATOR) && !CommonUtils.isNullOrEmpty(task.getCreator())) {
                taskUserIds.add(task.getCreator());
            }
            if (fields.contains(Dict.DEVELOPER) && !CommonUtils.isNullOrEmpty(task.getDeveloper())) {
                taskUserIds.addAll(Arrays.asList(task.getDeveloper()));
            }
            if (fields.contains(Dict.MASTER) && !CommonUtils.isNullOrEmpty(task.getMaster())) {
                taskUserIds.addAll(Arrays.asList(task.getMaster()));
            }
            if (fields.contains(Dict.SPDB_MASTER) && !CommonUtils.isNullOrEmpty(task.getSpdb_master())) {
                taskUserIds.addAll(Arrays.asList(task.getSpdb_master()));
            }
            if (fields.contains(Dict.TESTER) && !CommonUtils.isNullOrEmpty(task.getTester())) {
                taskUserIds.addAll(Arrays.asList(task.getTester()));
            }
            if (fields.contains(Dict.REVIEWER) && !CommonUtils.isNullOrEmpty(task.getReviewer())) {
                taskUserIds.add(task.getReviewer());
            }
            if (fields.contains(Dict.CONCERN) && !CommonUtils.isNullOrEmpty(task.getConcern())) {
                taskUserIds.addAll(Arrays.asList(task.getConcern()));
            }
            if (fields.contains(Dict.GROUP) && !CommonUtils.isNullOrEmpty(task.getGroup())) {
                taskGroupIds.add(task.getGroup());
            }
            //批量查询人员信息
            Map<String, Map> userMap = new HashedMap();
            if (!CommonUtils.isNullOrEmpty(taskUserIds)) {
                userMap = userUtils.getUserByIds(taskUserIds);
            }
            //批量查询小组信息
            List<Map> groupList = new ArrayList<>();
            if (!CommonUtils.isNullOrEmpty(taskGroupIds)) {
                groupList = userApi.queryGroupByIds(new ArrayList<>(taskGroupIds));
            }
            //将小组信息和id对应起来
            Map<String, Map> groupMap = new HashMap<>();
            for (Map group : groupList) {
                groupMap.put((String) group.get(Dict.ID), group);
            }
            Map<String, Object> taskMap = CommonUtils.beanToMap(task);
            if (fields.contains(Dict.MASTER)) {
                taskMap.put(Dict.MASTER, fdevTaskUtils.getUserName(task.getMaster(), userMap));
            }
            if (fields.contains(Dict.SPDB_MASTER)) {
                taskMap.put(Dict.SPDB_MASTER, fdevTaskUtils.getUserName(task.getSpdb_master(), userMap));
            }
            if (fields.contains(Dict.DEVELOPER)) {
                taskMap.put(Dict.DEVELOPER, fdevTaskUtils.getUserName(task.getDeveloper(), userMap));
            }
            if (fields.contains(Dict.CONCERN)) {
                taskMap.put(Dict.CONCERN, fdevTaskUtils.getUserName(task.getConcern(), userMap));
            }
            if (fields.contains(Dict.CREATOR)) {
                taskMap.put(Dict.CREATOR, fdevTaskUtils.getUserName(task.getCreator(), userMap));
            }
            if (fields.contains(Dict.REVIEWER)) {
                taskMap.put(Dict.REVIEWER, fdevTaskUtils.getUserName(task.getReviewer(), userMap));
            }
            if (fields.contains(Dict.TESTER)) {
                taskMap.put(Dict.TESTER, fdevTaskUtils.getUserName(task.getTester(), userMap));
            }
            if (fields.contains(Dict.GROUP)) {
                taskMap.put(Dict.GROUP, fdevTaskUtils.getGroupName(task.getGroup(), groupMap));
            }
            return taskMap;
        }
        return task;
    }

    @Override
    public void skipInnerTest(Map<String, Object> param) throws Exception {
        FdevTask task = fdevTaskDao.queryById((String) param.get(Dict.ID));
        if (CommonUtils.isNullOrEmpty(task)) {
            return;
        }
        // 生成测试报告
        String simpleFlag = CommonUtils.isNullOrEmpty(task.getSimpleDemand()) ? "1" : task.getSimpleDemand();
        param.put("simpleDemand", simpleFlag);
        docService.testReportCreate(param);
        // 更新任务内测开始时间，结束时间为当前时间
        task.setStart_inner_test_time(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1)); // 实际内测时间
        task.setStage(Dict.SIT);
        // 更新标识,返回给前端
        task.setSkipFlag("1");
        fdevTaskDao.updateInnerTestTime(task);
    }

    @Override
    public void exportAppTask(HttpServletResponse response) throws IOException {
        //查询使用中的应用信息
        List<Map> appList = new ArrayList<>();
        try {
            Map<String, Object> param = new HashMap<>();
            param.put(Dict.INDEX, 1);
            param.put("size", 0);
            param.put(Dict.REST_CODE, "queryAllApp");
            Map<String, Object> appResult = (Map<String, Object>) restTransport.submit(param);
            appList = (List<Map>) appResult.get("appEntity");
        } catch (Exception e) {
            logger.info(">>>>>查询应用数据异常,{}", e.getStackTrace());
        }
        List<FdevTask> taskList = fdevTaskDao.queryTaskInStage(CommonUtils.getTestTask());
        Map<String, List<FdevTask>> taskMap = taskList.stream().collect(Collectors.groupingBy(task -> task.getProject_id()));
        for (Map app : appList) {
            app.remove(Dict.SIT);
            app.remove(Dict.UAT);
            app.remove(Dict.REL_LOWERCASE);
            String appId = (String) app.get(Dict.ID);
            if (taskMap.get(appId) == null) {
                app.put(Dict.SIT, 0);
                app.put(Dict.UAT, 0);
                app.put(Dict.REL_LOWERCASE, 0);
            } else {
                Map<String, Long> countMap = taskMap.get(appId).stream().collect(Collectors.groupingBy(task -> task.getStage(), Collectors.counting()));
                app.putAll(countMap);
            }
        }
        //导出数据
        //报表每列所取字段的对应关系
        LinkedHashMap<String, String> columnMap = new LinkedHashMap<>();
        columnMap.put(Dict.NAME_EN, "微服务中文名");
        columnMap.put(Dict.NAME_ZH, "微服务英文名");
        columnMap.put(Dict.SIT, "sit阶段任务数");
        columnMap.put(Dict.UAT, "uat阶段任务数");
        columnMap.put(Dict.REL_LOWERCASE, "rel阶段任务数");
        CommonUtils.exportDataToExcel(response, appList, columnMap, "应用任务数量表");
    }
}
