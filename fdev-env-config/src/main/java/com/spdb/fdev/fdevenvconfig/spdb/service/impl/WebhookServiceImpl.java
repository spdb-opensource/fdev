package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.utils.GetModelFieldUtil;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelField;
import com.spdb.fdev.fdevenvconfig.spdb.service.IAppConfigMappingService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IGitlabApiService;
import com.spdb.fdev.fdevenvconfig.spdb.service.WebhookService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

@Service
@RefreshScope
public class WebhookServiceImpl implements WebhookService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${gitlab.token}")
    private String token;
    @Value("${node.scan.file}")
    private String nodeScanFile;
    @Autowired
    private IAppConfigMappingService appConfigMappingService;
    @Autowired
    private IGitlabApiService gitlabApiService;

    @Override
    public void saveNodeAppConfigMap(Map<String, Object> parse) {
        Integer projectId = (Integer) parse.get(Dict.PROJECT_ID);
        // 触发push的分支
        String pushBranch = (String) parse.get(Dict.REF);
        pushBranch = pushBranch.split("/")[2];
        // 只记录SIT、master、release*分支
        if (!Dict.SIT_UP.equals(pushBranch) && !Dict.MASTER.equals(pushBranch) && !pushBranch.startsWith(Dict.RELEASE)) {
            return;
        }
        // 获取需要扫描的文件列表，若文件不存在或者文件里的文件列表为空，更新存量数据
        String fileContent = null;
        try {
            fileContent = gitlabApiService.getFileContent(token, projectId.toString(), pushBranch, nodeScanFile);
        } catch (Exception e) {
            saveNodeConfig(projectId, pushBranch, new ArrayList<>());
            return;
        }
        if (StringUtils.isEmpty(fileContent)) {
            saveNodeConfig(projectId, pushBranch, new ArrayList<>());
            return;
        }
        // 若文件列表不为空，则遍历文件列表，依次获取文件内容，得到其引用的实体信息
        if (fileContent.contains("\r\n")) {
            fileContent = fileContent.replace("\r\n", "\n");
        }
        String[] filePathList = fileContent.split("\n");
        StringBuilder totalModelFieldStr = new StringBuilder();
        for (String filePath : filePathList) {
            if (StringUtils.isEmpty(filePath)) {
                continue;
            }
            String enCodeFilePath;
            try {
                enCodeFilePath = URLEncoder.encode(filePath, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("URLEncoder error:{}, path:{}", e.getMessage(), filePath);
                continue;
            }
            String configFileContent;
            try {
                configFileContent = gitlabApiService.getFileContent(token, projectId.toString(), pushBranch, enCodeFilePath);
            } catch (Exception e) {
                logger.error("获取文件{}内容出错：{}", filePath, e.getMessage());
                continue;
            }
            if (StringUtils.isEmpty(configFileContent)) {
                continue;
            }
            // 获取文件列表里所有占位符中的内容（"实体.实体属性"）
            String modelFieldStr = GetModelFieldUtil.getModelField(configFileContent);
            totalModelFieldStr.append(modelFieldStr);
        }
        // 组装数据
        String[] totalModelFieldStrSplit = totalModelFieldStr.toString().split("\n");
        Multimap<String, Object> listMultimap = ArrayListMultimap.create();
        for (String modelFieldString : totalModelFieldStrSplit) {
            String[] modelFieldStringSplit = modelFieldString.split("\\.");
            if (modelFieldStringSplit.length >= 2) {
                listMultimap.put(modelFieldStringSplit[0], modelFieldStringSplit[1]);
            }
        }
        Map<String, Collection<Object>> modelFieldMap = listMultimap.asMap();
        List<ModelField> modelFieldList = new ArrayList<>();
        for (Map.Entry<String, Collection<Object>> entry : modelFieldMap.entrySet()) {
            ModelField modelField = new ModelField();
            modelField.setModelName(entry.getKey());
            Set<Object> fieldSet = new HashSet<>();
            Collection<Object> value = entry.getValue();
            for (Object valueObj : value) {
                fieldSet.add(valueObj);
            }
            modelField.setField(fieldSet);
            modelFieldList.add(modelField);
        }
        saveNodeConfig(projectId, pushBranch, modelFieldList);
    }

    private void saveNodeConfig(Integer projectId, String pushBranch, List<ModelField> modelFieldList) {
        try {
            appConfigMappingService.saveOrUpdate(projectId, pushBranch, modelFieldList, "1");
            logger.info("保存Node应用引用的实体信息成功,project id:{},branch:{}", projectId, pushBranch);
        } catch (Exception e) {
            logger.error("保存Node应用引用的实体信息出错:{},project id:{},branch:{}", e.getMessage(), projectId, pushBranch);
        }
    }

    @Override
    public void saveAppConfigMap(Map<String, Object> parse) {
        Integer projectId = (Integer) parse.get(Dict.PROJECT_ID);
        // 触发push的分支
        String pushBranch = (String) parse.get(Dict.REF);
        pushBranch = pushBranch.split("/")[2];
        // 只记录SIT、master、release*分支
        if (!Dict.SIT_UP.equals(pushBranch) && !Dict.MASTER.equals(pushBranch) && !pushBranch.startsWith(Dict.RELEASE)) {
            return;
        }
        List<Map<String, Object>> commits = (List<Map<String, Object>>) parse.get(Dict.COMMITS);
        // 标记gitlab-ci/fdev-application.properties文件是否被改动了
        boolean flag = false;
        for (Map<String, Object> commit : commits) {
            List<String> modified = (List<String>) commit.get(Dict.MODIFIED);
            for (String fileName : modified) {
                if (Constants.FDEV_APPLICATION.equals(fileName)) {
                    // 只要改动过，就跳出循环
                    flag = true;
                    break;
                }
            }
            if (flag)
                break;
        }
        String before = (String) parse.get(Dict.BEFORE);
        // 判断是否为新拉的SIT分支
        if (Dict.ZEROS.equals(before) && Dict.SIT_UP.equals(pushBranch)) {
            flag = true;
        }
        if (!flag) {
            logger.info("外部配置文件未更新,project id:{},branch:{}", projectId, pushBranch);
            return;
        }
        saveAppConfig(projectId, pushBranch);
    }

    private void saveAppConfig(Integer projectId, String pushBranch) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Dict.PROJECT_ID, projectId);
        paramMap.put(Dict.BRANCH, pushBranch);
        try {
            this.appConfigMappingService.saveAppConfigByWebHook(paramMap);
            logger.info("保存外部配置文件引用的实体信息成功,project id:{},branch:{}", projectId, pushBranch);
        } catch (Exception e) {
            logger.error("保存外部配置文件引用的实体信息出错:{},project id:{},branch:{}", e.getMessage(), projectId, pushBranch);
        }
    }

}
