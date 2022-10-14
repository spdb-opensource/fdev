package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dao.IDemandDocDao;
import com.spdb.fdev.fdemand.spdb.entity.DemandDoc;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
public class DemandDocDaoImpl implements IDemandDocDao {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Resource
    private MongoTemplate mongoTemplate;

    @Autowired
    private DemandDoc demandDoc;

    @Override
    public List queryDemandDocPagination(Integer start, Integer pageSize, String demand_id, String demand_doc_type, String demand_kind) {
        Criteria criteria = Criteria.where(Dict.DEMAND_ID).is(demand_id);

        if (!CommonUtils.isNullOrEmpty(demand_doc_type)) {
            criteria.and(Dict.DEMAND_DOC_TYPE).is(demand_doc_type);
        }
        if (!CommonUtils.isNullOrEmpty(demand_kind)) {
            criteria.and(Dict.DEMAND_KIND).is(demand_kind);
        }

        Query query = new Query(criteria);

        if (!pageSize.toString().equals("0")) {
            query.limit(pageSize).skip(start);  //分页
        }
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, DemandDoc.class);
    }

    @Override
    public Long queryCountDemandDoc(String demand_id, String demand_doc_type, String demand_kind) {
        Criteria criteria = Criteria.where(Dict.DEMAND_ID).is(demand_id);

        if (!CommonUtils.isNullOrEmpty(demand_doc_type)) {
            criteria.and(Dict.DEMAND_DOC_TYPE).is(demand_doc_type);
        }

        if (!CommonUtils.isNullOrEmpty(demand_kind)) {
            criteria.and(Dict.DEMAND_KIND).is(demand_kind);
        }

        Query query = new Query(criteria);

        return mongoTemplate.count(query, DemandDoc.class);
    }


    @Override
    public DemandDoc save(DemandDoc demandDoc) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        demandDoc.set_id(objectId);
        demandDoc.setId(id);
        return mongoTemplate.save(demandDoc);
    }

    @Override
    public List<DemandDoc> query(DemandDoc demand) throws Exception {
        List<DemandDoc> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        String json = demand == null ? "{}" : objectMapper.writeValueAsString(demand);
        BasicDBObject demandJson = BasicDBObject.parse(json);
        Iterator<String> it = demandJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = demandJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation project = Aggregation.project().andExclude(Dict.OBJECTID);
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<DemandDoc> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.DEMAND_DOC, DemandDoc.class);
        docs.forEach(doc -> {
            result.add(doc);
        });
        return result;
    }

    @Override//查询数据库所有数据
    public List<DemandDoc> queryAll(DemandDoc demand) throws Exception {
        List<DemandDoc> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        String json = demand == null ? "{}" : objectMapper.writeValueAsString(demand);
        BasicDBObject demandJson = BasicDBObject.parse(json);
        Iterator<String> it = demandJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = demandJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation project = Aggregation.project().andExclude(Dict.OBJECTID);
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<DemandDoc> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Dict.DEMAND_DOC, DemandDoc.class);
        docs.forEach(doc -> {
            result.add(doc);
        });
        return result;
    }

    @Override//根据id进行查询
    public DemandDoc queryById(String id) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, DemandDoc.class);
    }

    @Override//根据id进行更新
    public DemandDoc updateById(DemandDoc demandDoc) throws Exception {
        Query query = Query.query(Criteria.where(Dict.ID).is(demandDoc.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(demandDoc);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Dict.ID, demandDoc.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (Dict.OBJECTID.equals(key)) {
                continue;
            }
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, DemandDoc.class);
        return this.mongoTemplate.findOne(query, DemandDoc.class);
    }

    @Override//根据id进行删除
    public void deleteById(String id) {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        mongoTemplate.findAndRemove(query, DemandDoc.class);
    }

    @Override
    public void saveBatch(List<DemandDoc> demandDocList) throws Exception {
        for (DemandDoc demandDoc : demandDocList) {
            ObjectId objectId = new ObjectId();
            demandDoc.set_id(objectId);
            demandDoc.setId(objectId.toString());
        }
        mongoTemplate.insert(demandDocList, DemandDoc.class);
    }
}
