package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.HistoryDetails;

import java.util.Map;

public interface IHistoryDetailsDao {

    HistoryDetails add(HistoryDetails historyDetails);

    /**
     * 根据实体id、环境id及类型查询流水记录
     *
     * @param modelId
     * @param envId
     * @return
     */
    Map<String, Object> getHistoryList(String modelId, String envId, String type, Integer page, Integer perPage);

    /**
     * 根据id查询流水
     *
     * @param id
     * @return
     */
    HistoryDetails getHistory(String id);
}
