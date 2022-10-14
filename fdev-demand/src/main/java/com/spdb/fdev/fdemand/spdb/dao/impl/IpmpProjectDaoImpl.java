package com.spdb.fdev.fdemand.spdb.dao.impl;

import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dao.IIpmpProjectDao;
import com.spdb.fdev.fdemand.spdb.entity.IpmpProject;
import com.spdb.fdev.fdemand.spdb.entity.IpmpTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IpmpProjectDaoImpl implements IIpmpProjectDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void addIpmpProjectBatch(List<IpmpProject> ipmpProjectList) {
        //先删除旧数据
        mongoTemplate.dropCollection(IpmpProject.class);
        //再批量插入所有查询过来的数据
        mongoTemplate.insert(ipmpProjectList, IpmpProject.class);
    }

    @Override
    public List<IpmpProject> queryIpmpProject(String projectNo) {
        Query query = new Query();
        if(!CommonUtils.isNullOrEmpty(projectNo)) {
            query.addCriteria(Criteria.where(Dict.PROJECT_NO).is(projectNo));
        }
        query.fields().exclude("_id");
        return mongoTemplate.find(query, IpmpProject.class);
    }

    @Override
    public List<IpmpProject> queryIpmpProjectByProjectNo(String projectNo) {
        Query query = new Query(Criteria.where(Dict.PROJECT_NO).is(projectNo));
        query.fields().exclude("_id");
        return mongoTemplate.find(query, IpmpProject.class);
    }
}
