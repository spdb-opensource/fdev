package com.spdb.fdev.fdemand.spdb.dao;

import com.spdb.fdev.fdemand.spdb.entity.IpmpUnit;
import com.spdb.fdev.fdemand.spdb.entity.OtherDemandTask;

import java.util.List;
import java.util.Map;

public interface IOtherDemandTaskDao {
    /**
     * 新增其他需求任务
     *
     * @param otherDemandTask
     * @throws Exception
     */
    void addOtherDemandTask(OtherDemandTask otherDemandTask) throws Exception;
    /**
     * 编辑其他需求任务
     *
     * @param otherDemandTask
     * @throws Exception
     */
    void updateOtherDemandTask(OtherDemandTask otherDemandTask) throws Exception;
    /**
     * 删除其他需求任务
     *
     * @param otherDemandTask
     * @throws Exception
     */
    void deleteOtherDemandTask(OtherDemandTask otherDemandTask) throws Exception;
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
     * 根据任务编号模糊其他需求任务详情
     *
     * @param params
     * @throws Exception
     */
    OtherDemandTask queryByTaskNum(String taskNum) throws Exception;
}
