package com.spdb.fdev.fdevinterface.spdb.service;

import com.spdb.fdev.fdevinterface.spdb.entity.RestApi;
import com.spdb.fdev.fdevinterface.spdb.entity.SoapApi;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceDetailShow;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceParamShow;

import java.util.List;
import java.util.Map;

/**
 * 接口提供方相关接口
 */
public interface InterfaceService {

    /**
     * Rest提供方相关接口
     */
    List<RestApi> getRestApiList(String appServiceId, String branchName);

    void updateRestApiIsNewByIds(List<String> idList);

    void saveRestApiList(List<RestApi> restApiList);

    void updateRestApiRegister(List<RestApi> restApiList);

    List<String> getNotRegister(String serviceId, String branch);

    void deleteRestData(Map params);

    /**
     * Soap提供方相关接口
     */
    List<SoapApi> getSoapApiList(String appServiceId, String branchName);

    void updateSoapApiIsNewByIds(List<String> idList);

    void saveSoapApiList(List<SoapApi> soapApiList);

    void deleteSoapData(Map params);

    /**
     * 接口提供方查询相关接口
     */
    Map showInterface(InterfaceParamShow interfaceParamShow);

    List<RestApi> getInterface();

    InterfaceDetailShow getInterfaceDetailById(String id, String interfaceType);

    List<InterfaceDetailShow> getInterfaceVer(String id, String interfaceType);

    /**
     * * 生成对外接口链接
     */
    String getInterfacesUrl(String branch, String serviceId, List<Map> idsList);

    Map getInterfacesByUrl(String id);

    void modifiInterfaceDescription(InterfaceDetailShow interfaceDetailShow);

}
