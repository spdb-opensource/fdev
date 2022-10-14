package com.spdb.fdev.fdevapp.spdb.dao.impl;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.fdevapp.spdb.dao.IDomainDao;
import com.spdb.fdev.fdevapp.spdb.entity.DomainEntity;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class DomainDaoImpl implements IDomainDao {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public DomainEntity save(DomainEntity entity) throws Exception {
        ObjectId id = new ObjectId();
        entity.set_id(id);
        entity.setId(id.toString());
        return mongoTemplate.save(entity);
    }

    @Override
    public List<DomainEntity> query(DomainEntity entity) throws Exception {

        List<DomainEntity> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = entity == null ? "{}" : objectMapper.writeValueAsString(entity);
        BasicDBObject taskJson = BasicDBObject.parse(json);
        Iterator<String> it = taskJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = taskJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<DomainEntity> docs = mongoTemplate.aggregate(Aggregation.newAggregation(match), "domains",
                DomainEntity.class);
        docs.forEach(doc -> {
            result.add(doc);
        });
        return result;

    }
}
