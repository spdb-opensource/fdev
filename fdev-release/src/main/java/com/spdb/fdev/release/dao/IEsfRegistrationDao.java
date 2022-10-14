package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.EsfConfiguration;
import com.spdb.fdev.release.entity.EsfRegistration;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IEsfRegistrationDao {

    void batchAdd(List<EsfConfiguration> esfConfigurationList) throws Exception;

    List<EsfConfiguration> queryEsfConfig(String env_name, String[] network) throws Exception;

    EsfRegistration queryEsfRegistByPlatform(String prod_id, String application_id, List<String> platform) throws Exception;

    EsfRegistration queryEsfRegistById(String id) throws Exception;

    /**
     * 根据变更id查询用户注册信息
     */
    List<EsfRegistration> queryEsfRegists(String prod_id) throws Exception;

    List<EsfRegistration> queryEsfRegistsById(String prod_id,String application_id) throws Exception;

    void addEsfRegistration(EsfRegistration esfRegistration) throws Exception;

    void updateEsfRegistration(String id, String prod_id, String application_id, String caas_network_area,String scc_network_area, String sid, List<String> platform, Map<String,Object> esfInfo) throws Exception;

    void delEsf(String id) throws  Exception;


}