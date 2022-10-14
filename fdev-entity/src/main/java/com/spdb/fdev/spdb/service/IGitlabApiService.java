package com.spdb.fdev.spdb.service;


/**
 * gitlab api 请求
 *
 * @author xxx
 * @date 2019/7/31 9:26
 */
public interface IGitlabApiService {

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

}
