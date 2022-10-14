package com.spdb.fdev.fdevtask.base.utils;


import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.entity.TaskDiffModifyRecord;
import com.spdb.fdev.fdevtask.spdb.entity.TaskReview;
import com.spdb.fdev.fdevtask.spdb.entity.TaskReviewChild;
import com.spdb.fdev.fdevtask.spdb.service.DemandService;
import com.spdb.fdev.fdevtask.spdb.service.IReleaseTaskApi;
import com.spdb.fdev.fdevtask.spdb.service.ITestApi;
import com.spdb.fdev.fdevtask.spdb.service.TaskDiffModifyRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


@Component
public class FdevTaskUtils {

    private static Logger logger = LoggerFactory.getLogger(ApplicationContextAware.class);

    @Autowired
    private DemandService demandService;
    @Autowired
    private ITestApi iTestApi;
    @Autowired
    private AsyncService asyncService;
    @Value("${update-order-topic}")
    private String updateOrderTopic;
    @Autowired
    private IReleaseTaskApi releaseTaskApi;
    @Autowired
    private TaskDiffModifyRecordService modifyRecordService;

    /**
     * 校验task是否处于暂缓状态
     *
     * @param spectialStatus
     * @throws Exception
     */
    public void checkIsWaitTask(Integer spectialStatus) throws Exception {

        //暂缓状态触发的更新，全部不能更新
        if (!CommonUtils.isNullOrEmpty(spectialStatus) && spectialStatus == 1) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"当前任务状态处于暂缓状态，无法进行更新"});
        }
    }

    /**
     *  更新研发单元的数据到任务
     * @param task
     * @param old
     * @return
     * @throws Exception
     */
    public FdevTask  updateTaskByImplUnit(FdevTask task,FdevTask old) {
        //修改研发单元 1.传参研发单元不为空并且和库里的研发单元不同   2.传参不为空并且库里的研发单元为null
        if ((task.getFdev_implement_unit_no() != null && !task.getFdev_implement_unit_no().equals(old.getFdev_implement_unit_no()))
                || (task.getFdev_implement_unit_no() != null && old.getFdev_implement_unit_no() == null)){
            String[] tag = old.getTag();
            ArrayList<String> tmp = new ArrayList<>();
            tmp.addAll(Arrays.asList(tag));
            if (old.getTag() != null && old.getTag().length >0){
                Iterator<String> iterator = tmp.iterator();
                while (iterator.hasNext()){
                    String next = iterator.next();
                    if (next.equals("确认书已到达")){
                        iterator.remove();
                        break;
                    }
                }
            }
            //查询研发单元信息
            Map implement_unit_info = (Map) demandService.queryByFdevNo("", task.getFdev_implement_unit_no()).get(Dict.IMPLEMENT_UNIT_INFO);
            //研发单元信息不为空更新研发单元信息
            if (implement_unit_info != null) {
                task.setRqrmnt_no((String) implement_unit_info.get("demand_id"));
                task.setPlan_start_time(CommonUtils.dateParse((String) implement_unit_info.get("plan_start_date")));
                task.setPlan_fire_time(CommonUtils.dateParse((String) implement_unit_info.get("plan_product_date")));
                task.setPlan_inner_test_time(CommonUtils.dateParse((String) implement_unit_info.get("plan_inner_test_date")));
                task.setPlan_rel_test_time(CommonUtils.dateParse((String) implement_unit_info.get("plan_test_finish_date")));
                task.setPlan_uat_test_start_time(CommonUtils.dateParse((String) implement_unit_info.get("plan_test_date")));
                task.setPlan_uat_test_stop_time(CommonUtils.dateParse((String) implement_unit_info.get("plan_test_finish_date")));
            }else {
                //研发单元信息异常
                throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"研发单元相关信息不存在！"});
            }
            task.setTag(CommonUtils.distinctArray(tmp.toArray(new String[tmp.size()])));
            //修改研发单元编号通知玉衡
            Map params = new HashMap();
            params.put("taskNo", old.getId());
            params.put("unitNo", task.getFdev_implement_unit_no());
            params.put("kafkaTopic",updateOrderTopic);
            params.put(Dict.REST_CODE,"updateUnitNo");
            asyncService.sendMessage(params);
        }
        task.setRedmine_id(task.getFdev_implement_unit_no());
        return task;
    }

    /**
     * 判断是否修改了用户测试时间
     * @param startUatTestTimeNew 修改后开始uat测试时间
     * @param startRelTestTimeNew 修改后开始rel测试时间
     * @param startUatTestTimeOld 修改前开始uat测试时间
     * @param startRelTestTimeOld 修改前开始rel测试时间
     * @return
     */
    public boolean updateTimeFlag(String startUatTestTimeNew, String startRelTestTimeNew,
                                  String startUatTestTimeOld, String startRelTestTimeOld) {
        //判断是否编辑了实际开始UAT日期和实际开始REL日期
        if(((null != startUatTestTimeNew && !startUatTestTimeNew.equals(startUatTestTimeOld))
                || (null != startRelTestTimeNew && !startRelTestTimeNew.equals(startRelTestTimeOld)))) {
            return true;
        }
        return false;
    }

    /**
     * 获取需要添加权限的用户集合
     */
    public HashSet getMembers(FdevTask oldTask, FdevTask newTask) {
        HashSet<String> members = new HashSet<>();
        String[] persons = CommonUtils.concatAll(newTask.getMaster(), newTask.getSpdb_master(), newTask.getDeveloper());
        if (CommonUtils.isNullOrEmpty(persons)) {
            return members;
        }
        members.addAll(Arrays.asList(persons));
        if (CommonUtils.isNullOrEmpty(oldTask)) {
            return members;
        } else {
            HashSet<String> oldmembers = new HashSet<>();
            String[] old_persons = CommonUtils.concatAll(oldTask.getMaster(), oldTask.getSpdb_master(), oldTask.getDeveloper());
            if (CommonUtils.isNullOrEmpty(persons)) {
                return members;
            }
            oldmembers.addAll(Arrays.asList(old_persons));
            oldmembers.retainAll(members);
            members.removeAll(oldmembers);
            return members;
        }
    }

    /**
     * 校验时间是否正常
     * @param taskId 任务id
     * @param startUatTestTimeNew 新的提交用户测试时间
     * @param startRelTestTimeNew 新的用户测试完成时间
     * @param proWantWindow 意向投产窗口时间
     * @param appType 任务类型
     * @param startUatTestTimeOld 旧的提交用户测试时间
     * @param startRelTestTimeOld 旧的用户测试完成时间
     * @throws Exception
     */
    public void checkTaskTime(String taskId, String startUatTestTimeNew, String startRelTestTimeNew,
                               String proWantWindow, String appType, String startUatTestTimeOld, String startRelTestTimeOld) throws Exception {
        //校验实际开始uat测试时间和完成uat测试时间格式是否正常
        String regex = "^[0-9]{4}[/]{1}[0-9]{2}[/]{1}[0-9]{2}$";
        if(CommonUtils.isNotNullOrEmpty(startUatTestTimeNew) && !Pattern.matches(regex,startUatTestTimeNew)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] {"实际提交用户测试日期","格式错误"});
        }
        if(CommonUtils.isNotNullOrEmpty(startRelTestTimeNew) && !Pattern.matches(regex,startRelTestTimeNew)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] {"实际用户测试完成日期","格式错误"});
        }
        SimpleDateFormat sdf = new SimpleDateFormat(CommonUtils.DATE_PATTERN_F1);
        if(CommonUtils.isNotNullOrEmpty(startUatTestTimeNew,startRelTestTimeNew)
                && sdf.parse(startUatTestTimeNew).after(sdf.parse(startRelTestTimeNew))) {
            throw new FdevException(ErrorConstants.DATE_AFTER_ERROR, new String[] {"实际提交用户测试日期","实际用户测试完成日期"+startRelTestTimeNew});
        }
        Map releaseWindowInfo = releaseTaskApi.queryDetailByTaskId(taskId, CommonUtils.getReleaseType(appType));
        if (!CommonUtils.isNullOrEmpty(releaseWindowInfo)) {
            String release_node_name = (String) releaseWindowInfo.get(Dict.RELEASE_NODE_NAME);
            if(CommonUtils.isNotNullOrEmpty(startRelTestTimeNew,proWantWindow,release_node_name)
                    && sdf.parse(startRelTestTimeNew).after(sdf.parse(proWantWindow))) {
                throw new FdevException(ErrorConstants.DATE_AFTER_ERROR, new String[] {"实际用户测试完成日期","此任务挂载的投产窗口日期"+proWantWindow});
            }
        }
        if("".equals(startUatTestTimeNew) && !CommonUtils.isNullOrEmpty(startUatTestTimeOld)) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"实际提交用户测试日期不能清除"});
        }
        if(CommonUtils.isNullOrEmpty(startUatTestTimeNew) && CommonUtils.isNullOrEmpty(startUatTestTimeOld)
                && !CommonUtils.isNullOrEmpty(startRelTestTimeNew)) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"先设置实际提交用户测试日期才能设置实际用户测试完成日期"});
        }
        if(startUatTestTimeNew == null && CommonUtils.isNullOrEmpty(startUatTestTimeOld)
                && !CommonUtils.isNullOrEmpty(startRelTestTimeOld)) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[]{"请补充实际提交用户测试日期"});
        }
    }

    /**
     * 推送阶段和时间
     * @param ftask
     * @param oldTask
     * @param updateTimeFlag
     * @param newStage
     */
    public void sendStageAndTime(FdevTask ftask, FdevTask oldTask, boolean updateTimeFlag, String newStage) {
        String rqrno = ftask.getRqrmnt_no();
        String oldRqrno = oldTask.getRqrmnt_no();
        //新的研发单元编号
        String newUnitNo = ftask.getFdev_implement_unit_no();
        //旧的研发单元编号
        String oldUnitNo = oldTask.getFdev_implement_unit_no();
        //向研发单元推阶段和时间
        if(!Dict.TASK_STAGE_FILE.equals(newStage)) {//任务归档不推送状态
            if (updateTimeFlag || CommonUtils.isNotNullOrEmpty(newStage, oldTask.getStage()) && !newStage.equals(oldTask.getStage())
                    || CommonUtils.isNullOrEmpty(oldRqrno) && !CommonUtils.isNullOrEmpty(rqrno)) {
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
    }

    /**
     * 判断是否修改了是否涉及数据库审核为否
     * @param taskReview
     * @return
     */
    public boolean changeDataBaseReviewFlag(TaskReview taskReview) {
        if (taskReview != null) {
            TaskReviewChild[] dataBaseReview = taskReview.getData_base_alter();
            if(!CommonUtils.isNullOrEmpty(dataBaseReview)) {
                for (int i = 0; i < dataBaseReview.length; i++) {
                    if("否".equals(dataBaseReview[i].getName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 设置数据库变更审核
     * @param task
     * @param oldTaskReview
     */
    public void setDataBaseReview(FdevTask task, TaskReview oldTaskReview) {
        //如果原来数据库变更就是才执行
        TaskReviewChild[] data_base_alter = oldTaskReview.getData_base_alter();
        for (TaskReviewChild taskReviewChild : data_base_alter) {
            if ("是".equals(taskReviewChild.getName())) {
                TaskReview review2 = task.getReview();
                if (review2 != null) {
                    review2.setData_base_alter(data_base_alter);
                }
                task.setReview(review2);
            }
        }
    }

    /**
     * 修改任务难度
     * @param task
     * @param oldTask
     */
    public void updateDifficultuDesc(FdevTask task, FdevTask oldTask, String userId) {
        if (!CommonUtils.isNullOrEmpty(task.getDifficulty_desc()) &&
                !task.getDifficulty_desc().equals(oldTask.getDifficulty_desc())) {
            if (Dict.TASK_STAGE_REL.equals(oldTask.getStage())) {
                throw new FdevException(ErrorConstants.UPDATE_FALSE, new String[]{":当前任务已进入rel环节，不支持更改任务难度评估"});
            }
            String[] spdb_master = task.getSpdb_master();
            if (!Arrays.asList(spdb_master).contains(userId)) {
                throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"只有“行内项目负责人”才可以修改任务难度评估"});
            }
            if (!CommonUtils.isNullOrEmpty(oldTask.getDifficulty_desc()) && CommonUtils.isNullOrEmpty(task.getModify_reason())) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"“任务难度”已改动，“修改原因”不可为空"});
            }
            //根据任务Id记录难度修改
            String difficulty_desc = task.getDifficulty_desc();//原难度修改
            String modify_reason = task.getModify_reason();//修改原因
            String updateTime = CommonUtils.dateFormat(new Date(), CommonUtils.DATE_TIME_PATTERN);
            TaskDiffModifyRecord taskDiffModifyRecords = new TaskDiffModifyRecord();
            taskDiffModifyRecords.setId(task.getId());//任务Id
            Map<String, Object> modifyRecordMap = new HashMap<>();
            modifyRecordMap.put(Dict.OPERATE_USER_ID, userId);
            modifyRecordMap.put(Dict.DIFFICULT_DESC, difficulty_desc);
            modifyRecordMap.put(Dict.MODIFY_REASON, modify_reason);
            modifyRecordMap.put(Dict.UPDATE_TIME, updateTime);
            TaskDiffModifyRecord taskDiffModifyRecord = modifyRecordService.queryById(task.getId());
            if (!CommonUtils.isNullOrEmpty(taskDiffModifyRecord)) {
                List<Map<String, Object>> modifyRecord = taskDiffModifyRecord.getModifyRecord();
                modifyRecord.add(modifyRecordMap);
                modifyRecordService.update(taskDiffModifyRecord);
            } else {
                ArrayList<Map<String, Object>> modifyRecordList = new ArrayList<>();
                modifyRecordList.add(modifyRecordMap);
                taskDiffModifyRecords.setModifyRecord(modifyRecordList);
                //第一次修改添加不记录
                if (!CommonUtils.isNullOrEmpty(modify_reason)) {
                    modifyRecordService.save(taskDiffModifyRecords);
                }
            }
        }
    }

    /**
     * 从用户信息map中取用户名称
     * @param userId
     * @param userMap
     * @return
     */
    public Object getUserName(Object userId, Map<String, Map> userMap) {
        if(userId instanceof String[]) {
            if(CommonUtils.isNullOrEmpty(userId)) {
                return new ArrayList<>();
            }
            List<String> ids = Arrays.asList((String[])userId);
            List<Map> result = new ArrayList<>();
            for (String id : ids) {
                if(!CommonUtils.isNullOrEmpty(userMap.get(id))) {
                    Map<String, Object> userInfo = userMap.get(id);
                    Map<String, String> user = new HashMap<String, String>(){{
                            put(Dict.ID, id);
                            put(Dict.USER_NAME_EN, userInfo.get(Dict.USER_NAME_EN) == null ? "" : (String)userInfo.get(Dict.USER_NAME_EN));
                            put(Dict.USER_NAME_CN, userInfo.get(Dict.USER_NAME_CN) == null ? "" : (String)userInfo.get(Dict.USER_NAME_CN));
                    }};
                    result.add(user);
                }
            }
            return result;
        }
        if(userId instanceof  String) {
            if(CommonUtils.isNullOrEmpty(userId)) {
                return new HashMap<>();
            }
            if(!CommonUtils.isNullOrEmpty(userMap.get(userId))) {
                Map<String, Object> userInfo = userMap.get(userId);
                return new HashMap<String, String>(){{
                    put(Dict.ID, (String) userId);
                    put(Dict.USER_NAME_EN, userInfo.get(Dict.USER_NAME_EN) == null ? "" : (String)userInfo.get(Dict.USER_NAME_EN));
                    put(Dict.USER_NAME_CN, userInfo.get(Dict.USER_NAME_CN) == null ? "" : (String)userInfo.get(Dict.USER_NAME_CN));
                }};
            }
        }
        return null;
    }

    /**
     * 从小组信息map中取小组名称
     * @param groupId
     * @param groupMap
     * @return
     */
    public String getGroupName(String groupId, Map<String, Map> groupMap) {
        if(CommonUtils.isNullOrEmpty(groupId)) {
            return "";
        }
        Map<String, Object> groupInfo = groupMap.get(groupId);
        if(!CommonUtils.isNullOrEmpty(groupInfo)) {
            return (String) groupInfo.get(Dict.NAME);
        }else {
            return "";
        }
    }

    /**
     * 从需求信息map中取需求编号
     * @param demandId
     * @param demandMap
     * @return
     */
    public String getDemandNo(String demandId, Map<String, Map> demandMap) {
        if(CommonUtils.isNullOrEmpty(demandId)) {
            return "";
        }
        Map<String, Object> demandInfo = demandMap.get(demandId);
        if(!CommonUtils.isNullOrEmpty(demandInfo)) {
            return (String) demandInfo.get(Dict.OA_CONTACT_NO);
        }else {
            return "";
        }
    }

    /**
     * 从小组信息map中取小组名称和id
     * @param groupId
     * @param groupMap
     * @return
     */
    public Map getGroupInfo(String groupId, Map<String, Map> groupMap) {
        if(CommonUtils.isNullOrEmpty(groupId)) {
            return new HashMap();
        }
        Map<String, Object> groupInfo = groupMap.get(groupId);
        if(!CommonUtils.isNullOrEmpty(groupInfo)) {
            return new HashMap<String, String>(){{
                put(Dict.ID, (String) groupInfo.get(Dict.ID));
                put(Dict.NAME, (String) groupInfo.get(Dict.NAME));
            }};
        }else {
            return new HashMap();
        }
    }

    /**
     * 设置指定要返回的字段
     * @param responseFields
     * @param taskMap
     * @param task
     * @param userMap
     * @param groupMap
     */
    public void putRepFields(List<String> responseFields, Map<String, Object> taskMap, FdevTask task, Map<String, Map> userMap, Map<String, Map> groupMap) {
        if (responseFields.contains(Dict.ID)) {
            taskMap.put(Dict.ID, task.getId());
        }
        if (responseFields.contains(Dict.NAME)) {
            taskMap.put(Dict.NAME, task.getName());
        }
        if (responseFields.contains(Dict.STAGE)) {
            taskMap.put(Dict.STAGE, task.getStage());
        }
        if (responseFields.contains(Dict.MASTER)) {
            taskMap.put(Dict.MASTER, getUserName(task.getMaster(), userMap));
        }
        if (responseFields.contains(Dict.SPDB_MASTER)) {
            taskMap.put(Dict.SPDB_MASTER, getUserName(task.getSpdb_master(), userMap));
        }
        if (responseFields.contains(Dict.DEVELOPER)) {
            taskMap.put(Dict.DEVELOPER, getUserName(task.getDeveloper(), userMap));
        }
        if (responseFields.contains(Dict.CONCERN)) {
            taskMap.put(Dict.CONCERN, getUserName(task.getConcern(), userMap));
        }
        if (responseFields.contains(Dict.CREATOR)) {
            taskMap.put(Dict.CREATOR, getUserName(task.getCreator(), userMap));
        }
        if (responseFields.contains(Dict.REVIEWER)) {
            taskMap.put(Dict.REVIEWER, getUserName(task.getReviewer(), userMap));
        }
        if (responseFields.contains(Dict.TESTER)) {
            taskMap.put(Dict.TESTER, getUserName(task.getTester(), userMap));
        }
        if (responseFields.contains(Dict.GROUP)) {
            taskMap.put(Dict.GROUP, getGroupName(task.getGroup(), groupMap));
            taskMap.put(Dict.GROUPID, task.getGroup());
        }
    }

    /**
     * 组合需要的其它字段
     * @param fields
     * @param taskMap
     * @param task
     * @param userMap
     * @param groupMap
     */
    public void addFields(List<String> fields, Map<String, Object> taskMap, FdevTask task, Map<String, Map> userMap, Map<String, Map> groupMap) {
        if (fields.contains(Dict.MASTER)) {
            taskMap.put(Dict.MASTER, getUserName(task.getMaster(), userMap));
        }
        if (fields.contains(Dict.SPDB_MASTER)) {
            taskMap.put(Dict.SPDB_MASTER, getUserName(task.getSpdb_master(), userMap));
        }
        if (fields.contains(Dict.DEVELOPER)) {
            taskMap.put(Dict.DEVELOPER, getUserName(task.getDeveloper(), userMap));
        }
        if (fields.contains(Dict.CONCERN)) {
            taskMap.put(Dict.CONCERN, getUserName(task.getConcern(), userMap));
        }
        if (fields.contains(Dict.CREATOR)) {
            taskMap.put(Dict.CREATOR, getUserName(task.getCreator(), userMap));
        }
        if (fields.contains(Dict.REVIEWER)) {
            taskMap.put(Dict.REVIEWER, getUserName(task.getReviewer(), userMap));
        }
        if (fields.contains(Dict.TESTER)) {
            taskMap.put(Dict.TESTER, getUserName(task.getTester(), userMap));
        }
        if (fields.contains(Dict.GROUP)) {
            taskMap.put(Dict.GROUP, getGroupName(task.getGroup(), groupMap));
            taskMap.put(Dict.GROUPID, task.getGroup());
        }
    }

    /**
     * 设置任务需要批量查询的用户id集合、小组id集合、需求id集合
     * @param task
     * @param taskUserIds
     * @param taskGroupIds
     * @param taskDemandIds
     */
    public static void setIds(FdevTask task, Set<String> taskUserIds, Set<String> taskGroupIds, Set<String> taskDemandIds) {
        if(!CommonUtils.isNullOrEmpty(task.getCreator())) {
            taskUserIds.add(task.getCreator());
        }
        if(!CommonUtils.isNullOrEmpty(task.getDeveloper())) {
            taskUserIds.addAll(Arrays.asList(task.getDeveloper()));
        }
        if(!CommonUtils.isNullOrEmpty(task.getMaster())) {
            taskUserIds.addAll(Arrays.asList(task.getMaster()));
        }
        if(!CommonUtils.isNullOrEmpty(task.getSpdb_master())) {
            taskUserIds.addAll(Arrays.asList(task.getSpdb_master()));
        }
        if(!CommonUtils.isNullOrEmpty(task.getTester())) {
            taskUserIds.addAll(Arrays.asList(task.getTester()));
        }
        if(!CommonUtils.isNullOrEmpty(task.getReviewer())) {
            taskUserIds.add(task.getReviewer());
        }
        if(!CommonUtils.isNullOrEmpty(task.getConcern())) {
            taskUserIds.addAll(Arrays.asList(task.getConcern()));
        }
        if(!CommonUtils.isNullOrEmpty(task.getGroup())) {
            taskGroupIds.add(task.getGroup());
        }
        if(!CommonUtils.isNullOrEmpty(task.getRqrmnt_no())) {
            taskDemandIds.add(task.getRqrmnt_no());
        }
    }

    /**
     * 修改任务时校验计划时间是否在研发单元计划时间范围内
     * @param task
     */
    public void checkPlanTime(FdevTask task) {
        if (CommonUtils.isNotNullOrEmpty(task.getFdev_implement_unit_no())) {
            //查询研发单元信息
            Map<String,Object> fdevImplUnit = new HashMap<>();
            try {
                fdevImplUnit = demandService.queryFdevImplUnitDetail(task.getFdev_implement_unit_no());
            } catch (Exception e) {
                logger.info(">>>>>>>checkPlanTime queryFdevImplUnitDetail fail,{}", e.getMessage());
                throw new FdevException(ErrorConstants.TASK_ERROR, new String[] {"查询研发信息失败"});
            }
            String planStartDate = (String) fdevImplUnit.get("plan_start_date");
            if(CommonUtils.isNotNullOrEmpty(task.getPlan_start_time(), planStartDate)
                    && task.getPlan_start_time().compareTo(planStartDate.replaceAll("-","/")) > 0){
                throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"任务计划开始时间不能大于研发单元计划开始时间"+planStartDate});
            }
            String planInnerTestDate = (String) fdevImplUnit.get("plan_inner_test_date");
            if(CommonUtils.isNotNullOrEmpty(task.getPlan_inner_test_time(), planInnerTestDate)
                    && task.getPlan_inner_test_time().compareTo(planInnerTestDate.replaceAll("-","/")) > 0){
                throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"任务计划内测启动日期不能大于研发单元计划内测启动日期"+planInnerTestDate});
            }
            String planTestDate = (String) fdevImplUnit.get("plan_test_date");
            if(CommonUtils.isNotNullOrEmpty(task.getPlan_uat_test_start_time(), planTestDate)
                    && task.getPlan_uat_test_start_time().compareTo(planTestDate.replaceAll("-","/")) > 0){
                throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"任务计划提交用户测试日期不能大于研发单元计划提交用户测试日期"+planTestDate});
            }
            String planTestFinishDate = (String) fdevImplUnit.get("plan_test_finish_date");
            if(CommonUtils.isNotNullOrEmpty(task.getPlan_rel_test_time(), planTestFinishDate)
                    && task.getPlan_rel_test_time().compareTo(planTestFinishDate.replaceAll("-","/")) > 0){
                throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"任务计划用户测试完成日期不能大于研发单元计划用户测试完成日期"+planTestFinishDate});
            }
            String planProductDate = (String) fdevImplUnit.get("plan_product_date");
            if(CommonUtils.isNotNullOrEmpty(task.getPlan_fire_time(), planProductDate)
                    && task.getPlan_fire_time().compareTo(planProductDate.replaceAll("-","/")) > 0){
                throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"任务计划投产日期不能大于研发单元计划投产日期"+planProductDate});
            }
        }
    }
}
