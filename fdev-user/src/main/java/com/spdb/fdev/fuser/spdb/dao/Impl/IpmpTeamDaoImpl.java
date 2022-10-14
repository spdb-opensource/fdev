package com.spdb.fdev.fuser.spdb.dao.Impl;

import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.IIpmpTeamDao;
import com.spdb.fdev.fuser.spdb.entity.user.IpmpTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IpmpTeamDaoImpl implements IIpmpTeamDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void addIpmpTeamBatch(List<IpmpTeam> ipmpTeamList) {
        //先删除旧数据
        mongoTemplate.dropCollection(IpmpTeam.class);
        //再批量插入所有查询过来的数据
        mongoTemplate.insert(ipmpTeamList, IpmpTeam.class);
    }

    @Override
    public List<IpmpTeam> queryIpmpLeadTeam(String deptId, String teamId, String deptName) {
        Query query = new Query();
        if(!CommonUtils.isNullOrEmpty(deptId)) {
            query.addCriteria(Criteria.where(Dict.DEPT_ID).is(deptId));
        }
        if(!CommonUtils.isNullOrEmpty(deptName)) {
            query.addCriteria(Criteria.where(Dict.DEPT_NAME).is(deptName));
        }
        if(!CommonUtils.isNullOrEmpty(teamId)) {
            query.addCriteria(Criteria.where(Dict.TEAM_ID).is(teamId));
        }
        query.fields().exclude("_id");
        return mongoTemplate.find(query, IpmpTeam.class);
    }
}
