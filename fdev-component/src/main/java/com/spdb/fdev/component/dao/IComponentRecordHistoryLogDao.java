package com.spdb.fdev.component.dao;

import com.spdb.fdev.component.entity.ComponentRecordHistoryLog;

import java.util.List;

public interface IComponentRecordHistoryLogDao {

    List query(ComponentRecordHistoryLog componentRecordHistoryLog) throws Exception;

    List queryByComponentId(ComponentRecordHistoryLog componentRecordHistoryLog);

    ComponentRecordHistoryLog save(ComponentRecordHistoryLog componentRecordHistoryLog) throws Exception;
}
