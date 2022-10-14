package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.dao.IComponentArchetypeDao;
import com.spdb.fdev.component.entity.ComponentArchetype;
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
public class ComponentArchetypeDaoImpl implements IComponentArchetypeDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<ComponentArchetype> query(ComponentArchetype componentArchetype) throws JsonProcessingException {
        List<ComponentArchetype> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = componentArchetype == null ? "{}" : objectMapper.writeValueAsString(componentArchetype);
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
        AggregationResults<ComponentArchetype> docs = mongoTemplate.aggregate(Aggregation.newAggregation(opetations), Dict.COMPONENT_ARCHETYPE, ComponentArchetype.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public ComponentArchetype save(ComponentArchetype componentArchetype) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        componentArchetype.set_id(objectId);
        componentArchetype.setId(id);
        return mongoTemplate.save(componentArchetype);
    }

    @Override
    public ComponentArchetype update(ComponentArchetype componentArchetype) throws JsonProcessingException {
        Query query = Query.query(Criteria.where(Dict.ID).is(componentArchetype.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(componentArchetype);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, componentArchetype.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, ComponentArchetype.class);
        return this.mongoTemplate.findOne(query, ComponentArchetype.class);
    }

    @Override
    public void delete(ComponentArchetype componentArchetype) {
        Query query = new Query(Criteria.where(Dict.ID).is(componentArchetype.getId()));
        mongoTemplate.findAndRemove(query, ComponentArchetype.class);
    }

    @Override
    public ComponentArchetype queryByArchetypeIdAndAppId(ComponentArchetype componentArchetype) {
        Query query = new Query();
        query.fields().exclude(Dict.OBJECTID);
        query.addCriteria(Criteria.where(Dict.ARCHETYPE_ID).is(componentArchetype.getArchetype_id()).and(Dict.ARCHETYPE_VERSION).is(componentArchetype.getArchetype_version()).and(Dict.COMPONENT_ID).is(componentArchetype.getComponent_id()));
        return mongoTemplate.findOne(query, ComponentArchetype.class);
    }

    @Override
    public List<ComponentArchetype> queryByArchetypeIdAndVersion(String archetypeId, String version) {
        Query query = new Query();
        query.fields().exclude(Dict.OBJECTID);
        query.addCriteria(Criteria.where(Dict.ARCHETYPE_ID).is(archetypeId).and(Dict.ARCHETYPE_VERSION).is(version));
        return mongoTemplate.find(query, ComponentArchetype.class);
    }

    @Override
    public List<ComponentArchetype> queryByArcIdAndVersionCopId(String archetypeId, String version, String componentId) {
        Query query = new Query();
        query.fields().exclude(Dict.OBJECTID);
        query.addCriteria(Criteria.where(Dict.ARCHETYPE_ID).is(archetypeId).and(Dict.ARCHETYPE_VERSION).is(version).and(Dict.COMPONENT_ID).is(componentId));
        return mongoTemplate.find(query, ComponentArchetype.class);
    }
}
