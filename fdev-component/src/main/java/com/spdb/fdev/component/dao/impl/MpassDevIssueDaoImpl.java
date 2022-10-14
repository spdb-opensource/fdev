package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.dao.IMpassDevIssueDao;
import com.spdb.fdev.component.entity.MpassDevIssue;
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
public class MpassDevIssueDaoImpl implements IMpassDevIssueDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List query(MpassDevIssue mpassDevIssue) throws Exception {
        List<MpassDevIssue> result = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = mpassDevIssue == null ? "{}" : objectMapper.writeValueAsString(mpassDevIssue);

        BasicDBObject queryJson = BasicDBObject.parse(json);

        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        List<AggregationOperation> opetations = new ArrayList<>();
        opetations.add(Aggregation.project().andExclude(Dict.OBJECTID));
        opetations.add(Aggregation.match(c));
        opetations.add(Aggregation.sort(Sort.Direction.DESC, Dict.CREATE_DATE));
        AggregationResults<MpassDevIssue> docs = mongoTemplate.aggregate(Aggregation.newAggregation(opetations), Dict.MPASS_DEV_ISSUE, MpassDevIssue.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public MpassDevIssue save(MpassDevIssue mpassDevIssue) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        mpassDevIssue.set_id(objectId);
        mpassDevIssue.setId(id);
        return mongoTemplate.save(mpassDevIssue);
    }

    @Override
    public MpassDevIssue update(MpassDevIssue mpassDevIssue) throws JsonProcessingException {
        Query query = Query.query(Criteria.where(Dict.ID).is(mpassDevIssue.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(mpassDevIssue);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, mpassDevIssue.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, MpassDevIssue.class);
        return this.mongoTemplate.findOne(query, MpassDevIssue.class);
    }

    @Override
    public void delete(MpassDevIssue mpassDevIssue) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(mpassDevIssue.getId()));
        mongoTemplate.findAndRemove(query, MpassDevIssue.class);
    }

    @Override
    public MpassDevIssue queryById(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, MpassDevIssue.class);
    }

    @Override
    public MpassDevIssue queryByComIdAndIssueIdBranch(String componentId, String issueId, String branch) {
        Query query = new Query(Criteria.where(Dict.COMPONENT_ID).is(componentId).and(Dict.ISSUE_ID).is(issueId).and(Dict.FEATURE_BRANCH).is(branch));
        return mongoTemplate.findOne(query, MpassDevIssue.class);
    }

    @Override
    public List<MpassDevIssue> queryByIssueId(String issueId) {
        Query query = new Query(Criteria.where(Dict.ISSUE_ID).is(issueId));
        return mongoTemplate.find(query, MpassDevIssue.class);
    }
}
