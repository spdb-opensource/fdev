package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.spdb.fdev.fdevinterface.spdb.dao.CentralJsonDao;
import com.spdb.fdev.fdevinterface.spdb.entity.CentralJson;
import com.spdb.fdev.fdevinterface.spdb.service.CentralJsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author xxx
 * @date 2020/7/28 19:37
 */
@Service
public class CentralJsonServiceImpl implements CentralJsonService {

    @Autowired
    private CentralJsonDao centralJsonDao;

    @Override
    public CentralJson getCentralJson(String env) {
        return centralJsonDao.centralJsonDao(env);
    }

    @Override
    public void updateCentralJson(String env, Map<String, Object> newCentralJson) {
        centralJsonDao.updateCentralJson(env, newCentralJson);
    }
}
