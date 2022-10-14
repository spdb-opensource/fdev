package com.spdb.fdev.fdevapp.spdb.dao;

import com.spdb.fdev.fdevapp.spdb.entity.AutoTestEnv;

public interface IAutoTestDao {
    /**
     * 获取自动测试实体信息
     * @return
     * @throws Exception
     */
    AutoTestEnv find(AutoTestEnv autoTest) throws Exception;

    /**
     * 更新自动测试实体信息
     * @param autoTest
     * @return
     * @throws Exception
     */
    AutoTestEnv update(AutoTestEnv autoTest)throws Exception;
}
