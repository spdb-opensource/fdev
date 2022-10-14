package com.spdb.fdev.fuser.spdb.service;

import com.spdb.fdev.fuser.spdb.entity.user.IpmpUser;

import java.util.List;
import java.util.Map;

public interface IIpmpUserService {
    /**
     * 同步全量人员信息
     */
    void syncAllIpmpUser();

    /**
     * 查询人员信息
     * @param userNameEN
     * @return
     */
    List<Map<String,String>> queryIpmpUser(String userNameEN);


}
