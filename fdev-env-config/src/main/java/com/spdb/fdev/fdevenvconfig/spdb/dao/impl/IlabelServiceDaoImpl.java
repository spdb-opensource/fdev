package com.spdb.fdev.fdevenvconfig.spdb.dao.impl;

import com.spdb.fdev.fdevenvconfig.spdb.dao.IlabelServiceDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Labels;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Repository
public class IlabelServiceDaoImpl implements IlabelServiceDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public Map queryAllLabels() {
        Query query = new Query();
        List<Labels> labels = mongoTemplate.find(query, Labels.class);
        return labels.get(0).getLabels();
    }
}
