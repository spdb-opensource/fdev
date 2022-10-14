package com.spdb.job.service;

import java.util.List;
import java.util.Map;

/**
 * @author lizz
 */
public interface IPausedStrategyManagerService {
    /**
     * 生成特殊时间段job不执行列表
     *
     * @param requestMap
     * @return
     */
    void insertPauseDateJob(Map<String, Object> requestMap);

    /**
     * 查询特殊时间段列表
     *
     * @param requestMap
     * @return
     */
    Map<String, Object> selectPausedTriggerBetweenDate(Map<String, Object> requestMap);

    /**
     * 查询特殊时间段列表及其相关任务
     *
     * @param requestMap
     * @return
     */
    Map<String, Object> selectPausedTriggerDateAndJob(Map<String, Object> requestMap);

    /**
     * 查询策略列表
     *
     * @param requestMap
     * @return
     */
    List<Map<String, Object>> selectPausedStrategy(Map<String, Object> requestMap);

    /**
     * 查询相关任务关联特殊时间段列表
     *
     * @param requestMap
     * @return
     */
    Map<String, Object> selectJobAndpausedDate(Map<String, Object> requestMap);

    /**
     * 删除时间区间暂停策略以及相关信息
     *
     * @param requestMap
     * @return
     */
    void deletePausedStrategy(Map<String, Object> requestMap);

    /**
     * 把单个任务从受时间区间暂停策略影响的Job移除
     *
     * @param requestMap
     * @return
     */
    int deletePausedStrategyJob(Map<String, Object> requestMap);

    /**
     * 把移除的任务恢复到受时间区间暂停策略影响的策略里面
     *
     * @param requestMap
     * @return
     */
    int recoverPausedStrategyJob(Map<String, Object> requestMap);
}
