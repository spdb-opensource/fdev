package com.spdb.fdev.freport.spdb.service;

import com.spdb.fdev.freport.spdb.dto.DmmDto;
import com.spdb.fdev.freport.spdb.vo.IntegrationRateVo;

import java.util.List;

public interface DmmService {

    List<IntegrationRateVo> queryIntegrationRate(DmmDto dto) throws Exception;
}
