package com.spdb.fdev.fdevenvconfig.spdb.service;


import java.util.Map;

public interface QueryDeployInfoService {

    Map<String, Object> query(Map<String, Object> requestParam);

    Map queryDeployByAppId(Map requestParam) throws Exception;

    Map queryBindMsgByApp(Map requestParam) throws Exception;

    Map queryRealTimeBindMsg(Map requestParam) throws Exception;

    Object definedDeploy(Map requestParam) throws Exception;

    Map queryVariablesById(Map requestMap);
}
