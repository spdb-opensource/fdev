package com.spdb.fdev.spdb.service;

import java.util.Map;

public interface IConfigFileService {
    /**
     * 用于保存配置模板
     *
     * @param requestParam
     */
    Map saveConfigTemplate(Map<String, Object> requestParam) throws Exception;

    Map<String, Object> analysisConfigFile(String configFileContent) throws Exception;

    /**
     * 配置文件依赖分析
     *
     * @param requestParam
     */
    Map<String, Object> queryConfigDependency(Map<String, Object> requestParam) throws Exception;

    /**
     * 部署依赖分析
     *
     * @param requestParam
     */
    Map<String, Object> queryDeployDependency(Map<String, Object> requestParam) throws Exception;
}
