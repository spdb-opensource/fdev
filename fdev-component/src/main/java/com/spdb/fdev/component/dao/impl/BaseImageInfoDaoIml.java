package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.dao.IBaseImageInfoDao;
import com.spdb.fdev.component.entity.BaseImageInfo;
import com.spdb.fdev.component.entity.MpassArchetype;
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
public class BaseImageInfoDaoIml implements IBaseImageInfoDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List query(BaseImageInfo baseImageInfo) throws Exception {
        List<BaseImageInfo> result = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = baseImageInfo == null ? "{}" : objectMapper.writeValueAsString(baseImageInfo);

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
        AggregationResults<BaseImageInfo> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.BASEIMAGE_INFO, BaseImageInfo.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public BaseImageInfo save(BaseImageInfo baseImageInfo) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        baseImageInfo.set_id(objectId);
        baseImageInfo.setId(id);
        return mongoTemplate.save(baseImageInfo);
    }

    @Override
    public BaseImageInfo update(BaseImageInfo baseImageInfo) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(baseImageInfo.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(baseImageInfo);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();

        Update update = Update.update(Dict.ID, baseImageInfo.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, BaseImageInfo.class);
        return this.mongoTemplate.findOne(query, BaseImageInfo.class);
    }

    @Override
    public BaseImageInfo queryById(String id) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        return mongoTemplate.findOne(query, BaseImageInfo.class);
    }

    @Override
    public BaseImageInfo queryByName(String name) {
        Query query = new Query(Criteria.where(Dict.NAME).is(name));
        return mongoTemplate.findOne(query, BaseImageInfo.class);
    }

    @Override
    public BaseImageInfo queryByGitlabUrl(String gitlabUrl) {
        Query query = new Query(Criteria.where(Dict.GITLAB_URL).is(gitlabUrl));
        return mongoTemplate.findOne(query, BaseImageInfo.class);
    }

    @Override
    public Integer queryDataByType(String start_date, String end_date) {
        Query query = new Query(Criteria.where(Dict.CREATE_DATE).lte(end_date));
        List<BaseImageInfo> BaseImageInfo = mongoTemplate.find(query, BaseImageInfo.class);
        return BaseImageInfo.size();
    }

    @Override
    public BaseImageInfo queryByGitlabId(String gitlabId) {
        Query query = new Query(Criteria.where(Dict.GITLAB_ID).is(gitlabId));
        return mongoTemplate.findOne(query, BaseImageInfo.class);
    }

    @Override
    public List<BaseImageInfo> getBaseImageByIds(List params) throws Exception{
        Criteria c = Criteria.where(Dict.ID).in(params);
        List<BaseImageInfo> baseImageList = this.mongoTemplate.find(Query.query(c), BaseImageInfo.class);
        return baseImageList;
    }
}
