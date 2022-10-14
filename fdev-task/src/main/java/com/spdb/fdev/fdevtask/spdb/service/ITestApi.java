package com.spdb.fdev.fdevtask.spdb.service;


import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;

import java.util.List;
import java.util.Map;

public interface ITestApi {


    public Map createTest(Map param) throws Exception;


    public Map queryTest(Map param) throws Exception;

    //测试平台交互通知
    public Object interactTest(Map param) throws Exception;

    Map deleteOrder(Map param) throws Exception;

    Object updateOrder(Map param) throws Exception;

    Map generateRequestParam(String taskId, Map changedDate) throws Exception;

    String getJobId(FdevTask task, String jobid) throws Exception;

    String queryFdevSitMsg(String taskId);

    void updateUnitNo(String taskNo,String unitNo);

    List queryWorkNoByUnitNo(Map params) throws Exception;

    /**
     * 生成玉衡工单
     * @param unitNo fdev研发单元编号
     * @param internalTestStart 计划开始Sit时间
     * @param internalTestEnd 计划开始Uat时间
     * @param expectedProductDate 计划pro投产时间
     * @param requireRemark 工单备注
     * @param group_id 组id
     * @param group_name 组中文名
     */
    void createYuhengOrder(String unitNo, String internalTestStart, String internalTestEnd,
                           String expectedProductDate, String requireRemark, String group_id,String group_name);


    void deleteTaskIssue(String id);

    /**
     * 创建安全测试工单
     * @param taskId
     * @param taskName
     * @param unitNo
     * @param correlationSystem
     * @param correlationInterface
     * @param interfaceFilePath
     * @param transFilePath
     * @param remark
     * @param appName
     * @param developers
     * @param group
     * @param transList
     */
    void createSecurityOrder(String taskId, String taskName, String unitNo, String correlationSystem, String correlationInterface,
                             String interfaceFilePath, String transFilePath, String remark, String appName, String developers, String group, List<Map> transList) throws Exception;
}
