package com.spdb.fdev.fuser.spdb.dao;

import com.spdb.fdev.fuser.spdb.entity.user.IpmpUser;

import java.util.List;

public interface IIpmpUserDao {
    /**
     * 批量添加ipmp用户信息
     * @param ipmpUserList
     */
    void addIpmpUserBatch(List<IpmpUser> ipmpUserList);

    /**
     * 查询ipmp用户信息
     * @param userNameEN
     * @return
     */
    List<IpmpUser> queryIpmpUser(String userNameEN);
}
