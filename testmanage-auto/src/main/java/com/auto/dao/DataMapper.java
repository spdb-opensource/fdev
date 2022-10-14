package com.auto.dao;

import com.auto.entity.Data;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DataMapper {

    void addData(Data data) throws Exception;

    List<Map<String, String>> queryData(@Param("search") String search, @Param("valid") String valid) throws Exception;
    
    List<Map<String, String>> queryDataByLabel(@Param("label") String label, @Param("dataId") String dataId) throws Exception;
    
    List<Map<String, String>> queryDataByTestCaseNo(@Param("testcaseNo") String testcaseNo) throws Exception;
    
    List<Map<String, String>> queryDataByModuleId(@Param("moduleId") String moduleId) throws Exception;

    void deleteData(@Param("dataId") String dataId, @Param("userNameEn") String userNameEn, @Param("time") String time) throws Exception;

    void updateData(@Param("dataId")String dataId, @Param("label")String label,
                    @Param("userNameEn")String userNameEn, @Param("time")String time,
                    @Param("map")Map map) throws Exception;

    void clearData(@Param("dataId")String dataId, @Param("blankMap") Map blankMap) throws Exception;
}