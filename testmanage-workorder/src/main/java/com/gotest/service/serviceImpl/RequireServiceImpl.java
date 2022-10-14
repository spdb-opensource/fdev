package com.gotest.service.serviceImpl;

import com.gotest.dao.*;
import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.domain.*;
import com.gotest.service.*;
import com.gotest.utils.CommonUtils;
import com.gotest.utils.MyUtil;
import com.gotest.utils.OrderUtil;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@Service
@RefreshScope
public class RequireServiceImpl implements RequireService {

    @Autowired
    private WorkOrderMapper workOrderMapper;

    @Autowired
    private TaskListMapper taskListMapper;

    @Autowired
    private PlanListTestCaseMapper pltCMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private TaskListService taskListService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private RollbackInfoMapper rollbackInfoMapper;

    @Autowired
    private FdevSitMsgMapper fdevSitMsgMapper;

    @Autowired
    private MessageFdevMapper messageFdevMapper;

    @Autowired
    private MyUtil myUtil;
    @Autowired
    private OrderUtil orderUtil;
    @Value("${manits.admin.token}")
    private String mantisAdminToken;
    @Autowired
    private RestTransport restTransport;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private IUserService userService;
    @Autowired
    private INotifyApiImpl iNotifyApi;
    @Value("${unit.group.ids}")
    private String unitGroupIdsStr;
    @Autowired
    private IDemandService demandService;


