package com.spdb.fdev.spdb.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.spdb.entity.Entity;
import com.spdb.fdev.spdb.entity.EntityLog;
import com.spdb.fdev.spdb.entity.HistoryDetails;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IEntityDao {

	Map queryEntityModel(Map<String, Object> requestParam);

	void addEntityModel(Entity entity);

	Entity queryEntityModelById(String id);

	Entity updateEntityModel(Entity entity) throws Exception ;

	Entity queryOneEntityModel(Map<String, Object> requestParam) throws Exception ;

	List<Entity> queryEntityModelByIds(List<String> entityIdList);

    Map queryServiceEntity(String templateId, Integer page, Integer perPage);

	List<Entity> queryByNameEnSet(Set<String> entityEnSet);

	Map<String, Object> queryHistoryDetails(Map<String, Object> requestParam);

	void addHistoryDetails(HistoryDetails historyDetails);

    void deleteEntity(Entity entity) throws JsonProcessingException;

    void addEntityLog(EntityLog entityLog);

	Map<String, Object> queryEntityLog(Map<String, Object> requestParam);
}
