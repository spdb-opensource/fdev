package com.spdb.fdev.codeReview.spdb.dao.impl;

import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.base.utils.CommonUtils;
import com.spdb.fdev.codeReview.spdb.dao.IDictDao;
import com.spdb.fdev.codeReview.spdb.entity.DictEntity;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
@Component
public class DictDaoImpl implements IDictDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public DictEntity queryDictById(String itemType) {
        Query query = Query.query(Criteria.where(Dict.ID).is(itemType));
        query.fields().exclude(Dict.OBJECT_ID);
        return mongoTemplate.findOne(query, DictEntity.class);
    }

    @Override
    public DictEntity add(DictEntity dictEntity) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        dictEntity.setId(id);
        return mongoTemplate.save(dictEntity);
    }

    @Override
    public List<DictEntity> query(Map param) {
        Criteria criteria = new Criteria();
        if(!CommonUtils.isNullOrEmpty(param.get(Dict.TYPE))){
            criteria.and(Dict.TYPE).is(param.get(Dict.TYPE));
        }
        Query query = Query.query(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.SORT));
        return mongoTemplate.find(query,DictEntity.class);
    }

    @Override
    public DictEntity queryDictByKey(String itemType) {
        Query query = Query.query(Criteria.where(Dict.KEY).is(itemType));
        query.fields().exclude(Dict.OBJECT_ID);
        return mongoTemplate.findOne(query, DictEntity.class);
    }
}
