package com.spdb.fdev.fdevtask.spdb.dao;


import com.spdb.fdev.fdevtask.spdb.entity.ChangeValueLog;

import java.util.List;

public interface OperateChangeLog {
    /**
     * 保存
     * @param changeValueLog 操作记录
     */
    void saveOperateChangeLog(ChangeValueLog changeValueLog);

    /**
     * 根据taskId查询操作记录
     * @param taskId 任务id
     * @return 操作记录,根据时间排序
     */
    List<ChangeValueLog> getOperateChangeLogByTaskId(String taskId);
}
