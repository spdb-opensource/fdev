package com.spdb.fdev.fdevinterface.spdb.dao;

import com.spdb.fdev.fdevinterface.spdb.entity.ApproveStatus;

import java.util.List;
import java.util.Map;

/**
 * 接口申请Dao层相关接口
 */
public interface InterfaceApplicationDao {

    void insertRecord(List<ApproveStatus> approveStatusList);

    /**
     * 获取接口申请列表
     * @param map
     * @return
     */
    Map<String, Object> getApproveList(Map map);

    /**
     * 获取接口申请状态
     * @param transId
     * @param serviceCalling
     * @return
     */
    List<ApproveStatus> ApproveRecord(String transId, String serviceCalling,String serviceId);

    /**
     * 修改接口申请状态
     * @param map
     */
    void updateApproveStatus(Map map);

    /**
     * 修改接口申请记录
     * @param approveStatus
     */
    void updateApproveRecord(ApproveStatus approveStatus);

    /**
     * 获取接口申请
     * @param transId
     * @param serviceCalling
     * @return
     */
    List<ApproveStatus> queryApproveList(String transId, String serviceCalling,String serviceId);

    /**
     * 通过id查询接口申请记录
     * @param id
     * @return
     */
    ApproveStatus queryRecordById(String id);
    
    
    /**
     * 删除申请记录
     * @param map
     */
    void deleteApproveStatus(Map map);

}
