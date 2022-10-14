package com.spdb.fdev.freport.spdb.service;

import com.spdb.fdev.freport.spdb.vo.ProductVo;

public interface QualityManageService {

    ProductVo queryProIssueTrend(Integer cycle) throws Exception;
}
