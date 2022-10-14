package com.spdb.fdev.fdevinterface.spdb.service;

import com.spdb.fdev.fdevinterface.spdb.entity.AppDat;

import java.util.List;
import java.util.Map;

/**
 * @author xxx
 * @date 2020/7/30 16:11
 */
public interface AppDatService {

    void save(AppDat appDat);

    Map<String, Object> getAppDatList(Map<String, Object> requestMap);

    Map<String, Object> getAppDatTarName(List<Map<String, String>> projectNameList);

    String getLastBranch(String appNameEn, String branch);
}
