package com.spdb.fdev.freport.spdb.dao;

import com.spdb.fdev.freport.base.config.MongoConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class BaseDao {

    @Autowired
    private MongoConfig mongoConfig;

    protected MongoTemplate getMongoTempate(String dsId) {
        return mongoConfig.getMongoTemplate(dsId);
    }

}
