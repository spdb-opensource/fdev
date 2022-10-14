package com.spdb.fdev.spdb.service;

import com.spdb.fdev.spdb.entity.EntityTemplate;

import java.util.List;
import java.util.Map;

public interface IEntityTemplateService {

    void add(Map<String,Object> req) throws Exception;

    List<EntityTemplate> queryList(Map<String,Object> req) throws Exception;

    EntityTemplate queryById(Map<String,Object> req) throws Exception;
}
