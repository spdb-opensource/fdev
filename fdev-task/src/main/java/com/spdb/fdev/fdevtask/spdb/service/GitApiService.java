package com.spdb.fdev.fdevtask.spdb.service;

import com.spdb.fdev.fdevtask.base.utils.Tuple;

import java.util.List;
import java.util.Map;

/**
 * 调用git相关api
 */
public interface GitApiService {
    /**
     * 获取当前分支落后master数量
     * @param gitlabId  app id
     * @param branch 分支
     * @return
     */
    Tuple.Tuple3 getAfterCommits(String gitlabId, String branch) throws  Exception;

    /**
     * 获取当前分支落后master数量
     * @param branch
     * @return
     */
    String getConflictInfo(String branch);

    /**
     * 判断当前分支知否合并过一次sit
     * @param branchName 当前分支
     * @param girLabId gitlabId
     * @return 结果
     */
    public boolean judgeMergeSit(String branchName, String girLabId, String flag) throws Exception;

    String getLatestCommitSha(Integer gitProjectId, String branchName);

    /**
     * 根据项目id
     * @param gitlabProjectId
     * @param featureBranch
     * @return
     */
    Map<String, Object> queryBranch(String gitlabProjectId, String featureBranch);

    /**
     * 根据tag名搜索项目下的tag
     * @param gitlabId
     * @param tagName
     * @return
     */
    List<Map> searchTags(String gitlabId, String tagName);

    /**
     * 出tag包
     * @param gitlabId 项目gitlab id
     * @param createFrom 基于哪个分支
     * @param tagName 包名称
     */
    void createTag(String gitlabId, String createFrom, String tagName);

    /**
     * 创建分支
     * @param gitlabId
     * @param branchName
     * @param createFrom
     */
    void createBranch(String gitlabId, String branchName, String createFrom);

    /**
     * 给用户添加项目git权限
     * @param gitlabProjectId
     * @param accessLevel
     * @param gitlabUserId
     */
    void addMember(String gitlabProjectId, int accessLevel, String gitlabUserId);

    /**
     * 查询合并请求信息
     * @param gitlabProjectId
     * @param mergeId
     * @return
     */
    Map<String, Object> queryMergeInfo(String gitlabProjectId, String mergeId);

    /**
     * 创建合并请求
     * @param gitLabId
     * @param sourceBranch
     * @param targetBranch
     * @param desc
     * @param title
     * @param token
     * @return
     */
    String createMergeRequest(String gitLabId, String sourceBranch, String targetBranch, String desc, String title, String token) throws Exception;

    /**
     * 查询git项目所有分支，若项目类型为镜像，则只返回dev开通的分支
     * @return
     */
    List<String> queryProjectBranchList(String gitLabId, String applicationType);

    Map getMergeRequestInfo(Map requestParam) throws Exception;

    Map queryMerged(Map param, String gitlabId, String appType);

    Map<String, Object> getMergeInfo(String projectId, String iid);
}
