package com.spdb.fdev.fdevapp.spdb.cache.impl;

import java.util.*;

import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.entity.ServiceSystem;
import com.spdb.fdev.fdevapp.spdb.service.IServiceSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.annoation.RemoveCachedProperty;
import com.spdb.fdev.fdevapp.spdb.cache.IAppEntityCache;
import com.spdb.fdev.fdevapp.spdb.dao.IAppEntityDao;
import com.spdb.fdev.transport.RestTransport;

@Component
@RefreshScope
public class AppEntityCacheImpl implements IAppEntityCache {
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private IAppEntityDao appEntityDao;

    @Autowired
    private IServiceSystemService serviceSystemService;

    @Value("${user.api}")
    private String userApi;

    /***
     * 对每一个应用中的用户信息进行懒加载  
     * @param key 每个应用的idkey/userNameENskey
     * @param param 向用户模块传入的数据
     * @return 最新的用户信息
     * @throws Exception
     */
    @Override
    @LazyInitProperty(redisKeyExpression = "fapp.user.{key}")
    public Map<String, Map> getUsersByIdsOrUserNameEN(String key, Map param) throws Exception {
        param.put(Dict.REST_CODE, Dict.USERCOREDATA);
        Object submit = this.restTransport.submit(param);
        return (Map<String, Map>) submit;
    }

    @Override
    @RemoveCachedProperty(redisKeyExpression = "{key}")
    public void resetAppEntityByNameEN(String key) throws Exception {
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "group.{id}")
    public Map<String, Object> queryByGroupId(String id) throws Exception {
        Map<String, Object> exceptionGroup = new HashMap<>();
        exceptionGroup.put(Dict.ID, id);
        exceptionGroup.put(Dict.NAME, "已废弃");
        // 获取所有组信息
        List<Map> groups;
        try {
            groups = this.findGroups();
        } catch (Exception e) {
            logger.error("获取组列表失败，方法名：findGroups");
            return exceptionGroup;
        }
        Map<String, String> map = new HashMap<>();
        groups.forEach(group -> map.put((String) group.get(Dict.ID), (String) group.get(Dict.NAME)));

        //发user模块获取小组详细信息
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.REST_CODE,"reBuildGroupName");
        send_map.put(Dict.ID, id);//
        List list;
        try {
            list = (List) restTransport.submit(send_map);
        } catch (Exception e) {
            logger.error("查询组全名称异常，组id：{}", id);
            return exceptionGroup;
        }

        if (list != null && list.size() > 0) {
            Map<String, Object> groupInfo = (Map<String, Object>) list.get(0);
            String groupName = (String) groupInfo.get("fullName");
            groupInfo.put(Dict.NAME, groupName);
            return groupInfo;
        }
        return exceptionGroup;
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "fuser.group")
    public List<Map> findGroups() throws Exception {
        HashMap<Object, Object> map = new HashMap<>();
        map.put(Dict.REST_CODE, "findGroups");
        return (List<Map>) this.restTransport.submit(map);
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "ftask.serviceSystem")
    public List<ServiceSystem> querySystem() throws Exception {

//        HashMap<Object, Object> map = new HashMap<>();
////        map.put(Dict.REST_CODE, "querySystem");
        return this.serviceSystemService.query(null);
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "allGroup.{groupId}")
    public List<String> getAllGroup(String groupId) throws Exception {
        List<String> result = new ArrayList<>();
        List<Map> groups = this.findGroups();
        //递归获取所有子组
        getChild(groupId, groups, result);
        result.add(groupId);
        return result;
    }


    public void getChild(String groupId, List<Map> groups, List<String> result) {
        List<String> childResult = new ArrayList<>();
        for (Map info : groups) {
            if (groupId.equals(info.get(Dict.PARENT_ID))) {
                childResult.add((String) info.get(Dict.ID));
            }
        }
        if (childResult.size() > 0) {
            result.addAll(childResult);
            for (String subChild : childResult) {
                getChild(subChild, groups, result);
            }
        }
    }
    
	@Override
	public List<Map> queryChildGroupById(String groupId) throws Exception {
		List resDate;
        Map sendDate = new HashMap();
        sendDate.put(Dict.REST_CODE, "queryChildGroupById");
        sendDate.put("id", groupId);
        resDate = (List<Map>) restTransport.submit(sendDate);
        return resDate;
	}

    @Override
    public void updateManager(String id,  Set<Map<String, String>> spdb_managers){
        try {
            Map map = new HashMap<>();
            map.put(Dict.ID, id);
            map.put("spdb_managers", spdb_managers);
            map.put(Dict.REST_CODE, "updateManager");
            restTransport.submit(map);
        } catch (Exception e) {
            logger.error("数据库模块应用负责人同步失败");
        }
    }
}
