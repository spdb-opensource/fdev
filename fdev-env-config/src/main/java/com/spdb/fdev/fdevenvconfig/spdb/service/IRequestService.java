package com.spdb.fdev.fdevenvconfig.spdb.service;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRequestService {

    /**
     * 查询应用模块 通过 应用 id查询应用信息
     *
     * @param appId
     * @return
     * @throws Exception
     */
    JSONObject findByAppId(String appId) throws Exception;

    /**
     * 查询应用模块 所有应用的信息
     *
     * @return
     */
    JSONArray queryAllAppInfo() throws Exception;

    /**
     * 根据gitlaId查询应用信息
     *
     * @param gitlabId
     * @return
     * @throws Exception
     */
    Map getAppByGitId(Integer gitlabId) throws Exception;

    List<Map> findGroups() throws Exception;

    Map createGitlabProject(String path, String projectName) throws Exception;

    /**
     * 根据多个gitlab id查询应用信息
     *
     * @param ids
     * @return
     */
    List<Map<String, Object>> getAppByIdsOrGitlabIds(String type, Set<String> ids, Set<Integer> gitlabIds);

    /**
     * 根据角色名查询用户
     *
     * @param roleName
     * @return
     */
    List<Map<String, Object>> queryUser(String roleName) throws Exception;

    /**
     * 根据GitLab ID和镜像名称查找镜像最新可用版本
     *
     * @param requestMap
     * @return
     */
    String queryBaseImageVersion(Map<String, String> requestMap);

    /**
     * 根据用户id查询用户
     *
     * @param param
     * @return
     * @throws Exception
     */
    Map queryUserById(Map param) throws Exception;

    /**
     * 根据多个gitlabId或多个应用id查询应用信息
     *
     * @param ids
     * @return
     */
    List<Map<String, Object>> getApps(String type, Set<String> ids);

    Map updateApp(Map map) throws Exception;

    /**
     * 获取应用模块未组装其他数据的原始信息（速度较快）
     *
     * @return
     */
    List<Map<String, Object>> queryApps();

    /**
     * 根据ids或usernameEns集合查询多个用户一级数据
     *
     * @param param
     * @return
     * @throws Exception
     */
    Map<String, Map> getUsersByIdsOrUserNameEn(Map<String, List<String>> param) throws Exception;


    Object getProjectById(Map<String, String> param) throws Exception;

}
