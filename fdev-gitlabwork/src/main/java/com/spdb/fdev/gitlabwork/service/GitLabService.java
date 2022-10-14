package com.spdb.fdev.gitlabwork.service;

import java.util.List;
import java.util.Map;

public interface GitLabService {

    /**
     * 通过username来获取gitlab用户
     *
     * @param username
     * @return
     */
    List<Map> getUserByUsername(String username);

    /**
     * 通过iid来查询merge请求的所有commits
     *
     * @param iid
     * @param projectId
     * @return
     */
    List<Map> getCommitsByIid(Integer iid, Integer projectId);

    /**
     * 通过sha值来查询当前commit的信息，通过次来查询merged是否有冲突
     *
     * @param projectId
     * @param sha
     * @return
     */
    Map getCommitBySha(Integer projectId, String sha);

    /**
     * 比较两个分支的不同
     *
     * @param projectId
     * @param from
     * @param to
     * @return
     */
    Map<String, Object> compare(Integer projectId, String from, String to);

    /**
     * 通过gitlab_id来查询用户的信息
     * @param creator
     * @return
     */
    Map getUserInfoById(Integer creator);
}
