package com.spdb.fdev.fdevenvconfig.spdb.event.listener;

import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IEnvironmentDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Environment;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelEnv;
import com.spdb.fdev.fdevenvconfig.spdb.event.DeleteModelEvent;
import com.spdb.fdev.fdevenvconfig.spdb.event.DeleteModelFieldMappingEvent;
import com.spdb.fdev.fdevenvconfig.spdb.event.UpdateModelEvent;
import com.spdb.fdev.fdevenvconfig.spdb.event.UpdateModelFieldMappingEvent;
import com.spdb.fdev.fdevenvconfig.spdb.event.core.EmailListenerCore;
import com.spdb.fdev.fdevenvconfig.spdb.service.HistoryDetailsService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IRequestService;
import com.spdb.fdev.fdevenvconfig.spdb.service.ISendEmailService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * create by Idea
 *
 * @Author aric
 * @Date 2020/1/6 13:48
 * @Version 1.0
 */
@Component
public class EmailListener {

    private Logger logger = LoggerFactory.getLogger(EmailListener.class);

    @Autowired
    private IModelDao modelDao;
    @Autowired
    private IEnvironmentDao environmentDao;
    @Autowired
    private ISendEmailService iSendEmailService;
    @Autowired
    private EmailListenerCore emailListenerCore;
    @Autowired
    private IRequestService requestService;

    /**
     * 监听更新实体属性事件
     *
     * @param event
     */
    @EventListener(classes = {UpdateModelEvent.class})
    public void onApplicationEvent(UpdateModelEvent event) throws Exception {
        Model before = event.getBefore();
        Model after = event.getAfter();
        String modelNameEn = before.getName_en();
        String modelNameCn = before.getName_cn();
        List<Object> beforeEnvKeys = before.getEnv_key();
        List<Object> afterEnvKeys = after.getEnv_key();
        List<Map<String, Object>> beforeEnvKeyList = emailListenerCore.objectListToMapList(beforeEnvKeys);
        List<Map<String, Object>> afterEnvKeyList = emailListenerCore.objectListToMapList(afterEnvKeys);
        // 获取删除实体的属性和新增的实体属性
        Map<String, List<Map<String, Object>>> updateModel = emailListenerCore.getUpdateModel(beforeEnvKeyList, afterEnvKeyList);
        List<Map<String, Object>> addKeyList = updateModel.get(Dict.ADD);
        List<Map<String, Object>> removedKeyList = updateModel.get(Dict.DELETE);
        if (CollectionUtils.isEmpty(removedKeyList)) {
            // 如果实体只新增了属性，没有删除属性，记录新增属性的流水后，直接返回
            if (CollectionUtils.isNotEmpty(addKeyList)) {
                emailListenerCore.saveHistoryDetails(modelNameEn, addKeyList, before, after, HistoryDetailsService.MODEL_UPDATE, event.getUserId(), "", "", "");
            }
            return;
        }
        // 存删除的实体属性和新增的实体属性
        List<Map<String, Object>> recordKeyList = new ArrayList<>();
        recordKeyList.addAll(removedKeyList);
        recordKeyList.addAll(addKeyList);
        /**
         * 异步记录流水
         */
        emailListenerCore.saveHistoryDetails(modelNameEn, recordKeyList, before, after, HistoryDetailsService.MODEL_UPDATE, event.getUserId(), "", Dict.SUCCES, "");
        // 向 app-config-map 中查询数据,获取应用相关的信息
        Map<Integer, Collection<Object>> appIdAndBranchAndRemoveKeys = emailListenerCore.getAppIdAndBranchAndKeys(modelNameEn, removedKeyList);
        if (appIdAndBranchAndRemoveKeys == null || appIdAndBranchAndRemoveKeys.size() == 0) {
            return;
        }
        // 封装邮件中应用的信息
        Set<String> emailTo = new HashSet<>();
        List<Map<String, Object>> emailAppContent = emailListenerCore.getEmailAppContent(appIdAndBranchAndRemoveKeys, emailTo);
        // 发送所有环境配置管理员，公共实体发送固定人员
        emailListenerCore.addEmail(emailTo, modelNameEn);
        // 封装邮件信息
        Map<String, Object> emailMap = new HashMap<>();
        emailMap.put(Dict.SUBJECT, Constants.EMAIL_SUBJECT_MODEL_UPDATE);
        emailMap.put(Dict.EMAIL, emailTo);
        emailMap.put(Dict.MODEL_NAME_EN, modelNameEn);
        emailMap.put(Dict.MODEL_NAME_CN, modelNameCn);
        emailMap.put(Dict.DELETE, removedKeyList);
        emailMap.put(Dict.VERSION, after.getVersion());
        emailMap.put(Dict.APP, emailAppContent);
        // 发送邮件
        iSendEmailService.sendSubjectModelUpdateEmail(emailMap);
    }

