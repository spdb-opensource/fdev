package com.spdb.fdev.fuser.spdb.dao.Impl;

import com.spdb.fdev.fuser.spdb.dao.StuckPointDao;
import com.spdb.fdev.fuser.spdb.entity.user.StuckPoint;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @author 李尚林 on 2020/7/23
 * @c-lisl1
 **/

@Repository
public class StuckPointDaoImpl implements StuckPointDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public StuckPoint addStuckPoint(StuckPoint stuckPoint) {
        //StuckPoint stuck = this.mongoTemplate.insert(stuckPoint, "stuckPoint");
        return this.mongoTemplate.insert(stuckPoint,"stuckPoint");
    }
}
