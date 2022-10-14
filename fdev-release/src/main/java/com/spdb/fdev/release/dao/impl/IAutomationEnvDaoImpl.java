package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.release.dao.IAutomationEnvDao;
import com.spdb.fdev.release.entity.AutomationEnv;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Repository
public class IAutomationEnvDaoImpl implements IAutomationEnvDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public List<AutomationEnv> query() {
        Query query = new Query();
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, AutomationEnv.class);
    }

    @Override
    public void save(AutomationEnv automationEnv) {
        mongoTemplate.save(automationEnv);
    }

    @Override
    public void update(AutomationEnv automationEnv) {
        Query query = new Query(Criteria.where(Dict.ID).is(automationEnv.getId()));
        Update update = Update.update(Dict.ENV_NAME, automationEnv.getEnv_name())
                .set(Dict.DESCRIPTION, automationEnv.getDescription())
                .set(Dict.UPDATE_TIME, TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"))
                .set("platform",automationEnv.getPlatform())
                .set(Dict.FDEV_ENV_NAME, automationEnv.getFdev_env_name())
                .set(Dict.SCC_FDEV_ENV_NAME, automationEnv.getScc_fdev_env_name());
        mongoTemplate.findAndModify(query, update, AutomationEnv.class);
    }

    @Override
    public void deleteById(String id) {
        Query query = new Query(Criteria.where(Dict.ID).is(id));
        mongoTemplate.findAndRemove(query, AutomationEnv.class);
    }

    @Override
    public List<AutomationEnv> queryByEnvName(String content, List<String> platform) {
        Query query = new Query(Criteria.where(Dict.ENV_NAME).regex(content).and("platform").in(platform));
        return mongoTemplate.find(query, AutomationEnv.class);
    }
}
