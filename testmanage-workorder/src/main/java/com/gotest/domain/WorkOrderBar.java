package com.gotest.domain;

/**
 * 柱状图 返回的字段
 */
public class WorkOrderBar {
    //创建工单数
    private Integer orderCreate;
    //进行工单数
    private Integer orderCount;
    //案例总数
    private Integer caseCount;
    //通过执行数
    private Integer casePass;
    //重复执行数
    private Integer passAgainCount;

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public Integer getCaseCount() {
        return caseCount;
    }

    public void setCaseCount(Integer caseCount) {
        this.caseCount = caseCount;
    }

    public Integer getCasePass() {
        return casePass;
    }

    public void setCasePass(Integer casePass) {
        this.casePass = casePass;
    }

    public Integer getPassAgainCount() {
        return passAgainCount;
    }

    public void setPassAgainCount(Integer passAgainCount) {
        this.passAgainCount = passAgainCount;
    }

    public Integer getOrderCreate() {
        return orderCreate;
    }

    public void setOrderCreate(Integer orderCreate) {
        this.orderCreate = orderCreate;
    }

    @Override
    public String toString() {
        return "WorkOrderBar{" +
                "orderCount=" + orderCount +
                ", orderCreate=" + orderCreate +
                ", caseCount=" + caseCount +
                ", casePass=" + casePass +
                ", passAgainCount=" + passAgainCount +
                '}';
    }
}
