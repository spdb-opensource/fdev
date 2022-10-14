package com.spdb.fdev.fdevenvconfig.spdb.service;

import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelEnv;

import java.util.List;
import java.util.Map;

public interface IModelEnvService {
    List<Map> query(ModelEnv modelEnv) throws Exception;

    ModelEnv add(Map modelEnv) throws Exception;

    ModelEnv update(Map<String, Object> map) throws Exception;

    void delete(Map map) throws Exception;

    Map<String, Object> queryEnvBySlug(Map<String, String> map) throws Exception;

    List<Map<String, Object>> queryEnvwithNameCnBySlug(String appId, List<Model> modelList, String envName) throws Exception;

    Map queryModelEnvByEnvNameEn(Map<String, String> map) throws Exception;

    List<Map> queryModelEnvByEnvIdOrName(Map<String, String> map) throws Exception;

    Map<String, Object> queryEnvMappingwithCn(Map requestMap) throws Exception;

    List<Map> queryVarByEnvAndType(Map requestMap) throws Exception;

    Map queryVarByLabelAndType(Map<String, String> requestMap) throws Exception;

    ModelEnv queryByEnvIdAndModelId(ModelEnv modelEnv) throws Exception;

    List<Map> queryModelEnvByModelNameEn(Map<String, Object> requestMap) throws Exception;

    String ciDecrypt(Map<String, String> map);

    Map<String, Object> returnModelKeyMap(String envId, List<Model> modelList) throws Exception;

    Map pageQuery(Map qmap) throws Exception;

    List<Map<String, Object>> queryModelEnvByValue(Map<String, Object> requestMap);

    Map<String, Object> getVariablesValue(Map<String, Object> requestParam) throws Exception;
}
