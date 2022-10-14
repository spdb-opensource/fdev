package com.mantis.service;

import java.util.List;
import java.util.Map;

public interface UserService {

    List queryUserByNameCn() throws Exception;

    List queryAllGroup();

    Map<String, List<Map>> queryChildGroupByIds(List<String> groupIds) throws Exception;
}
