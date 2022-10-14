package com.spdb.fdev.fdevenvconfig.spdb.event.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.cache.IConfigFileCache;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppConfigMapping;
import com.spdb.fdev.fdevenvconfig.spdb.entity.HistoryDetails;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelEnv;
import com.spdb.fdev.fdevenvconfig.spdb.service.HistoryDetailsService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IAppConfigMappingService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IMailService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * create by Idea
 *
 * @Author aric
 * @Date 2020/1/19 09:55
 * @Version 1.0
 */
@Component
@RefreshScope
public class EmailListenerCore {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${record.switch}")
    private Boolean recordSwitch;
    @Value("${fixedEmailPerson}")
    private String fixedEmailPerson;
    @Autowired
    private IAppConfigMappingService appConfigMappingService;
    @Autowired
    private IRequestService requestService;
    @Autowired
    private IMailService mailService;
    @Autowired
    private IConfigFileCache configFileCache;
    @Autowired
    private HistoryDetailsService historyDetailsService;

    /**
     * 根据删除的实体属性，获取用到该实体属性的应用及分支，并以应用的维度组装数据
     *
     * @param modelNameEn
     * @param updateKeys
     * @throws Exception
     */
    public Map<Integer, Collection<Object>> getAppIdAndBranchAndKeys(String modelNameEn, List<Map<String, Object>> updateKeys) {
        List<Map<String, Object>> appAndRemovedKeyList = new ArrayList<>();
        for (Map<String, Object> updateKeyMap : updateKeys) {
            String envKey = (String) updateKeyMap.get(Dict.NAME_EN);
            // 根据实体英文名及实体属性英文名，获得应用Gitlab id及分支信息
            List<AppConfigMapping> appConfigMapping = appConfigMappingService.getAppIdAndBranch(modelNameEn, envKey);
            for (AppConfigMapping configMapping : appConfigMapping) {
                Map<String, Object> appAndRemovedKeyMap = new HashMap<>();
                appAndRemovedKeyMap.put(Dict.GITLAB_PROJECT_ID, configMapping.getAppId());
                appAndRemovedKeyMap.put(Dict.BRANCH, configMapping.getBranch());
                appAndRemovedKeyMap.put(Dict.ENV_KEY, updateKeyMap);
                appAndRemovedKeyList.add(appAndRemovedKeyMap);
            }
        }
        Multimap<String, Object> groupByAppIdAndBranch = ArrayListMultimap.create();
        for (Map<String, Object> appAndRemovedKeyMap : appAndRemovedKeyList) {
            Integer appId = (Integer) appAndRemovedKeyMap.get(Dict.GITLAB_PROJECT_ID);
            String branch = (String) appAndRemovedKeyMap.get(Dict.BRANCH);
            Object envKey = appAndRemovedKeyMap.get(Dict.ENV_KEY);
            groupByAppIdAndBranch.put(appId + "|" + branch, envKey);
        }
        Multimap<Integer, Object> groupByAppId = ArrayListMultimap.create();
        Map<String, Collection<Object>> groupByAppIdAndBranchMap = groupByAppIdAndBranch.asMap();
        for (Map.Entry<String, Collection<Object>> map : groupByAppIdAndBranchMap.entrySet()) {
            String[] appIdAndBranch = map.getKey().split("\\|", 2);
            Map<String, Object> branchAndEnvKeys = new HashMap<>();
            branchAndEnvKeys.put(appIdAndBranch[1], map.getValue());
            groupByAppId.put(Integer.valueOf(appIdAndBranch[0]), branchAndEnvKeys);
        }
        return groupByAppId.asMap();
    }

