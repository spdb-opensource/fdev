package com.spdb.fdev.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.spdb.dao.IEntityDao;
import com.spdb.fdev.spdb.entity.Entity;
import com.spdb.fdev.spdb.entity.EntityLog;
import com.spdb.fdev.spdb.entity.HistoryDetails;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author huyz
 * @description  IEntityDao
 * @date 2021/5/7
 */
@Repository
public class EntityDaoImpl implements IEntityDao {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public Map<String , Object> queryEntityModel(Map<String, Object> requestParam) {

        String createUserId = (String) requestParam.get(Dict.CREATEUSERID);//实体创建人
        String nameEn = (String) requestParam.get(Dict.NAMEEN);//实体英文名　支持模糊搜索
        String nameCn = (String) requestParam.get(Dict.NAMECN);//实体中文名 支持模糊搜索
        String templateId = (String) requestParam.get(Dict.TEMPLATEID);//实体模板
        Integer page = (Integer) requestParam.get(Dict.PAGE);//页码
        Integer perPage = (Integer) requestParam.get(Dict.PERPAGE);//每页条数
		Criteria criteria = new Criteria();
		criteria.and(Dict.DELETE).ne(Constants.YES);
		if(!CommonUtil.isNullOrEmpty(createUserId)) {
			criteria.and(Constants.CREATEUSERUSERID).is(createUserId);
		}
		if(!CommonUtil.isNullOrEmpty(nameEn)) {
			Pattern pattern = Pattern.compile("^.*" + nameEn + ".*$", Pattern.CASE_INSENSITIVE);
			criteria.and(Dict.NAMEEN).regex(pattern);
		}
		if(!CommonUtil.isNullOrEmpty(nameCn)) {
			Pattern pattern = Pattern.compile("^.*" + nameCn + ".*$", Pattern.CASE_INSENSITIVE);
			criteria.and(Dict.NAMECN).regex(pattern);
		}
		if(!CommonUtil.isNullOrEmpty(templateId)) {
			if(Constants.EMPTY.equals(templateId.trim())) {
				criteria.orOperator(Criteria.where(Dict.TEMPLATEID).is(""),
		                Criteria.where(Dict.TEMPLATEID).is(null));
			}else {
				criteria.and(Dict.TEMPLATEID).is(templateId);
			}
		}
		Query query = new Query();
		query.addCriteria(criteria);
		Long total = mongoTemplate.count(query, Entity.class);
		if( !CommonUtil.isNullOrEmpty(perPage) && perPage != 0 ) {
			query.skip((page - 1L) * perPage).limit(perPage);
		}
		query.collation(Collation.of(Constants.EN).strength(Collation.ComparisonLevel.secondary()));
		query.with(new Sort(Sort.Direction.ASC, Dict.NAMEEN));
		List<Entity> list = mongoTemplate.find(query, Entity.class);
		Map<String , Object> map = new HashMap<String , Object>();
		map.put(Dict.COUNT, total);
		map.put(Dict.ENTITYMODELLIST, list);
		return map;
	}
	
	@Override
	public Entity queryEntityModelById(String id) {
		Criteria criteria = new Criteria();
		if(!CommonUtil.isNullOrEmpty(id)) {
			criteria.and(Dict.ID).is(id);
		}
		Query query = new Query();
		query.addCriteria(criteria);
		Entity entity = mongoTemplate.findOne(query, Entity.class);
		return entity;
	}
	@Override
	public void addEntityModel(Entity entity) {
        mongoTemplate.save(entity);
	}

