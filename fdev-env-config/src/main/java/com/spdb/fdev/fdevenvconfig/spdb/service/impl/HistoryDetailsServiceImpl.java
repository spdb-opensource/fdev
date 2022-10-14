package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IHistoryDetailsDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.HistoryDetails;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelEnv;
import com.spdb.fdev.fdevenvconfig.spdb.service.HistoryDetailsService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IModelService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IRequestService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * create by Idea
 *
 * @Author aric
 * @Date 2020/1/10 10:19
 * @Version 1.0
 */
@Service
public class HistoryDetailsServiceImpl implements HistoryDetailsService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IHistoryDetailsDao historyDetailsDao;
    @Autowired
    private IModelService modelService;
    @Autowired
    private IRequestService requestService;

    @Override
    public HistoryDetails add(HistoryDetails historyDetails) {
        return historyDetailsDao.add(historyDetails);
    }

    @Override
    public Map<String, Object> getMappingHistoryList(Map<String, Object> requestMap) {
        String modelId = (String) requestMap.get(Constants.MODEL_ID);
        String envId = (String) requestMap.get(Constants.ENV_ID);
        Integer page = (Integer) requestMap.get(Dict.PAGE);
        Integer perPage = (Integer) requestMap.get(Dict.PER_PAGE);
        Map<String, Object> historyMap = historyDetailsDao.getHistoryList(modelId, envId, HistoryDetailsService.MAPPING_UPDATE, page, perPage);
        List<HistoryDetails> historyList = (List<HistoryDetails>) historyMap.get(Dict.LIST);
        // 组装用户中文名
        List<String> userIdList = new ArrayList<>();
        for (HistoryDetails historyDetails : historyList) {
            String opno = historyDetails.getOpno();
            if (StringUtils.isNotEmpty(opno)) {
                userIdList.add(opno);
            }
        }
        if (CollectionUtils.isEmpty(userIdList)) {
            return historyMap;
        }
        Map<String, Map> users;
        Map<String, List<String>> paramMap = new HashMap<>();
        paramMap.put(Dict.IDS, userIdList);
        try {
            users = requestService.getUsersByIdsOrUserNameEn(paramMap);
        } catch (Exception e) {
            logger.info("调用户模块接口出错:{}", e.getMessage());
            throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[]{"调用户模块接口出错！"});
        }
        for (HistoryDetails historyDetails : historyList) {
            Map<String, Object> userMap = users.get(historyDetails.getOpno());
            if (MapUtils.isNotEmpty(userMap))
                historyDetails.setUsername((String) userMap.get(Dict.USER_NAME_CN));
        }
        return historyMap;
    }

    @Override
    public List<Map<String, Object>> getMappingHistoryDetail(Map<String, Object> requestMap) {
        String id = (String) requestMap.get(Dict.ID);
        HistoryDetails historyDetails = historyDetailsDao.getHistory(id);
        ModelEnv before = (ModelEnv) historyDetails.getBefore();
        ModelEnv after = (ModelEnv) historyDetails.getAfter();
        // 获取实体
        Model model = modelService.queryById(before.getModel_id());
        List<Object> beforeModelEnvKeyList = getModelEnvKeyList(model.getEnv_key(), before.getVariables());
        List<Object> afterModelEnvKeyList = getModelEnvKeyList(model.getEnv_key(), after.getVariables());
        // 对比，获取差异
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Object modelEnvKey : beforeModelEnvKeyList) {
            Map<String, Object> beforeModelEnvKeyMap = (Map<String, Object>) modelEnvKey;
            for (Object afterModelEnvKey : afterModelEnvKeyList) {
                Map<String, Object> afterModelEnvKeyMap = (Map<String, Object>) afterModelEnvKey;
                if (beforeModelEnvKeyMap.get(Dict.ID).equals(afterModelEnvKeyMap.get(Dict.ID))) {
                    afterModelEnvKeyMap.put(Dict.OLD_VALUE, beforeModelEnvKeyMap.get(Dict.VALUE));
                    afterModelEnvKeyMap.put(Dict.NEW_VALUE, afterModelEnvKeyMap.get(Dict.VALUE));
                    responseList.add(afterModelEnvKeyMap);
                }
            }
        }
        return responseList;
    }

    /**
     * 组装实体环境映射值
     *
     * @param envKey
     * @param variables
     * @return
     */
    private List<Object> getModelEnvKeyList(List<Object> envKey, Map<String, Object> variables) {
        List<Object> responseList = new ArrayList<>();
        for (Object envKeyObj : envKey) {
            Map<String, Object> envKeyMap = (Map<String, Object>) envKeyObj;
            Map<String, Object> responseMap = new HashMap<>();
            String keyId = (String) envKeyMap.get(Constants.ID);
            Object value = variables.get(keyId);
            responseMap.putAll(envKeyMap);
            responseMap.put(Constants.VALUE, value);
            responseList.add(responseMap);
        }
        return responseList;
    }

}
