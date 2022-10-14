package com.spdb.fdev.fdemand.spdb.service;

import java.util.List;
import java.util.Map;

public interface DashboardDemandService {


    List getDemandStatis(Map request) throws Exception;

    List queryImplUnit(Map map) throws Exception;

    List queryGroupDemand(List groups, String priority, boolean isParent) throws Exception;

    void sendDemandEmail() throws Exception;

    Map<String, Object> queryEndDemandDashboard(Map map);
    
    Map queryImpingDemandDashboard() throws Exception;

	Map queryIntGroupId();
}
