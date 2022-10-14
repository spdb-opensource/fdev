package com.spdb.fdev.fdevtask.spdb.dao.Impl;

import com.spdb.fdev.fdevtask.spdb.dao.OperateRecordDao;
import com.spdb.fdev.fdevtask.spdb.entity.OperateRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OperateRecordDaoImpl implements OperateRecordDao {
    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public OperateRecords save(OperateRecords operateRecord) {
        return mongoTemplate.save(operateRecord);
    }

    @Override
    public List<OperateRecords> queryConfirmRecordByTaskId(String id) {
        return mongoTemplate.find(Query.query(Criteria.where("id").is(id)).with(Sort.by(Sort.Order.desc("operateTime"))),OperateRecords.class);
    }

    @Override
    public List<OperateRecords> getAllConfirmRecordByTaskIdContainHistory(String id) {
        return mongoTemplate.find(Query.query(Criteria.where("id").is(id)),OperateRecords.class);
    }


}

