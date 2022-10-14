package com.spdb.fdev.fuser.spdb.dao.Impl;

import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.IIpmpDeptDao;
import com.spdb.fdev.fuser.spdb.entity.user.IpmpDept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IpmpDeptDaoImpl implements IIpmpDeptDao {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void addIpmpDeptBatch(List<IpmpDept> ipmpDeptList) {
        //先删除旧数据
        mongoTemplate.dropCollection(IpmpDept.class);
        //再批量插入所有查询过来的数据
        mongoTemplate.insert(ipmpDeptList, IpmpDept.class);
    }

    @Override
    public List<IpmpDept> queryIpmpDept(String deptId) {
        Query query = new Query();
        if(!CommonUtils.isNullOrEmpty(deptId)) {
            query.addCriteria(Criteria.where(Dict.DEPT_ID).is(deptId));
        }
        query.fields().exclude("_id");
        return mongoTemplate.find(query, IpmpDept.class);
    }
}
