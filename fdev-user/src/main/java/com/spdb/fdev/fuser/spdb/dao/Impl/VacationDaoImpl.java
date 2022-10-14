package com.spdb.fdev.fuser.spdb.dao.Impl;

import com.spdb.fdev.fuser.spdb.dao.VacationDao;
import com.spdb.fdev.fuser.spdb.entity.user.Vacation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class VacationDaoImpl implements VacationDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Vacation queryVacationByTime(String time) {
        return this.mongoTemplate.findOne(new Query(Criteria.where("time").is(time)), Vacation.class);
    }
}
