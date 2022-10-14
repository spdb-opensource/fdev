package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.esotericsoftware.minlog.Log;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceLazyInitService;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InterfaceLazyInitServiceImpl implements InterfaceLazyInitService {

    @Autowired
    private RestTransport restTransport;

    @Override
    @LazyInitProperty(redisKeyExpression = "finterface.appId.{name}")
    public String getAppIdByName(String name) {
        String appId = "";
        Map<String, String> map = new HashMap<>();
        map.put(Dict.REST_CODE, "queryApp");
        map.put(Dict.NAME_EN, name);
        List<Map<String, Object>> result;
        try {
            result = (List<Map<String, Object>>) restTransport.submit(map);
            if (CollectionUtils.isNotEmpty(result)) {
                for (int i = 0; i < result.size(); i++) {
                    if (name.equalsIgnoreCase(((String) result.get(i).get(Dict.NAME_EN)).trim())) {
                        appId = ((String) result.get(i).get(Dict.ID)).trim();
                    } else {
                        appId = "";
                    }
                }
            }
        } catch (Exception e) {
            Log.info("查询应用id出错-------" + name);
        }
        return appId;
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "finterface.appManagers.{name}")
    public String getAppManagers(String name) {
        String appManagers = "";
        Map<String, String> map = new HashMap<>();
        map.put(Dict.REST_CODE, "queryAppManagers");
        map.put(Dict.NAME_EN, name);
        List<Map<String, Object>> result;
        try {
            result = (List<Map<String, Object>>) restTransport.submit(map);
            if (CollectionUtils.isNotEmpty(result)) {
                for (int i = 0; i < result.size(); i++) {
                    if (name.equalsIgnoreCase(((String) result.get(i).get(Dict.NAME_EN)).trim())) {
                        List<Map> list = (List<Map>) result.get(i).get("dev_managers");
                        if(list.size()<=1){
                            appManagers=(String)list.get(0).get("user_name_cn");
                        }else {
                            for(Map map1:list){
                                String str=(String)map1.get("user_name_cn");
                                appManagers+=str+" ";
                            }
                        }
                    } else {
                        appManagers = null;
                    }
                }
            }
        } catch (Exception e) {
            Log.info("查询应用负责人出错-------" + name);
        }
        return appManagers;
    }
    /**
     * 获取当前小组的所有应用
     * @param groupId
     */
    @Override
    @LazyInitProperty(redisKeyExpression = "finterface.pagination.{groupId}")
    public List<Map<String, Object>> queryPagination(String groupId) {
        //发送应用模块接口查询当前小组的所有应用
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.REST_CODE, "queryPagination");
        map.put(Dict.SIZE, 0);//查询所有
        map.put(Dict.INDEX, 1);//当前页
        map.put(Dict.GROUPID, groupId);//当前小组ID
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            Map resultMap = (Map) restTransport.submit(map);
            list = (List<Map<String, Object>>) resultMap.get(Dict.APPENTITY);
        } catch (Exception e) {
        	Log.info("调用应用模块接口出错",e);
        }
        return list;
    }
    /**
     * 获取应用所属小组
     * @param serviceId
     */
    @Override
    @LazyInitProperty(redisKeyExpression = "finterface.appGroupName.{serviceId}")
    public String getAppGroupName(String serviceId) {
        //发送应用模块接口查询当前应用所属小组
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.REST_CODE, "queryApp");
        map.put(Dict.NAME_EN, serviceId );//当前应用ID
        Map<String, Object> appMap = new HashMap<>();
        String groupName = "";
        try {
        	List<Map<String, Object>> resultList = (List<Map<String, Object>>) restTransport.submit(map);
            if (!Util.isNullOrEmpty(resultList)) {
                appMap = (Map<String, Object>) resultList.get(0);
                groupName = (String) ((Map)appMap.get(Dict.GROUP)).get(Dict.NAME);
            }
        } catch (Exception e) {
        	  Log.info("调用fdev的app模块服务出错-------", e);
        }
        return groupName;
    }
}
