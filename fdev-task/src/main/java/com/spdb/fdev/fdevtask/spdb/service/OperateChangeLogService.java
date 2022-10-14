package com.spdb.fdev.fdevtask.spdb.service;


import com.spdb.fdev.fdevtask.spdb.entity.ChangeValueLog;
import com.spdb.fdev.fdevtask.spdb.entity.Task;

import java.util.List;

/**
 * @author patrick
 * @date 2021/3/15 上午11:10
 * @Des 操作日志
 * 最簡單的事是堅持，最難的事還是堅持
 */
public interface OperateChangeLogService {
    /**
     * 异步记录操作日志，新增任务操作
     *
     * @param task 任务对象
     */
    void asyncLogCreate(Task task)  throws Exception;


    /**
     * 异步记录操作日志，上传文件操作
     *
     * @param docNames 新增文件列表
     * @param taskId   关联对象id
     */
    void asyncLogUpload(List<String> docNames, String taskId)  throws Exception;

    /**
     * 异步记录数据修改的日志
     *
     * @param newObj 新对象
     * @param oldObj 旧对象
     * @param taskId 关联对象
     */
    void asyncUpdateLog(Object newObj, Object oldObj, String taskId)  throws Exception;

    /**
     * 异步记录删除对象日志
     *
     * @param task 任务对象
     */
    void asyncDeleteLog(Task task)  throws Exception;

    /**
     * 获取任务动态
     *
     * @param taskId 任务id
     */
    List<ChangeValueLog> getOperateChangeLogByTaskId(String taskId);

    /**
     * 记录任务数据修改的日志
     *
     * @param newTask 新任务
     * @param oldTask 旧任务
     */
    void asyncUpdateTaskListLog(List<Task> newTask, List<Task> oldTask) throws Exception;
}
