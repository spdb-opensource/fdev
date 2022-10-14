package com.spdb.fdev.pipeline.service;

import com.spdb.fdev.pipeline.entity.PipelineDigitalRate;

public interface IDigitalService {

    PipelineDigitalRate getDigitalRateByNameId(String nameId);

    void calculateDigital(String pipelineNameId) throws Exception;

}
