package com.spdb.fdev.release.dao.impl;

import com.spdb.fdev.release.dao.IErrorsDao;
import com.spdb.fdev.release.entity.ErrorCollection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class ErrorsDaoImpl implements IErrorsDao {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public void save(ErrorCollection errorCollection) {
        mongoTemplate.save(errorCollection);
    }
}
