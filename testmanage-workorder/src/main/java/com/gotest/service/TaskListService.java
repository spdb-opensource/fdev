package com.gotest.service;

import com.gotest.domain.TaskList;

import java.util.List;
import java.util.Map;

/**
 * 任务列表服务接口
 */
public interface TaskListService {

    Map queryTaskListCountByWorkNo(Map map) throws Exception;
    List<TaskList> queryTaskList(Map map) throws Exception;

    Map<String,Object> queryTasks(String workNo, String taskNo) throws Exception;

    String queryWorkNoByTaskNo(String taskNo) throws Exception;

    List<Map> queryTasksByWorkNo(String workNo) throws Exception;

    boolean isAllTasksInSitByWorkNo(String workNo) throws Exception;

    List<Map> queryFdevTaskByWorkNo(String workNo) throws Exception;
}
