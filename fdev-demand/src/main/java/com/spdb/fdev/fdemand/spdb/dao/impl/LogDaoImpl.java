package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.spdb.dao.ILogDao;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnitEntityLog;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnitOperateLog;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@RefreshScope
@Service
public class LogDaoImpl implements ILogDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Value("${ipmp.increment.updateImplUnit}")
    private String updateImplUnit;

    @Override
    public void saveIpmpUnitEntityLog(IpmpUnitEntityLog ipmpUnitEntityLog) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        ipmpUnitEntityLog.set_id(objectId);
        ipmpUnitEntityLog.setId(id);
        mongoTemplate.save(ipmpUnitEntityLog);
    }

    @Override
    public void saveIpmpUnitOperateLog(IpmpUnitOperateLog ipmpUnitOperateLog) throws Exception {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        ipmpUnitOperateLog.set_id(objectId);
        ipmpUnitOperateLog.setId(id);
        mongoTemplate.save(ipmpUnitOperateLog);
    }

    @Override
    public List<IpmpUnitOperateLog> queryIpmpUnitOperateLog(String implUnitNum) throws Exception {
        Query query = new Query();
        Criteria criteria = Criteria.where(Dict.IMPLUNITNUM).is(implUnitNum);
        criteria.and(Dict.INTERFACENAME).is(updateImplUnit);
        query.with(new Sort(Sort.Direction.DESC, Dict.UPDATETIME));
        query.addCriteria(criteria);
        return mongoTemplate.find(query,IpmpUnitOperateLog.class);

    }
}
