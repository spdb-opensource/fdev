package com.spdb.fdev.fdemand.spdb.dao;

import com.spdb.fdev.fdemand.spdb.entity.FdevUnitApprove;
import com.spdb.fdev.fdemand.spdb.entity.OtherDemandTask;

import java.util.List;
import java.util.Map;

public interface IFdevUnitApproveDao {

    /**
     * 保存研发单元审批
     * @throws Exception
     */
    void saveFdevUnitApprove(FdevUnitApprove fdevUnitApprove) throws Exception ;
    /**
     * 查询我的研发单元审批列表
     * @throws Exception
     */
    Map<String, Object> queryMyApproveList(String type ,String code,String sectionId,List<String> fdevUnitNos,String keyword,Integer pageNum,Integer pageSize) throws Exception ;
    /**
     * 根据IDS通过研发单元审批
     * @throws Exception
     */
    List<FdevUnitApprove> passFdevUnitApproveByIds(List<String> ids,String approverId) throws Exception;

    /**
     * 拒绝研发单元审批
     * @throws Exception
     */
    FdevUnitApprove rejectFdevUnitApproveById(String id,String approveRejectReason,String approverId) throws Exception;

    Map<String, Object> queryApproveList(String fdevUnitKey,List<String> fdevUnitLeaderIds,List<String> groupIds,List<String> fdevUnitNos, List<String> demandIds, String demandKey, List<String> approveStates, List<String> approverIds, Integer pageNum, Integer pageSize,String type);
}
