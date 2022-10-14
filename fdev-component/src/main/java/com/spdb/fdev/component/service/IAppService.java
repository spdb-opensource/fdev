package com.spdb.fdev.component.service;

import java.util.List;
import java.util.Map;

public interface IAppService {
    void createProjectPipeline(Map map) throws Exception;

    Map createTags(String id, String name, String ref, String token) throws Exception;

    /**
     * 通过应用id查询应用信息
     *
     * @param id
     * @return
     * @throws Exception
     */
    Map<String, Object> queryAPPbyid(String id) throws Exception;

    Map<String, Object> getAppByGitId(String gitlabid) throws Exception;

    List<Map<String, Object>> getAppList() throws Exception;
}
