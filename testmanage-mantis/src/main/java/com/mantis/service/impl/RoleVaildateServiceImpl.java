package com.mantis.service.impl;

import com.mantis.dict.Dict;
import com.mantis.dict.ErrorConstants;
import com.mantis.service.MantisService;
import com.mantis.service.RoleVaildateService;
import com.mantis.util.Utils;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoleVaildateServiceImpl implements RoleVaildateService {
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private MantisService mantisService;

    @Override
    public boolean userCanEditIssueVaildate(String id) throws Exception {
        Map<String, Object> user = redisUtils.getCurrentUserInfoMap();
        if (Utils.isEmpty(user)) {
            throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
        }
        //若用户角色为超级管理员或者管理员  可操作
        List<Map> roles = (List<Map>) user.get("role");
        List<String> roleNames = roles.stream().map(e -> (String) e.get(Dict.NAME)).collect(Collectors.toList());
        if (roleNames.contains("玉衡超级管理员") || roleNames.contains("玉衡-测试管理员")) {
            return true;
        }
        String user_en_name = (String) user.get(Dict.USER_NAME_EN);
        Map<String, Object> issue = mantisService.queryIssueDetail(id);
        //若用户角色为缺陷的报告人，处理人或开发责任人，则允许
        if (issue.get(Dict.REPORTER_EN_NAME).equals(user_en_name)) {
            return true;
        }
        if (issue.get(Dict.HANDLER_EN_NAME).equals(user_en_name)) {
            return true;
        }
        if (issue.get(Dict.DEVELOPER).equals(user_en_name)) {
            return true;
        }
        return false;
    }
}
