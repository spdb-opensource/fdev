package com.spdb.fdev.release.service;

import java.util.List;
import java.util.Map;

public interface IComponenService {
	/**
     * 通过组件id查询组件英文名和中文名
     *
     * @param id
     * @return
     * @throws Exception
     */
    Map<String, Object> queryComponenbyid(String id) throws Exception;
    /**
     * 通过骨架id查询骨架英文名和中文名
     *
     * @param id
     * @return
     * @throws Exception
     */
    Map<String, Object> queryArchetypeDetail(String id) throws Exception;
    /**
     * 通过镜像id查询镜像英文名和中文名
     *
     * @param id
     * @return
     * @throws Exception
     */
    Map<String, Object> queryBaseImageDetail(String id) throws Exception;
    /**
     * 创建分支
     *
     * @param id    appid
     * @param name  新建分支名
     * @param ref   源分支名
     * @param token gitlab token
     * @throws Exception
     */
    void createReleaseBranch(String id, String name, String ref, String type) throws Exception;
    /**
     * 通过前端组件id查询组件英文名和中文名
     *
     * @param id
     * @return
     * @throws Exception
     */
    Map<String, Object> queryMpassComponentDetail(String id) throws Exception;
    /**
     * 通过前端骨架id查询组件英文名和中文名
     *
     * @param id
     * @return
     * @throws Exception
     */
    Map<String, Object> queryMpassArchetypeDetail(String id) throws Exception;
    /**
     * 根据应用ids查应用信息集合
     * @param appIds
     * @return
     * @throws Exception
     */
    List<Map<String, String>> getComponenByIdsOrGitlabIds(List<String> appIds, String releaseNodeName, String applicationType) throws Exception;
    
    /**
     * 通过投产窗口名称和应用id查询tag列表
     *
     * @param release_node_name,app_id
     * @return
     * @throws Exception
     */
    List<String> queryTagList(String release_node_name, String app_id) throws Exception;
}
