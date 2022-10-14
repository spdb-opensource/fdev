package com.spdb.fdev.pipeline.service;

import java.util.List;
import java.util.Map;

public interface IRunnerService {

    Map getJob(String token) throws Exception;

    Map getPlugins(Map<String, Object> param) throws Exception;

    Map getPluginInput(Map<String, Object> param) throws Exception;

    void getPluginOutput(Map<String, Object> param) throws Exception;

    void jobWebHook(Map<String, Object> param) throws Exception;

    void artifactsWebhook(Map<String, Object> param) throws Exception;

    List<Map> getArtifacts(Map<String, Object> param) throws Exception;

    void jobClear(String runnerClusterId) throws Exception;

    void setJobLog(String output, String token, String range) throws Exception;
}
