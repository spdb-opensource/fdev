package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IAppModelDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class AppModelDaoImpl implements IAppModelDao {
    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public List<AppModel> query(AppModel appModel) throws Exception {
        List<AppModel> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = appModel == null ? "{}" : objectMapper.writeValueAsString(appModel);
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
        AggregationResults<AppModel> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), "app-model-rela", AppModel.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public AppModel add(AppModel appModel) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        appModel.set_id(objectId);
        appModel.setId(id);
        return mongoTemplate.save(appModel);
    }

}
