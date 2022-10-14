package com.fdev.database.spdb.service;

import java.util.*;

public interface UserService {
    /**
     * 查询用户详细信息
     * user_name_en 用户英文名
     *
     * @return 用户详细信息
     * @throws Exception
     */
    List<Map> queryUserInfo(String user_name_en) throws Exception;

    /**
     * 查询角色信息
     * @param roleName
     * @return
     * @throws Exception
     */
    List<Map> queryRoleInfo(String roleName) throws Exception;

}
