package com.testmanage.admin.service.impl;

import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.util.Util;
import com.testmanage.admin.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author 李尚林 on 2020/8/17
 * @c-lisl1
 **/
@Service
@RefreshScope
public class UserServiceImpl implements IUserService {

    @Autowired
    private RedisUtils redisUtils;

    @Value("${user.testManager.role.id}")
    private String testManagerRoleId;

    @Value("${user.testLeader.role.id}")
    private String testLeaderRoleId;

    @Value("${user.tester.role.id}")
    private String testerRoleId;

    @Value("${user.testAdmin.role.id}")
    private String testAdminRoleId;

    @Override
    public Map<String, Object> getCurrentUser() throws Exception {
        Map<String, Object> user = redisUtils.getCurrentUserInfoMap();
        if(Util.isNullOrEmpty(user)){

        }
        return null;
    }
}
