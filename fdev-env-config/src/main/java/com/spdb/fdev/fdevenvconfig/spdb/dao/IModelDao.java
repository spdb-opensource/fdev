package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelCategory;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Scope;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xxx
 * @date 2019/7/5 13:15
 */
public interface IModelDao {
    List<Model> query(Model model) throws Exception;

    Model add(Model model);

    Model update(Model model) throws Exception;

    Model delete(Model model);

    Model queryById(Model model);

    Model queryById(String id);

    List<Model> queryByIdList(Set<String> list, String var, String value);

    /**
     * 根据 实体英文集合 查询实体的信息
     *
     * @param modelNameEnSet
     * @param status
     * @return
     * @throws Exception
     */
    List<Model> queryByNameEnSet(Set<String> modelNameEnSet, String status);

    List<Model> queryByIdList(Set<String> list, String type);

    ModelCategory queryModelCateCategory();

    Scope queryScope();

    Model queryVaribleForOne(String name_en, String Variable);

    List<Model> queryFuzz(Map paramMap, String statusOpen, Class<Model> modelClass);

    List<Model> getByModelIds(List<String> idList);

    List<Model> getModelBySecondCategory(String secondCategory);

    Model getByNameEn(String nameEn);

    List<Model> queryByNameEn(Set<String> modelSet);

    Map<String, Object> pageQuery(Map map);

    List<Model> queryBySecondcategoryList(Set<String> secodSets);

    /**
     * 获取CI实体列表或者非CI实体列表
     *
     * @param type
     * @return
     */
    List<Model> getModels(String type);

    List<Map> joinTemplateName(List<Model> modelList);

    Map joinTemplateName(Model model);
}
