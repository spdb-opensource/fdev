package com.manager.ftms.dao;

import java.util.List;
import java.util.Map;

import com.manager.ftms.entity.TestcaseExeRecord;
import org.apache.ibatis.annotations.Param;
import org.bouncycastle.jce.exception.ExtCertPathBuilderException;
import org.springframework.stereotype.Repository;

import com.manager.ftms.entity.PlanlistTestcaseRelation;

@Repository
public interface PlanlistTestcaseRelationMapper {

	int deleteByPlanId(@Param("planId") Integer planId) throws Exception;

	List<PlanlistTestcaseRelation> selectByPlanId(@Param("map")Map map) throws Exception;

	List<PlanlistTestcaseRelation> selectAll() throws Exception;

	// int updateByPrimaryKey(PlanlistTestcaseRelation record)throws Exception;

	PlanlistTestcaseRelation selectRelationByTestcaseNoAndPlanId(@Param("testcaseNo") String testcaseNo,
			@Param("planId") Integer planId) throws Exception;

	int insert(PlanlistTestcaseRelation planlistTestcaseRelation) throws Exception;

	/**
	 * 批量复制/复用 案例
	 * 
	 * @param testcaseNos
	 * @param planId
	 * @return
	 */
	int batchAddRelation(@Param("testcaseNos") List testcaseNos, @Param("planId") Integer planId, @Param("workNo") String workNo,
			@Param("createTm") String createTm) throws Exception;

	/**
	 * 根据案例编号和计划编号查询关系表数据数量
	 * 
	 * @param testcaseNo
	 * @return 数量
	 */
	int queryCountByTestcaseNo(String testcaseNo) throws Exception;

	/**
	 * 根据流水id删除案例计划关系表关系
	 * 
	 * @param planlistTsetcaseId
	 * @return 受影响行数
	 */
	int deletePlanlistTsetcaseRelationById(String planlistTsetcaseId) throws Exception;

	/**
	 * 添加案例计划关系
	 * 
	 * @param map
	 * @return int
	 */
	int addPlanlistTsetcaseRelation(Map map) throws Exception;

	/**
	 * 根据计划删除计划关系
	 * 
	 * @param planid
	 * @return
	 */
	int delPlanlistTestcaseIdByPlanId(Integer planid) throws Exception;

	/**
	 * 批量删除案例关系
	 * 
	 * @param
	 * @return
	 */
	int delBatchRelationCase(@Param("planId") Integer planId, @Param("testcaseNoList") List<String> testcaseNoList)
			throws Exception;

	/**
	 * 根据计划Id查询并且是未生效的案例
	 * 
	 * @param planId
	 * @return
	 */
	List<String> selByPlanList(@Param("planId") Integer planId) throws Exception;

	/**
	 * 根据案例编号查询案例关系里面是否只有一个人在使用此案例
	 * 
	 * @param testcaseNo
	 * @return
	 */
	int queryPlanlistTestcaseRelationCountBytestcaseNo(@Param("testcaseNo") String testcaseNo) throws Exception;

	/**
	 * 根据案例编号，和计划id删除关系
	 * 
	 * @param testcaseNo
	 * @param planId
	 * @return
	 */
	int delPlanlistTestcaseRelationByTestcaseNo(@Param("testcaseNo") String testcaseNo,
			@Param("planId") Integer planId) throws Exception;

	/**
	 * 根据案例执行状态Id 修改案例执行状态
	 * 
	 * @param planlistTestcaseId
	 * @param testcaseExecuteResult
	 * @return
	 */
	int updateTestCaseExecuteStatus(@Param("planlistTestcaseId") String planlistTestcaseId,
			@Param("testcaseExecuteResult") String testcaseExecuteResult,
			@Param("testcaseExecuteDate") String testcaseExecuteDate, @Param("fnlOpr") String fnlOpr,
			@Param("fstOpr") String fstOpr, @Param("fstTm") String fstTm, @Param("exeNum") Integer exeNum,
			@Param("blockExeNum") Integer blockExeNum, @Param("failExeNum") Integer failExeNum)
			throws Exception;

	PlanlistTestcaseRelation queryRelation(@Param("planlistTestcaseId") String planlistTestcaseId) throws Exception;

	/**
	 * 根据案例编号和计划编号查询关系表数据数量
	 *
	 * @param testcaseNo
	 * @param planId
	 * @return
	 * @throws Exception
	 */
	int queryCountByPlanIdandTestcaseNo(@Param("testcaseNo") String testcaseNo, @Param("planId") Integer planId) throws Exception;

	/**
	 * 根据计划案例结果Id查询计划Id
	 * @param planlistTestcaseId
	 * @return
	 * @throws Exception
	 */
	int queryPlanIdByPlanlistTestcaseId(@Param("planlistTestcaseId") String planlistTestcaseId) throws Exception;

	/**
	 *
	 *
	 * @param finalUpdateTime
	 * @param finalUpdatePrs
	 * @param planlistTestcaseId
	 * @return
	 */
	int updateModify(@Param("finalUpdateTime") String finalUpdateTime, @Param("finalUpdatePrs") String finalUpdatePrs, @Param("planlistTestcaseId") String planlistTestcaseId);

	/**
	 * 新增案例执行记录
	 * @param testcaseExeRecord
	 * @throws Exception
	 */
	void addTestcaseExeRecord(TestcaseExeRecord testcaseExeRecord) throws  Exception;

    void deleteTestcaseExeRecord(@Param("planlistTsetcaseId")String planlistTsetcaseId) throws Exception;

	Integer updateTestcaseByPlanlistTestcaseId(@Param("testCaseNo")String testCaseNo, @Param("planlistTestcaseId")String planlistTestcaseId) throws Exception;

}