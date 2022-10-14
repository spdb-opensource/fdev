package com.spdb.fdev.fdevenvconfig.spdb.dao;


import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelTemplate;
import java.util.Map;

/**
 * @Author: lisy26
 * @date: 2020/11/16 10:21
 * @ClassName IModelTemplateDao
 * @Description
 **/
public interface IModelTemplateDao {

    //实体模板新增
    ModelTemplate add(ModelTemplate modelTemplate) throws Exception;

    //实体模板列表查询
    Map<String, Object> pageQuery(Map map) throws Exception;

    //实体模板详情查询
    ModelTemplate queryById(String id) ;
    
    //实体模板详情查询
    ModelTemplate queryByNameEn(String nameEn);
}
