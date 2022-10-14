package com.gotest.dao;


import com.gotest.domain.PlanList;
import com.gotest.domain.TaskList;
import com.gotest.domain.Testcase;
import com.gotest.domain.WorkOrder;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface NewFdevMapper {

    //根据需求编号查询工单
    List<WorkOrder> queryWorkOrderByOaRealNo(@Param("unitNos")String unitNos);

    //根据计划id查询
    Map<String, Object> queryAllStatus(Integer planId);

    //查询案例详情
    Map querytestCaseByTestCaseNo(String testcaseNo);

    // //根据实施单元号查询工单
    List<PlanList> queryPlanByUnitNo(@Param("unitNo") String unitNo, @Param("start_page") Integer start_page, @Param("page_size") Integer page_size);

    //查询案例详情
    Testcase queryDetailByTestcaseNo(@Param("testcaseNo")String testcaseNo,@Param("planId")String planId);

    //查询案例实体
    Testcase queryCaseByTestcaseNo(@Param("testcaseNo")String testcaseNo,@Param("planId")String planId);

    //根据实施单元id查询工单
    List<WorkOrder> queryWorkNoByUnitNos(@Param("unitNo") String unitNo);

    //根据英文名查询中文名
    List<String> queryUserName(@Param("nameList") List<String> nameList);
    List<WorkOrder> checkOrderExist(@Param("unitId") String unitId);

    //实施单元删除关联玉衡工单废弃
    int deleteWorkOrderByUnitNo(@Param("unitNos") String unitNos);

    //任务删除关联玉衡工单任务集删除
    int deleteTask(@Param("taskIds") String taskIds);

    //查询任务下的工单
    List<TaskList> queryWorkByTaskIds(@Param("taskIds") String taskIds);

    Map<String, Object> queryWorkinfoByTaskNo(@Param("taskNo") String taskNo);

    Map<String, Object> queryWorkOrderByMainTaskNo(@Param("taskNo") String taskNo);

    int synIpmpUpdateOrderName(@Param("unitNo")String unitNo,@Param("name")String name);

    List<WorkOrder> queryWorkNoBynuitNo(@Param("unitNo") String unitNo);

    int synDemandUpdateOrderInfo(@Param("demandId") String demandId, @Param("name") String name);
}
