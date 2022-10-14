package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.AppOwnFieldServiceDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppEnvMapping;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppOwnField;
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Repository
public class AppOwnFieldServiceDaoImpl implements AppOwnFieldServiceDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public AppOwnField save(AppOwnField appOwnField) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        appOwnField.set_id(objectId);
        appOwnField.setId(id);
        appOwnField.setCreateTime(DateUtil.getDate(new Date(), DateUtil.DATETIME_ISO_FORMAT));
        return this.mongoTemplate.save(appOwnField, "app_own_field");
    }


    @Override
    public void update(AppOwnField appOwnField) {
        Query query;
        if (CommonUtils.isNullOrEmpty(appOwnField.getApp_Id()) || CommonUtils.isNullOrEmpty(appOwnField.getEnv_id())) {
            query = new Query(Criteria.where(Dict.ID).is(appOwnField.get_id()));
        } else {
            query = new Query(Criteria.where(Dict.APPID).is(appOwnField.getApp_Id()).and(Dict.ENV_ID).is(appOwnField.getEnv_id()));
        }
        Update update = Update.update(Dict.MODELFIELD_VALUE, appOwnField.getModelFleld_value())
                .set(Dict.UPDATE_TIME, DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        mongoTemplate.findAndModify(query, update, AppOwnField.class);
    }

    @Override
    public AppOwnField queryByAppIdAndEnvId(AppOwnField appOwnField) {

        Query query = new Query(Criteria.where(Dict.ENV_ID).is(appOwnField.getEnv_id()).and(Dict.APPID).is(appOwnField.getApp_Id()));
        return mongoTemplate.findOne(query, AppOwnField.class);
    }

    @Override
    public List<AppOwnField> query(AppOwnField appOwnField) throws Exception{
        List<AppOwnField> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = appOwnField == null ? "{}" : objectMapper.writeValueAsString(appOwnField);
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
        AggregationResults<AppOwnField> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), "app_own_field", AppOwnField.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public void delete(String app_id,String env_id) {
        Query query = new Query(Criteria.where(Dict.APPID).is(app_id).and(Dict.ENV_ID).is(env_id));
        this.mongoTemplate.remove(query, AppOwnField.class);
    }
}
