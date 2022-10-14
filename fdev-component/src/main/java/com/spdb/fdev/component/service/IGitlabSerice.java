package com.spdb.fdev.component.service;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.entity.ComponentInfo;
import com.spdb.fdev.component.entity.MpassComponent;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IGitlabSerice {

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

    List<Map<String, Object>> getProjectTagsList(String id, String token) throws Exception;


    Object createTags(String id, String name, String ref, String token) throws Exception;

    Object createPipeline(String id, String ref, List<Map<String, String>> variables, String token)
            throws Exception;

    Object createBranch(String id, String name, String ref, String token) throws Exception;


    Object deleteBranch(String id, String branch, String token) throws Exception;

    void checkBranch(String token, String gitlabId, String branch) throws Exception;

    void checkTag(String token, String gitlabId, String tag) throws Exception;

    boolean checkBranchOrTag(String token, String gitlabId, String type, String branchOrTag);

    Object createMergeRequest(String id, String sbranch, String tbranch, String title, String desc, String token) throws Exception;

    Object addProjectHook(String id, String token, String mergeRequest, String pipeline, String type) throws Exception;

    boolean addDevops(String projectId, String token, String templatePath, String projectName, String http_url_to_repo) throws Exception;

    Map getProjectInfo(String id, String token) throws Exception;

    Object createProject(String path, String name, String token) throws Exception;

    void updateComponentAfterPip(String version, String pipid) throws Exception;

    void createFile(String token, String gitlab_project_id, String branch, String applicationFile, String content, String commitMessage);

    String getFileContent(String token, String gitlab_project_id, String branch, String applicationFile) throws Exception;

    Object deleteTag(String gitlabId, String tagName, String token) throws Exception;

    Object queryMergeList(String id, String branch, String state) throws Exception;

    Object queryPipelineList(String id, String branch, String status) throws Exception;

    Object setProtectedBranches(String id, String name) throws Exception;

    String getGitLabFileContent(Integer projectId, String fileDir, String branch);

    void updateReleaseChangeLog(MpassComponent mpassComponent, String feature_branch, String tag) throws Exception;

    void updateReleaseChangeLog(ComponentInfo component, String feature_branch, String tag) throws Exception;

    List queryTag (String projectId, String version) throws Exception;

    String getProjectIdById(String id,String type) throws Exception;
}
