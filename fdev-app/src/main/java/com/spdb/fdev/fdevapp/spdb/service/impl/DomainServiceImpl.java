package com.spdb.fdev.fdevapp.spdb.service.impl;

import com.spdb.fdev.fdevapp.spdb.dao.IDomainDao;
import com.spdb.fdev.fdevapp.spdb.entity.DomainEntity;
import com.spdb.fdev.fdevapp.spdb.service.IDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DomainServiceImpl implements IDomainService {


    @Autowired
    private IDomainDao domainDao;

    @Override
    public DomainEntity save(DomainEntity entity) throws Exception {
        return domainDao.save(entity);
    }

    @Override
    public List<DomainEntity> query(DomainEntity entity) throws Exception {
        return domainDao.query(entity);
    }
}
