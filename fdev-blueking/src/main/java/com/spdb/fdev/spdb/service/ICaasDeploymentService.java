package com.spdb.fdev.spdb.service;

/**
 * @Author:guanz2
 * @Date:2021/10/1-19:01
 * @Description: caas_deployment service层操作
 */
public interface ICaasDeploymentService {

    //手动拉取蓝鲸数据
    void pullBluekingDataByManual() throws Exception;

    void pullCaasDeploymentFromBlueking() throws Exception;
}
