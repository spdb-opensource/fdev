package com.manager.ftms.service.impl;

import com.manager.ftms.dao.WorkOrderMapper;
import com.manager.ftms.entity.WorkOrder;
import com.manager.ftms.service.IUserService;
import com.manager.ftms.util.Dict;
import com.manager.ftms.util.ErrorConstants;
import com.manager.ftms.util.Utils;
import com.test.testmanagecommon.cache.LazyInitProperty;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class UserServiceImpl implements IUserService {
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private Utils utils;
    @Value("${user.assessor.role.id}")
    private String assessorRoleId;
    @Value("${user.testManager.role.id}")
    private String testManagerRoleId;
    @Value("${user.testLeader.role.id}")
    private String testLeaderRoleId;
    @Value("${user.tester.role.id}")
    private String testerRoleId;
    @Value("${user.testAdmin.role.id}")
    private String testAdminRoleId;

    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private RestTransport restTransport;

    @Override
    public boolean isAdmin() throws Exception {
        Map<String, Object> user = redisUtils.getCurrentUserInfoMap();
        if (Utils.isEmpty(user)) {
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        List<String> roles = (List<String>) user.get(Dict.ROLE_ID);
        //若用户角色为 超级管理员  则返回true
        if (roles.contains(testAdminRoleId)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isMaster() throws Exception {
        Map<String, Object> user = redisUtils.getCurrentUserInfoMap();
        if (Utils.isEmpty(user)) {
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        List<String> roles = (List<String>) user.get(Dict.ROLE_ID);
        //若用户角色为 管理员 则返回true
        if (roles.contains(testManagerRoleId)) {
            return true;
        }
        return false;
    }

    @Override
    public String getUserEnName() throws Exception {
        Map<String, Object> user = redisUtils.getCurrentUserInfoMap();
        if (Utils.isEmpty(user)) {
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        return (String) user.get(Dict.USER_NAME_EN);
    }

    /**
     * 判断当前登录人是否属于工单号涉及人员
     *
     * @param workOrderNo
     * @return
     * @throws Exception
     */
    @Override
    public boolean isRelated(String workOrderNo) throws Exception {
        WorkOrder workOrder;
        try {
            workOrder = workOrderMapper.queryWorkOrderByNo(workOrderNo);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.QUERY_DATA_EXCEPTION, new String[]{"根据工单号查工单详情失败"});
        }
        if (workOrder == null) {
            throw new FtmsException(ErrorConstants.QUERY_DATA_EXCEPTION, new String[]{"该工单号在当前环境不存在"});
        }
        String testers = workOrder.getTesters();
        String workManager = workOrder.getWorkManager();
        String groupLeader = workOrder.getGroupLeader();
        String role_en_name = utils.getCurrentUserEnName();
        if (testers.contains(role_en_name) || workManager.contains(role_en_name) || groupLeader.contains(role_en_name)) {
            return true;
        }
        return false;
    }

    @LazyInitProperty(redisKeyExpression = "tuser.fdev.user.{userNameEn}")
    @Override
    public Map queryUserCoreData(String userNameEn) throws Exception {
        Map sendMap = new HashMap();
        sendMap.put(Dict.REST_CODE, "queryByUserCoreData");
        sendMap.put(Dict.USER_NAME_EN, userNameEn);
        List<Map> list = (List<Map>) restTransport.submitSourceBack(sendMap);
        if (Util.isNullOrEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    @LazyInitProperty(redisKeyExpression = "tuser.fdev.child.group.{groupId}")
    @Override
    public List<Map<String, String>> queryChildGroupById(String groupId) throws Exception {
        Map sendMap = new HashMap();
        sendMap.put(Dict.ID, String.valueOf(groupId));
        sendMap.put(Dict.REST_CODE, "queryChildGroupById");
        List<Map<String, String>> resultList = (List<Map<String, String>>) restTransport.submit(sendMap);
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

    @Override
    public Map queryUserById(String userId) throws Exception {
        Map send = new HashMap();
        send.put(Dict.ID, userId);
        send.put(Dict.REST_CODE, "fdev.user.query");
        try {
            List<Map> resultList = (List) restTransport.submitSourceBack(send);
            if (!Util.isNullOrEmpty(resultList)) {
                return resultList.get(0);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
