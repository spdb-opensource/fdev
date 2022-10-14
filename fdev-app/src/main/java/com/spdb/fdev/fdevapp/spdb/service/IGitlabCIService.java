package com.spdb.fdev.fdevapp.spdb.service;

import java.util.List;
import java.util.Map;

import com.spdb.fdev.fdevapp.spdb.entity.GitlabCI;

/**
 * 持续集成模板实体
 *
 * @author xxx
 */
public interface IGitlabCIService {
    //新增实体
    public GitlabCI save(GitlabCI gitlabci) throws Exception;

    //全量查询
    public List<GitlabCI> query(GitlabCI gitlabci) throws Exception;

    //更新实体
    public GitlabCI update(GitlabCI gitlabci) throws Exception;

    //根据id查询单个实体
    public GitlabCI findById(String id) throws Exception;

    //根据id查询单个实体
    public GitlabCI findByName(String name) throws Exception;

    /**
     * 给项目添加持续集成(.gitlab-ci.yml),功能入口
     *
     * @param projectId    gitlab project Id
     * @param token        gitlab 操作 token
     * @param templatePath 持续集成模板
     * @param projectName  项目名
     * @param encodingFile 应用编码类型
     * @return
     * @throws Exception
     */
    boolean addDevops(String projectId, String token, String templatePath, String projectName, String http_url_to_repo, String encodingFile) throws Exception;

    /**
     * 给项目添加持续集成(.gitlab-ci.yml),功能入口
     *
     * @param projectId    gitlab project Id
     * @param token        gitlab 操作 token
     * @param templatePath 持续集成模板
     * @param projectName  项目名
     * @param type         应用新增类型
     * @param encodingFile 应用编码类型
     * @return
     * @throws Exception
     */
    boolean addDevops(String projectId, String token, String templatePath, String projectName, String http_url_to_repo, Map archetype, String encodingFile) throws Exception;

}

