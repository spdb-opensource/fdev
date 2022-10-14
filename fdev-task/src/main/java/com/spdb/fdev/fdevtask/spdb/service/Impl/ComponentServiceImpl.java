package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Constants;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.spdb.service.IComponentService;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ComponentServiceImpl implements IComponentService {
    @Autowired
    private RestTransport restTransport;

    @Override
    public Map<String, Object> queryComponentWebDetail(String componentId) throws Exception {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.ID, componentId);
        param.put(Dict.REST_CODE, Constants.QUERYMPASSCOMPONENTDETAIL);
        try {
            return (Map<String, Object>) restTransport.submit(param);
        }catch (Exception e) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[] {"查询组件信息异常"});
        }

    }

    @Override
    public Map<String, Object> queryComponentServerDetail(String componentId) throws Exception {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.ID, componentId);
        param.put(Dict.REST_CODE, Constants.QUERYCOMPONENTDETAIL);
        try {
            return (Map<String, Object>) restTransport.submit(param);
        }catch (Exception e) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[] {"查询组件信息异常"});
        }
    }

    @Override
    public Map<String, Object> queryArchetypeWebDetail(String archetypeId) throws Exception {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.ID, archetypeId);
        param.put(Dict.REST_CODE, Constants.QUERYMPASSARCHETYPEDETAIL);
        try {
            return (Map<String, Object>) restTransport.submit(param);
        }catch (Exception e) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[] {"查询骨架信息异常"});
        }

    }

    @Override
    public Map<String, Object> queryArchetypeServerDetail(String archetypeId) throws Exception {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.ID, archetypeId);
        param.put(Dict.REST_CODE, Constants.QUERYARCHETYPEDETAIL);
        try {
            return (Map<String, Object>) restTransport.submit(param);
        }catch (Exception e) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[] {"查询骨架信息异常"});
        }
    }

    @Override
    public Map<String, Object> queryImageDetail(String imageId) throws Exception {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.ID, imageId);
        param.put(Dict.REST_CODE, Constants.QUERYBASEIMAGEDETAIL);
        try {
            return (Map<String, Object>) restTransport.submit(param);
        }catch (Exception e) {
            throw new FdevException(ErrorConstants.TASK_ERROR, new String[] {"查询镜像信息异常"});
        }
    }

    @Override
    public void savePredictVersion(String componentId, String targetVersion, String type) throws Exception {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.COMPONENT_ID, componentId);
        param.put(Dict.TARGET_VERSION, targetVersion);
        param.put(Dict.TYPE, type);
        param.put(Dict.REST_CODE, Constants.SAVEPREDICTVERSION);
        restTransport.submit(param);
    }

    @Override
    public void saveMergeRequest(String componentId, String archetypeId, String gitlabId, String mergeId, String targetVersion) throws Exception {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.COMPONENT_ID, componentId);
        param.put(Dict.ARCHETYPE_ID, archetypeId);
        param.put(Dict.GITLABID, gitlabId);
        param.put(Dict.MERGE_REQUEST_ID, mergeId);
        param.put(Dict.TARGET_VERSION, targetVersion);
        param.put(Dict.REST_CODE, Constants.SAVEMERGEREQUEST);
        restTransport.submit(param);
    }

    @Override
    public void testPackage(String componentId, String componentType, String predictVersion, String branch, String packageType, String targetVersion) throws Exception {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.COMPONENT_ID, componentId);
        param.put(Dict.TYPE, componentType);
        param.put(Dict.PREDICT_VERSION, predictVersion);
        param.put(Dict.BRANCH, branch);
        param.put(Dict.PACKAGETYPE, packageType);
        param.put(Dict.TARGET_VERSION, targetVersion);
        param.put(Dict.REST_CODE, Constants.TESTPACKAGE);
        restTransport.submit(param);
    }

    @Override
    public void saveTargetVersion(String archetypeId, String targetVersion) throws Exception {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.ARCHETYPE_ID, archetypeId);
        param.put(Dict.TARGET_VERSION, targetVersion);
        param.put(Dict.REST_CODE, Constants.SAVETARGETVERSION);
        restTransport.submit(param);
    }

    @Override
    public void judgePredictVersion(String componentId, String versionNum, String type) throws Exception {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.COMPONENT_ID, componentId);
        param.put(Dict.TARGET_VERSION, versionNum);
        param.put(Dict.TYPE, type);
        param.put(Dict.REST_CODE, Constants.JUDGEPREDICTVERSION);
        restTransport.submit(param);
    }

    @Override
    public void judgeTargetVersion(String archetypeId, String targetVersion) throws Exception {
        Map<String, String> param = new HashMap<>();
        param.put(Dict.ARCHETYPE_ID, archetypeId);
        param.put(Dict.TARGET_VERSION, targetVersion);
        param.put(Dict.REST_CODE, Constants.JUDGETARGETVERSION);
        restTransport.submit(param);
    }
}
