package com.gotest.service;


import com.gotest.domain.WorkOrder;
import com.gotest.domain.WorkOrderUser;

import java.util.List;
import java.util.Map;

public interface WorkOrderService {

    Integer orderGrab(String orderId) throws Exception;

    Integer addWorkOrder(WorkOrder workOrder) throws Exception;

    List<Map>  queryAdminAssignOrder(Map map) throws Exception;

    List<WorkOrder>  queryLeaderAssignOrder() throws Exception;

    List<WorkOrderUser>  queryUserAllWorkOrder(Map map) throws Exception;

    List queryUserAllOrderWithoutPage(Map map) throws Exception;

    List<WorkOrderUser>  queryHistoryWorkOrder(Map map) throws Exception;

    List<WorkOrderUser>  queryOrder(Map map) throws Exception;


    Integer updateWorkOrder(Map map) throws Exception;

    WorkOrder queryWorkOrderByNo(String workOrderNo) throws Exception;

    Map assignWorkOrder(Map map) throws Exception;

    Map orderCount(Map map) throws Exception;

    Map queryHistoryWorkOrderCount(Map map) throws Exception;


    Integer queryOrderCount(Map map) throws Exception;

    Integer rollBackWorkOrder(String workOrderNo) throws Exception;

	void changeUatSubmit(String workNo) throws Exception;

	String queryWorkNoByTaskId(String task_id, String orderType) throws Exception;

    List<Map> queryResourceManagement(Map map) throws Exception;

    List<Map<String, String>> queryWorkOrderStageList(String workNo, String groupId) throws Exception;

	Integer updateSitFlag(Map<String,String> map) throws Exception;

	Map<String, Object> queryTestcaseByTaskId(String taskNo, String orderType) throws  Exception;

    List<WorkOrder> queryWorkOrderList() throws  Exception;

    List<WorkOrderUser>  queryWasteOrder(Map map) throws Exception;

    Map queryWasteOrderCount(Map map) throws Exception;

    Integer movePlanOrCase(Map map) throws Exception;

    List<Map<String, String>> queryUserValidOrder(Map map) throws Exception;

    int writeObstacle(Map map) throws Exception;

    Map queryRqrFilesByWorkNo(String workNo) throws  Exception;

    boolean verifyOrderName(String mainTaskName) throws Exception;

    void splitWorkOrder(String workNo, List<String> taskIds, String name,List<String> testers,String selectedWorkNo) throws Exception;

    String createWorkOrder(Map<String, Object> map) throws Exception;

    Map<String,Object> queryWorkNoByUnitNo(String unitNo) throws Exception;

    void updateTimeByUnitNo(String unitNo, String planSitDate, String planUatDate, String planProDate);

    String queryWorkOrderName(String unit) throws Exception;

    List<Map> queryMergeOrderList(String workNo, String unitNo) throws Exception;

    void mergeWorkOrder(String workOrderName, List<String> workNos,String workNo) throws Exception;

    List<WorkOrder> querySplitOrderList(String workNo, String unitNo) throws Exception;

    String queryNewFdevByTaskNo(String taskId);

    void updateOrderStage(String workOrderNo, String stage);

    Map queryTaskNameTestersByNo(String workOrderNo) throws Exception;

    Map queryOrderByTaskId(String taskNo, String orderType) throws Exception;

    void updateOrderStageAndSitFlag(String workOrderNo, String s, String s1);

    /**
     * 创建安全测试工单
     * @param taskNo
     * @param taskName
     * @param unitNo
     * @param remark
     * @param correlationSystem
     * @param correlationInterface
     * @param interfaceFilePath
     * @param transFilePath
     * @param appName
     * @param developer
     * @param group
     * @param transList
     * @return
     */
    String createSecurityWorkOrder(String taskNo, String taskName, String unitNo, String remark,
                                   String correlationSystem, String correlationInterface, String interfaceFilePath,
                                   String transFilePath, String appName, String developer, String group, List<Map> transList) throws Exception;

    /**
     * 查询任务的安全测试是否完成
     * @param taskIds
     * @return
     */
    Map<String,Object> querySecurityTestResult(List<String> taskIds);

    /**
     * 根据工单号批量查询工单基础信息
     * @param workNos
     * @param fields
     * @return
     */
    List<WorkOrder> queryWorkOrderByNos(List<String> workNos, List<String> fields);
}
