package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.dao.IComponentIssueDao;
import com.spdb.fdev.component.dao.IComponentRecordDao;
import com.spdb.fdev.component.entity.ComponentIssue;
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
public class ComponentIssueDaoImpl implements IComponentIssueDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IComponentRecordDao componentRecordDao;

    @Override
    public List query(ComponentIssue componentIssue) throws Exception {
        List<ComponentIssue> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = componentIssue == null ? "{}" : objectMapper.writeValueAsString(componentIssue);
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
        AggregationResults<ComponentIssue> docs = mongoTemplate.aggregate(Aggregation.newAggregation(opetations), Dict.COMPONENT_ISSUE, ComponentIssue.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public ComponentIssue save(ComponentIssue componentIssue) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        componentIssue.set_id(objectId);
        componentIssue.setId(id);
        return mongoTemplate.save(componentIssue);
    }

    @Override
    public void delete(ComponentIssue componentIssue) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(componentIssue.getId()));
        mongoTemplate.findAndRemove(query, ComponentIssue.class);
    }

    @Override
    public ComponentIssue queryById(ComponentIssue componentIssue) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(componentIssue.getId()));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, ComponentIssue.class);
    }

    @Override
    public ComponentIssue queryByComponentIdAndVersion(String component_id, String version) {
        Query query = new Query(Criteria.where(Dict.COMPONENT_ID).is(component_id).and(Dict.TARGET_VERSION).is(version));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, ComponentIssue.class);
    }

    @Override
    public ComponentIssue queryById(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, ComponentIssue.class);
    }

    @Override
    public ComponentIssue update(ComponentIssue componentIssue) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(componentIssue.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(componentIssue);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, componentIssue.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, ComponentIssue.class);
        return this.mongoTemplate.findOne(query, ComponentIssue.class);
    }

    @Override
    public List<ComponentIssue> queryDevIssues(String component_id) {
        Query query = new Query(Criteria.where(Dict.COMPONENT_ID).is(component_id).and(Dict.STAGE).nin(Constants.STAGE_PASSED));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_DATE));
        return mongoTemplate.find(query, ComponentIssue.class);
    }

    @Override
    public List<ComponentIssue> queryAllDevIssues(String component_id) {
        Query query = new Query(Criteria.where(Dict.COMPONENT_ID).is(component_id));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_DATE));
        return mongoTemplate.find(query, ComponentIssue.class);
    }

    @Override
    public List<Map> queryQrmntsData(String userId) {
        Criteria c = new Criteria();
        c.and(Dict.ASSIGNEE).is(userId);
        AggregationOperation match = Aggregation.match(c);
        AggregationOperation lookup = Aggregation.lookup(Dict.COMPONENT_INFO, Dict.COMPONENT_ID, Dict.ID, Dict.COMPONENT_INFO);
        Aggregation aggregation = Aggregation.newAggregation(lookup, match);
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, Dict.COMPONENT_ISSUE, Map.class);
        List<Map> results = new ArrayList<>();
        docs.forEach(doc -> {
            if (((List) doc.get(Dict.COMPONENT_INFO)).size() > 0) {
                doc.put(Dict.COMPONENT_INFO, ((List) doc.get(Dict.COMPONENT_INFO)).get(0));
                results.add(doc);
            }
        });
        return results;
    }

    @Override
    public List<Map> queryIssueDelay(Map requestParam) {
        List<Map> results = new ArrayList<>();
        Criteria c = new Criteria();
        if (!CommonUtils.isNullOrEmpty(requestParam.get(Dict.ID))) {
            c.and(Dict.COMPONENT_ID).is(requestParam.get(Dict.ID));
        }
        if (!CommonUtils.isNullOrEmpty(requestParam.get(Dict.MEMBER))) {
            c.and(Dict.ASSIGNEE).is(requestParam.get(Dict.MEMBER));
        }
        AggregationOperation match = Aggregation.match(c);
        AggregationOperation component_info = Aggregation.lookup(Dict.COMPONENT_INFO, Dict.COMPONENT_ID, Dict.ID, Dict.COMPONENT_INFO);
        Aggregation aggregation = Aggregation.newAggregation(component_info, match);
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, Dict.COMPONENT_ISSUE, Map.class);
        docs.forEach(doc -> {
            if (((List) doc.get(Dict.COMPONENT_INFO)).size() > 0) {
                doc.put(Dict.COMPONENT_INFO, ((List) doc.get(Dict.COMPONENT_INFO)).get(0));
                results.add(doc);
            }
        });
        return results;
    }

    @Override
    public List<ComponentIssue> queryLatestVersion(String componentId, String releaseNodeName) {
        Query query = new Query(Criteria.where(Dict.COMPONENT_ID).is(componentId).and("release_node_name").is(releaseNodeName));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_DATE));
        return mongoTemplate.find(query, ComponentIssue.class);
    }
}
