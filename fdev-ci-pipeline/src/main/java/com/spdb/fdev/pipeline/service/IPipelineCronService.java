package com.spdb.fdev.pipeline.service;

import com.spdb.fdev.pipeline.entity.PipelineCronBatch;

import java.util.List;

public interface IPipelineCronService {

    List<PipelineCronBatch> getAllPipelineCrons();

    PipelineCronBatch getLastBatch();

    void upsertCronBatch(PipelineCronBatch pipelineCronBatch);
}