    /**
     * 封装邮件中应用的信息
     *
     * @param appIdAndBranchAndRemoveKeys
     * @throws Exception
     */
    public List<Map<String, Object>> getEmailAppContent(Map<Integer, Collection<Object>> appIdAndBranchAndRemoveKeys, Set<String> emailTo) throws Exception {
        List<Map<String, Object>> emailMapList = new ArrayList<>();
        Set<Integer> gitlabIds = appIdAndBranchAndRemoveKeys.keySet();
        List<Map<String, Object>> appInfo = requestService.getAppByIdsOrGitlabIds(Dict.GITLAB_PROJECT_ID, new HashSet<>(), gitlabIds);
        for (Map<String, Object> appMap : appInfo) {
            String nameEn = (String) appMap.get(Dict.NAME_EN);
            String nameZh = (String) appMap.get(Dict.NAME_ZH);
            Integer appId = (Integer) appMap.get(Dict.GITLAB_PROJECT_ID);
            // 行内负责人
            List<Map> spdb_managers = (List<Map>) appMap.get(Dict.SPDB_MANAGERS);
            // 应用负责人
            List<Map> dev_managers = (List<Map>) appMap.get(Dict.DEV_MANAGERS);
            // 获取项目中应用负责、行内负责人的邮箱和中文名
            Map<String, Object> userMap = mailService.valdateKeys(dev_managers, spdb_managers);
            Map<String, Map> params = configFileCache.getUsersByIdsOrUserNameEn(
                    (String) userMap.get(Dict.KEYS), (Map<String, List<String>>) userMap.get(Dict.DATA));
            Set<String> email = new HashSet<>();
            StringBuilder userNameCn = new StringBuilder();
            params.forEach((k, value) -> {
                userNameCn.append((String) value.get(Dict.USER_NAME_CN)).append(" ");
                email.add((String) value.get(Dict.EMAIL));
            });
            emailTo.addAll(email);
            List<Object> branchsAndEnvKeys = (List<Object>) appIdAndBranchAndRemoveKeys.get(appId);
            HashMap<String, Object> emailMap = new HashMap<>();
            // 应用英文名
            emailMap.put(Dict.NAME_EN, nameEn);
            // 应用中文名
            emailMap.put(Dict.NAME_ZH, nameZh);
            // 行内应用负责人及应用负责人中文名
            emailMap.put(Dict.USER_NAME_CN, userNameCn.toString());
            // 涉及到的分支及实体属性
            emailMap.put(Dict.BRANCH, branchsAndEnvKeys);
            emailMapList.add(emailMap);
        }
        return emailMapList;
    }

    /**
     * 更新实体时，获取删除实体的属性和新增的实体属性
     *
     * @param beforeEnvKeyList
     * @param afterEnvKeyList
     * @return
     */
    public Map<String, List<Map<String, Object>>> getUpdateModel(List<Map<String, Object>> beforeEnvKeyList, List<Map<String, Object>> afterEnvKeyList) {
        Map<String, List<Map<String, Object>>> updateModelMap = new HashMap<>();
        // 删除两个list中实体属性id相同的，则beforeEnvKeyList剩下来的便是删除的，afterEnvKeyList剩下来的便是新增的
        Iterator<Map<String, Object>> beforeIterator = beforeEnvKeyList.iterator();
        while (beforeIterator.hasNext()) {
            Map<String, Object> beforeEnvKeyMap = beforeIterator.next();
            Iterator<Map<String, Object>> afterIterator = afterEnvKeyList.iterator();
            while (afterIterator.hasNext()) {
                Map<String, Object> afterEnvKeyMap = afterIterator.next();
                if (beforeEnvKeyMap.get(Dict.ID).equals(afterEnvKeyMap.get(Dict.ID))) {
                    beforeIterator.remove();
                    afterIterator.remove();
                }
            }
        }
        updateModelMap.put(Dict.DELETE, beforeEnvKeyList);
        updateModelMap.put(Dict.ADD, afterEnvKeyList);
        return updateModelMap;
    }

