package com.spdb.fdev.fdevenvconfig.spdb.service;

import com.spdb.fdev.fdevenvconfig.spdb.entity.AppDeployMapping;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IAppDeployMappingService {

    /**
     * 根据gitlabID定为到应用在部署时大key和小key的映射关系
     *
     * @param gitlabId
     * @return AppDeployMapping
     * @throws Exception
     */
    AppDeployMapping queryByGitlabId(Integer gitlabId);

    /**
     * 查询应用deploy时需要的变量映射关系
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    Map queryVariablesMapping(Map requestMap) throws Exception;

    /**
     * 查询应用deploy时需要的变量映射关系,带小key的中文名
     *
     * @param requestMap
     * @return
     * @throws Exception
     */
    List<Map> queryVariablesMappingwithCn(Map requestMap) throws Exception;

    Map doQueryVariablesMapping(Map requestMap, Map<String, Map> variablesNameCnMap) throws Exception;

    void update(AppDeployMapping deployMapping);

    Map<String, Object> queryByPage(int page, int per_page);

    Map<String, Object> queryByPage(Map<String, Object> requestMap, List<Integer> gitLabIdList);

    AppDeployMapping save(AppDeployMapping deployMapping) throws Exception;

    List<AppDeployMapping> queryByGitlabIds(Set<Integer> pageGitLabIdList);

    Map<String, Object> queryImagepwd(Map<String, String> requestMap) throws Exception;

    AppDeployMapping queryTagsByGitlabId(Integer gitlabId);

    Map queryAllVariablesMapping(Map<String, Object> paramMap) throws Exception;

    Map querySccVariablesMapping(Map<String, Object> paramMap) throws Exception;
    
    Map querySpecifiedVariablesMapping(Map<String, Object> paramMap) throws Exception;
}
