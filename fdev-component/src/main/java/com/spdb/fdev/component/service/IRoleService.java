package com.spdb.fdev.component.service;

import java.util.HashSet;
import java.util.Map;

public interface IRoleService {
    //判断用户是否为基础架构管理员
    boolean isBasicArchitectManager() throws Exception;

    //判断用户是否为组件(骨架，基础镜像)管理员
    boolean isComponentManager(HashSet<Map<String, String>> set) throws Exception;

    Map<String, Object> queryUserbyid(String id) throws Exception;

    Map<String, Object> queryUserbyGitId(String gitlab_user_id) throws Exception;

    Map<String, Object> queryByGroupId(String id);

    void addUserName(Map map, String user_id);
}
