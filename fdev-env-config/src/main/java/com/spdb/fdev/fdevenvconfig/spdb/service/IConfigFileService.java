package com.spdb.fdev.fdevenvconfig.spdb.service;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface IConfigFileService {

    /**
     * 用于保存配置模板
     *
     * @param requestParam
     */
    Map saveConfigTemplate(Map<String, Object> requestParam) throws Exception;

    /**
     * 配置模板回显
     *
     * @param requestParam
     * @return
     */
    String queryConfigTemplate(Map<String, String> requestParam) throws Exception;

    /**
     * 配置模板预览
     *
     * @param requestParam
     * @return
     */
    Map<String, Object> previewConfigFile(Map<String, Object> requestParam) throws Exception;

    /**
     * 配置文件依赖搜索
     *
     * @param model_name_en
     * @param field_name_en
     * @param range
     * @return
     * @throws Exception
     */
    List<Map> queryConfigDependency(String model_name_en, String field_name_en, List<String> range) throws Exception;


    /**
     * 依赖搜索结果导出
     *
     * @param maps
     * @return
     * @throws Exception
     */
    void exportDependencySearchResult(List<Map> maps, HttpServletResponse response) throws Exception;

    String saveConfigProperties(Map map) throws Exception;

    /**
     * 配置文件上传开发配置中心
     *
     * @param requestParam
     * @return
     * @throws Exception
     */
    String saveDevConfigProperties(Map<String, Object> requestParam) throws Exception;

    /**
     * 由持续集成调用，将 介质 war包的md5值 保存至 gitlab auto-config组下
     *
     * @param requestParam
     */
    void saveWarMd5ToGitlab(Map<String, Object> requestParam) throws Exception;

    void saveSitConfigProperties(Map<String, Object> requestParam) throws Exception;

    Map<String, Object> checkConfigFile(Map<String, Object> requestParam) throws Exception;

    Map<String, Object> analysisConfigFile(String configFileContent);

    List<Map> batchSaveConfigFile(List<Map> requestList) throws Exception;

    List<Map> batchPreviewConfigFile(List<Map> requestParam) throws Exception;
}
