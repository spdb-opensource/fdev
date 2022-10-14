package com.spdb.fdev.pipeline.service;

import com.spdb.fdev.pipeline.entity.Author;
import com.spdb.fdev.pipeline.entity.Entity;

import java.util.List;
import java.util.Map;

public interface IAppService {

    Map<String, Object> queryAppDetailById(String id) throws Exception;

    /**
     * 使用git projectid 或者 应用id查询
     * @param id
     * @return
     * @throws Exception
     */
    Map<String, Object> queryGitProjectDetail(String id) throws Exception;

    /**
     * 用gitlab项目id 及分支名查询对应分支
     * @return
     * @throws Exception
     */

    List queryEntityVariables(String[] entitys, String env) throws Exception;

    Map queryAppDetailByGitId(Integer gitlabProjectId) throws Exception;

    List<Map<String, Object>> queryMyApps(String userId) throws Exception;

    boolean branchExists(Integer gitlabId, String branchName) throws Exception;

    boolean checkTagExisted(Integer gitlabId, String branchName) throws Exception;

    List<Map> queryModelTemplateDetailInfo(String id) throws Exception;

    Author queryUserByNameEn(String gitUserNameEn) throws Exception;

    List<String> queryCurrentAndChildGroup(String id) throws Exception;

    Map queryEntityModelDetail(String entityId,String envName) throws Exception;

    Map queryEntityMapping(String[] entityIds, String env) throws Exception;

    Map queryUserInfoByUserId(String userId) throws Exception;
}
