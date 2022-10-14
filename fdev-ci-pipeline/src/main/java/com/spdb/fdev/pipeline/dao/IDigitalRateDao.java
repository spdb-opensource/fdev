package com.spdb.fdev.pipeline.dao;

import com.spdb.fdev.pipeline.entity.PipelineDigitalRate;

import java.util.List;

public interface IDigitalRateDao {

    PipelineDigitalRate queryByNameId(String nameId);

    void upsert(PipelineDigitalRate upsertEnity);

}