    /**
     * 监听删除整个实体事件
     *
     * @param event
     */
    @EventListener(classes = {DeleteModelEvent.class})
    public void onApplicationEvent(DeleteModelEvent event) throws Exception {
        Model before = event.getBefore();
        String modelNameEn = before.getName_en();
        String modelNameCn = before.getName_cn();
        // beforeEnvKeys即为删除的实体属性
        List<Object> beforeEnvKeys = before.getEnv_key();
        List<Map<String, Object>> mapList = emailListenerCore.objectListToMapList(beforeEnvKeys);
        /**
         * 异步记录流水
         */
        emailListenerCore.saveHistoryDetails(modelNameEn, mapList, before, event.getAfter(), HistoryDetailsService.MODEL_DELETE, event.getUserId(), "", Dict.SUCCES, "");
        // 向 app-config-map 中查询数据,获取应用相关的信息
        Map<Integer, Collection<Object>> appIdAndBranchAndRemoveKeys = emailListenerCore.getAppIdAndBranchAndKeys(modelNameEn, mapList);
        if (appIdAndBranchAndRemoveKeys == null || appIdAndBranchAndRemoveKeys.size() == 0) {
            return;
        }
        // 封装邮件中应用的信息
        Set<String> emailTo = new HashSet<>();
        List<Map<String, Object>> emailAppContent = emailListenerCore.getEmailAppContent(appIdAndBranchAndRemoveKeys, emailTo);
        // 发送所有环境配置管理员，公共实体发送固定人员
        emailListenerCore.addEmail(emailTo, modelNameEn);
        // 封装邮件信息
        Map<String, Object> emailMap = new HashMap<>();
        emailMap.put(Dict.SUBJECT, Constants.EMAIL_SUBJECT_MODEL_DELETE);
        emailMap.put(Dict.EMAIL, emailTo);
        emailMap.put(Dict.MODEL_NAME_EN, modelNameEn);
        emailMap.put(Dict.MODEL_NAME_CN, modelNameCn);
        emailMap.put(Dict.DELETE, beforeEnvKeys);
        emailMap.put(Dict.VERSION, before.getVersion());
        emailMap.put(Dict.APP, emailAppContent);
        // 发送邮件
        iSendEmailService.sendSubjectModelDeleteEmail(emailMap);
    }

    /**
     * 监听更新实体映射值事件
     *
     * @param event
     * @throws Exception
     */
    @EventListener(classes = {UpdateModelFieldMappingEvent.class})
    public void onApplicationEvent(UpdateModelFieldMappingEvent event) throws Exception {
        ModelEnv before = event.getBefore();
        Map<String, Object> beforeVariables = before.getVariables();
        Map<String, Object> afterVariables = event.getAfter().getVariables();
        if (beforeVariables.equals(afterVariables)) {
            return;
        }
        // 实体英文名
        String modelNameEn = event.getModelNameEn();
        String modelNameCn = event.getModelNameCn();
        // 环境英文名
        String envNameEn = event.getEnvNameEn();
        String envNameCn = event.getEnvNameCn();
        // 查找更新的实体属性映射值
        List<Map<String, Object>> keyMapList = emailListenerCore.getUpdateMapping(event.getEnvKeyList(), beforeVariables, afterVariables);
        /**
         * 异步记录流水
         */
        emailListenerCore.saveHistoryDetails(modelNameEn, keyMapList, before, event.getAfter(), HistoryDetailsService.MAPPING_UPDATE, event.getUserId(), envNameEn, Dict.SUCCES, event.getApplyId());
        // 向 app-config-map 中查询数据,获取应用相关的信息
        Map<Integer, Collection<Object>> appIdAndBranchAndUpdateKeys = emailListenerCore.getAppIdAndBranchAndKeys(modelNameEn, keyMapList);
        if (appIdAndBranchAndUpdateKeys == null || appIdAndBranchAndUpdateKeys.size() == 0) {
            return;
        }
        // 封装邮件中应用的信息
        Set<String> emailTo = new HashSet<>();
        List<Map<String, Object>> emailAppContent = emailListenerCore.getEmailAppContent(appIdAndBranchAndUpdateKeys, emailTo);
        // 发送所有环境配置管理员，公共实体发送固定人员
        emailListenerCore.addEmail(emailTo, modelNameEn);
        // 封装邮件信息
        Map<String, Object> emailMap = new HashMap<>();
        emailMap.put(Dict.SUBJECT, Constants.EMAIL_SUBJECT_MODEL_ENV_UPDATE);
        emailMap.put(Dict.EMAIL, emailTo);
        emailMap.put(Dict.MODEL_NAME_EN, modelNameEn);
        emailMap.put(Dict.MODEL_NAME_CN, modelNameCn);
        emailMap.put(Dict.ENV_NAME_EN, envNameEn);
        emailMap.put(Dict.ENV_NAME_CN, envNameCn);
        emailMap.put(Dict.UPDATE, keyMapList);
        emailMap.put(Dict.VERSION, event.getAfter().getVersion());
        emailMap.put(Dict.APP, emailAppContent);
        emailMap.put(Dict.MODIFY_REASON, event.getModifyReason());
        emailMap.put(Dict.APPLY_USERNAME, event.getApply_username());
        // 发送邮件
        Environment environment = environmentDao.queryByNameEn(envNameEn);
        if (environment.getLabels().contains(Constants.PRO) || environment.getLabels().contains(Constants.TCYZ)) {
            iSendEmailService.sendSubjectModelEnvUpdateEmail(emailMap);
        }
    }

