package com.spdb.fdev.fdevinterface.spdb.dao.impl;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.dao.CentralJsonDao;
import com.spdb.fdev.fdevinterface.spdb.entity.CentralJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * @author xxx
 * @date 2020/7/28 19:48
 */
@Repository
public class CentralJsonDaoImpl implements CentralJsonDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public CentralJson centralJsonDao(String env) {
        return mongoTemplate.findOne(new Query(Criteria.where(Dict.ENV).is(env)), CentralJson.class);
    }

    @Override
    public void updateCentralJson(String env, Map<String, Object> newCentralJson) {
        Update update = Update.update(Dict.CENTRAL_JSON, newCentralJson)
                .set(Dict.UPDATE_TIME, TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        mongoTemplate.findAndModify(new Query(Criteria.where(Dict.ENV).is(env)), update, CentralJson.class);
    }
}
