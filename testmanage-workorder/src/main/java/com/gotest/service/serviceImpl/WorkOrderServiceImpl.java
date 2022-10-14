package com.gotest.service.serviceImpl;


import com.gotest.dao.*;
import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.dict.WorkOrderStage;
import com.gotest.domain.*;
import com.gotest.service.*;
import com.gotest.utils.*;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisClusterConfig;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@SuppressWarnings("all")
@Service
@RefreshScope
public class WorkOrderServiceImpl implements WorkOrderService {


    private static final Logger log = LoggerFactory.getLogger(RedisClusterConfig.class);

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private PlanListTestCaseMapper planListTestCaseMapper;

    @Autowired
    private MyUtil myUtil;
    @Autowired
    private OrderUtil orderUtil;
    @Autowired
    private FdevTransport fdevTransport;
    @Autowired
    private ITaskApi iTaskApi;
    @Autowired
    private INotifyApi iNotifyApi;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private TaskListMapper taskListMapper;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private OrderDimensionServiceImpl orderDimensionService;
    @Autowired
    private MessageFdevMapper messageFdevMapper;
    @Autowired
    private FdevSitMsgMapper fdevSitMsgMapper;
    @Autowired
    private RollbackInfoMapper rollbackInfoMapper;
    @Autowired
    private TaskListService taskListService;
    @Autowired
    private RequireService requireService;
    @Autowired
    private IDemandService demandService;
    @Autowired
    private AduitRecordMapper aduitRecordMapper;
    @Autowired
    private PlanListMapper planListMapper;
    @Autowired
    private PlanlistTestcaseRelationMapper planlistTestcaseRelationMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${manits.issue.url}")
    private String mantis_url;
    @Value("${manits.admin.token}")
    private String mantisAdminToken;
    @Autowired
    private RedisUtils redisUtils;
    @Value("${user.testAdmin.role.id}")
    private String testAdminRoleId;
    @Value("${user.testLeader.role.id}")
    private String testLeaderRoleId;
    @Value("${user.securityTestLeader.role.id}")
    private String securityTestLeaderRoleId;
    @Value("${user.assessor.role.id}")
    private String assessorRoleId;
    @Value("${unit.group.ids}")
    private String unitGroupIdsStr;

    private static Logger logger = LoggerFactory.getLogger(WorkOrderServiceImpl.class);
    @Autowired
    private IUserService userService;
    @Autowired
    private MessageFdevMapper msgFdevMapper;
    @Autowired
    private InformService informService;

