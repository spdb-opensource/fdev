package com.auto.dao;

import com.auto.entity.Testcase;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CaseMapper {

    void addCase(Testcase testcase) throws Exception;

    List<Map<String, String>> queryCase(@Param("search") String search, @Param("valid") String valid, @Param("flag") String flag) throws Exception;
    
    List<Map<String, String>> queryCaseByCaseName(@Param("testcaseName") String testcaseName, @Param("testcaseNo") String testcaseNo) throws Exception;

    void deleteCase(@Param("testcaseNo") String testcaseNo, @Param("userNameEn") String userNameEn, @Param("time") String time) throws Exception;

    void updateCase(@Param("testcaseNo") String testcaseNo, @Param("dataId") String dataId,
                    @Param("userId") String userId, @Param("funcPoint") String funcPoint,
                    @Param("testcaseName") String testcaseName, @Param("precondition") String precondition,
                    @Param("testcaseDesc") String testcaseDesc, @Param("expectedResult") String expectedResult,
                    @Param("validFlag") String validFlag, @Param("menuSheetId") String menuSheetId,
                    @Param("assertId") String assertId, @Param("userNameEn") String userNameEn,
                    @Param("time") String time) throws Exception;
}