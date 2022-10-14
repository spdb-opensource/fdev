package com.spdb.fdev.release.service;

import java.util.List;
import java.util.Map;

/**
 * 发送用户模块接口类
 */
public interface IUserService {

    /**
     * 查询应用负责人gitlab token
     *
     * @param application_id
     * @return
     * @throws Exception
     */
    String queryAppManagerGitlabToken(String application_id) throws Exception;

    /**
     * 查询小组管理员
     * @param owner_groupId 小组id
     * @return 投产管理员用户英文名
     * @throws Exception
     */
//	List<String> queryGroupReleaseManager(String owner_groupId) throws Exception;

    /**
     * 查询用户详细信息
     * user_name_en 用户英文名
     *
     * @return 用户详细信息
     * @throws Exception
     */
    Map<String, Object> queryUserInfo(String user_name_en) throws Exception;

    /**
     * 查询小组详情
     * @param group_id 组id
     * @return
     * @throws Exception
     */
    Map<String, Object> queryGroupDetail(String group_id) throws Exception;

    /**
     * 查询改组及子组的组列表
     * @param group_id
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> queryChildGroupByGroupId(String group_id) throws Exception;
    /**
     * 查询该组第三层级组
     * @param group_id
     * @return
     * @throws Exception
     */
    Map<String, Object> getThreeLevelGroup(String group_id) throws Exception;

        /**
         * 查询该组及父组的组列表
         * @param group_id
         * @return
         * @throws Exception
         */
    List<Map<String, Object>> queryParentGroupByGroupId(String group_id) throws Exception;
    /**
     * 查询小组下所有任务负责人
     * @param group_id
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> queryAllGroupManagers(String group_id, String role_id) throws Exception;
    /**
     * 根据用户id查询用户信息
     * @param id 用户id
     * @return
     * @throws Exception
     */
    Map<String, Object> queryUserById(String id)throws Exception;

    /**
     * 根据git用户名查询fdev账号
     * @param userName
     * @return
     * @throws Exception
     */
    Map<String, Object> queryUserByGitUser(String userName)throws Exception;

    /**
     * 查询应用行内负责人信息
     * @param application_id 应用id
     * @return
     * @throws Exception
     */
    List<Map<String, String>> queryAppSpdbManager(String application_id)throws Exception;
    /**
     * 查询应用厂商负责人
     * @param application_id 应用id
     * @return
     * @throws Exception
     */
    List<Map<String, String>> queryAppDevManager(String application_id)throws Exception;
    
    /**
     * 添加待办事项
     * @param map
     * @throws Exception
     */
    void addCommissionEvent(Map map) throws Exception;
    
    /**
     * 处理待办事项
     * @param map
     * @throws Exception
     */
    void updateCommissionEvent(Map map) throws Exception;
    
    /**
     * 删除待办事项
     * @param map
     * @throws Exception
     */
    void deleteCommissionEvent(Map map) throws Exception;

    List<Map<String, Object>> queryGroup() throws Exception;
    
    /**
     * 根据小组id查询小组中文名
     * @param String
     * @throws Exception
     */
    String queryGroupNameById(String group_id) throws Exception;
}