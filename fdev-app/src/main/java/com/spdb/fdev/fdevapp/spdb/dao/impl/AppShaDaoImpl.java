package com.spdb.fdev.fdevapp.spdb.dao.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.spdb.dao.IAppShaDao;
import com.spdb.fdev.fdevapp.spdb.entity.AppSha;
import net.sf.json.JSONObject;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Iterator;

/**
 * @author xxx
 * @date 2019/7/1 18:13
 */
@Repository
public class AppShaDaoImpl implements IAppShaDao {

    private static Logger logger = LoggerFactory.getLogger(AppShaDaoImpl.class);
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public AppSha getAppSha(String key, Integer gitlab_id) {
        return this.mongoTemplate.findOne(Query.query(Criteria.where(key).is(gitlab_id)), AppSha.class);
    }

    @Override
    public AppSha addAppSha(AppSha appSha) {
        AppSha appSha1 = null;
        ObjectId objectId = new ObjectId();
        appSha.set_id(objectId);
        appSha.setId(objectId.toString());
        appSha.setCtime(String.valueOf(System.currentTimeMillis()));
        try {
            appSha1 =  this.mongoTemplate.save(appSha);
        } catch (DuplicateKeyException e) {
            logger.error("save appSha error");
        }
        return appSha1;
    }

    @Override
    public AppSha updateAppSha(AppSha appSha) throws Exception{
        appSha.setUtime(String.valueOf(System.currentTimeMillis()));

        Query query = Query.query(Criteria.where(Dict.ID).is(appSha.getId()));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = objectMapper.writeValueAsString(appSha);
        JSONObject pJson = JSONObject.fromObject(json);
        Iterator<Object> it = pJson.keys();

        Update update = Update.update(Dict.ID, appSha.getId());
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = pJson.get(key);
            if (!Dict.OBJECTID.equals(key)) {
                update.set(key, value);
            }
        }
        // 返回 查询到的数据
        this.mongoTemplate.findAndModify(query, update, AppSha.class);
        return this.mongoTemplate.findOne(query, AppSha.class);
    }
}
