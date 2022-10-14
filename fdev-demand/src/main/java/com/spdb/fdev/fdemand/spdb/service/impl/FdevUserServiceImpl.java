package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.DashboradUtils;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.service.IFdevUserService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FdevUserServiceImpl implements IFdevUserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    private RestTransport restTransport;

    /**
     * 根据用户id查询用户相关信息
     */
    @Override
    public Map queryUserInfo(String userId) {
        List<Map> resDate;
        Map sendDate = new HashMap() {{
            put("id", userId);
        }};
        sendDate.put(Dict.REST_CODE, "queryUsers");
        try {
            resDate = (List<Map>) restTransport.submit(sendDate);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FdevException(ErrorConstants.FUSER_QUERY_ERROR, new String[]{"查询用户失败"});
        }
        return resDate.get(0);
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "fdemand.allgroup")
    public  Map<String,String> queryGroup() {
        List<LinkedHashMap<String, String>> resDate;
        Map sendDate = new HashMap();
        sendDate.put(Dict.REST_CODE, Dict.GETGROUPS);
        try {
            resDate = (List<LinkedHashMap<String, String>>) restTransport.submit(sendDate);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FdevException(ErrorConstants.FUSER_QUERY_ERROR);
        }
        List<LinkedHashMap<String, String>> resDate2 = DashboradUtils.getGroupFullName(resDate);
        Map<String,String> groupList = new HashMap<>();
        for (int j = 0; j < resDate2.size(); j++) {
            LinkedHashMap<String, String> group = resDate2.get(j);
            groupList.put((String)group.get("id"),(String)group.get("fullName"));
        }
        return groupList;
    }

    @Override
    public  Map<String,Map> queryByUserCoreData(Set<String> ids , Set<String> userEns) throws Exception {
        //3.获取用户的邮箱
        Map<String, Object> map = new HashMap<>();
        if(!CommonUtils.isNullOrEmpty(ids)){
            map.put(Dict.IDS, ids);
        }else {
            map.put(Dict.USERNAMEENS, userEns);
        }

        map.put(Dict.REST_CODE, Dict.QUERYBYUSERCOREDATA);
        Map<String,Map> userMap = (Map<String, Map>) restTransport.submit(map);
        if(CommonUtils.isNullOrEmpty(userMap)){
            userMap = new HashMap<>();
        }
        return userMap ;
    }

    @Override
    public Map addCommissionEvent(DemandBaseInfo demand, List<String> userId, String type,String link,String description ) {
        String[] userIdAll = userId.toArray(new String[userId.size()]);
        Map resDate = new HashMap();
        Map sendDate = new HashMap();
        sendDate.put(Dict.REST_CODE, "addCommissionEvent");
        sendDate.put("user_ids", userIdAll);
        sendDate.put("module", "rqrmnt");
        sendDate.put("description", description);
        sendDate.put("link", link);
        sendDate.put("type", type);
        sendDate.put("target_id", demand.getId());
        try {
            resDate = (Map) restTransport.submit(sendDate);
        } catch (Exception e) {
            logger.error("======" + description + "=====添加待办失败。");
        }
        return resDate;
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "fdemand.allSection")
    public List<Map<String, String>> queryAllSection() {
        List<Map<String, String>> resDate;
        Map sendDate = new HashMap();
        sendDate.put(Dict.REST_CODE, "queryAllSection");
        try {
            resDate = (List<Map<String, String>>) restTransport.submit(sendDate);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FdevException(ErrorConstants.FUSER_QUERY_ERROR);
        }
        return resDate;
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "fdemand.group.twoall")
    public  List<Map<String, Object>> queryGroupTwoAll() {
        List<Map<String, Object>> resDate;
        Map sendDate = new HashMap();
        sendDate.put(Dict.REST_CODE, Dict.GETGROUPS);
        try {
            resDate = (List<Map<String, Object>>) restTransport.submit(sendDate);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FdevException(ErrorConstants.FUSER_QUERY_ERROR);
        }
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> groupMap : resDate) {
            //筛选二级小组
            if(!CommonUtils.isNullOrEmpty(groupMap.get(Dict.LEVEL))
                    && (Integer) groupMap.get(Dict.LEVEL)  == 2){
                data.add(groupMap);
            }
        }
        return data;
    }

    @Override
    public Map<String, Object> getThreeLevelGroup(String id) {
        Map<String, Object> resDate;
        Map sendDate = new HashMap();
        sendDate.put(Dict.ID, id);
        sendDate.put(Dict.REST_CODE, "getThreeLevelGroup");
        try {
            resDate = (Map<String, Object>) restTransport.submit(sendDate);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FdevException(ErrorConstants.FUSER_QUERY_ERROR);
        }
        return resDate;
    }
}
