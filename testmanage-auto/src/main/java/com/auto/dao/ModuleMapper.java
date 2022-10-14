package com.auto.dao;

import com.auto.entity.Module;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ModuleMapper {

    void addModule(Module module) throws Exception;

    List<Map<String, String>> queryModule(@Param("search") String search, @Param("valid") String valid) throws Exception;
    
    List<Map<String, String>> queryModuleByNo(@Param("moduleNo") String moduleNo, @Param("moduleGroup") String moduleGroup, @Param("moduleName") String moduleName, @Param("moduleId") String moduleId) throws Exception;

    void deleteModule(@Param("module") String module, @Param("userNameEn") String userNameEn, @Param("time") String time) throws Exception;

    void updateModule(@Param("moduleId")String moduleId, @Param("moduleNo")String moduleNo,
                      @Param("moduleGroup")String moduleGroup, @Param("moduleName")String moduleName,
                      @Param("moduleNameCn")String moduleNameCn, @Param("userNameEn")String userNameEn,
                      @Param("time")String time) throws Exception;
}