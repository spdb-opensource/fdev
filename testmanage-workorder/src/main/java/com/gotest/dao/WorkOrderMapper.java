package com.gotest.dao;


import com.gotest.domain.WorkOrder;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface WorkOrderMapper {

    Integer addWorkOrder(WorkOrder workOrder);

    Integer queryOrderExist(String workOrderNo);

    List<WorkOrder> queryAdminAssignOrder(@Param("taskName") String taskName, @Param("start") Integer start, @Param("pageSize") Integer pageSize);

    List<WorkOrder> queryLeaderAssignOrder(String username);

    List<WorkOrder> queryUserAllWorkOrder(@Param("taskName") String taskName, @Param("userName") String userName,
                                          @Param("start") Integer start, @Param("pageSize") Integer pageSize,
                                          @Param("userRole") Integer userRole, @Param("workStage") String workStage,
                                          @Param("personFilter") String personFilter, @Param("sitFlag") String sitFlag,
                                          @Param("groupSort") String groupSort, @Param("stageSort") String stageSort, String orderType);

    List<WorkOrder> queryUserHistoryWorkOrder(@Param("testerName") String testerName, @Param("taskName") String taskName,
                                              @Param("userName") String userName, @Param("start") Integer start, @Param("pageSize") Integer pageSize,
                                              @Param("userRole") Integer userRole, String orderType);

    List<WorkOrder> queryWorkOrder(@Param("taskName") String taskName, @Param("userEnName") String userEnName, @Param("start") Integer start, @Param("pageSize") Integer pageSize, @Param("groupId") String groupId, String orderType);

    Integer queryOrderCount(@Param("taskName") String taskName, @Param("userEnName") String userEnName, @Param("groupId") String groupId, String orderType);

    Integer updateWorkOrder(WorkOrder workOrder);

    WorkOrder queryWorkOrderByNo(@Param("workOrderNo") String workOrderNo);

    void assignWorkOrder(@Param("orderNo") String orderNo, @Param("testers") String testers, @Param("workLeader") String workLeader, @Param("groupId") String groupId);

    void orderGrab(@Param("orderId") String orderId, @Param("userName") String userName);

    Integer orderAssignCount(@Param("taskName") String taskName);

    Integer orderUserCount(@Param("taskName") String taskName, @Param("userName") String userName, @Param("userRole") Integer userRole, @Param("workStage") String workStage, @Param("personFilter") String personFilter, @Param("sitFlag") String sitFlag, String orderType);

    Integer orderHistoryCount(@Param("testerName") String testerName, @Param("taskName") String taskName, @Param("userName") String userName, @Param("userRole") Integer userRole, String orderType);


    Integer dropWorkOrderByWorkNo(@Param("workOrderNo") String workOrderNo);

    String queryWorkFlagByWorkNo(@Param("workOrderNo") String workOrderNo);

    List<WorkOrder> queryAduitOrder(@Param("userEnName") String userEnName, @Param("taskName") String taskName, @Param("start") Integer start, @Param("pageSize") Integer pageSize, @Param("userRole") Integer userRole, String orderType);

    Integer passAduit(@Param("workOrderNo") String workOrderNo);

    Integer refuseAduit(@Param("workOrderNo") String workOrderNo);

    String queryOrderStateByWorkNo(@Param("workOrderNo") String workOrderNo);

    List<String> queryGroupByDate(@Param("dateField3") String dateField3);

    Integer rollBackWorkOrder(@Param("workOrderNo") String workOrderNo);

    Integer queryAduitOrderCount(@Param("userEnName") String userEnName, @Param("taskName") String taskName, @Param("userRole") Integer userRole, String orderType);

    List<Map<String, String>> queryWorkNoByTaskNo(@Param("taskNos") String taskNos);

    List<WorkOrder> queryUpSitReport(@Param("start") Integer start, @Param("pageSize") Integer pageSize, @Param("taskName") String taskName, @Param("workOrderNo") String workOrderNo, @Param("userEnName") String userEnName, @Param("done") String done, @Param("sortManager") String sortManager, String orderType);

    Integer queryUpSitOrderCount(@Param("taskName") String taskName, @Param("workOrderNo") String workOrderNo, @Param("userEnName") String userEnName, @Param("done") String done, String orderType);

    Integer fdevRollBackOrder(WorkOrder workOrder);

    Integer CheckWorkOrderByTaskNo(@Param("taskNo") String taskNo) throws Exception;

    String queryWorkNoByTaskId(@Param("task_id") String task_id, String orderType) throws Exception;

    List<Map<String, Object>> queryResourceManagement(Map map) throws Exception;

    Integer updateSitFlag(@Param("workNo") String workNo, @Param("updateFlag") String updateFlag, @Param("dateStr") String dateStr) throws Exception;

    void setOrderFdevGroupId(String workOrderNo, String fdevGroupId) throws Exception;

    Integer updateUatSubmitDate(@Param("workNo") String workNo, @Param("uatSubmitDate") Long uatSubmitDate) throws Exception;

    List<Map<String, String>> queryTestcaseByTaskId(@Param("workNo") String workNo) throws Exception;

    Integer wasteWorkOrder(@Param("workNo") String workNo) throws Exception;

    List<WorkOrder> queryWasteOrder(
            @Param("testerName") String testerName, @Param("taskName") String taskName,
            @Param("userName") String userName, @Param("start") Integer start,
            @Param("pageSize") Integer pageSize, @Param("userRole") Integer userRole, String orderType);

    Integer queryWasteOrderCount(
            @Param("testerName") String testerName, @Param("taskName") String taskName,
            @Param("userName") String userName, @Param("userRole") Integer userRole, String orderType);

    Integer movePlan(@Param("fromPlanId") Integer fromPlanId, @Param("toWorkNo") String toWorkNo);

    Integer moveCase(@Param("resultId") Integer resultId, @Param("toPlanId") Integer toPlanId, @Param("toWorkNo") String toWorkNo);

    List<Map<String, String>> queryUserValidOrder(@Param("userName") String userName, @Param("userRole") String userRole, String orderType);

    List<WorkOrder> queryWorkOrderList() throws Exception;

    Integer writeObstacle(@Param("workNo") String workNo, @Param("obstacle") String obstacle) throws Exception;

    int queryOrderHasCase(String workNo) throws Exception;

    Integer updateUatFlag(@Param("workNo") String workNo) throws Exception;

    Integer updateImageLink(@Param("workNo") String workNo, @Param("imageLink") String imageLink) throws Exception;

    List<WorkOrder> queryUserAllOrderWithoutPage(String taskName, String userName, Integer userRole, String workStage,
                                                 String personFilter, String sitFlag, String orderType) throws Exception;

    int verifyOrderName(String mainTaskName) throws Exception;

    List<WorkOrder> queryWorkOrderByUnit(@Param("unitNo") String unitNo, String orderType);

    Integer updateSitFlagDownByWorkNo(@Param("workNo") String workNo);

    int abandonNewOrder(@Param("fdev_implement_unit_no") String fdev_implement_unit_no) throws Exception;

    Integer updateSitFlagUpByWorkNo(@Param("workNo") String workNo);

    Integer updateMainTaskNoByWorkNo(@Param("mainTaskNo") String mainTaskNo, @Param("workNo") String workNo);

    void updateStage(@Param("stage") String stage, @Param("oldWorkNo") String oldWorkNo);

    void resetApproval(@Param("workOrderNo") String workOrderNo);

    Integer deleteWorkOrderByTaskNo(@Param("workNo") String workNo);

    Integer deleteWorkOrdersByWorkNos(@Param("workNos") List<String> workNos);

    List<WorkOrder> queryWorkOrderByOrderName(@Param("mainTaskName") String mainTaskName, String orderType);

    //根据需求编号查询所有的工单
    List<Map> queryWorkOrderByOaRealNo(@Param("oaRealNo") String oaRealNo);

    int updateOrderName(@Param("workNo") String workNo, @Param("name") String name);

    //判断该任务是否是新老工单下的
    String queryNewFdevBytaskId(@Param("taskId") String taskId, String orderType);

    void updateOrderStage(@Param("workOrderNo") String workOrderNo, @Param("stage") String stage);

    void updateOrderStageAndSitFlag(@Param("workOrderNo") String workOrderNo,@Param("stage") String stage, @Param("sitFlag") String sitFlag);

    Map queryWorkOrderMemberByTaskNo(@Param("taskNo") String taskNo, String orderType);

    List<WorkOrder> queryOrderByTaskNo(String taskNo, String orderType);

    /**
     * 查询安全测试未通过的任务
     * @param taskIds
     * @return
     */
    List<Map> queryNoPassSecurityOrder(@Param("taskIds") List<String> taskIds);

    /**
     * 根据工单号批量查询工单基础信息
     * @param workNos
     * @return
     */
    List<WorkOrder> queryWorkOrderByNos(@Param("workNos") List<String> workNos);

    /**
     * 查询时间段需要提测的任务数
     * @param startDate
     * @param endDate
     * @param groupIds
     */
    int queryWorkTaskCount(String startDate, String endDate, List<String> groupIds);

    /**
     * 根据工单号批量查询工单类型
     * @param workNos
     * @return
     */
    List<Map> queryOrderTypeByNos(@Param("workNos") List<String> workNos);

    /**
     * 根据需求编号查询工单
     * @param demandNo
     * @return
     */
    List<WorkOrder> queryWorkOrderByDemandNo(String demandNo);
}
