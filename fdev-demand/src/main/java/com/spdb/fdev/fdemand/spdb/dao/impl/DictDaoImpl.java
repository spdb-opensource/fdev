package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dao.DictDao;
import com.spdb.fdev.fdemand.spdb.entity.DictEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class DictDaoImpl implements DictDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<DictEntity> query(DictEntity dict) {
        Query query = new Query();
        if (!CommonUtils.isNullOrEmpty(dict.getType())) {
            query.addCriteria(Criteria.where(Dict.TYPE).is(dict.getType()));
        }
        return mongoTemplate.find(query, DictEntity.class);
    }

    @Override
    public DictEntity queryByCode(String code) {
        return mongoTemplate.findOne(new Query().addCriteria(Criteria.where(Dict.CODE).is(code)), DictEntity.class);
    }

    @Override
    public List<DictEntity> queryByTypes(Set<String> types) {
        return mongoTemplate.find(new Query().addCriteria(Criteria.where(Dict.TYPE).in(types)), DictEntity.class);
    }

    @Override
    public List<DictEntity> queryByCodes(List<String> codes) {
        Query query = Query.query(Criteria.where(Dict.CODE).in(codes));
        return mongoTemplate.find(query, DictEntity.class);
    }
}
