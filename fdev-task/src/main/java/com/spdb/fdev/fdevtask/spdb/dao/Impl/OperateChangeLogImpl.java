package com.spdb.fdev.fdevtask.spdb.dao.Impl;

import com.google.common.collect.Lists;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.spdb.dao.OperateChangeLog;
import com.spdb.fdev.fdevtask.spdb.entity.ChangeValueLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OperateChangeLogImpl implements OperateChangeLog {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveOperateChangeLog(ChangeValueLog changeValueLog) {
        mongoTemplate.save(changeValueLog);
    }

    @Override
    public List<ChangeValueLog> getOperateChangeLogByTaskId(String taskId) {
        List<ChangeValueLog> changeValueLogs = mongoTemplate.find(Query.query(
                Criteria.where(Dict.TASKID).is(taskId))
                .with(Sort.by(Sort.Order.desc(Dict.EXEC_TIME))), ChangeValueLog.class);
        return Optional.ofNullable(changeValueLogs).orElse(Lists.newArrayList());
    }
}
