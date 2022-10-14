package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ComponentRecordHistoryLog;

import java.util.List;

public interface IComponentRecordServiceHistoryLog {
    List query(ComponentRecordHistoryLog componentRecordHistoryLog) throws Exception;

    List queryByComponentId(ComponentRecordHistoryLog componentRecordHistoryLog);

    ComponentRecordHistoryLog save(ComponentRecordHistoryLog componentRecordHistoryLog) throws Exception;

}
