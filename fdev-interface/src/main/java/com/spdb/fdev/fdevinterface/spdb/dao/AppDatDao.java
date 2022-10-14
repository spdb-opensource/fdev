package com.spdb.fdev.fdevinterface.spdb.dao;

import com.spdb.fdev.fdevinterface.spdb.entity.AppDat;

import java.util.List;
import java.util.Map;

/**
 * @author xxx
 * @date 2020/7/30 16:10
 */
public interface AppDatDao {

    void save(AppDat appDat);

    Map<String, Object> getAppDatList(Map<String, Object> requestMap);

    List<AppDat> deleteAppDat(String projectName, String branch, Integer routesVersion);

    List<AppDat> getAppDatTarName(Map<String, String> nameAndBranchMap);

    AppDat getLastBranch(String appNameEn, String branch);

    List<AppDat> queryLastAppDat(String branch, String project_name);
}
