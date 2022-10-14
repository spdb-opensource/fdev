package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelSet;

import java.util.List;
import java.util.Map;

public interface ModelSetDao {

    void saveModelSet(ModelSet modelSet);

    Map<String, Object> listModelSetsByPage(String nameCn, String template,String id, int page, int perPage);

    void updateModelSet(String id, List<String> models, String opno);

    void deleteModelSet(String id, String opno);

    ModelSet getModelSetByName(String nameCn);

    ModelSet getModelSetByModels(List<String> models);

    ModelSet queryById(String id);

    ModelSet queryByName(String name);

    ModelSet queryByNameContAllStatus(String name);
}
