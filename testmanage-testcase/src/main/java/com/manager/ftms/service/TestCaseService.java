package com.manager.ftms.service;

import java.util.List;
import java.util.Map;

import com.manager.ftms.entity.Testcase;
import com.manager.ftms.util.TestcaseAuditQueryObject;

public interface TestCaseService {


    List<Testcase> selectTestCaseFuzzy(String parameters)throws Exception;

    /**
     * 根据编号查询案例
     *
     * @param
     * @return 案例集合
     */
    List<Testcase> selectAll()throws Exception;


    //--------------------testcase--------------------------------------
    /**
     * 根据执行计划id查询案例列表   总数
     *
     * @param planId
     * @return 案例集合
     */
    Integer queryTestCount(Integer planId,String testcaseStatus,String testcaseNature,String testcaseType,String testcaseExecuteResult,String testcaseName, String planlistTestcaseId)throws Exception;
    /**
    /**
     * 根据执行计划id查询案例列表   分页
     *
     * @param map
     * @return 案例集合
     */
    List<Testcase> queryTestByPlanId(Map map)throws Exception;

    /**
     * 根据执行计划id查询案例列表
     *
     * @param map
     * @return 案例集合
     */
    List<Testcase> queryTestcaseByPlanId(Map map)throws Exception;

    /**
     * 添加案例
     *
     * @param map
     * @return
     */
    Testcase addTestcase(Map map)throws Exception;

    /**
     * 根据编号查询案例
     *
     * @param testcaseNo
     * @return 单个案例
     */
    Testcase queryDetailByTestcaseNo(String testcaseNo)throws Exception;
    
    /**
     * 根据实体查询案例
     *
     * @param testcaseNo
     * @return 单个案例
     */
    Testcase queryCaseByTestcaseNo(String testcaseNo)throws Exception;

    /**
     * 根据实体修改案例
     *
     * @param map
     * @return 单个案例
     */
    int updateTestcaseByTestcaseNo(Map map, String planlistTestcaseId)throws Exception;


    /**
     * 根据编号删除案例
     *
     * @param testcaseNo
     * @return 受影响行数
     */
    int deleteTestcaseByTestcaseNo(String testcaseNo)throws Exception;


    /**
     * 根据编号修改案例状态为待审核状态
     *
     * @param testcaseNo
     * @return 受影响行数
     */
    int updateTestcaseByStatusWaitPass(String testcaseNo)throws Exception;

    /**
     * 根据编号修改案例状态为待生效状态
     *
     * @param testcaseNo
     * @return 受影响行数
     */
    int updateTestcaseByStatusWaitEffect(String testcaseNo)throws Exception;


    //*************************** 案例审批 ********************************

    /**
     * 根据用户英文名查询案例
     *
     * @param userEnName
     * @param status
     * @return
     */
    List<Testcase> queryTestcaseByUserEnName(String userEnName, String status)throws Exception;

    /**
     * 根据审核结果修改案例状态
     *
     * @param testcaseNo
     * @param status
     * @return
     */
    int auditTestcase(String testcaseNo, String status)throws Exception;

    /**
     * 根据案例编号批量删除
     *
     * @param testcaseNoList
     * @return
     */
    int delBatchTestCaseNos(List<String> testcaseNoList)throws Exception;

    /**
     * 多条件查询
     *
     * @param map
     * @return
     */
    List<Testcase> selTestCaseCondition(Map map)throws Exception;


    /**
     * 批量审核操作/生效
     *
     * @param testcaseNos    案例集合
     * @param testcaseStatus 提交状态
     */
    int batchAudit(List<String> testcaseNos, String testcaseStatus)throws Exception;

    /**
     * 根据功能菜单id查询案例
     *
     * @param map
     * @return
     */
    List<Testcase> queryTestcaseByFuncId(Map map)throws Exception;

    /**
     * 查询案例 根据案例编号 计划id 查询
     * @param testcaseNo
     * @param planId
     * @return
     */
    Testcase queryDetailByTestcaseNoAndPlanId(String testcaseNo, Integer planId)throws Exception;
    /**
     * 根据主键修改
     */
    int updateByPrimaryKey(String testCaseNo,String remark)throws Exception;

    Map<String, Integer> queryPlanAllStatus(Map map)throws Exception;

    /**
     * 案例审核,根据传入选项查询案例
     * @param requestParam   查询对象
     * @return
     */
    List<Testcase> queryTestcaseByOption(TestcaseAuditQueryObject requestParam,String userEnName)throws Exception;

    List<Testcase> queryTestcaseByPlanIdOrStatus(Integer planId)throws Exception;
    
    //根据时间查询当天最大案例编号
    List<String> queryMaxCaseNo(String testcaseDate)throws Exception;
    //查询执行计划下案例对应状态数量
    Map<String, Integer> queryAllStatus(Integer planId)throws Exception;
    
    //统计案例审批条数
    int countTestcase(TestcaseAuditQueryObject requestParam,String userEnName)throws Exception;
 
    //案例库案例统计
    int countTestcaseByFuncId(Map map)throws Exception;

	void changeNecessary(String testcaseNo, String necessaryFlag) throws Exception;

	Map<String,Integer> queryUserApprovalList() throws Exception;
	
	//修改案例的顺序
    int updateTestcaseOrder(Map map)throws Exception;

    Map queryRelativePeople(Map map) throws Exception;

    Integer queryTestcaseByOptionCount(TestcaseAuditQueryObject requestParam, String name);
}
