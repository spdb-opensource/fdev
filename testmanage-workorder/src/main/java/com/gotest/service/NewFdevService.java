package com.gotest.service;

import com.gotest.domain.PlanList;
import com.gotest.domain.Testcase;
import com.gotest.domain.WorkOrder;

import java.util.List;
import java.util.Map;

/**
 * 新fdev
 */
public interface NewFdevService {


    //根据需求id查询需求的缺陷，测试计划等
    Map<String, Object> queryWorkOrderByOaRealNo(List<String> unitNos, String index, String pageSize) throws Exception;

    //根据测试计划id查询测试用例
    List<Map> queryTestcaseByPlanId(Integer planId, Integer index, Integer pageSize);

    /**
     * 根据案例id查询案例详情
     *
     * @param testcaseNo
     * @return
     */
    Map querytestCaseByTestCaseNo(String testcaseNo);

    Map queryFuserMantisAll(String unitNo, String userNameEn, String currentPage, String pageSize);

    //实施单元id查询计划
    Map<String, Object> queryPlanByUnitNo(List unitNos, Integer index, Integer pageSize);

    //查询案例详情
    Testcase queryDetailByTestcaseNo(String testcaseNo,String planId);

    //查询案例详情
    Testcase queryCaseByTestcaseNo(String testcaseNo,String planId);

    //需求编号查询对应的工单
    List<WorkOrder> queryWorkNoByUnitNos(List unitNos);


    //根据计划id查询查询计划详情
    PlanList queryByPlanId(Integer planId) throws Exception;

    String newCreateWorkOrder(String unitId, String groupId, String unitName, String sitStart, String sitEnd,
                              String proDate, String remark, String id, String demandId, String oaRealNo, String oaContactName) throws Exception;

    String newCreateTask(String taskNo, String unitId, String taskName) throws Exception;

    void updateOrderNum(List<Map> update) throws Exception;

    /**
     * 实施单元删除关联玉衡工单废弃
     *
     * @param unitNo
     * @return
     */
    int deleteWorkOrderByUnitNo(List<String> unitNo);

    /**
     * 任务删除关联玉衡工单任务集删除
     *
     * @param tasks
     * @return
     */
    int deleteTask(List<String> tasks) throws Exception;

    /**
     * 任务变更实施单元
     *
     * @param taskNo
     * @param unitNo
     * @return
     */
    int updateTaskUnitNo(String taskNo, String taskName, String unitNo) throws Exception;

    //查询任务详情
    Map<String,Object> queryFdevTaskDetail(String taskNo);

    int updateOrderName(String workNo, String name) throws Exception;

    int synIpmpUpdateOrderName(String unitNo, String name) throws  Exception;

    int synDemandUpdateOrderInfo(String demandId, String name) throws Exception;
}
