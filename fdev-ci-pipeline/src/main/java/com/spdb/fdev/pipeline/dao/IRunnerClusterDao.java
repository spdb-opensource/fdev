package com.spdb.fdev.pipeline.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.pipeline.entity.RunnerCluster;

import java.util.List;
import java.util.Map;

public interface IRunnerClusterDao {

    List<RunnerCluster> queryAllRunnerCluster();

    /**
     * 插入RunnerCluster
     *
     * @param entityMap
     */
    String insertRunnerCluster(Map entityMap) throws Exception;

    List<RunnerCluster> queryRunnerClusterByParam(Map param) throws Exception;

    RunnerCluster queryRunnerClusterById(String id);

    RunnerCluster updateParamsById(Map<String, Object> param) throws JsonProcessingException;

    List<RunnerCluster> queryRunnerClusterByRunnerId(String runnerId);


}
