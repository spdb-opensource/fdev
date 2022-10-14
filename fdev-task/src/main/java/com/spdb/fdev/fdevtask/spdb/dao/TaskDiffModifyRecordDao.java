package com.spdb.fdev.fdevtask.spdb.dao;

import com.spdb.fdev.fdevtask.spdb.entity.TaskDiffModifyRecord;

public interface TaskDiffModifyRecordDao {

    void save(TaskDiffModifyRecord taskDiffModifyRecords);

    void update(TaskDiffModifyRecord taskDiffModifyRecords);

    TaskDiffModifyRecord query(String id);
}
