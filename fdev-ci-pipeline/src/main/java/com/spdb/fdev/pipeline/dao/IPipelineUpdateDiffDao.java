package com.spdb.fdev.pipeline.dao;

import com.spdb.fdev.pipeline.entity.PipelineUpdateDiff;

public interface IPipelineUpdateDiffDao {


    PipelineUpdateDiff queryDiffBySourceId(String sourcePipelineId);

    void saveDiff(PipelineUpdateDiff entity);

    PipelineUpdateDiff queryDiffById(String id);

}
