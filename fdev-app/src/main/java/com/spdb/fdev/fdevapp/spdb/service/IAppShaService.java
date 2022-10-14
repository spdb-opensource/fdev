package com.spdb.fdev.fdevapp.spdb.service;

import com.spdb.fdev.fdevapp.spdb.entity.AppSha;

import java.util.Map;

/**
 * @author xxx
 * @date 2019/7/1 18:11
 */
public interface IAppShaService {
    /**
     * 配合持续集成 判断当前 分支 是否要继续允许
     * @param gitlab_id
     * @param sha
     * @param branch
     * @return
     */
    Map getSha(Integer gitlab_id, String sha, String branch) throws Exception;
}
