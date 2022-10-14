package com.spdb.fdev.fdevapp.spdb.service;

import com.spdb.fdev.fdevapp.spdb.entity.ServiceSystem;

import java.util.List;

public interface IServiceSystemService {

    public ServiceSystem add(ServiceSystem serviceSystem);

    public List<ServiceSystem> query(ServiceSystem serviceSystem)throws Exception;
}
