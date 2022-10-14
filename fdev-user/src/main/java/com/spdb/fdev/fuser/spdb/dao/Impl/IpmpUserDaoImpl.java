package com.spdb.fdev.fuser.spdb.dao.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.IIpmpUserDao;
import com.spdb.fdev.fuser.spdb.entity.user.IpmpUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class IpmpUserDaoImpl implements IIpmpUserDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void addIpmpUserBatch(List<IpmpUser> ipmpUserList) {
        //先删除旧数据
        mongoTemplate.dropCollection(IpmpUser.class);
        //再批量插入所有查询过来的数据
        mongoTemplate.insert(ipmpUserList, IpmpUser.class);
    }

    @Override
    public List<IpmpUser> queryIpmpUser(String userId) {
        Query query = new Query();
        if(!CommonUtils.isNullOrEmpty(userId)) {
            query.addCriteria(Criteria.where(Dict.USER_NAME_EN).is(userId));
        }
        query.fields().exclude("_id");
        return mongoTemplate.find(query, IpmpUser.class);
    }
}
