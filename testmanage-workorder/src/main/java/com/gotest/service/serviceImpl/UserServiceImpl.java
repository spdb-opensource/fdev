package com.gotest.service.serviceImpl;

import com.alibaba.fastjson.JSONArray;
import com.gotest.dict.Dict;
import com.gotest.service.IUserService;
import com.gotest.utils.CommonUtils;
import com.gotest.utils.GitlabTransport;
import com.test.testmanagecommon.cache.LazyInitProperty;
import com.test.testmanagecommon.cache.LazyInitPropertyLong;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class UserServiceImpl implements IUserService {
    @Autowired
    private RestTransport restTransport;
    @Value("${gitlab.url}")
    private String gitlab_url;
    private Logger logger = LoggerFactory.getLogger(OrderDimensionServiceImpl.class);
    @Autowired
    private GitlabTransport gitlabTransport;

    @Override
    public Map<String, Object> queryUserDetail(String userNameEn ) throws Exception {
        Map sendMap = new HashMap();
        sendMap.put(Dict.REST_CODE, "queryByUserCoreData");
        sendMap.put(Dict.USER_NAME_EN, userNameEn);
        List<Map> list = (List<Map>)restTransport.submitSourceBack(sendMap);
        if(Util.isNullOrEmpty(list)){
            return null;
        }
        return list.get(0);
    }

    @LazyInitProperty(redisKeyExpression = "tuser.fdev.group.{groupId}")
    @Override
    public Map<String, Object> queryGroupDetailById(String groupId) throws Exception {
        Map sendMap = new HashMap();
        sendMap.put(Dict.REST_CODE, "fdev.user.queryGroupDetail");
        sendMap.put(Dict.ID, groupId);
        List<Map> groups = (List<Map>)restTransport.submitSourceBack(sendMap);
        if(!Util.isNullOrEmpty(groups)){
            return groups.get(0);
        }else{
            return null;
        }
    }

    @LazyInitProperty(redisKeyExpression = "tuser.fdev.child.group.{groupId}")
    @Override
    public List<Map<String, String>> queryChildGroupById(String groupId) throws Exception {
        Map sendMap = new HashMap();
        sendMap.put(Dict.ID, String.valueOf(groupId));
        sendMap.put(Dict.REST_CODE, "queryChildGroupById");
        List<Map<String, String>> resultList = (List<Map<String, String>>) restTransport.submitSourceBack(sendMap);
        return resultList;
    }

    @LazyInitProperty(redisKeyExpression = "tuser.fdev.group.testers.{groupId}")
    @Override
    public Integer queryGroupTester(String groupId) throws Exception {
        List<Map<String, String>> mapList = queryChildGroupById(groupId);
        Set<String> collect = mapList.stream().map(e -> e.get(Dict.ID)).collect(Collectors.toSet());
        Integer countTester = 0;
        for (String group_id : collect) {
            Map sendMap = new HashMap();
            sendMap.put(Dict.REST_CODE, "queryUser");
            sendMap.put(Dict.GROUP_ID, group_id);
            List<String> search = new ArrayList<>();
            search.add("测试人员");
            sendMap.put(Dict.SEARCH, search);
            sendMap.put(Dict.STATUS, "0");
            sendMap.put(Dict.PAGE, 0);
            sendMap.put(Dict.PER_PAGE, 0);
            Map users = (Map) restTransport.submitSourceBack(sendMap);
            if (!Util.isNullOrEmpty(users)) {
                countTester += (Integer) users.get(Dict.TOTAL);
            }
        }
        return countTester;
    }

    @LazyInitProperty(redisKeyExpression = "tuser.fdev.parent.group.{groupId}")
    @Override
    public List<Map> queryParentGroupById(String groupId) throws Exception {
        Map sendMap = new HashMap();
        sendMap.put(Dict.REST_CODE, "queryParentGroupById");
        sendMap.put(Dict.ID, groupId);
        List<Map> groups = (List<Map>)restTransport.submitSourceBack(sendMap);
        return groups;
    }

    @LazyInitProperty(redisKeyExpression = "tuser.fdev.user.{user_id}")
    @Override
    public Map<String, Object> queryUserCoreDataById(String user_id) throws Exception {
        Map sendData = new HashMap<String, String>();
        sendData.put(Dict.ID, user_id);
        sendData.put(Dict.REST_CODE, "queryByUserCoreData");
        List<Map> users = (List<Map>)restTransport.submitSourceBack(sendData);
        if(!Util.isNullOrEmpty(users)){
            return users.get(0);
        }
        return null;
    }

    @LazyInitProperty(redisKeyExpression = "tuser.fdev.user.{userNameEn}")
    @Override
    public Map<String, Object> queryUserCoreDataByNameEn(String userNameEn) throws Exception {
        Map sendData = new HashMap<String, String>();
        sendData.put(Dict.USER_NAME_EN, userNameEn);
        sendData.put(Dict.REST_CODE, "queryByUserCoreData");
        List<Map> users = (List<Map>)restTransport.submitSourceBack(sendData);
        if(!Util.isNullOrEmpty(users)){
            return users.get(0);
        }
        return null;
    }

    @LazyInitPropertyLong(redisKeyExpression = "ftms.query.fdev.user.developer")
    @Override
    public List queryDeveloper() throws Exception {
        Map sendMap = new HashMap();
        sendMap.put(Dict.REST_CODE, "queryUser");
        List<String> search = new ArrayList<>();
        search.add("开发人员");
        sendMap.put(Dict.SEARCH, search);
        sendMap.put(Dict.STATUS, "0");
        sendMap.put(Dict.PAGE, 0);
        sendMap.put(Dict.PER_PAGE, 0);
        Map users = (Map) restTransport.submitSourceBack(sendMap);
        if(!Util.isNullOrEmpty(users.get(Dict.LIST))){
            return (List)users.get(Dict.LIST);
        }
        return null;
    }

    @Override
    public JSONArray queryMergeRequest(String gitUser, String gitToken, String page) throws Exception {
        String url = gitlab_url + "api/v4/merge_requests?state=merged&per_page=100&author_username=" + gitUser + "&page=" + page;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, gitToken);
        Object submitGet = gitlabTransport.submitGet(url, header);
        JSONArray parseArray = JSONArray.parseArray((String) submitGet);
        return parseArray;
    }

    @Override
    public List<Map> queryGroupByIds(List<String> groupIds) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.GROUPIDS, groupIds);
        param.put(Dict.REST_CODE, "queryGroupByIds");
        return (List<Map>) restTransport.submitSourceBack(param);
    }

    @Override
    public Map queryUserByIds(Set<String> userIds) {
        Map<String, Object> param = new HashMap<String, Object>(){{
            put(Dict.IDS, userIds);
            put(Dict.REST_CODE, "queryUserByIds");
        }};
        try {
            return (Map) restTransport.submitSourceBack(param);
        }catch (Exception e) {
            logger.info(">>>queryUserByIds批量查询用户信息失败");
        }
        return new HashMap();
    }

    @Override
    public Map<String, List<Map>> queryChildGroupByIds(List<String> groupIds) throws Exception {
        Map<String,Object> param = new HashMap<>();
        param.put(Dict.IDS, groupIds);
        param.put(Dict.REST_CODE, "queryChildGroupByIds");
        return (Map<String, List<Map>>) restTransport.submitSourceBack(param);
    }

    @Override
    public Map<String, String> queryGroupNameByIds(List<String> groupIds) throws Exception {
        List<Map> groupInfos = queryGroupByIds(groupIds);
        Map<String,String> groupNameMap = new HashMap<>();
        if(!CommonUtils.isNullOrEmpty(groupInfos)) {
            for (Map<String,String> groupInfo : groupInfos) {
                groupNameMap.put(groupInfo.get(Dict.ID), groupInfo.get(Dict.NAME));
            }
        }
        return groupNameMap;
    }
}
