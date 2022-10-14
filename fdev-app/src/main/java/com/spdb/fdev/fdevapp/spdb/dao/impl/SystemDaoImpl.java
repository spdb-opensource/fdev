package com.spdb.fdev.fdevapp.spdb.dao.impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.dao.ISystemDao;
import com.spdb.fdev.fdevapp.spdb.entity.AppEntity;
import com.spdb.fdev.fdevapp.spdb.entity.AppSystem;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemDaoImpl implements ISystemDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<AppSystem> findSystemByParam(AppSystem param) {
        Criteria c = new Criteria();
        if (!CommonUtils.isNullOrEmpty(param.getId()))
            c.and(Dict.ID).is(param.getId());
        if (!CommonUtils.isNullOrEmpty(param.getName()))
            c.and(Dict.NAME).is(param.getName());
        Query query = new Query(c);
        query.fields().exclude(Dict.OBJECTID);
        return this.mongoTemplate.find(query, AppSystem.class, "system");
    }


    @Override
    public AppSystem save(AppSystem appSystemEntity) {
        if (appSystemEntity.get_id() == null){
            ObjectId objectId = new ObjectId();
            String id = objectId.toString();
            appSystemEntity.set_id(objectId);
            appSystemEntity.setId(id);
        }
        return this.mongoTemplate.save(appSystemEntity, "system");
    }


    @Override
    public List<AppSystem> querySystemByIds(List params) {
        Criteria criteria = new Criteria();
        criteria.and(Dict.ID).in(params);
        List<AppSystem> systemList = this.mongoTemplate.find(Query.query(criteria), AppSystem.class);
        return systemList;
    }
}
