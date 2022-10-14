package com.manager.ftms.entity;

public class WeekReportAll extends Report{
    private Integer workOrderNum; //工单总数

    public Integer getWorkOrderNum() {
        if (null == workOrderNum)
            workOrderNum = 0;
        return workOrderNum;
    }

    public void setWorkOrderNum(Integer workOrderNum) {
        this.workOrderNum = workOrderNum;
    }

    @Override
    public String toString() {
        return "WeekReportAll{" +
                "workOrderNum=" + workOrderNum +
                ", testcaseNum=" + testcaseNum +
                ", testcaseIds='" + testcaseIds + '\'' +
                ", executeNum=" + executeNum +
                ", developer='" + developer + '\'' +
                ", mantisNum=" + mantisNum +
                ", mantisSource='" + mantisSource + '\'' +
                ", requireNum=" + requireNum +
                ", requireRoleNum=" + requireRoleNum +
                ", funcMantisNum=" + funcMantisNum +
                ", errorNum=" + errorNum +
                ", historyNum=" + historyNum +
                ", adviseNum=" + adviseNum +
                ", javaNum=" + javaNum +
                ", packNum=" + packNum +
                ", dataNum=" + dataNum +
                ", enNum=" + enNum +
                ", otherNum=" + otherNum +
                '}';
    }
}
