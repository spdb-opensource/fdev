package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.esotericsoftware.minlog.Log;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.spdb.service.RestTransportService;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

@Service
@RefreshScope
public class RestTransportServiceImpl implements RestTransportService {

    @Autowired
    private RestTransport restTransport;
    @Value("${scan.user.id}")
    private String scanUserId;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Override
    public Map getAppCloneUrlAndProId(String appServiceId) {
        Map urlAndProIdMap = new HashMap<>();
        Map<String, Object> appInfoMap = getAppInfo(appServiceId);
        if (appInfoMap != null) {
            urlAndProIdMap.put(Dict.GIT, (appInfoMap.get(Dict.GIT)));
            urlAndProIdMap.put(Dict.GITLAB_PROJECT_ID, appInfoMap.get(Dict.GITLAB_PROJECT_ID));
        }
        return urlAndProIdMap;
    }

    @Override
    public boolean isAppDevManager(String appServiceId) {
        String userId = getUserId();
        // 调应用模块的接口获取应用详情
        Map<String, Object> appInfoMap = getAppInfo(appServiceId);
        if (StringUtils.isEmpty(userId) || appInfoMap == null) {
            return false;
        }
        if (inWhiteList(userId)) {
            return true;
        }
        List<Map<String, Object>> devManagersList = (List<Map<String, Object>>) appInfoMap.get(Dict.DEV_MANAGERS);
        if (CollectionUtils.isNotEmpty(devManagersList)) {
            for (Map manager : devManagersList) {
                String devManagersId = (String) manager.get(Dict.ID);
                if (userId.equals(devManagersId)) {
                    return true;
                }
            }
        }

        return userVerifyUtil.userRoleIsSPM(getCurrentUser().getRole_id());
    }

    @Override
    public boolean isTaskManager(String taskId) {
        String userId = getUserId();
        List<String> idList = new ArrayList<>();
        // 调任务模块的接口获取任务详情
        Map<String, Object> taskInfoMap = getTaskInfo(taskId);
        if (StringUtils.isEmpty(userId) || taskInfoMap == null) {
            return false;
        }
        if (inWhiteList(userId)) {
            return true;
        }
        // 行内负责人
        List<Map<String, Object>> spdbMasterList = (List<Map<String, Object>>) taskInfoMap.get(Dict.SPDB_MASTER);
        // 任务负责人
        List<Map<String, Object>> masterList = (List<Map<String, Object>>) taskInfoMap.get(Dict.MASTER);
        // 开发人员
        List<Map<String, Object>> developerList = (List<Map<String, Object>>) taskInfoMap.get(Dict.DEVELOPER);
        if (CollectionUtils.isNotEmpty(masterList)) {
            spdbMasterList.addAll(masterList);
        }
        if (CollectionUtils.isNotEmpty(developerList)) {
            spdbMasterList.addAll(developerList);
        }
        if (CollectionUtils.isNotEmpty(spdbMasterList)) {
            for (Map manager : spdbMasterList) {
                idList.add((String) manager.get(Dict.ID));
            }
        }
        for (String id : idList) {
            if (id.equals(userId)) {
                return true;
            }
        }
        return userVerifyUtil.userRoleIsSPM(getCurrentUser().getRole_id());
    }

    @Override
    public List<Map> getAllApp() {
        List<Map> mapList = new ArrayList<>();
        Map reqMap = new HashMap();
        reqMap.put(Dict.REST_CODE, "queryApp");
        try {
            List<Map<String, Object>> appInfoList = (List<Map<String, Object>>) restTransport.submit(reqMap);
            for (Map<String, Object> appMap : appInfoList) {
                Map map = new HashMap<>();
                String appName = (String) appMap.get(Dict.NAME_EN);
                //if (appName.contains("msper-web") || appName.contains("mspmk-web")) {
                map.put(Dict.SERVICE_ID, appName);
                map.put(Dict.TYPE, "09");
                mapList.add(map);
                //}
            }
        } catch (Exception e) {
            Log.info("调用fdev的app模块服务出错-------", e);
        }
        return mapList;
    }


    /**
     * 获取当前登录用户的Id
     *
     * @return
     */
    public String getUserId() {
        String userId = "";
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute("_USER");
        if (user != null) {
            userId = user.getId();
        }
        return userId;
    }

    /**
     * 根据应用英文名，调应用模块的接口拿到应用详情;
     *
     * @param appServiceId
     * @return
     */
    public Map<String, Object> getAppInfo(String appServiceId) {
        List<Map<String, Object>> appInfoList;
        Map<String, String> map = new HashMap<>();
        map.put(Dict.REST_CODE, "queryApp");
        map.put(Dict.NAME_EN, appServiceId);
        Map<String, Object> appMap = new HashMap<>();
        try {
            appInfoList = (List<Map<String, Object>>) restTransport.submit(map);
            if (CollectionUtils.isNotEmpty(appInfoList)) {
                appMap = appInfoList.get(0);
            }
        } catch (Exception e) {
            Log.info("调用fdev的app模块服务出错-------", e);
        }
        return appMap;
    }

    /**
     * 根据任务Id，调任务模块的接口拿到任务详情;
     *
     * @param taskId
     * @return
     */
    public Map<String, Object> getTaskInfo(String taskId) {
        List<Map<String, Object>> taskInfoList;
        Map<String, String> map = new HashMap<>();
        map.put(Dict.REST_CODE, "queryTask");
        map.put(Dict.ID, taskId);
        Map<String, Object> appMap = new HashMap<>();
        try {
            taskInfoList = (List<Map<String, Object>>) restTransport.submit(map);
            if (CollectionUtils.isNotEmpty(taskInfoList)) {
                appMap = taskInfoList.get(0);
            }
        } catch (Exception e) {
            Log.info("调用fdev的task模块服务出错-------", e);
        }
        return appMap;
    }

    private boolean inWhiteList(String userId) {
        String[] scanUserIds = scanUserId.split(",");
        for (String id : scanUserIds) {
            if (id.equals(userId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前登录用户对象
     *
     * @return
     */
    public User getCurrentUser() {
        return (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute("_USER");
    }

    @Override
    public List<Map<String, Object>> getAppByIdsOrGitlabIds(String type, Set<String> ids, Set<Integer> gitlabIds) {
        List<Map<String, Object>> appInfoList;
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.REST_CODE, "getAppByIdsOrGitlabIds");
        map.put(Dict.TYPE, type);
        // 若传过来的id都为空，则直接返回
        if (CollectionUtils.isEmpty(ids) && CollectionUtils.isEmpty(gitlabIds)) {
            return new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(gitlabIds)) {
            map.put(Dict.IDS, ids);
        } else {
            map.put(Dict.IDS, gitlabIds);
        }
        try {
            appInfoList = (List<Map<String, Object>>) restTransport.submit(map);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[]{"调应用模块getAppByIdsOrGitlabIds接口出错：" + e.getMessage()});
        }
        return appInfoList;
    }

    @Override
    public String queryLastTagByGitlabId(Integer gitLabId,String releaseNodeName) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Dict.REST_CODE, "queryLastTagByGitlabId");
        paramMap.put(Dict.RELEASE_NODE_NAME, releaseNodeName);
        paramMap.put(Dict.GITLAB_PROJECT_ID, gitLabId);
        String responseData = null;
        try {
            responseData = (String) restTransport.submit(paramMap);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[]{"调投产模块queryLastTagByGitlabId接口出错：" + e.getMessage()});
        }
        return responseData;
    }

}
