package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.dao.IApplicationArchetypeDao;
import com.spdb.fdev.component.entity.ApplicationArchetype;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class ApplicationArchetypeDaoImpl implements IApplicationArchetypeDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<ApplicationArchetype> query(ApplicationArchetype applicationArchetype) throws JsonProcessingException {
        List<ApplicationArchetype> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = applicationArchetype == null ? "{}" : objectMapper.writeValueAsString(applicationArchetype);
        BasicDBObject queryJson = BasicDBObject.parse(json);
        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation project = Aggregation.project().andExclude(Dict.OBJECTID);
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<ApplicationArchetype> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.APPLICATION_ARCHETYPE, ApplicationArchetype.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public ApplicationArchetype save(ApplicationArchetype applicationArchetype) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        applicationArchetype.set_id(objectId);
        applicationArchetype.setId(id);
        return mongoTemplate.save(applicationArchetype);
    }

    @Override
    public ApplicationArchetype update(ApplicationArchetype applicationArchetype) throws JsonProcessingException {
        Query query = Query.query(Criteria.where(Dict.ID).is(applicationArchetype.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(applicationArchetype);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, applicationArchetype.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, ApplicationArchetype.class);
        return this.mongoTemplate.findOne(query, ApplicationArchetype.class);
    }

    @Override
    public ApplicationArchetype queryByArchetypeIdAndAppId(ApplicationArchetype applicationArchetype) {
        Query query = new Query();
        query.fields().exclude(Dict.OBJECTID);
        query.addCriteria(Criteria.where(Dict.APPLICATION_ID).is(applicationArchetype.getApplication_id()).and(Dict.ARCHETYPE_ID).is(applicationArchetype.getArchetype_id()));
        return mongoTemplate.findOne(query, ApplicationArchetype.class);
    }

	@Override
	public void delete(ApplicationArchetype applicationArchetype) {
		Query query = new Query(Criteria.where(Dict.ID).is(applicationArchetype.getId()));
        mongoTemplate.findAndRemove(query, ApplicationArchetype.class);
	}

    @Override
    public void deleteByAppId(String id) {
        Query query = new Query(Criteria.where(Dict.APPLICATION_ID).is(id));
        mongoTemplate.findAndRemove(query, ApplicationArchetype.class);
    }
}
