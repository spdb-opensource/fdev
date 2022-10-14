package com.spdb.fdev.spdb.dao.impl;


import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.spdb.dao.IServiceConfigDao;
import com.spdb.fdev.spdb.entity.ServiceConfig;

import net.sf.json.JSONObject;

/**
 * @author huyz
 * @description  ServiceConfigDaoImpl
 * @date 2021/6/1
 */
@Repository
public class ServiceConfigDaoImpl implements IServiceConfigDao {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<ServiceConfig> queryByServiceGitId(Map<String, Object> paramMap) {
		String serviceGitId = (String) paramMap.get(Dict.SERVICEGITID);
	    String branch =   (String) paramMap.get(Dict.BRANCH);
	    Criteria criteria = new Criteria();
	    if(!CommonUtil.isNullOrEmpty(serviceGitId)) {
			criteria.and(Dict.SERVICEGITID).is(serviceGitId);
		}
		if(!CommonUtil.isNullOrEmpty(branch)) {
			criteria.and(Dict.BRANCH).is(branch);
		}
		Query query = new Query();
		query.addCriteria(criteria);
		List<ServiceConfig> list = mongoTemplate.find(query, ServiceConfig.class);
		return list;
	}

	@Override
	public void addServiceConfig(ServiceConfig serviceConfig) {
		 mongoTemplate.save(serviceConfig);
		
	}

	@Override
	public void updateServiceConfig(ServiceConfig serviceConfig) throws Exception {
		Query query = Query.query(Criteria.where(Dict.SERVICEGITID).is(serviceConfig.getServiceGitId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(serviceConfig);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
		Update update = Update.update(Dict.SERVICEGITID, serviceConfig.getServiceGitId());
	    while (it.hasNext()) {
	        String key = (String) it.next();
	        Object value = pJson.get(key);
	        if (Dict.OBJECTID.equals(key)) {
	            continue;
	        }
	        update.set(key, value);
	    }
	    mongoTemplate.findAndModify(query, update,ServiceConfig.class); 
	}
	
	@Override
	public List<ServiceConfig> queryServiceConfig(Map<String, Object> requestParam) {
		String entityNameEn = (String) requestParam.get(Dict.ENTITYNAMEEN);//实体英文名
		List<String> fields =   (List<String>) requestParam.get(Dict.FIELDS);//修改字段列表
		String delete = (String) requestParam.get(Dict.DELETE);//逻辑删除标记
		Integer page = (Integer) requestParam.get(Dict.PAGE);//页码
        Integer perPage = (Integer) requestParam.get(Dict.PERPAGE);//每页条数
	    Criteria elemCriteria = new Criteria();
        elemCriteria.and(Dict.ENTITYNAMEEN).is(entityNameEn);
        if (!CommonUtil.isNullOrEmpty(fields)) {
            elemCriteria.and(Dict.FIELDS).in(fields);
        }
        Criteria criteria = new Criteria();
        criteria.and(Dict.ENTITYFIELD).elemMatch(elemCriteria);
        if(CommonUtil.isNullOrEmpty(delete)) {
        	criteria.and(Dict.DELETE).ne(Constants.YES);
        }
		Query query = new Query();
		if( !CommonUtil.isNullOrEmpty(perPage) && perPage != 0 ) {
			query.skip((page - 1L) * perPage).limit(perPage);
		}
		query.addCriteria(criteria);
		List<ServiceConfig> list = mongoTemplate.find(query, ServiceConfig.class);
		return list;
	}
	
	@Override
	public Long queryServiceConfigCount(Map<String, Object> requestParam) {
		String entityNameEn = (String) requestParam.get(Dict.ENTITYNAMEEN);
		List<String> fields =   (List<String>) requestParam.get(Dict.FIELDS);
		String delete = (String) requestParam.get(Dict.DELETE);//逻辑删除标记
	    Criteria elemCriteria = new Criteria();
        elemCriteria.and(Dict.ENTITYNAMEEN).is(entityNameEn);
        if (!CommonUtil.isNullOrEmpty(fields)) {
            elemCriteria.and(Dict.FIELDS).in(fields);
        }
        Criteria criteria = new Criteria();
        criteria.and(Dict.ENTITYFIELD).elemMatch(elemCriteria);
        if(CommonUtil.isNullOrEmpty(delete)) {
        	criteria.and(Dict.DELETE).ne(Constants.YES);
        }
		Query query = new Query();
		query.addCriteria(criteria);
		Long count = mongoTemplate.count(query, ServiceConfig.class);
		return count;
	}

	@Override
	public void deleteServiceConfig(Map<String, Object> requestParam) {
		String serviceGitId = String.valueOf(requestParam.get(Dict.GITLABID));
		Query query = new Query(Criteria.where(Dict.SERVICEGITID).is(serviceGitId));
        Update update = new Update();
        update.set(Dict.DELETE, Constants.YES);
        mongoTemplate.findAndModify(query, update, ServiceConfig.class);
	}

}
