package com.spdb.fdev.spdb.service;

import java.util.Map;

public interface GitLabService {

    /**
     * 判断是否为maven项目
     *
     * @param projectId
     * @return
     */
    boolean mavenProFlag(Integer projectId);

    /**
     * 判断是否为纯前端项目
     *
     * @param projectId
     * @return
     */
    boolean jsProFlag(Integer projectId);

    /**
     * 获得.gitlab-ci.yml文件内容
     *
     * @return
     */
    Map<String, Object> getGitlabCiYml(Integer projectId);

    /**
     * 根据 gitlab ID 获取项目的信息
     * @param id 应用的gitlab对应id
     * @param token 应用负责人的token
     * @return
     */
    Map getProjectInfo(String id, String token);

    /**
     * 比较两个分支的不同
     *
     * @param projectId 应用id
     * @param from 主分支
     * @param to 开发分支
     * @return
     */
    Map<String, Object> compare(Integer projectId, String from, String to);
}
