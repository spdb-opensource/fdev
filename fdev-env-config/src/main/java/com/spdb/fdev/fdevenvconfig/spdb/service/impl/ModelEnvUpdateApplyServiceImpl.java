package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.base.utils.DES3Util;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.base.utils.WordUtils;
import com.spdb.fdev.fdevenvconfig.base.utils.excel.Export;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelEnvDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.ModelEnvUpdateApplyDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Environment;
import com.spdb.fdev.fdevenvconfig.spdb.entity.JsonSchema;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelEnv;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelEnvUpdateApply;
import com.spdb.fdev.fdevenvconfig.spdb.event.core.EmailListenerCore;
import com.spdb.fdev.fdevenvconfig.spdb.service.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class ModelEnvUpdateApplyServiceImpl implements ModelEnvUpdateApplyService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${record.switch}")
    private Boolean recordSwitch;
    @Value("${model.env.update.apply.page}")
    private String modelEnvUpdateApplyPage;
    @Autowired
    private ModelEnvUpdateApplyDao modelEnvUpdateApplyDao;
    @Autowired
    private IModelEnvService modelEnvService;
    @Autowired
    private IModelService modelService;
    @Autowired
    private IEnvironmentService environmentService;
    @Autowired
    private IModelEnvDao modelEnvDao;
    @Autowired
    private EmailListenerCore emailListenerCore;
    @Autowired
    private ISendEmailService iSendEmailService;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private JsonSchemaService jsonSchemaService;
    @Value("${fileUrl:/fdev/attachment/}")
    private String fileUrl;
    @Value("${fenvconfig.modelenvapply.path}")
    private String modelEnvApplyPath;
    @Autowired
    private IModelDao iModelDao;
    @Autowired
    private DES3Util des3Util;
    @Autowired
	public UserVerifyUtil userVerifyUtil;

    @Override
    public void save(Map<String, Object> requestMap) throws Exception{
        String modelId = (String) requestMap.get(Constants.MODEL_ID);
        String modelNameEn = (String) requestMap.get(Dict.MODEL_NAME_EN);
        String modelNameCn = (String) requestMap.get(Dict.MODEL_NAME_CN);
        String envId = (String) requestMap.get(Constants.ENV_ID);
        String envNameEn = (String) requestMap.get(Dict.ENV_NAME_EN);
        String envNameCn = (String) requestMap.get(Dict.ENV_NAME_CN);
        List<Map<String, Object>> variables = (List<Map<String, Object>>) requestMap.get(Dict.VARIABLES);
        String applyEmail = (String) requestMap.get(Dict.APPLY_EMAIL);
        String applyUsername = (String) requestMap.get(Dict.APPLY_USERNAME);
        String applyUserId = (String) requestMap.get(Dict.APPLY_USER_ID);
        String type = (String) requestMap.get(Dict.TYPE);
        String desc = (String) requestMap.get(Dict.DESC);
        String modelEnvId = (String) requestMap.get(Dict.MODEL_ENV_ID);
        String modifyReason = (String) requestMap.get(Dict.MODIFY_REASON);
        // 验证必传参数
        if (StringUtils.isEmpty(modelId) || StringUtils.isEmpty(modelNameEn) || StringUtils.isEmpty(modelNameCn)
                || StringUtils.isEmpty(envId) || StringUtils.isEmpty(envNameEn) || StringUtils.isEmpty(envNameCn)
                || CollectionUtils.isEmpty(variables) || StringUtils.isEmpty(type) || desc == null) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Constants.MODEL_ID + "," + Dict.MODEL_NAME_EN + ","
                    + Dict.MODEL_NAME_CN + "," + Constants.ENV_ID + "," + Dict.ENV_NAME_EN + "," + Dict.ENV_NAME_CN + "," + Constants.VARIABLES + ","
                    + Dict.DESC, Constants.PARAM_CAN_NOT_NULL});
        }
        // 验证必填属性
        checkRequire(modelNameEn, variables);
        // 如果type为update，则必传model_env_id和修改原因
        if (Dict.UPDATE.equals(type) && (StringUtils.isEmpty(modelEnvId) || modifyReason == null)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.MODEL_ENV_ID + "和" + Dict.MODIFY_REASON, Constants.PARAM_CAN_NOT_NULL});
        }
        // 若为私有实体，直接更新实体与环境映射
        if (Dict.UPDATE.equals(type) && modelNameEn.startsWith(Dict.PRIVATE)) {
            // 获取更新过value的实体属性映射值
            ModelEnv beforeModelEnv = getBeforeModelEnv(modelEnvId);
            Model model = getModel(beforeModelEnv.getModel_id());
            updateModelEnv(requestMap, beforeModelEnv, model, "");
            return;
        }
        if (StringUtils.isEmpty(applyEmail) || StringUtils.isEmpty(applyUsername) || StringUtils.isEmpty(applyUserId)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"", Constants.PARAM_CAN_NOT_NULL});
        }
        // 判断是否存在其他申请人对该实体的任何一个环境映射值的变动申请处于checking状态的
        exitCheckingApplyByOther(modelId, applyEmail);
        // 判断是否存在当前申请人对该实体-环境映射值的变动申请处于checking状态的
        exitCheckingApplyByEnv(modelId, envId, applyEmail);
        ModelEnvUpdateApply modelEnvUpdateApply = new ModelEnvUpdateApply();
        modelEnvUpdateApply.setModelId(modelId);
        modelEnvUpdateApply.setModelNameCn(modelNameCn);
        modelEnvUpdateApply.setModelNameEn(modelNameEn);
        modelEnvUpdateApply.setEnvId(envId);
        modelEnvUpdateApply.setEnvNameCn(envNameCn);
        modelEnvUpdateApply.setEnvNameEn(envNameEn);
        modelEnvUpdateApply.setModelEnvId(modelEnvId);
        modelEnvUpdateApply.setDesc(desc);
        modelEnvUpdateApply.setVariables(variables);
        modelEnvUpdateApply.setType(type);
        modelEnvUpdateApply.setApplyEmail(applyEmail);
        modelEnvUpdateApply.setApplyUsername(applyUsername);
        modelEnvUpdateApply.setApplyUserId(applyUserId);
        modelEnvUpdateApply.setAppNameEn((String) requestMap.get(Dict.APP_NAME_EN));
        modelEnvUpdateApply.setModify_reason(modifyReason);
        // 记录操作的环境管理员
        User user = null;
        modelEnvUpdateApply.setEnvManager("");
        if (this.recordSwitch) {
            try {
                user = userVerifyUtil.getRedisUser();
                if (user != null) {
                    modelEnvUpdateApply.setEnvManager(user.getId());
                }
            } catch (Exception e) {
                modelEnvUpdateApply.setEnvManager("录入小工具");
            }
        }
        // 发送核对邮件
        Map<String, Object> compare = compare(requestMap);
        List<Map<String, Object>> listUpdateEnvKeys = (List<Map<String, Object>>) compare.get(Dict.VARIABLES);
        Map<Integer, Collection<Object>> appIdAndBranchAndKeys = emailListenerCore.getAppIdAndBranchAndKeys(modelNameEn, listUpdateEnvKeys);
        List<Map<String, Object>> appContent;
        try {
            appContent = emailListenerCore.getEmailAppContent(appIdAndBranchAndKeys, new HashSet<>());
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{e.getMessage()});
        }
        Map<String, Object> emailMap = new HashMap<>();
        Set<String> emailTo = new HashSet<>();
        // 发送给申请人
        emailTo.add(applyEmail);
        // 发送给环境配置管理员
        if (user != null && StringUtils.isNotEmpty(user.getEmail())) {
            emailTo.add(user.getEmail());
        }
        emailMap.put(Dict.SUBJECT, Constants.EMAIL_MODEL_ENV_UPDATE_APP);
        emailMap.put(Dict.EMAIL, emailTo);
        emailMap.put(Dict.APPLY_USERNAME, applyUsername);
        emailMap.put(Dict.URL, modelEnvUpdateApplyPage);
        emailMap.put(Dict.MODEL_NAME_EN, modelNameEn);
        emailMap.put(Dict.MODEL_NAME_CN, modelNameCn);
        emailMap.put(Dict.ENV_NAME_EN, envNameEn);
        emailMap.put(Dict.ENV_NAME_CN, envNameCn);
        emailMap.put(Dict.UPDATE, listUpdateEnvKeys);
        emailMap.put(Dict.APP, appContent);
        iSendEmailService.sendModelEnvUpdateappEmail(emailMap);
        modelEnvUpdateApplyDao.save(modelEnvUpdateApply);
    }

    @Override
    public void finish(Map<String, Object> requestMap) throws Exception{
        // 验证申请人和当前操作人是否为同一人
        if (Boolean.TRUE.equals(this.recordSwitch)) {
            Object userIdObj = requestMap.get(Dict.APPLY_USER_ID);
            User user = userVerifyUtil.getRedisUser();
            if (userIdObj != null && !userIdObj.equals(user.getId())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR);
            }
        }
        Object idObj = requestMap.get(Dict.ID);
        Object typeObj = requestMap.get(Dict.TYPE);
        String modelEnvId = (String) requestMap.get(Dict.MODEL_ENV_ID);
        // 验证必传参数
        if (idObj == null || typeObj == null) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID + "," + Dict.TYPE, Constants.PARAM_CAN_NOT_NULL});
        }
        // 申请流水号
        String applyId = String.valueOf(idObj);
        if (Dict.UPDATE.equals(typeObj)) {
            if (StringUtils.isEmpty(modelEnvId)) {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.MODEL_ENV_ID, Constants.PARAM_CAN_NOT_NULL});
            }
            ModelEnv beforeModelEnv = getBeforeModelEnv(modelEnvId);
            Model model = getModel(beforeModelEnv.getModel_id());
            // 更新实体与环境映射
            updateModelEnv(requestMap, beforeModelEnv, model, applyId);
        } else {
            // 新增实体与环境映射值
            addModelEnv(requestMap);
        }
        // 修改申请表状态
        modelEnvUpdateApplyDao.updateStatus(applyId, Dict.FINISHED);
    }

    @Override
    public void update(Map<String, Object> requestMap) {
        String id = (String) requestMap.get(Dict.ID);
        String modelId = (String) requestMap.get(Constants.MODEL_ID);
        String envId = (String) requestMap.get(Constants.ENV_ID);
        String applyEmail = (String) requestMap.get(Dict.APPLY_EMAIL);
        // 验证必传参数
        if (StringUtils.isEmpty(id) || StringUtils.isEmpty(modelId) || StringUtils.isEmpty(envId) || StringUtils.isEmpty(applyEmail)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID + "," + Constants.MODEL_ID + "," + Constants.ENV_ID + "," + Dict.APPLY_EMAIL, Constants.PARAM_CAN_NOT_NULL});
        }
        // 判断是否存在其他申请人对该实体的任何一个环境映射值的变动申请处于checking状态的
        exitCheckingApplyByOther(modelId, applyEmail);
        // 判断是否存在当前申请人对该实体-环境映射值的变动申请处于checking状态的
        exitCheckingApplyByEnv(modelId, envId, applyEmail);
        modelEnvUpdateApplyDao.updateStatus(id, Dict.CHECKING);
    }

    @Override
    public Map<String, Object> listModelEnvUpdateApplysByPage(Map<String, Object> requestMap) {
        String modelId = (String) requestMap.get(Constants.MODEL_ID);
        String appUserId = (String) requestMap.get(Dict.APPLY_USER_ID);
        String status = (String) requestMap.get(Dict.STATUS);
        Object pageObj = requestMap.get(Dict.PAGE);
        Object perPageObj = requestMap.get(Dict.PER_PAGE);
        if (pageObj == null || perPageObj == null) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.PAGE + "," + Dict.PER_PAGE, Constants.PARAM_CAN_NOT_NULL});
        }
        return modelEnvUpdateApplyDao.listByPage(modelId, appUserId, status, (int) pageObj, (int) perPageObj);
    }

    @Override
    public Map<String, Object> compare(Map<String, Object> requestMap) {
        Map<String, Object> responseMap = new HashMap<>();
        String modelEnvId = (String) requestMap.get(Dict.MODEL_ENV_ID);
        List<Map<String, Object>> afterVariables = (List<Map<String, Object>>) requestMap.get(Dict.VARIABLES);
        String desc = (String) requestMap.get(Dict.DESC);
        String type = (String) requestMap.get(Dict.TYPE);
        String id = (String) requestMap.get(Dict.ID);
        
        // 若为新增类型，直接返回所有实体属性列表值
        if (Dict.INSERT.equals(type)) {
            for (Map<String, Object> afterVariable : afterVariables) {
                String dataType = (String) afterVariable.get(Dict.DATA_TYPE);
                if (Dict.OBJECT.equals(dataType)) {
                    afterVariable.put(Dict.OLD_VALUE, new HashMap<>());
                } else if (Dict.ARRAY.equals(dataType)) {
                    afterVariable.put(Dict.OLD_VALUE, new ArrayList<>());
                } else {
                    afterVariable.put(Dict.OLD_VALUE, "");
                }
                afterVariable.put(Dict.NEW_VALUE, afterVariable.get(Dict.VALUE));
            }
            Map<String, String> descMap = new HashMap<>();
            descMap.put(Dict.OLD_VALUE, "");
            descMap.put(Dict.NEW_VALUE, desc);
            responseMap.put(Dict.DESC, descMap);
            responseMap.put(Dict.VARIABLES, afterVariables);
            return requestMap;
        } else { // 非新增的情况
        	// 获取修改前的实体与环境映射对象
            ModelEnv beforeModelEnv = getBeforeModelEnv(modelEnvId);
            if(null == beforeModelEnv){
            	modelEnvUpdateApplyDao.updateStatus(id, Dict.CANCEL);
            	throw new FdevException(ErrorConstants.MODEL_ENV_NOT_EXIT_ERROR);
            } else {
            	// 获取实体
                Model model = getModel(beforeModelEnv.getModel_id());
                // 获取环境
                Environment environment = getEnv(beforeModelEnv.getEnv_id());
                if(null == model){
                	modelEnvUpdateApplyDao.updateStatus(id, Dict.CANCEL);
                	throw new FdevException(ErrorConstants.MODEL_NOT_EXIT_ERROR);
                }
                if(null == environment){
                	modelEnvUpdateApplyDao.updateStatus(id, Dict.CANCEL);
                	throw new FdevException(ErrorConstants.ENV_NOT_EXIT_ERROR);
                }
                // 获取更新过value的实体属性映射值
                List<Map<String, Object>> listUpdateEnvKeys = listUpdateValueEnvKeys(beforeModelEnv, model, afterVariables, id);
                responseMap.put(Dict.VARIABLES, listUpdateEnvKeys);
                return responseMap;
            }
        }
    }

    @Override
    public String downloadAppInfo(Map<String, Object> requestMap, HttpServletResponse response) {
        String modelNameEn = (String) requestMap.get(Dict.MODEL_NAME_EN);
        String modelNameCn = (String) requestMap.get(Dict.MODEL_NAME_CN);
        String envNameEn = (String) requestMap.get(Dict.ENV_NAME_EN);
        String envNameCn = (String) requestMap.get(Dict.ENV_NAME_CN);
        String applyUsername = (String) requestMap.get(Dict.APPLY_USERNAME);
        List<Map<String, Object>> listUpdateEnvKeys = (List<Map<String, Object>>) requestMap.get(Dict.VARIABLES);
        for (Map<String, Object> updateEnvKey : listUpdateEnvKeys) {
            updateEnvKey.put(Dict.VALUE, String.valueOf(updateEnvKey.get(Dict.VALUE)));
            updateEnvKey.put(Dict.OLD_VALUE, String.valueOf(updateEnvKey.get(Dict.OLD_VALUE)));
            updateEnvKey.put(Dict.NEW_VALUE, String.valueOf(updateEnvKey.get(Dict.NEW_VALUE)));
        }
        // 验证必传参数
        if (StringUtils.isEmpty(modelNameEn) || StringUtils.isEmpty(modelNameCn)
                || StringUtils.isEmpty(envNameEn) || StringUtils.isEmpty(envNameCn) || StringUtils.isEmpty(applyUsername)
                || CollectionUtils.isEmpty(listUpdateEnvKeys)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR,
                    new String[]{Dict.MODEL_NAME_EN + "," + Dict.MODEL_NAME_CN + "," + Dict.ENV_NAME_EN + "," + Dict.ENV_NAME_CN + "," + Dict.VARIABLES + "," + Dict.APPLY_USERNAME, Constants.PARAM_CAN_NOT_NULL});
        }
        // 获取受影响应用的GitLab Id、分支、具体哪些属性
        Map<Integer, Collection<Object>> appIdAndBranchAndKeys = emailListenerCore.getAppIdAndBranchAndKeys(modelNameEn, listUpdateEnvKeys);
        List<Map<String, Object>> appContent;
        try {
            appContent = emailListenerCore.getEmailAppContent(appIdAndBranchAndKeys, new HashSet<>());
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{e.getMessage()});
        }
        // 组装生成文档的数据
        Map<String, Object> fileContentMap = new HashMap<>();
        fileContentMap.put(Dict.MODEL_NAME_EN, modelNameEn);
        fileContentMap.put(Dict.MODEL_NAME_CN, modelNameCn);
        fileContentMap.put(Dict.ENV_NAME_EN, envNameEn);
        fileContentMap.put(Dict.ENV_NAME_CN, envNameCn);
        fileContentMap.put(Dict.UPDATE, listUpdateEnvKeys);
        fileContentMap.put(Dict.APP, appContent);
        fileContentMap.put(Dict.APPLY_USERNAME, applyUsername);
        fileContentMap.put(Dict.URL, modelEnvUpdateApplyPage);
        // 生成文档，返回下载链接
        try {
            String fileName = "影响范围清单" + DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT) + Dict.FILE_DOC;
            String filePath = fileUrl + fileName;
            WordUtils.exportMillCertificateWord(fileContentMap, "download.ftl", filePath, freeMarkerConfigurer);
            return modelEnvApplyPath + "/modelEnvUpdateApply/downloadWorld/" + fileName;
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.APP_FILE_ERROR, new String[]{e.getMessage() + "本地文件生成失败！"});
        }
    }

    @Override
    public void exportFile(String fileName, HttpServletResponse response) throws Exception {
        String filePath = fileUrl + fileName;
        Export.downloadWorld(fileName, filePath, response);
    }

    @Override
    public void updateStatus(String dateTime) {
        List<ModelEnvUpdateApply> modelEnvUpdateApplyList = modelEnvUpdateApplyDao.listCheckingByDate(dateTime);
        if (CollectionUtils.isNotEmpty(modelEnvUpdateApplyList)) {
            List<String> ids = modelEnvUpdateApplyList.stream().map(ModelEnvUpdateApply::getId).collect(Collectors.toList());
            modelEnvUpdateApplyDao.updateStatus(ids);
        }
    }

    @Override
    public void cancel(Map<String, Object> requestMap) {
        String id = (String) requestMap.get(Dict.ID);
        // 验证必传参数
        if (StringUtils.isEmpty(id)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.ID, Constants.PARAM_CAN_NOT_NULL});
        }
        modelEnvUpdateApplyDao.updateStatus(id, Dict.CANCEL);
    }

    /**
     * 判断是否存在其他申请人对该实体的任何一个环境映射值的变动申请处于checking状态的
     *
     * @param modelId
     * @param email
     * @return
     */
    private void exitCheckingApplyByOther(String modelId, String email) {
        List<ModelEnvUpdateApply> checkingModelEnvUpdateApplyList = modelEnvUpdateApplyDao.listCheckingModelEnvUpdateApplys(modelId, email);
        if (CollectionUtils.isNotEmpty(checkingModelEnvUpdateApplyList)) {
            String applyUsername = checkingModelEnvUpdateApplyList.get(0).getApplyUsername();
            throw new FdevException(ErrorConstants.APPLY_UPDATE_MODEL_ENV_ERROR, new String[]{applyUsername + "尚未核对确认该实体与环境映射的前期改动，请提醒该申请人尽快核对"});
        }
    }

    /**
     * 判断是否存在当前申请人对该实体-环境映射值的变动申请处于checking状态的
     *
     * @param modelId
     * @param envId
     * @param email
     */
    private void exitCheckingApplyByEnv(String modelId, String envId, String email) {
        List<ModelEnvUpdateApply> checkingModelEnvUpdateApplyList = modelEnvUpdateApplyDao.listCheckingModelEnvUpdateApplys(modelId, envId, email);
        if (CollectionUtils.isNotEmpty(checkingModelEnvUpdateApplyList)) {
            String applyUsername = checkingModelEnvUpdateApplyList.get(0).getApplyUsername();
            throw new FdevException(ErrorConstants.APPLY_UPDATE_MODEL_ENV_ERROR, new String[]{applyUsername + "尚未核对确认该实体与该环境映射的前期改动，请提醒该申请人尽快核对"});
        }
    }

    /**
     * 获取修改前的实体与环境映射对象
     *
     * @param modelEnvId
     * @return
     */
    private ModelEnv getBeforeModelEnv(String modelEnvId) {
        ModelEnv modelEnvParam = new ModelEnv();
        modelEnvParam.setId(modelEnvId);
        return this.modelEnvDao.queryId(modelEnvParam);
    }

    /**
     * 获取实体
     *
     * @param modelId
     * @return
     */
    private Model getModel(String modelId) {
        Model modelParam = new Model();
        modelParam.setId(modelId);
        try {
            return this.modelService.queryById(modelParam);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{e.getMessage()});
        }
    }
    
    /**
     * 获取环境
     *
     * @param envId
     * @return
     */
    private Environment getEnv(String envId) {
    	Environment environment = new Environment();
    	environment.setId(envId);
        try {
            return this.environmentService.queryById(environment);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{e.getMessage()});
        }
    }

    /**
     * 获取更新过value的实体属性映射值
     *
     * @param beforeModelEnv
     * @param model
     * @param afterVariables
     * @return
     */
    private List<Map<String, Object>> listUpdateValueEnvKeys(ModelEnv beforeModelEnv, Model model, List<Map<String, Object>> afterVariables, String applyId) {
        // 获取修改前的实体映射值
        Map<String, Object> beforeVariablesMap = beforeModelEnv.getVariables();
        List<Map<String, Object>> envKeyList = (List<Map<String, Object>>) (Object) model.getEnv_key();
        Map<String, Object> afterVariablesMap = new HashMap<>();
        Map<String, Object> modelEnvMap = new HashMap<>();
        for (Map<String, Object> variable : afterVariables) {
            afterVariablesMap.put((String) variable.get(Constants.ID), variable.get(Constants.VALUE));
        }
        for(Map<String, Object> envKeyMap : envKeyList){
        	// 实体中为必填项，但实体与环境映射中没填写
        	if("1".equals(envKeyMap.get(Dict.REQUIRE)) &&
        			(!afterVariablesMap.containsKey(envKeyMap.get(Dict.ID)) || "".equals(afterVariablesMap.get(envKeyMap.get(Dict.ID))))){
        		modelEnvUpdateApplyDao.updateStatus(applyId, Dict.CANCEL);
        		throw new FdevException(ErrorConstants.MODEL_ENV_FILED_MISS_ERROR, new String[]{(String) envKeyMap.get(Constants.NAME_CN)});
        	} else {
        		modelEnvMap.put((String) envKeyMap.get(Constants.ID), envKeyMap.get(Constants.VALUE));
        	}
        }
        List<Map<String, Object>> keyMapList = new ArrayList<>();
        // 需要判断是否存在实体新增属性的情况(暂不考虑实体删除属性的情况)
        for (Iterator<Map.Entry<String, Object>> afterIterator = afterVariablesMap.entrySet().iterator(); afterIterator.hasNext(); ) {
            Map.Entry<String, Object> afterEntry = afterIterator.next();
        	if(!modelEnvMap.containsKey(afterEntry.getKey())){// 实体中删除了该属性，但实体与环境映射中没删除
        		for(Map<String, Object> variable : afterVariables){
        			if(afterEntry.getKey().equals(variable.get(Constants.ID))){
        				modelEnvUpdateApplyDao.updateStatus(applyId, Dict.CANCEL);
        				throw new FdevException(ErrorConstants.MODEL_ENV_FILED_ERROR, new String[]{(String) variable.get(Constants.NAME_CN)}); 
        			}
        		}
        	}
            for (Iterator<Map.Entry<String, Object>> beforeIterator = beforeVariablesMap.entrySet().iterator(); beforeIterator.hasNext(); ) {
                Map.Entry<String, Object> beforeEntry = beforeIterator.next();
                if (beforeEntry.getKey().equals(afterEntry.getKey())) {
                    if (!beforeEntry.getValue().equals(afterEntry.getValue())) {
                        Map<String, Object> keyMap = new HashMap<>();
                        for (Map<String, Object> envKeyMap : envKeyList) {
                            if (envKeyMap.get(Dict.ID).equals(beforeEntry.getKey())) {
                                keyMap.put(Dict.NAME_EN, envKeyMap.get(Dict.NAME_EN));
                                keyMap.put(Dict.NAME_CN, envKeyMap.get(Dict.NAME_CN));
                                keyMap.put(Dict.REQUIRE, envKeyMap.get(Dict.REQUIRE));
                                keyMap.put(Dict.DATA_TYPE, envKeyMap.get(Dict.DATA_TYPE));
                                keyMap.put(Dict.DESC, envKeyMap.get(Dict.DESC));
                                keyMap.put(Dict.ID, envKeyMap.get(Dict.ID));
                                keyMap.put(Dict.OLD_VALUE, beforeEntry.getValue());
                                keyMap.put(Dict.NEW_VALUE, afterEntry.getValue());
                                keyMapList.add(keyMap);
                                break;
                            }
                        }
                    }
                    // 将更新前后共有的实体属性值删除，最后afterVariablesMap剩下的值即为新增的，beforeVariablesMap剩下的值即为删除的
                    beforeIterator.remove();
                    afterIterator.remove();
                }
            }
        }
        for (Map.Entry<String, Object> afterEntry : afterVariablesMap.entrySet()) {
            Map<String, Object> keyMap = new HashMap<>();
            for (Map<String, Object> envKeyMap : envKeyList) {
                if (envKeyMap.get(Dict.ID).equals(afterEntry.getKey())) {
                    Object newValue = afterEntry.getValue();
                    if (CommonUtils.isNullOrEmpty(newValue)) {
                        break;
                    }
                    keyMap.put(Dict.NAME_EN, envKeyMap.get(Dict.NAME_EN));
                    keyMap.put(Dict.NAME_CN, envKeyMap.get(Dict.NAME_CN));
                    keyMap.put(Dict.REQUIRE, envKeyMap.get(Dict.REQUIRE));
                    keyMap.put(Dict.DESC, envKeyMap.get(Dict.DESC));
                    keyMap.put(Dict.ID, envKeyMap.get(Dict.ID));
                    String dataType = (String) envKeyMap.get(Dict.DATA_TYPE);
                    keyMap.put(Dict.DATA_TYPE, dataType);
                    if (Dict.OBJECT.equals(dataType)) {
                        keyMap.put(Dict.OLD_VALUE, new HashMap<>());
                    } else if (Dict.ARRAY.equals(dataType)) {
                        keyMap.put(Dict.OLD_VALUE, new ArrayList<>());
                    } else {
                        keyMap.put(Dict.OLD_VALUE, "");
                    }
                    keyMap.put(Dict.NEW_VALUE, newValue);
                    keyMapList.add(keyMap);
                    break;
                }
            }
        }
        return keyMapList;
    }

    private void addModelEnv(Map<String, Object> requestMap) {
        Object modelId = requestMap.get(Constants.MODEL_ID);
        Object envId = requestMap.get(Constants.ENV_ID);
        Object desc = requestMap.get(Dict.DESC);
        Object variables = requestMap.get(Dict.VARIABLES);
        // 验证必传参数
        if (modelId == null || envId == null || desc == null || variables == null) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Constants.MODEL_ID + ","
                    + Constants.ENV_ID + "," + Dict.DESC + "," + Constants.VARIABLES, Constants.PARAM_CAN_NOT_NULL});
        }
        Map<String, Object> addParam = new HashMap<>();
        addParam.put(Constants.MODEL_ID, modelId);
        addParam.put(Constants.ENV_ID, envId);
        addParam.put(Dict.DESC, desc);
        addParam.put(Dict.VARIABLES, variables);
        try {
            modelEnvService.add(addParam);
        } catch (FdevException e) {
            throw new FdevException(ErrorConstants.INSERT_MODEL_ENV_ERROR, new String[]{String.valueOf(e.getArgs()[0])});
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.INSERT_MODEL_ENV_ERROR, new String[]{e.getMessage()});
        }
    }

    /**
     * 更新实体与环境映射
     *
     * @param requestMap
     * @param beforeModelEnv
     * @param model
     * @param applyId        申请流水号
     */
    private void updateModelEnv(Map<String, Object> requestMap, ModelEnv beforeModelEnv, Model model, String applyId) throws Exception{
        // 组装参数
        Map<String, Object> paramMap = new HashMap<>();
        String modelEnvId = (String) requestMap.get(Dict.MODEL_ENV_ID);
        String modelId = (String) requestMap.get(Constants.MODEL_ID);
        String envId = (String) requestMap.get(Constants.ENV_ID);
        String envNameEn = (String) requestMap.get(Dict.ENV_NAME_EN);
        String envNameCn = (String) requestMap.get(Dict.ENV_NAME_CN);
        Object descObj = requestMap.get(Dict.DESC);
        Object variablesObj = requestMap.get(Dict.VARIABLES);
        // 验证必传参数
        if (StringUtils.isEmpty(modelEnvId) || StringUtils.isEmpty(modelId)
                || StringUtils.isEmpty(envId) || StringUtils.isEmpty(envNameEn) || StringUtils.isEmpty(envNameCn)
                || descObj == null || variablesObj == null) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Dict.MODEL_ENV_ID + "," + Constants.MODEL_ID + ","
                    + Constants.ENV_ID + "," + Dict.ENV_NAME_EN + "," + Dict.ENV_NAME_CN + ","
                    + Dict.DESC + "," + Constants.VARIABLES, Constants.PARAM_CAN_NOT_NULL});
        }
        paramMap.put(Dict.ID, modelEnvId);
        paramMap.put(Constants.MODEL_ID, modelId);
        paramMap.put(Constants.ENV_ID, envId);
        paramMap.put(Dict.DESC, descObj);
        paramMap.put(Dict.VARIABLES, variablesObj);
        paramMap.put(Constants.OPNO, beforeModelEnv.getOpno());
        paramMap.put(Constants.CTIME, beforeModelEnv.getCtime());
        paramMap.put(Dict.MODEL_NAME_EN, model.getName_en());
        Map<String, Object> modelMap = new HashMap<>();
        modelMap.put(Dict.ENV_KEY, model.getEnv_key());
        modelMap.put(Dict.NAME_CN, model.getName_cn());
        modelMap.put(Dict.NAME_EN, model.getName_en());
        modelMap.put(Dict.ID, modelId);
        paramMap.put(Dict.MODEL, modelMap);
        // 组装环境信息
        paramMap.put(Dict.ENV_NAME_EN, envNameEn);
        Map<String, Object> envMap = new HashMap<>();
        envMap.put(Dict.ID, envId);
        envMap.put(Dict.NAME_CN, envNameCn);
        paramMap.put(Dict.ENV, envMap);
        paramMap.put(Dict.APPLY_ID, applyId);
        if (StringUtils.isEmpty(applyId)) {
            // applyId为空，表示私有实体修改
            if (this.recordSwitch) {
                User user = userVerifyUtil.getRedisUser();
                if (user != null) {
                    paramMap.put(Dict.APPLY_USERNAME, user.getUser_name_cn());
                }
            }
            paramMap.put(Dict.MODIFY_REASON, "私有实体映射值修改");
        } else {
            ModelEnvUpdateApply modelEnvUpdateApply = modelEnvUpdateApplyDao.get((String) requestMap.get(Dict.ID));
            if (modelEnvUpdateApply == null) {
                throw new FdevException(ErrorConstants.UPDATE_MODEL_ENV_ERROR, new String[]{model.getName_en(), envNameEn, "获取修改原因失败！"});
            }
            String applyUsername = modelEnvUpdateApply.getApplyUsername();
            String modifyReason = modelEnvUpdateApply.getModify_reason();
            paramMap.put(Dict.APPLY_USERNAME, applyUsername);
            paramMap.put(Dict.MODIFY_REASON, modifyReason);
        }
        try {
            modelEnvService.update(paramMap);
        } catch (FdevException e) {
            throw new FdevException(ErrorConstants.UPDATE_MODEL_ENV_ERROR, new String[]{model.getName_en(), envNameEn, String.valueOf(e.getArgs()[0])});
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.UPDATE_MODEL_ENV_ERROR, new String[]{model.getName_en(), envNameEn, e.getMessage()});
        }
    }

    /**
     * 验证必填属性
     *
     * @param modelNameEn
     * @param variables
     */
    private void checkRequire(String modelNameEn, List<Map<String, Object>> variables) {
        Model model = modelService.getByNameEn(modelNameEn);
        List<Object> modelEnvKeys = model.getEnv_key();
        for (Object envKeyObj : modelEnvKeys) {
            Map<String, Object> modelEnvKey = (Map<String, Object>) envKeyObj;
            String modelEnvKeyNameEn = (String) modelEnvKey.get(Dict.NAME_EN);
            String require = String.valueOf(modelEnvKey.get(Dict.REQUIRE));
            if (Constants.ONE.equals(require)) {
                for (Map<String, Object> variable : variables) {
                    String keyNameEn = (String) variable.get(Dict.NAME_EN);
                    Object keyValue = variable.get(Dict.VALUE);
                    if (modelEnvKeyNameEn.equals(keyNameEn)) {
                        if (CommonUtils.isNullOrEmpty(keyValue)) {
                            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{modelEnvKeyNameEn + "为必填属性！"});
                        }
                        // 验证高级属性
                        if (modelEnvKey.containsKey(Dict.JSON_SCHEMA_ID)) {
                            validateJsonValue(variable, require);
                        }
                        break;
                    }
                }
            } else {
                for (Map<String, Object> variable : variables) {
                    String keyNameEn = (String) variable.get(Dict.NAME_EN);
                    Object keyValue = variable.get(Dict.VALUE);
                    if (modelEnvKeyNameEn.equals(keyNameEn)) {
                        if (!CommonUtils.isNullOrEmpty(keyValue)) {
                            // 验证高级属性
                            if (modelEnvKey.containsKey(Dict.JSON_SCHEMA_ID)) {
                                validateJsonValue(variable, require);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * 校验json schema数据
     *
     * @param variable
     */
    private void validateJsonValue(Map<String, Object> variable, String require) {
        String nameEn = (String) variable.get(Dict.NAME_EN);
        String jsonSchemaId = (String) variable.get(Constants.JSON_SCHEMA_ID);
        JsonSchema jsonSchema = jsonSchemaService.getJsonSchema(jsonSchemaId);
        if (jsonSchema == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"json schema{" + jsonSchemaId + "}不存在！"});
        }
        Object jsonValue = variable.get(Constants.VALUE);
        if (Constants.ZERO.equals(require) && CommonUtils.isNullOrEmpty(jsonValue)) {
            return;
        }
        if (jsonValue instanceof List) {
            jsonValue = JSONArray.fromObject(jsonValue);
            List<Map<String, Object>> valueList = (List<Map<String, Object>>) jsonValue;
            for (int i = 0; i < valueList.size(); i++) {
                Map<String, Object> map1 = valueList.get(i);
                for (int j = i + 1; j < valueList.size(); j++) {
                    Map<String, Object> map2 = valueList.get(j);
                    if (map1.equals(map2)) {
                        throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{nameEn + "的数据不能重复"});
                    }
                }
            }
        }
        if (jsonValue instanceof Map) {
            jsonValue = JSONObject.fromObject(jsonValue);
        }
        ProcessingReport processingReport = CommonUtils.validateJsonSchema(String.valueOf(jsonValue), jsonSchema.getJsonSchema());
        if (!processingReport.isSuccess()) {
            if (logger.isDebugEnabled()) {
                logger.debug("校验高级属性出错，出错的属性是{}", nameEn);
            }
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{nameEn + "高级属性数据格式异常！"});
        }
    }

    public String checkConnectionDocker(Map requestMap) {
        String ip = (String) requestMap.get(Constants.FDEV_CAAS_SERVICE_REGISTRY);
        CommonUtils.checkBlankSpace(Constants.FDEV_CAAS_SERVICE_REGISTRY, ip);
        String user = (String) requestMap.get(Constants.FDEV_CAAS_USER);
        CommonUtils.checkBlankSpace(Constants.FDEV_CAAS_USER, user);
        String password = (String) requestMap.get(Dict.FDEV_CAAS_PD);
        CommonUtils.checkBlankSpace(Dict.FDEV_CAAS_PD, password);
        String modelId = (String) requestMap.get(Constants.MODEL_ID);
        String modelEnvId = (String) requestMap.get(Dict.MODEL_ENV_ID);
        if (StringUtils.isNotBlank(modelEnvId)) {
            Model model = new Model();
            model.setId(modelId);
            Model model1 = iModelDao.queryById(model);
            List<Object> env_key = model1.getEnv_key();
            ModelEnv modelEnv = new ModelEnv();
            modelEnv.setId(modelEnvId);
            ModelEnv modelEnv1 = modelEnvDao.queryId(modelEnv);
            Map<String, Object> variables = modelEnv1.getVariables();
            String fdevPwd = "";
            for (Object obj : env_key) {
                Map map = (Map) obj;
                String nameEn = (String) map.get(Dict.NAME_EN);
                if (Dict.FDEV_CAAS_PD.equals(nameEn)) {
                    String id = (String) map.get(Dict.ID);
                    if (variables.get(id) instanceof String) {
                        fdevPwd = (String) variables.get(id);
                    }
                }
            }
            if (fdevPwd.equals(password)) {
                password = des3Util.decrypt(password);
            }
        }
        if (StringUtils.isEmpty(ip) || StringUtils.isEmpty(user) || StringUtils.isEmpty(password)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{Constants.FDEV_CAAS_SERVICE_REGISTRY + "," + Constants.FDEV_CAAS_USER + ","
                    + Dict.FDEV_CAAS_PD});
        }
        Object runCmd = null;
        try {
            runCmd = CommonUtils.runCmd("docker login " + ip + " -u " + user + " -p " + password);
        } catch (IOException e) {
            throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{e.getMessage()});
        }
        logger.info(runCmd.toString());
        return runCmd.toString();
    }

}
