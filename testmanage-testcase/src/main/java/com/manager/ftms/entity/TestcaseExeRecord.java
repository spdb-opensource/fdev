package com.manager.ftms.entity;

public class TestcaseExeRecord {

    private String id;
    //案例关系id
    private String fprId;
    //执行时间
    private String date;
    //执行人
    private String opr;
    //执行状态
    private String status;
    //所属工单号
    private String workNo;
    //所属计划id
    private String planId;
    //对应案例编号
    private String testcaseNo;
    //操作类型（"0"为执行，"1"为删除）
    private String oprType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOpr() {
        return opr;
    }

    public void setOpr(String opr) {
        this.opr = opr;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWorkNo() {
        return workNo;
    }

    public void setWorkNo(String workNo) {
        this.workNo = workNo;
    }

    public String getFprId() {
        return fprId;
    }

    public void setFprId(String fprId) {
        this.fprId = fprId;
    }

    public String getPlanId() { return planId; }

    public void setPlanId(String planId) { this.planId = planId; }

    public String getTestcaseNo() { return testcaseNo; }

    public void setTestcaseNo(String testcaseNo) { this.testcaseNo = testcaseNo; }

    public String getOprType() { return oprType; }

    public void setOprType(String oprType) { this.oprType = oprType; }
}

