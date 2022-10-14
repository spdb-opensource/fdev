package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.AppConfigMapping;

import java.util.List;

/**
 * create by Idea
 *
 * @Author aric
 * @Date 2020/1/7 17:11
 * @Version 1.0
 */
public interface IAppConfigMappingDao {

    void add(AppConfigMapping appConfigMapping);

    List<AppConfigMapping> query(AppConfigMapping appConfigMapping) throws Exception;

    List<AppConfigMapping> query(Integer appId, String branch, String node);

    AppConfigMapping update(AppConfigMapping appConfigMapping) throws Exception;

    List<AppConfigMapping> getAppConfigMapping(String modelName, String field);

    List<AppConfigMapping> getAppConfigMapping(String modelName, String field, List<String> branchList);

}
