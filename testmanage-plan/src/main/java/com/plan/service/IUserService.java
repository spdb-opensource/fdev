package com.plan.service;

import java.util.Map;

public interface IUserService {

    Map<String, Object> getCurrentUser() throws  Exception;

    String queryGroupNameById(String groupId);

    Map queryGroupDetail(String groupId) throws Exception;
}
