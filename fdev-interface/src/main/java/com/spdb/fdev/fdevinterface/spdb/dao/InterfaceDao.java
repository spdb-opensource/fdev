package com.spdb.fdev.fdevinterface.spdb.dao;

import com.spdb.fdev.fdevinterface.spdb.entity.ParamDesciption;
import com.spdb.fdev.fdevinterface.spdb.entity.RestApi;
import com.spdb.fdev.fdevinterface.spdb.entity.SoapApi;
import com.spdb.fdev.fdevinterface.spdb.entity.TinyURL;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceDetailShow;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceParamShow;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceShow;

import java.util.List;
import java.util.Map;

/**
 * 接口提供方Dao层相关接口
 */
public interface InterfaceDao {

    /**
     * RestApi相关接口
     */
    void saveRestApiList(List<RestApi> restApiList);

    List<RestApi> getRestApiList(String appServiceId, String branch);

    void updateRestApiIsNewByIds(List<String> id);

    void updateRestApiRegister(List<RestApi> restApiList);

    Map<String, Object> showRestApi(InterfaceParamShow interfaceParamShow);

    List<RestApi> getRestApi();

    RestApi getRestApiName(String transId, String serviceId, String branch);

    RestApi getRestApiById(String id);

    List<RestApi> getRestApiVer(String serviceId, String transId, String branch);

    List<RestApi> getNotRegister(String serviceId, String branch);

    void deleteRestApi(Map params);

    /**
     * SoapApi相关接口
     */
    void saveSoapApiList(List<SoapApi> soapApiList);

    List<SoapApi> getSoapApiList(String appServiceId, String branch);

    void updateSoapApiIsNewByIds(List<String> id);

    Map<String, Object> showSoapApi(InterfaceParamShow interfaceParamShow);

    SoapApi getSoapApi(String transId, String branch);

    SoapApi getSoapApiById(String id);

    List<SoapApi> getSoapApiVer(String serviceId, String interfaceAlias, String branch);

    void updateSoapApiInterfaceName(List<InterfaceShow> interfaceShowList);

    void deleteSoapApi(Map params);

    /**
     * * 对外链接相关接
     */
    String saveTinyUrlKey(TinyURL tinyUrl);

    Map getTinyUrlKeyById(String id);

    void saveParamDescription(List<ParamDesciption> paramDesciption);

    ParamDesciption getParamDescription(String transId,String serviceId,String interfaceType);

    void updateParamDescription(InterfaceDetailShow interfaceDetailShow);

    List<RestApi> getRestApi(String transId,String serviceId);

}
