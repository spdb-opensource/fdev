package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.dao.IArchetypeInfoDao;
import com.spdb.fdev.component.entity.ArchetypeInfo;
import com.spdb.fdev.component.entity.MpassComponent;
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
import java.util.Map;

@Repository
public class ArchetypeInfoDaoImpl implements IArchetypeInfoDao {
    @Autowired
    private MongoTemplate mongoTemplate;



    @Override
    public List query(ArchetypeInfo archetypeInfo) throws Exception {
        List<ArchetypeInfo> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = archetypeInfo == null ? "{}" : objectMapper.writeValueAsString(archetypeInfo);
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
        AggregationResults<ArchetypeInfo> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.ARCHETYPEINFO, ArchetypeInfo.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public ArchetypeInfo save(ArchetypeInfo archetypeInfo) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        archetypeInfo.set_id(objectId);
        archetypeInfo.setId(id);
        return mongoTemplate.save(archetypeInfo);
    }

    @Override
    public void delete(ArchetypeInfo archetypeInfo) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(archetypeInfo.getId()));
        mongoTemplate.findAndRemove(query, ArchetypeInfo.class);
    }

    @Override
    public ArchetypeInfo queryById(ArchetypeInfo archetypeInfo) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(archetypeInfo.getId()));
        return mongoTemplate.findOne(query, ArchetypeInfo.class);
    }

    @Override
    public ArchetypeInfo queryById(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        return mongoTemplate.findOne(query, ArchetypeInfo.class);
    }

    @Override
    public ArchetypeInfo queryByNameEn(String name_en) throws Exception {
        Query query = new Query(Criteria.where(Dict.NAME_EN).is(name_en));
        return mongoTemplate.findOne(query, ArchetypeInfo.class);
    }

    @Override
    public ArchetypeInfo queryByGroupIdAndArtifact(String groupId, String artifactId) {
        Query query = new Query(Criteria.where(Dict.GROUPID).is(groupId).and(Dict.ARTIFACTID).is(artifactId));
        return mongoTemplate.findOne(query, ArchetypeInfo.class);
    }

    @Override
    public ArchetypeInfo queryByWebUrl(String web_url) {
        Query query = new Query(Criteria.where(Dict.GITLAB_URL).is(web_url));
        return mongoTemplate.findOne(query, ArchetypeInfo.class);
    }

    @Override
    public List<ArchetypeInfo> queryByType(String type) throws Exception {
        Query query = new Query(Criteria.where(Dict.TYPE).is(type));
        return mongoTemplate.find(query, ArchetypeInfo.class);
    }

    @Override
    public ArchetypeInfo update(ArchetypeInfo archetypeInfo) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(archetypeInfo.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(archetypeInfo);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, archetypeInfo.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, ArchetypeInfo.class);
        return this.mongoTemplate.findOne(query, ArchetypeInfo.class);
    }

    @Override
    public List<ArchetypeInfo> queryByUserId(String user_id) {
        Criteria criteria = new Criteria();
        if (!StringUtils.isEmpty(user_id)) {
            criteria.and(Dict.ID).in(user_id);
        }
        Query query = new Query(Criteria.where(Dict.MANAGER_ID).elemMatch(criteria));
        return mongoTemplate.find(query, ArchetypeInfo.class);
    }

    @Override
    public Integer queryDataByType(String start_date, String end_date) {
        Query query = new Query(Criteria.where(Dict.CREATE_DATE).lte(end_date));
        List<ArchetypeInfo> ArchetypeInfo = mongoTemplate.find(query, ArchetypeInfo.class);
        return ArchetypeInfo.size();
    }

    @Override
    public List<Map> queryQrmntsData(String userId) {
        Criteria c = new Criteria();
        c.and(Dict.ASSIGNEE).is(userId);
        AggregationOperation match = Aggregation.match(c);
        AggregationOperation lookup = Aggregation.lookup(Dict.ARCHETYPEINFO, Dict.ARCHETYPE_ID, Dict.ID, Dict.ARCHETYPEINFO);
        Aggregation aggregation = Aggregation.newAggregation(lookup, match);
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, Dict.ARCHETYPE_ISSUE, Map.class);
        List<Map> results = new ArrayList<>();
        docs.forEach(doc -> {
            if (((List) doc.get(Dict.ARCHETYPEINFO)).size() > 0) {
                doc.put(Dict.ARCHETYPEINFO, ((List) doc.get(Dict.ARCHETYPEINFO)).get(0));
                results.add(doc);
            }
        });
        return results;
    }

    @Override
    public List<ArchetypeInfo> getArchetypeByIds(List params) throws Exception {
        Criteria c = Criteria.where(Dict.ID).in(params);
        List<ArchetypeInfo> archetypeInfoList = this.mongoTemplate.find(Query.query(c), ArchetypeInfo.class);
        return archetypeInfoList;
    }


}
