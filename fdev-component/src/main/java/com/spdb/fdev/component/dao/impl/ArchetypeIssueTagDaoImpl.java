package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.dao.IArchetypeIssueTagDao;
import com.spdb.fdev.component.entity.ArchetypeIssue;
import com.spdb.fdev.component.entity.ArchetypeIssueTag;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class ArchetypeIssueTagDaoImpl implements IArchetypeIssueTagDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<ArchetypeIssueTag> query(ArchetypeIssueTag archetypeIssueTag) throws JsonProcessingException {
        List<ArchetypeIssueTag> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = archetypeIssueTag == null ? "{}" : objectMapper.writeValueAsString(archetypeIssueTag);
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
        AggregationResults<ArchetypeIssueTag> docs = mongoTemplate.aggregate(Aggregation.newAggregation(opetations), Dict.ARCHETYPE_ISSUE_TAG, ArchetypeIssueTag.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public ArchetypeIssueTag save(ArchetypeIssueTag archetypeIssueTag) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        archetypeIssueTag.set_id(objectId);
        archetypeIssueTag.setId(id);
        return mongoTemplate.save(archetypeIssueTag);
    }

    @Override
    public void delete(ArchetypeIssueTag archetypeIssueTag) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(archetypeIssueTag.getId()));
        mongoTemplate.findAndRemove(query, ArchetypeIssueTag.class);
    }

    @Override
    public ArchetypeIssueTag queryById(String id) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, ArchetypeIssueTag.class);
    }

    @Override
    public List<ArchetypeIssueTag> queryByIssueId(String issue_id) {
        Query query = new Query(Criteria.where(Dict.ISSUE_ID).is(issue_id));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.DATE));
        return mongoTemplate.find(query, ArchetypeIssueTag.class);
    }

    @Override
    public List<ArchetypeIssueTag> queryByArchetypeId(String archetype_id) {
        Query query = new Query(Criteria.where(Dict.ARCHETYPE_ID).is(archetype_id));
        query.fields().exclude(Dict.OBJECTID);
        query.with(new Sort(Sort.Direction.DESC, Dict.DATE));
        return mongoTemplate.find(query, ArchetypeIssueTag.class);
    }
}
