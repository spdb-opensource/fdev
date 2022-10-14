package com.spdb.fdev.fuser.spdb.dao.Impl;

import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.IXTestUserDao;
import com.spdb.fdev.fuser.spdb.entity.user.TestManagerUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class XTestUserDaoImpl implements IXTestUserDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void addTestManagerInfo(List<TestManagerUser> testManagerUserList) {
        //先删除旧数据
        mongoTemplate.dropCollection(TestManagerUser.class);
        //再批量插入所有查询过来的数据
        mongoTemplate.insert(testManagerUserList, TestManagerUser.class);
    }

    @Override
    public List<TestManagerUser> getTestManagerInfo(String userEn) {
        Query query = new Query();
        if(!CommonUtils.isNullOrEmpty(userEn)) {
            query.addCriteria(Criteria.where(Dict.USER_NAME_EN).is(userEn));
        }
        query.fields().exclude("_id");
        return mongoTemplate.find(query, TestManagerUser.class);
    }
}
