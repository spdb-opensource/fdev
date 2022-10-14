package com.spdb.fdev.fdevinterface.spdb.service;

import com.google.gson.JsonObject;
import com.spdb.fdev.fdevinterface.spdb.entity.RoutesApi;
import com.spdb.fdev.fdevinterface.spdb.entity.RoutesRelation;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Map;

public interface RoutesService {

    /**
     * 保存路由模型
     *
     * @param routesApiList
     */
    void insertRoutesApi(List<RoutesApi> routesApiList);

    /**
     * 路由调用关系保存
     *
     * @param routesRelationList
     */
    void insertRoutesRelation(List<RoutesRelation> routesRelationList);

    /**
     * 查询路由列表
     *
     * @param params
     * @return
     */
    Map getRoutesApi(Map params);

    /**
     * 查询路由调用关系
     *
     * @param params
     * @return
     */
    Map getRoutesRelation(Map params);

    /**
     * 获取路由详情
     *
     * @param params
     * @return
     */
    List<RoutesApi> getRoutesDetailVer(Map params);

    /**
     * 获取路由模型
     *
     * @param appName
     * @param branch
     * @return
     */
    List<RoutesRelation> getRoutes(String appName, String branch);

    /**
     * 删除
     *
     * @param appName
     */
    void removeRoutes(String appName, String branch);

    void romoveRoutesRelation(String appName, String branch);

    List<RoutesApi> getRoutesApiList(String projectName, String branchName);

    void updateIsNew(String projectName, String branchName);

    @Async
    void deleteRoutesApi(String appServiceId, String branchName, int ver);

    void analysisRoutesApi(String appNameEn, String branch, String newMD5Str, JsonObject jsonObject, String type);

    Map<String, Integer> getMD5AndVer(String appNameEn, Integer gitLabId, String branch, String newMD5Str, JsonObject jsonObject);

    /**
     * 持续集成时，扫描路由
     *
     * @param md5AndVer
     * @param appNameEn
     * @param branch
     * @param newMD5Str
     * @param jsonObject
     * @param type
     */
    void analysisRoutesApi(Map<String, Integer> md5AndVer, String appNameEn, String branch, String newMD5Str, JsonObject jsonObject, String type);

}
