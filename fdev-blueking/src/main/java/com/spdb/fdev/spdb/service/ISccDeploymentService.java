package com.spdb.fdev.spdb.service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Author:guanz2
 * @Date:2021/10/5-12:50
 * @Description:  scc_deploy service 层
 */
public interface ISccDeploymentService {

    //手动拉取蓝鲸数据
    void pullBluekingDataByManual() throws Exception;

    //拉取蓝鲸scc_deploy数据
    void pullSccDeployFromBlueking () throws Exception;

    //获取指定条件下的scc_deploy
    List<LinkedHashMap<String, Object>> getSccDeployCondition(String resource_code) throws Exception;
}