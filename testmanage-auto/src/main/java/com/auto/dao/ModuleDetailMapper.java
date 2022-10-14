package com.auto.dao;

import com.auto.entity.ModuleDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ModuleDetailMapper {

    void addModuleDetail(ModuleDetail moduleDetail) throws Exception;

    List<Map<String, String>> queryModuleDetail(@Param("search") String search, @Param("valid") String valid) throws Exception;

    List<Map<String, String>> queryModuleDetailByIdAndStep(@Param("moduleId") String moduleId, @Param("elementStepNo") String elementStepNo, @Param("moduleDetailId") String moduleDetailId) throws Exception;
    
    List<Map<String, String>> queryModuleDetailByModuleId(@Param("moduleId") String moduleId) throws Exception;
    
    void deleteModuleDetail(@Param("module") String module, @Param("userNameEn") String userNameEn, @Param("time") String time) throws Exception;

    void updateModuleDetail(@Param("moduleDetailId") String moduleDetailId, @Param("moduleId") String moduleId,
                            @Param("elementStepNo") String elementStepNo, @Param("elementId") String elementId,
                            @Param("elementType") String elementType, @Param("elementData") String elementData,
                            @Param("exeTimes") String exeTimes, @Param("userNameEn") String userNameEn,
                            @Param("time") String time) throws Exception;
}