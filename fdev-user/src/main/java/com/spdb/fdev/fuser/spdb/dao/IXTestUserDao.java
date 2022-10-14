package com.spdb.fdev.fuser.spdb.dao;

import com.spdb.fdev.fuser.spdb.entity.user.TestManagerUser;

import java.util.List;

public interface IXTestUserDao {
    /**
     * 批量添加测试经理信息
     * @param xTestUserList
     */
    void addTestManagerInfo(List<TestManagerUser> xTestUserList);

    /**
     * 查询测试经理信息
     * @param userNameEN
     * @return
     */
    List<TestManagerUser> getTestManagerInfo(String userNameEN);
}
