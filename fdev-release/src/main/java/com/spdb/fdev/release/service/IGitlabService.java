package com.spdb.fdev.release.service;

import com.spdb.fdev.common.exception.FdevException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IGitlabService {
    /**
     * 检查组路径是否存在
     *
     * @param groupPath 组全路径
     * @return
     * @throws FdevException
     */
    boolean checkGroupExist(String groupPath) throws FdevException;

    /**
     * 根据gitlab组路径查询组信息
     *
     * @param groupFullPath 组全路径 ebank/devops/testGroups/testassets
     * @return
     * @throws FdevException
     */
    Map queryGroupInfo(String groupFullPath) throws FdevException;

    /**
     * 根据组id和项目名查询gitlab项目是否存在
     *
     * @param namespaceId gitlab组id
     * @param name        项目名
     * @return
     * @throws Exception
     */
    boolean isProjectExist(Integer namespaceId, String name) throws Exception;

    /**
     * 根据组id和项目名查询gitlab上项目id
     *
     * @param name 项目名
     * @return
     * @throws Exception
     */

    String returnProjectId(Integer namespaceId, String name) throws Exception;

    String queryProjectIdByUrl(String project_url) throws Exception;

    /**
     * 根据项目id查询gitlab上分支名
     *
     * @param projectId 项目id
     * @return
     * @throws Exception
     */

    List queryBranches(String projectId) throws Exception;

    /**
     * 查询gitlab配置文件项目文件列表
     *
     * @param projectId 应用id
     * @param branch    分支名
     * @return
     * @throws Exception
     */
    Set<String> queryResourceFiles(String projectId, String branch) throws Exception;

    /**
     * 判断gitlab此文件是否已存在
     *
     * @param filepath  文件路径
     * @param projectId 项目id
     * @param branch    分支名
     * @return
     * @throws Exception
     */
    boolean isFileExist(String filepath, String projectId, String branch) throws Exception;


    /**
     * 创建一个 组
     *
     * @param group 组名字 /ebank/devops /ebank/devops/
     * @return
     */
    Map createGroup(String group) throws Exception;

    //清除缓存
    void cleanCacheBranches(String projectId) throws Exception;

    //清除缓存
    void cleanCacheFiles(String projectId, String branch) throws Exception;


    /**
     * 根据gitlab组id更新组信息
     *
     * @return
     * @throws FdevException
     */
    void updateGroup(String groupId, String newValue) throws Exception;

    void updateProjectName(String projectId, String newName) throws Exception;

    List queryPiplineWithJobs(String application_id, String ref, String pages) throws Exception;

    Object querySimpleJobs(Map param) throws Exception;

    Object querySinglePipeline(Map param) throws Exception;

    Object queryJobs(Map param) throws Exception;

    List getProjectTagsList(String id, String releaseNodeName, String token) throws Exception;

    boolean checkTagExisted(String gitlabId, String tag) throws Exception;

    /**
     * 根据gitlab应用id删除分支
     *
     * @return
     * @throws FdevException
     */
    void deleteBranch(Integer gitlabId, String branchName) throws Exception;

    /**
     * 根据gitlab应用id创建分支
     * @param gitlabId gitlab应用id
     * @param  branchName 拉取的分支名
     * @param  ref 目标分支
     * @return
     * @throws FdevException
     */
    void createBranch(Integer gitlabId, String branchName, String ref) throws Exception;

    void setProtectedBranches(Integer id, String branchName);

    Map<String, Object> compareBranches(String configGitlabId, String old_tag, String new_tag) throws Exception;

    String findWebUrlByGitlabId(String configGitlabId);

    Object createMergeRequest(Integer id, String sbranch, String tbranch, String title, String desc) throws Exception;

    void addMember(String id, String userId, String role) throws Exception;

    Object createTag(String id, String name, String ref, String token) throws Exception;

    void deleteTag(Integer gitlabId, String tagName) throws Exception;

    boolean checkBranchExists(Integer id, String branchName);

    /**
     * 创建项目pipline
     *
     * @param id        项目id
     * @param ref       所属分支 exl：master
     * @param variables 所需参数[{ "key" : "CI_DEPLOY", "value" : "deploy" }]
     * @param token     AccessTokens验证用户
     * @return pipline相关信息
     * @throws Exception
     */
    Object createPipeline(String id, String ref, List<Map<String, String>> variables, String token) throws Exception;

}