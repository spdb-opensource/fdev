package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.spdb.service.IEnvConfigApi;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class IEnvConfigApiImpl implements IEnvConfigApi {
    @Autowired
    public RestTransport restTransport;


    @Override
    public Map queryEnvByNameEn(String name, Integer project_id) throws Exception {
        Map param = new HashMap();
        param.put(Dict.NAME_EN,name);
        param.put("gitlabId",project_id);
        Map result = new HashMap();
        try {
            param.put(Dict.REST_CODE, "queryModelEnvByEnvNameEn");
            result = (Map)restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.APP_THIRD_SERVER_ERROR);
        }
        if(CommonUtils.isNullOrEmpty(result)){
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"对应环境 数据异常"});
        }
        return result;
    }


    @Override
    public Map queryEnv(String id) throws Exception {
        Map param = new HashMap();
        param.put(Dict.ID,id);
        Map result = new HashMap();
        try {
            param.put(Dict.REST_CODE, "queryEnv");
            result =(Map) ((List)restTransport.submit(param)).get(0);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.APP_THIRD_SERVER_ERROR);
        }
        if(CommonUtils.isNullOrEmpty(result)){
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"环境 数据异常"});
        }
        return result;
    }


    @Override
    public Map mapFilter(Map map) throws Exception {
        // 转换 key
//         接口变更 部分 转译 FDEV_CAAS_REGISTRY FDEV_CAAS_IP FDEV_CAAS_TENANT
        final String[] envConfiKeys = {
                Dict.PROFILENAME,Dict.SPRING_CLOUD_CONFIG_URI,Dict.HOSTLOGSPATH,Dict.EUREKASERVERURI,Dict.CONFIGSERVERURI,
                Dict.FDEV_CAAS_REGISTRY,Dict.FDEV_CAAS_IP,Dict.FDEV_CAAS_TENANT,Dict.EUREKA1SERVERURI,Dict.EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
        };
        final String[] appKeys = {
                Dict.PROFILENAME_UP,Dict.SPRING_CLOUD_CONFIG_URI_UP,Dict.HOSTLOGSPATH_UP,Dict.EUREKASERVERURI_UP,Dict.CONFIGSERVERURI_UP,
                Dict.CI_CAAS_REGISTRY,Dict.CI_CAAS_IP,Dict.CI_CAAS_TENANT,Dict.EUREKA1SERVERURI_UP,Dict.EUREKA_CLIENT_SERVICEURL_DEFAULTZONE_UP
        };
        List varList = (List)map.get(Dict.VAR_LIST);
        List newVarList = new ArrayList();
        for(int i=0;i<envConfiKeys.length;i++){
            Map tmp = new HashMap();
            for(Object item:varList){
                if(envConfiKeys[i].equalsIgnoreCase((String) ((Map)item).get(Dict.KEY))){
                    tmp.put(Dict.KEY,appKeys[i]);
                    tmp.put(Dict.VALUE,((Map)item).get(Dict.VALUE));
                    tmp.put(Dict.NAME_ZH,((Map)item).get(Dict.NAME_ZH));
                }
            }
            if(!CommonUtils.isNullOrEmpty(tmp)){
                newVarList.add(tmp);
            }
        }
        map.put(Dict.VAR_LIST,newVarList);
        return map;
    }


}
