package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.component.service.IRoleService;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RefreshScope
public class RoleServiceImpl implements IRoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Value("${fdev.role.control.enabled:true}")
    private boolean roleControlEnabled;

    @Value("${basic.architect.manager.role.id}")
    private String basic_architect_manager;

    @Autowired
    private RestTransport restTransport;

    @Override
    public boolean isBasicArchitectManager() throws Exception {
        if (roleControlEnabled) {
            User user = CommonUtils.getSessionUser();
            if (user.getRole_id().contains(basic_architect_manager)) {
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isComponentManager(HashSet<Map<String, String>> set) throws Exception {
        User user = CommonUtils.getSessionUser();
        if (set != null & set.size() > 0) {
            for (Map map : set) {
                String userId = (String) map.get(Dict.ID);
                if (user.getId().equals(userId)) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    @LazyInitProperty(redisKeyExpression = "userinfo.{id}")
    public Map<String, Object> queryUserbyid(String id) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.REST_CODE, Dict.QUERYUSERCOREDATA);
        send_map.put(Dict.ID, id);// 发app模块获取人员详细信息
        List<Map<String, Object>> list = (ArrayList) restTransport.submit(send_map);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "userinfo.{gitlab_user_id}")
    public Map<String, Object> queryUserbyGitId(String gitlab_user_id) throws Exception {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.REST_CODE, Dict.QUERYUSER);
        send_map.put(Dict.GIT_USER_ID, gitlab_user_id);// 发app模块获取人员详细信息
        List<Map<String, Object>> list = (ArrayList) restTransport.submit(send_map);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    @LazyInitProperty(redisKeyExpression = "group.{id}")
    public Map<String, Object> queryByGroupId(String id) {
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.REST_CODE, Dict.QUERYCHILDGROUPBYID);
        send_map.put(Dict.ID, id);// 发app模块获取小组详细信息
        try {
            List<Map<String, Object>> list = (ArrayList) restTransport.submit(send_map);
            if (list != null && list.size() > 0) {
                return list.get(0);
            }
            return null;
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 根据人员id增加用户中英文名
     *
     * @param map
     * @param user_id
     */
    @Override
    public void addUserName(Map map, String user_id) {
        try {
            if (StringUtils.isNotBlank(user_id)) {
                Map userMap = this.queryUserbyid(user_id);
                if (userMap != null) {
                    map.put(Dict.NAME_EN, userMap.get(Dict.USER_NAME_EN));
                    map.put(Dict.NAME_CN, userMap.get(Dict.USER_NAME_CN));
                    Map assigneeMap = new HashMap();
                    assigneeMap.put(Dict.ID, user_id);
                    assigneeMap.put(Dict.USER_NAME_EN, userMap.get(Dict.USER_NAME_EN));
                    assigneeMap.put(Dict.USER_NAME_CN, userMap.get(Dict.USER_NAME_CN));
                    map.put("assigneeMap", assigneeMap);
                }
            }
        } catch (Exception e) {
            logger.error("根据人员id增加用户中英文名失败,{}", e.getMessage());
        }

    }

}
