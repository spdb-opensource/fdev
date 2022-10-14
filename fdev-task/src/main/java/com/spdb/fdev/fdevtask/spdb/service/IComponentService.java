package com.spdb.fdev.fdevtask.spdb.service;

import java.util.Map;

/**
 * 调用组件模块接口
 */
public interface IComponentService {
    /**
     * 根据组件id查询前端组件信息
     * @param componentId
     * @return
     */
    Map<String, Object> queryComponentWebDetail(String componentId) throws Exception;

    /**
     * 根据组件id查询后端组件信息
     * @param componentId
     * @return
     */
    Map<String, Object> queryComponentServerDetail(String componentId) throws Exception;

    /**
     * 根据骨架id查询前端骨架信息
     * @param archetypeId
     * @return
     */
    Map<String, Object> queryArchetypeWebDetail(String archetypeId) throws Exception;

    /**
     * 根据骨架id查询后端骨架信息
     * @param archetypeId
     * @return
     */
    Map<String, Object> queryArchetypeServerDetail(String archetypeId) throws Exception;

    /**
     * 根据镜像id查询镜像信息
     * @param imageId
     * @return
     */
    Map<String, Object> queryImageDetail(String imageId) throws Exception;

    /**
     * 保存预设版本号
     * @param componentId
     * @param targetVersion
     * @param type
     * @throws Exception
     */
    void savePredictVersion(String componentId, String targetVersion, String type) throws Exception;

    /**
     * 保存合并请求信息
     * @param componentId
     * @param archetypeId
     * @param gitlabId
     * @param mergeId
     * @param targetVersion
     * @throws Exception
     */
    void saveMergeRequest(String componentId, String archetypeId, String gitlabId, String mergeId, String targetVersion) throws Exception;

    /**
     * 出组件测试包
     * @param componentId 组件id
     * @param componentType 组件类型，前端-mpass，后端-back
     * @param predictVersion 预设版本号
     * @param branch 出包分支
     * @param packageType 包类型，beta/alpha
     * @param targetVersion 目标版本号
     */
    void testPackage(String componentId, String componentType, String predictVersion, String branch,
                     String packageType, String targetVersion) throws Exception;

    /**
     * 保存骨架的目标版本号
     * @param archetypeId
     * @param targetVersion
     */
    void saveTargetVersion(String archetypeId, String targetVersion) throws Exception;

    /**
     * 校验组件预设版本号
     * @param componentId
     * @param predictVersion
     * @param type
     */
    void judgePredictVersion(String componentId, String predictVersion, String type) throws Exception;

    /**
     * 校验骨架预设版本号
     * @param archetypeId
     * @param targetVersion
     */
    void judgeTargetVersion(String archetypeId, String targetVersion) throws Exception;
}
