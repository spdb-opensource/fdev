package com.spdb.fdev.fdevenvconfig.spdb.service;

import java.util.List;
import java.util.Map;


public interface IBluekingService {

    //同步蓝鲸生产信息.
    void updateBlueking() throws Exception;

    //查询蓝鲸应用
    Map<String, Object> getAllDeployments(Map<String, Object> map) throws Exception;

    //查询蓝鲸详情信息
    List<Map<String, Object>> listDeploymentDetail(Map<String, Object> map) throws Exception;

}
