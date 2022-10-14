package com.spdb.fdev.fdevtask.spdb.service;


import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IUserApi {

    public Map queryUser(Map param) throws Exception;

    public Map queryTaskDetailUser(Map param) throws Exception;

    public Map queryGroup(Map param) throws Exception ;

    //	@LazyInitProperty(redisKeyExpression = "ftask.username.{param.id}")
    List queryUserAll(Map param) throws Exception;

    public List queryUserList(Map param) throws Exception;

    List queryAllGroup() throws Exception;

    List queryAllGroupV2();

    public Map addTodoList(Map param) throws Exception;

    public Map updateTodoList(Map param) throws Exception;

    List queryDevResource(List groups);

    List queryRole();

    List<Map> queryChildGroup(String id) throws Exception;

    List<Map> hasChild(String id) throws Exception;

    List<Map>getJobUser()throws Exception;

    List<Map> queryParentGroupById(String id) throws Exception;

    List<String> getAllChildGroup(String groupId) throws Exception;

    public Object deleteTodoList(Map param) throws Exception;

    List<Map> queryGroupByIds(List<String> ids) throws Exception;

    String getGroupNameById(String groupId);

    Map queryUser(String userName) throws Exception;

    Map<String, List> queryChildGroupByIds(List<String> group) throws Exception;

    Map getGroupsNameByIds(Set<String> groupIds) throws Exception;
}
