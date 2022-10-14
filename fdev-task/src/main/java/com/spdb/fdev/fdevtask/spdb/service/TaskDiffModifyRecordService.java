package com.spdb.fdev.fdevtask.spdb.service;

import com.spdb.fdev.fdevtask.spdb.entity.TaskDiffModifyRecord;

public interface TaskDiffModifyRecordService {

    void save(TaskDiffModifyRecord taskDiffModifyRecord);

    void update(TaskDiffModifyRecord taskDiffModifyRecord);

    TaskDiffModifyRecord queryById(String id);
}
