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
import com.spdb.fdev.fdevapp.spdb.dao.IDeployNetDao;
import com.spdb.fdev.fdevapp.spdb.entity.DeployNet;

import net.sf.json.JSONObject;

@Repository
public class DeployNetDaoImpl implements IDeployNetDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public DeployNet save(DeployNet deployNet) throws Exception {

        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        deployNet.set_id(objectId);
        deployNet.setId(id);

        return mongoTemplate.save(deployNet);

    }

    @Override
    public List<DeployNet> query(DeployNet deployNet) throws Exception {
        List<DeployNet> result = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = deployNet == null ? "{}" : objectMapper.writeValueAsString(deployNet);

        BasicDBObject queryJson = BasicDBObject.parse(json);

        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<DeployNet> docs = mongoTemplate.aggregate(Aggregation.newAggregation(match), "app-deploy-net", DeployNet.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public DeployNet update(DeployNet deployNet) throws Exception {

        Query query = Query.query(Criteria.where(Dict.ID).is(deployNet.getId()));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(deployNet);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();

        Update update = Update.update(Dict.ID, deployNet.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        return mongoTemplate.findAndModify(query, update, DeployNet.class);

    }

    @Override
    public DeployNet findById(String id) throws Exception {
        List<DeployNet> list = mongoTemplate.find(Query.query(Criteria.where(Dict.ID).is(id)), DeployNet.class);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}
