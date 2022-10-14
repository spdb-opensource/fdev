package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.dao.IArchetypeIssueDao;
import com.spdb.fdev.component.entity.ArchetypeIssue;
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
public class ArchetypeIssueDaoImpl implements IArchetypeIssueDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List query(ArchetypeIssue archetypeIssue) throws Exception {
        List<ArchetypeIssue> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = archetypeIssue == null ? "{}" : objectMapper.writeValueAsString(archetypeIssue);
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
        AggregationResults<ArchetypeIssue> docs = mongoTemplate.aggregate(Aggregation.newAggregation(opetations), Dict.ARCHETYPE_ISSUE, ArchetypeIssue.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public ArchetypeIssue save(ArchetypeIssue archetypeIssue) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        archetypeIssue.set_id(objectId);
        archetypeIssue.setId(id);
        return mongoTemplate.save(archetypeIssue);
    }

    @Override
    public void delete(ArchetypeIssue archetypeIssue) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(archetypeIssue.getId()));
        mongoTemplate.findAndRemove(query, ArchetypeIssue.class);
    }

    @Override
    public ArchetypeIssue queryById(ArchetypeIssue archetypeIssue) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(archetypeIssue.getId()));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, ArchetypeIssue.class);
    }

    @Override
    public ArchetypeIssue queryByArchetypeIdAndVersion(String archetype_id, String version) {
        Query query = new Query(Criteria.where(Dict.ARCHETYPE_ID).is(archetype_id).and(Dict.TARGET_VERSION).is(version));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, ArchetypeIssue.class);
    }

    @Override
    public ArchetypeIssue queryById(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, ArchetypeIssue.class);
    }

    @Override
    public ArchetypeIssue update(ArchetypeIssue archetypeIssue) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(archetypeIssue.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(archetypeIssue);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, archetypeIssue.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, ArchetypeIssue.class);
        return this.mongoTemplate.findOne(query, ArchetypeIssue.class);
    }

    @Override
    public List queryDevIssues(String archetypeId) {
        Query query = new Query(Criteria.where(Dict.ARCHETYPE_ID).is(archetypeId).and(Dict.STAGE).nin(Constants.ARCHETYPE_STAGE_PASSED));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_DATE));
        return mongoTemplate.find(query, ArchetypeIssue.class);
    }

    @Override
    public List<Map> queryIssueDelay(Map requestParam) {
        List<Map> results = new ArrayList<>();
        Criteria c = new Criteria();
        if (!CommonUtils.isNullOrEmpty(requestParam.get(Dict.ID))) {
            c.and(Dict.ARCHETYPE_ID).is(requestParam.get(Dict.ID));
        }
        if (!CommonUtils.isNullOrEmpty(requestParam.get(Dict.MEMBER))) {
            c.and(Dict.ASSIGNEE).is(requestParam.get(Dict.MEMBER));
        }
        AggregationOperation match = Aggregation.match(c);
        AggregationOperation component_info = Aggregation.lookup(Dict.ARCHETYPEINFO, Dict.ARCHETYPE_ID, Dict.ID, Dict.ARCHETYPEINFO);
        Aggregation aggregation = Aggregation.newAggregation(component_info, match);
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, Dict.ARCHETYPE_ISSUE, Map.class);
        docs.forEach(doc -> {
            if (((List) doc.get(Dict.ARCHETYPEINFO)).size() > 0) {
                doc.put(Dict.ARCHETYPEINFO, ((List) doc.get(Dict.ARCHETYPEINFO)).get(0));
            }
            results.add(doc);
        });
        return results;
    }

    @Override
    public List<ArchetypeIssue> queryAllDevIssues(String archetypeId) {
        Query query = new Query(Criteria.where(Dict.ARCHETYPE_ID).is(archetypeId));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_DATE));
        return mongoTemplate.find(query, ArchetypeIssue.class);
    }

    @Override
    public List<ArchetypeIssue> queryLatestVersion(String archetypeId, String releaseNodeName) {
        Query query = new Query(Criteria.where(Dict.ARCHETYPE_ID).is(archetypeId).and("release_node_name").is(releaseNodeName));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_DATE));
        return mongoTemplate.find(query, ArchetypeIssue.class);
    }
}
