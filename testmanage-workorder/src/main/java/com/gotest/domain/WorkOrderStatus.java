package com.gotest.domain;

public class WorkOrderStatus {

    //工单状态
    private String workStage;
    //工单状态数量
    private Integer stageCount;

    public WorkOrderStatus() {
    }

    public WorkOrderStatus(String workStage, Integer stageCount) {
        this.workStage = workStage;
        this.stageCount = stageCount;
    }

    public String getWorkStage() {
        return workStage;
    }

    public void setWorkStage(String workStage) {
        this.workStage = workStage;
    }

    public Integer getStageCount() {
        return stageCount;
    }

    public void setStageCount(Integer stageCount) {
        this.stageCount = stageCount;
    }

    @Override
    public String toString() {
        return "WorkOrderStatus{" +
                "workStage='" + workStage + '\'' +
                ", stageCount=" + stageCount +
                '}';
    }
}
