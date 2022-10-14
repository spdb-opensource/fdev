package com.spdb.fdev.pipeline.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.pipeline.entity.RunnerCluster;

import java.util.List;
import java.util.Map;

public interface IRunnerClusterService {

    List<RunnerCluster> getAllRunnerCluster();

    String addRunnerCluster(Map entityMap) throws Exception;

    List<RunnerCluster> getRunnerClusterByParam(Map param) throws Exception;

    RunnerCluster getRunnerClusterById(String runnerClusterId);

    RunnerCluster getRunnerClusterByRunner(String runnerId) throws Exception;

    RunnerCluster updateRunnerCluster(Map<String, Object> param) throws Exception;

}
