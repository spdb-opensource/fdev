package com.spdb.fdev.fdevapp.spdb.service;

import com.spdb.fdev.fdevapp.spdb.entity.AutoTestEnv;

import java.util.Map;

public interface IAutoTestService {

    AutoTestEnv query(AutoTestEnv autoTest) throws Exception;

    AutoTestEnv update(AutoTestEnv autoTest) throws Exception;

    Map autoTestEnv(Map requestParam)throws Exception;
}
