package com.spdb.fdev.release.service;

import com.spdb.fdev.release.entity.ReleaseTask;

import java.util.List;
import java.util.Map;

public interface IReleaseBatchService {

    /**
     * 查询任务批次信息
     *
     * @param taskId
     * @return
     * @throws Exception
     */
    Map<String, Object> queryBatchInfoByTaskId(String taskId, String appId, String releaseNodeName) throws Exception;

    /**
     * 新增批次信息
     *
     * @param releaseNodeName
     * @param batchId
     * @param appId
     * @param modifyReason
     * @param bindAppIds
     * @throws Exception
     */
    void addBatch(String releaseNodeName, String batchId, String appId, String modifyReason, List<String> bindAppIds) throws Exception;

    /**
     * 根据应用id查询任务批次信息
     *
     * @param appIds
     * @param releaseNodeName
     * @return
     * @throws Exception
     */
    List<Map<String, String>> queryBatchInfoByAppId(List<String> appIds, String releaseNodeName) throws Exception;

    /**
     * 根据投产窗口和批次查应用集合（变更用）
     *
     * @param releaseNodeName
     * @param batchId
     * @return
     * @throws Exception
     */
    List<Map<String, String>> queryAppIdsByNodeAndBatch(String releaseNodeName, String batchId) throws Exception;

    List<ReleaseTask> batchReleaseTask(String date) throws Exception;
}
