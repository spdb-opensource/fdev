package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppConfigMapping;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Environment;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelField;
import com.spdb.fdev.fdevenvconfig.spdb.service.IAppConfigMappingService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IConfigFileService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IEnvironmentService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IModelEnvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * create by Idea
 *
 * @Author aric
 * @Date 2020/1/7 15:29
 * @Version 1.0
 */
@Service
public class AppConfigMappingServiceImpl extends BaseService implements IAppConfigMappingService {

    @Autowired
    private IModelEnvService modelEnvService;
    @Autowired
    private IEnvironmentService environmentService;
    @Autowired
    private IModelDao modelDao;
    @Autowired
    private IConfigFileService configFileService;

    /**
     * 扫描 gitlab 对应分支的配置文件 保存到数据库
     *
     * @param requestParam
     * @return
     */
    @Override
    public void saveAppConfigByWebHook(Map<String, Object> requestParam) throws Exception {
        // 触发 webhook 对应的分支
        String branch = ((String) requestParam.get(Dict.BRANCH)).trim();
        // 触发 webhook 对应的应用 projectId
        Integer projectId = ((Integer) requestParam.get(Dict.PROJECT_ID));
        // 获取文件内容
        String fileContent = this.gitlabApiService.getFileContent(this.token, projectId.toString(), branch, this.applicationFile);
        boolean fileFlag = CommonUtils.checkFileContentIsNone(fileContent);
        List<ModelField> modelField;
        if (fileFlag) {
            modelField = new ArrayList<>();
        } else {
            // 解析文件内容
            modelField = parseFileContent(fileContent);
        }
        saveOrUpdate(projectId, branch, modelField, "0");
    }

    @Override
    public void saveOrUpdate(Integer projectId, String branch, List<ModelField> modelField, String node) throws Exception {
        List<AppConfigMapping> queryResult = this.appConfigMappingDao.query(projectId, branch, node);
        AppConfigMapping appConfigMapping = new AppConfigMapping();
        // 数据库中不存在，则新增
        if (queryResult.isEmpty()) {
            appConfigMapping.setAppId(projectId);
            appConfigMapping.setBranch(branch);
            appConfigMapping.setModelField(modelField);
            appConfigMapping.setNode(node);
            this.appConfigMappingDao.add(appConfigMapping);
            return;
        }
        // 存在 则 更新
        appConfigMapping = queryResult.get(0);
        appConfigMapping.setModelField(modelField);
        appConfigMapping.setNode(node);
        appConfigMapping.setUtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_ISO_FORMAT));
        this.appConfigMappingDao.update(appConfigMapping);
    }

    @Override
    public List<AppConfigMapping> getAppIdAndBranch(String modelName, String field) {
        return appConfigMappingDao.getAppConfigMapping(modelName, field);
    }

    /**
     * 解析文件内容
     *
     * @param fileContent
     */
    private List<ModelField> parseFileContent(String fileContent) {
        Map<String, Object> analysisConfigFileMap = configFileService.analysisConfigFile(fileContent);
        Map<String, Object> primaryMap = (Map<String, Object>) analysisConfigFileMap.get(Dict.MODEL_FIELD);
        List<ModelField> result = new ArrayList<>();
        primaryMap.forEach((key, value) -> result.add(new ModelField(key, (Set<Object>) value)));
        return result;
    }

    /**
     * 通过 应用 gitlab id ，分支名，环境 查询 实体环境映射值
     *
     * @param requestParam
     * @return
     */
    @Override
    public Map queryConfigVariables(Map<String, Object> requestParam) throws Exception {
        Integer gitlabId = (Integer) requestParam.get(Dict.GITLABID);
        String branch = String.valueOf(requestParam.get(Dict.BRANCH));
        // 若传来的是pro开头的tag，则查master分支，因为存的时候只存了master分支
        if (branch.startsWith(Dict.PRO)) {
            branch = Dict.MASTER;
        }
        String envName = String.valueOf(requestParam.get(Dict.ENV_NAME));
        String node = String.valueOf(requestParam.get(Dict.NODE));
        Map<Object, Object> result = new HashMap<>();
        Environment environment = this.environmentService.queryByNameEn(envName);
        if (environment == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"环境不存在！", envName});
        }
        List<AppConfigMapping> queryResult = this.appConfigMappingDao.query(gitlabId, branch, node);
        if (queryResult.isEmpty())
            return result;
        // 实体集合
        Set<String> modelSet = new HashSet<>();
        // 实体.属性字段集合
        Set<String> modelFieldSet = new HashSet<>();
        queryResult.forEach(configMap -> configMap.getModelField().forEach(modelField -> {
            String modelName = modelField.getModelName();
            modelSet.add(modelName);
            modelField.getField().forEach(field -> modelFieldSet.add(modelName + "." + field));
        }));
        List<Model> modelList = this.modelDao.queryByNameEn(modelSet);
        if (modelList == null || modelList.isEmpty())
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"应用绑定实体为空"});
        Map<String, Object> preliminaryResult = this.modelEnvService.returnModelKeyMap(environment.getId(), modelList);
        modelFieldSet.forEach(modelField -> {
            if (preliminaryResult.containsKey(modelField))
                result.put(modelField, preliminaryResult.get(modelField));
        });
        return result;
    }
}
