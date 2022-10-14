package com.spdb.fdev.fdevinterface.spdb.service;

import com.spdb.fdev.fdevinterface.spdb.entity.CentralJson;

import java.util.Map;

/**
 * @author xxx
 * @date 2020/7/28 19:34
 */
public interface CentralJsonService {
    CentralJson getCentralJson(String env);

    void updateCentralJson(String env, Map<String, Object> newCentralJson);
}
