package com.spdb.fdev.component.service;

import java.util.List;
import java.util.Map;

public interface IGitlabUserService {

    /**
     * 添加用户权限
     *
     * @param map
     * @throws Exception
     */
    void addMembers(Map map) throws Exception;


    /**
     * 为项目添加成员
     *
     * @param id     项目id
     * @param userId 新成员用户id
     * @param role   用户角色
     * @param token  验证码
     * @return 添加结果
     * @throws Exception
     */
    Object addMember(String id, String userId, String role, String token) throws Exception;

    /**
     * 获取项目下所有用户
     *
     * @param id
     * @return
     * @throws Exception
     */
    List<Map> getProjectsUser(String id, String gitlab_token) throws Exception;

    /**
     * 通过用户名查询用户id
     *
     * @param userName 用户名
     * @param token    验证码
     * @return 用户id
     * @throws Exception
     */
    String queryUserIdByName(String userName, String token) throws Exception;

    /**
     * 检验用户是否具有role级别权限，是返回true
     *
     * @param user_id
     * @param userList
     * @return
     * @throws Exception
     */
    boolean validateUser(String user_id, List<Map> userList, String role) throws Exception;


    /**
     * 更改用户项目权限
     *
     * @param id     项目id
     * @param userId 用户id
     * @param role   权限
     * @param token  gitlabtoken
     * @return
     * @throws Exception
     */
    Object editProjectMember(String id, String userId, String role, String token) throws Exception;

    /**
     * @param projectId
     * @param role
     * @throws Exception
     */
    void changeFdevRole(String projectId, String role) throws Exception;
}
