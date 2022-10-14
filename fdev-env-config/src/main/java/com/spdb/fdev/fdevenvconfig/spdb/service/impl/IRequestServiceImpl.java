package com.spdb.fdev.fdevenvconfig.spdb.service.impl;


import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.spdb.service.IRequestService;
import com.spdb.fdev.transport.RestTransport;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author xxx
 * @date 2019/7/29 16:10
 */
@Component
public class IRequestServiceImpl implements IRequestService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTransport restTransport;

    /**
     * 查询应用模块 通过 应用 id查询应用信息
     *
     * @param appId
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject findByAppId(String appId) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.ID, appId);
        param.put(Dict.REST_CODE, "findByAppId");
        Object result = this.restTransport.submit(param);
        if (result instanceof Map) {
            return JSONObject.fromObject(result);
        } else {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"应用不存在id为：" + appId});
        }
    }

    /**
     * 查询应用模块 所有应用的信息
     *
     * @return
     */
    @Override
    public JSONArray queryAllAppInfo() throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.REST_CODE, "queryAllAppInfo");
        Object result = this.restTransport.submit(param);
        if (result instanceof List) {
            return JSONArray.fromObject(result);
        } else {
            throw new FdevException(ErrorConstants.APP_NOT_EXIST, new String[]{"查询应用信息出错: " + result.toString()});
        }
    }

    /**
     * 根据gitlabId查询应用信息
     */
    @Override
    public Map getAppByGitId(Integer gitlabId) throws Exception {
        Map<String, Object> param = new HashedMap<>();
        param.put(Constants.ID, gitlabId);
        param.put(Dict.REST_CODE, "getAppByGitId");
        return (Map) this.restTransport.submit(param);
    }


    @Override
    public List<Map> findGroups() throws Exception {
        HashMap<Object, Object> map = new HashMap<>();
        map.put(Dict.REST_CODE, "findGroups");
        return (List<Map>) this.restTransport.submit(map);
    }

    @Override
    public Map createGitlabProject(String path, String projectName) throws Exception {
        HashMap<Object, Object> map = new HashMap<>();
        map.put(Dict.REST_CODE, "createProject");
        map.put(Dict.PATH, path);
        map.put(Dict.PROJECT_NAME, projectName);
        return (Map) this.restTransport.submit(map);
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
            throw new FdevException(ErrorConstants.APP_NOT_EXIST, new String[]{"调用fdev的app模块getAppByIdsOrGitlabIds接口出错：" + e.getMessage()});
        }
        return appInfoList;
    }

    @Override
    public List<Map<String, Object>> queryUser(String roleName) throws Exception {
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put(Dict.NAME, roleName);
        roleMap.put(Dict.REST_CODE, "queryRole");
        List<Map<String, Object>> roleList = (List<Map<String, Object>>) this.restTransport.submit(roleMap);
        String roleId = "";
        if (CollectionUtils.isNotEmpty(roleList)) {
            Map<String, Object> objectMap = roleList.get(0);
            roleId = (String) objectMap.get(Dict.ID);
        }
        List<String> roleIds = new ArrayList<>();
        if (StringUtils.isNotEmpty(roleId)) {
            roleIds.add(roleId);
        }
        HashMap<Object, Object> map = new HashMap<>();
        map.put(Dict.ROLE_ID, roleIds);
        map.put(Dict.REST_CODE, "queryUser");
        return (List<Map<String, Object>>) this.restTransport.submit(map);
    }

    @Override
    public String queryBaseImageVersion(Map<String, String> requestMap) {
        Map<String, Object> responseMap = null;
        requestMap.put(Dict.REST_CODE, "queryBaseImageVersion");
        try {
            responseMap = (Map<String, Object>) this.restTransport.submit(requestMap);
        } catch (Exception e) {
            logger.error("请求组件模块queryBaseImageVersion接口出错:{}", e.getMessage());
        }
        if (responseMap != null) {
            return (String) responseMap.get(Dict.FDEV_CAAS_BASE_IMAGE_VERSION);
        } else {
            return "";
        }
    }

    @Override
    public Map queryUserById(Map param) throws Exception {
        if (CommonUtils.isNullOrEmpty(param)) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"用户id"});
        }
        param.put(Dict.REST_CODE, "queryUser");
        Object result = restTransport.submit(param);
        if (CommonUtils.isNullOrEmpty(result)) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"TASK->USER 查询用户返回为空"});
        }
        ArrayList jsonArray = (ArrayList) result;
        return (Map) jsonArray.get(0);
    }

    @Override
    public List<Map<String, Object>> getApps(String type, Set<String> ids) {
        List<Map<String, Object>> appInfoList = new ArrayList<>();
        if (CollectionUtils.isEmpty(ids)) {
            return appInfoList;
        }
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.REST_CODE, "getAppByIdsOrGitlabIds");
        map.put(Dict.TYPE, type);
        map.put(Dict.IDS, ids);
        try {
            appInfoList = (List<Map<String, Object>>) restTransport.submit(map);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.APP_NOT_EXIST, new String[]{"调用fdev的app模块getAppByIdsOrGitlabIds接口出错：" + e.getMessage()});
        }
        return appInfoList;
    }

    @Override
    public Map updateApp(Map map) throws Exception {
        Map param = new HashMap();
        param.put(Dict.ID, map.get(Dict.ID));
        param.put(Dict.SIT, map.get(Dict.SIT));
        param.put(Dict.NETWORK, map.get(Dict.NETWORK));
        param.put(Dict.CAASSTATUS, map.get(Dict.CAASSTATUS));
        param.put(Dict.SCCSTATUS, map.get(Dict.SCCSTATUS));
        param.put(Dict.REST_CODE, "updateApp");
        Map app = (Map) restTransport.submit(param);
        return app;
    }

    @Override
    public List<Map<String, Object>> queryApps() {
        List<Map<String, Object>> appInfoList;
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.REST_CODE, "queryApps");
        try {
            appInfoList = (List<Map<String, Object>>) restTransport.submit(map);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.INVAILD_OPERATION_DATA, new String[]{"调用fdev的app模块queryApps接口出错：" + e.getMessage()});
        }
        return appInfoList;
    }

    @Override
    public Map<String, Map> getUsersByIdsOrUserNameEn(Map<String, List<String>> param) throws Exception {
        Map<String, Object> data = new HashMap<>(param);
        data.put(Dict.REST_CODE, "queryByUserCoreData");
        Object userObj = this.restTransport.submit(data);
        return (Map<String, Map>) userObj;
    }

    @Override
    public Object getProjectById(Map<String, String> param) throws Exception {
        Map<String, String> data = new HashMap<>(param);
        data.put(Dict.REST_CODE, "getProjectById");
        Object userObj = this.restTransport.submit(data);
        return userObj;
    }
}
