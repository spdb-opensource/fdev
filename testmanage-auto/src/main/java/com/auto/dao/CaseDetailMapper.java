package com.auto.dao;

import com.auto.entity.CaseDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CaseDetailMapper {

    void addCaseDetail(CaseDetail caseDetail) throws Exception;

    List<Map<String, String>> queryCaseDetail(@Param("search") String search, @Param("valid") String valid) throws Exception;
    
    List<Map<String, String>> queryCaseDetailByCaseNoAndStep(@Param("testcaseNo") String testcaseNo, @Param("stepNo") String stepNo, @Param("detailId") String detailId) throws Exception;
    
    List<Map<String, String>> queryCaseDetailByTestCaseNo(@Param("testcaseNo") String testcaseNo) throws Exception;

    void deleteCaseDetail(@Param("detailId") String detailId, @Param("userNameEn") String userNameEn, @Param("time") String time) throws Exception;

    void updateCaseDetail(@Param("detailId") String detailId, @Param("testcaseNo") String testcaseNo, @Param("stepNo") String stepNo,
                          @Param("moduleId") String moduleId, @Param("elementType") String elementType, @Param("assertId") String assertId,
                          @Param("elementId") String elementId, @Param("elementData") String elementData, @Param("exeTimes") String exeTimes,
                          @Param("userNameEn") String userNameEn, @Param("time") String time) throws Exception;
}