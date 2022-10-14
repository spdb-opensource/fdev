package com.gotest.service.serviceImpl;

import com.alibaba.fastjson.JSON;
import com.gotest.dao.*;
import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.domain.PlanList;
import com.gotest.domain.TaskList;
import com.gotest.domain.Testcase;
import com.gotest.domain.WorkOrder;
import com.gotest.service.NewFdevService;
import com.gotest.service.TaskListService;
import com.gotest.utils.CommonUtils;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@Service
@RefreshScope
public class NewFdevServiceImpl implements NewFdevService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private NewFdevMapper newFdevMapper;

    @Autowired
    private PlanListMapper planListMapper;

    @Value("${unit.group.ids}")
    private String unitGroupIdsStr;
    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private TaskListMapper taskListMapper;
    @Autowired
    private RequireServiceImpl requireService;
    @Autowired
    private MessageFdevMapper messageFdevMapper;
    @Autowired
    private FdevSitMsgMapper fdevSitMsgMapper;
    @Autowired
    private RollbackInfoMapper rollbackInfoMapper;
    @Autowired
    private TaskListService taskListService;

    /**
     * 新fdev创建工单
     *
     * @param unitId
     * @param groupId
     * @param unitName
     * @param sitStart
     * @param sitEnd
     * @param proDate
     * @param remark
     * @return
     * @throws Exception
     */
    @Override
    public String newCreateWorkOrder(String unitId, String groupId, String unitName, String sitStart, String sitEnd,
                                     String proDate, String remark, String id, String demandId, String oaRealNo, String oaContactName) throws Exception {
        //检查同实施单元id的工单是否已经存在，如果存在不允许新增
        String existOrderNo = this.checkOrderExist(unitId);
        if (!CommonUtils.isNullOrEmpty(existOrderNo))
            return "创建工单失败" + unitId + "已存在工单" + existOrderNo;
        WorkOrder workOrder = new WorkOrder();//创建对象
        String dateField3 = new SimpleDateFormat("yyyyMMdd").format(new Date());//生成流水工单号
        //生成工单号
        this.createOrderNo(dateField3, workOrder);
        //检查用户是否为需求参与人
        if (!this.checkDemandUser(demandId, id)) {
            logger.error("authorize fail");
            return "创建工单失败" + unitId + "检查用户创建工单权限异常";
        }
        workOrder.setWorkOrderFlag(Constants.NUMBER_1);//区分fdev工单和自建工单
        workOrder.setMainTaskName(unitName);//工单名取需求名
        workOrder.setCreateTime(new Date().getTime() / 1000);//创建时间
        workOrder.setField3(dateField3);//当天日期
        workOrder.setStage(Constants.NUMBER_0);//默认状态0：未分配
        workOrder.setSitFlag(Constants.NUMBER_0);//sit_flag默认为0
        workOrder.setDemandNo(oaRealNo);
        workOrder.setDemandName(oaContactName);
        workOrder.setFdevGroupId(groupId);//工单牵头小组
        workOrder.setDemandId(demandId);//需求id
        workOrder.setFdevNew(Constants.NUMBER_1);//1代表重构后的fdev
        workOrder.setPlanProDate(proDate);//计划投产时间
        workOrder.setPlanUatDate(sitEnd);//计划uat时间
        workOrder.setPlanSitDate(sitStart);//计划sit时间
        workOrder.setUnit(unitId);//实施单元id
        //自动分配工单
        this.autoAllocate(groupId, workOrder);
        if (workOrderMapper.addWorkOrder(workOrder) == 1) {
            return workOrder.getWorkOrderNo().toString();
        } else {
            return "创建工单失败" + unitId + "新增工单失败";
        }
    }

    /**
     * 检查操作人是否有权
     *
     * @param demandId
     * @param userNameEn
     */
    private boolean checkDemandUser(String demandId, String id) throws Exception {
        Map send = new HashMap();
        send.put("userId", id);
        send.put("demandId", demandId);
        send.put(Dict.REST_CODE, "isDemandMember");
        try {
            Map result = (Map) restTransport.submitSourceBack(send);
            if (Util.isNullOrEmpty(result)) {
                return false;
            }
            return (boolean) result.get("isDemandMember");
        } catch (Exception e) {
            logger.error("fail to check demand user");
            return false;
        }
    }

    /**
     * 重构fdev新增任务
     *
     * @param taskNo
     * @param unitId
     * @param taskName
     * @return
     * @throws Exception
     */
    @Override
    public String newCreateTask(String taskNo, String unitId, String taskName) throws Exception {
        //判断实施单元是否创建过工单
        List<WorkOrder> orders = workOrderMapper.queryWorkOrderByUnit(unitId, Constants.ORDERTYPE_FUNCTION);
        if (CommonUtils.isNullOrEmpty(orders)) {
            logger.error("实施单元id为{}的工单不存在", unitId);
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"该实施单元未同步工单"});
        }
        //判断任务是否在未废弃的工单里添加过
        TaskList taskListExist = taskListMapper.queryTaskByTaskNo(taskNo);
        if (!CommonUtils.isNullOrEmpty(taskListExist)) {
            logger.error("任务编号为{}的任务已经添加过工单号为{}的工单", taskNo, taskListExist.getWorkno());
            throw new FtmsException(ErrorConstants.REPET_INSERT_REEOR, new String[]{"该任务已同步到玉衡"});
        }
        WorkOrder workOrder = orders.get(0);
        TaskList taskList;
        //获取特殊处理的组id集合
        List<String> specialGroupIds = Arrays.asList(unitGroupIdsStr.split(","));
        if (specialGroupIds.contains(workOrder.getFdevGroupId())) {
            //如果实施单元所在工单的组为特殊组，那么单独为任务创建工单
            WorkOrder order = this.buildSpecialOrder(workOrder, taskName);
            //将任务添加进工单任务列表
            taskList = buildTaskList(taskNo, taskName, order.getWorkOrderNo(), unitId);
        } else {
            //查询工单的状态是否已经提测，提测了改为未提测
            String sitFlag = workOrder.getSitFlag();
            if (Constants.NUMBER_1.equals(sitFlag)) {
                workOrderMapper.updateSitFlagDownByWorkNo(workOrder.getWorkOrderNo());
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
            taskList = buildTaskList(taskNo, taskName, workOrder.getWorkOrderNo(), unitId);
        }
        if (taskListMapper.addTaskList(taskList) == 1) {
            //添加任务集并通知测试人员
            try {
                requireService.notifyFtmsUser(workOrder, taskName);
            } catch (Exception e) {
                logger.error("fail to notify ftms");
            }
            return taskList.getWorkno();
        } else {
            logger.error("fail to create taskList");
            return "Create taskList error";
        }
    }

    /**
     * 为创建了工单的实施单元实体传送工单号属性
     *
     * @param update
     * @throws Exception
     */
    @Override
    public void updateOrderNum(List<Map> update) throws Exception {
        Map send = new HashMap();
        send.put("workOrders", update);
        send.put(Dict.REST_CODE, "updateWorkOrderIds");
        try {
            restTransport.submitSourceBack(send);
        } catch (Exception e) {
            logger.error("fail to send orderNo");
        }
    }

    /**
     * 实施单元删除关联玉衡工单废弃
     *
     * @param unitNo
     * @return
     */
    @Override
    public int deleteWorkOrderByUnitNo(List<String> unitNo) {
        String unitNos = "'" + String.join("','", unitNo) + "'";
        //查询实施单元下所有的工单
        List<WorkOrder> workOrders = newFdevMapper.queryWorkNoByUnitNos(unitNos);
        try {
            int count = newFdevMapper.deleteWorkOrderByUnitNo(unitNos);
            Map sendMap = new HashMap();
            List<String> workNo = workOrders.stream().map(workOrder -> workOrder.getWorkOrderNo()).collect(Collectors.toList());
            sendMap.put(Dict.REST_CODE, "updateMantisStatus");
            sendMap.put("workNos", workNo);
            restTransport.submit(sendMap);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 任务删除关联玉衡工单任务集删除以及缺陷关闭
     *
     * @param tasks
     * @return
     */
    @Override
    public int deleteTask(List<String> tasks) throws Exception {
        //删除任务集
        String taskIds = "'" + String.join("','", tasks) + "'";
        //获取删除任务所在工单号
        List<String> workNos = newFdevMapper.queryWorkByTaskIds(taskIds).stream().map(TaskList::getWorkno).collect(Collectors.toList());
        int count = 0;
        try {
            count = newFdevMapper.deleteTask(taskIds);
        } catch (Exception e) {
            logger.error("delete task error");
            return 0;
        }
        //关闭任务相关缺陷
        Map sendMap = new HashMap();
        sendMap.put(Dict.REST_CODE, "updateMantisByTaskIds");
        sendMap.put(Dict.TASKIDS, tasks);
        try {
            restTransport.submit(sendMap);
        } catch (Exception e) {
            logger.error("close mantis error");
        }
        //如果工单属于效能并且没有剩余任务，则废弃工单
        List<String> specialGroupIds = Arrays.asList(unitGroupIdsStr.split(","));
        for (String workNo : workNos) {
            WorkOrder workOrder = workOrderMapper.queryWorkOrderByNo(workNo);
            if (!Util.isNullOrEmpty(workOrder) && specialGroupIds.contains(workOrder.getFdevGroupId())) {
                //效能的工单
                List<TaskList> taskLists = taskListMapper.queryTaskByNo(workNo);
                if (Util.isNullOrEmpty(taskLists)) {
                    //废弃
                    workOrderMapper.dropWorkOrderByWorkNo(workNo);
                }
            }
        }
        return count;
    }

    /**
     * 任务变更实施单元
     *
     * @param taskNo
     * @param unitNo
     * @return
     */
    @Override
    public int updateTaskUnitNo(String taskNo, String taskName, String unitNo) throws Exception {
        Integer result = 0;
        //查询任务更换后的实施单元所对的工单
        List<WorkOrder> workOrderList = workOrderMapper.queryWorkOrderByUnit(unitNo, Constants.ORDERTYPE_FUNCTION);
        if (CommonUtils.isNullOrEmpty(workOrderList)) {
            //从工单任务集中删除任务
            this.deleteTask(Arrays.asList(new String[]{taskNo}));
            logger.error("实施单元id为{}的工单不存在", unitNo);
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"实施单元id对应工单不存在,请先创建工单"});
        }
        WorkOrder workOrder = workOrderList.get(0);
        //查询要修改的任务是否存在
        TaskList taskList = taskListMapper.queryTaskByTaskNo(taskNo);
        if (!CommonUtils.isNullOrEmpty(taskList)) {
            //如果任务列表存在该任务，修改该条记录的工单号和实施单元id
            result = taskListMapper.updateWorkNoAndUnitNoByTaskNo(taskNo, workOrder.getWorkOrderNo(), unitNo);
        } else {
            //如果任务列表不存在该任务记录，则新增一条记录
            TaskList taskListNew = new TaskList();
            taskListNew.setWorkno(workOrder.getWorkOrderNo());
            taskListNew.setTaskname(taskName);
            taskListNew.setTaskno(taskNo);
            taskListNew.setTaskunit(unitNo);
            taskListNew.setField1(workOrder.getRemark());
            result = taskListMapper.addTaskList(taskListNew);
        }

        //修改mantis表中的工单编号
        requireService.handleMantis(taskNo, workOrder.getWorkOrderNo(), unitNo);
        //修改提测消息表
        messageFdevMapper.updateWorkNoByTaskNos(Arrays.asList(taskNo), workOrder.getWorkOrderNo());
        //修改提测记录表
        fdevSitMsgMapper.updateWorkNoByTaskNos(Arrays.asList(taskNo), workOrder.getWorkOrderNo());
        //修改打回表
        rollbackInfoMapper.updateRollBackInfoByTaskNos(Arrays.asList(taskNo), workOrder.getWorkOrderNo(),
                workOrder.getMainTaskName(), workOrder.getFdevGroupId());
        //查询新老工单下的所有任务是否都已经提测，更新工单sit标识
        if (taskListService.isAllTasksInSitByWorkNo(workOrder.getWorkOrderNo())) {
            workOrderMapper.updateSitFlagUpByWorkNo(workOrder.getWorkOrderNo());
        } else {
            workOrderMapper.updateSitFlagDownByWorkNo(workOrder.getWorkOrderNo());
        }
        WorkOrder oldOrder = new WorkOrder();
        if (!CommonUtils.isNullOrEmpty(taskList)) {
            if (taskListService.isAllTasksInSitByWorkNo(taskList.getWorkno())) {
                workOrderMapper.updateSitFlagUpByWorkNo(taskList.getWorkno());
            } else {
                workOrderMapper.updateSitFlagDownByWorkNo(taskList.getWorkno());
            }
            oldOrder = workOrderMapper.queryWorkOrderByNo(taskList.getWorkno());
        }
        //判断状态工单
        if ("6".equals(workOrder.getStage()) || "10".equals(workOrder.getStage())) {
            if (!CommonUtils.isNullOrEmpty(oldOrder) && ("3".equals(oldOrder.getStage()) || "9".equals(oldOrder.getStage())))
                //如果原工单存在且状态已经是uat或者分包完成，代表这个任务也已经测完，直接返回
                return result;
            //如果迁入的工单为含风险，且原工单不存在，或者状态不是uat或分包完成，代表任务没完，需重新审核
            workOrderMapper.resetApproval(workOrder.getWorkOrderNo());
        }
        //如果原工单不存在，或者存在但是状态不是uat或分包完成，新工单要刷新为开发中
        workOrderMapper.updateStage("1", workOrder.getWorkOrderNo());
        return result;
    }

    /**
     * 查询任务详情
     *
     * @param taskNo
     * @return
     */
    @Override
    public Map<String, Object> queryFdevTaskDetail(String taskNo) {
        //老玉衡重构
        Map<String, Object> workOrderMap = newFdevMapper.queryWorkOrderByMainTaskNo(taskNo);
        Map<String, Object> task = new HashMap<>();
        if (!Util.isNullOrEmpty(workOrderMap)) {
            task = queryTaskDetail(taskNo);
        } else {
            //根据任务查询工单，判断该任务是否是新fdev产生的
            Map<String, Object> map = newFdevMapper.queryWorkinfoByTaskNo(taskNo);
            if (!Util.isNullOrEmpty(map)) {
                String fdev_new = (String) map.get("fdev_new");
                //判断是否是新fdev
                if ("1".equals(fdev_new)) {
                    //新fdev任务
                    task = getNewTaskById(taskNo);
                } else {
                    //老任务
                    task = queryTaskDetail(taskNo);
                }
            }
        }
        return task;
    }

    /**
     * 更新工单名
     *
     * @param workNo
     * @param name
     * @return
     * @throws Exception
     */
    @Override
    public int updateOrderName(String workNo, String name) throws Exception {
        return workOrderMapper.updateOrderName(workNo, name);
    }

    /**
     * 同步实时单元更新工单名
     *
     * @param unitNo
     * @param name
     * @return
     * @throws Exception
     */
    @Override
    public int synIpmpUpdateOrderName(String unitNo, String name) throws Exception {
        //查询该实施单元是否有工单
        List<WorkOrder> workOrders = newFdevMapper.queryWorkNoBynuitNo(unitNo);
        int count = 0;
        if (!Util.isNullOrEmpty(workOrders)) {
            count = newFdevMapper.synIpmpUpdateOrderName(unitNo, name);
        }
        return count;
    }

    /**
     * 同步需求更新工单信息
     *
     * @param demandId
     * @param name
     * @return
     * @throws Exception
     */
    @Override
    public int synDemandUpdateOrderInfo(String demandId, String name) throws Exception {
        return newFdevMapper.synDemandUpdateOrderInfo(demandId, name);
    }

    /**
     * 给工单添加任务
     *
     * @param taskNo
     * @param taskName
     * @param workOrderNo
     * @param unit
     * @return
     */
    private TaskList buildTaskList(String taskNo, String taskName, String workOrderNo, String unit) {
        TaskList taskList = new TaskList();
        taskList.setTaskno(taskNo);
        taskList.setTaskname(taskName);
        taskList.setWorkno(workOrderNo);
        taskList.setTaskunit(unit);
        return taskList;
    }

    /**
     * 根据已有工单创建新工单
     *
     * @param order
     * @param taskName
     */
    private WorkOrder buildSpecialOrder(WorkOrder order, String taskName) {
        String dateField3 = new SimpleDateFormat("yyyyMMdd").format(new Date());
        order.setField3(dateField3);
        //生成工单号
        this.createOrderNo(dateField3, order);
        order.setTesters(null);
        order.setMainTaskName(taskName);
        order.setWorkOrderFlag(Constants.NUMBER_1);//fdev传递的
        order.setCreateTime(new Date().getTime() / 1000);
        order.setStage(Constants.NUMBER_0);//默认状态：未分配
        order.setSitFlag(Constants.NUMBER_0);//sit_flag默认为0
        order.setFdevNew(Constants.NUMBER_1);//标记重构fdev
        //自动分配工单
        autoAllocate(order.getFdevGroupId(), order);
        if (workOrderMapper.addWorkOrder(order) == 1) {
            return order;
        } else {
            throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR, new String[]{"创建工单失败！"});
        }
    }

    /**
     * 根据日期生成工单号
     *
     * @param dateField3
     * @param order
     */
    private void createOrderNo(String date, WorkOrder order) {
        do {
            //获取当天生成的所有工单号，按大小倒序排列
            List<String> dates = workOrderMapper.queryGroupByDate(date);
            //当天已有工单数据，则加一生成新工单号;当天未有工单数据，生成当天第一条工单号
            String workNo = !dates.isEmpty() ? (Long.parseLong(dates.get(0)) + 1L) + "" : date + "0000";
            order.setWorkOrderNo(workNo);
        } while (workOrderMapper.queryOrderExist(order.getWorkOrderNo()) > 0);//防止高并发导致相同工单
    }

    /**
     * 自动分配工单
     *
     * @param fdevGroupId
     * @param order
     */
    private void autoAllocate(String fdevGroupId, WorkOrder order) {
        try {
            //自动分配工单
            if (!Util.isNullOrEmpty(fdevGroupId)) {
                Map<String, String> resultMap = groupMapper.queryAutoWorkOrder(fdevGroupId);
                if (!Util.isNullOrEmpty(resultMap)) {
                    order.setWorkManager(resultMap.get(Dict.WORKMANAGER));
                    order.setGroupLeader(resultMap.get(Dict.GROUPLEADER));
                } else {
                    List<Map> groups = userService.queryParentGroupById(fdevGroupId);
                    if (!Util.isNullOrEmpty(groups)) {
                        //获取其父组
                        Map group = groups.get(1);
                        Map<String, String> result = groupMapper.queryAutoWorkOrder((String) group.get(Dict.ID));
                        if (!Util.isNullOrEmpty(result)) {
                            order.setWorkManager(result.get(Dict.WORKMANAGER));
                            order.setGroupLeader(result.get(Dict.GROUPLEADER));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("fail to allocate group" + e.toString());
        }
    }

    /**
     * 根据需求id查询新需求信息
     *
     * @param demandId
     * @return
     * @throws Exception
     */
    public Map queryNewDemandInfoById(String demandId) throws Exception {
        try {
            Map send = new HashMap();
            send.put(Dict.DEMANDID, demandId);
            send.put(Dict.REST_CODE, "queryDemandInfoDetailNew");
            Map result = (Map) restTransport.submitSourceBack(send);
            if (Util.isNullOrEmpty(result)) {
                logger.error("fail to get new demand info");
                throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询新需求信息失败！"});
            }
            return result;
        } catch (Exception e) {
            logger.error("fail to get new demand info");
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询新需求信息失败！"});
        }
    }

    /**
     * 检查同需求id的工单是否已经存在，如果存在不允许新增
     *
     * @param demandId
     * @throws Exception
     */
    private String checkOrderExist(String unitId) throws Exception {
        List<WorkOrder> orders = newFdevMapper.checkOrderExist(unitId);
        if (!CommonUtils.isNullOrEmpty(orders)) {
            logger.error("order already exists");
            return orders.get(0).getWorkOrderNo();
        }
        return null;
    }

    private void checkExist(String demandId) throws Exception {

    }

    /**
     * 根据实施单元id列表批量查询测试计划
     *
     * @param oaRealNo
     * @return
     */
    @Override
    public Map<String, Object> queryWorkOrderByOaRealNo(List<String> unitNos, String index, String pageSize) throws Exception {
        //查询实施单元id下的所有工单
        Integer current_page = Integer.parseInt(index);
        Integer page_size = Integer.parseInt(pageSize);
        Integer start_page = page_size * (current_page - 1);
        Map<String, Object> mapAll = new HashMap<>();
        //循环便利实施单元id
        List listAll = new ArrayList();
        if (!CommonUtils.isNullOrEmpty(unitNos)) {
            for (String unitNo : unitNos) {
                List<WorkOrder> list = newFdevMapper.queryWorkOrderByOaRealNo(unitNo);
                if (!CommonUtils.isNullOrEmpty(list)) {
                    for (WorkOrder workOrder : list) {
                        String workOrderNo = workOrder.getWorkOrderNo();
                        //根据工单查询计划
                        List<PlanList> allPlanLists = planListMapper.queryPlanByworkNo(workOrderNo, start_page, page_size);
                        //查询计划
                        if (!CommonUtils.isNullOrEmpty(allPlanLists)) {
                            for (PlanList planList : allPlanLists) {
                                Map map1 = JSON.parseObject(JSON.toJSONString(planList), Map.class);
                                Integer planId = planList.getPlanId();
                                //根据计划id查询案例
                                String testers = workOrder.getTesters();
                                map1.put("testers", testers);
                                //执行用例情况
                                Map map = newFdevMapper.queryAllStatus(planId);
                                //int allCase =(int) map.get("allCase"); //案例总数
                                Long allCase = Long.valueOf(map.get("allCase").toString());
                                Long exeNum = Long.valueOf(map.get("exeNum").toString());//执行总数
                                Long sumSucc = Long.valueOf(map.get("sumSucc").toString());//通过的数
                                Long sumFail = Long.valueOf(map.get("sumFail").toString());//失败用例数量
                                Long sumBlock = Long.valueOf(map.get("sumBlock").toString());//阻塞案例数量
                                Long sumUnExe = Long.valueOf(map.get("sumUnExe").toString());//未执行的案例数量
                                planList.setAllCase(allCase);
                                planList.setExeNum(exeNum);
                                planList.setSumBlock(sumBlock);
                                planList.setSumFail(sumFail);
                                planList.setSumSucc(sumSucc);
                                planList.setSumUnExe(sumUnExe);
                                planList.setUnitNo(unitNo);
                            }

                        }
                        listAll.addAll(allPlanLists);
                    }
                }
            }
        }
        mapAll.put("listAll", listAll);
        return mapAll;
    }


//        Map mapList=new HashMap();
//        mapList.put("list",list);
//        //获取所有的工单id
//        List<String> workOrderNos = list.stream().map(e -> e.getWorkOrderNo()).collect(Collectors.toList());
//        //根据工单id查询测试计划
//        for (WorkOrder workOrder : list) {
//            //根据工单编号查询计划
//            String unit = workOrder.getUnit();
//            List<PlanList>  allPlanLists = planListMapper.queryPlanByworkNo(workOrder.getWorkOrderNo(),start_page,page_size);
//          List<Map> list1=new ArrayList<>();
//            for (PlanList planList : allPlanLists) {
//                Map map1 = JSON.parseObject(JSON.toJSONString(planList), Map.class);
//                Integer planId = planList.getPlanId();
//                //根据计划id查询案例
//                String testers = workOrder.getTesters();
//                map1.put("testers",testers);
//                //执行用例情况
//                Map  map= newFdevMapper.queryAllStatus(planId);
//                //int allCase =(int) map.get("allCase"); //案例总数
//                Long allCase = Long.valueOf(map.get("allCase").toString());
//                Long exeNum = Long.valueOf(map.get("exeNum").toString());//执行总数
//                Long sumSucc = Long.valueOf(map.get("sumSucc").toString());//通过的数
//                Long sumFail = Long.valueOf(map.get("sumFail").toString());//失败用例数量
//                Long sumBlock = Long.valueOf(map.get("sumBlock").toString());//阻塞案例数量
//                Long sumUnExe = Long.valueOf(map.get("sumUnExe").toString());//未执行的案例数量
//                //执行用例百分比
//                String exeNumPercent="";
//                String sumUnExePercent="";
//                String sumSuccPercent = "" ;
//                String sumBlockPercent = "" ;
//                String sumFailPercent = "" ;
//                if (allCase!=0) {
//                    double exeNumPer = exeNum / allCase;
//                    double sumUnExePer =sumUnExe / allCase;
//                    double sumSuccPer = sumSucc / allCase ;
//                    double sumBlockPer = sumBlock / allCase ;
//                    double sumFailPer = sumFail /allCase ;
//                    //创建一个数值格式化对象
//                    NumberFormat format = NumberFormat.getPercentInstance();
//                    //设置保留两位小数
//                    format.setMaximumFractionDigits(2);
//                    exeNumPercent = format.format(exeNumPer);
//                     sumUnExePercent = format.format(sumUnExePer);
//                    sumSuccPercent=  format.format(sumSuccPer);
//                    sumBlockPercent=  format.format(sumBlockPer);
//                    sumFailPercent =  format.format(sumFailPer);
//                }
//                map1.put("exeNumPercent", exeNumPercent); //执行用例百分比
//                map1.put("sumUnExePercent",sumUnExePercent);//未执行用例百分比
//                map1.put("sumSuccPercent",sumSuccPercent); //成功的百分比
//                map1.put("sumBlockPercent",sumBlockPercent); //阻塞用例百分比
//                map1.put("sumFailPercent",sumFailPercent) ;     //失败用例百分比
//                map1.putAll(map);
//                list1.add(map1);
//            }
//            workOrder.setMapList(list1);
//        }
//        return mapList;
//    }

    /**
     * 根据测试计划id查询测试用例
     *
     * @param planId
     * @return
     */
    @Override
    public List<Map> queryTestcaseByPlanId(Integer planId, Integer current_page, Integer page_size) {
        //起始索引
        Integer start_page = page_size * (current_page - 1);
        List<Map> mapList = planListMapper.queryTestcaseByPlanId(planId, start_page, page_size);
        return mapList;
    }

    /**
     * 根据案例编号查询案例详情
     *
     * @param testcaseNo
     * @return
     */
    @Override
    public Map querytestCaseByTestCaseNo(String testcaseNo) {
        Map map = newFdevMapper.querytestCaseByTestCaseNo(testcaseNo);
        return map;
    }

    /**
     * 根据实施单元编号 查询
     *
     * @param unitNo
     * @param userNameEn
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public Map queryFuserMantisAll(String unitNo, String userNameEn, String currentPage, String pageSize) {

        return null;
    }

    @Override
    public Map<String, Object> queryPlanByUnitNo(List unitNos, Integer current_page, Integer page_size) {
        //查询实施单元id下的所有工单
        Integer start_page = page_size * (current_page - 1);
        Map<String, Object> mapAll = new HashMap<>();
        String unitNo = "";
        if (!Util.isNullOrEmpty(unitNos)) {
            unitNo = "'" + String.join("','", unitNos) + "'";
        }
        //实施单元id
        List<PlanList> planLists = planListMapper.queryPlanByUnitNo(unitNo, start_page, page_size);
        Integer planCount = planListMapper.queryPlanCountByUnitNo(unitNo);
        if (!CommonUtils.isNullOrEmpty(planLists)) {
            for (PlanList planList : planLists) {
                Integer planId = planList.getPlanId();
                Map map = newFdevMapper.queryAllStatus(planId);
                Long allCase = Long.valueOf(map.get("allCase").toString());
                Long exeNum = Long.valueOf(map.get("exeNum").toString());//执行总数
                Long sumSucc = Long.valueOf(map.get("sumSucc").toString());//通过的数
                Long sumFail = Long.valueOf(map.get("sumFail").toString());//失败用例数量
                Long sumBlock = Long.valueOf(map.get("sumBlock").toString());//阻塞案例数量
                Long sumUnExe = Long.valueOf(map.get("sumUnExe").toString());//未执行的案例数量
                planList.setAllCase(allCase);
                planList.setExeNum(exeNum);
                planList.setSumBlock(sumBlock);
                planList.setSumFail(sumFail);
                planList.setSumSucc(sumSucc);
                planList.setSumUnExe(sumUnExe);
                planList.setTestResult(map);
            }
        }
        Map map = new HashMap();
        map.put("total", planCount);
        map.put("plan", planLists);
        return map;
    }

    /**
     * 根据案例id查询案例详情
     *
     * @param testcaseNo
     * @return
     */
    @Override
    public Testcase queryDetailByTestcaseNo(String testcaseNo, String planId) {
        return newFdevMapper.queryDetailByTestcaseNo(testcaseNo, planId);
    }

    /**
     * 功能点不为空
     *
     * @param testcaseNo
     * @return
     */
    @Override
    public Testcase queryCaseByTestcaseNo(String testcaseNo, String planId) {
        return newFdevMapper.queryCaseByTestcaseNo(testcaseNo, planId);
    }

    /**
     * 根据实施单元id集合查询工单
     *
     * @param unitNos
     * @return
     */
    @Override
    public List<WorkOrder> queryWorkNoByUnitNos(List unitNos) {
        String unitNo = "";
        if (!Util.isNullOrEmpty(unitNos)) {
            unitNo = "'" + String.join("','", unitNos) + "'";
        }
        return newFdevMapper.queryWorkNoByUnitNos(unitNo);
    }

    /**
     * 根据计划id查询计划详情
     *
     * @param planId
     * @return
     */
    @Override
    public PlanList queryByPlanId(Integer planId) throws Exception {
        PlanList planList = planListMapper.selectByPlanId(planId);
        List<String> stringList = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(planList)) {
            String testers = planList.getTesters();
            String[] names = testers.split(",");
            List<String> nameList = Arrays.asList(names);
            if (!CommonUtils.isNullOrEmpty(nameList)) {
                for (String name : nameList) {
                    Map<String, Object> map = userService.queryUserCoreDataByNameEn(name);
                    if (!CommonUtils.isNullOrEmpty(map)) {
                        String testernameCn = (String) map.get(Dict.USER_NAME_CN);
                        stringList.add(testernameCn);
                    }
                }
            }
            planList.setTesternames(stringList);
            Map map = newFdevMapper.queryAllStatus(Integer.valueOf(planId));
            Long allCase = Long.valueOf(map.get("allCase").toString());
            Long exeNum = Long.valueOf(map.get("exeNum").toString());//执行总数
            Long sumSucc = Long.valueOf(map.get("sumSucc").toString());//通过的数
            Long sumFail = Long.valueOf(map.get("sumFail").toString());//失败用例数量
            Long sumBlock = Long.valueOf(map.get("sumBlock").toString());//阻塞案例数量
            Long sumUnExe = Long.valueOf(map.get("sumUnExe").toString());//未执行的案例数量
            planList.setAllCase(allCase);
            planList.setExeNum(exeNum);
            planList.setSumBlock(sumBlock);
            planList.setSumFail(sumFail);
            planList.setSumSucc(sumSucc);
            planList.setSumUnExe(sumUnExe);
        }

        return planList;
    }

    //查询老任务的详情
    private Map<String, Object> queryTaskDetail(String taskId) {
        Map<String, Object> map = new HashMap();
        try {
            map.put(Dict.ID, taskId);
            map.put(Dict.REST_CODE, "queryTaskDetail");
            map = (Map<String, Object>) restTransport.submitSourceBack(map);
            return map;
        } catch (Exception e) {
            logger.error("fail to query fdev subtask info");
        }
        return null;
    }

    //查询新fdev的任务详情
    private Map<String, Object> getNewTaskById(String id) {
        Map send = new HashMap();
        send.put(Dict.ID, id);
        send.put(Dict.REST_CODE, "getTaskById");
        try {
            return (Map<String, Object>) restTransport.submitSourceBack(send);
        } catch (Exception e) {
            logger.error("fail to get new Task info");
        }
        return null;
    }
}
