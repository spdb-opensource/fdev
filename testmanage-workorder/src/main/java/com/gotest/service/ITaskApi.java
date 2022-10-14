package com.gotest.service;

import java.util.List;
import java.util.Map;

public interface ITaskApi {
    //根据任务id查询子任务
    Map queryByJobId(String id) throws Exception;

    //根据任务id查询任务信息
    Map queryTaskDetail(String id) throws Exception;

    String queryTaskDevelopNameCns(String taskId, String type) throws Exception;

    Map<String, String> queryTasksDeveloper(List<String> taskIds, String type);

    //根据任务id集合查询任务详情列表（key为任务id，value为任务详情）
    Map<String, Object> queryTaskDetailByIds(List<String> taskIds);

    /**
     * 根据新任务id查任务详情
     *
     * @param taskId
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> queryNewTaskDetail(List<String> taskId) throws Exception;

    /**
     * 查询重构fdev任务指派人
     *
     * @param task
     * @return
     * @throws Exception
     */
    List queryNewTaskAssignEns(Map task) throws Exception;

    /**
     * 变更新任务模块玉衡插件状态
     *
     * @param taskId
     * @param s
     */
    void changeTestComponentStatus(String taskId, String s);

    /**
     * 根据新任务id查询新任务
     * @param id
     * @return
     */
     Map<String,Object> getNewTaskById(String id);

    /**
     * 批量查询任务基本信息
     * @param taskIds
     * @param fields
     * @param responseFields
     * @return
     */
    List<Map> queryTaskBaseInfoByIds(List<String> taskIds, List<String> fields, List<String> responseFields);
}
