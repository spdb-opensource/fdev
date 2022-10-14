package com.spdb.fdev.fuser.spdb.service;

import com.spdb.fdev.fuser.spdb.entity.user.TestManagerUser;

import java.util.List;

public interface IXTestUserService {
    /**
     * 同步全量测试经理信息
     */
    void syncTestManagerInfo();

    /**
     * 查询测试经理信息
     * @param userNameEN
     * @return
     */
    List<TestManagerUser> getTestManagerInfo(String userNameEN);

}
