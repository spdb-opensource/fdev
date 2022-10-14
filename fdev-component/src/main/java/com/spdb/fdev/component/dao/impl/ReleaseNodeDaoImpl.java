package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.dao.IMpassReleaseIssueDao;
import com.spdb.fdev.component.dao.IReleaseNodeDao;
import com.spdb.fdev.component.entity.ArchetypeInfo;
import com.spdb.fdev.component.entity.ReleaseNode;
import com.spdb.fdev.component.entity.TagRecord;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Iterator;

@Repository
public class ReleaseNodeDaoImpl implements IReleaseNodeDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ReleaseNode save(ReleaseNode releaseNode) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        releaseNode.set_id(objectId);
        releaseNode.setId(id);
        return mongoTemplate.save(releaseNode);
    }

    @Override
    public ReleaseNode queryByAppIdAndReleaseNodeName(String appId,String releaseNodeName) throws Exception {
        Query query =new Query(Criteria.where("release_node_name")
                .is(releaseNodeName)
                .and("app_id")
                .is(appId));
        return mongoTemplate.findOne(query, ReleaseNode .class);

    }

    @Override
    public ReleaseNode update(ReleaseNode releaseNode) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(releaseNode.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(releaseNode);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, releaseNode.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, ReleaseNode.class);
        return this.mongoTemplate.findOne(query, ReleaseNode.class);
    }

}
