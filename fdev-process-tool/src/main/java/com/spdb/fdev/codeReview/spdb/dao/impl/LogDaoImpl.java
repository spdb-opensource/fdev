package com.spdb.fdev.codeReview.spdb.dao.impl;

import com.mongodb.client.result.DeleteResult;
import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.spdb.dao.ILogDao;
import com.spdb.fdev.codeReview.spdb.entity.CodeLog;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author liux81
 * @DATE 2021/11/10
 */
@Component
public class LogDaoImpl implements ILogDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public CodeLog add(CodeLog codeLog) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        codeLog.set_id(objectId);
        codeLog.setId(id);
        return mongoTemplate.save(codeLog);
    }

    @Override
    public DeleteResult deleteLog(String orderId) {
        Query query = Query.query(Criteria.where(Dict.ORDER_ID).is(orderId));
        return mongoTemplate.remove(query, CodeLog.class);
    }

    @Override
    public List<CodeLog> queryLogs(String orderId) {
        Query query = Query.query(Criteria.where(Dict.ORDER_ID).is(orderId));
        query.with(new Sort(Sort.Direction.DESC, Dict.OPERATE_TIME));
        return mongoTemplate.find(query, CodeLog.class);
    }
}
