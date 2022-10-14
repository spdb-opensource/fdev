package com.spdb.fdev.fdevapp.spdb.dao;

import com.spdb.fdev.fdevapp.spdb.entity.DomainEntity;

import java.util.List;

public interface IDomainDao {

    public DomainEntity save(DomainEntity entity) throws Exception;

    public List<DomainEntity> query(DomainEntity entity) throws Exception;
}
