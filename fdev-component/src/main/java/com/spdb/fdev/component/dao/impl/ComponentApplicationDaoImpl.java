package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.dao.IComponentApplicationDao;
import com.spdb.fdev.component.entity.ComponentApplication;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
public class ComponentApplicationDaoImpl implements IComponentApplicationDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<ComponentApplication> query(ComponentApplication componentApplication) throws JsonProcessingException {
        List<ComponentApplication> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = componentApplication == null ? "{}" : objectMapper.writeValueAsString(componentApplication);
        BasicDBObject queryJson = BasicDBObject.parse(json);
        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        List<AggregationOperation> opetations = new ArrayList<>();
        opetations.add(Aggregation.project().andExclude(Dict.OBJECTID));
        opetations.add(Aggregation.match(c));
        opetations.add(Aggregation.sort(Sort.Direction.DESC, Dict.UPDATE_TIME));
        AggregationResults<ComponentApplication> docs = mongoTemplate.aggregate(Aggregation.newAggregation(opetations), Dict.COMPONENT_APPLICATION, ComponentApplication.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public ComponentApplication save(ComponentApplication componentApplication) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        componentApplication.set_id(objectId);
        componentApplication.setId(id);
        return mongoTemplate.save(componentApplication);
    }

    @Override
    public ComponentApplication update(ComponentApplication componentApplication) throws JsonProcessingException {
        Query query = Query.query(Criteria.where(Dict.ID).is(componentApplication.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(componentApplication);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, componentApplication.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, ComponentApplication.class);
        return this.mongoTemplate.findOne(query, ComponentApplication.class);
    }

    @Override
    public ComponentApplication queryByComponentIdAndAppId(ComponentApplication componentApplication) {
        Query query = new Query();
        query.fields().exclude(Dict.OBJECTID);
        query.addCriteria(Criteria.where(Dict.APPLICATION_ID).is(componentApplication.getApplication_id()).and(Dict.COMPONENT_ID).is(componentApplication.getComponent_id()));
        return mongoTemplate.findOne(query, ComponentApplication.class);
    }

    @Override
    public void delete(ComponentApplication componentApplication) {
        Query query = new Query(Criteria.where(Dict.ID).is(componentApplication.getId()));
        mongoTemplate.findAndRemove(query, ComponentApplication.class);
    }

    /**
     * 根据应用id 删除所有数据
     */
    @Override
    public void deleteAllByApplicationId(ComponentApplication componentApplication) {
        Query query = new Query(Criteria.where(Dict.APPLICATION_ID).is(componentApplication.getApplication_id()));
        mongoTemplate.findAllAndRemove(query, ComponentApplication.class);
    }

    /**
     * 根据组件id 删除所有数据
     */
    @Override
    public void deleteAllByComponentId(ComponentApplication componentApplication) {
        Query query = new Query(Criteria.where(Dict.COMPONENT_ID).is(componentApplication.getComponent_id()));
        mongoTemplate.findAllAndRemove(query, ComponentApplication.class);
    }
}
