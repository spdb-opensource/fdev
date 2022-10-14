package com.spdb.fdev.fdevenvconfig.spdb.dao;

import com.spdb.fdev.fdevenvconfig.spdb.entity.AppDeployMapping;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAppDeployMappingDao {

    AppDeployMapping queryById(Integer gitlabId);

    AppDeployMapping add(AppDeployMapping deployMapping);

    void update(AppDeployMapping deployMapping);

    List<AppDeployMapping> getAppDeployMapping(String modelNameEn, String fieldNameEn);

    Map<String,Object> queryByPage(int page, int per_page);

    List<AppDeployMapping> queryByGitlabIds(Set<Integer> pageGitLabIdList);

    Map<String, Object> queryByAppIdPage(Integer gitLabId, List<Integer> gitLabIdList, int page, int perPage);

}
