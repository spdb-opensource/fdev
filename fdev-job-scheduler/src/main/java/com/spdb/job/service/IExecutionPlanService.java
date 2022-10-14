package com.spdb.job.service;

import java.util.Map;

/**
 * @author lizz
 */
public interface IExecutionPlanService {
    /**
     * 查询job运行信息
     *
     * @param requestMap
     * @return
     */
    Map<String, Object> selectJobInfo(Map<String, Object> requestMap);

    /**
     * 查询具体job运行信息
     *
     * @param requestMap
     * @return
     */
    Map<String, Object> selectJobInfoOne(Map<String, Object> requestMap);

    /**
     * 查询执行计划详情
     *
     * @param requestMap
     * @return
     */
    Map<String, Object> selectExecutorPlanDetail(Map<String, Object> requestMap);

    /**
     * 修改job运行信息
     *
     * @param requestMap
     * @return
     */
    int updateJobInfo(Map<String, Object> requestMap);

    /**
     * 添加job运行信息
     *
     * @param requestMap
     * @return
     */
    int insertJobInfo(Map<String, Object> requestMap);

    /**
     * 生成执行列表
     *
     * @return
     */
    Map<String, Object> generateFutureJobList();

    /**
     * 删除job运行信息
     *
     * @param requestMap
     * @return
     */
    int deleteExecutionPlan(Map<String, Object> requestMap);

    /**
     * 取消任务
     *
     * @param requestMap
     */
    void cancelJob(Map<String, Object> requestMap);

    /**
     * 延期任务
     *
     * @param requestMap
     */
    void postponeJob(Map<String, Object> requestMap);

    /**
     * 查询不满足时间的job个数
     *
     * @param requestMap
     * @return
     */
    int selectLessTimeJobNum(Map<String, Object> requestMap);

    /**
     * run 立即执行job
     *
     * @param requestMap
     * @return
     */
    void triggerJob(Map<String, Object> requestMap);

}
