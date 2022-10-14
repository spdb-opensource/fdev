package com.spdb.fdev.fdevapp.spdb.dao;

import java.util.List;
import java.util.Map;

public interface IGitlabAPIDao {
    /**
     * 获取项目1000条分支
     * @param id
     * @return
     * @throws Exception
     */
    List<Map> getProjectsBranch(String id) throws Exception;

    /**
     * 获取项目下所有用户
     * @param id
     * @return
     * @throws Exception
     */
    List<Map> getProjectsUser(String id,String gitlab_token) throws Exception;
}
