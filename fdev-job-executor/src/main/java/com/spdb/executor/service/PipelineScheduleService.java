package com.spdb.executor.service;

public interface PipelineScheduleService {

    void pipelineExecuteCi();

    void calcPluginNumCi();

    void cronStopPipeline();
}
