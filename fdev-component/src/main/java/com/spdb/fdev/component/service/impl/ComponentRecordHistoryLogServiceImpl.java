package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.component.dao.IComponentRecordHistoryLogDao;
import com.spdb.fdev.component.entity.ComponentRecordHistoryLog;
import com.spdb.fdev.component.service.IComponentRecordServiceHistoryLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RefreshScope
public class ComponentRecordHistoryLogServiceImpl implements IComponentRecordServiceHistoryLog {

    @Autowired
    IComponentRecordHistoryLogDao componentRecordHistoryLogDao;

    @Override
    public List query(ComponentRecordHistoryLog componentRecordHistoryLog) throws Exception {
        return componentRecordHistoryLogDao.query(componentRecordHistoryLog);
    }

    @Override
    public List queryByComponentId(ComponentRecordHistoryLog componentRecordHistoryLog) {
        return componentRecordHistoryLogDao.queryByComponentId(componentRecordHistoryLog);
    }

    @Override
    public ComponentRecordHistoryLog save(ComponentRecordHistoryLog componentRecordHistoryLog) throws Exception {
        return componentRecordHistoryLogDao.save(componentRecordHistoryLog);
    }
}
