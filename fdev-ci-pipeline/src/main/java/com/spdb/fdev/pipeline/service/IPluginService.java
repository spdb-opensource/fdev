package com.spdb.fdev.pipeline.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.common.User;
import com.spdb.fdev.pipeline.entity.Plugin;
import com.spdb.fdev.pipeline.entity.PluginEvaluate;
import com.spdb.fdev.pipeline.entity.Stage;
import com.spdb.fdev.pipeline.entity.YamlConfig;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public interface IPluginService {

    public List queryPlugin(List<String> key, Map param) throws Exception;

    void savePlugin(MultipartFile script, String pluginName, String pluginDesc, Map category, Boolean isPublic, List entityTemplateList, List params, Map artifacts, Map categoryMap) throws Exception;

    void editPlugin(String pluginCode, String pluginName, String pluginDesc, MultipartFile script, String releaseLog, String entityTemplateList, String params, Boolean isPublic,String version, String artifacts, String category, Boolean scriptUpdateFlag) throws Exception;

    void delPlugin(String pluginCode) throws Exception;

    public List<Map<String,String>> queryPluginHistory(String nameId) throws Exception;

    Plugin queryPluginDetail(String pluginCode) throws Exception;

    Map getPluginFullInfo(String pluginCode) throws Exception;

    Map<String,String> uploadFile(MultipartFile file, String filename) throws Exception;

    Map queryMarkDowm(String pluginCode) throws Exception;

    Map queryEntityTemplateByContent(String str) throws Exception;

    void statisticsPluginUseCount() throws Exception;

    void upsertPluginEvaluate(User user, PluginEvaluate pluginEvaluate);

    PluginEvaluate queryPluginEvaluate(User user, String nameId);

    YamlConfig getYamlConfigById(String configId);

    String addYamlConfig(YamlConfig addEntity);

    void copyYamlConfigInStages(List<Stage> stages);

    void closeYamlConfigInStages(List<Stage> stages);

    boolean checkUploadFileSuffix(String fileName)throws Exception;

    Object downloadArt(Map requestParam);

    void uploadArt(MultipartFile file, String path) throws Exception;

    void uploadMinio(MultipartFile file, String bucket, String object) throws Exception;

    Object downloadMinio(String bucket, String object, HttpServletResponse response) throws Exception;
}