    /**
     * 删除实体与环境映射时，获取该实体的属性映射值
     *
     * @param before
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> getDeleteMapping(ModelEnv before, Model model) {
        Map<String, Object> beforeVariables = before.getVariables();
        List<Map<String, Object>> keyMapList = new ArrayList<>();
        for (Map.Entry<String, Object> beforeEntry : beforeVariables.entrySet()) {
            Map<String, Object> keyMap = new HashMap<>();
            for (Object envKey : model.getEnv_key()) {
                Map<String, Object> envKeyMap = (Map<String, Object>) envKey;
                if (envKeyMap.get(Dict.ID).equals(beforeEntry.getKey())) {
                    keyMap.put(Dict.NAME_EN, envKeyMap.get(Dict.NAME_EN));
                    keyMap.put(Dict.NAME_CN, envKeyMap.get(Dict.NAME_CN));
                    keyMap.put(Dict.REQUIRE, envKeyMap.get(Dict.REQUIRE));
                    keyMap.put(Dict.DESC, envKeyMap.get(Dict.DESC));
                    keyMap.put(Dict.ID, envKeyMap.get(Dict.ID));
                    keyMap.put(Dict.VALUE, beforeEntry.getValue());
                    keyMapList.add(keyMap);
                    break;
                }
            }
        }
        return keyMapList;
    }

    /**
     * 更新实体与环境映射时，对比更新前后实体属性映射值的差异
     *
     * @param envKeyList      实体属性
     * @param beforeVariables 更新前的实体属性映射值
     * @param afterVariables  更新后的实体属性映射值
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> getUpdateMapping(List<Map<String, Object>> envKeyList, Map<String, Object> beforeVariables, Map<String, Object> afterVariables) {
        List<Map<String, Object>> keyMapList = new ArrayList<>();
        for (Map.Entry<String, Object> beforeEntry : beforeVariables.entrySet()) {
            for (Map.Entry<String, Object> afterEntry : afterVariables.entrySet()) {
                if (beforeEntry.getKey().equals(afterEntry.getKey())) {
                    if (!beforeEntry.getValue().equals(afterEntry.getValue())) {
                        Map<String, Object> keyMap = new HashMap<>();
                        for (Map<String, Object> envKeyMap : envKeyList) {
                            if (envKeyMap.get(Dict.ID).equals(beforeEntry.getKey())) {
                                keyMap.put(Dict.NAME_EN, envKeyMap.get(Dict.NAME_EN));
                                keyMap.put(Dict.NAME_CN, envKeyMap.get(Dict.NAME_CN));
                                keyMap.put(Dict.REQUIRE, envKeyMap.get(Dict.REQUIRE));
                                keyMap.put(Dict.DESC, envKeyMap.get(Dict.DESC));
                                keyMap.put(Dict.ID, envKeyMap.get(Dict.ID));
                                keyMap.put(Dict.OLD_VALUE, String.valueOf(beforeEntry.getValue()));
                                keyMap.put(Dict.NEW_VALUE, String.valueOf(afterEntry.getValue()));
                                keyMapList.add(keyMap);
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
        return keyMapList;
    }

    /**
     * 将List<Object>转成List<Map<String, Object>>
     *
     * @param objectList
     * @return
     */
    public List<Map<String, Object>> objectListToMapList(List<Object> objectList) {
        List<Map<String, Object>> removedKeyMapList = new ArrayList<>();
        for (Object removedKey : objectList) {
            Map<String, Object> removedKeyMap = (Map<String, Object>) removedKey;
            removedKeyMapList.add(removedKeyMap);
        }
        return removedKeyMapList;
    }

    /**
     * 异步记录流水
     *
     * @param modelName
     * @param fields
     * @param before
     * @param after
     * @param type
     */
    @Async
    public void saveHistoryDetails(String modelName, List<Map<String, Object>> fields, Object before, Object after,
                                   String type, String userId, String env, String status, String applyId) {
        HistoryDetails historyDetails = new HistoryDetails();
        historyDetails.setModelName(modelName);
        historyDetails.setFields(fields);
        historyDetails.setBefore(before);
        historyDetails.setAfter(after);
        historyDetails.setStatus(status);
        historyDetails.setType(type);
        historyDetails.setOpno(userId);
        historyDetails.setEnv(env);
        historyDetails.setApplyId(applyId);
        historyDetailsService.add(historyDetails);
    }

    /**
     * 发送所有环境配置管理员，公共实体发送固定人员
     *
     * @param emailTo
     * @param modelNameEn
     */
    public void addEmail(Set<String> emailTo, String modelNameEn) {
        //公共实体添加固定邮件通知人
        if (!modelNameEn.startsWith(Dict.PRIVATE)) {
            //环境管理员
            List<Map<String, Object>> maps = null;
            try {
                maps = requestService.queryUser(Dict.ROLE_ENV_ADMIN);
            } catch (Exception e) {
                logger.error("调用户模块获得所有环境配置管理员出错：{}", e.getMessage());
            }
            for (Map<String, Object> map : maps) {
                emailTo.add((String) map.get(Dict.EMAIL));
            }
            String[] split = fixedEmailPerson.split(",");
            emailTo.addAll(Arrays.asList(split));
        }
    }
}
