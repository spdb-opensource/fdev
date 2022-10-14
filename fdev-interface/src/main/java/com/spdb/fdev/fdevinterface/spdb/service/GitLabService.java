package com.spdb.fdev.fdevinterface.spdb.service;

import java.util.List;
import java.util.Map;

public interface GitLabService {

    /**
     * 获取GitLab上的Repository ID
     *
     * @param projectId    GitLab上Project ID
     * @param appServiceId 应用英文名
     * @param branch       分支
     * @param path         文件夹路径
     * @return
     */
    List<Map> getProjectsRepository(Integer projectId, String appServiceId, String branch, String path);

    /**
     * 获取 gitlab 文件内容
     *
     * @param gitlabId
     * @param branch
     * @param fileDir
     * @return
     */
    String getFileContent(Integer gitlabId, String branch, String fileDir);

}
