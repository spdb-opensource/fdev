package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.AppEnvMapping;

import java.util.List;
import java.util.Map;

public interface AppEnvMappingDao {

    AppEnvMapping save(AppEnvMapping appEnvMapping);

    List<AppEnvMapping> query(AppEnvMapping appEnvMapping) throws Exception;

    void update(AppEnvMapping appEnvMapping);

    void delete(AppEnvMapping envMapping);

    Map<String, Object> queryByPage(AppEnvMapping appEnvMapping1, int page, int per_page);

    Map<String, Object> queryByPage(List<String> appIdList, String appId, String envId, List<String> tags, String network, int page, int perPage);

    List<AppEnvMapping> queryByAppIds(List<String> appIdList);

    /**
     * 通过应用id查询该应用是否绑定过生产环境
     *
     * @param appId
     * @return
     */
    List<Map> queryProEnvByAppId(String appId);

    List<Map> querySccProEnvByAppId(String appId);

    List<AppEnvMapping> queryEnvByAppId(String appId);

}
