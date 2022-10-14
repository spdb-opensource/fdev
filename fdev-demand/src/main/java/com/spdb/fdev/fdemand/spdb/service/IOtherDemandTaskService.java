package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.OtherDemandTask;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IOtherDemandTaskService {
    /**
     * 新增其他需求任务
     *
     * @throws Exception
     */
    String addOtherDemandTask(OtherDemandTask otherDemandTask) throws Exception;
    /**
     * 编辑其他需求任务
     *
     * @param otherDemandTask
     * @throws Exception
     */
    String updateOtherDemandTask(OtherDemandTask otherDemandTask) throws Exception;
    /**
     * 删除其他需求任务
     *
     * @param params
     * @throws Exception
     */
    String deleteOtherDemandTask(Map<String, Object> params) throws Exception;
    /**
     * 查询其他需求任务列表
     *
     * @param params
     * @throws Exception
     */
    Map<String, Object> queryOtherDemandTaskList(Map<String, Object> params) throws Exception;
    /**
     * 查询其他需求任务详情
     *
     * @param params
     * @throws Exception
     */
    OtherDemandTask queryOtherDemandTask(Map<String, Object> params) throws Exception;
    /**
     * 更新状态
     * @throws Exception
     */
    void updateStatus(OtherDemandTask otherDemandTask)  throws Exception;
    /**
     * s删除需求下所有其他需求任务
     * @throws Exception
     */
    String deleteAllOtherDemandTask(DemandBaseInfo demandBaseInfo) throws Exception;
    /**
     * 更新其他需求任务工作量
     * @throws Exception
     */
    void updateOtherDemandTaskWorkload(Set<String> taskNum) throws Exception ;
}
