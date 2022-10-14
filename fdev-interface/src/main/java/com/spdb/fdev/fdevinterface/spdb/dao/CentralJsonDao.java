package com.spdb.fdev.fdevinterface.spdb.dao;

import com.spdb.fdev.fdevinterface.spdb.entity.CentralJson;

import java.util.Map;

/**
 * @author xxx
 * @date 2020/7/28 19:34
 */
public interface CentralJsonDao {
    CentralJson centralJsonDao(String env);

    void updateCentralJson(String env, Map<String, Object> newCentralJson);
}
