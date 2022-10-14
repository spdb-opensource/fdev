package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.dao.IImageArchetypeDao;
import com.spdb.fdev.component.entity.ImageArchetype;
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
public class ImageArchetypeDaoImpl implements IImageArchetypeDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<ImageArchetype> query(ImageArchetype imageArchetype) throws JsonProcessingException {
        List<ImageArchetype> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = imageArchetype == null ? "{}" : objectMapper.writeValueAsString(imageArchetype);
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
        AggregationResults<ImageArchetype> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.IMAGE_ARCHETYPE, ImageArchetype.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public ImageArchetype save(ImageArchetype imageArchetype) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        imageArchetype.set_id(objectId);
        imageArchetype.setId(id);
        return mongoTemplate.save(imageArchetype);
    }

    @Override
    public ImageArchetype update(ImageArchetype imageArchetype) throws JsonProcessingException {
        Query query = Query.query(Criteria.where(Dict.ID).is(imageArchetype.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(imageArchetype);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, imageArchetype.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, ImageArchetype.class);
        return this.mongoTemplate.findOne(query, ImageArchetype.class);
    }

    @Override
    public void delete(ImageArchetype imageArchetype) {
        Query query = new Query(Criteria.where(Dict.ID).is(imageArchetype.getId()));
        mongoTemplate.findAndRemove(query, ImageArchetype.class);
    }

    @Override
    public ImageArchetype queryByArchetypeIdAndImageName(ImageArchetype imageArchetype) {
        Query query = new Query();
        query.fields().exclude(Dict.OBJECTID);
        query.addCriteria(Criteria.where(Dict.ARCHETYPE_ID).is(imageArchetype.getArchetype_id()).and(Dict.ARCHETYPE_VERSION).is(imageArchetype.getArchetype_version()).and(Dict.IMAGE_NAME).is(imageArchetype.getImage_name()));
        return mongoTemplate.findOne(query, ImageArchetype.class);
    }

    @Override
    public List<ImageArchetype> queryByImage(String imageName) {
        Query query = new Query(Criteria.where(Dict.IMAGE_NAME).is(imageName));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.UPDATE_TIME));
        return mongoTemplate.find(query, ImageArchetype.class);
    }
}
