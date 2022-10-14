package com.gotest.domain;

import java.util.List;

/**
 * 单独记录fdev任务提交sit测试时的数据，对应数据表 FTMS_SUBMIT_SIT_RECORD
 */
public class FdevSitMsg {
    //主键id
    private Integer id;
    //任务编号
    private String taskNo;
    //测试原因
    private String testReason;
    //功能（修复）描述
    private String repairDesc;
    //JIRA编号
    private String jiraNo;
    //创建日期
    private String createTime;
    //预计开始uat日期
    private String planStartUatDate;
    //预计投产日期
    private String planProductDate;
    //回归测试范围
    private String regressionTestScope;
    //交易接口变动
    private String interfaceChange;
    //数据库变动
    private String databaseChange;
    //关联系统变更
    private String otherSystemChange;
    //开发人员
    private String developer;
    //测试环境
    private String testEnv;
    //应用名称
    private String appName;
    //客户端版本
    private String clientVersion;
    //工单号
    private String workNo;
    //工单名称
    private String taskName;
    //测试人员
    private String testers;
    //需求编号
    private String rqrNo;
    //组id
    private String groupId;
    //组名
    private String groupName;
    //工单状态
    private String stage;
    //任务名称
    private String fdevTaskName;
    //fdev_new
    private String fdev_new ;
    //抄送人集合
    private String copyTo;
    //安全测试关联系统
    private String correlationSystem;
    //安全测试关联接口
    private String correlationInterface;
    //接口文件路径
    private String interfaceFilePath;
    //交易清单列表文件路径
    private String transFilePath;
    //对应工单类型
    private String orderType;
    //工单所属组id
    private String orderGroupId;
    //工单所属组名称
    private String orderGroupName;

    public String getCopyTo() {
        return copyTo;
    }

    public void setCopyTo(String copyTo) {
        this.copyTo = copyTo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public String getTestReason() {
        return testReason;
    }

    public void setTestReason(String testReason) {
        this.testReason = testReason;
    }

    public String getRepairDesc() {
        return repairDesc;
    }

    public void setRepairDesc(String repairDesc) {
        this.repairDesc = repairDesc;
    }

    public String getJiraNo() {
        return jiraNo;
    }

    public void setJiraNo(String jiraNo) {
        this.jiraNo = jiraNo;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getPlanStartUatDate() {
        return planStartUatDate;
    }

    public void setPlanStartUatDate(String planStartUatDate) {
        this.planStartUatDate = planStartUatDate;
    }

    public String getPlanProductDate() {
        return planProductDate;
    }

    public void setPlanProductDate(String planProductDate) {
        this.planProductDate = planProductDate;
    }

    public String getRegressionTestScope() {
        return regressionTestScope;
    }

    public void setRegressionTestScope(String regressionTestScope) {
        this.regressionTestScope = regressionTestScope;
    }

    public String getInterfaceChange() {
        return interfaceChange;
    }

    public void setInterfaceChange(String interfaceChange) {
        this.interfaceChange = interfaceChange;
    }

    public String getDatabaseChange() {
        return databaseChange;
    }

    public void setDatabaseChange(String databaseChange) {
        this.databaseChange = databaseChange;
    }

    public String getOtherSystemChange() {
        return otherSystemChange;
    }

    public void setOtherSystemChange(String otherSystemChange) {
        this.otherSystemChange = otherSystemChange;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getTestEnv() {
        return testEnv;
    }

    public void setTestEnv(String testEnv) {
        this.testEnv = testEnv;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getWorkNo() {
        return workNo;
    }

    public void setWorkNo(String workNo) {
        this.workNo = workNo;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getRqrNo() {
        return rqrNo;
    }

    public void setRqrNo(String rqrNo) {
        this.rqrNo = rqrNo;
    }

    public String getTesters() {
        return testers;
    }

    public void setTesters(String testers) {
        this.testers = testers;
    }

    public String getGroupId() { return groupId; }

    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }

    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getStage() { return stage; }

    public void setStage(String stage) { this.stage = stage; }

    public String getFdevTaskName() { return fdevTaskName; }

    public void setFdevTaskName(String fdevTaskName) { this.fdevTaskName = fdevTaskName; }

    public String getFdev_new() {
        return fdev_new;
    }

    public void setFdev_new(String fdev_new) {
        this.fdev_new = fdev_new;
    }

    public String getCorrelationSystem() {
        return correlationSystem;
    }

    public void setCorrelationSystem(String correlationSystem) {
        this.correlationSystem = correlationSystem;
    }

    public String getCorrelationInterface() {
        return correlationInterface;
    }

    public void setCorrelationInterface(String correlationInterface) {
        this.correlationInterface = correlationInterface;
    }

    public String getInterfaceFilePath() {
        return interfaceFilePath;
    }

    public void setInterfaceFilePath(String interfaceFilePath) {
        this.interfaceFilePath = interfaceFilePath;
    }

    public String getTransFilePath() {
        return transFilePath;
    }

    public void setTransFilePath(String transFilePath) {
        this.transFilePath = transFilePath;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderGroupId() {
        return orderGroupId;
    }

    public void setOrderGroupId(String orderGroupId) {
        this.orderGroupId = orderGroupId;
    }

    public String getOrderGroupName() {
        return orderGroupName;
    }

    public void setOrderGroupName(String orderGroupName) {
        this.orderGroupName = orderGroupName;
    }
}
