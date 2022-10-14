package com.spdb.fdev.fdevenvconfig.spdb.service;

import com.spdb.fdev.fdevenvconfig.spdb.entity.Environment;

import java.util.List;
import java.util.Map;

public interface IEnvironmentService {
    List<Environment> query(Environment envEntity);

    Environment save(Environment envEntity) throws Exception;

    void delete(Map map) throws Exception;

    Environment update(Environment envEntity) throws Exception;

    Environment queryById(Environment envEntity) throws Exception;

    Environment queryById(String id);

    List<Environment> queryByLabels(Environment environment) throws Exception;

    List<Environment> queryAutoEnv(Environment environment) throws Exception;

    List<String> getEnvNameByLabels(String label);

    List<Environment> getEnvByLabels(List<String> labels);

    Environment queryByNameEn(String envNameEn);

    /**
     * 将网段标签区分开去查询环境（注意default）
     * 比如labels传["sit","biz","dmz"]，则返回环境标签包括["sit","biz"]和["sit","dmz"]的环境列表
     * 比如labels传["sit","biz","dmz","default"]，则返回环境标签包括["sit","default","biz"]和["sit","default","dmz"]的环境列表
     *
     * @param requestParamMap
     * @return
     */
    List<Environment> queryByLabelsFuzzy(Map<String, Object> requestParamMap);

    /**
     * 将网段标签和环境阶段标签区分开去查询环境（注意default）
     * 比如labels传["sit","uat",biz","dmz"]，则返回环境标签包括["sit","biz"]、["sit","dmz"]、["uat","biz"]以及["uat","dmz"]的环境列表
     * 比如labels传["sit","uat","biz","dmz","default"]，则也返回环境标签包括["sit","biz"]、["sit","dmz"]、["uat","biz"]以及["uat","dmz"]的环境列表
     *
     * @param labels
     * @return
     */
    List<Environment> queryByLabelsFuzzy(List<String> labels);

    Environment queryByNameCN(String env_name);


    /**
     * 根据环境标签获取环境列表，若标签为空，则返回所有环境
     *
     * @param labels
     * @return
     */
    List<Environment> getEnvs(List<String> labels);

    List<Environment> queryEnvByAppId(Map<String, String> map);

    List<Environment> querySccEnvByAppId(Map<String, String> requestParamMap) throws Exception;
}
