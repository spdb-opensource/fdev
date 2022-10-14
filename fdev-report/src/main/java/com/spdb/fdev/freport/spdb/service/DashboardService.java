package com.spdb.fdev.freport.spdb.service;

import com.spdb.fdev.freport.spdb.entity.report.DashBoardUserConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DashboardService {

    DashBoardUserConfig queryUserConfig() throws Exception;

    void addUserConfig(List<Map<String, Object>> configs) throws Exception;

    Set<String> queryDefaultGroupIds() throws Exception;

}
