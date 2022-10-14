package com.plan.service;

import java.util.*;

public interface TaskService {
    /**
     * 获取任务详情
     * @param mainTaskNo 任务id
     */
    Map<String, Object> queryTaskDetail(String mainTaskNo) throws Exception;

    List<Map> queryInfoByTaskNos(List<String> taskNos) throws Exception;

    /**
     * new fdev任务
     * @param taskNo
     * @return
     */
    Map<String, Object> getNewTaskById(String taskNo) throws  Exception;

    /**
     * 查询任务信息，可以按需组装字段
     * @param taskNo
     * @param fields
     * @return
     */
    Map<String, Object> queryTaskInfo(String taskNo, List<String> fields);
}
