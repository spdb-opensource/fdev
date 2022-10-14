package com.spdb.fdev.fdevtask.spdb.service;

public interface InteractTestApi {

     void newCreateTask(String taskId, String implUnitId, String taskName);

     void updateTaskUnitNo(String taskId,String taskName,String unitNo);

     void  deleteTask(String taskId);
}
