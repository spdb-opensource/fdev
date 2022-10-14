package com.spdb.fdev.fdemand.spdb.dao.impl;


import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dao.IpmpTaskDao;
import com.spdb.fdev.fdemand.spdb.entity.IpmpTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;


@Repository
public class IpmpTaskDaoImpl implements IpmpTaskDao {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public IpmpTask add(IpmpTask ipmpTask) {
        return mongoTemplate.save(ipmpTask);
    }

    @Override
    public IpmpTask update(IpmpTask ipmpTask) {
        Query query = new Query(Criteria.where(Dict.ID).is(ipmpTask.getId()));
        Update update = Update.update("implement_unit_list", ipmpTask.getIpmpImplementUnitList());
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);
        return mongoTemplate.findAndModify(query, update, options, IpmpTask.class);
    }

    @Override
    public IpmpTask findIpmpTaskById(String id) {
        Query q = Query.query(Criteria.where(Dict.ID).is(id));
        q.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(q, IpmpTask.class);
    }

    @Override
    public List<IpmpTask> queryIpmpTaskByGroupId(String groupId) {
        Query query = new Query();
        if (!CommonUtils.isNullOrEmpty(groupId)) {
            Criteria criteria = Criteria.where("groupId").is(groupId);
            query.addCriteria(criteria);
        }
        Criteria criteria = Criteria.where("implement_unit_list").ne(new ArrayList<>());
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, IpmpTask.class);
    }

    @Override
    public IpmpTask queryUnitByTakskId(String id) {
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.ID).is(id);
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, IpmpTask.class);
    }

    @Override
    public List<IpmpTask> queryAll() {
        return mongoTemplate.findAll(IpmpTask.class);
    }

    @Override
    public List<IpmpTask> queryUnitByUnitNo(String unintNo) {
        Query q = new Query();
        q.addCriteria(Criteria.where("implement_unit_list.ipmpImplementUnitNo").is(unintNo));
        return mongoTemplate.find(q, IpmpTask.class);
    }

    @Override
    public IpmpTask findIpmpTaskByTaskNoAndGroupId(String groupId,String taskNo) {
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.TASK_NO).is(taskNo).and("groupId").is(groupId);
        query.addCriteria(criteria);
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.findOne(query, IpmpTask.class);
    }

    @Override
    public List<IpmpTask> queryIpmpTaskByGroupIds(List<String> groupIds) {
        Query query = new Query();
        if (!CommonUtils.isNullOrEmpty(groupIds)) {
            Criteria criteria = Criteria.where("groupId").in(groupIds);
            query.addCriteria(criteria);
        }
        query.fields().exclude(Dict.OBJECTID);
        return mongoTemplate.find(query, IpmpTask.class);
    }

    @Override
    public IpmpTask queryGroupIdByTaskNoAndUnitNo(String taskNo, String ipmpImplementUnitNo) {
        Query q = new Query();
        q.addCriteria(Criteria.where("implement_unit_list.ipmpImplementUnitNo").is(ipmpImplementUnitNo).and(Dict.TASK_NO).is(taskNo));
        return mongoTemplate.findOne(q, IpmpTask.class);
    }

}
