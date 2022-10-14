package com.spdb.fdev.spdb.service.impl;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.FdevUserCacheUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.spdb.dao.IEntityDao;
import com.spdb.fdev.spdb.dao.IEntityTemplateDao;
import com.spdb.fdev.spdb.dao.IServiceConfigDao;
import com.spdb.fdev.spdb.entity.Entity;
import com.spdb.fdev.spdb.entity.EntityField;
import com.spdb.fdev.spdb.entity.EntityTemplate;
import com.spdb.fdev.spdb.entity.ServiceConfig;
import com.spdb.fdev.spdb.service.IConfigFileService;
import com.spdb.fdev.spdb.service.IGitlabApiService;
import com.spdb.fdev.spdb.service.IRestService;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RefreshScope
public class ConfigFileServiceImpl implements IConfigFileService {

    @Value("${record.switch}")
    private Boolean recordSwitch;
    @Value("${gitlab.token}")
    private String token;
    @Value("${gitlab.application.file}")
    private String applicationFile;
    @Value("${gitlab.application.file.ci}")
   	private String applicationFileCi;
    @Value("${gitlab.application.yaml.file}")
    private String yamlFile;
    @Value("${gitlab.ci-application.file}")
    private String ci_applicationFile;
    @Autowired
    private IGitlabApiService gitlabApiService;
    @Autowired
    public UserVerifyUtil userVerifyUtil;
    @Autowired
    IRestService restService;
    @Autowired
    private IEntityDao entityDao;
    @Autowired
    private IEntityTemplateDao entityTemplateDao;
    @Autowired
    private IServiceConfigDao serviceConfigDao;
    @Autowired
    private RestTransport restTransport;
    @Autowired
    private FdevUserCacheUtil fdevUserCacheUtil;

    /**
     * 用于保存配置模板
     *
     * @param requestParam
     */
    @Override
    public Map saveConfigTemplate(Map<String, Object> requestParam) throws Exception {
        // 文件内容
        String content = ((String) requestParam.get(Dict.CONTENT)).trim();
        // 任务对应的分支
        String featureBranch = ((String) requestParam.get(Dict.FEATUREBRANCH)).trim();
        // 校验文件内容
        Map<String, Object> errorMap = this.analysisConfigFile(content);
        List<String> formatError = (List<String>) errorMap.get(Dict.FORMATERROR);
        List<String> modelErrorList = (List<String>) errorMap.get(Dict.MODELERRORLIST);
        List<String> FiledErrorList = (List<String>) errorMap.get(Dict.FILEDERRORLIST);
        if (CollectionUtils.isNotEmpty(formatError) || CollectionUtils.isNotEmpty(modelErrorList) || CollectionUtils.isNotEmpty(FiledErrorList)) {
            errorMap.remove(Dict.MODELS);
            errorMap.remove(Dict.MODEL_FIELD);
            return errorMap;
        }

        // 保存配置模板时记录操作人
        User user = null;
        if (Boolean.TRUE.equals(this.recordSwitch)) {
            user = userVerifyUtil.getRedisUser();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Dict.COMMIT_MESSAGE);
        if (user != null && StringUtils.isNotEmpty(user.getEmail())) {
            stringBuilder.append(" by ").append(user.getEmail());
        }

        String gitlabId = "";
        String applicationFile = "";
        if (CommonUtil.isNullOrEmpty(requestParam.get(Dict.PROJECTID))) {     //组件模块获取保存模版的信息
            gitlabId = ((String) requestParam.get(Dict.GITLABID)).trim();
            applicationFile = this.ci_applicationFile;
            // 判断 分支是否存在
            this.gitlabApiService.checkBranch(this.token, gitlabId, featureBranch);
        } else {
        	// 任务对应的应用
            String projectId = ((String) requestParam.get(Dict.PROJECTID)).trim();
            // 通过id 查询应用信息
            Map<String, Object> app  = this.restService.queryApp(projectId);
            gitlabId = ((Integer) app.get(Dict.GITLAB_PROJECT_ID)).toString();
             // 判断 分支是否存在
            this.gitlabApiService.checkBranch(this.token, gitlabId, featureBranch);
        	//获取ci目录下配置文件
        	String oldContent = this.gitlabApiService.getFileContent(this.token, gitlabId, featureBranch, this.applicationFileCi);
    		//为空获取gitlab-ci目录下配置文件  不为空赋值 ci 保存配置文件至ci目录下
        	if(CommonUtil.isNullOrEmpty(oldContent)) {
        		//获取gitlab-ci目录下配置文件
    			oldContent = this.gitlabApiService.getFileContent(this.token, gitlabId, featureBranch, this.applicationFile);
    			//为空赋值 ci 保存配置文件至ci目录下 不为空保存至 gitlab-ci目录下
    			if(CommonUtil.isNullOrEmpty(oldContent)) {
    				//ci/fdev-application.properties
    				applicationFile = this.applicationFileCi;
    			}else {
    				//gitlab-ci/fdev-application.properties
    				applicationFile = this.applicationFile;
    			}
    		}else
    			//ci/fdev-application.properties
    			applicationFile = this.applicationFileCi;
    		
        }
        // 用于保存配置模板
        this.gitlabApiService.createFile(this.token, gitlabId, featureBranch, applicationFile, content, stringBuilder.toString());
        return null;
    }

