package com.spdb.fdev.fdevenvconfig.spdb.service;

import java.util.List;
import java.util.Map;

/**
 * gitlab api 请求
 *
 * @author xxx
 * @date 2019/7/31 9:26
 */
public interface IGitlabApiService {

    /**
     * 向 对应的分支 创建文件
     *
     * @param token
     * @param gitlabId
     * @param branch
     * @param fileDir
     * @param content
     * @param commitMessage
     */
    void createFile(String token, String gitlabId, String branch, String fileDir, String content, String commitMessage);

    /**
     * 检验分支是否存在
     *
     * @param token
     * @param gitlabId
     * @param branch
     */
    void checkBranch(String token, String gitlabId, String branch) throws Exception;

    /**
     * 获取 gitlab 文件内容
     *
     * @param token
     * @param gitlabId
     * @param featureBranch
     * @param fileDir
     * @return
     */
    String getFileContent(String token, String gitlabId, String featureBranch, String fileDir) throws Exception;

    /**
     * 判断该分支与文件是否存在
     * @param token
     * @param gitlabId
     * @param featureBranch
     * @param fileDir
     * @return
     */
    boolean checkFileExists(String token, String gitlabId, String featureBranch, String fileDir);

    /**
     * 获取分支的commit提交信息
     *
     * @param token
     * @param gitlabProjectId
     * @param branch
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> getCommits(String token, String gitlabProjectId, String branch) throws Exception;

    /**
     * 获取应用信息
     *
     * @param token
     * @param gitlabProjectId
     * @return
     * @throws Exception
     */
    Map<String, Object> getProjectById(String token, String gitlabProjectId) throws Exception;

    /**
     * 获取提交请求的结果
     * @param token
     * @param gitlabProjectId
     * @param commitId
     * @return
     * @throws Exception
     */
    List<Map<String,Object> > getCommitDetail(String token, String gitlabProjectId, String commitId) throws Exception;

    /**
     * 拉取 tag
     * @param gitlabId
     * @param name
     * @param ref
     * @param token
     * @return
     * @throws Exception
     */
    Object createTag(String gitlabId, String name, String ref, String token) throws Exception;

    /**
     * 校验 tag 是否存在
     * @param gitlabId
     * @param tag
     * @param token
     * @return
     * @throws Exception
     */
    boolean checkTag(String gitlabId, String tag, String token) throws Exception;

    /**
     * 删除指定 tag
     * @param gitlabId
     * @param tag
     * @param token
     * @return
     * @throws Exception
     */
    Object deleteTag(String gitlabId, String tag, String token) throws Exception;


    Integer isProjectExist(Integer namespaceId, String name, String token) throws Exception;

}
