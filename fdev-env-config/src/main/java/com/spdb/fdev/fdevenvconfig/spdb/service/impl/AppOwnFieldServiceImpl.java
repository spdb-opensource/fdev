package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.fdevenvconfig.spdb.dao.AppOwnFieldServiceDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppOwnField;
import com.spdb.fdev.fdevenvconfig.spdb.service.AppOwnFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AppOwnFieldServiceImpl implements AppOwnFieldService {

    @Autowired
    private AppOwnFieldServiceDao appOwnFieldServiceDao;

    @Override
    public AppOwnField save(AppOwnField appOwnField) {
        return appOwnFieldServiceDao.save(appOwnField);
    }

    @Override
    public void update(AppOwnField appOwnField) {
        appOwnFieldServiceDao.update(appOwnField);
    }

    @Override
    public AppOwnField queryByAppIdAndEnvId(AppOwnField appOwnField) {
        return appOwnFieldServiceDao.queryByAppIdAndEnvId(appOwnField);
    }

    @Override
    public List<AppOwnField> query(AppOwnField appOwnField) throws Exception {
        return appOwnFieldServiceDao.query(appOwnField);
    }

    @Override
    public void deleteByAppId(String app_id,String env_id) {
        appOwnFieldServiceDao.delete(app_id,env_id);
    }
}
