package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.AppEnvMappingDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppEnvMapping;
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
import java.util.regex.Pattern;


@Repository
public class AppEnvMappingDaoImpl implements AppEnvMappingDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public AppEnvMapping save(AppEnvMapping appEnvMapping) {
        ObjectId objectId = new ObjectId();
        appEnvMapping.set_id(objectId);
        appEnvMapping.setId(objectId.toString());
        appEnvMapping.setCreateTime(DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        mongoTemplate.save(appEnvMapping, "app-env-map");
        return appEnvMapping;
    }

    @Override
    public List<AppEnvMapping> query(AppEnvMapping appEnvMapping) throws Exception {

        List<AppEnvMapping> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = appEnvMapping == null ? "{}" : objectMapper.writeValueAsString(appEnvMapping);
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
        AggregationResults<AppEnvMapping> docs = mongoTemplate.aggregate(Aggregation.newAggregation(project, match), "app-env-map", AppEnvMapping.class);
        docs.forEach(result::add);
        return result;
    }

    @Override
    public void update(AppEnvMapping appEnvMapping) {
        Query query = new Query(Criteria.where(Dict.APPID).is(appEnvMapping.getApp_id()).and(Dict.ENV_ID).is(appEnvMapping.getEnv_id()));
        Update update = Update.update(Dict.UPDATE_TIME, DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        mongoTemplate.findAndModify(query, update, AppEnvMapping.class);
    }

    @Override
    public void delete(AppEnvMapping envMapping) {
        Query query = new Query(Criteria.where(Dict.APPID).is(envMapping.getApp_id()));
        this.mongoTemplate.remove(query, AppEnvMapping.class);
    }

    @Override
    public Map<String, Object> queryByPage(AppEnvMapping appEnvMapping1, int page, int per_page) {
        Query query = new Query();
        if (!CommonUtils.isNullOrEmpty(appEnvMapping1.getEnv_id())) {
            query = new Query(Criteria.where(Dict.ENV_ID).is(appEnvMapping1.getEnv_id()));
        }
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        Long total = mongoTemplate.count(query, AppEnvMapping.class);
        query.skip((page - 1L) * per_page).limit(per_page);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(Dict.TOTAL, total);
        responseMap.put(Dict.LIST, mongoTemplate.find(query, AppEnvMapping.class));
        return responseMap;
    }

    @Override
    public Map<String, Object> queryByPage(List<String> appIdList, String appId, String envId, List<String> tags, String network, int page, int perPage) {
        Criteria criteria = new Criteria();
        if (StringUtils.isEmpty(appId)) {
            criteria.and(Dict.APPID).in(appIdList);
        } else {
            criteria.and(Dict.APPID).is(appId);
        }
        if (StringUtils.isEmpty(envId)) {
            Pattern pattern = Pattern.compile("^.*" + network + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.NETWORK).regex(pattern);
            criteria.and(Dict.TAGS).in(tags);
        } else {
            criteria.and(Dict.ENV_ID).is(envId);
        }
        Query query = new Query(criteria);
        Long total = mongoTemplate.count(query, AppEnvMapping.class);
        query.skip(((page - 1L) * perPage)).limit(perPage);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(Dict.TOTAL, total);
        responseMap.put(Dict.LIST, mongoTemplate.find(query, AppEnvMapping.class));
        return responseMap;
    }

    @Override
    public List<AppEnvMapping> queryByAppIds(List<String> appIdList) {
        return mongoTemplate.find(new Query(Criteria.where(Dict.APPID).in(appIdList)), AppEnvMapping.class);
    }

    /**
     * 通过应用id查询该应用是否绑定过生产环境
     *
     * @param appId
     * @return
     */
    @Override
    public List<Map> queryProEnvByAppId(String appId) {
        Criteria criteria = Criteria.where(Constants.APP_ID).is(appId).and(Constants.ENV_ID).ne(null);
        Criteria envInfoCriteria = Criteria.where("envInfo.status").is("1");
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.lookup(Constants.ENVIRONMENT, Dict.ENV_ID, Dict.ID, "envInfo"),
                Aggregation.project("envInfo"),
                Aggregation.unwind("envInfo"),
                Aggregation.match(envInfoCriteria),
                Aggregation.project("envInfo.id", "envInfo.name_en", "envInfo.name_cn", "envInfo.labels")
        );
        return mongoTemplate.aggregate(aggregation, Constants.APP_ENV_MAP, Map.class).getMappedResults();
    }

    /**
     * 通过应用id查询该应用是否绑定过的SCC生产环境
     *
     * @param appId
     * @return
     */
    @Override
    public List<Map> querySccProEnvByAppId(String appId) {
        Criteria criteria = Criteria.where(Constants.APP_ID).is(appId).and(Dict.SCC_PRO_ID).ne(null);
        Criteria envInfoCriteria = Criteria.where("envInfo.status").is("1");
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.lookup(Constants.ENVIRONMENT, Dict.SCC_PRO_ID, Dict.ID, "envInfo"),
                Aggregation.project("envInfo"),
                Aggregation.unwind("envInfo"),
                Aggregation.match(envInfoCriteria),
                Aggregation.project("envInfo.id", "envInfo.name_en", "envInfo.name_cn", "envInfo.labels")
        );
        return mongoTemplate.aggregate(aggregation, Constants.APP_ENV_MAP, Map.class).getMappedResults();
    }

    @Override
    public List<AppEnvMapping> queryEnvByAppId(String appId) {
        return mongoTemplate.find(new Query(Criteria.where(Dict.APPID).is(appId)), AppEnvMapping.class);
    }
}
