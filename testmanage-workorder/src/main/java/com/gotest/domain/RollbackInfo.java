package com.gotest.domain;

/**
 * 工单打回信息
 */
public class RollbackInfo {

    private String id;

    private String workNo;//工单编号

    private String date;//打回日期

    private String rollbcakOpr;//执行人员

    private String fdevGroupId;//实施单元组id

    private String mainTaskName;//主任务名

    private String fdevTaskNo;//fdev任务id

    private String reason;//原因  1-文档不规范，2-文档缺失，3-冒烟测试不通过

    private String detailInfo;//打回详情信息

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkNo() {
        return workNo;
    }

    public void setWorkNo(String workNo) {
        this.workNo = workNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRollbcakOpr() {
        return rollbcakOpr;
    }

    public void setRollbcakOpr(String rollbcakOpr) {
        this.rollbcakOpr = rollbcakOpr;
    }

    public String getFdevGroupId() {
        return fdevGroupId;
    }

    public void setFdevGroupId(String fdevGroupId) {
        this.fdevGroupId = fdevGroupId;
    }

    public String getMainTaskName() {
        return mainTaskName;
    }

    public void setMainTaskName(String mainTaskName) {
        this.mainTaskName = mainTaskName;
    }

    public String getFdevTaskNo() {
        return fdevTaskNo;
    }

    public void setFdevTaskNo(String fdevTaskNo) {
        this.fdevTaskNo = fdevTaskNo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetailInfo() { return detailInfo; }

    public void setDetailInfo(String detailInfo) { this.detailInfo = detailInfo; }
}
