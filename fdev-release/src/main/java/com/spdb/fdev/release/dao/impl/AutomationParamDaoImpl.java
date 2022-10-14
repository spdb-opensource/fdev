package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.release.dao.IAutomationParamDao;
import com.spdb.fdev.release.entity.AutomationParam;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class AutomationParamDaoImpl implements IAutomationParamDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public List<AutomationParam> query() {
        Query query = new Query();
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, AutomationParam.class);
    }

    @Override
    public void save(AutomationParam automationParam) {
        mongoTemplate.save(automationParam);
    }

    @Override
    public void update(AutomationParam automationParam) {
        Query query = new Query(Criteria.where(Dict.ID).is(automationParam.getId()));
        Update update = Update.update(Dict.KEY, automationParam.getKey())
                .set(Dict.VALUE, automationParam.getValue())
                .set(Dict.DESCRIPTION, automationParam.getDescription());
        mongoTemplate.findAndModify(query, update, AutomationParam.class);
    }

    @Override
    public void deleteById(String id) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        mongoTemplate.findAndRemove(query, AutomationParam.class);
    }

    @Override
    public AutomationParam queryByKey(String key) {
        Query query = new Query(Criteria.where(Dict.KEY).is(key));
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, AutomationParam.class);
    }

}