    @Override
    public List<WorkOrderUser> queryUserAllWorkOrder(Map map) throws Exception {
        Map<String, Object> user = redisUtils.getCurrentUserInfoMap();
        if (Util.isNullOrEmpty(user)) {
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        String userName = (String) user.get(Dict.USER_NAME_EN);
        String taskName = "%" + ((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET)).trim() + "%";
        Integer pageSize = (Integer) (map.get(Dict.PAGESIZE));
        Integer currentPage = (Integer) (map.get(Dict.CURRENTPAGE));
        Integer start = pageSize * (currentPage - 1);//起始
        //判断权限
        List<String> role_ids = (List<String>) user.get(Dict.ROLE_ID);
        Integer userRole = 0;
        if (role_ids.contains(testAdminRoleId)) {
            userRole = 1;
        }
        //判断是否选择了状态
        String workStage = (String) map.getOrDefault(Dict.STAGE, Constants.FAIL_GET);
        //判断是否选择了人员过滤
        String personFilter = (String) map.getOrDefault(Dict.PERSON, Constants.FAIL_GET);
        //判断是否选择了内测标识过滤
        String sitFlag = (String) map.getOrDefault(Dict.SITFLAG, Constants.FAIL_GET);
        //获取stage排序和group排序
        String groupSort = (String) map.getOrDefault(Dict.GROUPSORT, Constants.FAIL_GET);
        String stageSort = (String) map.getOrDefault(Dict.STAGESORT, Constants.FAIL_GET);
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        List<WorkOrder> userOrderList = workOrderMapper.queryUserAllWorkOrder(taskName, userName, start,
                pageSize, userRole, workStage, personFilter, sitFlag, groupSort, stageSort, orderType);
        List<WorkOrderUser> orderUserList = myUtil.makeEnNameToChName(userOrderList);
        return orderUserList;
    }

    @Override
    public List queryUserAllOrderWithoutPage(Map map) throws Exception {
        String userName = myUtil.getCurrentUserEnName();
        //工单名搜索
        String taskName = "%" + ((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET)).trim() + "%";
        //判断是否选择了状态
        String workStage = (String) map.getOrDefault(Dict.STAGE, Constants.FAIL_GET);
        //判断是否选择了人员过滤
        String personFilter = (String) map.getOrDefault(Dict.PERSON, Constants.FAIL_GET);
        //判断是否选择了内测标识过滤
        String sitFlag = (String) map.getOrDefault(Dict.SITFLAG, Constants.FAIL_GET);
        Map<String, Object> currentUser = redisUtils.getCurrentUserInfoMap();
        if (Util.isNullOrEmpty(currentUser)) {
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        List<String> role_id = (List<String>) currentUser.get(Dict.ROLE_ID);
        //判断权限
        Integer userRole = role_id.contains(testAdminRoleId) ? 50 : 0;
        //工单类型
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        List<WorkOrder> userOrderList = workOrderMapper.queryUserAllOrderWithoutPage(taskName, userName, userRole,
                workStage, personFilter, sitFlag, orderType);
        List<String> groupIds = userOrderList.stream().map(WorkOrder::getFdevGroupId).distinct().collect(Collectors.toList());
        Map<String,String> groupNameMap = null;
        if(!CommonUtils.isNullOrEmpty(groupIds)) {
            List<Map> groupInfos = userService.queryGroupByIds(groupIds);
            if(!CommonUtils.isNullOrEmpty(groupInfos)) {
                groupNameMap = new HashMap<>();
                for (Map<String,String> groupInfo : groupInfos) {
                    groupNameMap.put(groupInfo.get(Dict.ID), groupInfo.get(Dict.NAME));
                }
            }
        }
        List result = new ArrayList();
        for (WorkOrder workOrder : userOrderList) {
            myUtil.addFdevGroupName(workOrder, groupNameMap);
            Map order = Util.beanToMap(workOrder);
            order.put(Dict.TESTERS, myUtil.changeEnNameToCn(workOrder.getTesters()));
            order.put(Dict.WORKMANAGER, myUtil.changeEnNameToCn(workOrder.getWorkManager()));
            order.put(Dict.GROUPLEADER, myUtil.changeEnNameToCn(workOrder.getGroupLeader()));
            order.put(Dict.WORKSTAGE, WorkOrderStage.getStage(workOrder.getStage()));
            order.put(Dict.ORDERTYPE, orderUtil.getOrderTypeCH(workOrder.getOrderType()));
            result.add(order);
        }
        return result;
    }

    /**
     * 首先查询数据库中的该工单的状态值，
     * 如果已经状态被修改之后，那么就无法进行抢单了，
     * 如果还是未分配状态，那么使用redis，进行set设置key -value，是否成功
     * 未成功说明已经有人先手一步抢单了，并占用了操作线程
     * 成功就可以继续抢单，修改数据库中该工单的数据并反悔结果
     *
     * @param orderId 工单主键编号
     * @return 状态值
     */
    @Override
    public Integer orderGrab(String orderId) throws Exception {
        String userName = myUtil.getCurrentUserEnName();
//            String userName = "admin";
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(orderId);
        if (workOrder.getStage().equals("0")) {
            if (redisLock.tryLock(RedisLock.FORDER_ORDER + orderId, orderId, RedisLock.LOCK_TIME)) {
                workOrderMapper.orderGrab(orderId, userName);
                return 1;//抢单成功
            } else {
                return 0;//刚被抢单
            }
        } else {
            return -1;//早已被抢单
        }

    }

    /**
     * 查询待分配工单-----
     * 管理员权限使用
     */
    @Override
    public List<Map> queryAdminAssignOrder(Map map) throws Exception {
        String taskName = "%" + ((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET)).trim() + "%";
        Integer pageSize = (Integer) (map.get(Dict.PAGESIZE));
        Integer currentPage = (Integer) (map.get(Dict.CURRENTPAGE));
        Integer start = pageSize * (currentPage - 1);//起始
        List<WorkOrder> assignOrderList = workOrderMapper.queryAdminAssignOrder(taskName, start, pageSize);
        List<Map> result = new ArrayList<>();
        for (WorkOrder order : assignOrderList) {
            Map orderMap = myUtil.beanToMap(order);
            String fdevGroupId = order.getFdevGroupId();
            try {
                Map groupInfo = userService.queryGroupDetailById(fdevGroupId);
                if (!Util.isNullOrEmpty(groupInfo)) {
                    orderMap.put(Dict.GROUPNAME, String.valueOf(groupInfo.get(Dict.NAME)));
                }
            } catch (Exception e) {
                logger.error("fail to query group info");
            }
            result.add(orderMap);
        }
        return result;
    }

    @Override
    public List<WorkOrder> queryLeaderAssignOrder() throws Exception {
        String userName = myUtil.getCurrentUserEnName();
//        String userName = "admin";
        List<WorkOrder> assignOrderList = workOrderMapper.queryLeaderAssignOrder(userName);
        return assignOrderList;
    }

    @Override
    public WorkOrder queryWorkOrderByNo(String workOrder) throws Exception {
        return workOrderMapper.queryWorkOrderByNo(workOrder);
    }


    /**
     * 新增工单 新增需求
     */
    @Override
    public Integer addWorkOrder(WorkOrder workOrder) throws Exception {
        //生成 流水工单号
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Date nowDate = new Date();
        String dateField3 = simpleDateFormat.format(nowDate);
        List<String> dates = workOrderMapper.queryGroupByDate(dateField3);
        if (!dates.isEmpty()) {
            String workNo = (Long.parseLong(dates.get(0)) + 1L) + "";
            workOrder.setWorkOrderNo(workNo);
        } else {
            String workNo = dateField3 + "0000";
            workOrder.setWorkOrderNo(workNo);
        }
        workOrder.setField3(dateField3);
        workOrder.setFdevGroupId(workOrder.getGroupId());
        workOrder.setStage("1");//开发中
        workOrder.setWorkOrderFlag("0");//测试平台新建
        workOrder.setCreateTime(new Date().getTime() / 1000);
        workOrder.setSitFlag(Constants.NUMBER_0);//默认状态：未分配
        Integer count = workOrderMapper.addWorkOrder(workOrder);
        return count;
    }

    /**
     * 修改工单
     */
    @Override
    public Integer updateWorkOrder(Map map) throws Exception {
        String stage = (String) map.get(Dict.STAGE);
        String workOrderNo = (String) map.get(Dict.WORKORDERNO);
        String mainTaskNo = (String) map.get(Dict.MAINTASKNO);
        //查询工单是否存在
        Integer count = workOrderMapper.queryOrderExist(workOrderNo);
        //查询工单下任务集
        List<String> tasks = taskListMapper.queryTaskNoByOrder(workOrderNo);
        if (!Util.isNullOrEmpty(mainTaskNo)) {
            tasks.add(mainTaskNo);
        }
        if (count == 1) {
            WorkOrder workOrder = orderUtil.makeWorkOrder(map);
            //控制权限，功能测试组长只能修改功能工单，安全测试组长只能修改安全工单
            Map user = redisUtils.getCurrentUserInfoMap();
            List<String> roleIds = (List<String>) user.get(Dict.ROLE_ID);
            if(Constants.ORDERTYPE_SECURITY.equals(workOrder.getOrderType()) &&
                    !roleIds.contains(securityTestLeaderRoleId) && !roleIds.contains(testAdminRoleId) && !roleIds.contains(assessorRoleId)) {
                throw new FtmsException(ErrorConstants.ROLE_ERROR, new String[] {"请联系工单负责人或安全测试组长"});
            }
            if(Constants.ORDERTYPE_FUNCTION.equals(workOrder.getOrderType()) &&
                    !roleIds.contains(testLeaderRoleId) && !roleIds.contains(testAdminRoleId) && !roleIds.contains(assessorRoleId)) {
                throw new FtmsException(ErrorConstants.ROLE_ERROR, new String[] {"请联系工单负责人或测试组长"});
            }
            //防止单选
            List<String> testers = orderUtil.strToList(map, Dict.TESTERS);
            List<String> groupLeader = orderUtil.strToList(map, Dict.GROUPLEADER);
            workOrder.setTesters(String.join(",", testers));
            workOrder.setGroupLeader(String.join(",", groupLeader));
            WorkOrder oldWorkOrder = workOrderMapper.queryWorkOrderByNo(workOrder.getWorkOrderNo());
            String fdevNew = oldWorkOrder.getFdevNew();
            if (Constants.NUMBER_1.equals(oldWorkOrder.getWorkOrderFlag()) && !Constants.NUMBER_1.equals(fdevNew)) {
                //如果是重构前fdev创建的工单，确保所有任务都进sit
                if (!Util.isNullOrEmpty(tasks)){
                    checkAllTaskSit(tasks, stage);
                }
            }
            //防止工单回到待分配状态
            if (!Constants.NUMBER_0.equals(oldWorkOrder.getStage()) && Constants.NUMBER_0.equals(workOrder.getStage())) {
                logger.error("order already assigned");
                throw new FtmsException(ErrorConstants.DATA_UPDATE_ERROR, new String[]{"工单状态已更新，请刷新页面后再提交修改"});
            }
            //如果原状态为待分配且分配了测试组长和组员，那么修改工单状态为开发中
            if (oldWorkOrder.getStage().equals("0") && !Util.isNullOrEmpty(workOrder.getTesters()) && !Util.isNullOrEmpty(workOrder.getGroupLeader())) {
                workOrder.setStage(Constants.NUMBER_1);
            }
            //只在工单进入uat/uat（含风险）/分包测试/分包测试（含风险）/ 时被记录uat提测时间
            if ("3".equals(workOrder.getStage()) || "6".equals(workOrder.getStage()) ||
                    "9".equals(workOrder.getStage()) || "10".equals(workOrder.getStage()) ||
                    "12".equals(workOrder.getStage()) || "13".equals(workOrder.getStage())) {
                String uatSubmitDate = String.valueOf(map.get(Dict.UAT_SUBMIT_DATE));
                if(CommonUtils.isNullOrEmpty(map.get(Dict.UAT_SUBMIT_DATE))) {
                    uatSubmitDate = myUtil.getCurrentDateStr_1();
                }
                String uatSubmitDate1 = uatSubmitDate.replaceAll("-", "/");
                List<String> riskDescription = (List<String>) map.get(Dict.RISKDESCRIPTION);
                String join = StringUtils.join(riskDescription, ",");
                workOrder.setRiskDescription(join);
                workOrder.setUatSubmitDate(OrderUtil.dateStrToLong2(uatSubmitDate1));
            }
            String old_order_State = oldWorkOrder.getStage();
            if (!workOrder.getStage().equals(old_order_State)) {
                //若将老工单状态修改为 分包测试（内测完成）则送fdev uat_test_time字段
                if ("9".equals(workOrder.getStage()) && !Constants.NUMBER_1.equals(fdevNew)) {
                    try {
                        changeUatSubmit(workOrder.getWorkOrderNo());
                    } catch (Exception e) {
                        logger.error("fail to update fdev uatsubmit time");
                    }
                }
                //若将工单状态修改为 含风险状态  则将审批标志重置
                if ("6".equals(workOrder.getStage()) || "10".equals(workOrder.getStage()) || "13".equals(workOrder.getStage())) {
                    workOrder.setApprovalFlag("0");
                }
            }
            if ("4".equals(stage)) {
                //如果将老工单状态改为4已投产，检测所有子任务状态，但凡有一个状态处于uat之前，就报错，不让主任务改已投产
                if (!Util.isNullOrEmpty(tasks) && !Constants.NUMBER_1.equals(fdevNew)) {
                    Map<String, Object> taskInfos = iTaskApi.queryTaskDetailByIds(tasks);
                    for (String taskId : taskInfos.keySet()) {
                        Map taskInfo = (Map) taskInfos.get(taskId);
                        if ("develop".equals(taskInfo.get(Dict.STAGE)) || "sit".equals(taskInfo.get(Dict.STAGE))) {
                            logger.error("task not finished thus order could not step into prod");
                            throw new FtmsException(ErrorConstants.SUBTASK_NOT_FINISH_ERROR);
                        }
                    }
                }
                //放开对效能组的已投产限制
                List<String> unitGroupIds = Arrays.asList(unitGroupIdsStr.split(","));
                if (!unitGroupIds.contains(oldWorkOrder.getFdevGroupId()) && !Constants.NUMBER_1.equals(fdevNew)) {
                    //如果实施单元未归档则不允许修改为已投产
                    Map unitResult = demandService.queryByFdevNoAndDemandId(oldWorkOrder.getUnit());
                    Map unitInfo = (Map) unitResult.get(Dict.IMPLEMENT_UNIT_INFO);
                    if (!Util.isNullOrEmpty(unitInfo)) {
                        int unitStatus = ((Integer) unitInfo.get(Dict.IMPLEMENT_UNIT_STATUS_NORMAL)).intValue();
                        if ((oldWorkOrder.getUnit().startsWith("FDEV") && unitStatus != 8)
                                || (oldWorkOrder.getUnit().startsWith("IPMP") && unitStatus != 7)) {
                            logger.error("unit not finished thus order could not step into prod");
                            throw new FtmsException(ErrorConstants.UPDATE_WORKORDER_STATUS_FAIL);
                        }
                    }
                }
            }
            count = workOrderMapper.updateWorkOrder(workOrder);
            if (1 == count && Constants.NUMBER_1.equals(fdevNew) && ("3".equals(stage) || "9".equals(stage))) {
                //如果新工单更新为uat或者分包测试（内测完成）成功，则改变所有任务玉衡提测插件状态为成功
                for (String taskId : tasks) {
                    iTaskApi.changeTestComponentStatus(taskId, "2");
                }
            }
            //判断是否为fdev任务
            String flag = oldWorkOrder.getWorkOrderFlag();
            //如果非fdev做废弃
            if ("0".equals(flag)) {
                if ("8".equals(workOrder.getStage())) {
                    count = workOrderMapper.dropWorkOrderByWorkNo(workOrder.getWorkOrderNo());
                }
            }
            workOrder = workOrderMapper.queryWorkOrderByNo(workOrderNo);
            myUtil.makeUserToCh(workOrder);
            //合并组员们
            groupLeader.addAll(testers);
            if (Constants.NUMBER_1.equals(flag)) {
                //fdev工单，发送通知给fdev相关人员
                if ("2".equals(stage) && "1".equals(old_order_State)) {
                    sendNotify(tasks, mainTaskNo, fdevNew);
                }
                if(!Constants.NUMBER_1.equals(fdevNew)){
                    //重构前工单，同步fdev测试人员
                    fdevTransport.sendMessageToFdev(workOrderNo, groupLeader);
                    //当状态为uat或分包测试（内测完成）发送邮件通知
                    if ("3".equals(workOrder.getStage()) || "9".equals(workOrder.getStage()) || "13".equals(workOrder.getStage())) {
                        if ("3".equals(workOrder.getStage()) || "9".equals(workOrder.getStage())) {
                            //修改任务标签为 内测完成
                            changeTaskTagFinished(tasks);
                        }
                        //若工单阶段与上次相比未发生变化则不发送邮件
                        if (!workOrder.getStage().equals(old_order_State)) {
                            Set<String> peoples = new HashSet<>();
                            peoples.addAll(groupLeader);
                            HashMap sendDate = (HashMap) myUtil.beanToMap(workOrder);
                            sendDate.forEach((k, v) -> {
                                if (v == null || "".equals(v))
                                    sendDate.put(k, "无");
                            });
                            if ("9".equals(workOrder.getStage())) {
                                sendDate.put(Dict.STAGE, "分包测试");
                            }else if("13".equals(workOrder.getStage())) {
                                sendDate.put(Dict.STAGE, "安全测试(内测完成)");
                            }else {
                                sendDate.put(Dict.STAGE, "uat");
                            }
                            String rqrmntName = workOrder.getDemandName();
                            if (Util.isNullOrEmpty(rqrmntName)) {
                                rqrmntName = "无";
                            }
                            List<String> taskNames = new ArrayList<>();
                            if (!Util.isNullOrEmpty(mainTaskNo)) {
                                Map result = (Map) iTaskApi.queryTaskDetailByIds(Arrays.asList(mainTaskNo)).get(mainTaskNo);
                                taskNames.add((String) result.get(Dict.NAME));
                            }
                            List<TaskList> taskLists = taskListMapper.queryTaskByNo(workOrderNo);
                            for (TaskList taskList : taskLists) {
                                Map result = (Map) iTaskApi.queryTaskDetailByIds(Arrays.asList(taskList.getTaskno())).get(taskList.getTaskno());
                                taskNames.add((String) result.get(Dict.NAME));
                            }
                            sendDate.put(Dict.RQRMNTNAME, rqrmntName);
                            sendDate.put(Dict.TASKLIST, String.join("、", taskNames));
                            try {
                                myUtil.sendFastMail(sendDate, peoples, Constants.UPDATETASK, workOrder.getMainTaskName());
                            } catch (Exception e) {
                                log.error("into UAT send email error:" + e.getStackTrace());
                            }
                        }
                    }
                }else if(Constants.NUMBER_1.equals(fdevNew) && !workOrder.getStage().equals(old_order_State) &&
                        ("3".equals(workOrder.getStage()) || "9".equals(workOrder.getStage()))){
                    //新玉衡发送邮件给任务处理人
                    HashMap sendDate = (HashMap) myUtil.beanToMap(workOrder);
                    sendDate.forEach((k, v) -> {
                        if (v == null || "".equals(v))
                            sendDate.put(k, "无");
                    });
                    if ("9".equals(workOrder.getStage())) {
                        sendDate.put(Dict.STAGE, "分包测试");
                    } else {
                        sendDate.put(Dict.STAGE, "uat");
                    }
                    List<TaskList> taskLists = taskListMapper.queryTaskByNo(workOrderNo);
                    List<String> taskNames = taskLists.stream().map(TaskList::getTaskname).collect(Collectors.toList());
                    sendDate.put(Dict.RQRMNTNAME, workOrder.getDemandName());
                    sendDate.put(Dict.TASKLIST, String.join("、", taskNames));
                    Set peoples = new HashSet();
                    peoples.addAll(groupLeader);
                    try {
                        myUtil.sendFastMail(sendDate, peoples, Constants.UPDATETASK, workOrder.getMainTaskName());
                    } catch (Exception e) {
                        log.error("into UAT send email error:" + e.getStackTrace());
                    }
                }
            }
            return count;
        }
        return 0;


    }

    /**
     * 检测工单下任务是否都已经提测
     *
     * @param tasks
     */
    private void checkAllTaskSit(List<String> tasks, String orderStage) {
        if (Arrays.asList("3,4,6,9,10".split(",")).contains(orderStage)) {
            Map<String, Object> taskInfos = iTaskApi.queryTaskDetailByIds(tasks);
            for (String taskId : tasks) {
                String stage = String.valueOf(((Map) taskInfos.get(taskId)).get(Dict.STAGE));
                if ("create-info".equals(stage) || "develop".equals(stage)) {
                    logger.error("order has unqualified task");
                    throw new FtmsException(ErrorConstants.UPDATE_ORDER_ERROR, new String[]{"工单下有任务未提测"});
                }

            }
        }
    }

    private void sendNotify(List<String> tasks, String mainTaskNo, String fdevNew) throws Exception {
        if (!Util.isNullOrEmpty(tasks)) {
            List<String> users = new ArrayList<String>();
            //子任务id
            if (!Util.isNullOrEmpty(mainTaskNo)) {
                tasks.add(mainTaskNo);
            }
            if (!Util.isNullOrEmpty(tasks)) {
                for (String id : tasks) {
                    Set set = new HashSet<>();
                    //根据任务编号查询任务详情
                    Map taskInfo;
                    if (Constants.NUMBER_1.equals(fdevNew)) {
                        //重构后fdev任务
                        taskInfo = iTaskApi.queryNewTaskDetail(Arrays.asList(id)).get(0);
                    } else {
                        //重构前fdev任务
                        taskInfo = (Map) iTaskApi.queryTaskDetailByIds(Arrays.asList(id)).get(id);
                    }
                    if (MyUtil.isNullOrEmpty(taskInfo)) {
                        return;
                    }
                    Map messageMap = new HashMap();
                    String type = "启动SIT测试";
                    messageMap.put("content", taskInfo.get(Dict.NAME) + "-" + type);
                    messageMap.put("desc", type);
                    messageMap.put("type", "2");
                    messageMap.put("target", Constants.NUMBER_1.equals(fdevNew) ?
                            iTaskApi.queryNewTaskAssignEns(taskInfo) : MyUtil.getTaskPersonNameEn(taskInfo));
                    iNotifyApi.sendUserNotifyForFdev(messageMap, fdevNew);
                }
            }
        }
    }

    @Async
    private void changeTaskTagFinished(List<String> tasks) {
        for (String taskNo : tasks) {
            try {
                Map send = new HashMap();
                send.put(Dict.ID, taskNo);
                send.put(Dict.REST_CODE, "queryTaskDetail");
                send = (Map) restTransport.submitSourceBack(send);
                //更新任务tag
                List<String> tagList = (List<String>) send.get(Dict.TAG);
                orderUtil.updateFdevTag(tagList, "内测完成", Arrays.asList(new String[]{"已提内测", "提测打回", "审核完成"}), taskNo);
            } catch (Exception e) {
                logger.error("fail to query fdev task info and update tag");
            }
        }
    }

    private void addUserIdByRoleName(Set<String> target, Map source, String role) {
        if (!Util.isNullOrEmpty(source.get(role))) {
            List<Map<String, String>> list = (List<Map<String, String>>) source.get(role);
            for (Map m : list) {
                target.add(String.valueOf(m.get(Dict.ID)));
            }
        }
    }


    /**
     * 分配事件，给指定号的工单
     * 设置对应的组长及组员
     */
    @Override
    public Map assignWorkOrder(Map map) throws Exception {
        List<String> testers = orderUtil.strToList(map, Dict.TESTERS);
        List<String> groupLeader = orderUtil.strToList(map, Dict.GROUPLEADER);
        String groupId = String.valueOf(map.get(Dict.GROUPID));
        String workOrderNo = (String) map.getOrDefault(Dict.WORKORDERNO, Constants.FAIL_GET);
        WorkOrder oldWorkOrder = workOrderMapper.queryWorkOrderByNo(workOrderNo);
        workOrderMapper.assignWorkOrder(workOrderNo, String.join(",", testers), String.join(",", groupLeader), groupId);
        //人员拼接
        groupLeader.addAll(testers);
        groupLeader.remove("");//去除空串
        //如果 是fdev推送的工单，那么需要同步测试人员
        String flag = workOrderMapper.queryWorkFlagByWorkNo(workOrderNo);
        if ("1".equals(flag) && !Constants.NUMBER_1.equals(oldWorkOrder.getFdevNew())) {
            fdevTransport.sendMessageToFdev(workOrderNo, groupLeader);
        }
        return map;
    }

    /**
     * 查询总数，我的工单总数，可抢工单总数
     */
    @Override
    public Map orderCount(Map map) throws Exception {
        Map<String, Object> user = redisUtils.getCurrentUserInfoMap();
        if (Util.isNullOrEmpty(user)) {
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        String userName = (String) user.get(Dict.USER_NAME_EN);
        String taskName = "%" + ((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET)).trim() + "%";
        //判断权限
        List<String> role_ids = (List<String>) user.get(Dict.ROLE_ID);
        Integer userRole = 0;
        if (role_ids.contains(testAdminRoleId)) {
            userRole = 1;
        }
        Integer count1 = workOrderMapper.orderAssignCount(taskName);
        //判断是否选择了状态
        String workStage = (String) map.getOrDefault(Dict.STAGE, Constants.FAIL_GET);
        //判断是否选择了人员过滤
        String personFilter = (String) map.getOrDefault(Dict.PERSON, Constants.FAIL_GET);
        //判断是否选择了内测标识过滤
        String sitFlag = (String) map.getOrDefault(Dict.SITFLAG, Constants.FAIL_GET);
        //工单类型，默认查询功能工单
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        Integer count2 = workOrderMapper.orderUserCount(taskName, userName, userRole, workStage, personFilter, sitFlag, orderType);
        Map result = new HashMap();
        result.put(Dict.TOTAL1, count1);
        result.put(Dict.TOTAL2, count2);
        return result;
    }


    @Override
    public List<WorkOrderUser> queryHistoryWorkOrder(Map map) throws Exception {
        String userName = myUtil.getCurrentUserEnName();
        //String userName = "admin";
        String taskName = "%" + ((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET)).trim() + "%";
        Integer pageSize = (Integer) (map.get(Dict.PAGESIZE));
        Integer currentPage = (Integer) (map.get(Dict.CURRENTPAGE));
        Integer start = pageSize * (currentPage - 1);//起始
        Integer userRole = Integer.parseInt((String) map.getOrDefault(Dict.USERROLE, Constants.NUMBER_0));
        String testerName = (String) map.getOrDefault(Dict.TESTERNAME, null); //测试人员名称
        //工单类型，默认查询功能工单
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        List<WorkOrder> myHistoryList = workOrderMapper.queryUserHistoryWorkOrder(testerName, taskName, userName, start, pageSize, userRole, orderType);
        //英文名转中文
        List<WorkOrderUser> orderUserList = myUtil.makeEnNameToChName(myHistoryList);
        return orderUserList;
    }

    /**
     * 历史工单，查询总数
     */
    @Override
    public Map queryHistoryWorkOrderCount(Map map) throws Exception {
        String userName = myUtil.getCurrentUserEnName();
        //String userName = "admin";
        String taskName = "%" + ((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET)).trim() + "%";
        Integer userRole = Integer.parseInt((String) map.getOrDefault(Dict.USERROLE, Constants.NUMBER_0));
        String testerName = (String) map.getOrDefault(Dict.TESTERNAME, null); //测试人员名称
        //工单类型，默认查询功能工单
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        Integer count = workOrderMapper.orderHistoryCount(testerName, taskName, userName, userRole, orderType);
        Map result = new HashMap();
        result.put(Dict.TOTAL, count);
        return result;
    }

    /**
     * 查询工单
     */
    @Override
    public List<WorkOrderUser> queryOrder(Map map) throws Exception {
        String taskName = "%" + ((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET)).trim() + "%";
        Integer pageSize = (Integer) (map.get(Dict.PAGESIZE));
        Integer currentPage = (Integer) (map.get(Dict.CURRENTPAGE));
        String groupId = (String) map.get(Dict.GROUPID);
        Integer start = pageSize * (currentPage - 1);//起始
        //人员过滤
        String userEnName = ((String) map.getOrDefault(Dict.USER_EN_NAME, Constants.FAIL_GET)).trim();
        //工单类型，默认查询功能工单
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        List<WorkOrder> queryList = workOrderMapper.queryWorkOrder(taskName, userEnName, start, pageSize, groupId, orderType);
        //英文名转中文
        List<WorkOrderUser> orderUserList = myUtil.makeEnNameToChName(queryList);
        return orderUserList;
    }

    /**
     * 查询工单处，总数
     */
    @Override
    public Integer queryOrderCount(Map map) throws Exception {
        String taskName = "%" + ((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET)).trim() + "%";
        //人员过滤
        String userEnName = ((String) map.getOrDefault(Dict.USER_EN_NAME, Constants.FAIL_GET)).trim();
        String groupId = (String) map.get(Dict.GROUPID);
        //工单类型，默认查询功能工单
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        Integer count = workOrderMapper.queryOrderCount(taskName, userEnName, groupId, orderType);
        return count;
    }

    /**
     * 误抢 可抢工单 回退操作
     */
    @Override
    public Integer rollBackWorkOrder(String workOrderNo) throws Exception {
        Integer count = workOrderMapper.rollBackWorkOrder(workOrderNo);
        return count;
    }

    @Override
    public void changeUatSubmit(String workNo) throws Exception {
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workNo);
        String fdevNew = workOrder.getFdevNew();
        if (!"1".equals(fdevNew)) {
        String mainTaskNo = workOrder.getMainTaskNo();
        if (Util.isNullOrEmpty(workOrder) || Constants.NUMBER_0.equals(workOrder.getWorkOrderFlag())) {
            return;
        }
        List<String> taskIds = new ArrayList<>();
        if (!Util.isNullOrEmpty(mainTaskNo)) {
            taskIds.add(mainTaskNo);
        }
        List<String> list = taskListMapper.queryTaskByNo(workNo).stream().
                map(taskList -> taskList.getTaskno()).collect(Collectors.toList());
        taskIds.addAll(list);
        for (String taskNo : taskIds) {
            try {
                Map send = new HashMap();
                send.put(Dict.ID, taskNo);
                send.put(Dict.REST_CODE, "queryTaskDetail");
                Map task = (Map) restTransport.submitSourceBack(send);
                //发fdev
                Map sendData = new HashMap();
                sendData.put(Dict.ID, taskNo);
                if (Util.isNullOrEmpty(task.get(Dict.UAT_TEST_TIME))) {
                    sendData.put(Dict.UAT_TEST_TIME, MyUtil.getDateStr());
                }
                List<String> tagList = (List<String>) task.get(Dict.TAG);
                if (!tagList.contains("审核完成")) {
                    tagList.add("审核完成");
                }
                tagList.remove("已提内测");
                tagList.remove("提测打回");
                sendData.put(Dict.TAG, tagList);
                sendData.put(Dict.REST_CODE, "updatetaskinner");
                restTransport.submitSourceBack(sendData);
            } catch (Exception e) {
                continue;
            }
        }
    }
    }

    @Override
    public String queryWorkNoByTaskId(String task_id, String orderType) throws Exception {
        return workOrderMapper.queryWorkNoByTaskId(task_id, orderType);
    }

    public List queryResourceManagement(Map map) throws Exception {
        boolean flag = (boolean) map.get(Dict.IFCONTAINSSUBGROUP);
        List<String> groups = (List<String>) map.get(Dict.FDEVGROUPID);
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> groupIds = new HashSet();
        if (flag) {
            for (String groupId : groups) {
                Map dataMap = new HashMap();
                List<Map<String, String>> mapList = userService.queryChildGroupById(groupId);
                Set<String> collect = mapList.stream().map(e -> e.get(Dict.ID)).collect(Collectors.toSet());
                map.put(Dict.FDEVGROUPID, collect);
                List<Map<String, Object>> resultList = workOrderMapper.queryResourceManagement(map);
                Map sendMap = new HashMap();
                sendMap.put(Dict.GROUPIDS, collect);
                sendMap.put(Dict.REST_CODE, "queryGroupIssueInfo");
                Map<String, Map> isuues = (Map) restTransport.submitSourceBack(sendMap);
                Collection<Map> values = isuues.values();
                int mantis = 0;
                int vaildMantis = 0;
                for (Map issue : values) {
                    if (!Util.isNullOrEmpty(issue)) {
                        mantis += (Integer) issue.get(Dict.COUNT_MANTIS);
                        vaildMantis += (Integer) issue.get(Dict.VALID_COUNT_MANTIS);
                    }
                }
                if (Util.isNullOrEmpty(resultList)) {
                    continue;
                }
                int caseNumber = 0;
                int performCaseNumber = 0;
                int requestNumber = 0;
                for (Map<String, Object> data : resultList) {
                    caseNumber += Integer.valueOf(data.get(Dict.CASENUMBER).toString());
                    performCaseNumber += Integer.valueOf(data.get(Dict.PERFORM_CASE_NUMBER).toString());
                    requestNumber += Integer.valueOf(data.get(Dict.REQUESTNUMBER).toString());
                }
                dataMap.put(Dict.CASENUMBER, caseNumber);
                dataMap.put(Dict.PERFORM_CASE_NUMBER, performCaseNumber);
                dataMap.put(Dict.REQUESTNUMBER, requestNumber);
                dataMap.put(Dict.FDEVGROUPID, groupId);
                Map<String, Object> groupMap = userService.queryGroupDetailById(groupId);
                if (!Util.isNullOrEmpty(groupMap)) {
                    String name = (String) groupMap.get(Dict.NAME);
                    dataMap.put(Dict.FDEVGROUPNAME, name);
                }
                dataMap.put(Dict.COUNT_MANTIS, mantis);
                dataMap.put(Dict.VALID_COUNT_MANTIS, vaildMantis);
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                String format = "";
                if (vaildMantis == 0) {
                    format = "0";
                } else {
                    format = decimalFormat.format((float) vaildMantis / performCaseNumber);
                }
                dataMap.put(Dict.PROPORTION, format);
                dataMap.put(Dict.TESTER, userService.queryGroupTester(groupId));
                result.add(dataMap);
            }
        } else {
            List<Map<String, Object>> resultList = workOrderMapper.queryResourceManagement(map);
            Map sendMap = new HashMap();
            sendMap.put(Dict.GROUPIDS, groups);
            sendMap.put(Dict.REST_CODE, "queryGroupIssueInfo");
            Map<String, Map> issues = (Map) restTransport.submitSourceBack(sendMap);
            for (Map<String, Object> data : resultList) {
                String groupId = (String) data.get(Dict.GROUPID);
                Map<String, Object> groupMap = userService.queryGroupDetailById(groupId);
                if (!Util.isNullOrEmpty(groupMap)) {
                    String name = (String) groupMap.get(Dict.NAME);
                    data.put(Dict.FDEVGROUPNAME, name);
                }
                Map issue = issues.get(groupId);
                Integer countMantis = 0;
                Integer vaildMantis = 0;
                if (!Util.isNullOrEmpty(issue)) {
                    countMantis = (Integer) issue.get(Dict.COUNT_MANTIS);
                    vaildMantis = (Integer) issue.get(Dict.VALID_COUNT_MANTIS);
                    data.put(Dict.COUNT_MANTIS, issue.get(Dict.COUNT_MANTIS));
                    data.put(Dict.VALID_COUNT_MANTIS, issue.get(Dict.VALID_COUNT_MANTIS));
                }
                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                Integer performCaseNumber = Integer.valueOf((String.valueOf(data.get(Dict.PERFORM_CASE_NUMBER))));
                String format = decimalFormat.format((float) vaildMantis / performCaseNumber);
                if (performCaseNumber != 0) {
                    data.put(Dict.PROPORTION, format);
                } else {
                    data.put(Dict.PROPORTION, 1);
                }
                data.put(Dict.TESTER, userService.queryGroupTester(groupId));
                result.add(data);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, String>> queryWorkOrderStageList(String workNo, String groupId) throws Exception {
        List<Map<String, String>> stageList = new ArrayList<>();
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workNo);
        String workOrderFlag = workOrder.getWorkOrderFlag();// 0 内部工单
        String[] stageArrays = null;
        if (!Util.isNullOrEmpty(workOrder)) {
            if (Util.isNullOrEmpty(groupId)) {
                groupId = workOrder.getGroupId();
            }
            String[] groupLists = new String[]{"9", "10", "12"};
            //改小组非零售组
            if (ArrayUtils.contains(groupLists, groupId)) {
                if ("0".equals(workOrderFlag)) {
                    stageArrays = new String[]{"1", "2", "3", "4", "6", "8"};
                } else {
                    stageArrays = new String[]{"1", "2", "3", "4", "6"};
                }
            } else {
                if ("0".equals(workOrderFlag)) {
                    stageArrays = new String[]{"1", "2", "3", "4", "6", "8", "9", "10"};
                } else {
                    stageArrays = new String[]{"1", "2", "3", "4", "6", "9", "10"};
                }
            }
            //如果是安全工单，返回12-安全测试（内测完成）、13-安全测试（含风险）、14-安全测试（不涉及）
            if(Constants.ORDERTYPE_SECURITY.equals(workOrder.getOrderType())) {
                stageArrays = new String[]{"1", "2", "12", "13", "14"};
            }
            for (int i = 0; i < stageArrays.length; i++) {
                Map<String, String> stage = new HashMap<String, String>();
                stage.put(Dict.STAGECNNAME, WorkOrderStage.getStage(stageArrays[i]));
                stage.put(Dict.STAGE, stageArrays[i]);
                stageList.add(stage);
            }
        }
        return stageList;
    }

    private void statisticalMantis(Map<String, Integer> countMantis, String workNo) {
        if (countMantis.get(workNo) != null) {
            int num = countMantis.get(workNo) + 1;
            countMantis.put(workNo, num);
        } else {
            countMantis.put(workNo, 1);
        }
    }

    @Override
    public Integer updateSitFlag(Map<String, String> map) throws Exception {
        String workNo = map.getOrDefault(Dict.JOBID, Constants.FAIL_GET);
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workNo);
        String updateFlag = "0";
        if (MyUtil.isNullOrEmpty(workOrder.getFstSitDate())) {
            updateFlag = "1";
        }
        String dateStr = MyUtil.getCurrentDateStr_1();
        return workOrderMapper.updateSitFlag(workNo, updateFlag, dateStr);
    }

    /**
     * 根据任务id查询案例列表
     *
     * @param taskNo
     * @param orderType
     * @return
     * @throws Exception
     */
    public Map<String, Object> queryTestcaseByTaskId(String taskNo, String orderType) throws Exception {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> testcases = new ArrayList<>();
        //先判断需求改版后新老工单
        String workNo = workOrderMapper.queryWorkNoByTaskId(taskNo, orderType);
        if (Util.isNullOrEmpty(workNo)) {
            TaskList taskList = taskListMapper.queryTaskByTaskNo(taskNo);
            if (Util.isNullOrEmpty(taskList)) {
                return null;
            }
            workNo = taskList.getWorkno();
        }
        List<Map<String, String>> resultList = workOrderMapper.queryTestcaseByTaskId(workNo);
        if (Util.isNullOrEmpty(resultList)) {
            return null;
        }
        for (Map m : resultList) {
            result.put(Dict.TASKNAME, m.get(Dict.MAINTASKNAME));
            Map<String, String> testcase = new HashMap<>();
            testcase.put(Constants.TESTCASENAME, String.valueOf(m.get(Constants.TESTCASENAME)));
            testcase.put(Constants.EXPECTEDRESULT, String.valueOf(m.get(Constants.EXPECTEDRESULT)));
            testcases.add(testcase);
        }
        result.put(Constants.TESTCASES, testcases);
        return result;
    }


    @Override
    public List<WorkOrder> queryWorkOrderList() throws Exception {
        return workOrderMapper.queryWorkOrderList();
    }

    /**
     * 查询废弃工单
     *
     * @param map
     * @return
     * @throws Exception
     */
    public List<WorkOrderUser> queryWasteOrder(Map map) throws Exception {
        String userName = myUtil.getCurrentUserEnName();
        String taskName = "%" + ((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET)).trim() + "%";
        Integer pageSize = (Integer) (map.get(Dict.PAGESIZE));
        Integer currentPage = (Integer) (map.get(Dict.CURRENTPAGE));
        Integer start = pageSize * (currentPage - 1);//起始
        Integer userRole = Integer.parseInt((String) map.getOrDefault(Dict.USERROLE, Constants.NUMBER_0));
        String testerName = (String) map.getOrDefault(Dict.TESTERNAME, null); //测试人员名称
        //工单类型，默认查询功能工单
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        List<WorkOrder> myHistoryList = workOrderMapper.queryWasteOrder(testerName, taskName, userName, start, pageSize, userRole, orderType);
        //英文名转中文
        List<WorkOrderUser> orderUserList = myUtil.makeEnNameToChName(myHistoryList);
        return orderUserList;
    }

    /**
     * 废弃工单，查询总数
     */
    @Override
    public Map queryWasteOrderCount(Map map) throws Exception {
        String userName = myUtil.getCurrentUserEnName();
        String taskName = "%" + ((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET)).trim() + "%";
        Integer userRole = Integer.parseInt((String) map.getOrDefault(Dict.USERROLE, Constants.NUMBER_0));
        String testerName = (String) map.getOrDefault(Dict.TESTERNAME, null); //测试人员名称
        //工单类型，默认查询功能工单
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        Integer count = workOrderMapper.queryWasteOrderCount(testerName, taskName, userName, userRole, orderType);
        Map result = new HashMap();
        result.put(Dict.TOTAL, count);
        return result;
    }

    /**
     * 废弃工单的计划或者案例迁移
     *
     * @param map
     * @return
     * @throws Exception
     */
    public Integer movePlanOrCase(Map map) throws Exception {
        String moveType = String.valueOf(map.get(Constants.MOVETYPE));
        Integer count = 0;
        WorkOrder newOrder = workOrderMapper.queryWorkOrderByNo(String.valueOf(map.get(Constants.TOWORKNO)));
        try {
            if (Constants.NUMBER_1.equals(moveType)) {
                //进行计划迁移,计划可多选
                List<Integer> fromPlanIds = (List<Integer>) map.get(Constants.FROMPLANID);
                String toWorkNo = String.valueOf(map.get(Constants.TOWORKNO));
                //如果目标工单为空则迁移失败
                if (Util.isNullOrEmpty(toWorkNo)) return Constants.DEFAULT_0;
                for (Integer fromPlanId : fromPlanIds) {
                    count = workOrderMapper.movePlan(fromPlanId, toWorkNo);
                    //更新缺陷
                    updateMantisByPlan(fromPlanId, toWorkNo, newOrder.getUnit());
                }
            } else {
                //进行案例迁移
                List<Integer> resultIds = (List<Integer>) map.get(Dict.PLANTCASEID);
                Integer toPlanId = (Integer) map.get(Constants.TOPLANID);
                String toWorkNo = String.valueOf(map.get(Constants.TOWORKNO));
                if (Util.isNullOrEmpty(toPlanId)) return Constants.DEFAULT_0;
                for (Integer resultId : resultIds) {
                    count = workOrderMapper.moveCase(resultId, toPlanId, toWorkNo);
                    //更新缺陷
                    updateMantisByResultId(resultId, toWorkNo, newOrder.getUnit());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_UPDATE_ERROR, new String[]{"工单迁移失败"});
        }
        return count;
    }

    /**
     * 计划迁移,根据原计划id和目标工单号改变原计划下缺陷
     *
     * @param planId
     * @param workNo
     * @throws Exception
     */
    private void updateMantisByPlan(Integer planId, String workNo, String unit) throws Exception {
        //计划下所有案例的缺陷都要变更
        List<Integer> resultIds = planListTestCaseMapper.queryResultIdByPlanId(planId);
        //遍历案例
        resultIds.parallelStream().forEach(resultId -> {
            try {
                //更新关系表里workNo
                Integer row = planListTestCaseMapper.updateWorkNoByResultId(resultId, workNo);
                if (row != 1) {
                    logger.info("fail to update work_no in FTMS_PLAN_RESULT");
                }
                updateMantisByResultId(resultId, workNo, unit);
            } catch (Exception e) {
                lambdaThrowException(e);
            }
        });
    }

    /**
     * 根据案例关系id修改其下缺陷到指定工单下
     *
     * @param resultId
     * @param workNo
     * @throws Exception
     */
    private void updateMantisByResultId(Integer resultId, String workNo, String unit) throws Exception {
        //发mantis接口查缺陷
        Map send = new HashMap();
        send.put(Dict.ID, resultId);
        send.put(Dict.REST_CODE, "queryIssueByPlanResultId");
        List<Map<String, String>> mantisList = new ArrayList<>();
        try {
            mantisList = (List<Map<String, String>>) restTransport.submit(send);
        } catch (Exception e) {
            lambdaThrowException(e);
        }
        //若有缺陷，遍历缺陷
        for (Map<String, String> mantis : mantisList) {
            String id = String.valueOf(mantis.get(Dict.ID));
            String url = new StringBuilder(mantis_url).append("/api/rest/issues/").append(id).toString();
            Map<String, Object> sendMap = new HashMap<String, Object>();
            sendMap.put(Dict.WORKNO, assemblyParamMap(Dict.ID, workNo));
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            list.add(assemblyCustomMap(13, workNo));//'工单号'
            list.add(assemblyCustomMap(10, unit));//unit
            sendMap.put(Dict.CUSTOM_FIELDS, list);
            try {
                fdevTransport.sendPatch(url, mantisAdminToken, sendMap);
            } catch (Exception e) {
                logger.error("======" + e.getMessage());
                throw new FtmsException(ErrorConstants.UPDATE_MANTIS_ISSUES_ERROR);
            }
        }
    }

    public Map<String, Object> assemblyCustomMap(Integer id, Object value) {
        Map<String, Object> customMap = new HashMap<String, Object>();
        Map<String, Integer> custom_item = new HashMap<String, Integer>();
        custom_item.put(Dict.ID, id);
        customMap.put(Dict.FIELD, custom_item);
        customMap.put(Dict.VALUE, value);
        return customMap;
    }

    public Map<String, Object> assemblyParamMap(String type, Object value) {
        Map<String, Object> projectMap = new HashMap<String, Object>();
        projectMap.put(type, value);
        return projectMap;
    }

    static <E extends Exception> void lambdaThrowException(Exception e) throws E {
        throw (E) e;
    }

    /**
     * 查询当前角色的有效工单(排除投产和废弃)
     *
     * @param map
     * @return
     * @throws Exception
     */
    public List<Map<String, String>> queryUserValidOrder(Map map) throws Exception {
        String userName = myUtil.getCurrentUserEnName();
        String userRole = String.valueOf(map.get(Dict.USERROLE));
        String orderType = (String) map.getOrDefault(Dict.ORDERTYPE, Constants.ORDERTYPE_FUNCTION);
        List<Map<String, String>> result = workOrderMapper.queryUserValidOrder(userName, userRole, orderType);
        return result;
    }

    @Override
    public int writeObstacle(Map map) throws Exception {
        String workNo = String.valueOf(map.get(Dict.WORKNO));
        String obstacle = String.valueOf(map.get(Dict.OBSTACLE));
        int row = workOrderMapper.writeObstacle(workNo, obstacle);
        return row;
    }

    @Override
    public Map queryRqrFilesByWorkNo(String workNo) throws Exception {
        //查询工单
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workNo);
        if (Util.isNullOrEmpty(workOrder)) {
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"工单不存在！"});
        }
        String mainTaskNo = workOrder.getMainTaskNo();
        String workOrderFlag = workOrder.getWorkOrderFlag();
        Map result = new HashMap();
        List<Map<String, String>> rqrmnt = new ArrayList<>();
        List<Map<String, String>> rqrmntRule = new ArrayList<>();
        List<Map<String, String>> other = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(workOrder.getFdevNew()) && "1".equals(workOrder.getFdevNew())) {
          //新fdev工单下查询需求关联的文档，获取当前工单的需求id
            String demandId = workOrder.getDemandId();
            //根据需求id查询需求关联的文档
          Map<String,Object> send=new HashMap<>();
          send.put(Dict.REST_CODE,"queryDocFileByDemandId");
          send.put("demandId",demandId);
         List<Map<String,Object>> docsInfos =(List<Map<String, Object>>) restTransport.submitSourceBack(send);
            if (!Util.isNullOrEmpty(docsInfos)){
                for (Map<String, Object> docsInfo : docsInfos) {
                    String docName =(String) docsInfo.get(Dict.NAME);
                    String path=(String) docsInfo.get(Dict.PATH);
                    Map<String, String> docs = new HashMap<>();
                    docs.put(Dict.NAME, docName);
                    docs.put(Dict.DOC, path);
                    rqrmnt.add(docs);
                }
            }
        } else {
            try {
                //改造后数据查询
                if (Util.isNullOrEmpty(mainTaskNo) && Constants.NUMBER_1.equals(workOrderFlag)) {
                    //根据实施单元查询需求id
                    String unitNo = workOrder.getUnit();
                    Map<String, Object> unitResult = demandService.queryByFdevNoAndDemandId(unitNo);
                    String rqrmntNo = String.valueOf(((Map) unitResult.get(Dict.IMPLEMENT_UNIT_INFO)).get(Dict.DEMAND_ID));
                    //根据需求id查询需求文档
                    Map sendMap = new HashMap();
                    sendMap.put(Dict.REST_CODE, "queryDemandDoc");
                    sendMap.put(Dict.DEMAND_ID, rqrmntNo);
                    Map<String, Object> returnData = (Map<String, Object>) restTransport.submitSourceBack(sendMap);
                    if (!Util.isNullOrEmpty(returnData)) {
                        List<Map<String, Object>> docsInfo = (List<Map<String, Object>>) returnData.get("data");
                        if (!Util.isNullOrEmpty(docsInfo)) {
                            for (Map<String, Object> docInfo : docsInfo) {
                                String docName = (String) docInfo.get(Dict.DOC_NAME);
                                String docType = (String) docInfo.get(Dict.DOC_TYPE);
                                String docPath = (String) docInfo.get(Dict.DOC_PATH);
                                Map<String, String> docs = new HashMap<>();
                                docs.put(Dict.NAME, docName);
                                docs.put(Dict.DOC, docPath);
                                if (Dict.DEMANDINSTRUCTION.equals(docType)) {
                                    rqrmnt.add(docs);
                                }
                                if (Dict.DEMANDPLANINSTRUCTION.equals(docType)) {
                                    rqrmntRule.add(docs);
                                }
                                if (Dict.OTHERRELATEDFILE.equals(docType)) {
                                    other.add(docs);
                                }
                            }
                        }
                    }
                } else if (!Util.isNullOrEmpty(mainTaskNo) && Constants.NUMBER_1.equals(workOrderFlag)) {
                    Map sendMap = new HashMap();
                    sendMap.put(Dict.REST_CODE, "queryTaskDetail");
                    sendMap.put(Dict.ID, mainTaskNo);
                    try {
                        Map task = (Map) restTransport.submitSourceBack(sendMap);
                        String rqrmntNo = String.valueOf(task.get(Dict.RQRMNT_NO));
                        sendMap.put(Dict.REST_CODE, "queryDemandDoc");
                        sendMap.put(Dict.DEMAND_ID, rqrmntNo);
                        Map<String, Object> returnData = (Map<String, Object>) restTransport.submitSourceBack(sendMap);
                        if (!Util.isNullOrEmpty(returnData)) {
                            List<Map<String, Object>> docsInfo = (List<Map<String, Object>>) returnData.get("data");
                            if (!Util.isNullOrEmpty(docsInfo)) {
                                for (Map<String, Object> docInfo : docsInfo) {
                                    String docName = (String) docInfo.get(Dict.DOC_NAME);
                                    String docType = (String) docInfo.get(Dict.DOC_TYPE);
                                    String docPath = (String) docInfo.get(Dict.DOC_PATH);
                                    Map<String, String> docs = new HashMap<>();
                                    docs.put(Dict.NAME, docName);
                                    docs.put(Dict.DOC, docPath);
                                    if (Dict.DEMANDINSTRUCTION.equals(docType)) {
                                        rqrmnt.add(docs);
                                    }
                                    if (Dict.DEMANDPLANINSTRUCTION.equals(docType)) {
                                        rqrmntRule.add(docs);
                                    }
                                    if (Dict.OTHERRELATEDFILE.equals(docType)) {
                                        other.add(docs);
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                        logger.error("query task error: task_id = " + mainTaskNo);
                        throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"任务查询失败"});
                    }
                }
            } catch (Exception e) {
                logger.error("old data not exist");
            }
        }
        result.put(Dict.RQRMNTINSTRUCTION, rqrmnt);
        result.put(Dict.RQRMNTRULE, rqrmntRule);
        result.put(Dict.OTHERDOC, other);
        return result;
    }

    private Map<String, String> getDocNameById(Map<String, Object> map) throws Exception {
        Map<String, String> result = new HashMap<>();
        List<Map<String, Object>> docs = (List<Map<String, Object>>) map.get(Dict.VALUE);
        for (Map<String, Object> doc : docs) {
            result.put(String.valueOf(doc.get(Dict.ID)), String.valueOf(doc.get(Dict.NAME)));
        }
        return result;
    }

    @Override
    public boolean verifyOrderName(String mainTaskName) throws Exception {
        //新增工单 工单名不可重复
        List<WorkOrder> workOrder = workOrderMapper.queryWorkOrderByOrderName(mainTaskName, Constants.ORDERTYPE_FUNCTION);
        boolean allAbandon = false;
        for (WorkOrder o : workOrder) {
            if ("11".equals(o.getStage())) {
                allAbandon = true;
            } else {
                allAbandon = false;
            }
        }
        //如果查不到该实施单元命名的工单（说明从未创建过该实施单元）或者该实施单元已经废弃，允许创建
        if (workOrderMapper.verifyOrderName(mainTaskName) == 0 || allAbandon) {
            return true;
        }
        ;
        return false;
    }

    @Override
    @Transactional
    public void splitWorkOrder(String workNo, List<String> taskIds, String name, List<String> testers, String selectedWorkNo) throws Exception {
        //查询原来的工单
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workNo);
        if (CommonUtils.isNullOrEmpty(workOrder)) {
            logger.error("工单号为{}不存在！", workNo);
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"工单不存在！"});
        }
        //不允许工单下的所有任务全部被拆分
        List<TaskList> taskLists = taskListMapper.queryTaskByNo(workNo);
        if (taskIds.size() >= taskLists.size() || taskIds.size() == 0) {
            throw new FtmsException(ErrorConstants.SPLIT_ORDER_FAIL, new String[]{"工单号为" + workNo + "的工单下的任务少于两个"});
        }
        //判断是选择拆到已有工单还是新工单
        if (!Util.isNullOrEmpty(selectedWorkNo)) {
            handleTaskAndWorkOrderRelation(workNo, taskIds, selectedWorkNo);
            WorkOrder selectedWorkOrder = workOrderMapper.queryWorkOrderByNo(selectedWorkNo);
            //将工单的状态改为开发中
            String stage = selectedWorkOrder.getStage();
            //根据工单状态，在创建任务时改为开发中
            if ("3".equals(stage) || "4".equals(stage) || "9".equals(stage)) {
                selectedWorkOrder.setStage(Constants.NUMBER_1);
                workOrderMapper.updateWorkOrder(selectedWorkOrder);
            }
            //如果是含风险的修改approvalFlag
            if ("6".equals(stage) || "10".equals(stage)) {
                selectedWorkOrder.setStage(Constants.NUMBER_1);
                selectedWorkOrder.setApprovalFlag(Constants.NUMBER_0);
                workOrderMapper.updateWorkOrder(selectedWorkOrder);
            }
        } else {
            //判断传参新工单名和测试人员是否为空
            if (Util.isNullOrEmpty(name) || Util.isNullOrEmpty(testers)) {
                throw new FtmsException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"names || testers"});
            }
            //生成新工单的工单号
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            Date nowDate = new Date();
            String dateField3 = simpleDateFormat.format(nowDate);
            do {
                //获取当天生成的所有工单号，按大小倒序排列
                List<String> dates = workOrderMapper.queryGroupByDate(dateField3);
                //当天已有工单数据，则加一生成新工单号
                if (!dates.isEmpty()) {
                    String newWorkNo = (Long.parseLong(dates.get(0)) + 1L) + "";
                    workOrder.setWorkOrderNo(newWorkNo);
                } else {
                    //当天未有工单数据，生成当天第一条工单号
                    String newWorkNo = dateField3 + "0000";
                    workOrder.setWorkOrderNo(newWorkNo);
                }
            } while (workOrderMapper.queryOrderExist(workOrder.getWorkOrderNo()) > 0);//防止高并发导致相同工单
            workOrder.setField3(dateField3);
            //设置新工单名
            workOrder.setMainTaskName(name);
            //设置创建时间
            workOrder.setCreateTime(new Date().getTime() / 1000);
            //设置测试人员
            workOrder.setTesters(String.join(",", testers));
            //创建新的工单
            workOrderMapper.addWorkOrder(workOrder);
            handleTaskAndWorkOrderRelation(workNo, taskIds, workOrder.getWorkOrderNo());
        }
    }

    private void handleTaskAndWorkOrderRelation(String workNo, List<String> taskIds, String newWorkOrderNo) throws Exception {
        //修改缺陷数据
        requireService.handleIssues(taskIds, workNo, newWorkOrderNo, null);
        //修改任务列表对应的工单号
        taskListMapper.updateWorkNoByTaskNos(taskIds, newWorkOrderNo);
        //修改msg_fdev表
        messageFdevMapper.updateWorkNoByTaskNos(taskIds, newWorkOrderNo);
        //修改FTMS_SUBMIT_SIT_RECORD表
        fdevSitMsgMapper.updateWorkNoByTaskNos(taskIds, newWorkOrderNo);
        //修改FTMS_ROLLBACK_INFO表
        rollbackInfoMapper.updateWorkNoByTaskNos(taskIds, newWorkOrderNo);
        //判断拆出的两个工单下的任务提测情况
        if (taskListService.isAllTasksInSitByWorkNo(workNo)) {
            workOrderMapper.updateSitFlagUpByWorkNo(workNo);
        } else {
            workOrderMapper.updateSitFlagDownByWorkNo(workNo);
        }
        if (taskListService.isAllTasksInSitByWorkNo(newWorkOrderNo)) {
            workOrderMapper.updateSitFlagUpByWorkNo(newWorkOrderNo);
        } else {
            workOrderMapper.updateSitFlagDownByWorkNo(newWorkOrderNo);
        }
    }

    @Override
    public String createWorkOrder(Map<String, Object> map) throws Exception {
        String unitNo = (String) map.getOrDefault(Dict.UNITNO, Constants.FAIL_GET);
        //查询是否已经创建该实施单元的工单
        if (verifyOrderName(unitNo)) {
            WorkOrder workOrder = new WorkOrder();
            //生成流水工单号
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            Date nowDate = new Date();
            String dateField3 = simpleDateFormat.format(nowDate);
            do {
                //获取当天生成的所有工单号，按大小倒序排列
                List<String> dates = workOrderMapper.queryGroupByDate(dateField3);
                //当天已有工单数据，则加一生成新工单号
                if (!dates.isEmpty()) {
                    String workNo = (Long.parseLong(dates.get(0)) + 1L) + "";
                    workOrder.setWorkOrderNo(workNo);
                } else {
                    //当天未有工单数据，生成当天第一条工单号
                    String workNo = dateField3 + "0000";
                    workOrder.setWorkOrderNo(workNo);
                }
            } while (workOrderMapper.queryOrderExist(workOrder.getWorkOrderNo()) > 0);//防止高并发导致相同工单
            Map unitResult = demandService.queryByFdevNoAndDemandId(unitNo);
            String rqrmntNo = String.valueOf(((Map) unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NO));
            String rqrmntName = String.valueOf(((Map) unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NAME));
            workOrder.setWorkOrderFlag(Constants.NUMBER_1);//fdev传递的
            workOrder.setUnit((String) map.getOrDefault(Dict.UNITNO, Constants.FAIL_GET));
            workOrder.setPlanSitDate((String) map.getOrDefault(Dict.INTERNALTESTSTART, Constants.FAIL_GET));
            workOrder.setPlanUatDate((String) map.getOrDefault(Dict.INTERNALTESTEND, Constants.FAIL_GET));
            workOrder.setPlanProDate((String) map.getOrDefault(Dict.EXPECTEDPRODUCTDATE, Constants.FAIL_GET));
            workOrder.setMainTaskName((String) map.getOrDefault(Dict.UNITNO, Constants.FAIL_GET));
            workOrder.setRemark((String) map.getOrDefault(Dict.REQUIREREMARK, Constants.FAIL_GET));
            workOrder.setCreateTime(new Date().getTime() / 1000);
            workOrder.setField3(dateField3);//当天日期
            workOrder.setStage(Constants.NUMBER_0);//默认状态：未分配
            workOrder.setSitFlag(Constants.NUMBER_0);//sit_flag默认为0
            workOrder.setDemandNo(rqrmntNo);
            workOrder.setDemandName(rqrmntName);
            String fdevGroupId = (String) map.getOrDefault(Dict.GROUP_ID, Constants.FAIL_GET);
            workOrder.setFdevGroupId(fdevGroupId);//保存实施单元组id
            workOrder.setFdevGroupName((String) map.getOrDefault(Dict.GROUP_NAME, Constants.FAIL_GET));//保存实施单元组名
            try {
                //自动分配工单
                if (!Util.isNullOrEmpty(fdevGroupId)) {
                    Map<String, String> resultMap = groupMapper.queryAutoWorkOrder(fdevGroupId);
                    if (!Util.isNullOrEmpty(resultMap)) {
                        workOrder.setWorkManager(resultMap.get(Dict.WORKMANAGER));
                        workOrder.setGroupLeader(resultMap.get(Dict.GROUPLEADER));
                    } else {
                        List<Map> groups = userService.queryParentGroupById(fdevGroupId);
                        if (!Util.isNullOrEmpty(groups)) {
                            //获取其父组
                            Map group = groups.get(1);
                            Map<String, String> result = groupMapper.queryAutoWorkOrder((String) group.get(Dict.ID));
                            if (!Util.isNullOrEmpty(result)) {
                                workOrder.setWorkManager(result.get(Dict.WORKMANAGER));
                                workOrder.setGroupLeader(result.get(Dict.GROUPLEADER));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("fail to allocate group" + e.toString());
            }
            if (workOrderMapper.addWorkOrder(workOrder) == 1) {
                return workOrder.getWorkOrderNo().toString();
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> queryWorkNoByUnitNo(String unitNo) throws Exception {
        Map<String, Object> result = new HashMap<>();
        unitNo = demandService.getUnitNo(unitNo);
        List<WorkOrder> workOrderList = workOrderMapper.queryWorkOrderByUnit(unitNo, Constants.ORDERTYPE_FUNCTION);
        if (Util.isNullOrEmpty(workOrderList)) {
            result.put(Dict.WORKNOS, new ArrayList<>());
        }
        List<String> workNos = workOrderList.stream().map(workOrder -> workOrder.getWorkOrderNo()).collect(Collectors.toList());
        result.put(Dict.WORKNOS, workNos);
        return result;
    }

    @Override
    public void updateTimeByUnitNo(String unitNo, String planSitDate, String planUatDate, String planProDate) {
        //判断待修改的时间都存在
        if (Util.isNullOrEmpty(planSitDate) && Util.isNullOrEmpty(planUatDate) && Util.isNullOrEmpty(planProDate)) {
            throw new FtmsException(ErrorConstants.UPDATA_DATE_FAIL, new String[]{"修改的时间不能都为空"});
        }
        //查询所有实施单元下的工单
        List<WorkOrder> workOrderList = workOrderMapper.queryWorkOrderByUnit(unitNo, Constants.ORDERTYPE_FUNCTION);
        if (Util.isNullOrEmpty(workOrderList)) {
            throw new FtmsException(ErrorConstants.UPDATA_DATE_FAIL, new String[]{"待修改的工单不存在"});
        }
        //修改工单的时间
        for (WorkOrder workOrder : workOrderList) {
            if (!Util.isNullOrEmpty(planSitDate)) {
                workOrder.setPlanSitDate(planSitDate);
            }
            if (!Util.isNullOrEmpty(planUatDate)) {
                workOrder.setPlanUatDate(planUatDate);
            }
            if (!Util.isNullOrEmpty(planProDate)) {
                workOrder.setPlanProDate(planProDate);
            }
            workOrderMapper.updateWorkOrder(workOrder);
        }
    }

    @Override
    public String queryWorkOrderName(String unit) throws Exception {
        //查询同一实施单元下的工单
        List<WorkOrder> workOrderList = workOrderMapper.queryWorkOrderByUnit(unit, Constants.ORDERTYPE_FUNCTION);
        //设置新工单名
        String workOrderName = workOrderList.get(0).getMainTaskName();
        String newWorkOrderName = "";
        if (workOrderList.size() < 10) {
            newWorkOrderName = workOrderName + "_0" + workOrderList.size();
        } else {
            newWorkOrderName = workOrderName + "_" + workOrderList.size();
        }
        return newWorkOrderName;
    }


    @Override
    @Transactional
    public void mergeWorkOrder(String workOrderName, List<String> workNos, String workNo) throws Exception {
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workNo);
        if (Util.isNullOrEmpty(workOrder)) {
            logger.error("工单号为{}不存在！", workNo);
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"工单不存在！"});
        }
        List<String> taskIds = new ArrayList<>();
        for (String workOrderNo : workNos) {
            List<TaskList> taskLists = taskListMapper.queryTaskByNo(workOrderNo);
            List<String> taskNos = taskLists.stream().map(taskList -> taskList.getTaskno()).collect(Collectors.toList());
            taskIds.addAll(taskNos);
            requireService.handleIssues(taskNos, workOrderNo, workNo, null);
        }
        //修改待合并工单下任务的工单号
        taskListMapper.updateWorkNoByTaskNos(taskIds, workNo);
        //修改msg_fdev表
        messageFdevMapper.updateWorkNoByTaskNos(taskIds, workNo);
        //修改FTMS_SUBMIT_SIT_RECORD表
        fdevSitMsgMapper.updateWorkNoByTaskNos(taskIds, workNo);
        //修改FTMS_ROLLBACK_INFO表
        rollbackInfoMapper.updateWorkNoByTaskNos(taskIds, workNo);
        //修改FTMS_PLAN_RESULT表
        planlistTestcaseRelationMapper.updateWorkNoByWorkNos(workNos, workNo);
        //修改FTMS_TESTCASE_EXE_RECORD表
        planlistTestcaseRelationMapper.updateTestCaseExeWorkNoByWorkNos(workNos, workNo);
        //修改plan_list表
        planListMapper.updateWorkNoByWorkNos(workNos, workNo);
        //修改work_order_aduit_record表
        aduitRecordMapper.updateAduitWorkNoByWorkNos(workNos, workNo);
        List<PlanList> allPlanLists = planListMapper.queryByworkNo(workNo);
        //如果执行计划名字相同进行处理
        Map<String, Long> collect = allPlanLists.stream().map(PlanList::getPlanName).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        collect.forEach((k, v) -> {
            if (v > 1) {
                int num = 1;
                for (PlanList planList : allPlanLists) {
                    Integer planId = planList.getPlanId();
                    String planName = planList.getPlanName();
                    if (k.equals(planName)) {
                        planName = planName + "_" + num;
                        planListMapper.updatePlanNameByPlanId(planId, planName);
                        num++;
                    }
                }
            }
        });

        //修改新工单的信息
        workOrder.setMainTaskName(workOrderName);
        //判断工单的提测情况
        if (taskListService.isAllTasksInSitByWorkNo(workNo)) {
            workOrder.setSitFlag(Constants.NUMBER_1);
        } else {
            workOrder.setSitFlag(Constants.NUMBER_0);
        }
        workOrderMapper.updateWorkOrder(workOrder);
        //刪除需要合并的工单
        workOrderMapper.deleteWorkOrdersByWorkNos(workNos);
    }

    @Override
    public List<WorkOrder> querySplitOrderList(String workNo, String unitNo) throws Exception {
        if (Util.isNullOrEmpty(unitNo)) {
            return null;
        }
        //查询同一实施单元下所有工单
        List<WorkOrder> workOrderList = workOrderMapper.queryWorkOrderByUnit(unitNo, Constants.ORDERTYPE_FUNCTION);
        workOrderList = workOrderList.stream().filter(workOrder -> !workNo.equals(workOrder.getWorkOrderNo()))
                .filter(workOrder -> !"4".equals(workOrder.getStage())).collect(Collectors.toList());
        Map<String, WorkOrder> map = workOrderList.stream().collect(Collectors.toMap(WorkOrder::getWorkOrderNo, workOrder -> workOrder));
        for (WorkOrder workOrder : workOrderList) {
            if ("1".equals(workOrder.getFdevNew())) {
                //新fdev生成的工单
                List<TaskList> taskLists = taskListMapper.queryTaskByNo(workOrder.getWorkOrderNo());
                //排除空任务的工单
                if (Util.isNullOrEmpty(taskLists)) {
                    map.remove(workOrder.getWorkOrderNo());
                    continue;
                }
              //排除含有投产或者归档任务的工单
                int num = 0;
                for (TaskList taskList : taskLists) {
                    Map<String, Object> taskMap = iTaskApi.getNewTaskById(taskList.getTaskno());
                    if (Util.isNullOrEmpty(taskMap)) {
                        num++;
                        break;
                    }
                    String endDate =(String) taskMap.get("endDate");
                    if (!Util.isNullOrEmpty(endDate)){
                        num++;
                        break;
                    }
                }
                if (num!=0){
                    map.remove(workOrder.getWorkOrderNo());
                }

            }else {
            List<TaskList> taskLists = taskListMapper.queryTaskByNo(workOrder.getWorkOrderNo());
            //排除空任务的工单
            if (Util.isNullOrEmpty(taskLists)) {
                map.remove(workOrder.getWorkOrderNo());
                continue;
            }
            //排除含有投产或者归档任务的工单
            Map<String, Object> taskMap = iTaskApi.queryTaskDetailByIds(taskLists.stream().map(taskList -> taskList.getTaskno()).collect(Collectors.toList()));
            int num = 0;
            for (TaskList taskList : taskLists) {
                Map<String, Object> task = (Map<String, Object>) taskMap.get(taskList.getTaskno());
                if (Util.isNullOrEmpty(task)) {
                    num++;
                    break;
                }
                String stage = String.valueOf(task.get(Dict.STAGE));
                if (Dict.PRODUCTION.equals(stage) || Dict.FILE.equals(stage)) {
                    num++;
                    break;
                }
            }
            if (num != 0) {
                map.remove(workOrder.getWorkOrderNo());
            }
        }
        }
        return new ArrayList<WorkOrder>(map.values());
    }

    /**
     * 查询是否是新老工单
     * @param taskId
     * @return
     */
    @Override
    public String queryNewFdevByTaskNo(String taskId) {
        String newFdev = workOrderMapper.queryNewFdevBytaskId(taskId, Constants.ORDERTYPE_FUNCTION);
        return newFdev;
    }

    @Override
    public void updateOrderStage(String workOrderNo, String stage) {
        workOrderMapper.updateOrderStage(workOrderNo, stage);
    }

    @Override
    public Map queryTaskNameTestersByNo(String workOrderNo) throws Exception {
        WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workOrderNo);
        Map<String, Object> map = new HashMap<>();
        StringBuilder testers = new StringBuilder();
        if(!Util.isNullOrEmpty(workOrder.getTesters())){
            String[] names = workOrder.getTesters().split(",");
            for(String name : names){
                try {
                    Map user = userService.queryUserCoreDataByNameEn(name);
                    if(!Util.isNullOrEmpty(user)){
                        testers.append(user.get(Dict.USER_NAME_CN)+ "  ");
                    }
                } catch (Exception e) {
                    logger.error("fail to get userInfo for : " + name);
                }
            }
        }
        map.put(Dict.TESTERS, testers.toString().trim());
        map.put("workOrder", workOrder);
        return map;
    }

    @Override
    public Map queryOrderByTaskId(String taskNo, String orderType) throws Exception {
        return workOrderMapper.queryWorkOrderMemberByTaskNo(taskNo, orderType);
    }

    /**
     * 修改工单的状态为开发中
     * @param workOrderNo
     * @param s
     * @param s1
     */
    @Override
    public void updateOrderStageAndSitFlag(String workOrderNo, String stage, String sitFlag) {
        workOrderMapper.updateOrderStageAndSitFlag(workOrderNo,stage,sitFlag);
    }


    @Override
    public List<Map> queryMergeOrderList(String workNo, String unitNo) throws Exception {
        if (Util.isNullOrEmpty(unitNo)) {
            return null;
        }
        //查询同一个实施单元下的所有工单
        List<WorkOrder> workOrderList = workOrderMapper.queryWorkOrderByUnit(unitNo, Constants.ORDERTYPE_FUNCTION);
        if (Util.isNullOrEmpty(workOrderList)) {
            return null;
        }
        workOrderList = workOrderList.stream().filter(workOrder -> !workNo.equals(workOrder.getWorkOrderNo()))
                .filter(workOrder -> !"4".equals(workOrder.getStage())).collect(Collectors.toList());
        //查询工单下的任务列表详情
        List<Map> result = new ArrayList<>();
        for (WorkOrder workOrder : workOrderList) {
            //新老工单
            if ("1".equals(workOrder.getFdevNew())) {
                List<TaskList> taskLists = taskListMapper.queryTaskByNo(workOrder.getWorkOrderNo());
                if (Util.isNullOrEmpty(taskLists)) {
                    continue;
                }
                //判断任务是否归档的标识
                int num = 0;
                List<Map> taskDetailList = new ArrayList<>();
                for (TaskList taskList : taskLists) {
                    Map<String, Object> map = iTaskApi.getNewTaskById(taskList.getTaskno());
                    if (Util.isNullOrEmpty(map)) {
                        num++;
                        break;
                    }
                    taskDetailList.add(map);
                }
                //如果工单下所有任务不处于已归档就取出该工单和任务信息
                if (num == 0) {
                    Map workOrderMap = Util.beanToMap(workOrder);
                    workOrderMap.put(Dict.TASKLIST, taskDetailList);
                    result.add(workOrderMap);
                }
            } else {
            List<TaskList> taskLists = taskListMapper.queryTaskByNo(workOrder.getWorkOrderNo());
            if (Util.isNullOrEmpty(taskLists)) {
                continue;
            }
            //判断任务是否归档的标识
            int num = 0;
            List<Map> taskDetailList = new ArrayList<>();
            for (TaskList taskList : taskLists) {
                Map map = (Map) iTaskApi.queryTaskDetailByIds(Arrays.asList(taskList.getTaskno())).get(taskList.getTaskno());
                if (Util.isNullOrEmpty(map)) {
                    num++;
                    break;
                }
                taskDetailList.add(map);
            }
            //如果工单下所有任务不处于已归档就取出该工单和任务信息
            if (num == 0) {
                Map workOrderMap = Util.beanToMap(workOrder);
                workOrderMap.put(Dict.TASKLIST, taskDetailList);
                result.add(workOrderMap);
            }
        }
        }
        return result;

    }

    @Override
    public String createSecurityWorkOrder(String taskNo, String taskName, String unitNo,
                                          String remark, String correlationSystem, String correlationInterface,
                                          String interfaceFilePath, String transFilePath, String appName,
                                          String developer, String taskGroup, List<Map> transList) throws Exception {
        Map unitResult = demandService.queryByFdevNoAndDemandId(unitNo);
        Map unitInfo = (Map) unitResult.get(Dict.IMPLEMENT_UNIT_INFO);
        String rqrmntNo = String.valueOf(((Map) unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NO));
        String rqrmntName = String.valueOf(((Map) unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NAME));
        //查询是否已经创建该任务的安全测试工单
        List<WorkOrder> workOrderList = workOrderMapper.queryOrderByTaskNo(taskNo, Constants.ORDERTYPE_SECURITY);
        if (CommonUtils.isNullOrEmpty(workOrderList)) {
            //新增安全测试工单
            WorkOrder workOrder = new WorkOrder();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.YYYYMMDD);
            Date nowDate = new Date();
            String dateField3 = simpleDateFormat.format(nowDate);
            workOrder.setWorkOrderFlag(Constants.NUMBER_1);//fdev传递的
            workOrder.setUnit(unitNo);
            workOrder.setPlanSitDate((String) unitInfo.getOrDefault(Dict.PLAN_INNER_TEST_DATE, Constants.FAIL_GET));
            workOrder.setPlanUatDate((String) unitInfo.getOrDefault(Dict.PLAN_TEST_DATE, Constants.FAIL_GET));
            workOrder.setPlanProDate((String) unitInfo.getOrDefault(Dict.PLAN_PRODUCT_DATE, Constants.FAIL_GET));
            workOrder.setMainTaskNo(taskNo);
            workOrder.setMainTaskName(taskName);
            workOrder.setRemark(remark);
            workOrder.setCreateTime(new Date().getTime() / 1000);
            workOrder.setField3(dateField3);//当天日期
            workOrder.setStage(Constants.NUMBER_0);//默认状态：未分配
            workOrder.setSitFlag(Constants.NUMBER_1);//sit_flag默认为1
            workOrder.setDemandNo(rqrmntNo);
            workOrder.setDemandName(rqrmntName);
            String fdevGroupId = (String) unitInfo.get(Dict.GROUP);
            workOrder.setFdevGroupId(fdevGroupId);//保存实施单元组id
            workOrder.setOrderType(Constants.ORDERTYPE_SECURITY);//工单类型
            try {
                //查询安全测试组长
                if (!Util.isNullOrEmpty(fdevGroupId)) {
                    Map<String, String> resultMap = groupMapper.queryAutoWorkOrder(fdevGroupId);
                    if (!Util.isNullOrEmpty(resultMap)) {
                        workOrder.setWorkManager(resultMap.get(Dict.WORKMANAGER));
                        workOrder.setGroupLeader(resultMap.get(Dict.SECURITYLEADER));
                    } else {
                        List<Map> groups = userService.queryParentGroupById(fdevGroupId);
                        if (!Util.isNullOrEmpty(groups)) {
                            //获取其父组
                            Map group = groups.get(1);
                            Map<String, String> result = groupMapper.queryAutoWorkOrder((String) group.get(Dict.ID));
                            if (!Util.isNullOrEmpty(result)) {
                                workOrder.setWorkManager(result.get(Dict.WORKMANAGER));
                                workOrder.setGroupLeader(result.get(Dict.SECURITYLEADER));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("fail to allocate group" + e.toString());
            }
            if(CommonUtils.isNullOrEmpty(workOrder.getGroupLeader())) {
                throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[] {"未找到牵头小组的安全测试组长，暂不支持提交安全测试"});
            }
            //获取当天生成的所有工单号，按大小倒序排列
            List<String> dates = workOrderMapper.queryGroupByDate(dateField3);
            //当天已有工单数据，则加一生成新工单号
            if (!dates.isEmpty()) {
                String workNo = (Long.parseLong(dates.get(0)) + 1L) + "";
                workOrder.setWorkOrderNo(workNo);
            } else {
                //当天未有工单数据，生成当天第一条工单号
                String workNo = dateField3 + "0000";
                workOrder.setWorkOrderNo(workNo);
            }
            if (workOrderMapper.addWorkOrder(workOrder) == 1) {
                String workOrderNo = workOrder.getWorkOrderNo();
                //发送玉衡通知
                sendMsg(workOrder);
                //记录提测信息
                addPutTestInfo(taskNo, remark, appName, developer, workOrderNo, rqrmntNo+"/"+rqrmntName,
                        taskGroup, correlationSystem, correlationInterface, interfaceFilePath, transFilePath, transList);
                return workOrder.getWorkOrderNo();
            }
        }else {
            WorkOrder workOrder = workOrderList.get(0);
            workOrder.setSitFlag(Constants.NUMBER_1);
            workOrderMapper.updateWorkOrder(workOrder);
            //记录提测信息
            addPutTestInfo(taskNo, remark, appName, developer, workOrder.getWorkOrderNo(), rqrmntNo+"/"+rqrmntName,
                    taskGroup, correlationSystem, correlationInterface, interfaceFilePath, transFilePath, transList);
            return workOrder.getWorkOrderNo();
        }
        return null;
    }

    /**
     * 发送玉衡消息
     * @param workOrder
     */
    @Async
    public void sendMsg(WorkOrder workOrder) {
        Set<String> allUser = new HashSet<>();
        // 发送给 管理员
        if (!Util.isNullOrEmpty(workOrder.getWorkManager())) {
            allUser.add(workOrder.getWorkManager());
        }
        //发送 给测试组长
        if (!Util.isNullOrEmpty(workOrder.getGroupLeader())) {
            allUser.addAll(Arrays.asList(workOrder.getGroupLeader().split(",")));
        }
        // 发送给 测试人员
        if (!Util.isNullOrEmpty(workOrder.getTesters())) {
            allUser.addAll(Arrays.asList(workOrder.getTesters().split(",")));
        }
        MessageFdev messageFdev = null;
        for (String userName : allUser) {
            messageFdev = new MessageFdev();
            messageFdev.setRqrNo(workOrder.getDemandNo());
            messageFdev.setUserEnName(userName);
            messageFdev.setTaskNo(workOrder.getMainTaskNo());
            messageFdev.setTaskName(workOrder.getMainTaskName());
            messageFdev.setWorkNo(workOrder.getWorkOrderNo());
            messageFdev.setWorkStage(workOrder.getStage());
            messageFdev.setTaskReason(Constants.NUMBER_1);
            messageFdev.setJiraNo("");
            messageFdev.setTaskDesc(workOrder.getRemark());
            messageFdev.setCreateTime((int) (new Date().getTime() / 1000));
            messageFdev.setMessageFlag(Constants.NUMBER_1); //未读
            msgFdevMapper.addMsgFromFdev(messageFdev);
        }
    }

    /**
     * 记录提测信息
     * @param taskNo
     * @param remark
     * @param appName
     * @param developer
     * @param workOrderNo
     * @param rqrNo
     * @param taskGroup
     * @param correlationSystem
     * @param correlationInterface
     * @param interfaceFilePath
     * @param transFilePath
     * @param transList
     * @throws Exception
     */
    @Async
    private void addPutTestInfo(String taskNo, String remark, String appName, String developer, String workOrderNo,
                               String rqrNo, String taskGroup, String correlationSystem, String correlationInterface,
                               String interfaceFilePath, String transFilePath, List<Map> transList) throws Exception {
        FdevSitMsg fdevSitMsg = new FdevSitMsg();
        fdevSitMsg.setTaskNo(taskNo);
        fdevSitMsg.setTestReason(Constants.NUMBER_1);
        fdevSitMsg.setRepairDesc(remark);
        fdevSitMsg.setJiraNo("");
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        String date = sdf.format(new Date());
        fdevSitMsg.setCreateTime(date);
        fdevSitMsg.setAppName(appName);
        fdevSitMsg.setClientVersion("不涉及");
        fdevSitMsg.setRegressionTestScope("不涉及");
        fdevSitMsg.setTestEnv("不涉及");
        fdevSitMsg.setDeveloper(developer);
        fdevSitMsg.setOtherSystemChange("否");
        fdevSitMsg.setDatabaseChange("否");
        fdevSitMsg.setInterfaceChange("不涉及");
        fdevSitMsg.setWorkNo(workOrderNo);
        fdevSitMsg.setRqrNo(rqrNo);
        fdevSitMsg.setGroupId(taskGroup);
        fdevSitMsg.setCopyTo("");
        fdevSitMsg.setCorrelationSystem(correlationSystem);
        fdevSitMsg.setCorrelationInterface(correlationInterface);
        fdevSitMsg.setInterfaceFilePath(interfaceFilePath);
        fdevSitMsg.setTransFilePath(transFilePath);
        String fdevSitMsgId = informService.addFdevSitMsg(fdevSitMsg);
        //添加交易清单表数据
        informService.addSecurityTestTrans(fdevSitMsgId, transList);
    }

    @Override
    public Map<String, Object> querySecurityTestResult(List<String> taskIds) {
        List<Map> noPassTaskList = workOrderMapper.queryNoPassSecurityOrder(taskIds);
        Map<String,Object> result = new HashMap<>();
        result.put(Dict.NOPASSTASKLIST, noPassTaskList);
        return result;
    }

    @Override
    public List<WorkOrder> queryWorkOrderByNos(List<String> workNos, List<String> fields) {
        if (CommonUtils.isNullOrEmpty(fields)) {
            return workOrderMapper.queryWorkOrderByNos(workNos);
        } else {
            List<WorkOrder> workOrderList = workOrderMapper.queryWorkOrderByNos(workNos);
            Map<String, Map> groupMap = new HashMap<>();
            if (fields.contains(Dict.GROUPNAME)) {
                List<String> groupIds = workOrderList.stream().map(WorkOrder::getFdevGroupId).distinct().collect(Collectors.toList());
                groupIds.remove(null);
                try {
                    List<Map> groupList = userService.queryGroupByIds(groupIds);
                    for (Map group : groupList) {
                        groupMap.put((String) group.get(Dict.ID), group);
                    }
                } catch (Exception e) {
                    logger.info(">>>queryGroupByIds fail");
                }
            }
            for (WorkOrder workOrder : workOrderList) {
                if (fields.contains(Dict.GROUPNAME) && !CommonUtils.isNullOrEmpty(groupMap)) {
                    Map group = groupMap.get(workOrder.getFdevGroupId());
                    if (!CommonUtils.isNullOrEmpty(group)) {
                        workOrder.setFdevGroupName((String) group.get(Dict.NAME));
                    }
                }
            }
            return workOrderList;
        }
    }
}

