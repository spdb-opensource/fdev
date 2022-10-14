package com.spdb.fdev.fdevtask.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.fdevtask.spdb.dao.MateDataDao;
import com.spdb.fdev.fdevtask.spdb.entity.MateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Iterator;

@Repository
public class MateDataDaoImpl implements MateDataDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public MateData queryMateDataByType(String type) {
       return mongoTemplate.findOne(Query.query(Criteria.where("type").is(type)), MateData.class);
    }

    @Override
    public MateData addMateDataByType(MateData mateData) {
        return mongoTemplate.insert(mateData);
    }

    @Override
    public MateData queryMateDataByTaskId(String taskId) {
        return mongoTemplate.findOne(Query.query(Criteria.where("id").is(taskId)), MateData.class);
    }

    @Override
    public MateData updateMateDataByType(MateData mateData) {
        String id = mateData.getId();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = null;
        try {
            json = objectMapper.writeValueAsString(mateData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        BasicDBObject taskJson = BasicDBObject.parse(json);
        Iterator<String> it = taskJson.keySet().iterator();
        Query query = Query.query(Criteria.where("id").is(id));
        Update update = Update.update("id", id);
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = taskJson.get(key);
            if ("_id".equals(key) || "serialVersionUID".equals(key)) {
                continue;
            }
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, MateData.class);
        return mongoTemplate.findOne(query, MateData.class);
    }
}
