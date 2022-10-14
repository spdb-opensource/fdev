package com.spdb.fdev.freport.spdb.service;

import com.spdb.fdev.freport.spdb.vo.ProductVo;

public interface PublishManageService {

    ProductVo queryPublishCountTrend(Integer cycle) throws Exception;

}
