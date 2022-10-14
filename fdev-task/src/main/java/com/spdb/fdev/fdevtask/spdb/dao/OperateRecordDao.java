package com.spdb.fdev.fdevtask.spdb.dao;

import com.spdb.fdev.fdevtask.spdb.entity.OperateRecords;

import java.util.List;

public interface OperateRecordDao {
    OperateRecords save(OperateRecords operateRecord);

    List<OperateRecords>  queryConfirmRecordByTaskId(String id);

    List<OperateRecords> getAllConfirmRecordByTaskIdContainHistory(String id);
}
