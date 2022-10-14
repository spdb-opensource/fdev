package com.spdb.fdev.fdevapp.spdb.dao.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import com.spdb.fdev.fdevapp.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.fdevapp.spdb.dao.IGitlabCIDao;
import com.spdb.fdev.fdevapp.spdb.entity.GitlabCI;

import net.sf.json.JSONObject;
@Repository
public class GitlabCIDaoImpl implements IGitlabCIDao {

	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public GitlabCI save(GitlabCI gitlabci) throws Exception {
		ObjectId objectId = new ObjectId();
		String id = objectId.toString();
		gitlabci.set_id(objectId);
		gitlabci.setId(id);
		return mongoTemplate.save(gitlabci);
	}

	@Override
	public List<GitlabCI> query(GitlabCI gitlabci) throws Exception {
		List<GitlabCI> result = new ArrayList<GitlabCI>();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		String json = gitlabci == null ? "{}" : objectMapper.writeValueAsString(gitlabci);
		BasicDBObject queryJson = BasicDBObject.parse(json);

		Iterator<String> it = queryJson.keySet().iterator();
		Criteria c = new Criteria();
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = queryJson.get(key);
			c.and(key).is(value);
		}
		AggregationOperation match = Aggregation.match(c);
		AggregationResults<GitlabCI> docs = mongoTemplate.aggregate(Aggregation.newAggregation(match),
				Dict.APP_GITLABCI, GitlabCI.class);
		docs.forEach(result::add);
		return result;
	}

	@Override
	public GitlabCI update(GitlabCI gitlabci) throws Exception {
		Query query = Query.query(Criteria.where(Dict.ID).is(gitlabci.getId()));

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		String json = objectMapper.writeValueAsString(gitlabci);
		JSONObject pJson = JSONObject.fromObject(json);
		Iterator<Object> it = pJson.keys();

		Update update = Update.update(Dict.ID, gitlabci.getId());
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = pJson.get(key);
			update.set(key, value);
		}
		return mongoTemplate.findAndModify(query, update, GitlabCI.class);
	}

	@Override
	public GitlabCI findById(String id) throws Exception {
		List<GitlabCI> list = mongoTemplate.find(Query.query(Criteria.where(Dict.ID).is(id)),
				GitlabCI.class);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public GitlabCI findByName(String name) throws Exception {
		List<GitlabCI> list = mongoTemplate.find(Query.query(Criteria.where(Dict.YAML_NAME).is(name)),
				GitlabCI.class);
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

}
