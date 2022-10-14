package com.manager.ftms.service;

import com.manager.ftms.entity.PlanlistTestcaseRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface IPlanlistTestcaseRelationServier {
    /**
     * 根据案例id和计划id查询关系表数据
     *
     * @param testcaseNo
     * @param planId
     * @return
     */
    PlanlistTestcaseRelation queryRelationByTestcaseNoAndPlanId(String testcaseNo, Integer planId)throws Exception;

    /**
     * 案例复用,保存计划和案例的关系
     *
     * @param planlistTestcase
     */
    int savePlanlistTsetcaseRelation(PlanlistTestcaseRelation planlistTestcase)throws Exception;

    /**
     * 批量案例 复用
     *
     * @param testcaseNos
     * @param planId
     * @return
     */
    int batchRepeatedRelation(List testcaseNos, Integer planId, String workNo)throws Exception;

    /**
     * 查询计划下面所有案例关系
     *
     * @return
     */
    List<PlanlistTestcaseRelation> selectByPlanId(Map map)throws Exception;

    /**
     * 根据计划查询案例关系表
     *
     * @param planId
     * @return
     */
    int deleteByPlanId(Integer planId)throws Exception;


    /**
     * 根据案例编号和计划编号查询关系表数据数量
     * @param testcaseNo
     * @return 数量
     */
    int queryCountByTestcaseNo(String testcaseNo)throws Exception;

    /**
     * 根据流水id删除案例计划关系表关系
     * @param planlistTsetcaseId
     * @return 受影响行数
     */
    int deletePlanlistTsetcaseRelationById(String planlistTsetcaseId)throws Exception;

    /**
     * 添加案例计划关系
     * @param map
     * @return int
     */
    int addPlanlistTsetcaseRelation(Map map)throws Exception;

    /**
     * 根据计划删除计划关系
     * @param planid
     * @return
     */
    int delPlanlistTestcaseIdByPlanId(Integer planid)throws Exception;

    /**
     * 删除案例关系
     * @param map
     * @return
     */
    int delBatchRelationCase(Map map)throws Exception;

    /**
     * 批量复制案例
     * @param testcaseNos
     * @param planId
     * @return
     */
    int batchCopyTestcaseToOtherPlan(List<String> testcaseNos, Integer planId,String workNo,String createTm)throws Exception;

    /**
     * 根据案例编号，执行计划修改案例状态
     * @return
     */
    int updateTestCaseExecuteStatus(String id, String testcaseExecuteResult, String remark, String userNameEn)throws Exception;
    
    int queryCountByPlanIdandTestcaseNo(String testCaseNo,Integer planId)throws Exception;

    /**
     * 根据计划案例结果Id查询计划Id
     * @param planlistTestcaseId
     * @return
     * @throws Exception
     */
    String queryPlanIdByPlanlistTestcaseId(String planlistTestcaseId,String workOrderNo) throws Exception;

    /**
     * 新增案例删除记录
     * @param planlistTsetcaseId
     * @throws Exception
     */
    void addRelationDeleteRecord(String planlistTsetcaseId) throws Exception;

    Integer updateTestcaseByPlanlistTestcaseId(String testcaseNo, String planlistTestcaseId) throws Exception;

    /**
     * 新增案例修改记录
     * @param planlistTsetcaseId
     * @throws Exception
     */
   void addRelationModifyRecord(String planlistTsetcaseId) throws Exception;

    /**
     * 批量执行
     * @param ids
     * @param testcaseExecuteResult
     * @throws Exception
     */
   void batchExecuteTestcase(List<String> ids, String testcaseExecuteResult) throws Exception;
}
