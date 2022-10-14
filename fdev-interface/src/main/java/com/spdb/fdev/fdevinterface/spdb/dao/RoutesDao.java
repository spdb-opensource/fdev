package com.spdb.fdev.fdevinterface.spdb.dao;

import com.spdb.fdev.fdevinterface.spdb.entity.RoutesApi;
import com.spdb.fdev.fdevinterface.spdb.entity.RoutesRelation;

import java.util.List;
import java.util.Map;

public interface RoutesDao {

    /**
     * 路由模型保存
     *
     * @param routesApiList
     */
    void saveRoutesApi(List<RoutesApi> routesApiList);

    /**
     * 路由调用关系保存
     *
     * @param routesRelationList
     */
    void saveRoutesRelation(List<RoutesRelation> routesRelationList);

    /**
     * 查询路由列表
     *
     * @param params
     * @return
     */
    Map queryRoutesApi(Map params);

    /**
     * 查询路由调用关系
     *
     * @param params
     * @return
     */
    Map queryRoutesRelation(Map params);

    /**
     * 获取路由模型
     *
     * @param appName
     * @param branch
     * @return
     */
    List<RoutesRelation> queryRoutes(String appName, String branch);

    /**
     * 删除
     *
     * @param appName
     */
    void deleteRoutes(String appName, String branch);

    void deleteRoutesRelation(String appName, String branch);

    /**
     * 获取最新路由
     *
     * @param appServiceId
     * @param branchName
     * @return
     */
    List<RoutesApi> queryRoutesApiList(String appServiceId, String branchName);

    void updateIsNew(String projectName, String branchName);

    void deleteRoutesApi(String projectName, String branchName, int ver);

    RoutesApi queryRoutesDetail(String name, String targetProject, String branch);

    RoutesApi queryRoutesById(String id);

    List<RoutesApi> queryRoutesDetailVer(String name, String projectName, String branch);

    void updateScanType(String appNameEn, String branch, Integer ver);
}
