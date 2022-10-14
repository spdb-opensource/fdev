package com.spdb.executor.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface FuserService {

    public List getUserResource(String roleName, String roleEnName, String startTimeKey, String endTimeKey);

    public List queryRole();
    public Map queryRoleByName(String roleName);


    public List queryUser(String roleId);

    public List queryAllGroup();

    public List<Map> queryChildGroupById(String id);
    public List<String> queryChildGroup(String id);

}
