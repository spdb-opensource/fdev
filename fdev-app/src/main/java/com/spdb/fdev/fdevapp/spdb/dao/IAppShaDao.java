package com.spdb.fdev.fdevapp.spdb.dao;

import com.spdb.fdev.fdevapp.spdb.entity.AppSha;

/**
 * @author xxx
 * @date 2019/7/1 18:13
 */
public interface IAppShaDao {
    /**
     *
     * @param key
     * @param gitlab_id
     * @return
     */
    AppSha getAppSha(String key, Integer gitlab_id);

    /**
     * 插入一条AppSha
     * @param appSha
     */
    AppSha addAppSha(AppSha appSha);

    /**
     *
     * @param appSha
     */
    AppSha updateAppSha(AppSha appSha) throws Exception;
}
