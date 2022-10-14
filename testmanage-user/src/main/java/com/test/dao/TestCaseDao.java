package com.test.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseDao {
	
	List<Map> countUserTestCaseByTime(@Param("startDate")String startDate, @Param("endDate")String endDate, @Param("userName")List<String> userName) throws Exception;

}	
