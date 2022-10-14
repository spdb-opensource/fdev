package com.spdb.fdev.fdevapp.spdb.dao;

import com.spdb.fdev.fdevapp.spdb.entity.ServiceSystem;

import java.util.List;

public interface IServiceSystemDao {

    public ServiceSystem add(ServiceSystem serviceSystem);

    public List<ServiceSystem> query(ServiceSystem serviceSystem) throws Exception;
}
