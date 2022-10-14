package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import com.spdb.fdev.common.FdevResponseBodyAdvice;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.GitlabTransport;
import com.spdb.fdev.fdevtask.spdb.dao.TaskDao;
import com.spdb.fdev.fdevtask.spdb.entity.*;
import com.spdb.fdev.fdevtask.spdb.service.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: fdev-process
 * @description:
 * @author: c-jiangl2
 * @create: 2021-01-25 13:29
 **/
@Service
@RefreshScope
public class TaskServiceImpl implements TaskService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志

    @Value("${git.token}")
    private String token;

    @Autowired
    private GitlabTransport gitlabTransport;

    @Autowired
    private ProcessApiImpl processApi;

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private IUserApi iUserApi;

    @Resource
    private FdocmanageService fdocmanageService;

    @Resource
    private MongoTemplate mongoTemplate;

    @Autowired
    private IAppApi appApi;

    @Autowired
    private IUserApi userApi;

    @Autowired
    private DemandService demandService;

    @Autowired
    private DemandManageApi demandManageApi;

    @Autowired
    private OperateChangeLogService operateChangeLogService;

    @Autowired
    private ServiceApi serviceApi;

    @Autowired
    private UserManageApi userManageApi;

    @Autowired
    private GitApiService gitApiService;

    @Autowired
    public UserVerifyUtil userVerifyUtil;

    @Autowired
    private InteractTestApi interactTestApi;

    @Override
    public Task createTask(Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        //任务名称重复处理
        String taskName = (String) request.get(Dict.NAME);
        if(CommonUtils.isNullOrEmpty(taskName)){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"任务名不能为空"});
        }
        Task task1 = new Task();
        task1.setName(taskName);
        task1.setDelete(0);
        List<Task> allTasks = taskDao.query(task1);
        if (allTasks.size() > 0) {
            throw new FdevException(ErrorConstants.TASK_NAME_EROOR);
        }
        String now = CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN);
        Task task = CommonUtils.mapToBean(request,Task.class);
        String workload = task.getWorkload();
        if(CommonUtils.isNotNullOrEmpty(workload)){
            try{
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                task.setWorkload(decimalFormat.format(new BigDecimal(workload)));
            }catch (Exception e){
                throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"工作量应为00.00类型"});
            }
        }
        String planStartDate = (String) request.get(Dict.PLAN_START_DATE);
        String planEndDate = (String) request.get(Dict.PLAN_END_DATE);
        task.setPlanStartDate(planStartDate);
        task.setPlanEndDate(planEndDate);
        if(CommonUtils.isNotNullOrEmpty(planStartDate,planEndDate)){
            if(compareDate(planStartDate,planEndDate)){
                throw new FdevException(ErrorConstants.DATE_ERROR);
            }
        }
        ObjectId id = new ObjectId();
        task.set_id(id);
        task.setId(id.toString());
        task.setCreateTime(now);
        task.setCreateUserId(user.getId());
        task.setUpdateTime(now);
        task.setDelete(0);
        task.setConfirmRelease("0");
        String requirementId = task.getRequirementId();
        if(CommonUtils.isNullOrEmpty(requirementId)){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"需求不能为空"});
        }
        //团队id的获取方式
        String teamId = (String)request.get(Dict.TEAM_ID);
        task.setTeamId(teamId);
        //启动时间怎么计算
        task.setStartDate(task.getPlanStartDate());
        String processId = (String)request.get(Dict.PROCESS_ID);
        //新建任务实例 以及保存任务属性
        processApi.createProcessInstance(id.toString(),processId);
        Map processInstance = processApi.queryInstanceByTaskId(id.toString());
        task.setStatus((String)processInstance.get(Dict.TASK_STATUS_ID));
        task.setProcessId(processId);
        //存储执行人组id
        String[] assigneeList = task.getAssigneeList();
        if(CommonUtils.isNullOrEmpty(assigneeList)){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"请选择执行人"});
        }
        if(assigneeList.length > 1){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"执行人员数目过多，当前限制为1人"});
        }
        Map assigneeUser = userManageApi.query(assigneeList[0]);
        task.setAssigneeGroupId((String)assigneeUser.get(Dict.GROUP_ID));
        //关联应用分支为新增时调用新增分支接口
        if (CommonUtils.isNotNullOrEmpty((String)request.get(Dict.BRANCHNAME),task.getRelatedApplication())){
            relatedBranch(task,request);
        }
        operateChangeLogService.asyncLogCreate(task);
        //修改需求状态
        demandManageApi.queryDemandStatus(requirementId,"1");
        //判断研发单元编号是否填写通知玉衡
        if(!CommonUtils.isNullOrEmpty(task.getImplUnitId())){
            if (testInform(processInstance,task)){
                interactTestApi.newCreateTask(task.getId(),task.getImplUnitId(),task.getName());
            }
        }
        Task save = taskDao.save(task);
        return save;
    }

    @Override
    public Map updateTask(Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        //任务名称重复处理
        String taskName = (String) request.get(Dict.NAME);
        if(CommonUtils.isNullOrEmpty(taskName)){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"任务名不能为空"});
        }
        Task task = new Task();
        task.setId((String) request.get(Dict.ID));
        List<Task> query = taskDao.query(task);
        if(CommonUtils.isNullOrEmpty(query)){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"任务不存在"});
        }
        Task currentTask = query.get(0);
        Task originalTask = CommonUtils.mapToBean(request, Task.class);
        //判断工作量值
        if(CommonUtils.isNotNullOrEmpty(currentTask.getWorkload()) && CommonUtils.isNotNullOrEmpty(originalTask.getWorkload())
                && !currentTask.getWorkload().equals(originalTask.getWorkload())){
            String workload = originalTask.getWorkload();
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            originalTask.setWorkload(decimalFormat.format(new BigDecimal(workload)));
        }
        //判断结束时间
        if(CommonUtils.isNotNullOrEmpty(currentTask.getEndDate()) && CommonUtils.isNullOrEmpty(originalTask.getEndDate())){
            originalTask.setEndDate(currentTask.getEndDate());
        }
        //计划开始结束时间判断
        if(CommonUtils.isNotNullOrEmpty(originalTask.getPlanStartDate()) && CommonUtils.isNotNullOrEmpty(originalTask.getPlanEndDate()) && compareDate(originalTask.getPlanStartDate(),originalTask.getPlanEndDate())){
            throw new FdevException(ErrorConstants.PLAN_DATE_ERROR);
        }
        //开始结束时间判断
        if(CommonUtils.isNotNullOrEmpty(originalTask.getEndDate(),originalTask.getStartDate())){
            if(compareDate(originalTask.getStartDate(),originalTask.getEndDate())){
                throw new FdevException(ErrorConstants.DATE_ERROR);
            }
        }
        originalTask.setUpdateTime(CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN));
        //执行人判断
        String[] assigneeList = originalTask.getAssigneeList();
        if(CommonUtils.isNullOrEmpty(assigneeList)){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"请选择执行人"});
        }
        if(assigneeList.length > 1){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"执行人员数目过多，当前限制为1人"});
        }
        //判断分支
        if(CommonUtils.isNullOrEmpty(currentTask.getBranchName()) && !CommonUtils.isNullOrEmpty(originalTask.getBranchName())){
            relatedBranch(originalTask,request);
        }
        //判断研发单元编号是否填写通知玉衡
        if(!CommonUtils.isNullOrEmpty(originalTask.getImplUnitId())){
            Map<String, Object> processInstance = processApi.queryInstanceByTaskId((String) request.get(Dict.ID));
            if (testInform(processInstance,originalTask)){
                if(CommonUtils.isNullOrEmpty(currentTask.getImplUnitId())){
                    interactTestApi.newCreateTask(task.getId(),task.getImplUnitId(),task.getName());
                }else {
                    if (!currentTask.getImplUnitId().equals(originalTask.getImplUnitId())){
                        interactTestApi.updateTaskUnitNo(task.getId(),task.getName(),task.getImplUnitId());
                    }
                }
            }
        }
        operateChangeLogService.asyncUpdateLog(originalTask,currentTask,currentTask.getId());
        taskDao.updateTaskInfo(originalTask);
        return null;
    }

    @Override
    public Map getTaskList(Map params) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        Map result = taskDao.getTaskList(user.getId(), params);
        List<Task> taskList = CommonUtils.castList(result.get(Dict.TASK), Task.class);
        List<Map> tasks = updateTaskValue(taskList);
        result.put(Dict.TASK,tasks);
        result.put(Dict.STATUS_LIST,getProcessListByRequirementId(taskList));
        return result;
    }


    @Override
    public Map getTaskById(Map request) throws Exception {
        String id = (String)request.get(Dict.ID);
        Task task = new Task();
        task.setId(id);
        List<Task> tasks = taskDao.query(task);
        ArrayList<String> taskIds = new ArrayList<>();
        for(Task taskTemp:tasks) {
            taskIds.add(taskTemp.getId());
        }
        List<Map> instance = processApi.queryInstanceByTaskIds(taskIds);
        ArrayList<Map> taskMaps = new ArrayList<>();
        for (Task taskTemp:tasks) {
            Map taskMap = CommonUtils.beanToMap(taskTemp);
            taskMaps.add(taskMap);
        }
        for (Map taskTemp:taskMaps) {
            getStatusName(taskTemp,instance);
        }
        return taskMaps.get(0);
    }


    @Override
    public Map getTaskDetail(Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }

        //查询任务信息
        String taskId = (String) request.get(Dict.TASKID);
        Task queryTask = new Task();
        queryTask.setId(taskId);
        List<Task> tasks = taskDao.query(queryTask);
        if(tasks.size() == 0){
            throw new FdevException(ErrorConstants.TASK_NOT_EXIST);
        }
        Task task = tasks.get(0);
        Map<String,Object> map = CommonUtils.beanToMap(task);
        //需求编号赋值
        Map requirement = demandManageApi.queryDemandManageInfoDetail(task.getRequirementId());
        map.put(Dict.REQUIREMENT_NO,requirement.get(Dict.OA_REAL_NO));
        map.put(Dict.REQUIREMENT_NAME,requirement.get(Dict.OA_CONTACT_NAME));
        //创建人姓名赋值
        Map userMap = userManageApi.query(task.getCreateUserId());
        map.put(Dict.CREATE_USER_NAME,userMap.get(Dict.USER_NAME_CN));
        //受理人姓名
        Map<String, List<String>> queryUsers = new HashMap<>();
        String[] assigneeList = task.getAssigneeList();
        queryUsers.put(Dict.IDS,Arrays.asList(assigneeList));
        List userList = new ArrayList();
        userList.add(userManageApi.query(assigneeList[0]));
        map.put(Dict.ASSIGNEE_LIST,userList);
        //组名称赋值
        ArrayList<String> groupIds = new ArrayList<>();
        groupIds.add(task.getAssigneeGroupId());
        List<Map> groups = userApi.queryGroupByIds(groupIds);
        if(!CommonUtils.isNullOrEmpty(groups)){
            Map group = groups.get(0);
            map.put(Dict.GROUPNAME,group.get(Dict.NAME));
        }
        //研发单元编号赋值
        if(!CommonUtils.isNullOrEmpty(task.getImplUnitId())){
            ArrayList<String> implId = new ArrayList<>();
            implId.add(task.getImplUnitId());
            List<Map> impls = demandManageApi.queryIpmpListByid(implId);
            Map impl = new HashMap();
            if (!CommonUtils.isNullOrEmpty(impls)){
                impl = impls.get(0);
            }
            if (!CommonUtils.isNullOrEmpty(impl)){
                map.put(Dict.IMPL_CONTENT,impl.get(Dict.IMPL_CONTENT));
                map.put(Dict.IMPL_UNIT_NO,impl.get(Dict.IMPL_UNIT_NUM));
            }
        }
        //状态列表
        ArrayList<Object> emptyList = new ArrayList<>();
        Map<String, Object> currentStatus;
        Map<String,Object> processInstance = processApi.queryInstanceByTaskId(taskId);
        List<Map<String,Object>> processStatusList = (List<Map<String,Object>>)processInstance.get(Dict.PROCESS_STATUS_LIST);
        ArrayList<Map> beginProcess = new ArrayList<>();
        ArrayList<Map> process = new ArrayList<>();
        ArrayList<Map> endProcess = new ArrayList<>();
        for (Map processStatus:processStatusList) {
            if((int)processStatus.get("statusType") == 0){
                beginProcess.add(processStatus);
            }
            if((int)processStatus.get("statusType") == 1){
                process.add(processStatus);
            }
            if((int)processStatus.get("statusType") == 2){
                endProcess.add(processStatus);
            }
        }
        beginProcess.addAll(process);
        beginProcess.addAll(endProcess);
        //所有流程状态
        map.put(Dict.STATUS_LIST,beginProcess);
        for (Map emptyStatus:processStatusList) {
            if(CommonUtils.isNullOrEmpty(emptyStatus.get(Dict.OUT_LIST))){
                emptyStatus.put(Dict.OUT_LIST,emptyList);
            }
            if(CommonUtils.isNullOrEmpty(emptyStatus.get(Dict.IN_LIST))){
                emptyStatus.put(Dict.IN_LIST,emptyList);
            }
        }
        ArrayList<Map> currentMap = new ArrayList<>();
        ArrayList<Map> beforeMap = new ArrayList<>();
        ArrayList<Map> afterMap = new ArrayList<>();
        Map<String, Object> processStatus = processStatusList.stream().filter(p -> p.get(Dict.ID).equals(processInstance.get(Dict.TASK_STATUS_ID))).findFirst().orElse(null);

        //当前状态名称
        map.put(Dict.STATUS,processStatus.get(Dict.NAME));
        //获取前后流程
        HashMap<String, List<Map>> threeStatus = new HashMap<>();
        currentMap.add(processStatus);
        currentStatus = processStatus;
        threeStatus.put(Dict.CURRENT,currentMap);
        List<Map> taskHistoryStatus = (List<Map>) processInstance.get(Dict.TASK_HISTORY_STATUS);
        if(taskHistoryStatus.size() > 1){
            Map taskHistory = taskHistoryStatus.get(taskHistoryStatus.size() - 2);
            String beforeTaskId = (String)taskHistory.get(Dict.ID);
            beforeMap.add(processStatusList.stream().filter(p -> p.get(Dict.ID)
                    .equals(beforeTaskId)).findFirst().orElse(null));
        }

        if(!CommonUtils.isNullOrEmpty(processStatus.get(Dict.OUT_LIST))){
            List<Map<String,Object>> outList = (List<Map<String,Object>>) processStatus.get(Dict.OUT_LIST);
            afterMap.addAll(processStatusList.stream().filter( status -> outList.stream().anyMatch(out -> status.get(Dict.ID)
                    .equals(out.get(Dict.FDEV_PROCESS_STATUS_ID)))).collect(Collectors.toList()));
        }
        threeStatus.put(Dict.BEFORE,beforeMap);
        threeStatus.put(Dict.AFTER,afterMap);
        map.put(Dict.THREE_STATUS,threeStatus);

        ArrayList<Map<String,Object>> activitys = new ArrayList<>();
        //获取当前状态组件列表
        List<Map> outList = (List<Map>) currentStatus.get(Dict.OUT_LIST);
        ArrayList<String> activityIds = new ArrayList<>();
        for (Map out:outList) {
            if (CommonUtils.isNullOrEmpty(out.get(Dict.ACTIVITY_LIST))){
                continue;
            }
            List<Map> activityList = (List<Map>) out.get(Dict.ACTIVITY_LIST);
            for (Map acctivity:activityList) {
                activityIds.add((String)acctivity.get(Dict.ACTIVITY_ID));
                acctivity.put(Dict.SOURCE_STATUS_ID,task.getStatus());
                acctivity.put(Dict.DEST_STATUS_ID,out.get(Dict.FDEV_PROCESS_STATUS_ID));
                activitys.add(acctivity);
            }
        }
        List<Map> activityList = processApi.queryComponentByIds(activityIds);
        for (Map activity:activityList) {
            for (Map activityTemp:activitys) {
                if(activity.get(Dict.ID).equals(activityTemp.get(Dict.ACTIVITY_ID))){
                    activityTemp.putAll(activity);
                }
            }
        }
        //获取组件执行信息
        if (!CommonUtils.isNullOrEmpty(processInstance.get(Dict.COMPONENT_STATUS_LIST))){
            List<Map> componentSta = CommonUtils.castList(processInstance.get(Dict.COMPONENT_STATUS_LIST), Map.class).stream()
                    .filter(componentStatus -> componentStatus.get(Dict.SOURCE_ID).equals(processInstance.get(Dict.TASK_STATUS_ID))).collect(Collectors.toList());
            if(!CommonUtils.isNullOrEmpty(componentSta)){
               for (Map componentResult:componentSta) {
                   List<Map> componentMapList = CommonUtils.castList(componentResult.get(Dict.COMPONENT_RESULT_STATUS),Map.class) ;
                   if(CommonUtils.isNullOrEmpty(componentMapList)){
                       break;
                   }
                   for (Map componentMap:componentMapList){
                       for (Map activityTemp:activitys) {
                           if(activityTemp.get(Dict.ACTIVITY_ID).equals(componentMap.get(Dict.COMPONENT_ID)) && activityTemp.get(Dict.DEST_STATUS_ID).equals(componentResult.get(Dict.DEST_ID))){
                               if(!CommonUtils.isNullOrEmpty(componentMap.get(Dict.EXE_STATUS))){
                                   activityTemp.put(Dict.EXESTATUS,componentMap.get(Dict.EXE_STATUS));
                               }
                           }
                       }
                   }
                }
            }
        }
        for (Map value:activitys) {
            if(!value.containsKey(Dict.EXESTATUS)){
                value.put(Dict.EXESTATUS,"0");
            }
        }
        map.put(Dict.ACTIVITY,activitys);
        return map;
    }

    @Override
    public Long uploadFile(String taskId, List<Map<String, String>> doc, MultipartFile[] file) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        //准备上传
        boolean result = fdocmanageService.uploadFilestoMinio("fdev-task", doc, file, user);
        //上传完毕后更新任务数据库
        if (result) {
            Long aLong = updateTaskDoc(taskId, doc);
            logger.info("文物材料更新结果update={}", aLong);
            return aLong;
        }
        throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"上传文件失败,请重试!"});
    }

    /**
     * 更新单个任务的doc
     *
     * @param taskId
     * @param doc
     * @return
     */
    @Override
    public Long updateTaskDoc(String taskId, List<Map<String, String>> doc) throws Exception {
        Task task = new Task();
        task.setId(taskId);
        List<Task> tasks = taskDao.query(task);
        if(tasks.size() == 0){
            throw new FdevException(ErrorConstants.TASK_NOT_EXIST);
        }
        List<Map<String, String>> newDoc = Optional.ofNullable(tasks.get(0).getNewDoc()).orElse(new ArrayList<>());
        newDoc.addAll(doc);
        newDoc = newDoc
                .stream()
                .distinct()
                .filter(map -> !CommonUtils.isNullOrEmpty(map))
                .collect(Collectors.toList());
        Query query = Query.query(Criteria.where(Dict.ID).is(taskId));
        Update update = new Update()
                .set("newDoc", newDoc);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Task.class);
        return updateResult.getMatchedCount();
    }

    /**
     * @param taskId
     * @return
     * @throws Exception
     */
    @Override
    public Map queryDocDetail(String taskId) throws Exception {
        Task task = new Task();
        task.setId(taskId);
        List<Task> list = taskDao.query(task);
        if (CommonUtils.isNullOrEmpty(list)) {
            return null;
        }
        Map tmp = new HashMap();
        tmp.put(Dict.DOC, list.get(0).getNewDoc());
        return tmp;
    }

    @Override
    public List<Map> getProcessList(Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        List<Map> result = new ArrayList<>();
        List<Task> processList = taskDao.getProcessList(user.getId(),request);
        List<String> taskIds = processList.stream().map(Task::getId).collect(Collectors.toList());
        if(CommonUtils.isNullOrEmpty(taskIds)){
            return result;
        }
        List<Map> instances = CommonUtils.castList(processApi.queryInstanceByTaskIds(taskIds), Map.class);
        for (Task task:processList) {
            Map processInstance = instances.stream().filter(instance -> instance.get(Dict.TASKID).equals(task.getId())).findFirst().orElse(null);
            List<Map> processStatusList = CommonUtils.castList(processInstance.get(Dict.PROCESS_STATUS_LIST),Map.class);
            HashMap<String, String> map;
            for (Map processStatus:processStatusList) {
                Map tempMap = result.stream().filter(temp -> processStatus.get(Dict.ID).equals(temp.get(Dict.ID))).findFirst().orElse(null);
                if (!CommonUtils.isNullOrEmpty(tempMap)){
                    break;
                }
                map = new HashMap<>();
                map.put(Dict.ID,(String)processStatus.get(Dict.ID));
                map.put(Dict.NAME,(String)processStatus.get(Dict.NAME));
                result.add(map);
            }
        }
        return result;
    }

    @Override
    public void updateEndTime(Map request) throws Exception {
        String endDate =  CommonUtils.dateFormat(new Date(), CommonUtils.DATE_PATTERN_F1);
        String taskId = (String) request.get(Dict.TASKID);
        Task oldTask = getTask(taskId);
        taskDao.updateEndTime(taskId,endDate);
        Task newTask = getTask(taskId);
        operateChangeLogService.asyncUpdateLog(newTask,oldTask,taskId);
        demandManageApi.queryDemandStatus(newTask.getRequirementId(),"4");
    }

    @Override
    public void changeTaskStatus(Map request) throws Exception {
        String taskId = (String) request.get(Dict.TASKID);
        Task oldTask = getTask(taskId);
        processApi.changeTaskStatus(request);
        Task task = new Task();
        task.setStatus((String)request.get(Dict.DEST_STATUS_ID));
        task.setId(taskId);
        taskDao.updateTask(task);
        Task newTask = getTask(taskId);
        operateChangeLogService.asyncUpdateLog(newTask,oldTask,taskId);
    }

    @Override
    public List<String> getImplUnitNoByIds(Map request) throws Exception {
        List<String> ids = CommonUtils.castList(request.get(Dict.TASK_ID_LIST), String.class);
        List<Task> tasks = taskDao.queryIds(ids);
        ArrayList<String> fdevImplUnitNoList = new ArrayList<>();
        tasks.forEach(task -> fdevImplUnitNoList.add(task.getImplUnitId()));
        return fdevImplUnitNoList;
    }

    @Override
    public Map queryTaskByApplicationId(Map request) throws Exception {
        ArrayList<Map<String, Object>> returnTasks = new ArrayList<>();
        String applicationId = (String) request.get(Dict.APPLICATIONID);
        Integer type = (Integer) request.get(Dict.TYPE);
        Integer pageNum =  Integer.parseInt((String)request.get(Dict.PAGE_NUM));
        Integer page = Integer.parseInt((String) request.get(Dict.Page));
        String name = (String) request.get(Dict.NAME);
        Map map = taskDao.queryTaskByApplicationId(applicationId, type, pageNum, page,name);
        if((long)map.get(Dict.COUNT) == 0){
            return map;
        }
        List<Task> tasks = (List<Task>) map.get(Dict.TASKLIST);
        ArrayList<String> taskIds = new ArrayList<>();
        ArrayList<String> groupIds = new ArrayList<>();
        ArrayList<String> createUserIds = new ArrayList<>();
        ArrayList<String> assigneeIds = new ArrayList<>();
        ArrayList<String> implUnitIds = new ArrayList<>();
        ArrayList<String> requirementIds = new ArrayList<>();
        tasks.forEach(task -> {
            taskIds.add(task.getId());
            groupIds.add(task.getAssigneeGroupId());
            createUserIds.add(task.getCreateUserId());
            assigneeIds.add(task.getAssigneeList()[0]);
            implUnitIds.add(task.getImplUnitId());
            requirementIds.add(task.getRequirementId());
        });
        for (Task task:tasks){
            returnTasks.add(CommonUtils.beanToMap(task));
        }
        //组名称，组id赋值
        if (!CommonUtils.isNullOrEmpty(groupIds)){
            List<Map> groups = userApi.queryGroupByIds(groupIds);
            if (!CommonUtils.isNullOrEmpty(groups)){
                for(Map task:returnTasks){
                    for (Map group:groups) {
                        if((!CommonUtils.isNullOrEmpty(task.get(Dict.ASSIGNEE_GROUP_ID))) && task.get(Dict.ASSIGNEE_GROUP_ID).equals(group.get(Dict.ID))){
                            task.put(Dict.TEAM_ID,group.get(Dict.ID));
                            task.put(Dict.TEAM_NAME,group.get(Dict.NAME));
                            break;
                        }
                    }
                }
            }
        }
        //创建人姓名赋值
        if (!CommonUtils.isNullOrEmpty(createUserIds)){
            HashMap<String, Object> createUserIdList = new HashMap<>();
            createUserIdList.put(Dict.IDS,createUserIds);
            List<Map> userList = (List<Map>) userApi.queryUserList(createUserIdList);
            if(!CommonUtils.isNullOrEmpty(userList)){
                for(Map task:returnTasks){
                    for (Map user:userList) {
                        if(task.get(Dict.CREATEUSERID).equals(user.get(Dict.ID))){
                            task.put(Dict.CREATE_USER_NAME,user.get(Dict.USER_NAME_CN));
                            break;
                        }
                    }
                }
            }
        }
        //执行人姓名赋值
        if (!CommonUtils.isNullOrEmpty(assigneeIds)){
            HashMap<String, Object> assigneeIdList = new HashMap<>();
            assigneeIdList.put(Dict.IDS,assigneeIds);
            List<Map> assigneeList = (List<Map>) userApi.queryUserList(assigneeIdList);
            if(!CommonUtils.isNullOrEmpty(assigneeList)){
                for(Map taskTemp:returnTasks){
                    for (Map user:assigneeList) {
                        List<String> strings = (List<String>) taskTemp.get(Dict.ASSIGNEE_LIST);
                        if(strings.get(0).equals(user.get(Dict.ID))){
                            taskTemp.put(Dict.ASSIGNEE,user.get(Dict.USER_NAME_CN));
                            break;
                        }
                    }
                }
            }
        }
        //任务状态赋值
        if(!CommonUtils.isNullOrEmpty(taskIds)){
            List<Map> instances = processApi.queryInstanceByTaskIds(taskIds);
            for (Map taskTemp : returnTasks) {
                for (Map instance:instances){
                    if(taskTemp.get(Dict.ID).equals(instance.get(Dict.TASKID))){
                        List<Map> processStatusList = (List<Map>)instance.get(Dict.PROCESS_STATUS_LIST);
                        for (Map process: processStatusList) {
                            if (process.get(Dict.ID).equals(instance.get(Dict.TASK_STATUS_ID))){
                                taskTemp.put(Dict.STATUS,process.get(Dict.NAME));
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
        //研发单元编号赋值
        if(!CommonUtils.isNullOrEmpty(implUnitIds)){
            List<Map> implUnits = demandManageApi.queryIpmpListByid(implUnitIds);
            if (!CommonUtils.isNullOrEmpty(implUnits)){
                for (Map taskTemp : returnTasks) {
                    Map implUnit = implUnits.stream().filter(implUnitIs ->
                            implUnitIs.get(Dict.ID).equals(taskTemp.get(Dict.IMPL_UNIT_ID))).findFirst().orElse(null);
                    if(!CommonUtils.isNullOrEmpty(implUnit)){
                        taskTemp.put(Dict.IMPL_UNIT_ID,implUnit.get(Dict.ID));
                        taskTemp.put(Dict.IMPL_UNIT_NO,implUnit.get(Dict.IMPL_UNIT_NUM));
                    }
                }
            }
        }
        //需求名称赋值
        if(!CommonUtils.isNullOrEmpty(requirementIds)){
            List<Map> requirements = demandManageApi.queryDemandInfoByIds(requirementIds);
            for (Map taskTemp : returnTasks) {
                if(!CommonUtils.isNullOrEmpty(requirements)){
                    for (Map requirement:requirements){
                        if(taskTemp.get(Dict.REQUIREMENT_ID).equals(requirement.get(Dict.DEMAND_ID))){
                            taskTemp.put(Dict.REQUIRMENT_NO,requirement.get(Dict.OA_REAL_NO));
                            taskTemp.put(Dict.REQUIREMENT_NAME,requirement.get(Dict.OA_CONTACT_NAME));
                        }
                    }
                }
            }
        }
        map.put(Dict.TASKLIST,returnTasks);
        return map;
    }

    @Override
    public Map<String,Object> queryTaskByFdevUnitId(Map request) throws Exception {
        List<Map<String, Object>> tasks = new ArrayList<>();
        String fdevUnitId = (String) request.get(Dict.FDEV_UNIT_ID);
        Integer pageNum =  Integer.parseInt(CommonUtils.isNullOrEmpty(request.get(Dict.PAGE_NUM))?"0":(String)request.get(Dict.PAGE_NUM));
        Integer page = Integer.parseInt(CommonUtils.isNullOrEmpty(request.get(Dict.Page))?"1":(String)request.get(Dict.Page));
        Integer delete = CommonUtils.isNullOrEmpty(request.get(Dict.DELETE))? null : (Integer)request.get(Dict.DELETE);
        Map map = taskDao.queryTaskByFdevUnitId(fdevUnitId, pageNum, page,delete);
        if(CommonUtils.isNullOrEmpty(map.get(Dict.TASKLIST))){
            return map;
        }
        List<Task> tasksTemp = CommonUtils.castList(map.get(Dict.TASKLIST),Task.class);
        for (Task task:tasksTemp){
            tasks.add(CommonUtils.beanToMap(task));
        }
        ArrayList<String> taskIds = new ArrayList<>();
        ArrayList<String> assigneeIds = new ArrayList<>();
        tasks.forEach(taskTemp -> {
                    taskIds.add((String)taskTemp.get(Dict.ID));
                    assigneeIds.add(((ArrayList<String>) taskTemp.get(Dict.ASSIGNEE_LIST)).get(0));
                }
        );
        List<Map> instances = processApi.queryInstanceByTaskIds(taskIds);
        HashMap<String, Object> assigneeIdList = new HashMap<>();
        assigneeIdList.put(Dict.IDS,assigneeIds);
        List<Map> assigneeList = CommonUtils.castList(userApi.queryUserList(assigneeIdList),Map.class);
        for (Map taskTemp:tasks){
            Map instance = instances.stream().filter(instanceTemp -> taskTemp.get(Dict.ID).equals(instanceTemp.get(Dict.TASKID))).findFirst().orElse(null);
            List<Map> processStatusList = CommonUtils.castList(instance.get(Dict.PROCESS_STATUS_LIST),Map.class);
            for (Map process: processStatusList) {
                if (process.get(Dict.ID).equals(instance.get(Dict.TASK_STATUS_ID))){
                    taskTemp.put(Dict.STATUS,process.get(Dict.NAME));
                    break;
                }
            }
            if(!CommonUtils.isNullOrEmpty(assigneeList)){
                for (Map user:assigneeList) {
                    List strings = (ArrayList<String>) taskTemp.get(Dict.ASSIGNEE_LIST);
                    if(strings.get(0).equals(user.get(Dict.ID))){
                        taskTemp.put(Dict.ASSIGNEE,user.get(Dict.USER_NAME_CN));
                        break;
                    }
                }
            }
            if (CommonUtils.isNullOrEmpty(taskTemp.get(Dict.ENDDATE))){
                taskTemp.put(Dict.TASK_TYPE,"0");
            }else {
                taskTemp.put(Dict.TASK_TYPE,"1");
            }
        }
        map.put(Dict.TASKLIST,tasks);
        return map;
    }

    public boolean compareDate(String start, String end) throws Exception{
        int i = CommonUtils.dateParse(start, CommonUtils.DATE_PATTERN_F1).compareTo(CommonUtils.dateParse(end, CommonUtils.DATE_PATTERN_F1));
        if(i == 1){
            return true;
        }
        return false;
    }

    @Override
    public void deleteTask(Map request) throws Exception {
        String taskId = (String)request.get(Dict.TASKID);
        Task task = taskDao.deleteTask(taskId);
        demandManageApi.queryDemandStatus(task.getRequirementId(),"4");
        interactTestApi.deleteTask(taskId);
        operateChangeLogService.asyncDeleteLog(task);
    }

    @Override
    public List<Task> queryTaskByRequirementId(Map request) throws Exception {
        String requirementId = (String) request.get(Dict.REQUIRMENT_ID);
        Object complete = request.get(Dict.COMPLETE);
        Task task = new Task();
        task.setRequirementId(requirementId);
        List<Task> query = taskDao.query(task);
        query.stream().forEach(taskTemp ->
        {
            if(CommonUtils.isNullOrEmpty(taskTemp.getEndDate())){
                taskTemp.setStatus("0");
            }else {
                taskTemp.setStatus("1");
            }
        });
        if (!CommonUtils.isNullOrEmpty(complete)){
            return query.stream().filter(taskTemp -> complete.equals(taskTemp.getStatus())).collect(Collectors.toList());
        }
        return query;
    }

    @Override
    public List<Task> queryTaskByUserId(Map request) throws Exception {
        Task task = new Task();
        task.setAssigneeList(new String[]{(String)request.get(Dict.USERID)});
        if (!CommonUtils.isNullOrEmpty(request.get(Dict.REQUIREMENT_ID))){
            task.setRequirementId((String)request.get(Dict.REQUIREMENT_ID));
        }
        task.setDelete((Integer) request.get(Dict.DELETE));
        return taskDao.query(task);
    }

    @Override
    public Task updateTaskInner(Map request) throws Exception {
        Task task = CommonUtils.mapToBean(request, Task.class);
        String taskId = task.getId();
        Task oldTask = getTask(taskId);
        Task newTask = taskDao.updateTask(task);
        operateChangeLogService.asyncUpdateLog(newTask,oldTask,taskId);
        return newTask;
    }

    @Override
    public List<Task> querTaskListByImplUnitIds(Map request) throws Exception {
        List<String> implUnitIds = CommonUtils.castList(request.get(Dict.IMPL_UNIT_IDS), String.class);
        Integer delete = null;
        if(!CommonUtils.isNullOrEmpty(request.get(Dict.DELETE))){
            delete = (Integer) request.get(Dict.DELETE);
        }
        return taskDao.querTaskListByImplUnitIds(implUnitIds,delete);
    }

    @Override
    public List<Map> queryTaskByVersionIds(Map request) throws Exception {
        ArrayList<Map> returnMessage = new ArrayList<>();
        List<String> versionIds = CommonUtils.castList(request.get(Dict.VERSION_IDS), String.class);
        Integer mountStatus = (Integer) request.get(Dict.MOUNT_STATUS);
        Integer delete = (Integer) request.get(Dict.DELETE);
        List<Task> taskList = taskDao.queryTaskByVersionIds(versionIds,mountStatus,delete);
        if (CommonUtils.isNullOrEmpty(taskList)){
            return new ArrayList<>();
        }
        List<Task> mountTaskList = taskList.stream().filter(task -> task.getMountStatus() == 1).collect(Collectors.toList());
        List<Task> intentionMountTaskList = taskList.stream().filter(task -> task.getMountStatus() == 0).collect(Collectors.toList());
        List<String> taskIds = taskList.stream().map(Task::getId).collect(Collectors.toList());
        List<Map> instances = processApi.queryInstanceByTaskIds(taskIds);
        List<Map> components = processApi.queryComponentList();
        Map branchComponent = components.stream().filter(component -> ((String) component.get(Dict.CLASS_NAME)).endsWith("MountMBranchComponent")).findFirst().orElse(null);
        String componentId = (String) branchComponent.get(Dict.ID);
        for (String versionId:versionIds) {
            HashMap<String, Object> task = new HashMap<>();
            ArrayList<Map> intentionMountTaskMaps = new ArrayList<>();
            ArrayList<Map> mountTaskMaps = new ArrayList<>();
            HashMap<String, Object> taskListMap = new HashMap<>();
            task.put(Dict.VERSION_ID,versionId);
            List<Task> intentionMountTasks = intentionMountTaskList.stream().filter(taskTemp -> versionId.equals(taskTemp.getVersionId())).collect(Collectors.toList());
            List<Task> mountTasks = mountTaskList.stream().filter(taskTemp -> versionId.equals(taskTemp.getVersionId())).collect(Collectors.toList());
            for (Task taskTemp:intentionMountTasks) {
                Map taskMap = CommonUtils.beanToMap(taskTemp);
                getStatusName(taskMap,instances);
                getMergeId(taskMap,instances,componentId);
                intentionMountTaskMaps.add(taskMap);
            }
            for (Task taskTemp:mountTasks) {
                Map taskMap = CommonUtils.beanToMap(taskTemp);
                getStatusName(taskMap,instances);
                getMergeId(taskMap,instances,componentId);
                mountTaskMaps.add(taskMap);
            }
            getAssigneeName(intentionMountTaskMaps);
            getAssigneeName(mountTaskMaps);
            taskListMap.put(Dict.INTENTION_MOUNT_TASK_LIST,intentionMountTaskMaps);
            taskListMap.put(Dict.MOUNT_TASK_LIST,mountTaskMaps);
            task.put(Dict.TASKLIST,taskListMap);
            returnMessage.add(task);
        }
        return returnMessage;
    }

    @Override
    public List<String> updateMountStatus(Map request) throws Exception {
        Integer mountStatus = (Integer) request.get(Dict.MOUNT_STATUS);
        List<String> taskIds =  CommonUtils.castList(request.get(Dict.IDS),String.class);
        if(CommonUtils.isNullOrEmpty(taskIds)){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"任务数量不能为空"});
        }
        List<Task> taskList = taskDao.queryIds(taskIds);
        if(taskIds.size() != taskList.size()){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"部分任务不存在"});
        }
        String versionId = (String) request.get(Dict.VERSION_ID);
        List<Map> results = processApi.mountVersionCallBack(mountStatus, taskIds, versionId);
        List<String> strings = taskDao.updateMountStatus(mountStatus, versionId, results);
        List<Task> newTaskList = taskDao.queryIds(taskIds);
        ArrayList<String> taskNames = new ArrayList<>();
        operateChangeLogService.asyncUpdateTaskListLog(newTaskList,taskList);
        for (String taskId:strings) {
            for (Task task:taskList) {
                if (taskId.equals(task.getId())){
                    taskNames.add(task.getName());
                }
            }
        }
        return taskNames;
    }

    @Override
    public List<Task> getTaskByTask(Map request) throws Exception {
        Task task = CommonUtils.mapToBean(request, Task.class);
        return taskDao.query(task);
    }

    @Override
    public Map getTaskByRequirementId(Map params) throws Exception {
        Map result = taskDao.getTaskList("", params);
        List<Task> taskList = CommonUtils.castList(result.get(Dict.TASK), Task.class);
        List<Map> tasks = updateTaskValue(taskList);
        result.put(Dict.TASK,tasks);
        result.put(Dict.STATUS_LIST,getProcessListByRequirementId(taskList));
        return result;
    }

    @Override
    public List<Map> getProcessListByRequirementId(List<Task> taskList) throws Exception {
        List<Map> result = new ArrayList<>();
        List<String> taskIds = taskList.stream().map(Task::getId).collect(Collectors.toList());
        if(CommonUtils.isNullOrEmpty(taskIds)){
            return result;
        }
        List<Map> instances = CommonUtils.castList(processApi.queryInstanceByTaskIds(taskIds), Map.class);
        HashMap<String, Object> map;
        for (Map instance:instances){
            List<Map> processStatusList = CommonUtils.castList(instance.get(Dict.PROCESS_STATUS_LIST), Map.class);
            ArrayList<Map> beginProcess = new ArrayList<>();
            ArrayList<Map> process = new ArrayList<>();
            ArrayList<Map> endProcess = new ArrayList<>();
            for (Map processStatus:processStatusList) {
                if((int)processStatus.get("statusType") == 0){
                    beginProcess.add(processStatus);
                }
                if((int)processStatus.get("statusType") == 1){
                    process.add(processStatus);
                }
                if((int)processStatus.get("statusType") == 2){
                    endProcess.add(processStatus);
                }
            }
            beginProcess.addAll(process);
            beginProcess.addAll(endProcess);
            for (Map processStatus:beginProcess) {
                Map temp = result.stream().filter(resultTemp -> processStatus.get(Dict.ID).equals(resultTemp.get(Dict.ID))).findFirst().orElse(null);
                if (!CommonUtils.isNullOrEmpty(temp)){
                    break;
                }
                map = new HashMap<>();
                map.put(Dict.ID,processStatus.get(Dict.ID));
                map.put(Dict.NAME,processStatus.get(Dict.NAME));
                map.put(Dict.COUNT,taskList.stream().filter(taskTemp -> taskTemp.getStatus().equals(processStatus.get(Dict.ID))).count());
                result.add(map);
            }
        }
        return result;
    }

    @Override
    public List<Map> getTaskByIds(Map request) throws Exception {
        List<String> ids = CommonUtils.castList(request.get(Dict.IDS), String.class);
        List<Task> tasks = taskDao.queryIds(ids);
        ArrayList<String> taskIds = new ArrayList<>();
        for(Task taskTemp:tasks) {
            taskIds.add(taskTemp.getId());
        }
        if (CommonUtils.isNullOrEmpty(taskIds)){
            return new ArrayList<>();
        }
        List<Map> instance = processApi.queryInstanceByTaskIds(taskIds);
        ArrayList<Map> taskMaps = new ArrayList<>();
        for (Task taskTemp:tasks) {
            Map taskMap = CommonUtils.beanToMap(taskTemp);
            taskMaps.add(taskMap);
        }
        for (Map taskTemp:taskMaps) {
            getStatusName(taskTemp,instance);
        }
        return taskMaps;
    }

    private void relatedBranch(Task task, Map request) throws Exception{
        List<Map> services = serviceApi.queryProjectMember(task.getRelatedApplication());
        Map createUser = services.stream().filter(service -> service.get(Dict.USERID).equals(task.getCreateUserId())).findFirst().orElse(null);
        Map assignee = services.stream().filter(service -> service.get(Dict.USERID).equals(task.getAssigneeList()[0])).findFirst().orElse(null);
        if (CommonUtils.isNullOrEmpty(createUser)){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"当前用户不在所选应用中,请联系应用负责人处理"});
        }
        if (CommonUtils.isNullOrEmpty(assignee)){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"执行人员不在所选应用中,请联系应用负责人处理"});
        }else {
            String role = (String)assignee.get(Dict.ROLE);
            if("20".equals(role) || "10".equals(role)){
                throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"执行人员权限不足,请联系应用负责人处理"});
            }
        }
        Map service = serviceApi.queryApp(task.getRelatedApplication());
        Map requirement = demandManageApi.queryDemandManageInfoDetail(task.getRequirementId());
        try{
            //新建获取master的commitSha,关联获取所选分支的commitSha
            if(task.getBranchType()==0){
                processApi.createFeature(task.getId(),task.getRelatedApplication(),task.getBranchName()
                        ,(String)requirement.get(Dict.OA_REAL_NO),(String)request.get(Dict.SOURCEBRANCH),Arrays.asList(task.getAssigneeList()));
                task.setCommitSha(gitApiService.getLatestCommitSha((Integer) service.get(Dict.GITLAB_ID), Dict.MASTER));
            }else {
                task.setCommitSha(gitApiService.getLatestCommitSha((Integer) service.get(Dict.GITLAB_ID),task.getBranchName()));
            }
        }catch (Exception e){
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{e.getMessage()});
        }
    }

    private void getStatusName(Map taskTemp,List<Map> instances) throws Exception{
        Map instance = instances.stream().filter(instanceTemp -> taskTemp.get(Dict.ID).equals(instanceTemp.get(Dict.TASKID))).findFirst().orElse(null);
        List<Map> processStatusList = CommonUtils.castList(instance.get(Dict.PROCESS_STATUS_LIST),Map.class);
        for (Map process: processStatusList) {
            if (process.get(Dict.ID).equals(instance.get(Dict.TASK_STATUS_ID))){
                taskTemp.put(Dict.STATUS,process.get(Dict.NAME));
                taskTemp.put(Dict.TYPE,process.get(Dict.STATUS_TYPE));
                break;
            }
        }
    }

    private void getMergeId(Map taskTemp,List<Map> instances,String componentId) throws Exception{
        Map instance = instances.stream().filter(instanceTemp -> taskTemp.get(Dict.ID).equals(instanceTemp.get(Dict.TASKID))).findFirst().orElse(null);
        List<Map> componentStatusList = CommonUtils.castList(instance.get(Dict.COMPONENT_STATUS_LIST), Map.class);
        for (Map componentStatus: componentStatusList) {
            if (componentStatus.get(Dict.SOURCE_ID).equals(instance.get(Dict.TASK_STATUS_ID))){
                List<Map> componentResultAndStatus = CommonUtils.castList(componentStatus.get(Dict.COMPONENT_RESULT_STATUS), Map.class);
                for (Map componentResult:componentResultAndStatus){
                    if(componentId.equals(componentResult.get(Dict.COMPONENT_ID))){
                        HashMap<String, Object> mountVersion = new HashMap<>();
                        if (Dict.OPEND.equals(componentResult.get(Dict.MERGE_STATUS))){
                            mountVersion.put(Dict.MERGEID,componentResult.get(Dict.MERGE_ID));
                        }
                        mountVersion.put(Dict.SOURCE_BRANCH,componentResult.get(Dict.SOURCE_BRANCH));
                        mountVersion.put(Dict.TARGET_BRANCH,componentResult.get(Dict.TARGET_BRANCH));
                        taskTemp.put(Dict.MOUNT_VERSION,mountVersion);
                    }
                }
            }
        }
    }

    private void getAssigneeName(List<Map> taskTemp) throws Exception{
        ArrayList<String> assigneeIds = new ArrayList<>();
        for (Map task:taskTemp) {
            List assigneeList = (List) task.get(Dict.ASSIGNEE_LIST);
            assigneeIds.add((String)assigneeList.get(0));
        }
        HashMap<String, Object> assigneeIdList = new HashMap<>();
        assigneeIdList.put(Dict.IDS,assigneeIds);
        List<Map> assigneeList = CommonUtils.castList(userApi.queryUserList(assigneeIdList),Map.class);
        for (Map task:taskTemp){
            if(!CommonUtils.isNullOrEmpty(assigneeList)){
                for (Map user:assigneeList) {
                    List strings = (ArrayList<String>) task.get(Dict.ASSIGNEE_LIST);
                    if(strings.get(0).equals(user.get(Dict.ID))){
                        task.put(Dict.ASSIGNEE,user.get(Dict.USER_NAME_CN));
                        break;
                    }
                }
            }
        }
    }

    private List<Map> updateTaskValue(List<Task> tasks)throws Exception{
        ArrayList<Map> taskList = new ArrayList<>();
        ArrayList<String> taskIds = new ArrayList<>();
        ArrayList<String> implUnitIds = new ArrayList<>();
        ArrayList<String> requirementIds = new ArrayList<>();
        ArrayList<String> createUserIds = new ArrayList<>();
        ArrayList<String> assigneeIds = new ArrayList<>();
        tasks.stream().forEach(taskTemp -> {
            taskIds.add(taskTemp.getId());
            implUnitIds.add(taskTemp.getImplUnitId());
            requirementIds.add(taskTemp.getRequirementId());
            createUserIds.add(taskTemp.getCreateUserId());
            implUnitIds.add(taskTemp.getImplUnitId());
            assigneeIds.add(taskTemp.getAssigneeList()[0]);
        });
        if(CommonUtils.isNullOrEmpty(taskIds)){
            return new ArrayList();
        }
        List<Map> instances = CommonUtils.castList(processApi.queryInstanceByTaskIds(taskIds), Map.class);
        List<Map> implUnits = demandManageApi.queryIpmpListByid(implUnitIds);
        List<Map> requirements = demandManageApi.queryDemandInfoByIds(requirementIds);
        List<Map> userList = new ArrayList<>();
        List<Map> assigneeList = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(createUserIds)) {
            HashMap<String, Object> createUserIdList = new HashMap<>();
            createUserIdList.put(Dict.IDS,createUserIds);
            userList = CommonUtils.castList(userApi.queryUserList(createUserIdList),Map.class);
        }
        if (!CommonUtils.isNullOrEmpty(assigneeIds)) {
            HashMap<String, Object> assigneeIdList = new HashMap<>();
            assigneeIdList.put(Dict.IDS,assigneeIds);
            assigneeList = CommonUtils.castList(userApi.queryUserList(assigneeIdList),Map.class);
        }
        for (Task taskTemp:tasks) {
            Map taskMap = CommonUtils.beanToMap(taskTemp);
            taskMap.put(Dict.ID,taskTemp.getId());
            taskMap.put(Dict.NAME,taskTemp.getName());
            taskMap.put(Dict.PRIORITY,taskTemp.getPriority());
            taskMap.put(Dict.PLAN_START_DATE,taskTemp.getPlanStartDate());
            taskMap.put(Dict.PLAN_END_DATE,taskTemp.getPlanEndDate());
            taskMap.put(Dict.CREATE_TIME,taskTemp.getCreateTime());
            //状态和下一状态赋值
            Map processInstance = instances.stream().filter(instance -> instance.get(Dict.TASKID).equals(taskTemp.getId())).findFirst().orElse(null);
            if(processInstance != null){
                ArrayList<String> nextStatusList = new ArrayList<>();
                List<Map> statusList = CommonUtils.castList(processInstance.get(Dict.PROCESS_STATUS_LIST),Map.class);
                Map currentStatus = statusList.stream().filter(statusL -> statusL.get(Dict.ID).equals(processInstance.get(Dict.TASK_STATUS_ID))).findFirst().orElse(null);
                nextStatusList.add((String)currentStatus.get(Dict.ID));
                List<Map> outList = CommonUtils.castList(currentStatus.get(Dict.OUT_LIST), Map.class);
                List<Map> inList = CommonUtils.castList(currentStatus.get(Dict.IN_LIST), Map.class);
                outList.stream().forEach(outL -> nextStatusList.add((String)outL.get(Dict.FDEV_PROCESS_STATUS_ID)));
                Map status = statusList.stream().filter(statusTemp -> statusTemp.get(Dict.ID).equals(processInstance.get(Dict.TASK_STATUS_ID))).findFirst().orElse(null);
                taskMap.put(Dict.NEXT_STATUS_LIST, nextStatusList);
                taskMap.put(Dict.STATUS_NAME, status.get(Dict.NAME));
                Integer statusType = 1;
                if(inList.size() == 0){
                    statusType = 0;
                }
                if (outList.size() == 0){
                    statusType = 2;
                }
                taskMap.put(Dict.STATUS_TYPE, statusType);
            }
            //研发单元编号赋值
            if(!CommonUtils.isNullOrEmpty(implUnits)){
                Map implUnit = implUnits.stream().filter(implUnitIs ->
                        implUnitIs.get(Dict.ID).equals(taskTemp.getImplUnitId())).findFirst().orElse(null);
                if (!CommonUtils.isNullOrEmpty(implUnit)){
                    taskMap.put(Dict.IMPL_UNIT_NO,implUnit.get(Dict.IMPL_UNIT_NUM));
                    taskMap.put(Dict.IMPL_CONTENT,implUnit.get(Dict.IMPL_CONTENT));
                }
            }
            //创建人姓名赋值
            if(!CommonUtils.isNullOrEmpty(userList)){
                for (Map user:userList) {
                    if(taskTemp.getCreateUserId().equals(user.get(Dict.ID))){
                        taskMap.put(Dict.CREATE_USER_NAME,user.get(Dict.USER_NAME_CN));
                        break;
                    }
                }
            }
            //执行人员姓名赋值
            if(!CommonUtils.isNullOrEmpty(assigneeList)){
                for (Map assignee:assigneeList) {
                    if(taskTemp.getAssigneeList()[0].equals(assignee.get(Dict.ID))){
                        taskMap.put(Dict.ASSIGNEE_NAME,assignee.get(Dict.USER_NAME_CN));
                        break;
                    }
                }
            }
            //需求名称赋值
            if (CommonUtils.isNotNullOrEmpty(taskTemp.getRequirementId())){
                Map requirement = requirements.stream().filter(requirementTemp -> taskTemp.getRequirementId().equals(requirementTemp.get(Dict.DEMAND_ID))).findFirst().orElse(null);
                if (!CommonUtils.isNullOrEmpty(requirement)){
                    taskMap.put(Dict.REQUIRMENT_NO,requirement.get(Dict.OA_REAL_NO));
                }else {
                    taskMap.put(Dict.REQUIRMENT_NO,"");
                }
            }
            taskList.add(taskMap);
        }
        return taskList;
    }

    Task getTask(String taskId) throws Exception{
        Task task = new Task();
        task.setId(taskId);
        List<Task> oldTaskList = taskDao.query(task);
        if (CommonUtils.isNullOrEmpty(oldTaskList)){
            throw new FdevException(ErrorConstants.TASK_NOT_EXIST);
        }
        return oldTaskList.get(0);
    }

    @Override
    public Integer queryNotEndTask(String processId, String groupId) {
        return taskDao.queryNotEndTask(processId,groupId);
    }

    @Override
    public List<Map> getCompleteTaskByImplIds(Map request) throws Exception {
        List<String> implUnitIds = CommonUtils.castList(request.get(Dict.IMPL_UNIT_IDS), String.class);
        Integer delete = 0;
        HashMap<String, Object> implMap;
        ArrayList<Map> result = new ArrayList<>();
        List<Task> taskList = taskDao.querTaskListByImplUnitIds(implUnitIds, delete);
        for (String implId:implUnitIds){
            List<Task> collect = taskList.stream().filter(task -> task.getImplUnitId().equals(implId)).collect(Collectors.toList());
            long count = collect.stream().filter(task -> CommonUtils.isNotNullOrEmpty(task.getEndDate())).count();
            implMap = new HashMap<>();
            implMap.put(Dict.IMPL_UNIT_ID,implId);
            implMap.put(Dict.IMPL_UNIT_TASKS,collect);
            implMap.put(Dict.COUNT,collect.size());
            implMap.put(Dict.COMPLETE_TASK,count);
            result.add(implMap);
        }
        return result;
    }

    @Override
    public BulkWriteResult updateMountSameFBConfirms(Map request){
        List<Map> maps = CommonUtils.castList(request.get(Dict.VALUE), Map.class);
        return taskDao.updateMountSameFBConfirms(maps);
    }

    private boolean testInform(Map processInstance, Task task){
        List<Map> processStatusList = CommonUtils.castList(processInstance.get(Dict.PROCESS_STATUS_LIST), Map.class);
        for (Map processStatus : processStatusList) {
            List<Map> outList = CommonUtils.castList(processStatus.get(Dict.OUT_LIST), Map.class);
            for (Map out : outList) {
                List<Map> activityList = CommonUtils.castList(out.get(Dict.ACTIVITY_LIST), Map.class);
                for (Map activity : activityList) {
                    if ("玉衡测试".equals(activity.get(Dict.ACTIVITY_NAME))){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
