package com.spdb.fdev.fdevtask.spdb.dao.Impl;

import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.spdb.dao.TaskDiffModifyRecordDao;
import com.spdb.fdev.fdevtask.spdb.entity.TaskDiffModifyRecord;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class TaskDiffModifyRecordDaoImpl implements TaskDiffModifyRecordDao {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public void save(TaskDiffModifyRecord taskDiffModifyRecord) {
        ObjectId objectId = new ObjectId();
        taskDiffModifyRecord.set_id(objectId);
        mongoTemplate.save(taskDiffModifyRecord);
    }

    @Override
    public void update(TaskDiffModifyRecord taskDiffModifyRecord) {
        Query query = Query.query(Criteria.where(Dict.ID).is(taskDiffModifyRecord.getId()));
        Update update = new Update();
        update.set(Dict.MODIFY_RECORD, taskDiffModifyRecord.getModifyRecord());
        mongoTemplate.findAndModify(query, update, TaskDiffModifyRecord.class);
    }

    @Override
    public TaskDiffModifyRecord query(String id) {
        Query query = Query.query(Criteria.where(Dict.ID).is(id));
        return mongoTemplate.findOne(query, TaskDiffModifyRecord.class);
    }
}
