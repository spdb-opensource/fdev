package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.dao.IMpassReleaseIssueDao;
import com.spdb.fdev.component.entity.ComponentIssue;
import com.spdb.fdev.component.entity.MpassReleaseIssue;
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
import java.util.Map;

@Repository
public class MpassReleaseIssueDaoImpl implements IMpassReleaseIssueDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List query(MpassReleaseIssue mpassReleaseIssue) throws Exception {
        List<MpassReleaseIssue> result = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = mpassReleaseIssue == null ? "{}" : objectMapper.writeValueAsString(mpassReleaseIssue);

        BasicDBObject queryJson = BasicDBObject.parse(json);

        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        c.and(Dict.FEATURE_BRANCH).ne(null);
        while (it.hasNext()) {
            String key = it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        List<AggregationOperation> opetations = new ArrayList<>();
        opetations.add(Aggregation.project().andExclude(Dict.OBJECTID));
        opetations.add(Aggregation.match(c));
        opetations.add(Aggregation.sort(Sort.Direction.DESC, Dict.CREATE_DATE));
        AggregationResults<MpassReleaseIssue> docs = mongoTemplate.aggregate(Aggregation.newAggregation(opetations), Dict.MPASS_RELEASE_ISSUE, MpassReleaseIssue.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public MpassReleaseIssue save(MpassReleaseIssue mpassReleaseIssue) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        mpassReleaseIssue.set_id(objectId);
        mpassReleaseIssue.setId(id);
        return mongoTemplate.save(mpassReleaseIssue);
    }

    @Override
    public MpassReleaseIssue update(MpassReleaseIssue mpassReleaseIssue) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(mpassReleaseIssue.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(mpassReleaseIssue);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, mpassReleaseIssue.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, MpassReleaseIssue.class);
        return this.mongoTemplate.findOne(query, MpassReleaseIssue.class);
    }

    @Override
    public void delete(MpassReleaseIssue mpassReleaseIssue) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(mpassReleaseIssue.getId()));
        mongoTemplate.findAndRemove(query, MpassReleaseIssue.class);
    }

    @Override
    public MpassReleaseIssue queryById(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        return mongoTemplate.findOne(query, MpassReleaseIssue.class);
    }

    @Override
    public MpassReleaseIssue queryByComponentAndBranch(String component_id, String branch) throws Exception {
        Query query = new Query(Criteria.where(Dict.COMPONENT_ID).is(component_id).and(Dict.FEATURE_BRANCH).is(branch));
        return mongoTemplate.findOne(query, MpassReleaseIssue.class);
    }

    @Override
    public MpassReleaseIssue queryByBranch(String branch) throws Exception {
        Query query = new Query(Criteria.where(Dict.FEATURE_BRANCH).is(branch));
        return mongoTemplate.findOne(query, MpassReleaseIssue.class);
    }

    @Override
    public List<MpassReleaseIssue> queryByComponentId(String componentId) {
        Query query = new Query(Criteria.where(Dict.COMPONENT_ID).is(componentId));
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_DATE));
        return mongoTemplate.find(query, MpassReleaseIssue.class);
    }

    @Override
    public List<Map> queryQrmntsData(String userId) {
        Criteria c = new Criteria();
        c.and(Dict.ASSIGNEE).is(userId);
        AggregationOperation match = Aggregation.match(c);
        AggregationOperation mpassComponentLookup = Aggregation.lookup(Dict.MPASS_COMPONENT, Dict.COMPONENT_ID, Dict.ID, Dict.MPASS_COMPONENT);
        AggregationOperation mpassReleaseIssueLookups = Aggregation.lookup(Dict.MPASS_RELEASE_ISSUE, Dict.ISSUE_ID, Dict.ID, Dict.MPASS_RELEASE_ISSUE);
        Aggregation aggregation = Aggregation.newAggregation(mpassComponentLookup, match, mpassReleaseIssueLookups);
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, Dict.MPASS_DEV_ISSUE, Map.class);
        List<Map> results = new ArrayList<>();
        docs.forEach(doc -> {
            if (((List) doc.get(Dict.MPASS_COMPONENT)).size() > 0) {
                doc.put(Dict.MPASS_COMPONENT, ((List) doc.get(Dict.MPASS_COMPONENT)).get(0));
                doc.put(Dict.MPASS_RELEASE_ISSUE, ((List) doc.get(Dict.MPASS_RELEASE_ISSUE)).get(0));
                results.add(doc);
            }
        });
        return results;
    }

    @Override
    public List<Map> queryIssueDelay(Map requestParam) {
        List<Map> results = new ArrayList<>();
        Criteria c = new Criteria();
        if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.ID))){
            c.and(Dict.COMPONENT_ID).is(requestParam.get(Dict.ID));
        }
        if(!CommonUtils.isNullOrEmpty(requestParam.get(Dict.MEMBER))){
            c.and(Dict.ASSIGNEE).is(requestParam.get(Dict.MEMBER));
        }
        AggregationOperation match = Aggregation.match(c);
        AggregationOperation mpass_component = Aggregation.lookup(Dict.MPASS_COMPONENT, Dict.COMPONENT_ID, Dict.ID, Dict.MPASS_COMPONENT);
        AggregationOperation mpass_dev_issue = Aggregation.lookup(Dict.MPASS_RELEASE_ISSUE, Dict.ISSUE_ID, Dict.ID, Dict.MPASS_RELEASE_ISSUE);
        Aggregation aggregation = Aggregation.newAggregation(mpass_component,mpass_dev_issue, match);
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, Dict.MPASS_DEV_ISSUE,Map.class);
        docs.forEach(doc -> {
            if(((List) doc.get(Dict.MPASS_COMPONENT)).size() > 0){
                doc.put(Dict.MPASS_COMPONENT, ((List) doc.get(Dict.MPASS_COMPONENT)).get(0));
            }
            if(((List) doc.get(Dict.MPASS_RELEASE_ISSUE)).size() > 0){
                doc.put(Dict.MPASS_RELEASE_ISSUE, ((List) doc.get(Dict.MPASS_RELEASE_ISSUE)).get(0));
            }
            results.add(doc);
        } );
        return results;
    }

    @Override
    public List<MpassReleaseIssue> queryLatestVersion(String componentId, String releaseNodeName) {
        Query query = new Query(Criteria.where(Dict.COMPONENT_ID).is(componentId).and("release_node_name").is(releaseNodeName));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_DATE));
        return mongoTemplate.find(query, MpassReleaseIssue.class);
    }

    @Override
    public List<MpassReleaseIssue> queryAllReleaseIssues(String componentId) {
        Query query = new Query(Criteria.where(Dict.COMPONENT_ID).is(componentId));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_DATE));
        return mongoTemplate.find(query, MpassReleaseIssue.class);
    }
}
