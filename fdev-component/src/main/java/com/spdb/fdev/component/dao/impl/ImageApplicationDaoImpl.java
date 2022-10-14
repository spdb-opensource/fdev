package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.dao.IImageApplicationDao;
import com.spdb.fdev.component.entity.ImageApplication;
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
public class ImageApplicationDaoImpl implements IImageApplicationDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<ImageApplication> query(ImageApplication imageApplication) throws JsonProcessingException {
        List<ImageApplication> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = imageApplication == null ? "{}" : objectMapper.writeValueAsString(imageApplication);
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
        AggregationResults<ImageApplication> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.IMAGE_APPLICATION, ImageApplication.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public ImageApplication save(ImageApplication imageApplication) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        imageApplication.set_id(objectId);
        imageApplication.setId(id);
        return mongoTemplate.save(imageApplication);
    }

    @Override
    public ImageApplication update(ImageApplication imageApplication) throws JsonProcessingException {
        Query query = Query.query(Criteria.where(Dict.ID).is(imageApplication.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(imageApplication);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, imageApplication.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, ImageApplication.class);
        return this.mongoTemplate.findOne(query, ImageApplication.class);
    }

    @Override
    public void delete(ImageApplication imageApplication) {
        Query query = new Query(Criteria.where(Dict.ID).is(imageApplication.getId()));
        mongoTemplate.findAndRemove(query, ImageApplication.class);
    }

    /**
     * 根据应用id删除
     *
     * @param applicationId
     */
    @Override
    public void deleteAllByApplicationId(String applicationId) {
        Query query = new Query(Criteria.where(Dict.APPLICATION_ID).is(applicationId));
        mongoTemplate.findAllAndRemove(query, ImageApplication.class);
    }

    /**
     * 根据镜像名称删除
     *
     * @param imageName
     */
    @Override
    public void deleteAllByImageName(String imageName) {
        Query query = new Query(Criteria.where(Dict.IMAGE_NAME).is(imageName));
        mongoTemplate.findAllAndRemove(query, ImageApplication.class);
    }

    @Override
    public ImageApplication queryByApplicationAndImage(String id, String image_name) {
        Query query = new Query(Criteria.where(Dict.APPLICATION_ID).is(id).and(Dict.IMAGE_NAME).in(image_name));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, ImageApplication.class);
    }

    @Override
    public List<ImageApplication> queryByImage(String imageName) {
        Query query = new Query(Criteria.where(Dict.IMAGE_NAME).is(imageName));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.UPDATE_TIME));
        return mongoTemplate.find(query, ImageApplication.class);
    }
}
