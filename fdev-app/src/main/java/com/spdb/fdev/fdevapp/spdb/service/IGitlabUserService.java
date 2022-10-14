package com.spdb.fdev.fdevapp.spdb.service;

import java.util.List;
import java.util.Map;

public interface IGitlabUserService {
    /**
     * 查询用户在项目中的权限登记
     *
     * @param projectId 项目id
     * @param userId    用户id
     * @param token     验证码
     * @return 用户权限等级
     * @throws Exception
     */
    int queryUserRole(String projectId, String userId, String token) throws Exception;

    /**
     * 根据gitlab_user_id 检验用户是否存在
     *
     * @param userId
     * @param token
     * @return
     * @throws Exception
     */
    String checkGitlabUser(String userId, String token) throws Exception;

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
     * 为项目添加成员
     *
     * @param id     项目id
     * @param userId 新成员用户id
     * @param role   用户角色
     * @param token  验证码
     * @return 添加结果
     * @throws Exception
     */
    Object addResourceMember(String id, String userId, String role, String token) throws Exception;


    /**
     * 为用户添加组权限
     *
     * @param path   组路径
     * @param userId 添加用户id
     * @param role   用户角色
     * @param token  验证码
     * @return 添加结果
     * @throws Exception
     */
    Object addGroupMember(String path, String userId, String role, String token) throws Exception;

    /**
     * 检验用户的存在与否
     *
     * @param id
     * @return
     */
    boolean userVaildation(String id) throws Exception;

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
     * 判断用户是否属于该项目
     *
     * @param user_id     用户id
     * @param member_list 成员列表
     * @return
     * @throws Exception
     */
    boolean isProjectMember(String user_id, List member_list) throws Exception;

    /**
     * 查询项目owner
     *
     * @param id    项目id
     * @param token gittoken
     * @return
     * @throws Exception
     */
    Map<String, Object> queryProjectOwner(String id, String token) throws Exception;

    /**
     * 验证用户token是否正确
     *
     * @param token gitlab token
     * @return
     * @throws Exception
     */
    boolean checkGitlabToken(String token) throws Exception;


    /**
     * 供创建应用添加用户权限
     *
     * @param map
     * @throws Exception
     */
    void addMembers(Map map) throws Exception;


    boolean validateUser(String git_user_id, List<Map> userList, String role) throws Exception;

    List<Map> getProjectsUser(String id, String gitlab_token) throws Exception;

    void changeFdevRole(String projectId, String role) throws Exception;
}
