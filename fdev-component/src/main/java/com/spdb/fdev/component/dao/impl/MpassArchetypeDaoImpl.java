package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.dao.IMpassArchetypeDao;
import com.spdb.fdev.component.entity.ArchetypeInfo;
import com.spdb.fdev.component.entity.ArchetypeIssueTag;
import com.spdb.fdev.component.entity.BaseImageRecord;
import com.spdb.fdev.component.entity.MpassArchetype;
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

import java.util.*;

@Repository
public class MpassArchetypeDaoImpl implements IMpassArchetypeDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List query(MpassArchetype mpassArchetype) throws Exception {
        List<MpassArchetype> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = mpassArchetype == null ? "{}" : objectMapper.writeValueAsString(mpassArchetype);
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
        AggregationResults<MpassArchetype> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.MPASS_ARCHETYPE, MpassArchetype.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public MpassArchetype save(MpassArchetype mpassArchetype) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        mpassArchetype.set_id(objectId);
        mpassArchetype.setId(id);
        return mongoTemplate.save(mpassArchetype);
    }

    @Override
    public void delete(MpassArchetype mpassArchetype) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(mpassArchetype.getId()));
        mongoTemplate.findAndRemove(query, MpassArchetype.class);
    }

    @Override
    public MpassArchetype queryById(MpassArchetype mpassArchetype) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(mpassArchetype.getId()));
        return mongoTemplate.findOne(query, MpassArchetype.class);
    }

    @Override
    public MpassArchetype queryById(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        return mongoTemplate.findOne(query, MpassArchetype.class);
    }

    @Override
    public MpassArchetype queryByWebUrl(String web_url) {
        Query query = new Query(Criteria.where(Dict.GITLAB_URL).is(web_url));
        return mongoTemplate.findOne(query, MpassArchetype.class);
    }

    @Override
    public MpassArchetype update(MpassArchetype MpassArchetype) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(MpassArchetype.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(MpassArchetype);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, MpassArchetype.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, MpassArchetype.class);
        return this.mongoTemplate.findOne(query, MpassArchetype.class);
    }

    @Override
    public List<MpassArchetype> queryByUserId(String user_id) {
        Criteria criteria = new Criteria();
        if (!StringUtils.isEmpty(user_id)) {
            criteria.and(Dict.ID).in(user_id);
        }
        Query query = new Query(Criteria.where(Dict.MANAGER).elemMatch(criteria));
        return mongoTemplate.find(query, MpassArchetype.class);
    }

    @Override
    public Integer queryDataByType(String start_date, String end_date) {
        Query query = new Query(Criteria.where(Dict.CREATE_DATE).lte(end_date));
        List<MpassArchetype> MpassArchetype = mongoTemplate.find(query, MpassArchetype.class);
        return MpassArchetype.size();
    }

    @Override
    public List<Map> queryQrmntsData(String userId) {
        Criteria c = new Criteria();
        c.and(Dict.ASSIGNEE).is(userId);
        AggregationOperation match = Aggregation.match(c);
        AggregationOperation lookup = Aggregation.lookup(Dict.MPASS_ARCHETYPE, Dict.ARCHETYPE_ID, Dict.ID, Dict.MPASS_ARCHETYPE);
        Aggregation aggregation = Aggregation.newAggregation(lookup, match);
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, Dict.ARCHETYPE_ISSUE, Map.class);
        List<Map> results = new ArrayList<>();
        docs.forEach(doc -> {
            if (((List) doc.get(Dict.MPASS_ARCHETYPE)).size() > 0) {
                doc.put(Dict.MPASS_ARCHETYPE, ((List) doc.get(Dict.MPASS_ARCHETYPE)).get(0));
                results.add(doc);
            }
        });
        return results;
    }

    @Override
    public Map<String, Integer> queryIssueData(String startTime, String endTime) {
        HashMap<String, Integer> MpassArcheNum = new HashMap<>();
        Criteria c = new Criteria();
        if (!CommonUtils.isNullOrEmpty(startTime)) {
            c.and(Dict.DATE).gte(startTime);
        }
        List<ArchetypeIssueTag> archetypeIssueTags = mongoTemplate.find(new Query(Criteria.where(Dict.DATE).lte(endTime).andOperator(c)), ArchetypeIssueTag.class);
        MpassArcheNum.put(Dict.ALPHA, 0);
        MpassArcheNum.put(Dict.RELEASE.toLowerCase(), archetypeIssueTags.size());
        MpassArcheNum.put(Dict.TOTAL, archetypeIssueTags.size());
        return MpassArcheNum;
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
        AggregationOperation component_info = Aggregation.lookup(Dict.MPASS_ARCHETYPE, Dict.ARCHETYPE_ID, Dict.ID, Dict.MPASS_ARCHETYPE);
        Aggregation aggregation = Aggregation.newAggregation(component_info, match);
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, Dict.ARCHETYPE_ISSUE, Map.class);
        docs.forEach(doc -> {
            if (((List) doc.get(Dict.MPASS_ARCHETYPE)).size() > 0) {
                doc.put(Dict.MPASS_ARCHETYPE, ((List) doc.get(Dict.MPASS_ARCHETYPE)).get(0));
            }
            results.add(doc);
        });
        return results;
    }

    @Override
    public List<MpassArchetype> getMpassArchetypeByIds(List params)  throws Exception{
        Criteria c = Criteria.where(Dict.ID).in(params);
        List<MpassArchetype> mpassArchetypeList = this.mongoTemplate.find(Query.query(c), MpassArchetype.class);
        return mpassArchetypeList;
    }
}
