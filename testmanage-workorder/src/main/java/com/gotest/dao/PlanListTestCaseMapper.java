package com.gotest.dao;

import com.gotest.domain.PlanListTestCase;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PlanListTestCaseMapper {


    List<PlanListTestCase> queryFdevOrderState(@Param("workNo")String workorderNo,Integer start,Integer pageSize);

    Integer queryFdevOrderStateCount(@Param("workNo")String workorderNo);

    String queryOrderStateByWorkNo(@Param("workNo")String workorderNo);

    List<Map<String,String>> countCase(@Param("workNo") String workNo) throws Exception;

    List<Integer> queryResultIdByPlanId(@Param("planId")Integer planId) throws Exception;

    Integer updateWorkNoByResultId(@Param("resultId")Integer resultId, @Param("workNo")String workNo) throws Exception;

}
