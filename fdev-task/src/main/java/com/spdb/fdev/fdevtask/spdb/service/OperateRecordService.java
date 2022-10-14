package com.spdb.fdev.fdevtask.spdb.service;

import com.spdb.fdev.fdevtask.spdb.entity.OperateRecords;

import java.util.List;

public interface OperateRecordService {
    OperateRecords save(OperateRecords operateRecord);

    OperateRecords queryConfirmRecordByTaskId(String id);

    List<OperateRecords> getAllConfirmRecordByTaskIdContainHistory(String id);
}
