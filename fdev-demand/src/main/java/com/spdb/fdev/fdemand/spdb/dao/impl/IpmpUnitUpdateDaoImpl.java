package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.dao.IIpmpUnitUpdateDao;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnitUpdate;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IpmpUnitUpdateDaoImpl implements IIpmpUnitUpdateDao {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public IpmpUnitUpdate queryNewInfo() {
        Query query = new Query();
        query.with(Sort.by(Sort.Order.desc(Dict.CREATETIME)));
        List<IpmpUnitUpdate> ipmpunitUpdates = mongoTemplate.find(query, IpmpUnitUpdate.class);
        if(CommonUtils.isNullOrEmpty(ipmpunitUpdates)){
            return null;
        }
        return ipmpunitUpdates.get(0);
    }

    @Override
    public IpmpUnitUpdate save(IpmpUnitUpdate ipmpunitUpdate) {
        ObjectId objectId = new ObjectId();
        String id = objectId.toString();
        ipmpunitUpdate.set_id(objectId);
        ipmpunitUpdate.setId(id);
        ipmpunitUpdate.setCreateTime(TimeUtil.formatTodayHs());
        return mongoTemplate.save(ipmpunitUpdate);
    }

}
