package com.spdb.fdev.fdevenvconfig.spdb.service;

import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import org.codehaus.janino.Mod;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IModelService {
    List<Model> query(Model model) throws Exception;

    Model add(Model model) throws Exception;

    Model update(Model model) throws Exception;

    void delete(Map map) throws Exception;

    Model queryById(Model model) throws Exception;

    Model queryById(String id);

    Model getByNameEn(String nameEn);

    List<Model> queryByIdList(Set<String> modelIdList, String var, String value);

    List<Model> queryFuzz(Map map) throws Exception;

    Map<String, Object> queryModelCategory();

    Map queryModelVariables(Map requestMap);

    List<Model> queryExcludePirvateModel(Model model) throws Exception;

    void checkUseModel(String modelNameEn, String fieldNameEn);

    Map pageQuery(Map map);

    /**
     * type type=ci:获取CI实体列表 type=not_ci:获取非CI实体列表 type=""：全部实体列表
     *
     * @param
     * @return
     */
    List<Model> getModels(String type);

    List<Model> queryByNameEnSet(Set<String> modelNameEnSet, String status);

    List<Map> joinTemplateName(List<Model> modelList);

    List<Map> queryNoCiEnvKeyList() throws Exception;

    List<Model> configModel() throws Exception;
}
