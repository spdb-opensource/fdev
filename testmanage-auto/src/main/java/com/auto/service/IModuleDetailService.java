package com.auto.service;

import com.auto.entity.ModuleDetail;

import java.util.List;
import java.util.Map;

public interface IModuleDetailService {

    void addModuleDetail(ModuleDetail moduleDetail) throws Exception;

    List<Map<String, String>> queryModuleDetail(Map<String, String> map) throws Exception;
    
    List<Map<String, String>> queryModuleDetailByModuleId(String moduleId) throws Exception;

    void deleteModuleDetail(Map<String, String[]> map) throws Exception;

    void updateModuleDetail(Map<String, String> map) throws Exception;
}
