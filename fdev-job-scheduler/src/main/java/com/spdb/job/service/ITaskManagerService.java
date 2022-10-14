package com.spdb.job.service;

import java.util.Map;

/**
 * @author lizz
 */
public interface ITaskManagerService {
    /**
     * 注册job信息
     *
     * @param requestMap
     */
    void addJob(Map<String, Object> requestMap);

    /**
     * 查询job实例
     *
     * @param requestMap
     * @return
     */
    Map<String, Object> selectTrigger(Map<String, Object> requestMap);

    /**
     * 删除job信息
     *
     * @param requestMap
     * @return
     */
    void deleteJob(Map<String, Object> requestMap);

    /**
     * 更新job信息
     *
     * @param requestMap
     */
    void updateJob(Map<String, Object> requestMap);

    /**
     * 查询调度信息
     *
     * @param requestMap
     * @return
     */
    Map<String, Object> selectJobDetail(Map<String, Object> requestMap);

    /**
     * 查询job元数据
     *
     * @param requestMap
     * @return
     */
    Map<String, Object> selectDetail(Map<String, Object> requestMap);

    /**
     * 暂停调度任务
     *
     * @param requestMap
     */
    void pauseJob(Map<String, Object> requestMap);

    /**
     * 继续调度任务
     *
     * @param requestMap
     */
    void resumeJob(Map<String, Object> requestMap);

    /**
     * 暂停调度、恢复调度辅助类
     *
     * @param requestMap
     * @return
     */
    void pauseAndResumeHelp(Map<String, Object> requestMap);

    /**
     * 一次性任务
     *
     * @param requestMap
     * @return
     */
    void fillTriggerJob(Map<String, Object> requestMap);

    /**
     * 获取交易列表
     *
     * @return
     */
    Map<String, Object> getServiceList();
}
