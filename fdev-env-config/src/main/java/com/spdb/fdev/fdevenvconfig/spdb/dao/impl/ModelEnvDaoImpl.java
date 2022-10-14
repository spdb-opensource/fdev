package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelEnvDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelEnv;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author xxx
 * @date 2019/7/5 13:21
 */
@Repository
public class ModelEnvDaoImpl implements IModelEnvDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public List<ModelEnv> query(ModelEnv modelEnv) throws Exception {
        List<ModelEnv> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = modelEnv == null ? "{}" : objectMapper.writeValueAsString(modelEnv);
        BasicDBObject queryJson = BasicDBObject.parse(json);

        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        c.and(Constants.STATUS).ne(Constants.ZERO);
        AggregationOperation project = Aggregation.project().andExclude(Constants.OBJECTID);
        AggregationOperation match = Aggregation.match(c);
        AggregationResults<ModelEnv> docs = this.mongoTemplate.aggregate(Aggregation.newAggregation(project, match), Constants.MODEL_ENV, ModelEnv.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public ModelEnv add(ModelEnv modelEnv) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        modelEnv.set_id(objectId);
        modelEnv.setId(id);
        return this.mongoTemplate.save(modelEnv);
    }

    @Override
    public void delete(ModelEnv modelEnv) {
        Query query = Query.query(Criteria.where(Constants.ID).is(modelEnv.getId()));
        Update update = Update.update(Constants.ID, modelEnv.getId());
        update.set(Constants.STATUS, Constants.STATUS_LOSE).set(Constants.OPNO, modelEnv.getOpno())
                .set(Constants.UTIME, DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        this.mongoTemplate.findAndModify(query, update, ModelEnv.class);
    }

    @Override
    public ModelEnv update(ModelEnv modelEnv) throws Exception {
        Query query = Query.query(Criteria.where(Constants.ID).is(modelEnv.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(modelEnv);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Constants.ID, modelEnv.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, ModelEnv.class);
        return this.mongoTemplate.findOne(query, ModelEnv.class);
    }

    /**
     * 通过 实体集合 和 环境 查询 实体与环境的映射
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<ModelEnv> queryModelEnvByModelsAndEnvId(Set<String> models, String envId) {
        Query query = Query.query(Criteria.where("model_id").in(models).and("env_id").is(envId).and(Constants.STATUS).ne(Constants.ZERO));
        return this.mongoTemplate.find(query, ModelEnv.class);
    }

    @Override
    public ModelEnv queryId(ModelEnv modelEnv) {
        Query query = Query.query(Criteria.where(Constants.ID).is(modelEnv.getId()).and(Constants.STATUS).ne(Constants.ZERO));
        return this.mongoTemplate.findOne(query, ModelEnv.class);
    }

    @Override
    public void deleteModelEnv(String field, String param, String opno) {
        Query query = null;
        if (Constants.ENV_ID.equals(field)) {
            query = Query.query(Criteria.where(Constants.ENV_ID).is(param).and(Dict.STATUS).ne(Constants.ZERO));
        } else if (Constants.MODEL_ID.equals(field)) {
            query = Query.query(Criteria.where(Constants.MODEL_ID).is(param).and(Dict.STATUS).ne(Constants.ZERO));
        }
        Update update = Update.update(Dict.STATUS, Constants.ZERO).set(Constants.OPNO, opno)
                .set(Constants.UTIME, DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        if (query != null) {
            mongoTemplate.updateMulti(query, update, ModelEnv.class);
        }
    }

    @Override
    public Map<String, Object> pageQuery(Map map) {
        Map<String, Object> responseMap = new HashMap<>();
        Criteria c = new Criteria();
        c.and(Constants.STATUS).ne(Constants.ZERO);
        if (StringUtils.isNotBlank((String) map.get(Constants.MODEL_ID))) {
            c.and(Constants.MODEL_ID).is(map.get(Constants.MODEL_ID));
        }
        if (StringUtils.isNotBlank((String) map.get(Constants.ENV_ID))) {
            c.and(Constants.ENV_ID).is(map.get(Constants.ENV_ID));
        }
        if (CollectionUtils.isNotEmpty((Collection) map.get(Constants.LABELS))) {
            c.and(Constants.ENV_ID).in((Collection) map.get(Constants.LABELS));
        }
        Query query = new Query(c);
        Long total = mongoTemplate.count(query, ModelEnv.class);
        responseMap.put(Dict.TOTAL, total);
        long startPage = ((Integer) map.get(Dict.PAGE) - 1) * ((Integer) map.get(Dict.PER_PAGE));
        query.skip(startPage).limit((Integer) map.get(Dict.PER_PAGE));
        responseMap.put(Dict.LIST, mongoTemplate.find(query, ModelEnv.class));
        return responseMap;
    }

    @Override
    public List<ModelEnv> getModelEnvList(List<String> modelIds, List<String> envIds) {
        Criteria criteria = new Criteria();
        if (CollectionUtils.isNotEmpty(modelIds)) {
            criteria.and(Constants.MODEL_ID).in(modelIds);
        }
        if (CollectionUtils.isNotEmpty(envIds)) {
            criteria.and(Constants.ENV_ID).in(envIds);
        }
        criteria.and(Dict.STATUS).ne(Constants.STATUS_LOSE);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.ASC, Constants.MODEL_ID));
        return mongoTemplate.find(query, ModelEnv.class);
    }
    
    @Override
    public ModelEnv  getModelEnv(String modelId,String envId) {
        Criteria criteria = new Criteria();
        criteria.and(Constants.MODEL_ID).is(modelId).and(Constants.ENV_ID).is(envId).and(Dict.STATUS).ne(Constants.STATUS_LOSE);;
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, ModelEnv.class);
    }
    
    

}
