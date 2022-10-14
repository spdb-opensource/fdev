package com.gotest.service;

import java.util.Map;

public interface IDemandService {

    Map<String, Object> queryDemandById(String id) throws Exception;

    Map queryByFdevNoAndDemandId(String unit) throws Exception;

    Map queryNewUnitInfoById(String unitId) throws Exception;

    Map<String, Object> queryImplUnitById(String implUnitNum) throws Exception;

    Map<String, Object> queryFdevImplUnitDetail(String unitNo) throws Exception;

    String getUnitNo(String unitNo) throws Exception;
}