    /**
     * 监听删除整个实体与环境映射事件
     *
     * @param event
     * @throws Exception
     */
    @EventListener(classes = {DeleteModelFieldMappingEvent.class})
    public void onApplicationEvent(DeleteModelFieldMappingEvent event) throws Exception {
        ModelEnv before = event.getBefore();
        // 通过实体id查找实体
        Model modelParam = new Model();
        modelParam.setId(before.getModel_id());
        Model model = modelDao.queryById(modelParam);
        // 通过环境id查找环境
        Environment environmentParam = new Environment();
        environmentParam.setId(before.getEnv_id());
        Environment environment = environmentDao.queryById(environmentParam);
        // 实体英文名
        String modelNameEn = model.getName_en();
        String modelNameCn = model.getName_cn();
        // 环境英文名
        String envNameEn = environment.getName_en();
        String envNameCn = environment.getName_cn();
        // 查找删除的实体属性映射值
        List<Map<String, Object>> keyMapList = emailListenerCore.getDeleteMapping(before, model);
        /**
         * 异步记录流水
         */
        emailListenerCore.saveHistoryDetails(modelNameEn, keyMapList, before, event.getAfter(), HistoryDetailsService.MAPPING_DELETE, event.getUserId(), envNameEn, Dict.SUCCES, "");
        // 向 app-config-map 中查询数据,获取应用相关的信息
        Map<Integer, Collection<Object>> appIdAndBranchAndUpdateKeys = emailListenerCore.getAppIdAndBranchAndKeys(modelNameEn, keyMapList);
        if (appIdAndBranchAndUpdateKeys == null || appIdAndBranchAndUpdateKeys.size() == 0) {
            return;
        }
        // 封装邮件中应用的信息
        Set<String> emailTo = new HashSet<>();
        List<Map<String, Object>> emailAppContent = emailListenerCore.getEmailAppContent(appIdAndBranchAndUpdateKeys, emailTo);
        // 发送所有环境配置管理员，公共实体发送固定人员
        emailListenerCore.addEmail(emailTo, modelNameEn);
        // 封装邮件信息
        Map<String, Object> emailMap = new HashMap<>();
        emailMap.put(Dict.SUBJECT, Constants.EMAIL_SUBJECT_MODEL_ENV_DELETE);
        emailMap.put(Dict.EMAIL, emailTo);
        emailMap.put(Dict.MODEL_NAME_EN, modelNameEn);
        emailMap.put(Dict.MODEL_NAME_CN, modelNameCn);
        emailMap.put(Dict.ENV_NAME_EN, envNameEn);
        emailMap.put(Dict.ENV_NAME_CN, envNameCn);
        emailMap.put(Dict.UPDATE, keyMapList);
        emailMap.put(Dict.VERSION, before.getVersion());
        emailMap.put(Dict.APP, emailAppContent);
        // 发送邮件
        iSendEmailService.sendSubjectModelEnvDeleteEmail(emailMap);
    }
}
