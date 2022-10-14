package com.spdb.fdev.fdevapp.spdb.dao.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.spdb.dao.IAutoTestDao;
import com.spdb.fdev.fdevapp.spdb.entity.AutoTestEnv;
import net.sf.json.JSONObject;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;

@Repository
public class AutoTestDaoImpl implements IAutoTestDao {

    @Resource
    MongoTemplate mongoTemplate;

    @Override
    public AutoTestEnv find(AutoTestEnv autoTest) throws Exception {
        List<AutoTestEnv> list = mongoTemplate.find(Query.query(Criteria.where(Dict.GITLAB_PROJECT_ID).is(autoTest.getGitlab_project_id())), AutoTestEnv.class);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public AutoTestEnv update(AutoTestEnv autoTest) throws Exception {
        Query query = Query.query(Criteria.where(Dict.GITLAB_PROJECT_ID).is(autoTest.getGitlab_project_id()));

        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(autoTest);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();

        Update update = Update.update(Dict.GITLAB_PROJECT_ID, autoTest.getGitlab_project_id());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (!Dict.OBJECTID.equals(key)) {
                update.set(key, value);
            }
        }
        // 返回 查询到的数据
        this.mongoTemplate.upsert(query, update, AutoTestEnv.class);
        return this.mongoTemplate.findOne(query, AutoTestEnv.class);
    }
}
