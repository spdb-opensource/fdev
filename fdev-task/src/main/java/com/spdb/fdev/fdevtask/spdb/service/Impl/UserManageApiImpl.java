package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.service.UserManageApi;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: fdev-task
 * @description:
 * @author: c-jiangl2
 * @create: 2021-03-18 17:04
 **/
@Service
@RefreshScope
public class UserManageApiImpl implements UserManageApi {

    @Autowired
    private RestTransport restTransport;

    @Override
    public Map query(String userId) throws Exception {
        HashMap<String, Object> params = new HashMap<>();
        params.put(Dict.ID,userId);
        params.put(Dict.REST_CODE,"query");
        Map result = new HashMap();
        try{
            Object submit = restTransport.submit(params);
            if(!CommonUtils.isNullOrEmpty(submit)){
                result = CommonUtils.castList(submit,Map.class).get(0);
            }
        }catch (Exception e){
            throw new FdevException(ErrorConstants.TASK_ERROR,new String[]{"查询用户信息失败"});
        }
        return result;
    }
}
