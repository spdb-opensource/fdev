package com.gotest.dao;


import com.gotest.domain.PlanlistTestcaseRelation;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public interface PlanlistTestcaseRelationMapper {
	List<PlanlistTestcaseRelation> queryByWorkNo(@Param("workNo") String workNo);

	int updateWorkNoById(@Param("planlistTestcaseId")Integer planlistTestcaseId,@Param("workNo")String workNo);

	List<Map<String,Object>> queryTestCaseExeByWorkNo(@Param("workNo")String workNo);

	int updateTestCaseExeWorkNoById(@Param("id") Integer id, @Param("workNo")String workNo);

	int updateWorkNoByWorkNos(@Param("workNos")List<String> workNos,@Param("workNo")String workNo);

	int updateTestCaseExeWorkNoByWorkNos(@Param("workNos")List<String> workNos,@Param("workNo")String workNo);
}