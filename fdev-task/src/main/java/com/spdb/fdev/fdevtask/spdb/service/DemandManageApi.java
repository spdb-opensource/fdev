package com.spdb.fdev.fdevtask.spdb.service;

import java.util.List;
import java.util.Map;

public interface DemandManageApi {

    List<Map> queryIpmpListByid(List<String> implUnitIds);

    List<Map> queryDemandInfoByIds(List<String> demandIds) throws Exception;

    void queryDemandStatus(String demandId,String demandStatus) throws Exception;

    Map queryDemandManageInfoDetail(String demandId) throws Exception;
}
