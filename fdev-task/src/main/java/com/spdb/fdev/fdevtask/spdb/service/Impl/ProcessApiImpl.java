package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.entity.Component;
import com.spdb.fdev.fdevtask.spdb.entity.ModelExt;
import com.spdb.fdev.fdevtask.spdb.service.ProcessApi;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @program: fdev-task
 * @description:
 * @author: c-jiangl2
 * @create: 2021-01-29 15:41
 **/
@Service
@RefreshScope
public class ProcessApiImpl implements ProcessApi {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTransport restTransport;

    @Autowired
    public UserVerifyUtil userVerifyUtil;

    @Override
    public Object getProcessDetail(String processId) throws Exception{
        Map param = new HashMap();
        param.put(Dict.ID,processId);
        param.put(Dict.REST_CODE,"getProcessDetail");
        Object result;
        try {
            result = restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{e.getMessage()});
        }
        return result;
    }

    @Override
    public Object updateValue(List<ModelExt> modelExts) throws Exception{
        Map map = new HashMap();
        map.put(Dict.EXT_LIST,modelExts);
        map.put(Dict.REST_CODE,"updateValue");
        Object result;
        try {
            result = restTransport.submit(map);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{"保存字段值异常"});
        }
        return result;
    }

    @Override
    public void createProcessInstance(String taskId, String processId) throws Exception {
        Map map = new HashMap();
        map.put(Dict.TASK_ID,taskId);
        map.put(Dict.PROCESS_ID,processId);
        map.put(Dict.REST_CODE,"createProcessInstance");
        Object result;
        try {
            result = restTransport.submit(map);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{e.getMessage()});
        }
    }

    @Override
    public Map getFieldAndValue(String model, String modelId, String teamId,String lable) throws Exception {
        Map map = new HashMap();
        map.put(Dict.MODEL,model);
        map.put(Dict.TEAM_ID,teamId);
        map.put(Dict.MODEL_ID,modelId);
        map.put(Dict.LABLE,lable);
        map.put(Dict.REST_CODE,"getFieldAndValue");
        HashMap result;
        try {
            result = (HashMap)restTransport.submit(map);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{"获取自定义字段和值异常"});
        }
        return result;
    }

    @Override
    public Map<String , Object> queryInstanceByTaskId(String taskId){
        Map<String , Object> map = new HashMap();
        map.put(Dict.TASK_ID,taskId);
        map.put(Dict.REST_CODE,"queryInstanceByTaskId");
        Map result;
        try {
            result = (Map<String,Object>)restTransport.submit(map);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{e.getMessage()});
        }
        return result;
    }

    @Override
    public List<Component> queryComponent(List<String> id) throws Exception {
        Map map = new HashMap();
        map.put(Dict.ID,id);
        map.put(Dict.REST_CODE,"queryComponent");
        List<Component> result;
        try {
            result = (List<Component>)restTransport.submit(map);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{"获取组件信息异常"});
        }
        return result;
    }

    @Override
    public Map getInstanceStatus(String task_id, String statusId) throws Exception {
        Map map = new HashMap();
        map.put(Dict.TASK_ID,task_id);
        map.put(Dict.STATUS_ID,statusId);
        map.put(Dict.REST_CODE,"getInstanceStatus");
        Map result;
        try {
            result = (Map)restTransport.submit(map);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{"获取目标状态信息异常"});
        }
        return result;
    }

    @Override
    public void changeTaskStatus(Map request) throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest()
                .getHeader(com.spdb.fdev.common.dict.Dict.TOKEN_AUTHORIZATION_KEY));
        request.put(Dict.REST_CODE,"changeStatus");
        request.put(Dict.USERID,user.getId());
        request.put(Dict.ROLE_ID,user.getRole_id());
        request.put(Dict.TASK_ID,request.get(Dict.TASKID));
        request.put(Dict.SOURCESTATUSID,request.get(Dict.SOURCE_STATUS_ID));
        request.put(Dict.DESSTATUSID,request.get(Dict.DEST_STATUS_ID));
        try {
            restTransport.submit(request);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{e.getMessage()});
        }
    }

    @Override
    public List<Map> queryComponentByIds(List<String> ids){
        Map map = new HashMap();
        map.put(Dict.IDS,ids);
        map.put(Dict.REST_CODE,"queryComponentByIds");
        List<Map> result;
        try {
            result = (List<Map>)restTransport.submit(map);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{"获取组件列表异常"});
        }
        return result;
    }

    @Override
    public void createFeature(String taskId, String applicationId, String branchName, String requirmentNo
            , String createFrom, List<String> assigneeList) throws Exception{
        Map map = new HashMap();
        map.put(Dict.REST_CODE,"createFeature");
        map.put(Dict.TASK_ID,taskId);
        map.put(Dict.APPLICATION_ID,applicationId);
        map.put(Dict.BRANCH_NAME,branchName);
        map.put(Dict.REQUIRMENT_NO,requirmentNo);
        map.put(Dict.CREATE_FROM,createFrom);
        map.put(Dict.ASSIGNEE_LIST,assigneeList);
        restTransport.submit(map);
    }

    @Override
    public List<Map> queryInstanceByTaskIds(List<String> taskIds){
        Map map = new HashMap();
        map.put("task_ids",taskIds);
        map.put(Dict.REST_CODE,"queryInstanceByTaskIds");
        List<Map> result;
        try {
            result = (List<Map>)restTransport.submit(map);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{"获取流程实例列表失败"});
        }
        return result;
    }

    @Override
    public List<Map> mountVersionCallBack(Integer mountTask, List<String> taskId, String versionId) throws Exception {
        Map map = new HashMap();
        map.put(Dict.IDS,taskId);
        map.put(Dict.MOUNT_STATUS,mountTask+"");
        map.put(Dict.VERSION_ID,versionId);
        map.put(Dict.REST_CODE,"mountVersionCallBack");
        List<Map> result = new ArrayList<>();
        try {
            Object submit = restTransport.submit(map);
            if(!CommonUtils.isNullOrEmpty(submit)){
                result = CommonUtils.castList(submit,Map.class);
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{"回掉挂载失败"});
        }
        return result;
    }

    @Override
    public List<Map> queryComponentList() throws Exception {
        Map map = new HashMap();
        map.put(Dict.REST_CODE,"queryComponentList");
        List<Map> result;
        try {
            result = CommonUtils.castList(restTransport.submit(map),Map.class);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.PROCESS_ERROR,new String[]{"获取组件列表列表失败"});
        }
        return result;
    }
}
