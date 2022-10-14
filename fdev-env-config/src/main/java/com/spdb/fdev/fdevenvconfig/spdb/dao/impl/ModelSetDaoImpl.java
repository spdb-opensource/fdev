package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.ModelSetDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelSet;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ModelSetDaoImpl implements ModelSetDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveModelSet(ModelSet modelSet) {
        ObjectId objectId = new ObjectId();
        modelSet.set_id(objectId);
        modelSet.setId(objectId.toString());
        modelSet.setStatus(1);
        modelSet.setCreateTime(DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        modelSet.setUpdateTime(DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        mongoTemplate.save(modelSet);
    }

    @Override
    public Map<String, Object> listModelSetsByPage(String nameCn, String template, String id, int page, int perPage) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.STATUS).is(1);
        if (StringUtils.isNotEmpty(nameCn)) {
            Pattern pattern = Pattern.compile("^.*" + nameCn
                    + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Constants.NAME_CN).regex(pattern);
        }
        if (StringUtils.isNotEmpty(template)) {
            criteria.and(Dict.TEMPLATE).is(template);
        }
        if (StringUtils.isNotBlank(id)) {
            criteria.and(Dict.MODELS).is(id);
        }
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        Long total = mongoTemplate.count(query, ModelSet.class);
        query.skip((page - 1L) * perPage).limit(perPage);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put(Dict.TOTAL, total);
        responseMap.put(Dict.LIST, mongoTemplate.find(query, ModelSet.class));
        return responseMap;
    }

    @Override
    public void updateModelSet(String id, List<String> models, String opno) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.MODELS, models)
                .set(Constants.OPNO, opno)
                .set(Dict.UPDATE_TIME, DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        mongoTemplate.findAndModify(query, update, ModelSet.class);
    }

    @Override
    public void deleteModelSet(String id, String opno) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        Update update = Update.update(Dict.STATUS, 0)
                .set(Constants.OPNO, opno)
                .set(Dict.UPDATE_TIME, DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        mongoTemplate.findAndModify(query, update, ModelSet.class);
    }

    @Override
    public ModelSet getModelSetByName(String nameCn) {
        Criteria criteria = new Criteria();
        criteria.and(Constants.NAME_CN).is(nameCn).and(Dict.STATUS).is(1);
        return mongoTemplate.findOne(new Query(criteria), ModelSet.class);
    }

    @Override
    public ModelSet getModelSetByModels(List<String> models) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.MODELS).all(models).and(Dict.STATUS).is(1);
        return mongoTemplate.findOne(new Query(criteria), ModelSet.class);
    }

    @Override
    public ModelSet queryById(String id) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.ID).is(id).and(Dict.STATUS).is(1);
        return mongoTemplate.findOne(new Query(criteria), ModelSet.class);
    }

    @Override
    public ModelSet queryByName(String name) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.NAME_CN).is(name);
        return mongoTemplate.findOne(new Query(criteria), ModelSet.class);
    }

    @Override
    public ModelSet queryByNameContAllStatus(String name) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.NAME_CN).is(name);
        return mongoTemplate.findOne(new Query(criteria), ModelSet.class);
    }
}
