package com.auto.dao;

import com.auto.entity.Assert;
import com.auto.entity.Data;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AssertMapper {

    void addAssert(Assert asrt) throws Exception;

    List<Map<String, String>> queryAssert(@Param("search") String search, @Param("valid") String valid) throws Exception;
    
    List<Map<String, String>> queryAssertByLabel(@Param("label") String label, @Param("assertId") String assertId) throws Exception;
    
    List<Map<String, String>> queryAssertByTestCaseNo(@Param("testcaseNo") String testcaseNo) throws Exception;

    void deleteAssert(@Param("asrt") String asrt, @Param("userNameEn") String userNameEn, @Param("time") String time) throws Exception;

    void updateAssert(@Param("assertId") String assertId, @Param("label") String label,
                    @Param("userNameEn") String userNameEn, @Param("time") String time,
                    @Param("map") Map map) throws Exception;
}