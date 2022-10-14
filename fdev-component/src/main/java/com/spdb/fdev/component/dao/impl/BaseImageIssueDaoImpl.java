package com.spdb.fdev.component.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.dao.IBaseImageIssueDao;
import com.spdb.fdev.component.entity.BaseImageInfo;
import com.spdb.fdev.component.entity.BaseImageIssue;
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
public class BaseImageIssueDaoImpl implements IBaseImageIssueDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List query(BaseImageIssue baseImageIssue) throws Exception {
        List<BaseImageIssue> result = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = baseImageIssue == null ? "{}" : objectMapper.writeValueAsString(baseImageIssue);

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
        AggregationResults<BaseImageIssue> docs = mongoTemplate.aggregate(Aggregation.newAggregation(opetations), Dict.BASEIMAGE_ISSUE, BaseImageIssue.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public BaseImageIssue save(BaseImageIssue baseImageIssue) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        baseImageIssue.set_id(objectId);
        baseImageIssue.setId(id);
        return mongoTemplate.save(baseImageIssue);
    }

    @Override
    public BaseImageIssue update(BaseImageIssue baseImageIssue) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(baseImageIssue.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(baseImageIssue);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();

        Update update = Update.update(Dict.ID, baseImageIssue.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        mongoTemplate.findAndModify(query, update, BaseImageIssue.class);
        return this.mongoTemplate.findOne(query, BaseImageIssue.class);
    }

    @Override
    public BaseImageIssue queryById(String id) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, BaseImageIssue.class);
    }

    @Override
    public BaseImageIssue queryByNameAndBranch(String name, String branch) {
        Query query = new Query(Criteria.where(Dict.NAME).is(name).and(Dict.BRANCH).is(branch));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, BaseImageIssue.class);
    }

    @Override
    public void delete(BaseImageIssue baseImageIssue) throws Exception {
        Query query = new Query(Criteria.where(Dict.ID).is(baseImageIssue.getId()));
        mongoTemplate.findAndRemove(query, BaseImageIssue.class);
    }

    @Override
    public List<Map> queryQrmntsData(String userId) {
        Criteria c = new Criteria();
        c.and(Dict.ASSIGNEE).is(userId);
        AggregationOperation match = Aggregation.match(c);
        AggregationOperation lookup = Aggregation.lookup(Dict.BASEIMAGE_INFO, Dict.NAME, Dict.NAME, Dict.BASEIMAGE_INFO);
        Aggregation aggregation = Aggregation.newAggregation(lookup, match);
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, Dict.BASEIMAGE_ISSUE, Map.class);
        List<Map> results = new ArrayList<>();
        docs.forEach(doc -> {
            if (((List) doc.get(Dict.BASEIMAGE_INFO)).size() > 0) {
                doc.put(Dict.BASEIMAGE_INFO, ((List) doc.get(Dict.BASEIMAGE_INFO)).get(0));
                results.add(doc);
            }
        });
        return results;
    }

    @Override
    public List<Map> queryIssueDelay(Map requestParam) {
        List<Map> results = new ArrayList<>();
        Criteria c = new Criteria();
        String id = (String) requestParam.get(Dict.ID);
        if (!CommonUtils.isNullOrEmpty(id)) {
            BaseImageInfo baseImageInfo = mongoTemplate.findOne(new Query(Criteria.where(Dict.ID).is(id)), BaseImageInfo.class);
            //镜像不存在则直接返回
            if (CommonUtils.isNullOrEmpty(baseImageInfo))
                return results;
            //以name关联
            c.and(Dict.NAME).is(baseImageInfo.getName());
        }
        if (!CommonUtils.isNullOrEmpty(requestParam.get(Dict.MEMBER))) {
            c.and(Dict.ASSIGNEE).is(requestParam.get(Dict.MEMBER));
        }
        AggregationOperation match = Aggregation.match(c);
        AggregationOperation component_info = Aggregation.lookup(Dict.BASEIMAGE_INFO, Dict.NAME, Dict.NAME, Dict.BASEIMAGE_INFO);
        Aggregation aggregation = Aggregation.newAggregation(component_info, match);
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, Dict.BASEIMAGE_ISSUE, Map.class);
        docs.forEach(doc -> {
            if (((List) doc.get(Dict.BASEIMAGE_INFO)).size() > 0) {
                doc.put(Dict.BASEIMAGE_INFO, ((List) doc.get(Dict.BASEIMAGE_INFO)).get(0));
            }
            results.add(doc);
        });
        return results;
    }
}
