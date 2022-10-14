package com.gotest.domain;

/**
 * 对应数据表 msg_fdev
 */
public class MessageFdev {
    //消息id
    private Integer messageId;
    //用户英文名：用户的唯一性
    private String userEnName;
    //任务编号
    private String taskNo;
    //任务名称
    private String taskName;
    //工单编号
    private String workNo;
    //工单状态 所属阶段 0未分配 1分配中 2开发中 3sit 4uat 5已投产
    private String workStage;
    //消息状态 1：未读，0：已读
    private String messageFlag;
    //测试原因
    private String taskReason;
    //jira编号
    private String jiraNo;
    //任务描述
    private String taskDesc;
    //创建时间
    private Integer createTime;
    //需求编号
    private String rqrNo;

    @Override
    public String toString() {
        return "MessageFdev{" +
                "messageId='" + messageId + '\'' +
                ", userEnName='" + userEnName + '\'' +
                ", taskNo='" + taskNo + '\'' +
                ", taskName='" + taskName + '\'' +
                ", workNo='" + workNo + '\'' +
                ", workStage='" + workStage + '\'' +
                ", messageFlag='" + messageFlag + '\'' +
                ", taskReason='" + taskReason + '\'' +
                ", jiraNo='" + jiraNo + '\'' +
                ", taskDesc='" + taskDesc + '\'' +
                ", createTime=" + createTime +
                '}';
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public String getUserEnName() {
        return userEnName;
    }

    public void setUserEnName(String userEnName) {
        this.userEnName = userEnName;
    }

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getWorkNo() {
        return workNo;
    }

    public void setWorkNo(String workNo) {
        this.workNo = workNo;
    }

    public String getWorkStage() {
        return workStage;
    }

    public void setWorkStage(String workStage) {
        this.workStage = workStage;
    }

    public String getMessageFlag() {
        return messageFlag;
    }

    public void setMessageFlag(String messageFlag) {
        this.messageFlag = messageFlag;
    }

    public String getTaskReason() {
        return taskReason;
    }

    public void setTaskReason(String taskReason) {
        this.taskReason = taskReason;
    }

    public String getJiraNo() {
        return jiraNo;
    }

    public void setJiraNo(String jiraNo) {
        this.jiraNo = jiraNo;
    }

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public String getRqrNo() {
        return rqrNo;
    }

    public void setRqrNo(String rqrNo) {
        this.rqrNo = rqrNo;
    }
}
