package com.spdb.fdev.fdevtask.spdb.service;


import com.spdb.fdev.fdevtask.base.utils.Tuple;


public interface SonarQubeService {

    /**
     * 获取sonar扫描结果
     *
     * @param task_id 任务id
     * @return
     */
    Tuple.Tuple3 getSonarScanInfo(String task_id) throws Exception;


    String updateTaskSonarId(String featureId, String webNamEn, String SonarId, String scanTime);

    Tuple.Tuple3 getScanProcess(String task_id);
}
