package com.spdb.fdev.release.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IProdTemplateDao;
import com.spdb.fdev.release.entity.ProdTemplate;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class ProdTemplateDaoImpl implements IProdTemplateDao{
	@Resource
	private MongoTemplate mongoTemplate;
	
	@Override
	public ProdTemplate create(ProdTemplate releaseTemplate) throws Exception{
		ObjectId id = new ObjectId();
		releaseTemplate.set_id(id);
		releaseTemplate.setId(id.toString());
		releaseTemplate.setCreate_time(TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
		return mongoTemplate.save(releaseTemplate);
	}

	@Override
	public List<ProdTemplate> query(ProdTemplate releaseTemplate)throws Exception {
		   List<ProdTemplate> result = new ArrayList<>();
	        ObjectMapper objectMapper = new ObjectMapper();
	        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
	        String json = releaseTemplate == null ? "{}" : objectMapper.writeValueAsString(releaseTemplate);
	        BasicDBObject taskJson = BasicDBObject.parse(json);
	        Iterator<String> it = taskJson.keySet().iterator();
	        Criteria c = new Criteria();
	        while (it.hasNext()) {
	            String key = it.next();
	            Object value = taskJson.get(key);
	            if(Dict.OWNER_GROUP.equals(key)) {
					c.and(Dict.OWNER_GROUP).in(((String)value).split(","));
				} else {
					c.and(key).is(value);
				}
	        }	      	        
	        c.and(Dict.STATUS).ne(Constants.TEMPLATE_STATUS_CANCEL);
	        AggregationOperation project = Aggregation.project().andExclude(Dict.OBJECTID);
	        AggregationOperation match = Aggregation.match(c);
	        AggregationResults<ProdTemplate> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), "prod_template", ProdTemplate.class);
	        docs.forEach(doc ->result.add(doc));
	        return result;
	}

	@Override
	public ProdTemplate queryDetail(ProdTemplate releaseTemplate) throws Exception{
		List<ProdTemplate> list = query(releaseTemplate);
		if(CommonUtils.isNullOrEmpty(list))
		{
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[] { "模版不存在!" });
		}
		return list.get(0);
	}

	@Override
	public ProdTemplate update(ProdTemplate releaseTemplate) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
		String json = objectMapper.writeValueAsString(releaseTemplate);
		BasicDBObject taskJson = BasicDBObject.parse(json);
		Iterator<String> it = taskJson.keySet().iterator();
		Query query = Query.query(Criteria.where(Dict.ID).is(releaseTemplate.getId()));
		Update update = Update.update(Dict.ID, releaseTemplate.getId());

		while (it.hasNext()) {
			String key = it.next();
			Object value = taskJson.get(key);
			if (Dict.OBJECTID.equals(key) || "serialVersionUID".equals(key)) {
				continue;
			}
			update.set(key, value);
		}
		mongoTemplate.findAndModify(query, update, ProdTemplate.class);
		return mongoTemplate.findOne(query, ProdTemplate.class);
	}

	@Override
	public boolean delete(String tempId) throws Exception{
		Query query = new Query(Criteria.where(Dict.ID).is(tempId));
		mongoTemplate.findAndRemove(query, ProdTemplate.class);
		return true;
	}

	@Override
	public void cancel(String template_id) throws Exception {
		Query query = new Query(Criteria.where(Dict.ID).is(template_id));
		Update update = Update.update(Dict.STATUS, Constants.TEMPLATE_STATUS_CANCEL);
		mongoTemplate.findAndModify(query, update, ProdTemplate.class);
	}

	@Override
	public ProdTemplate queryDetailById(String template_id) {
		Query query = new Query(Criteria.where(Dict.ID).is(template_id));
		query.fields().exclude(Dict.OBJECTID);
		return mongoTemplate.findOne(query, ProdTemplate.class);
	}

	@Override
	public List<ProdTemplate> findExists(ProdTemplate prodTemplate) {
		Query query = new Query(Criteria.where(Dict.OWNER_GROUP).is(prodTemplate.getOwner_group())
				.and(Dict.OWNER_SYSTEM).is(prodTemplate.getOwner_system())
				.and(Dict.TEMPLATE_TYPE).is(prodTemplate.getTemplate_type())
				.and(Dict.STATUS).ne(Constants.TEMPLATE_STATUS_CANCEL));
		return mongoTemplate.find(query, ProdTemplate.class);
	}

	@Override
	public List<ProdTemplate> queryByGroupType(ProdTemplate prodTemplate) {
		Query query = new Query(Criteria.where(Dict.OWNER_GROUP).is(prodTemplate.getOwner_group())
				.and(Dict.TEMPLATE_TYPE).is(prodTemplate.getTemplate_type())
                .and(Dict.STATUS).ne(Constants.TEMPLATE_STATUS_CANCEL));
		return mongoTemplate.find(query, ProdTemplate.class);
	}

	@Override
	public List<ProdTemplate> queryList(String owner_group, String owner_system, String type) {
		Query query = new Query(Criteria.where(Dict.OWNER_GROUP).is(owner_group).and(Dict.OWNER_SYSTEM).is(owner_system)
				.and(Dict.TEMPLATE_TYPE).is(type).and(Dict.STATUS).ne(Constants.TEMPLATE_STATUS_CANCEL));
		return mongoTemplate.find(query, ProdTemplate.class);
	}

}
