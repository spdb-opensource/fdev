package com.spdb.fdev.fdevenvconfig.spdb.service;

import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelSet;

import java.util.List;
import java.util.Map;

public interface ModelSetService {

    List<Map<String, Object>> getModelSetTemplate();

    List<Map<String, Object>> getModles(Map<String, Object> requestMap);

    void saveModelSet(Map<String, Object> requestMap) throws Exception;

    Map<String, Object> listModelSetsByPage(Map<String, Object> requestMap);

    void updateModelSet(Map<String, Object> requestMap) throws Exception;

    void deleteModelSet(Map<String, Object> requestMap) throws Exception;

    ModelSet queryById(String id);

    ModelSet queryByName(String name) throws Exception;

    List<Model> queryTemplateContainsModel();
}
