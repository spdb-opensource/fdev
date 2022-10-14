package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.spdb.dao.IServiceConfigDao;
import com.spdb.fdev.spdb.entity.EntityField;
import com.spdb.fdev.spdb.entity.ServiceConfig;
import com.spdb.fdev.spdb.service.IConfigFileService;
import com.spdb.fdev.spdb.service.IGitlabApiService;
import com.spdb.fdev.spdb.service.IRestService;
import com.spdb.fdev.spdb.service.IWebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RefreshScope
public class WebhookServiceImpl implements IWebhookService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${gitlab.token}")
    private String token;
    @Value("${gitlab.application.file}")
    private String applicationFile;
    @Value("${gitlab.application.file.ci}")
	private String applicationFileCi;
    @Autowired
    private IGitlabApiService gitlabApiService;
    @Autowired
    private IServiceConfigDao serviceConfigDao;
    @Autowired
    private IConfigFileService configFileService;
    @Autowired
    IRestService restService;

    @Override
    public void saveAppConfigMap(Map<String, Object> parse) throws Exception {
        Integer projectId = (Integer) parse.get(Dict.PROJECT_ID);
        // 触发push的分支
        String pushBranch = (String) parse.get(Dict.REF);
        pushBranch = pushBranch.split("/")[2];
        // 只记录master*分支
        if ( !Dict.MASTER.equals(pushBranch)) {
            return;
        }
      	//根据应用git查询应用信息
        //应用gitId列表
      	Set<Integer> gitlabIds = new HashSet<Integer>();
      	gitlabIds.add(projectId);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(Dict.GITLABIDS, gitlabIds);
		// 只记录在FDEV存在的应用
        if ( CommonUtil.isNullOrEmpty(restService.queryApps(map))) {
            return;
        }
		List<Map<String, Object>> commits = (List<Map<String, Object>>) parse.get(Dict.COMMITS);
        // 标记gitlab-ci/fdev-application.properties文件是否被改动了
        boolean flag = false;
        String application = "";
        for (Map<String, Object> commit : commits) {
            List<String> modified = (List<String>) commit.get(Dict.MODIFIED);
            // ci/fdev-application
            if (modified.contains(Constants.CI_FDEV_APPLICATION)) {
            	application = Constants.CI_FDEV_APPLICATION;
            	 flag = true;
            	 break;
            	 //gitlab-ci/fdev-application
            }else if(modified.contains(Constants.GITLAB_CI_FDEV_APPLICATION)) {
            	application = Constants.GITLAB_CI_FDEV_APPLICATION;
            	flag = true;
            	break;
            }
        }
        if (!flag) {
            logger.info("外部配置文件未更新,project id:{},branch:{}", projectId, pushBranch);
            return;
        }
        saveAppConfig(projectId, pushBranch,application);
    }

    private void saveAppConfig(Integer projectId, String pushBranch, String application) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Dict.PROJECTID, projectId);
        paramMap.put(Dict.BRANCH, pushBranch);
        paramMap.put(Dict.APPLICATION, application);
        try {
            saveAppConfigByWebHook(paramMap);
            logger.info("保存外部配置文件引用的实体信息成功,project id:{},branch:{}", projectId, pushBranch);
        } catch (Exception e) {
            logger.error("保存外部配置文件引用的实体信息出错:{},project id:{},branch:{}", e.getMessage(), projectId, pushBranch);
        }
    }

    /**
     * 扫描 gitlab 对应分支的配置文件 保存到数据库
     *
     * @param requestParam
     * @return
     */
    public void saveAppConfigByWebHook(Map<String, Object> requestParam) throws Exception {
        // 触发 webhook 对应的分支
        String branch = ((String) requestParam.get(Dict.BRANCH)).trim();
        // 触发提交的配置文件目录
        String application = ((String) requestParam.get(Dict.APPLICATION));
        // 触发 webhook 对应的应用 projectId
        Integer serviceGitId = ((Integer) requestParam.get(Dict.PROJECTID));
        // 获取文件内容 先获取ci目录下文件 如果为空则获取
        String fileContent = "";
        if(Constants.CI_FDEV_APPLICATION.equals(application)) {
        	//获取ci目录下文件
        	fileContent = this.gitlabApiService.getFileContent(this.token, serviceGitId.toString(), branch, this.applicationFileCi);
        }else {
        	//获取gitlab-ci目录下文件
        	fileContent = this.gitlabApiService.getFileContent(this.token, serviceGitId.toString(), branch, this.applicationFile);
        }
        boolean fileFlag = CommonUtil.checkFileContentIsNone(fileContent);
        List<EntityField> modelField;
        if (fileFlag) {
            modelField = new ArrayList<>();
        } else {
            // 解析文件内容
            modelField = parseFileContent(fileContent);
        }
        saveOrUpdate(serviceGitId, branch, modelField);
    }

    public void saveOrUpdate(Integer serviceGitId, String branch, List<EntityField> entityFieldList) throws Exception {
    	Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Dict.SERVICEGITID, String.valueOf(serviceGitId));
        paramMap.put(Dict.BRANCH, branch);
    	List<ServiceConfig> queryResult = this.serviceConfigDao.queryByServiceGitId(paramMap);
        ServiceConfig serviceConfig = new ServiceConfig();
        // 数据库中不存在，则新增
        if (CommonUtil.isNullOrEmpty(queryResult)) {
        	serviceConfig.setServiceGitId(String.valueOf(serviceGitId));
        	serviceConfig.setBranch(branch);
        	serviceConfig.setEntityField(entityFieldList);
        	serviceConfig.setCreateTime(TimeUtils.formatToday());
        	serviceConfig.setUpdateTime(TimeUtils.formatToday());
            this.serviceConfigDao.addServiceConfig(serviceConfig);
        }else {
        	// 存在 则 更新
            serviceConfig = queryResult.get(0);
            serviceConfig.setEntityField(entityFieldList);
            serviceConfig.setUpdateTime(TimeUtils.formatToday());
            this.serviceConfigDao.updateServiceConfig(serviceConfig);	
        }
    }

    /**
     * 解析文件内容
     *
     * @param fileContent
     */
    private List<EntityField> parseFileContent(String fileContent)  throws Exception {
        Map<String, Object> analysisConfigFileMap = configFileService.analysisConfigFile(fileContent);
        Map<String, Object> primaryMap = (Map<String, Object>) analysisConfigFileMap.get(Dict.MODEL_FIELD);
        List<EntityField> result = new ArrayList<>();
        primaryMap.forEach((key, value) -> result.add(new EntityField(key, (Set<Object>) value)));
        return result;
    }
}
