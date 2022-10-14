package com.manager.ftms.service;

import java.util.List;
import java.util.Map;

public interface IUserService {

    boolean isAdmin() throws Exception;

    boolean isMaster() throws Exception;

    String getUserEnName() throws Exception;

    /**
     * 判断当前登录人是否属于工单号涉及人员
     *
     * @param workOrderNo
     * @return
     * @throws Exception
     */
    boolean isRelated(String workOrderNo) throws Exception;

    Map queryUserCoreData(String userNameEn) throws Exception;

    List<Map<String, String>> queryChildGroupById(String groupId) throws Exception;

    Integer queryGroupTester(String groupId) throws Exception;

    Map queryUserById(String userId) throws Exception;
}