	@Override
	public Entity updateEntityModel(Entity entity) throws Exception {
		Query query = Query.query(Criteria.where(Dict.ID).is(entity.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(entity);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
		Update update = Update.update(Dict.ID, entity.getId());
	    while (it.hasNext()) {
	        String key = (String) it.next();
	        Object value = pJson.get(key);
	        if (Dict.OBJECTID.equals(key)) {
	            continue;
	        }
	        update.set(key, value);
	    }
	    
		return mongoTemplate.findAndModify(query, update, Entity.class);
	}

	@Override
	public Entity queryOneEntityModel(Map<String, Object> requestParam) throws Exception {
		String id = (String) requestParam.get(Dict.ID);//id
        String createUserId = (String) requestParam.get(Dict.CREATEUSERID);//实体创建人
        String nameEn = (String) requestParam.get(Dict.NAMEEN);//实体英文名　
        String nameCn = (String) requestParam.get(Dict.NAMECN);//实体中文名 
        String templateId = (String) requestParam.get(Dict.TEMPLATEID);//实体模板
		Criteria criteria = new Criteria();
		criteria.and(Dict.DELETE).ne(Constants.YES);
		if(!CommonUtil.isNullOrEmpty(id)) {
			criteria.and(Dict.ID).is(id);
		}
		if(!CommonUtil.isNullOrEmpty(createUserId)) {
			criteria.and(Constants.CREATEUSERUSERID).is(createUserId);
		}
		if(!CommonUtil.isNullOrEmpty(nameEn)) {
			criteria.and(Dict.NAMEEN).is(nameEn);
		}
		if(!CommonUtil.isNullOrEmpty(nameCn)) {
			criteria.and(Dict.NAMECN).is(nameCn);
		}
		if(!CommonUtil.isNullOrEmpty(templateId)) {
			criteria.and(Dict.TEMPLATEID).is(templateId);
		}
		Query query = new Query();
		query.addCriteria(criteria);
		return mongoTemplate.findOne(query, Entity.class);
	}
	
	@Override
	public List<Entity> queryEntityModelByIds(List<String> entityIdList) {
		return mongoTemplate.find(new Query(Criteria.where(Dict.ID).in(entityIdList)), Entity.class);
	}

	@Override
	public Map queryServiceEntity(String templateId, Integer page, Integer perPage) {
		Criteria criteria = new Criteria();
		criteria.and(Dict.DELETE).ne(Constants.YES);

		if(!CommonUtil.isNullOrEmpty(templateId)) {
			//查询自定义实体
			if(Constants.EMPTY.equals(templateId.trim())) {
				criteria.orOperator(Criteria.where(Dict.TEMPLATEID).is(""),
		                Criteria.where(Dict.TEMPLATEID).is(null));
			}else {
				criteria.and(Dict.TEMPLATEID).is(templateId);
			}
		}
		Query query = new Query();
		query.addCriteria(criteria);
		Long total = mongoTemplate.count(query, Entity.class);
		if( !CommonUtil.isNullOrEmpty(perPage) && perPage != 0 ) {
			query.skip((page - 1L) * perPage).limit(perPage);
		}
		query.collation(Collation.of(Constants.EN).strength(Collation.ComparisonLevel.secondary()));
		query.with(new Sort(Sort.Direction.ASC, Dict.NAMEEN));
		List<Entity> list = mongoTemplate.find(query, Entity.class);
		Map<String , Object> map = new HashMap<String , Object>();
		map.put(Dict.COUNT, total);
		map.put(Dict.ENTITYMODELLIST, list);
		return map;
	}

    @Override
    public List<Entity> queryByNameEnSet(Set<String> entityEnSet) {
		Query query = Query.query(Criteria.where(Dict.NAMEEN).in(entityEnSet));
		List<Entity> entityList= this.mongoTemplate.find(query, Entity.class);
	    return entityList;
    }

	@Override
	public Map<String, Object> queryHistoryDetails(Map<String, Object> requestParam) {
		String updateUserId = (String) requestParam.get(Dict.UPDATEUSERID);//更新人ID
        String entityId = (String) requestParam.get(Dict.ENTITYID);//实体id
        List<String> envNames = (List<String>) requestParam.get(Dict.ENVNAMES);//环境英文名
        Integer page = (Integer) requestParam.get(Dict.PAGE);//页码
        Integer perPage = (Integer) requestParam.get(Dict.PERPAGE);//每页条数
		Criteria criteria = new Criteria();
		if(!CommonUtil.isNullOrEmpty(updateUserId)) {
			criteria.and(Dict.UPDATEUSERID).is(updateUserId);
		}
		if(!CommonUtil.isNullOrEmpty(entityId)) {
			criteria.and(Dict.ENTITYID).is(entityId);
		}
		if(!CommonUtil.isNullOrEmpty(envNames)) {
			criteria.and(Dict.ENVNAME).in(envNames);
		}
		Query query = new Query();
		query.addCriteria(criteria);
		Long total = mongoTemplate.count(query, HistoryDetails.class);
		if( !CommonUtil.isNullOrEmpty(perPage) && perPage != 0 ) {
			query.skip((page - 1L) * perPage).limit(perPage);
		}
		query.with(new Sort(Sort.Direction.DESC, Dict.UPDATETIME));
		List<HistoryDetails> list = mongoTemplate.find(query, HistoryDetails.class);
		Map<String , Object> map = new HashMap<String , Object>();
		map.put(Dict.COUNT, total);
		map.put(Dict.HISTORYLIST, list);
		return map;
	}
	
	@Override
	public void addHistoryDetails(HistoryDetails historyDetails) {
        mongoTemplate.save(historyDetails);
	}

	@Override
	public void deleteEntity(Entity entity){

		entity.setDelete(Constants.YES);
		mongoTemplate.save(entity);
	}

	@Override
	public void addEntityLog(EntityLog entityLog){
		mongoTemplate.save(entityLog);
	}

	@Override
	public Map<String, Object> queryEntityLog(Map<String, Object> requestParam) {
		String entityId = (String) requestParam.get(Dict.ENTITYID);//实体ID
		Integer page = (Integer) requestParam.get(Dict.PAGE);//页码
		Integer perPage = (Integer) requestParam.get(Dict.PERPAGE);//每页条数
		Criteria criteria = new Criteria();
		criteria.and(Dict.ENTITYID).is(entityId);
		Query query = new Query();
		query.addCriteria(criteria);
		Long total = mongoTemplate.count(query, EntityLog.class);
		if( !CommonUtil.isNullOrEmpty(perPage) && perPage != 0 ) {
			query.skip((page - 1L) * perPage).limit(perPage);
		}
		query.with(new Sort(Sort.Direction.DESC, Dict.UPDATETIME));
		List<EntityLog> list = mongoTemplate.find(query, EntityLog.class);
		Map<String , Object> map = new HashMap<String , Object>();
		map.put(Dict.COUNT, total);
		map.put(Dict.ENTITYLOGLIST, list);
		return map;
	}
}
