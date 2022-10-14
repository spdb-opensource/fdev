package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.AutomationEnv;

import java.util.List;
import java.util.Map;

public interface IAutomationEnvService {

    List<AutomationEnv> query();

    void addAutomationEnv(Map<String,Object> req) throws Exception;

    void update(Map<String,Object> req) throws Exception;

    void deleteById(String id);

    /**
     * 根据gitlabId,环境名，属性列表查询实体属性的值
     * */
    Map<String,Object> queryEnvParams(Map<String,Object> req) throws Exception;
}
