package com.spdb.fdev.pipeline.dao.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.pipeline.dao.IDigitalRateDao;
import com.spdb.fdev.pipeline.entity.PipelineDigitalRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DigitalDaoImpl implements IDigitalRateDao {

    private static final Logger logger = LoggerFactory.getLogger(DigitalDaoImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public PipelineDigitalRate queryByNameId(String nameId) {

        Criteria criteria = new Criteria();
        criteria.and(Dict.NAMEID).is(nameId);
        Query query = new Query(criteria);
        return this.mongoTemplate.findOne(query, PipelineDigitalRate.class, Dict.PIPELINEDIGITALRATE);
    }

    @Override
    public void upsert(PipelineDigitalRate upsertEnity) {
        if (CommonUtils.isNullOrEmpty(upsertEnity.getNameId())) {
            logger.error(" pipelineDigital upserting but nameId is null or \"\", upsert cancel!");
            return;
        }
        Criteria criteria = new Criteria();
        criteria.and(Dict.NAMEID).is(upsertEnity.getNameId());
        Query query = new Query(criteria);
        Update update = new Update();
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(upsertEnity);
        for (Map.Entry<String, Object> objectEntry : jsonObject.entrySet()) {
            String key = objectEntry.getKey();
            Object value = objectEntry.getValue();
            if (Dict._ID.equals(key) || Dict.ID.equals(key) || Dict.NAMEID.equals(key)) {
                continue;
            }
            update.set(key, value);
        }
        this.mongoTemplate.upsert(query, update, PipelineDigitalRate.class, Dict.PIPELINEDIGITALRATE);
    }
}
