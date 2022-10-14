package com.spdb.fdev.fdevinterface.spdb.dao;

import java.util.List;
import java.util.Map;

import com.spdb.fdev.fdevinterface.spdb.entity.RestRelation;
import com.spdb.fdev.fdevinterface.spdb.entity.SoapRelation;
import com.spdb.fdev.fdevinterface.spdb.entity.SopRelation;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceParamShow;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceRelationShow;

/**
 * 接口调用方Dao层相关接口
 */
public interface InterfaceRelationDao {

    /**
     * Rest调用方相关接口
     */
    void saveRestRelation(List<RestRelation> restRelationList);

    void deleteRestRelation(String appServiceId, String branchName);

    List<RestRelation> showRestRelation(InterfaceParamShow interfaceParamShow);

    List<RestRelation> getRestRelation(String serviceCalling);

    List<RestRelation> queryRestRelation();

    void deleteRestRelation(Map params);

    /**
     * Soap调用方相关接口
     */
    List<SoapRelation> showSoapRelation(InterfaceParamShow interfaceParamShow);

    SoapRelation getSoapRelationById(String id);

    SoapRelation getSoapRelation(String serviceId, String interfaceAlias, String branch);

    List<SoapRelation> getSoapRelationList(String appServiceId, String branchName);

    void updateSoapRelationIsNewByIds(List<String> idList);

    void saveSoapRelationList(List<SoapRelation> soapRelationList);

    List<SoapRelation> getSoapRelationVer(String serviceId, String interfaceAlias, String branch);

    void updateSoapRelationServiceId(List<InterfaceRelationShow> updateServiceIdList);

    void updateSoapRelationInterfaceName(List<InterfaceRelationShow> interfaceRelationShowList);

    void deleteSoapRelation(Map params);

    /**
     * Sop调用方相关接口
     */
    List<SopRelation> showSopRelation(InterfaceParamShow interfaceParamShow);

    SopRelation getSopRelationById(String id);

    List<SopRelation> getSopRelationList(String appServiceId, String branchName);

    void updateSopRelationIsNewByIds(List<String> idList);

    void saveSopRelationList(List<SopRelation> sopRelationList);

    List<SopRelation> getSopRelationVer(String serviceId, String interfaceAlias, String branch);

    void updateSopRelationInterfaceName(List<InterfaceRelationShow> interfaceRelationShowList);

    SoapRelation getSoapId(String esbTransId);

    SopRelation getSopId(String esbTransId);

    List<SoapRelation> addSoapField();

    List<SopRelation> addSopField();

    void deleteSoap(SoapRelation soapRelation);

    void deleteSop(SopRelation sopRelation);

    void deleteSopRelation(Map params);

    /**
     * 查询所有种类接口调用关系
     */
    List<Map> queryAllRelation(InterfaceParamShow param,List<String> list); 
}
