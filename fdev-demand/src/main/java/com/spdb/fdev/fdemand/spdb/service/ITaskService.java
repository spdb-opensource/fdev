package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ITaskService {

    void taskDeferOrRecover(String demandId,Integer status);

    Map judgeDeferOrDelete(String demandId);

	void taskRecoverByImpl(String implId, Integer status);

    List<Map> queryTaskByDemandId(String demandId);

    //查询该需求所有板块的评估状态，返回板块最小的评估状态
    HashMap<String,String> queryDemandStatusByPart(DemandBaseInfo demandBaseInfo);

    List findTaskByTerms(Set<String> groupIds, Boolean isParent) throws Exception;
    
    Integer queryNotDiscarddnum(String fdevImplNo);
    
    void updateDemandStatus(String id) throws Exception;

    List<Map> queryDetailByUnitNo(List<String> fdevImplUnitNo);

    void updateTaskSpectialStatus(String unitNo ,String stage ,String taskSpectialStatus );

    List<Map> queryTaskByUnitNos(List<String> fdevImplUnitNo);

    void updateStartUatTime(String unitNo  ,String taskSpectialStatus );

    List<Map> queryDocDetail(String taskId);
}
