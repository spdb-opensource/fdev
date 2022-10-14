package com.spdb.fdev.fdevapp.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.spdb.dao.IAppTypeDao;
import com.spdb.fdev.fdevapp.spdb.entity.AppType;
import net.sf.json.JSONObject;
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
public class AppTypeDaoImpl implements IAppTypeDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public AppType save(AppType appType) throws Exception {
        ObjectId oId = new ObjectId();
        appType.set_id(oId);
        appType.setId(oId.toString());
        return mongoTemplate.save(appType);
    }

    @Override
    public List<AppType> query(AppType appType) throws Exception {
        List<AppType> result = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = appType == null ? "{}" : objectMapper.writeValueAsString(appType);

        BasicDBObject queryJson = BasicDBObject.parse(json);

        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<AppType> docs = mongoTemplate.aggregate(Aggregation.newAggregation(match), "app_type",AppType.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public AppType update(AppType appType) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(appType.getId()));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(appType);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();

        Update update = Update.update(Dict.ID, appType.getId());
        while(it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        return mongoTemplate.findAndModify(query, update,  AppType.class);
    }

    @Override
    public AppType findById(String id) throws Exception {
        List <AppType> list = mongoTemplate.find(Query.query(Criteria.where(Dict.ID).is(id)), AppType.class);
        if (list != null && list.size() > 0){
            return list.get(0);
        }
        return null;
    }
}
