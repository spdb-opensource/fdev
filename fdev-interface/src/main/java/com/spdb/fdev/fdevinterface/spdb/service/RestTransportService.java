package com.spdb.fdev.fdevinterface.spdb.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RestTransportService {

    /**
     * 根据应用英文名，调应用模块的接口拿到cloneUrl及GitLab Project ID
     *
     * @param appServiceId
     * @return
     */
    Map getAppCloneUrlAndProId(String appServiceId);

    /**
     * 根据应用英文名判断登录用户是否为应用负责人
     *
     * @param appServiceId
     * @return
     */
    boolean isAppDevManager(String appServiceId);

    /**
     * 根据任务Id判断登录用户是否为应用负责人
     *
     * @param taskId
     * @return
     */
    boolean isTaskManager(String taskId);

    List<Map> getAllApp();

    /**
     * 根据应用id或者GitLab id查询应用
     *
     * @param type
     * @param ids
     * @param gitlabIds
     * @return
     */
    List<Map<String, Object>> getAppByIdsOrGitlabIds(String type, Set<String> ids, Set<Integer> gitlabIds);

    String queryLastTagByGitlabId(Integer gitLabId,String releaseNodeName);

}
