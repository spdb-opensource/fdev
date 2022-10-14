package com.spdb.fdev.codeReview.base.utils;

import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author liux81
 * @DATE 2021/11/9
 */
@Component
public class AppUtil {
    @Autowired
    private RestTransport restTransport;

    public Map findAppByid(String appId) throws Exception {
        Map<String, Object> map=new HashMap();
        map.put(Dict.ID,appId);
        map.put(Dict.REST_CODE, Dict.FINDBYID);
        return (Map)restTransport.submit(map);
    }

    /**
     * 根据系统id集合批量获取系统名称，并以map的形式封装
     * @param systemIds
     * @return
     * @throws Exception
     */
    public Map<String, String> getSystemsNameMap(Set systemIds) throws Exception {
        Map<String, String> resultMap = new HashMap();
        if(!CommonUtils.isNullOrEmpty(systemIds)){
            Map param = new HashMap();
            param.put(Dict.IDS,systemIds);
            param.put(Dict.REST_CODE,Dict.QUERYSYSTEMBYIDS);
            List<Map<String,String>> systems = (List<Map<String,String>>)restTransport.submit(param);
            for(Map system:systems){
                resultMap.put((String) system.get(Dict.ID),(String) system.get(Dict.NAME));
            }
        }
        return resultMap;
    }

    /**
     * 根据系统id集合，批量获取系统名称，并拼接为字符串
     * @param systemIds
     * @param allSystemNameMap
     * @return
     */
    public String getSystemsName(Set<String> systemIds, Map<String, String> allSystemNameMap){
        String result = "";
        if(CommonUtils.isNullOrEmpty(systemIds)){
            return result;
        }
        for(String systemId : systemIds){
            result += allSystemNameMap.get(systemId) + ",";
        }
        return result.substring(0 , result.length()-1);
    }
}
