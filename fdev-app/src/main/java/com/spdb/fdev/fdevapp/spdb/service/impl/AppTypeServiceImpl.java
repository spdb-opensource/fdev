package com.spdb.fdev.fdevapp.spdb.service.impl;

import com.spdb.fdev.fdevapp.spdb.dao.IAppTypeDao;
import com.spdb.fdev.fdevapp.spdb.entity.AppType;
import com.spdb.fdev.fdevapp.spdb.service.IAppTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppTypeServiceImpl implements IAppTypeService {

    @Autowired
    private IAppTypeDao apptypeDao;


    @Override
    public AppType save(AppType appType) throws Exception {
        return apptypeDao.save(appType);
    }

    @Override
    public List<AppType> query(AppType appType) throws Exception {
        return apptypeDao.query(appType);
    }

    @Override
    public AppType update(AppType appType) throws Exception {
        return apptypeDao.update(appType);
    }

    @Override
    public AppType findById(String id) throws Exception {
        return apptypeDao.findById(id);
    }
}
