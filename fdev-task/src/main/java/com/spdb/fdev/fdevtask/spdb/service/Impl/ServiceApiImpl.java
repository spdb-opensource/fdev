package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.service.ServiceApi;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: fdev-task
 * @description:
 * @author: c-jiangl2
 * @create: 2021-03-18 16:45
 **/
@Service
@RefreshScope
public class ServiceApiImpl implements ServiceApi {

    @Autowired
    private RestTransport restTransport;

    @Override
    public void addProjectMember(Map params) throws Exception {
        params.put(Dict.REST_CODE,"addProjectMember");
        restTransport.submit(params);
    }

    @Override
    public Map queryApp(String id){
        HashMap<String, Object> params = new HashMap<>();
        params.put(Dict.ID,id);
        params.put(Dict.REST_CODE,"queryApp");
        Map submit;
        try{
            submit = (Map)restTransport.submit(params);
        }catch (Exception e){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"查询应用信息异常"});
        }
        return submit;
    }

    @Override
    public List<Map> queryProjectMember(String id) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(Dict.SERVICE_ID,id);
        params.put(Dict.Page,1);
        params.put(Dict.PAGE_NUM,99);
        params.put(Dict.REST_CODE,"queryProjectMember");
        Map submit;
        try{
            submit = (Map)restTransport.submit(params);
        }catch (Exception e){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"查询应用成员信息失败"});
        }
        return CommonUtils.castList(submit.get(Dict.MEMBER), Map.class);
    }

    @Override
    public Map queryVersionDetail(String id) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(Dict.VERSION_ID,id);
        params.put(Dict.REST_CODE,"queryVersionDetail");
        Map submit;
        try{
            submit = (Map)restTransport.submit(params);
        }catch (Exception e){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"查询应用信息异常"});
        }
        return submit;
    }

    @Override
    public void updateByTask(Map<String, Object> params) {
        params.put(Dict.REST_CODE,"updateByTask");
        try{
            restTransport.submit(params);
        }catch (Exception e){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"查询应用信息异常"});
        }
    }
}
