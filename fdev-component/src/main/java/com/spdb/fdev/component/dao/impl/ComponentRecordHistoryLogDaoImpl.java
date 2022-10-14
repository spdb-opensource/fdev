package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.dao.IComponentRecordHistoryLogDao;
import com.spdb.fdev.component.entity.ComponentRecordHistoryLog;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ComponentRecordHistoryLogDaoImpl implements IComponentRecordHistoryLogDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List query(ComponentRecordHistoryLog componentRecordHistoryLog) throws Exception {
        List<ComponentRecordHistoryLog> result = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = componentRecordHistoryLog == null ? "{}" : objectMapper.writeValueAsString(componentRecordHistoryLog);

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
        AggregationResults<ComponentRecordHistoryLog> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.COMPONENT_RECORD, ComponentRecordHistoryLog.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public List queryByComponentId(ComponentRecordHistoryLog componentRecordHistoryLog) {
        Criteria c = new Criteria();
        if (StringUtils.isNotBlank(componentRecordHistoryLog.getComponent_id())) {
            c.and(Dict.COMPONENT_ID).is(componentRecordHistoryLog.getComponent_id());
        }
        Query query = new Query(c);
        return mongoTemplate.find(query, ComponentRecordHistoryLog.class);
    }


    @Override
    public ComponentRecordHistoryLog save(ComponentRecordHistoryLog componentRecordHistoryLog) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        componentRecordHistoryLog.set_id(objectId);
        componentRecordHistoryLog.setId(id);
        return mongoTemplate.save(componentRecordHistoryLog);
    }

}
