package com.manager.ftms.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.manager.ftms.entity.Testcase;
import com.manager.ftms.util.TestcaseAuditQueryObject;

@Repository
public interface TestcaseMapper {


    /**
     * 根据编号查询案例
     *
     * @param
     * @return 案例集合
     */
    List<Testcase> selectAll();


//--------------------------testcase--------------------------------------
    
    /**
     * 根据执行计划id查询案例列表   总数
     *
     * @param planId
     * @return 案例集合
     */
    Integer queryTestCount(@Param("planId")Integer planId,
    		@Param("testcaseStatus") String testcaseStatus,
    		@Param("testcaseNature") String testcaseNature,
    		@Param("testcaseType") String testcaseType,
    		@Param("testcaseExecuteResult") String testcaseExecuteResult,
    		@Param("testcaseName")String testcaseName,
            @Param("planlistTestcaseId")String planlistTestcaseId) throws Exception;
    /**
     * 根据执行计划id查询案例列表   分页
     *
     * @param planId
     * @return 案例集合
     */
    List<Testcase> queryTestByPlanId(
            @Param("planId") Integer planId,
            @Param("start") Integer start,
            @Param("pageSize") Integer pageSize,
    		@Param("testcaseStatus") String testcaseStatus,
    		@Param("testcaseExecuteResult") String testcaseExecuteResult,
    		@Param("testcaseType") String testcaseType,
    		@Param("testcaseNature") String testcaseNature,
    		@Param("testcaseName") String testcaseName,
            @Param("planlistTestcaseId") String planlistTestcaseId
    		) throws Exception;
    /**
     * 根据执行计划id查询案例列表
     *
     * @param map
     * @return 案例集合
     */
    List<Testcase> queryTestcaseByPlanId(@Param("map")Map map) throws Exception;

    /**
     * 添加案例
     *
     * @param testcase
     * @return 单个案例
     */
    int addTestcase(Testcase testcase) throws Exception;


    /**
     * 添加案例计划关系
     *
     * @param map
     * @return int
     */
    int addPlanlistTsetcaseRelation(Map map) throws Exception;

    /**
     * 根据实体查询案例
     *
     * @param testcaseNo
     * @return 单个案例详细信息连表
     */
    Testcase queryDetailByTestcaseNo(@Param("testcaseNo") String testcaseNo) throws Exception;
    
    /**
     * 根据实体查询案例
     *
     * @param testcaseNo
     * @return 单个案例
     */
    Testcase queryCaseByTestcaseNo(String testcaseNo) throws Exception;

    /**
     * 根据编号修改案例
     *
     * @param map
     * @return 单个案例
     */
    int updateTestcaseByTestcaseNo(Map map) throws Exception;

    /**
     * 根据编号删除案例
     *
     * @param testcaseNo
     * @return 受影响行数
     */
    int deleteTestcaseByTestcaseNo(String testcaseNo) throws Exception;

    /**
     * 根据流水id删除案例计划关系表关系
     *
     * @param planlistTsetcaseId
     * @return 受影响行数
     */
    int deletePlanlistTsetcaseRelationById(int planlistTsetcaseId) throws Exception;


    

    /**
     * 根据编号修改案例状态为待审核状态
     *
     * @param testcaseNo
     * @return 受影响行数
     */
    int updateTestcaseByStatusWaitPass(String testcaseNo) throws Exception;

    /**
     * 根据编号修改案例状态为待生效状态
     *
     * @param testcaseNo
     * @return 受影响行数
     */
    int updateTestcaseByStatusWaitEffect(String testcaseNo) throws Exception;

    /**
     * 根据计划id删除计划关系表
     *
     * @param planid
     * @return
     */
    int delPlanlistTestcaseIdByPlanId(@Param("planid") Integer planid) throws Exception;

    List<Testcase> selectTestCaseFuzzy(@Param("parameters") String parameters) throws Exception;

    //*************************** 案例审批 ********************************
    //根据当前用户查询关联的案例
    List<Testcase> selectTestcaseByUserEnName(@Param("userEnName") String userEnName, @Param("testcaseStatus") String status) throws Exception;

    //更新案例状态
    int updateTestcaseByStatus(@Param("testcaseNo") String testcaseNo, @Param("testcaseStatus") String status)throws Exception;

    /**
     * 根据编号查询
     *
     * @param testcaseNoList
     * @return
     */
    int delBatchTestCaseNos(@Param("testcaseNoList") List<String> testcaseNoList) throws Exception;

    /**
     * 根据多条件查询
     *
     * @param
     * @return
     */
    List<Testcase> selTestCaseCondition(@Param("testcaseStatus") String testcaseStatus,
                                        @Param("testcaseExecuteResult") String testcaseExecuteResult,
                                        @Param("testcaseType")String testcaseType,
                                        @Param("testcaseNature")String testcaseNature,
                                        @Param("planId") Integer planId
    ) throws Exception;


    //批量审批
    int batchAudit(@Param("testcaseNos") List<String> testcaseNos, @Param("testcaseStatus") String testcaseStatus)throws Exception;

    //案例库查询
    List<Testcase> selectTestcaseByFuncId(@Param("map") Map testcase, @Param("start")Integer start)throws Exception;

    /**
     * 根据案例编号 计划id查询
     * @param testcaseNo
     * @param planId
     * @return
     */
    Testcase queryDetailByTestcaseNoAndPlanId(@Param("testcaseNo") String testcaseNo, @Param("planId") Integer planId) throws Exception;
    /**
     *根据主键修改案例备注
     */
    int updateByPrimaryKey(@Param("testcaseNo") String testcaseNo,@Param("remark") String remark) throws Exception;


    /**
     * 案例审核,根据传入选项查询案例
     * @param requestParam 查询参数
     * @return
     */
	List<Testcase> queryTestcaseByOption(@Param("start")Integer start,@Param("pageSize")Integer pageSize,@Param("map")TestcaseAuditQueryObject requestParam,@Param("userEnName") String userEnName) throws Exception;
	
	

    List<Testcase> queryTestcaseByPlanIdOrStatus(Integer planId) throws Exception;
    //根据时间查询当天最大案例编号
    List<String> queryMaxCaseNo(String testcaseDate) throws Exception;
    
    Map<String, Integer> queryAllStatus(@Param("planId") Integer planId) throws Exception;
    
    int countTestcase(@Param("map")TestcaseAuditQueryObject requestParam,@Param("userEnName") String userEnName)throws Exception;
    
    //案例库案例统计
    int countTestcaseByFuncId(@Param("map")Map map)throws Exception;

	void changeNecessary(@Param("testcaseNo")String testcaseNo, @Param("necessaryFlag")String necessaryFlag) throws Exception;

	Map<String, Integer> queryUserApprovalList(@Param("userName")String userName) throws Exception;

    List<Map<String, String>> queryPlanIdByNo(@Param("workNo")String workNo) throws Exception;

	Map<String, String> querySpeByOrder(@Param("workNo")String workNo) throws Exception;
	
	//修改案例序号
    int updateTestcaseOrder(@Param("orderNum")String orderNum, @Param("planId")Integer planId, @Param("testcaseNo")String testcaseNo)throws Exception;

    /**
     * 根据执行计划id查询所有案例
     *
     * @param planId
     * @return 案例集合
     */
    List<Testcase> queryAllTestByPlanId(@Param("planId") Integer planId) throws Exception;

    Integer queryTestcaseByOptionCount(@Param("map")TestcaseAuditQueryObject requestParam,@Param("userEnName") String userEnName);
}


