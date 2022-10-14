package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.fdevenvconfig.spdb.dao.IAppModelDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppModel;
import com.spdb.fdev.fdevenvconfig.spdb.service.IAppModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppModelServiceImpl implements IAppModelService {
    @Autowired
    private IAppModelDao appModelDao;

    @Override
    public List<AppModel> query(AppModel appModel) throws Exception {
        return appModelDao.query(appModel);
    }

    @Override
    public AppModel add(AppModel appModel) {
        return appModelDao.add(appModel);
    }

}
