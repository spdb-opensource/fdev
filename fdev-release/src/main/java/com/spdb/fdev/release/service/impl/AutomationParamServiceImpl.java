package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.release.dao.IAutomationParamDao;
import com.spdb.fdev.release.entity.AutomationParam;
import com.spdb.fdev.release.service.IAutomationParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutomationParamServiceImpl implements IAutomationParamService {

    @Autowired
    private IAutomationParamDao automationParamDao;

    @Override
    public List<AutomationParam> query() {
        return automationParamDao.query();
    }

    @Override
    public void save(AutomationParam automationParam) {
        automationParamDao.save(automationParam);
    }

    @Override
    public void update(AutomationParam automationParam) {
        automationParamDao.update(automationParam);
    }

    @Override
    public void deleteById(String id) {
        automationParamDao.deleteById(id);
    }

    @Override
    public AutomationParam queryByKey(String key) {
        return automationParamDao.queryByKey(key);
    }

}
