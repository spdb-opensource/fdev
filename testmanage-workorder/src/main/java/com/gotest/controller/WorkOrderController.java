package com.gotest.controller;


import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.domain.TaskList;
import com.gotest.domain.WorkOrder;
import com.gotest.domain.WorkOrderUser;
import com.gotest.service.*;
import com.gotest.utils.MyUtil;
import com.gotest.utils.OrderUtil;
import com.gotest.utils.RedisLock;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.util.Util;
import com.test.testmanagecommon.vaildate.RequestValidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@SuppressWarnings("all")
@RestController
@RequestMapping(value="/order")
public class WorkOrderController {

    @Autowired
    private RedisLock redisLock;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private WorkOrderService workOrderService;
    @Autowired
    private TaskListService taskListService;
    @Autowired
    private TestCaseService testCaseService;
    @Autowired
    private MantisService mantisService;
    @Autowired
    private INotifyApi iNotifyApi;
    @Autowired
    private ITaskApi iTaskApi;
    @Autowired
    private MyUtil myUtil;
    @Autowired
    private ExportExcelService exportExcelService;
    @Autowired
    private OrderUtil orderUtil;
    @Value("${unit.group.ids}")
    private String unitGroupIdsStr;

    /**
     * 获取管理员可分配的工单
     * */
    @RequestMapping("/queryAdminAssignOrder")
    public JsonResult queryAdminAssignOrder(@RequestBody Map map){
        List<Map> assignOrderList;
        try {
            assignOrderList = workOrderService.queryAdminAssignOrder(map);
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"获取管理员可分配的工单失败！"});
        }
        return JsonResultUtil.buildSuccess(assignOrderList);
    }


    /**
     * 抢工单
     * */
    @RequestMapping("/orderGrab")
    public JsonResult orderGrab(@RequestBody Map map){
        String orderId = (String) map.get(Dict.ORDERID);
        try {
            int flag = workOrderService.orderGrab(orderId);
            if(flag == 1){
                return JsonResultUtil.buildSuccess(orderId);
            }else {
                return JsonResultUtil.buildError(Constants.I_FAILED,"已被抢单！");
            }
        }catch (FtmsException e){
            logger.error(e.getMessage());
            return JsonResultUtil.buildError(ErrorConstants.GET_CURRENT_USER_INFO_ERROR,"登录失效，请重新登录！");
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"抢单操作异常！"});
        }finally {
            redisLock.unlock(Dict.ORDERID, RedisLock.FORDER_ORDER + orderId);
        }

    }

    /**
     * 获取组长可分配的工单
     * */
    @RequestMapping("/queryLeaderAssignOrder")
    public JsonResult queryLeaderAssignOrder(){
        List<WorkOrder> assignOrderList;
        try {
            assignOrderList = workOrderService.queryLeaderAssignOrder();
        }catch (FtmsException e){
            logger.error(e.getMessage());
            return JsonResultUtil.buildError(ErrorConstants.GET_CURRENT_USER_INFO_ERROR,"登录失效，请重新登录！");
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"获取组长可分配的工单失败！"});
        }
        return JsonResultUtil.buildSuccess(assignOrderList);
    }

    /**
     * 获取分配给当前用户的工单
     * */
    @RequestValidate(NotEmptyFields = {Dict.CURRENTPAGE, Dict.PAGESIZE})
    @RequestMapping("/queryUserAllOrder")
    public JsonResult queryUserAllOrder(@RequestBody Map map) throws Exception{
        return JsonResultUtil.buildSuccess(workOrderService.queryUserAllWorkOrder(map));
    }

    /**
     * 导出当前用户工单列表
     * */
    @PostMapping("/exportUserAllOrder")
    public void exportUserAllOrder(@RequestBody Map map, HttpServletResponse resp) throws Exception{
        List userOrderList = workOrderService.queryUserAllOrderWithoutPage(map);
        exportExcelService.exportUserAllOrder(userOrderList,resp);
    }

    /**
     * 新增工单
     * */
    @RequestMapping("/addOrder")
    public JsonResult addWorkOrder(@RequestBody WorkOrder workOrder) throws Exception{
        if(!workOrderService.verifyOrderName(workOrder.getMainTaskName())){
            throw new FtmsException(ErrorConstants.ORDER_NAME_REPORT_ERROR);
        };
        Integer count = workOrderService.addWorkOrder(workOrder);
        return JsonResultUtil.buildSuccess(null);
    }

    /**
     * 校验工单名称
     * */
    @RequestMapping("/verifyOrderName")
    public JsonResult verifyOrderName(@RequestBody Map<String,String> requestMap) throws Exception{
        String orderName = requestMap.get(Dict.MAINTASKNAME);
        return JsonResultUtil.buildSuccess(workOrderService.verifyOrderName(orderName)) ;
    }

    /**
     * 修改工单
     * */
    @RequestMapping("/updateOrder")
    public JsonResult updateWorkOrder(@RequestBody Map map) throws Exception{
        Integer count = workOrderService.updateWorkOrder(map);
        if ( count == 1 ){
            return JsonResultUtil.buildSuccess(count);
        }
        return JsonResultUtil.buildError(Constants.I_FAILED,"修改工单失败！");
    }


    /**
    * 根据工单号获取
    * 工单
    * */
    @RequestMapping("/queryOrderByOrderNo")
    public JsonResult queryOrderByOrderNo(@RequestBody Map map){
        WorkOrder workOrder;
        String workOrderNo = String.valueOf(map.get(Dict.WORKNO));
        try{
            workOrder = workOrderService.queryWorkOrderByNo(workOrderNo);
            if(Util.isNullOrEmpty(workOrder)){
                return JsonResultUtil.buildSuccess(null);
            }
            workOrder.setGroupLeader(myUtil.changeEnNameToCn(workOrder.getGroupLeader()));
            workOrder.setTesters(myUtil.changeEnNameToCn(workOrder.getTesters()));
            workOrder.setWorkManager(myUtil.changeEnNameToCn(workOrder.getWorkManager()));
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"根据工单号获取工单失败！"});
        }
        return JsonResultUtil.buildSuccess(workOrder);
    }

    /**
     * 根据工单号获取
     * 工单
     * */
    @RequestMapping("/queryOrderByNo")
    public JsonResult queryOrderByNo(@RequestBody String workOrderNo){
        WorkOrder workOrder;
        try{
            workOrder = workOrderService.queryWorkOrderByNo(workOrderNo);
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"根据工单号获取工单失败！"});
        }
        return JsonResultUtil.buildSuccess(workOrder);
    }

    /**
     * 根据工单号查询工单基本信息
     * @param param
     * @return
     */
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
    @RequestMapping(value = "/queryOrderInfoByNo", method = RequestMethod.POST)
    public JsonResult queryOrderInfoByNo(@RequestBody Map<String, String> param){
        WorkOrder workOrder;
        try{
            workOrder = workOrderService.queryWorkOrderByNo(param.get(Dict.WORKNO));
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"根据工单号获取工单失败！"});
        }
        return JsonResultUtil.buildSuccess(workOrder);
    }

    /**
     * 查询历史工单
     * */
    @RequestMapping("/queryHistoryOrder")
    public JsonResult queryHistoryWorkOrder(@RequestBody Map map) throws Exception{
        return JsonResultUtil.buildSuccess(workOrderService.queryHistoryWorkOrder(map));
    }

    /**
     * 查询历史工单总数
     * */
    @RequestMapping("/queryHistoryOrderCount")
    public JsonResult queryHistoryWorkOrderCount(@RequestBody Map map){
        Map myHistoryCount;
        try {
            myHistoryCount = workOrderService.queryHistoryWorkOrderCount(map);
        }catch (FtmsException e){
            logger.error(e.getMessage());
            return JsonResultUtil.buildError(ErrorConstants.GET_CURRENT_USER_INFO_ERROR,"登录失效，请重新登录！");
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"查询历史工单失败！"});
        }
        return JsonResultUtil.buildSuccess(myHistoryCount);
    }

    /**
     * 工单分配
     * */
    @RequestMapping("/assignOrder")
    public JsonResult assignWorkOrder(@RequestBody Map map) throws Exception{
    	WorkOrder workOrder;
    	String taskName = null;
    	String testers = null;
    	List users = null;
        workOrderService.assignWorkOrder(map);
        testers = (String) map.get("testers");
        workOrder = workOrderService.queryWorkOrderByNo((String) map.get("workOrderNo"));
        if(!Util.isNullOrEmpty(workOrder) && !Util.isNullOrEmpty(testers)) {
            taskName = workOrder.getMainTaskName();
            users = new ArrayList<String>();
            users.addAll(Arrays.asList(testers.split(",")));
            Map messageMap = new HashMap();
            messageMap.put("content", taskName);
            messageMap.put("type", "工单分配 ");
            messageMap.put("target", users);
            iNotifyApi.sendUserNotify(messageMap);
        }
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 工单总数统计
     * */
    @RequestMapping("/orderCount")
    public JsonResult orderCount(@RequestBody Map map){
        Map result;
        try{
             result = workOrderService.orderCount(map);
        }catch (FtmsException e){
            logger.error(e.getMessage());
            return JsonResultUtil.buildError(ErrorConstants.GET_CURRENT_USER_INFO_ERROR,"登录失效，请重新登录！");
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.SERVER_ERROR);
        }
        return JsonResultUtil.buildSuccess(result);
    }


    /**
     * 查询工单
     * */
    @RequestMapping("/queryOrder")
    public JsonResult queryOrder(@RequestBody Map map){
        List<WorkOrderUser> userOrderList;
        try {
            userOrderList = workOrderService.queryOrder(map);
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"查询工单失败！"});
        }
        return JsonResultUtil.buildSuccess(userOrderList);
    }

    /**
     * 查询工单 处的 查询总数
     * */
    @RequestMapping("/queryOrderCount")
    public JsonResult queryOrderCount(@RequestBody Map map){
        Integer count = null;
        try {
            count = workOrderService.queryOrderCount(map);
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"查询工单总数失败失败！"});
        }
        return JsonResultUtil.buildSuccess(count);
    }



    /**
     * 查询工单的任务列表
     * {workNo:}
     * */
    @RequestMapping("/queryTaskList")
    public JsonResult queryTaskList(@RequestBody Map map){
        List<TaskList> myTaskList;
        try {
            myTaskList = taskListService.queryTaskList(map);
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"查询任务列表失败！"});
        }
        return JsonResultUtil.buildSuccess(myTaskList);
    }

    /**
     * 查询任务列表总数
     * */
    @RequestMapping("/queryTaskListCount")
    public JsonResult queryTaskListCount(@RequestBody Map map){
        Map myTaskListCount;
        try {
            myTaskListCount = taskListService.queryTaskListCountByWorkNo(map);
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"查询任务列表失败！"});
        }
        return JsonResultUtil.buildSuccess(myTaskListCount);
    }


    /**
     * 回退 误抢工单
     * */
    @RequestMapping("/rollBackWorkOrder")
    public JsonResult rollBackWorkOrder(@RequestBody Map map){
        String workOrderNo = (String) map.getOrDefault(Dict.WORKORDERNO, Constants.FAIL_GET);
        Map myTaskListCount;
        if(!Util.isNullOrEmpty(workOrderNo)){
            try {
                Integer count = workOrderService.rollBackWorkOrder(workOrderNo);
                if (count == null || count == 0){
                    return JsonResultUtil.buildError(ErrorConstants.DATA_QUERY_ERROR, "数据查询修改失败");
                }else if (count > 1){
                    return JsonResultUtil.buildError(ErrorConstants.REPET_INSERT_REEOR, "数据回退多次异常，请联系开发人员处理");
                }
                return JsonResultUtil.buildSuccess(count);
            }catch (Exception e){
                logger.error(e.getMessage());
                throw new FtmsException(ErrorConstants.SERVER_ERROR);
            }
        }else {
            return JsonResultUtil.buildError(ErrorConstants.PARAM_CANNOT_BE_EMPTY, "传入的工单编号不能为空");
        }
    }

    
    /**
     * 根据任务id查询工单编号
     * @throws Exception 
     * */
    @RequestValidate(NotEmptyFields = {Dict.TASK_ID})
    @RequestMapping("/queryWorkNoByTaskId")
    public JsonResult queryWorkNoByTaskId(@RequestBody Map<String,String> map) throws Exception{
    	String task_id = map.get(Dict.TASK_ID);
    	return JsonResultUtil.buildSuccess(workOrderService.queryWorkNoByTaskId(task_id, map.get(Dict.ORDERTYPE)));
    }

    /**
     * 需求资源管理相关报表
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryResourceManagement")
    public JsonResult queryResourceManagement(@RequestBody Map<String,Object> map) throws Exception{
        return JsonResultUtil.buildSuccess(workOrderService.queryResourceManagement(map));
    }

    /**
     * 查询工单所能修改的阶段列表
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryWorkOrderStageList")
    public JsonResult queryWorkOrderStageList(@RequestBody Map<String,String> map) throws Exception{
        String workNo = map.get(Dict.WORKNO);
        String groupId = String.valueOf(map.get(Dict.GROUPID));
        return JsonResultUtil.buildSuccess(workOrderService.queryWorkOrderStageList(workNo, groupId));
    }

    /**
     * 根据任务id查询案例列表
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryTestcaseByTaskId")
    @RequestValidate()
    public JsonResult queryTestcaseByTaskId(@RequestBody Map<String,String> map) throws Exception {
        String taskNo = map.get(Dict.TASKNO);
        return JsonResultUtil.buildSuccess(workOrderService.queryTestcaseByTaskId(taskNo, map.get(Dict.ORDERTYPE)));
    }

    /**
     * 获取废弃的工单
     * */
    @RequestMapping("/queryWasteOrder")
    public JsonResult queryWasteOrder(@RequestBody Map map){
        List<WorkOrderUser> userOrderList;
        List<WorkOrder> orderList;
        try{
            userOrderList = workOrderService.queryWasteOrder(map);
        }catch (FtmsException e){
            logger.error(e.getMessage());
            return JsonResultUtil.buildError(ErrorConstants.GET_CURRENT_USER_INFO_ERROR,"登录失效，请重新登录！");
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"获取分配给当前用户的工单失败！"});
        }
        return JsonResultUtil.buildSuccess(userOrderList);
    }

    /**
     * 查询废弃工单总数
     * */
    @RequestMapping("/queryWasteOrderCount")
    public JsonResult queryWasteOrderCount(@RequestBody Map map){
        Map myWasteCount;
        try {
            myWasteCount = workOrderService.queryWasteOrderCount(map);
        }catch (FtmsException e){
            logger.error(e.getMessage());
            return JsonResultUtil.buildError(ErrorConstants.GET_CURRENT_USER_INFO_ERROR,"登录失效，请重新登录！");
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"查询废弃工单失败！"});
        }
        return JsonResultUtil.buildSuccess(myWasteCount);
    }

    /**
     * 废弃工单的计划或者案例迁移
     * @param map
     * @return
     */
    @RequestMapping("/movePlanOrCase")
    @RequestValidate(NotEmptyFields = {Constants.MOVETYPE, Constants.TOWORKNO})
    public JsonResult movePlanOrCase(@RequestBody Map map) {
        try {
            Integer count = workOrderService.movePlanOrCase(map);
            if (count == 1) {
                return JsonResultUtil.buildSuccess(count);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_UPDATE_ERROR, new String[]{"工单迁移失败"});
        }
        return JsonResultUtil.buildError(Constants.I_FAILED,"工单迁移缺少必要参数！");
    }

    /**
     * 查询当前角色的有效工单(排除投产和废弃)
     * @param map
     * @return
     */
    @RequestMapping("/queryUserValidOrder")
    public JsonResult queryUserValidOrder(@RequestBody Map map) {
        List<Map<String, String>> result = null;
        try {
            result = workOrderService.queryUserValidOrder(map);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询角色有效工单失败！"});
        }
        return JsonResultUtil.buildSuccess(result);
    }

    /** 查询所有工单名及id
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryWorkOrderList")
    public JsonResult queryWorkOrderList() throws Exception{
        return JsonResultUtil.buildSuccess(workOrderService.queryWorkOrderList());
    }

    /**
     * 写入测试阻碍
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/writeObstacle")
    @RequestValidate(NotEmptyFields = {Dict.WORKNO, Dict.OBSTACLE})
    public JsonResult writeObstacle(@RequestBody Map map) throws Exception {
        int row;
        try {
            row = workOrderService.writeObstacle(map);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"查询任务列表失败！"});
        }
        return JsonResultUtil.buildSuccess(row);
    }

    /**
     * 查工单需求文档
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryRqrFilesByOrderNo")
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
    public JsonResult queryRqrFilesByOrderNo(@RequestBody Map<String,String> map) throws Exception {
        String workNo = map.get(Dict.WORKNO);
        return JsonResultUtil.buildSuccess(workOrderService.queryRqrFilesByWorkNo(workNo));
    }

    /**
     * 工单拆分
     * */
    @RequestMapping("/splitWorkOrder")
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
    public JsonResult splitWorkOrder(@RequestBody Map<String,Object> map) throws Exception{
        String workNo = (String) map.get(Dict.WORKNO);//工单编号
        List<String> taskIds = (List<String>) map.get(Dict.TASKIDS);//选择拆分的任务编号集合
        String name = (String)map.get(Dict.NAME);
        List<String> testers = orderUtil.strToList(map, Dict.TESTERS);
        String selectedWorkNo = (String) map.get(Dict.SELECTEDWORKNO);
        workOrderService.splitWorkOrder(workNo, taskIds, name,testers,selectedWorkNo);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 工单拆分
     * */
    @RequestMapping("/queryWorkOrderName")
    public JsonResult queryWorkOrderName(@RequestBody Map<String,Object> map) throws Exception{
        String unit = (String) map.get(Dict.UNIT);//工单编号
        return JsonResultUtil.buildSuccess(workOrderService.queryWorkOrderName(unit));
    }

    /**
     * 创建工单:实施单元评估完成后创建工单
     * */
    @RequestMapping("/createWorkOrder")
    public JsonResult createWorkOrder(@RequestBody Map<String,Object> map) throws Exception{
        try {
            //效能组可以一个任务集创建N多任务，不走修改逻辑
            if(!unitGroupIdsStr.contains(String.valueOf(map.get(Dict.GROUP_ID)))) {
                //查询该实施单元是否已创建工单
                String unitNo = (String) map.getOrDefault(Dict.UNITNO, Constants.FAIL_GET);
                Map<String,Object> workOrderMap = workOrderService.queryWorkNoByUnitNo(unitNo);
                List<String> workNoList = (List<String>)workOrderMap.get(Dict.WORKNOS);
                if(workNoList.size() == 1) {
                    //修改原工单
                    map.put(Dict.WORKORDERNO, workNoList.get(0));
                    if(workOrderService.updateWorkOrder(map) == 0) {
                        return JsonResultUtil.buildError(Constants.I_FAILED,"修改工单失败！");
                    }
                    map.put(Dict.WORKORDERNO, workNoList.get(0));
                    return JsonResultUtil.buildSuccess(map);
                }
            }
            String workOrder = workOrderService.createWorkOrder(map);
            if(Util.isNullOrEmpty(workOrder)){
                throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR,new String[]{"创建实施单元为"+(String) map.get(Dict.UNITNO)+"工单失败！"});
            }
            map.put(Dict.WORKORDERNO, workOrder);
        }catch (Exception e){
            logger.error(e.toString());
            throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR,new String[]{"创建实施单元为"+(String) map.get(Dict.UNITNO)+"工单失败！"});
        }
        return JsonResultUtil.buildSuccess(map);
    }

    /**
     * 根据实施单元查询工单号
     * */
    @RequestMapping("/queryWorkNoByUnitNo")
    @RequestValidate(NotEmptyFields = {Dict.UNITNO})
    public JsonResult queryWorkNoByUnitNo(@RequestBody Map<String,String> map) throws Exception {
        String unitNo = map.get(Dict.UNITNO);
        Map<String,Object> workNos=workOrderService.queryWorkNoByUnitNo(unitNo);
        return JsonResultUtil.buildSuccess(workNos);
    }

    /**
     * 根据实施单元修改所有对应工单的计划sit、uat、投产时间
     * */
    @RequestMapping("/updateTimeByUnitNo")
    @RequestValidate(NotEmptyFields = {Dict.UNITNO})
    public JsonResult updateTimeByUnitNo(@RequestBody Map<String,String> map){
        String unitNo = map.get(Dict.UNITNO);
        String planSitDate = map.get(Dict.INTERNALTESTSTART);
        String planUatDate = map.get(Dict.INTERNALTESTEND);
        String planProDate = map.get(Dict.EXPECTEDPRODUCTDATE);
        workOrderService.updateTimeByUnitNo(unitNo,planSitDate,planUatDate,planProDate);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 合并查询该实施单元的工单列表,剔除原工单要
     * */
    @RequestMapping("/queryMergeOrderList")
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
    public JsonResult queryMergeOrderList(@RequestBody Map<String,String> map) throws Exception {
        String workNo = map.get(Dict.WORKNO);
        String unitNo = map.get(Dict.UNITNO);
        return JsonResultUtil.buildSuccess(workOrderService.queryMergeOrderList(workNo,unitNo));
    }

    /**
     * 工单合并
     * */
    @RequestMapping("/mergeWorkOrder")
    public JsonResult mergeWorkOrder(@RequestBody Map map) throws Exception {
        String workOrderName = (String) map.get(Dict.NAME);
        List<String> workNos = (List<String>) map.get(Dict.WORKNOS);
        String workNo = (String) map.get(Dict.WORKNO);
        workOrderService.mergeWorkOrder(workOrderName,workNos,workNo);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 拆分工单查询该实施单元下符合要求的工单
     * */
    @RequestMapping("/querySplitOrderList")
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
    public JsonResult querySplitOrderList(@RequestBody Map map) throws Exception {
        String workNo = (String) map.get(Dict.WORKNO);
        String unitNo = (String) map.get(Dict.UNITNO);
        return JsonResultUtil.buildSuccess(workOrderService.querySplitOrderList(workNo,unitNo));
    }

    /**
     * 查询是否是新老fdev
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryNewFdevByTaskNo")
    public JsonResult queryNewFdevByTaskNo(@RequestBody Map map) throws Exception {
        String taskId = (String) map.get(Dict.TASKID);
        return JsonResultUtil.buildSuccess(workOrderService.queryNewFdevByTaskNo(taskId));
    }


    /**
     * 根据工单号返回工单信息
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryTaskNameTestersByNo", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.WORKNO})
    @ResponseBody
    public JsonResult queryTaskNameTestersByNo(@RequestBody Map<String, String> requestParam) throws Exception {
        String workOrderNo = requestParam.get(Dict.WORKNO);
        return JsonResultUtil.buildSuccess(workOrderService.queryTaskNameTestersByNo(workOrderNo));
    }

    /**
     * 根据任务id查工单详情
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryOrderByTaskId", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.TASKNO})
    @ResponseBody
    public JsonResult queryOrderByTaskId(@RequestBody Map<String, String> requestParam) throws Exception {
        String taskNo = requestParam.get(Dict.TASKNO);
        return JsonResultUtil.buildSuccess(workOrderService.queryOrderByTaskId(taskNo, requestParam.get(Dict.ORDERTYPE)));
    }

    @RequestMapping(value = "/createSecurityWorkOrder", method = RequestMethod.POST)
    @RequestValidate(NotEmptyFields = {Dict.TASKNO})
    public JsonResult createSecurityWorkOrder(@RequestBody Map<String, Object> requestParam) throws Exception {
        String taskNo = (String) requestParam.get(Dict.TASKNO);
        String taskName = (String) requestParam.get(Dict.TASKNAME);
        String unitNo = (String) requestParam.get(Dict.UNITNO);
        String remark = (String) requestParam.get(Dict.REMARK);
        String correlationSystem = (String) requestParam.get(Dict.CORRELATIONSYSTEM);
        String correlationInterface = (String) requestParam.get(Dict.CORRELATIONINTERFACE);
        String interfaceFilePath = (String) requestParam.get(Dict.INTERFACEFILEPATH);
        String transFilePath = (String) requestParam.get(Dict.TRANSFILEPATH);
        String appName = (String) requestParam.get(Dict.APPNAME);
        String developer = (String) requestParam.get(Dict.DEVELOPER);
        String group = (String) requestParam.get(Dict.GROUP);
        List<Map> transList = (List<Map>) requestParam.get(Dict.TRANSLIST);
        return JsonResultUtil.buildSuccess(workOrderService.createSecurityWorkOrder(taskNo, taskName, unitNo,remark,
                correlationSystem, correlationInterface, interfaceFilePath, transFilePath, appName, developer, group, transList));
    }

    /**
     * 查询任务的安全测试是否完成
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/querySecurityTestResult", method = RequestMethod.POST)
    public JsonResult querySecurityTestResult(@RequestBody Map<String,List<String>> params) throws Exception {
        return JsonResultUtil.buildSuccess(workOrderService.querySecurityTestResult(params.get(Dict.TASKIDS)));
    }

    /**
     * 根据工单号批量查询工单基础信息
     * @param params
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryWorkOrderByNos", method = RequestMethod.POST)
    public JsonResult queryWorkOrderByNos(@RequestBody Map<String,List<String>> params) throws Exception {
        return JsonResultUtil.buildSuccess(workOrderService.queryWorkOrderByNos(params.get(Dict.WORKNOS), params.get(Dict.FIELDS)));
    }
}
