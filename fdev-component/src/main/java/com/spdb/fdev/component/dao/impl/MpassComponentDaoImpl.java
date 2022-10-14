package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.dao.IMpassComponentDao;
import com.spdb.fdev.component.entity.ComponentInfo;
import com.spdb.fdev.component.entity.MpassArchetype;
import com.spdb.fdev.component.entity.MpassComponent;
import com.spdb.fdev.component.entity.MpassReleaseIssue;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
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
public class MpassComponentDaoImpl implements IMpassComponentDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<MpassComponent> getMpassComponentByIds(List params) throws Exception{
        Criteria c = Criteria.where(Dict.ID).in(params);
        List<MpassComponent> mpassComponentList = this.mongoTemplate.find(Query.query(c), MpassComponent.class);
        return mpassComponentList;
    }

    @Override
    public List query(MpassComponent mpassComponent) throws Exception {
        List<MpassComponent> result = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = mpassComponent == null ? "{}" : objectMapper.writeValueAsString(mpassComponent);

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
        AggregationResults<MpassComponent> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.MPASS_COMPONENT, MpassComponent.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public MpassComponent save(MpassComponent mpassComponent) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        mpassComponent.set_id(objectId);
        mpassComponent.setId(id);
        return mongoTemplate.save(mpassComponent);
    }

    @Override
    public void delete(MpassComponent mpassComponent) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(mpassComponent.getId()));
        mongoTemplate.findAndRemove(query, MpassComponent.class);
    }

    @Override
    public MpassComponent queryById(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        return mongoTemplate.findOne(query, MpassComponent.class);
    }

    @Override
    public MpassComponent queryByGitlabUrl(String web_url) {
        Query query = new Query(Criteria.where(Dict.GITLAB_URL).is(web_url));
        return mongoTemplate.findOne(query, MpassComponent.class);
    }


    @Override
    public MpassComponent update(MpassComponent mpassComponent) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(mpassComponent.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(mpassComponent);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, mpassComponent.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, MpassComponent.class);
        return this.mongoTemplate.findOne(query, MpassComponent.class);
    }

    @Override
    public List<MpassComponent> queryByUserId(String user_id) {
        Criteria criteria = new Criteria();
        if (!StringUtils.isEmpty(user_id)) {
            criteria.and(Dict.ID).in(user_id);
        }
        Query query = new Query(Criteria.where(Dict.MANAGER).elemMatch(criteria));
        return mongoTemplate.find(query, MpassComponent.class);
    }

    @Override
    public MpassComponent queryByNpmname(String npm_name) {
        Query query = new Query(Criteria.where(Dict.NPM_NAME).is(npm_name));
        return mongoTemplate.findOne(query, MpassComponent.class);
    }

    @Override
    public Integer queryDataByType(String start_date, String end_date) {
        Query query = new Query(Criteria.where(Dict.CREATE_DATE).lte(end_date));
        List<MpassComponent> MpassComponent = mongoTemplate.find(query, MpassComponent.class);
        return MpassComponent.size();
    }

    @Override
    public MpassComponent queryByGitlabId(String gitlabId) {
        Query query = new Query(Criteria.where(Dict.GITLAB_ID).is(gitlabId));
        return mongoTemplate.findOne(query, MpassComponent.class);
    }

}
