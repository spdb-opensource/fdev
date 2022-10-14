package com.spdb.fdev.fdevinterface.spdb.service;

import com.spdb.fdev.fdevinterface.spdb.entity.AppJson;

import java.util.List;
import java.util.Map;

public interface AppJsonService {
    void save(AppJson appJson);

    Map<String, Object> getAppJsonList(Map<String, Object> requestMap);

    Map<String, Object> getAppJsonLast(List<Map<String, String>> projectNameList);

    String getLastBranch(String appNameEn, String branch);

    Map<String , Object> queryLastAppJson(Map<String, Object> requestMap);
}