    /**
     * 解析配置模板，校验模板，获得模板中所引用的实体信息
     *
     * @param configFileContent
     * @return
     */
    @Override
    public Map<String, Object> analysisConfigFile(String configFileContent) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<String> formatErrorList = new ArrayList<>();
        List<String> modelErrorList = new ArrayList<>();
        List<String> modelFieldErrorList = new ArrayList<>();
        // 记录等号左边的appkey
        List<String> leftAppKey = new ArrayList<>();
        // 记录行号及当前行"$<"与">"之间的内容
        Map<Integer, Set<String>> recordMap = new HashMap<>();
        Set<String> entityEnSet = new HashSet<>();
        SetMultimap<String, Object> modelFieldMultimap = LinkedHashMultimap.create();
        // 解析配置模板
        configFileContent = configFileContent.replace("\r", "");
        String[] configFileContentSplit = configFileContent.split("\n");
        for (int i = 0; i < configFileContentSplit.length; i++) {
            Integer lineNum = i + 1;
            String lineContent = configFileContentSplit[i];
            // 跳过注释
            if (lineContent.startsWith(Constants.NOTE_PLACEHOLDER)) {
                continue;
            }
            // 跳过不包括等号的行
            String[] lineSplit = lineContent.split("=", 2);
            if (lineSplit.length <= 1) {
                continue;
            }
            // 记录等号左边的appkey
            leftAppKey.add(lineSplit[0]);
            // 若等号右边的内容包括$<>，则获取$<>中的内容
            String rightContent = lineSplit[1];
            if (rightContent.contains("$<")) {
                String[] rightContentSplit = rightContent.split("\\$<");
                Set<String> lineModelFieldSet = new HashSet<>();
                for (int k = 1; k < rightContentSplit.length; k++) {
                    String singleRightContentSplit = rightContentSplit[k];
                    if (singleRightContentSplit.contains(">")) {
                        String modelField = singleRightContentSplit.split(">", 2)[0];
                        if (StringUtils.isEmpty(modelField) || modelField.contains("$") || modelField.contains("<")) {
                            formatErrorList.add("配置模版中第" + lineNum + "行不符合\"$<实体英文名.实体属性英文名>\"的规范！");
                        } else {
                            String[] centerContentSplit = modelField.split("\\.");
                            // "$<"与">"之间的内容，包含且只包含一个"."，且"."前后的内容都不能为空
                            if (!modelField.endsWith(".") && centerContentSplit.length == 2 && StringUtils.isNotEmpty(centerContentSplit[0]) && StringUtils.isNotEmpty(centerContentSplit[1])) {
                                lineModelFieldSet.add(modelField);
                                entityEnSet.add(centerContentSplit[0]);
                            } else {
                                formatErrorList.add("配置模版中第" + lineNum + "行不符合\"$<实体英文名.实体属性英文名>\"的规范！");
                            }
                        }
                    } else {
                        formatErrorList.add("配置模版中第" + lineNum + "行不符合\"$<实体英文名.实体属性英文名>\"的规范！");
                    }
                }
                recordMap.put(lineNum, lineModelFieldSet);
            }
            if (rightContent.endsWith("$<")) {
                formatErrorList.add("配置模版中第" + lineNum + "行不符合\"$<实体英文名.实体属性英文名>\"的规范！");
            }
        }
        // 等号左边的appKey不能重复
        Set<String> repeatKey = CommonUtil.checkRepeat(leftAppKey);
        if (CollectionUtils.isNotEmpty(repeatKey)) {
            formatErrorList.add("配置模版中等号左边的key" + repeatKey + "不能重复！");
        }
        // "$<"和">"中配的实体属性须存在
        List<Entity> entityList = this.entityDao.queryByNameEnSet(entityEnSet);
        Set<Map.Entry<Integer, Set<String>>> entries = recordMap.entrySet();
        for (Map.Entry<Integer, Set<String>> recordMapEntry : recordMap.entrySet()) {
            Integer lineNum = recordMapEntry.getKey();
            Set<String> lineModelFieldSet = recordMapEntry.getValue();
            for (String lineModelField : lineModelFieldSet) {
                String[] lineModelFieldSplit = lineModelField.split("\\.");
                String modelNameEn = lineModelFieldSplit[0];
                String fieldNameEn = lineModelFieldSplit[1];
                boolean modelExistFlag = false;
                boolean filedExistFlag = false;
                for (Entity  entity : entityList) {
                	String templateId = entity.getTemplateId();
                	List<Map<String, Object>> properties = entity.getProperties();
                	if(!CommonUtil.isNullOrEmpty(templateId)) {
                		 EntityTemplate entityTemplate = entityTemplateDao.queryById(templateId);
                		 properties = entityTemplate.getProperties();
                	}
                    if (modelNameEn != null && modelNameEn.equals(entity.getNameEn())) {
                        modelExistFlag = true;
                        for (Map propertie : properties) {
                            if (fieldNameEn != null && fieldNameEn.equals(propertie.get(Dict.NAMEEN))) {
                                filedExistFlag = true;
                                modelFieldMultimap.put(modelNameEn, fieldNameEn);
                                break;
                            }
                        }
                        break;
                    }
                }
                if (!modelExistFlag) {
                    modelErrorList.add("配置模版中第" + lineNum + "行" + modelNameEn + "实体不存在！");
                } else if (!filedExistFlag) {
                    modelFieldErrorList.add("配置模版中第" + lineNum + "行" + modelNameEn + "实体的" + fieldNameEn + "属性不存在！");
                }
            }
        }
        resultMap.put(Dict.FORMATERROR, formatErrorList);
        resultMap.put(Dict.MODELERRORLIST, modelErrorList);
        resultMap.put(Dict.FILEDERRORLIST, modelFieldErrorList);
        resultMap.put(Dict.MODELS, entityList);
        resultMap.put(Dict.MODEL_FIELD, modelFieldMultimap.asMap());
        return resultMap;
    }

	@Override
	public Map<String, Object> queryConfigDependency(Map<String, Object> requestParam) throws Exception {
		Integer page = (Integer) requestParam.get(Dict.PAGE);//页码
        Integer perPage = (Integer) requestParam.get(Dict.PERPAGE);//每页条数
        String entityNameEn = (String) requestParam.get(Dict.ENTITYNAMEEN);//实体英文名
		List<ServiceConfig> serviceConfigs = serviceConfigDao.queryServiceConfig(requestParam);
		Long count = serviceConfigDao.queryServiceConfigCount(requestParam); 
		List<Map> apps = new ArrayList<Map>();
		Set<String> fieldList = new HashSet<String>();
		if(!CommonUtil.isNullOrEmpty(page) && !CommonUtil.isNullOrEmpty(perPage)){
			//应用gitId列表
			Set<Integer> gitlabIds = new HashSet<Integer>();
			//分支map
			Map<String,String> branchMap = new HashMap<String,String>();
			for (ServiceConfig serviceConfig : serviceConfigs) {
				gitlabIds.add(Integer.valueOf(serviceConfig.getServiceGitId()));
				branchMap.put(serviceConfig.getServiceGitId(), serviceConfig.getBranch());
			}
			
			if(!CommonUtil.isNullOrEmpty(gitlabIds)) {
				//根据应用git查询应用信息
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(Dict.GITLABIDS, gitlabIds);
				apps = (List<Map>)restService.queryApps(map);
			}
		}else {
			for(ServiceConfig serviceConfig : serviceConfigs) {
				List<EntityField> entityFieldList = serviceConfig.getEntityField();
				for(EntityField entityField : entityFieldList) {
					if(entityNameEn.equals(entityField.getEntityNameEn()) && !CommonUtil.isNullOrEmpty(entityField.getFields()) ) {
						fieldList.addAll(entityField.getFields());
					}
				}
			}
		}
			
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put(Dict.COUNT, count);
		returnMap.put(Dict.SERVICELIST, apps);
		returnMap.put(Dict.FIELDS, fieldList);
		return returnMap;
	}

	private Map<String, Object> queryServiceByGitlabId(Integer gitlabId) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        List<Integer> gitlabIds = new ArrayList<>();
        gitlabIds.add(gitlabId);
        map.put(Dict.GITLABIDS, gitlabIds);
        List apps = (List<Map>)restService.queryApps(map);
        Map<String, Object> appMap = new HashMap<>();
        if(apps.size() > 0){
            appMap = (Map<String, Object>) apps.get(0);

            //获取小组名称
            appMap.put(Dict.GROUPNAME, (String)(!Util.isNullOrEmpty(appMap) ? appMap.get("name") : ""));
            return appMap;
        }
       return null;
    }

    @Override
    public Map<String, Object> queryDeployDependency(Map<String, Object> requestParam) throws Exception {
        String entityNameEn = (String) requestParam.get(Dict.ENTITYNAMEEN);//实体英文名
        List<String> fields =   (List<String>) requestParam.get(Dict.FIELDS);//修改字段列表
        Integer page = (Integer) requestParam.get(Dict.PAGE);//页码
        Integer perPage = (Integer) requestParam.get(Dict.PERPAGE);//每页条数
        Map<String, Object> result = new HashMap<>();//返回结果
        Set<String> set = new HashSet();
        set.add(entityNameEn);
        List<Entity> entitys = entityDao.queryByNameEnSet(set);
        if(CommonUtil.isNullOrEmpty(entitys)){
            throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA);
        }
        String entityId = entitys.get(0).getId();
        List<String> entityIds = new ArrayList<>();
        entityIds.add(entityId);
        //发送流水线接口，获取实体用到的流水线
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.ENTITYIDS, entityIds);
        param.put(Dict.PAGENUM, page);
        param.put(Dict.PAGESIZE, perPage);
        param.put(Dict.REST_CODE,"getPipelineByEntityId");
        List<Map<String,Object>> pipelineInfo = (List<Map<String, Object>>) restTransport.submit(param);
        List<Map<String, Object>> serviceList = new ArrayList<>();
        Integer count = 0;
        if(!CommonUtil.isNullOrEmpty(pipelineInfo)){
            Map<String, Object> pipelines = pipelineInfo.get(0);
            //封装返回结果
            Map<String, Object> serviceMap = new HashMap();
            List<Map<String, Object>> pipelineList = (List<Map<String, Object>>) pipelines.get("pipelineList");
            count = (Integer) pipelines.get(Dict.COUNT);
            if(count != 0){
                for(Map<String, Object> pipeline:pipelineList){
                    Map<String, Object> bindProject = (Map<String, Object>) pipeline.get("bindProject");
                    serviceMap = this.queryServiceByGitlabId((Integer) bindProject.get("gitlabProjectId"));
                    if(!CommonUtil.isNullOrEmpty(serviceMap)){
                        serviceMap.put(Dict.PIPELINEID,pipeline.get(Dict.PIPELINEID).toString());
                        serviceMap.put(Dict.PIPELINENAME,pipeline.get(Dict.PIPELINENAME).toString());
                        serviceMap.put(Dict.PIPELINENAMEID,pipeline.get(Dict.NAMEID).toString());
                    }else {
                        //应用已被删掉
                        if(count > 0){
                            count--;
                        }
                    }
                    serviceList.add(serviceMap);
                }
            }

        }
        result.put(Dict.COUNT,count);
        result.put(Dict.SERVICELIST,serviceList);
        return result;
    }
}
