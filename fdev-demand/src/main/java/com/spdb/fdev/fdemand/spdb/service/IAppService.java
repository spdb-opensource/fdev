package com.spdb.fdev.fdemand.spdb.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAppService {

    /**
     * 根据应用ids查询应用列表
     * @param appIds
     * @return
     */
    List<Map<String, Object>> getAppByIdsOrGitlabIds(Set<String> appIds );
    /**
     * 应用所属系统
     * @param systemId
     * @return
     */
    Map<String, String> querySystem(String systemId );
}
