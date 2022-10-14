package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.fdevtask.spdb.dao.TaskDiffModifyRecordDao;
import com.spdb.fdev.fdevtask.spdb.entity.TaskDiffModifyRecord;
import com.spdb.fdev.fdevtask.spdb.service.TaskDiffModifyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 任务难度修改记录
 */
@Service
public class TaskDiffModifyRecordServiceImpl implements TaskDiffModifyRecordService {

    @Autowired
    private TaskDiffModifyRecordDao modifyRecordDao;

    @Override
    public void save(TaskDiffModifyRecord taskDiffModifyRecord) {
        modifyRecordDao.save(taskDiffModifyRecord);
    }

    @Override
    public void update(TaskDiffModifyRecord taskDiffModifyRecord) {
        modifyRecordDao.update(taskDiffModifyRecord);
    }

    @Override
    public TaskDiffModifyRecord queryById(String id) {
        return modifyRecordDao.query(id);
    }
}
