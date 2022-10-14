package com.spdb.fdev.freport.spdb.dao;

import com.spdb.fdev.freport.base.dict.EntityDict;
import com.spdb.fdev.freport.base.dict.MongoConstant;
import com.spdb.fdev.freport.base.dict.TaskEnum;
import com.spdb.fdev.freport.base.utils.CommonUtils;
import com.spdb.fdev.freport.spdb.entity.task.Task;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class TaskDao extends BaseDao {

    public List<Task> find(Task task) {
        Query query = new Query();
        if (!CommonUtils.isNullOrEmpty(task.getGroup()))
            query.addCriteria(Criteria.where(EntityDict.GROUP).is(task.getGroup()));
        query.addCriteria(Criteria.where(EntityDict.STAGE).nin(TaskEnum.TaskStage.ABORT.getName(), TaskEnum.TaskStage.DISCARD.getName()));
        return getMongoTempate(MongoConstant.TASK).find(query, Task.class);
    }

    public List<Task> findByStage(String group, String stage, String startTime, String endTime) {
        Query query = new Query();
        query.addCriteria(Criteria.where(EntityDict.GROUP).is(group));
        switch (stage){
            case "create":
                query.addCriteria(new Criteria().andOperator(Criteria.where(EntityDict.START_TIME).gte(startTime), Criteria.where(EntityDict.START_TIME).lte(endTime)));
                break;
            case "pro":
                query.addCriteria(new Criteria().andOperator(Criteria.where(EntityDict.FIRE_TIME).gte(startTime), Criteria.where(EntityDict.FIRE_TIME).lte(endTime)));
                break;
        }
        query.addCriteria(Criteria.where(EntityDict.STAGE).nin(TaskEnum.TaskStage.ABORT.getName(), TaskEnum.TaskStage.DISCARD.getName()));
        return getMongoTempate(MongoConstant.TASK).find(query, Task.class);
    }

    public List<Task> findByGroupIds(Set<String> groupIdSet) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        query.addCriteria(Criteria.where(EntityDict.GROUP).in(groupIdSet));
        query.addCriteria(Criteria.where(EntityDict.STAGE).nin(TaskEnum.TaskStage.ABORT.getName(), TaskEnum.TaskStage.DISCARD.getName()));
        query.addCriteria(criteria);
        return getMongoTempate(MongoConstant.TASK).find(query, Task.class);
    }

    public List<Task> findThroughputStatistics(Set<String> groupIdSet, String startTime, String endTime) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        query.addCriteria(Criteria.where(EntityDict.GROUP).in(groupIdSet));
        if (!CommonUtils.isNullOrEmpty(startTime) && !CommonUtils.isNullOrEmpty(endTime)) {
            criteria.orOperator(
                    new Criteria().andOperator(Criteria.where(EntityDict.START_TIME).gte(startTime),
                            Criteria.where(EntityDict.START_TIME).lte(endTime)),
                    new Criteria().andOperator(Criteria.where(EntityDict.FIRE_TIME).gte(startTime),
                            Criteria.where(EntityDict.FIRE_TIME).lte(endTime)));
        }
        query.addCriteria(Criteria.where(EntityDict.STAGE).nin(TaskEnum.TaskStage.ABORT.getName(), TaskEnum.TaskStage.DISCARD.getName()));
        query.addCriteria(criteria);
        return getMongoTempate(MongoConstant.TASK).find(query, Task.class);
    }

    public List<Task> findByUserIds(Set<String> userIds) {
        return getMongoTempate(MongoConstant.TASK).find(new Query().addCriteria(new Criteria().orOperator(
                Criteria.where(EntityDict.CREATOR).in(userIds),
                Criteria.where(EntityDict.MASTER).in(userIds),
                Criteria.where(EntityDict.SPDB_MASTER).in(userIds),
                Criteria.where(EntityDict.TESTER).in(userIds),
                Criteria.where(EntityDict.DEVELOPER).in(userIds)
        )), Task.class);
    }

    public List<Task> findByAppIds(Set<String> appIds) {
        return getMongoTempate(MongoConstant.TASK).find(new Query().addCriteria(Criteria.where(EntityDict.PROJECT_ID).in(appIds)), Task.class);
    }

}
