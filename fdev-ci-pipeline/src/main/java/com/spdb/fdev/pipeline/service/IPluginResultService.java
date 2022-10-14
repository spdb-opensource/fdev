package com.spdb.fdev.pipeline.service;

public interface IPluginResultService {


    Object getPluginResultData(String pipelineExeId, Integer stageIndex, Integer jobIndex, Integer stepIndex) throws Exception;
}
