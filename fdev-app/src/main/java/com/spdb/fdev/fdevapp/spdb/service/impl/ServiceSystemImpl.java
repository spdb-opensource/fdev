package com.spdb.fdev.fdevapp.spdb.service.impl;

import com.spdb.fdev.fdevapp.spdb.dao.IServiceSystemDao;
import com.spdb.fdev.fdevapp.spdb.entity.ServiceSystem;
import com.spdb.fdev.fdevapp.spdb.service.IServiceSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceSystemImpl implements IServiceSystemService {

    @Autowired
    private IServiceSystemDao serviceSystemDao;


    @Override
    public ServiceSystem add(ServiceSystem serviceSystem) {
        return serviceSystemDao.add(serviceSystem);
    }

    @Override
    public List<ServiceSystem> query(ServiceSystem serviceSystem) throws Exception {
        return serviceSystemDao.query(serviceSystem);
    }
}
