package com.spdb.fdev.fdevenvconfig.spdb.service;

import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelTemplate;

import java.util.Map;

/**
 *  @Author:        lisy26
 *  @Date:          2020/11/16 10:58
 *  @Description:
 *  @Param:
 *  @return:
 **/
public interface IModelTemplateService {

    //实体模板新增
    ModelTemplate add(ModelTemplate modelTemplate) throws Exception;

    //实体模板列表查询
    Map pageQuery(Map map) throws Exception;

    //实体模板详情查询
    ModelTemplate queryById(String id) throws Exception;
    
    //实体模板详情查询
    ModelTemplate queryByNameEn(String nameEn) throws Exception;


}
