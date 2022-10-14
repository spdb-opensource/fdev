package com.spdb.fdev.fdevinterface.spdb.service;

import com.spdb.fdev.fdevinterface.spdb.entity.TotalDat;
import com.spdb.fdev.fdevinterface.spdb.entity.TotalJson;

import java.util.List;
import java.util.Map;

/**
 * @author xxx
 * @date 2020/7/30 16:11
 */
public interface TotalJsonService {

    void save(TotalJson totalJson);

    List<TotalJson> getTotalJsonList(Map<String, Object> requestMap);

    List<TotalJson> queryTotalJsonHistory(Map<String, Object> requestMap);

    TotalDat getNewTotalJson(String env);

    Integer haveNewApp(String env, String prodId);

    List<TotalJson> getTotalJsonListByEnv(Map<String, Object> envMap);
}
