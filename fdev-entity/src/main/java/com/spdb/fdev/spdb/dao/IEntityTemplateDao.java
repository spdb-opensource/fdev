package com.spdb.fdev.spdb.dao;

import com.spdb.fdev.spdb.entity.EntityTemplate;

import java.util.List;

public interface IEntityTemplateDao {

    void save(List<EntityTemplate> templates) throws Exception;

    List<EntityTemplate> queryList(String likeKey) throws Exception;

    EntityTemplate queryById(String id) throws Exception;
}
