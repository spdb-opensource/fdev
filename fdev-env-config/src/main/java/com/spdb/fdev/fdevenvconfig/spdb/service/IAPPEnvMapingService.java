package com.spdb.fdev.fdevenvconfig.spdb.service;

import com.spdb.fdev.fdevenvconfig.spdb.entity.AppEnvMapping;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAPPEnvMapingService {

    AppEnvMapping save(AppEnvMapping appEnvMapping);

    List<AppEnvMapping> query(AppEnvMapping appEnvMapping) throws Exception;

    void update(AppEnvMapping appEnvMapping);

    void delete(AppEnvMapping envMapping);

    void addAppEnvMapping(Map requestParam) throws Exception;

    Map<String, Object> queryByPage(AppEnvMapping appEnvMapping1, int page, int per_page);

    Map<String, Object> queryAppEnvMapByPage(Map<String, Object> requestMap, List<String> appIdList);

    List<AppEnvMapping> queryByAppIds(List<String> appIdList);

    /**
     * 通过应用id查询该应用是否绑定过生产环境
     *
     * @param requestMap
     * @return
     */
    List<Map> queryProEnvByAppId(Map requestMap);

    List<Map> queryProEnvByGitLabId(Map<String, Object> requestMap);
}