    /**
     * 根据fdev数据判断创建主工单/任务集
     * fdev实施单元-玉衡工单，fdev任务-玉衡任务集
     *
     * @return 返回工单编号
     */
    @Override
    @Transactional
    public String createFdevOrder(Map map) throws Exception {
        String unitNo = (String) map.get(Dict.UNITNO);
        if (unitNo == null || "".equals(unitNo)) {
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"未选择实施单元"});
        } else {
            List<String> unitGroupIds = Arrays.asList(unitGroupIdsStr.split(","));
            Map unitResult = demandService.queryByFdevNoAndDemandId(unitNo);
            Map unitInfo = (Map) unitResult.get(Dict.IMPLEMENT_UNIT_INFO);
            TaskList taskList;
            WorkOrder workOrder;
            //如果是效能组单独创建工单
            if (unitGroupIds.contains(unitInfo.get(Dict.GROUP))) {
                //创建工单
                workOrder = buildWorkOrder(unitResult, (String) map.get(Dict.TASKNAME), unitNo);
                if(Util.isNullOrEmpty(workOrder)){
                    throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR,new String[]{"创建工单失败！"});
                }
                //添加到任务列表
                taskList = buildTaskList(workOrder.getWorkOrderNo(), (String) map.getOrDefault(Dict.TASKNAME,
                        Constants.FAIL_GET), (String) map.getOrDefault(Dict.TASKNO,Constants.FAIL_GET), unitNo,
                        (String) map.getOrDefault(Dict.REQUIREREMARK, Constants.FAIL_GET));
            }else {
                //判断工单是否存在
                List<WorkOrder> workOrderList = workOrderMapper.queryWorkOrderByUnit(unitNo, Constants.ORDERTYPE_FUNCTION);
                if (CommonUtils.isNullOrEmpty(workOrderList)) {
                    logger.error("实施单元编号为{}的工单不存在", unitNo);
                    throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"工单不存在"});
                }
                workOrder = workOrderList.get(0);
                //判断任务是否已经被添加过
                String taskNo = (String) map.get(Dict.TASKNO);
                if (taskListMapper.queryTaskExist(taskNo, workOrder.getWorkOrderNo()) != 0) {
                    logger.error("任务编号为{}的任务已经添加过工单号为{}的工单", taskNo, workOrderList.get(0).getWorkOrderNo());
                    throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR, new String[]{"任务重复插入"});
                }
                //查询工单的状态是否已经提测，提测了改为未提测
                String sitFlag = workOrder.getSitFlag();
                if (Constants.NUMBER_1.equals(sitFlag)) {
                    workOrderMapper.updateSitFlagDownByWorkNo(workOrderList.get(0).getWorkOrderNo());
                }
                //将工单的状态改为开发中
                String stage = workOrder.getStage();
                //根据工单状态，在创建任务时改为开发中
                if ("3".equals(stage) || "4".equals(stage) || "9".equals(stage)) {
                    workOrder.setStage(Constants.NUMBER_1);
                    workOrderMapper.updateWorkOrder(workOrder);
                }
                //如果是含风险的修改approvalFlag
                if ("6".equals(stage) || "10".equals(stage)) {
                    workOrder.setStage(Constants.NUMBER_1);
                    workOrder.setApprovalFlag(Constants.NUMBER_0);
                    workOrderMapper.updateWorkOrder(workOrder);
                }
                //添加到任务列表
                taskList = buildTaskList(workOrderList.get(0).getWorkOrderNo(),
                        (String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET),
                        taskNo, unitNo, (String) map.getOrDefault(Dict.REQUIREREMARK, Constants.FAIL_GET));
            }
            if (taskListMapper.addTaskList(taskList) == 1) {
                try {
                    notifyFtmsUser(workOrder, String.valueOf(map.getOrDefault(Dict.TASKNAME, "")));
                } catch (Exception e) {
                    logger.error("fail to notify ftms");
                }
                return taskList.getWorkno();
            }
        }
        return "Create WorkOrder Exception error";
    }

    private TaskList buildTaskList(String workNo,String taskName,String taskNo,String unitNo,String remark) {
        TaskList taskList = new TaskList();
        taskList.setWorkno(workNo);
        taskList.setTaskname(taskName);
        taskList.setTaskno(taskNo);
        taskList.setTaskunit(unitNo);
        taskList.setField1(remark);
        return taskList;
    }

    /**
     * 根据任务创建工单
     */
    private WorkOrder buildWorkOrder(Map unitResult, String taskName, String unitNo) {
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
        Map unitInfo = (Map) unitResult.get(Dict.IMPLEMENT_UNIT_INFO);
        String rqrmntNo = String.valueOf(((Map) unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NO));
        String rqrmntName = String.valueOf(((Map) unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NAME));
        workOrder.setWorkOrderFlag(Constants.NUMBER_1);//fdev传递的
        workOrder.setUnit(unitNo);
        workOrder.setPlanSitDate((String) unitInfo.getOrDefault(Dict.PLAN_INNER_TEST_DATE, Constants.FAIL_GET));
        workOrder.setPlanUatDate((String) unitInfo.getOrDefault(Dict.PLAN_TEST_DATE, Constants.FAIL_GET));
        workOrder.setPlanProDate((String) unitInfo.getOrDefault(Dict.PLAN_PRODUCT_DATE, Constants.FAIL_GET));
        workOrder.setMainTaskName(taskName);
        workOrder.setCreateTime(new Date().getTime() / 1000);
        workOrder.setField3(dateField3);//当天日期
        workOrder.setStage(Constants.NUMBER_0);//默认状态：未分配
        workOrder.setSitFlag(Constants.NUMBER_0);//sit_flag默认为0
        workOrder.setDemandNo(rqrmntNo);
        workOrder.setDemandName(rqrmntName);
        String fdevGroupId = (String) unitInfo.get(Dict.GROUP);
        workOrder.setFdevGroupId(fdevGroupId);//保存实施单元组id
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
            return workOrder;
        }
        return null;
    }

    /**
     * fdev查询工单状态
     *
     * @param taskNo 任务编号
     * @param orderType
     * @return 返回一个Map，，map里面两条数据，一条是工单状态，一条是查询到的所有数据的List<Map<key, value>>
     */
    @Override
    public Map queryFdevOrderState(String taskNo, String orderType,Integer page,Integer pageSize) throws Exception {
        //分页
        Integer start = null ;
        if ( !Util.isNullOrEmpty(page) && !Util.isNullOrEmpty(pageSize) && 0 != pageSize ) {
            start = pageSize * (page - 1) ;   //起始
        }else{
            pageSize = null ;
        }
        //确保老数据可用，先判断main_task_no是否有值
        String oldWorkNo = workOrderMapper.queryWorkNoByTaskId(taskNo, orderType);
        if (Util.isNullOrEmpty(oldWorkNo)) {
            //查询任务对应的工单编号
            TaskList taskList = taskListMapper.queryTaskByTaskNo(taskNo);
            if (!CommonUtils.isNullOrEmpty(taskList)) {
                String workorderNo = taskList.getWorkno();
                Map resultMap = new HashMap();
                List<Map<String, String>> testPlan = new ArrayList<>();
                WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workorderNo);
                resultMap.put(Dict.TESTERS, myUtil.testersStrToUser(workOrder));
                String stageStatus = workOrder.getStage();
                if ("6".equals(stageStatus) || "10".equals(stageStatus)) {
                    if ("1".equals(workOrder.getApprovalFlag())) {
                        stageStatus = "3";
                    }
                }
                if (!Util.isNullOrEmpty(workOrder.getUatSubmitDate())) {
                    resultMap.put(Dict.UAT_SUBMIT_DATE, OrderUtil.timeStampToStr(workOrder.getUatSubmitDate(), OrderUtil.dateFormate2));
                }
                if (!Util.isNullOrEmpty(stageStatus)) {
                    List<PlanListTestCase> resultList = pltCMapper.queryFdevOrderState(workorderNo,start,pageSize);
                    Map<String, String> tempMap;
                    for (PlanListTestCase pltc : resultList) {
                        tempMap = new HashMap<>();
                        tempMap.put(Dict.TESTPLANNAME, pltc.getTestplanName());
                        tempMap.put(Dict.ALLCOUNT, pltc.getAllCount().toString());
                        tempMap.put(Dict.ALLPASSED, pltc.getAllPassed().toString());
                        tempMap.put(Dict.ALLFAILED, pltc.getAllFailed().toString());
                        tempMap.put(Dict.ALLBLOCKED, pltc.getAllBlocked().toString());
                        testPlan.add(tempMap);
                    }
                    resultMap.put(Dict.STAGESTATUS, stageStatus);
                    resultMap.put(Dict.TESTPLAN, testPlan);
                    resultMap.put(Dict.COUNT, pltCMapper.queryFdevOrderStateCount(workorderNo));
                } else {
                    resultMap.put(Dict.STAGESTATUS, Constants.NUMBER_0);  //工单状态 ： 0未分配
                    resultMap.put(Dict.TESTPLAN, testPlan);
                    resultMap.put(Dict.COUNT,0);
                }
                resultMap.put(Dict.WORK_NO,workorderNo);
                return resultMap;
            }
        } else {
            Map resultMap = new HashMap();
            List<Map<String, String>> testPlan = new ArrayList<>();
            WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(oldWorkNo);
            String stageStatus = workOrder.getStage();
            if ("6".equals(stageStatus) || "10".equals(stageStatus)) {
                if ("1".equals(workOrder.getApprovalFlag())) {
                    stageStatus = "3";
                }
            }
            if (!Util.isNullOrEmpty(stageStatus)) {
                List<PlanListTestCase> resultList = pltCMapper.queryFdevOrderState(oldWorkNo,start,pageSize);
                Map<String, String> tempMap;
                for (PlanListTestCase pltc : resultList) {
                    tempMap = new HashMap<>();
                    tempMap.put(Dict.TESTPLANNAME, pltc.getTestplanName());
                    tempMap.put(Dict.ALLCOUNT, pltc.getAllCount().toString());
                    tempMap.put(Dict.ALLPASSED, pltc.getAllPassed().toString());
                    tempMap.put(Dict.ALLFAILED, pltc.getAllFailed().toString());
                    tempMap.put(Dict.ALLBLOCKED, pltc.getAllBlocked().toString());
                    testPlan.add(tempMap);
                }
                resultMap.put(Dict.STAGESTATUS, stageStatus);
                resultMap.put(Dict.TESTPLAN, testPlan);
                resultMap.put(Dict.COUNT, pltCMapper.queryFdevOrderStateCount(oldWorkNo));
            } else {
                resultMap.put(Dict.TESTPLAN, testPlan);
                resultMap.put(Dict.COUNT,0);
            }
            if (!Util.isNullOrEmpty(workOrder.getUatSubmitDate())) {
                resultMap.put(Dict.UAT_SUBMIT_DATE, OrderUtil.timeStampToStr(workOrder.getUatSubmitDate(), OrderUtil.dateFormate2));
            }
            resultMap.put(Dict.TESTERS, myUtil.testersStrToUser(workOrder));
            resultMap.put(Dict.WORK_NO,oldWorkNo);
            return resultMap;
        }
        return null;
    }

    /**
     * 删除任务。。.
     * taskNoOnly: 需要删除的子任务编号
     * jobNo:  需要删除的工单(主任务)编号
     * taskNo：选举的新的主任务的任务编号
     * taskName： 新的任务名称
     * internalTestStart:  开始Sit时间
     * internalTestEnd:   开始Uat时间
     * requireRemark：需求备注
     * 根据taskNoOnly 判断是否只是删除子任务
     * 根据jobNo判断主任务。。。
     * 再根据taskNo任务编号判断对应 挂载任务是否存在
     * ①jobNo不为空，taskNo传递数据为空； 直接删除工单即可
     * ②jobNo不为空，taskNo不为空
     * （1）判断对应工单下的taskNo是否存在
     * 不存在：更新工单信息为fdev传递信息
     * 存在：更新工单信息为fdev传递信息 并删除 挂载的任务
     * ③taskNoOnly 不为空：删除子任务即
     */
    @Override
    public Integer deleteOrder(Map map) throws Exception {
        if (!Util.isNullOrEmpty(map.get(Dict.FDEV_IMPLEMENT_UNIT_NO))) {
            List<String> unitNoList = (List<String>) map.get(Dict.FDEV_IMPLEMENT_UNIT_NO);
            for (int i = 0; i < unitNoList.size(); i++) {
                unitNoList.set(i, demandService.getUnitNo(unitNoList.get(i)));
            }
            String unitNos = "'" + String.join("','", unitNoList) + "'";
            int count = workOrderMapper.abandonNewOrder(unitNos);
            return count;
        }
        String taskNoOnly = (String) map.get(Dict.TASKNOONLY);
        Integer resultNum = 0;
        if (taskNoOnly == null || "".equals(taskNoOnly)) {  // 为空 则说明 不删除子任务，需要删除主任务
            String workOrderNo = (String) map.getOrDefault(Dict.JOBNO, Constants.FAIL_GET);
            String taskNo = (String) map.getOrDefault(Dict.TASKNO, Constants.FAIL_GET);
            if (Constants.FAIL_GET.equals(taskNo) || "".equals(taskNo)) {    //主任务 下没有 子任务了。直接删除主任务 即删除工单。
                if (workOrderMapper.queryOrderExist(workOrderNo) == 1) {  //存在该工单
                    resultNum = workOrderMapper.dropWorkOrderByWorkNo(workOrderNo);
                }
            } else { //主任务下有 子任务， 需要判断 新的主任务是否在该工单下
                if (taskListMapper.queryTaskExist(taskNo, workOrderNo) == 1) {   //如果 新的主任是 作为任务挂载工单下，那么需要在任务表中删除该任务
                    taskListMapper.dropTaskListByTaskNo(taskNo);
                }
                //直接 根据 推送上的主任务，更新测试管理平台的工单信息即可
                resultNum = workOrderMapper.updateWorkOrder(makeWorkOrder(map, workOrderNo));
            }
        } else { // 只删除 子任务
            resultNum = taskListMapper.dropTaskListByTaskNo(taskNoOnly);
            //如果这个任务有关联的安全工单，将工单置为废弃
            List<WorkOrder> workOrders = workOrderMapper.queryOrderByTaskNo(taskNoOnly, Constants.ORDERTYPE_SECURITY);
            if(!CommonUtils.isNullOrEmpty(workOrders)) {
                workOrderMapper.dropWorkOrderByWorkNo(workOrders.get(0).getWorkOrderNo());
            }
        }
        return resultNum;
    }

    /**
     * 修改工单
     * 首先判断 该工单号的工单是否存在
     */
    @Override
    public Integer updateWorkOrder(Map map) throws Exception {
        WorkOrder workOrder = orderUtil.makeWorkOrder(map);
        String unitNo = (String) map.getOrDefault(Dict.UNIT, Constants.FAIL_GET);
        unitNo = demandService.getUnitNo(unitNo);
        workOrder.setUnit(unitNo);
        WorkOrder oldWorkOrder = workOrderMapper.queryWorkOrderByNo(workOrder.getWorkOrderNo());
        if (Util.isNullOrEmpty(oldWorkOrder)) {
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"工单编号异常：" + workOrder.getWorkOrderNo()});
        }
        if (workOrder.getStage().equals("0")) {
            return workOrderMapper.fdevRollBackOrder(workOrder);
        }
        return workOrderMapper.updateWorkOrder(workOrder);
    }

    /**
     * 根据fdev给定的参数数据，构建一个WorkOrder对象
     */
    private WorkOrder makeWorkOrder(Map map, String workOrderNo) {
        WorkOrder workOrder = new WorkOrder();
//根据 fdev 给定的数据更新 工单（主任务）
        if (workOrderNo != null)
            workOrder.setWorkOrderNo(workOrderNo);
        workOrder.setWorkOrderFlag(Constants.NUMBER_1);//fdev传递的
        workOrder.setUnit((String) map.getOrDefault(Dict.UNITNO, Constants.FAIL_GET));
        workOrder.setPlanSitDate((String) map.getOrDefault(Dict.INTERNALTESTSTART, Constants.FAIL_GET));
        workOrder.setPlanUatDate((String) map.getOrDefault(Dict.INTERNALTESTEND, Constants.FAIL_GET));
        workOrder.setPlanProDate((String) map.getOrDefault(Dict.EXPECTEDPRODUCTDATE, Constants.FAIL_GET));
        workOrder.setMainTaskName((String) map.getOrDefault(Dict.TASKNAME, Constants.FAIL_GET));
        workOrder.setMainTaskNo((String) map.getOrDefault(Dict.TASKNO, Constants.FAIL_GET));
        workOrder.setRemark((String) map.getOrDefault(Dict.REQUIREREMARK, Constants.FAIL_GET));
        workOrder.setCreateTime(new Date().getTime() / 1000);

        return workOrder;
    }

    /**
     * 废弃工单
     *
     * @param map
     * @return
     * @throws Exception
     */
    public Integer wasteWorkOrder(Map map) throws Exception {
        Integer row = workOrderMapper.wasteWorkOrder(String.valueOf(map.get(Dict.JOBNO)));
        return row;
    }

    /**
     * fdev任务修改实施单元编号
     */
    @Override
    @Transactional
    public Integer updateUnitNo(Map map) throws Exception {
        String taskNo = (String) map.get(Dict.TASKNO);
        String unitNo = (String) map.get(Dict.UNITNO);
        unitNo = demandService.getUnitNo(unitNo);
        //查询任务更换后的实施单元所对的工单
        List<WorkOrder> workOrderList = workOrderMapper.queryWorkOrderByUnit(unitNo, Constants.ORDERTYPE_FUNCTION);
        if (CommonUtils.isNullOrEmpty(workOrderList)) {
            logger.error("实施单元编号为{}的工单不存在", unitNo);
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"实施单元对应工单不存在"});
        }
        String workNo = workOrderList.get(0).getWorkOrderNo();
        String oldWorkNo = "";
        Integer result = null;
        //先判断修改的任务是否为存量数据
        String beforeWorkNo = workOrderMapper.queryWorkNoByTaskId(taskNo, Constants.ORDERTYPE_FUNCTION);
        if (!Util.isNullOrEmpty(beforeWorkNo)) {
            //修改原来工单的主任务编号
            WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(beforeWorkNo);
            //若老工单下有子任务，则选
            List<TaskList> oldTasks = taskListMapper.queryTaskByNo(beforeWorkNo);
            if (!Util.isNullOrEmpty(oldTasks)) {
                workOrderMapper.updateMainTaskNoByWorkNo(oldTasks.get(0).getTaskno(), beforeWorkNo);
                taskListMapper.dropTaskListByTaskNo(oldTasks.get(0).getTaskno());
            } else {
                workOrderMapper.dropWorkOrderByWorkNo(beforeWorkNo);
                //通知测试人员迁移计划和案例
                String unit = workOrderList.get(0).getUnit();
                notifyTester(workOrder, unit);
            }
            oldWorkNo = beforeWorkNo;
            //在任务列表新增一条任务记录
            TaskList taskList = new TaskList();
            taskList.setWorkno(workNo);
            taskList.setTaskname(workOrder.getMainTaskName());
            taskList.setTaskno(taskNo);
            taskList.setTaskunit(unitNo);
            taskList.setField1(workOrder.getRemark());
            result = taskListMapper.addTaskList(taskList);
        } else {
            //查询要修改的任务是否存在
            TaskList taskList = taskListMapper.queryTaskByTaskNo(taskNo);
            if (CommonUtils.isNullOrEmpty(taskList)) {
                logger.error("任务编号为{}不存在", taskNo);
                throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"任务不存在"});
            }
            oldWorkNo = taskList.getWorkno();
            //修改任务对应的工单号
            result = taskListMapper.updateWorkNoAndUnitNoByTaskNo(taskNo, workNo, unitNo);
        }
        //修改mantis表中的工单编号
        handleMantis(taskNo, workNo, unitNo);
        //修改msg_fdev表
        List<MessageFdev> messageFdevs = messageFdevMapper.queryByTaskNo(taskNo, Constants.ORDERTYPE_FUNCTION);
        if (!CommonUtils.isNullOrEmpty(messageFdevs)) {
            for (MessageFdev messageFdev : messageFdevs) {
                messageFdevMapper.updateWorkNoByMsgId(workNo, messageFdev.getMessageId());
            }
        }
        //修改FTMS_SUBMIT_SIT_RECORD表
        List<FdevSitMsg> fdevSitMsgs = fdevSitMsgMapper.queryTaskSitMsg(taskNo, Constants.ORDERTYPE_FUNCTION);
        if (!CommonUtils.isNullOrEmpty(fdevSitMsgs)) {
            for (FdevSitMsg fdevSitMsg : fdevSitMsgs) {
                fdevSitMsgMapper.updateWorkNoById(workNo, fdevSitMsg.getId());
            }
        }
        //修改FTMS_ROLLBACK_INFO表
        List<RollbackInfo> rollbackInfos = rollbackInfoMapper.queryByTaskNo(taskNo, Constants.ORDERTYPE_FUNCTION);
        if (!CommonUtils.isNullOrEmpty(rollbackInfos)) {
            for (RollbackInfo rollbackInfo : rollbackInfos) {
                rollbackInfoMapper.updateWorkNoById(workNo, rollbackInfo.getId());
            }
        }
        //查询两个工单下的所有任务是否都已经提测，更新工单sit标识
        if (taskListService.isAllTasksInSitByWorkNo(workNo)) {
            workOrderMapper.updateSitFlagUpByWorkNo(workNo);
        } else {
            workOrderMapper.updateSitFlagDownByWorkNo(workNo);
        }
        if (taskListService.isAllTasksInSitByWorkNo(oldWorkNo)) {
            workOrderMapper.updateSitFlagUpByWorkNo(oldWorkNo);
        } else {
            workOrderMapper.updateSitFlagDownByWorkNo(oldWorkNo);
        }
        //判断状态工单
        WorkOrder oldOrder = workOrderMapper.queryWorkOrderByNo(oldWorkNo);
        if(Constants.ORDERTYPE_FUNCTION.equals(oldOrder.getOrderType())) {
            WorkOrder into = workOrderList.get(0);
            if (!"3".equals(oldOrder.getStage()) && !"9".equals(oldOrder.getStage())) {
                workOrderMapper.updateStage("1", into.getWorkOrderNo());
            }
            if ("6".equals(into.getStage()) || "10".equals(into.getStage())) {
                if (!"3".equals(oldOrder.getStage()) && !"9".equals(oldOrder.getStage())) {
                    workOrderMapper.resetApproval(into.getWorkOrderNo());
                }
            }
        }
        //根据任务查询安全工单，更新工单信息中的实施单元、需求编号等字段
        List<WorkOrder> workOrders = workOrderMapper.queryOrderByTaskNo(taskNo, Constants.ORDERTYPE_SECURITY);
        if(!CommonUtils.isNullOrEmpty(workOrders)) {
            Map unitResult = demandService.queryByFdevNoAndDemandId(unitNo);
            Map unitInfo = (Map) unitResult.get(Dict.IMPLEMENT_UNIT_INFO);
            String rqrmntNo = String.valueOf(((Map) unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NO));
            String rqrmntName = String.valueOf(((Map) unitResult.get(Dict.DEMAND_BASEINFO)).get(Dict.OA_CONTACT_NAME));
            WorkOrder workOrder = workOrders.get(0);
            workOrder.setUnit(unitNo);
            workOrder.setDemandNo(rqrmntNo);
            workOrder.setDemandName(rqrmntName);
            String fdevGroupId = (String) unitInfo.get(Dict.GROUP);
            workOrder.setFdevGroupId(fdevGroupId);//保存实施单元组id
            workOrderMapper.updateWorkOrder(workOrder);
            //修改提测信息中的需求编号和需求名称
            fdevSitMsgMapper.updateByWorkNo(workOrder.getWorkOrderNo(), rqrmntNo+"/"+rqrmntName);
        }
        return result;
    }

    private void notifyTester(WorkOrder workOrder, String unit) {
        Set<String> ftms = new HashSet<>();
        ftms.add(workOrder.getWorkManager());
        if (!Util.isNullOrEmpty(workOrder.getGroupLeader())) {
            ftms.addAll(Arrays.asList(workOrder.getGroupLeader().split(",")));
        }
        if (!Util.isNullOrEmpty(workOrder.getTesters())) {
            ftms.addAll(Arrays.asList(workOrder.getTesters().split(",")));
        }
        ftms.remove("");
        Map messageMap = new HashMap();
        messageMap.put("content", "工单【" + workOrder.getMainTaskName() + "】因fdev任务迁移实施单元而废弃，" +
                "请前往废弃工单手动迁移计划和案例至新工单【" + unit + "】");
        messageMap.put("target", new ArrayList<>(ftms));
        messageMap.put("type", "工单任务转移");
        try {
            iNotifyApi.sendUserNotify(messageMap);
        } catch (Exception e) {
            logger.error("fail to notify testers");
        }
    }

    /**
     * 修改mantis表中的记录
     */
    @Override
    public void handleMantis(String taskNo, String workNo, String untiNo) {
        //根据任务编号查询缺陷
        List<Integer> mantisIds = queryMantisIdByTaskNo(taskNo);
        if (!CommonUtils.isNullOrEmpty(mantisIds)) {
            //遍历缺陷修改工单号
            if (!CommonUtils.isNullOrEmpty(mantisIds)) {
                for (Integer id : mantisIds) {
                    updateMantis(workNo, id, untiNo);
                }
            }
        }
    }

    /**
     * 根据任务编号查询缺陷
     */
    private Map queryMantisByTaskNo(String taskNo) {
        Map sendDate = new HashMap();
        sendDate.put(Dict.CURRENTPAGE, "1");
        sendDate.put(Dict.PAGESIZE, String.valueOf(Integer.MAX_VALUE));
        sendDate.put(Dict.TASK_NO, taskNo);
        sendDate.put(Dict.REST_CODE, "mantis.query");
        Map<String, Object> result = new HashMap<>();
        try {
            result = (Map<String, Object>) restTransport.submit(sendDate);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.MANTIS_ERROR);
        }
        return result;
    }

    /**
     * 修改mantis表中的缺陷
     */
    @Override
    public void handleIssues(List<String> taskIds,String oldWorkNo,String newWorkNo,String untiNo){
        Map sendDate = new HashMap();
        sendDate.put(Dict.CURRENTPAGE, "1");
        sendDate.put(Dict.PAGESIZE, String.valueOf(Integer.MAX_VALUE));
        sendDate.put(Dict.WORKNO, oldWorkNo);
        sendDate.put(Dict.REST_CODE, "mantis.query");
        Map<String, Object> result = new HashMap<>();
        try {
            result = (Map<String, Object>) restTransport.submit(sendDate);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.MANTIS_ERROR);
        }
        if(!Util.isNullOrEmpty(result)){
            List<Map> issues = (List<Map>) result.get(Dict.ISSUES);
            if(!Util.isNullOrEmpty(issues)){
                for (Map issue : issues) {
                    String taskId = String.valueOf(issue.get(Dict.TASK_ID));
                    if(taskIds.contains(taskId)) {
                        Integer id = (Integer) issue.get(Dict.ID);
                        updateMantis(newWorkNo, id, untiNo);
                    }
                }
            }
        }
    }

    /**
     * 修改mantis表中单个缺陷
     */
    private void updateMantis(String workNo, Integer id, String untiNo) {
        if (Util.isNullOrEmpty(workNo) && Util.isNullOrEmpty(untiNo)) {
            return;
        }
        Map sendMap = new HashMap();
        if (!Util.isNullOrEmpty(workNo)) {
            sendMap.put(Dict.WORKNO, workNo);
        }
        if (!Util.isNullOrEmpty(untiNo)) {
            sendMap.put(Dict.UNITNO, untiNo);
        }
        sendMap.put(Dict.ID, id);
        sendMap.put(Dict.MANTIS_TOKEN, mantisAdminToken);
        sendMap.put(Dict.REST_CODE, "mantis.update");
        List<Map<String, String>> resultList = null;
        try {
            restTransport.submitSourceBack(sendMap);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_UPDATE_ERROR, new String[]{"mantis平台修改数据失败！"});
        }
    }

    /**
     * 给玉衡测试人员发送通知
     *
     * @param notifyFlag
     * @param workOrder
     */
    public void notifyFtmsUser(WorkOrder workOrder, String taskName) {
        List<String> target = new ArrayList<>();
        target.add(workOrder.getWorkManager());
        if (!Util.isNullOrEmpty(workOrder.getGroupLeader())) {
            target.addAll(Arrays.asList(workOrder.getGroupLeader().split(",")));
        }
        if (!Util.isNullOrEmpty(workOrder.getTesters())) {
            target.addAll(Arrays.asList(workOrder.getTesters().split(",")));
        }
        target = target.stream().distinct().collect(Collectors.toList());
        target.remove(null);
        String content = "工单【" + workOrder.getMainTaskName() + "】新增fdev任务【" + taskName + "】，请关注工单状态";
        Map messageMap = new HashMap();
        messageMap.put("content", content);
        messageMap.put("type", "工单状态重置");
        messageMap.put("target", target);
        try {
            iNotifyApi.sendUserNotify(messageMap);
        } catch (Exception e) {
            logger.error("fail to nofity tester" + e);
        }
    }

    /**
     * 查询任务相关mantis的id
     * @param taskNo
     * @return
     */
    public List<Integer> queryMantisIdByTaskNo(String taskNo) {
        Map param = new HashMap();
        param.put(Dict.TASKNO, taskNo);
        param.put(Dict.REST_CODE, "queryMantisIdByTaskNo");
        List<Integer> result = new ArrayList<>();
        try {
            result = (List<Integer>) restTransport.submit(param);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.MANTIS_ERROR);
        }
        return result;
    }

}
