package com.spdb.fdev.fdevinterface.spdb.service;

import com.spdb.fdev.fdevinterface.spdb.entity.RestApi;
import com.spdb.fdev.fdevinterface.spdb.entity.RestRelation;
import com.spdb.fdev.fdevinterface.spdb.entity.SoapRelation;
import com.spdb.fdev.fdevinterface.spdb.entity.SopRelation;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceParamShow;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceRelationShow;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 接口调用方相关接口
 */
public interface InterfaceRelationService {

    /**
     * Rest调用方相关接口
     */
    void saveRestRelation(List<RestRelation> restRelationList, String appServiceId, String branchName);

    void deleteRestRelationData(Map params);

    /**
     * Soap调用方相关接口
     */
    void saveSoapRelationList(List<SoapRelation> soapRelationList);

    List<SoapRelation> getSoapRelationList(String appServiceId, String branchName);

    void updateSoapRelationIsNewByIds(List<String> idList);

    void deleteSoapRelationData(Map params);

    /**
     * Sop调用方相关接口
     */
    void saveSopRelationList(List<SopRelation> sopRelationList);

    List<SopRelation> getSopRelationList(String appServiceId, String branchName);

    void updateSopRelationIsNewByIds(List<String> idList);

    void deleteSopRelationData(Map params);

    /**
     * 查询相关接口
     */
    List showInterfaceRelation(InterfaceParamShow interfaceParamShow);


    /**
     * 创建rest调用关系工作表
     * @param map
     */
    String createExcel(Map<String,Object> map);


    Map ExcelList(List<RestApi> restApiList, List<InterfaceRelationShow>  interfaceRelationShowList);

    /**
     * @param fileName
     * @return
     */
    void exportFile(String fileName, HttpServletResponse response) throws Exception;

    /**
     * soap和sop调用关系表新增esb相关字段初始化数据
     */
    void relateEsbData();

    List<RestRelation> queryRestRelation(String serviceCalling);

    List<RestRelation> getRestRetation();
    
    /**
     * 查询服务链路信息
     * @param serviceId
     * @param branch
     * @return
     */
    Map getServiceChainInfo(Map params);
    
    Map queryAllRelationByType(String interfaceType);
}
