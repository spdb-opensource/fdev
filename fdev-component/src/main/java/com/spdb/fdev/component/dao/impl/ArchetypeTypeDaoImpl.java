package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.dao.IArchetypeTypeDao;
import com.spdb.fdev.component.entity.ArchetypeType;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class ArchetypeTypeDaoImpl implements IArchetypeTypeDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List query(ArchetypeType archetypeType) throws Exception {
        List<ArchetypeType> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = archetypeType == null ? "{}" : objectMapper.writeValueAsString(archetypeType);
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
        AggregationResults<ArchetypeType> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.ARCHETYPETYPE, ArchetypeType.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public ArchetypeType save(ArchetypeType archetypeType) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        archetypeType.set_id(objectId);
        archetypeType.setId(id);
        return mongoTemplate.save(archetypeType);
    }

    @Override
    public void delete(ArchetypeType archetypeType) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(archetypeType.getId()));
        mongoTemplate.findAndRemove(query, ArchetypeType.class);
    }
}
