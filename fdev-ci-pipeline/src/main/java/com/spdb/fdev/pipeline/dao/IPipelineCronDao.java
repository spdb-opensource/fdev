package com.spdb.fdev.pipeline.dao;

import com.spdb.fdev.pipeline.entity.PipelineCronBatch;

import java.util.List;

public interface IPipelineCronDao {


    void upsertCron(PipelineCronBatch pipelineCronBatch);

    List<PipelineCronBatch> queryAllCron();

    PipelineCronBatch getLastBatch();
}
