package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IAppConfigMappingDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppConfigMapping;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * create by Idea
 *
 * @Author aric
 * @Date 2020/1/7 17:08
 * @Version 1.0
 */
@Repository
public class AppConfigMappingDaoImpl implements IAppConfigMappingDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void add(AppConfigMapping appConfigMapping) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        appConfigMapping.set_id(objectId);
        appConfigMapping.setId(id);
        appConfigMapping.setCtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_ISO_FORMAT));
        appConfigMapping.setUtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_ISO_FORMAT));
        mongoTemplate.save(appConfigMapping, "app-config-map");
    }

    @Override
    public List<AppConfigMapping> query(AppConfigMapping appConfigMapping) throws Exception {
        List<AppConfigMapping> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = appConfigMapping == null ? "{}" : objectMapper.writeValueAsString(appConfigMapping);
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
        AggregationResults<AppConfigMapping> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), "app-config-map", AppConfigMapping.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public List<AppConfigMapping> query(Integer appId, String branch, String node) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.APP_ID).is(appId);
        criteria.and(Dict.BRANCH).is(branch);
        // node为1表示为node应用的配置
        if ("1".equals(node)) {
            criteria.and(Dict.NODE).is("1");
        } else {
            criteria.and(Dict.NODE).ne("1");
        }
        return mongoTemplate.find(new Query(criteria), AppConfigMapping.class);
    }

    @Override
    public AppConfigMapping update(AppConfigMapping appConfigMapping) throws Exception {
        Query query = Query.query(Criteria.where(Constants.ID).is(appConfigMapping.getId()));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String json = objectMapper.writeValueAsString(appConfigMapping);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();
        Update update = Update.update(Constants.ID, appConfigMapping.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            update.set(key, value);
        }
        this.mongoTemplate.findAndModify(query, update, AppConfigMapping.class);
        return this.mongoTemplate.findOne(query, AppConfigMapping.class);
    }

    @Override
    public List<AppConfigMapping> getAppConfigMapping(String modelName, String field) {
        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where(Dict.MODEL_NAME).is(modelName));
        if (!StringUtils.isEmpty(field)) {
            criteria.and(Dict.FIELD).in(field);
        }
        Query query = new Query(Criteria.where(Dict.MODEL_FIELD).elemMatch(criteria));
        query.fields().include(Dict.APP_ID).include(Dict.BRANCH);
        return mongoTemplate.find(query, AppConfigMapping.class);
    }

    /**
     * @param modelName
     * @param field
     * @param branchList
     * @return
     */
    @Override
    public List<AppConfigMapping> getAppConfigMapping(String modelName, String field, List<String> branchList) {
        Criteria elemCriteria = new Criteria();
        elemCriteria.and(Dict.MODEL_NAME).is(modelName);
        if (!StringUtils.isEmpty(field)) {
            elemCriteria.and(Dict.FIELD).in(field);
        }
        Criteria criteria = new Criteria();
        criteria.and(Dict.MODEL_FIELD).elemMatch(elemCriteria);
        Criteria masterCriteria = null;
        Criteria sitCriteria = null;
        Criteria releaseCriteria = null;
        for (String branch : branchList) {
            Pattern pattern = Pattern.compile("^.*" + branch + ".*$", Pattern.CASE_INSENSITIVE);
            if (Dict.MASTER.equals(branch)) {
                masterCriteria = new Criteria().andOperator(criteria).and(Dict.BRANCH).regex(pattern);
            } else if (Dict.SIT.equals(branch)) {
                sitCriteria = new Criteria().andOperator(criteria).and(Dict.BRANCH).regex(pattern);
            } else if (branch.startsWith(Dict.RELEASE)) {
                releaseCriteria = new Criteria().andOperator(criteria).and(Dict.BRANCH).regex(pattern);
            }
        }
        Criteria totalCriteria = new Criteria();
        if (sitCriteria != null) {
            if (releaseCriteria != null) {
                if (masterCriteria != null) {
                    totalCriteria.orOperator(sitCriteria, releaseCriteria, masterCriteria);
                } else {
                    totalCriteria.orOperator(sitCriteria, releaseCriteria);
                }
            } else {
                if (masterCriteria != null) {
                    totalCriteria.orOperator(sitCriteria, masterCriteria);
                } else {
                    totalCriteria.orOperator(sitCriteria);
                }
            }
        } else {
            if (releaseCriteria != null) {
                if (masterCriteria != null) {
                    totalCriteria.orOperator(releaseCriteria, masterCriteria);
                } else {
                    totalCriteria.orOperator(releaseCriteria);
                }
            } else {
                if (masterCriteria != null) {
                    totalCriteria.orOperator(masterCriteria);
                }
            }
        }
        Query query = new Query(totalCriteria);
        query.fields().include(Dict.APP_ID).include(Dict.BRANCH);
        return mongoTemplate.find(query, AppConfigMapping.class);
    }

}
