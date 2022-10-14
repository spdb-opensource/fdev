package com.spdb.fdev.fdevinterface.spdb.dao;

import com.spdb.fdev.fdevinterface.spdb.entity.AppJson;

import java.util.List;
import java.util.Map;

public interface AppJsonDao {

    void save(AppJson appJson);

    Map<String, Object> getAppJsonList(Map<String, Object> requestMap);

    List<AppJson> deleteAppJson(String projectName, String branch, Integer routesVersion);

    List<AppJson> getAppJsonLast(Map<String, String> nameAndBranchMap);

    AppJson getLastBranch(String appNameEn, String branch);

    void updateIsNew(String projectName, String branch);

    List<AppJson> queryLastAppJson(String branch, String project_name);
}
