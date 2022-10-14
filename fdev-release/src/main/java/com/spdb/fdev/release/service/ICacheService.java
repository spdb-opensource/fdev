package com.spdb.fdev.release.service;

/**
 * Created by xxx on 下午7:44.
 */
public interface ICacheService {

    /**
     * 通过用户名查询用户gitlab token
     *
     * @param username 用户英文名
     * @return 用户token
     * @throws Exception
     */
    String queryUserGitlabToken(String username) throws Exception;
}
