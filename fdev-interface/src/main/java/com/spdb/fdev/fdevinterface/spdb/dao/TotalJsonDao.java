package com.spdb.fdev.fdevinterface.spdb.dao;

import com.spdb.fdev.fdevinterface.spdb.entity.TotalDat;
import com.spdb.fdev.fdevinterface.spdb.entity.TotalJson;

import java.util.List;

/**
 * @author xxx
 * @date 2020/7/30 16:10
 */
public interface TotalJsonDao {

    void save(TotalJson totalJson);

    void updateIsNew(String env);

    List<TotalJson> getTotalJsonList(String projectName);

    List<TotalJson> queryTotalJsonHistory(String env);

    TotalDat getNewTotalJson(String env);

    List<TotalJson> getNewTwentyTotalDat(String env);

    List<TotalJson> deleteTotalTar(String env, String datTime);

    TotalJson getTotalDatByProdId(String env, String prodId);

    void updateByProdId(TotalJson totalJson);

    List<TotalJson> getTotalJsonListByEnv(String env);
}
