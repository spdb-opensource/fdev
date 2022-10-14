package com.fdev.database.spdb.dao.Impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fdev.database.dict.Dict;
import com.fdev.database.spdb.dao.SysInfoDao;
import com.fdev.database.spdb.entity.SysInfo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;


@Repository
public class SysInfoDaoImpl implements SysInfoDao {

    @Resource
    private MongoTemplate mongoTemplate;


    @Override
    public List<SysInfo> query(SysInfo sysInfo) throws Exception {
        ObjectMapper mapper=new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String json = sysInfo ==null?"{}":mapper.writeValueAsString(sysInfo);
        Query query =new BasicQuery(json);
        return mongoTemplate.find(query, SysInfo.class);
    }

    @Override
    public SysInfo add(SysInfo sysInfo) {
        sysInfo.initId();
       return mongoTemplate.save(sysInfo);
    }

    @Override
    public void update(SysInfo sysInfo) {
        Query query =Query.query(Criteria.where(Dict.SYS_ID).is(sysInfo.getSys_id()));
        Update update = Update.update("system_name_cn", sysInfo.getSystem_name_cn());
        mongoTemplate.findAndModify(query, update, SysInfo.class);
    }
}
