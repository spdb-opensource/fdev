package com.gotest.service;

import com.alibaba.fastjson.JSONArray;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IUserService {
    Map<String, Object> queryUserDetail(String userNameEn) throws  Exception;

    Map<String, Object> queryGroupDetailById(String groupId) throws Exception;

    List<Map<String, String>> queryChildGroupById(String groupId) throws Exception;

    Integer queryGroupTester(String groupId) throws Exception;

    List<Map> queryParentGroupById(String groupId) throws Exception;

    Map<String, Object> queryUserCoreDataById(String user_id) throws Exception;

    Map<String, Object> queryUserCoreDataByNameEn(String userNameEn) throws Exception;

    List queryDeveloper() throws Exception;

    JSONArray queryMergeRequest(String gitUser, String gitToken, String page) throws Exception;

    /**
     * 批量查询小组
     * @param groupIds
     * @return
     */
    List<Map> queryGroupByIds(List<String> groupIds) throws Exception;

    /**
     * 批量查询用户信息
     * @param userIds
     * @return
     */
    Map queryUserByIds(Set<String> userIds);

    /**
     * 批量查询小组子组
     * @param groupIds
     * @return
     */
    Map<String, List<Map>> queryChildGroupByIds(List<String> groupIds) throws Exception;

    /**
     * 批量获取小组名称
     * @param groupIds
     * @return
     */
    Map<String, String> queryGroupNameByIds(List<String> groupIds) throws Exception;
}
