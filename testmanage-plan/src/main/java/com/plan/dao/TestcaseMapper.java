package com.plan.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TestcaseMapper {

    Integer countCaseByPlanID(@Param("planId")Integer planId, @Param("workNo")String workNo) throws Exception;

}
