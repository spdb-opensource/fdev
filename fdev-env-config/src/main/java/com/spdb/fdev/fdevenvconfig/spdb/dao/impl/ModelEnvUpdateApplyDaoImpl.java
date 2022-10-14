package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.ModelEnvUpdateApplyDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelEnvUpdateApply;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Repository
public class ModelEnvUpdateApplyDaoImpl implements ModelEnvUpdateApplyDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(ModelEnvUpdateApply modelEnvUpdateApply) {
        ObjectId objectId = new ObjectId();
        modelEnvUpdateApply.set_id(objectId);
        modelEnvUpdateApply.setId(objectId.toString());
        // 新增时，初始化数据
        modelEnvUpdateApply.setStatus(Dict.CHECKING);
        modelEnvUpdateApply.setCheckTime("");
        modelEnvUpdateApply.setCreateTime(DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        modelEnvUpdateApply.setUpdateTime(DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        mongoTemplate.save(modelEnvUpdateApply);
    }

    @Override
    public ModelEnvUpdateApply get(String id) {
        return mongoTemplate.findOne(new Query(Criteria.where(Dict.ID).is(id)), ModelEnvUpdateApply.class);
    }

    @Override
    public void updateStatus(String id, String status) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.STATUS, status)
                .set(Dict.UPDATE_TIME, DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        if (Dict.FINISHED.equals(status)) {
            update.set(Dict.CHECK_TIME, DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        }
        mongoTemplate.findAndModify(query, update, ModelEnvUpdateApply.class);
    }

    @Override
    public void update(String id, List<Map<String, Object>> variables, String status) {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.VARIABLES, variables).set(Dict.STATUS, status)
                .set(Dict.UPDATE_TIME, DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        mongoTemplate.findAndModify(query, update, ModelEnvUpdateApply.class);
    }

    @Override
    public List<ModelEnvUpdateApply> listCheckingModelEnvUpdateApplys(String modelId, String email) {
        Criteria criteria = new Criteria();
        criteria.and(Constants.MODEL_ID).is(modelId).and(Dict.APPLY_EMAIL).ne(email).and(Dict.STATUS).is(Dict.CHECKING);
        return mongoTemplate.find(new Query(criteria), ModelEnvUpdateApply.class);
    }

    @Override
    public List<ModelEnvUpdateApply> listCheckingModelEnvUpdateApplys(String modelId, String envId, String email) {
        Criteria criteria = new Criteria();
        criteria.and(Constants.MODEL_ID).is(modelId).and(Constants.ENV_ID).is(envId).and(Dict.APPLY_EMAIL).is(email).and(Dict.STATUS).is(Dict.CHECKING);
        return mongoTemplate.find(new Query(criteria), ModelEnvUpdateApply.class);
    }

    @Override
    public Map<String, Object> listByPage(String modelId, String appUserId, String status, int page, int perPage) {
        Criteria criteria = new Criteria();
        if (StringUtils.isNotEmpty(modelId)) {
            criteria.and(Constants.MODEL_ID).is(modelId);
        }
        if (StringUtils.isNotEmpty(appUserId)) {
            criteria.and(Dict.APPLY_USER_ID).is(appUserId);
        }
        if (StringUtils.isNotEmpty(status)) {
            Pattern statusPattern = Pattern.compile("^.*" + status + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.STATUS).regex(statusPattern);
        }
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        Long total = mongoTemplate.count(query, ModelEnvUpdateApply.class);
        query.skip((page - 1L) * perPage).limit(perPage);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(Dict.TOTAL, total);
        responseMap.put(Dict.LIST, mongoTemplate.find(query, ModelEnvUpdateApply.class));
        return responseMap;
    }

    @Override
    public List<ModelEnvUpdateApply> listCheckingByDate(String date) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.STATUS).is(Dict.CHECKING).and(Dict.CREATE_TIME).lt(date);
        return mongoTemplate.find(new Query(criteria), ModelEnvUpdateApply.class);
    }

    @Override
    public void updateStatus(List<String> ids) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.ID).in(ids);
        Update update = Update.update(Dict.STATUS, Dict.OVERTIME);
        mongoTemplate.updateMulti(new Query(criteria), update, ModelEnvUpdateApply.class);
    }

}
