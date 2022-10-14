package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IEnvironmentDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Environment;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author xxx
 * @date 2019/7/5 13:21
 */
@Repository
@RefreshScope
public class EnvironmentDaoImpl implements IEnvironmentDao {

    @Value("${envTest}")
    private String envTest;

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public List<Environment> query(Environment environment) {
        List<Environment> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = null;
        try {
            json = environment == null ? "{}" : objectMapper.writeValueAsString(environment);
        } catch (JsonProcessingException e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{e.getMessage()});
        }
        BasicDBObject queryJson = BasicDBObject.parse(json);

        Iterator<String> it = queryJson.keySet().iterator();
        Criteria c = new Criteria();
        while (it.hasNext()) {
            String key = it.next();
            Object value = queryJson.get(key);
            c.and(key).is(value);
        }
        AggregationOperation project = Aggregation.project().andExclude(Constants.OBJECTID);
        AggregationOperation match = Aggregation.match(c);
        SortOperation sort = Aggregation.sort(new Sort(Sort.Direction.ASC, Dict.NAME_EN));
        AggregationResults<Environment> docs = this.mongoTemplate.aggregate(Aggregation.newAggregation(project, match, sort),
                Constants.ENVIRONMENT, Environment.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public Environment save(Environment environment) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        environment.set_id(objectId);
        environment.setId(id);
        return this.mongoTemplate.save(environment);

    }

    @Override
    public void delete(String id, String opno) {
        Query query = new Query(Criteria.where(Constants.ID).is(id));
        Update update = Update.update(Constants.STATUS, Constants.STATUS_LOSE)
                .set(Constants.OPNO, opno)
                .set(Constants.UTIME, DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        this.mongoTemplate.findAndModify(query, update, Environment.class);
    }

    @Override
    public Environment update(Environment environment) throws Exception {
        Query query = Query.query(Criteria.where(Constants.ID).is(environment.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(environment);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Constants.ID, environment.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, Environment.class);
        return this.mongoTemplate.findOne(query, Environment.class);
    }

    @Override
    public Environment queryById(Environment environment) {
        Query query = Query.query(Criteria.where(Constants.ID).is(environment.getId()));
        return this.mongoTemplate.findOne(query, Environment.class);
    }

    @Override
    public Environment queryById(String id) {
        return mongoTemplate.findOne(new Query(Criteria.where(Constants.ID).is(id)), Environment.class);
    }

    @Override
    public List<Environment> queryByLabels(List<String> labels, String status) {
        Query query = new Query(Criteria.where(Constants.STATUS).is(status).and(Constants.LABELS).in(labels));
        return this.mongoTemplate.find(query, Environment.class);
    }

    @Override
    public List<Environment> getByLabels(List<String> labels, String status) {
        if (labels.size() == 1) {
            Query query = new Query(Criteria.where(Constants.STATUS).is(status).and(Constants.LABELS).in(labels));
            return this.mongoTemplate.find(query, Environment.class);
        }
        Query query = new Query(Criteria.where(Constants.STATUS).is(status).and(Constants.LABELS).is(labels));
        return this.mongoTemplate.find(query, Environment.class);
    }

    @Override
    public List<Environment> queryAutoEnv(Environment environment) throws Exception {
        Criteria criteria = new Criteria();
        Pattern pattern = Pattern.compile(envTest, Pattern.CASE_INSENSITIVE);
        criteria.and(Constants.NAME_EN).regex(pattern);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Environment.class);

    }

    @Override
    public Environment queryByNameEn(String envNameEn) {
        Query query = Query.query(Criteria.where(Constants.NAME_EN).is(envNameEn).and(Dict.STATUS).ne(Constants.ZERO));
        return this.mongoTemplate.findOne(query, Environment.class);
    }

    @Override
    public List<Environment> queryByLabelsFuzzy(List<String> labels) {
        Query query = new Query(Criteria.where(Constants.STATUS).is(Constants.ONE).and(Constants.LABELS).all(labels));
        return this.mongoTemplate.find(query, Environment.class);
    }

    @Override
    public Environment queryByNameCN(String env_name) {
        Query query = Query.query(Criteria.where(Constants.NAME_CN).is(env_name).and(Dict.STATUS).ne(Constants.ZERO));
        return this.mongoTemplate.findOne(query, Environment.class);
    }

}
