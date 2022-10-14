package com.gotest.domain;

/**
 * 审核记录 对应表work_order_aduit_record
 */
public class AduitRecord {
    //记录id 主键 自增长
    private Integer aduitId;
    //记录工单号
    private String aduitWorkNo;
    //工单测试组长
    private String workLeader;
    //工单测试人员
    private String testers;
    //记录时间
    private String createTime;
    //备用字段
    private String field1;
    private String field2;
    private String field3;
    private String field4;
    private String field5;

    public Integer getAduitId() {
        return aduitId;
    }

    public void setAduitId(Integer aduitId) {
        this.aduitId = aduitId;
    }

    public String getAduitWorkNo() {
        return aduitWorkNo;
    }

    public void setAduitWorkNo(String aduitWorkNo) {
        this.aduitWorkNo = aduitWorkNo;
    }

    public String getWorkLeader() {
        return workLeader;
    }

    public void setWorkLeader(String workLeader) {
        this.workLeader = workLeader;
    }

    public String getTesters() {
        return testers;
    }

    public void setTesters(String testers) {
        this.testers = testers;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public String getField3() {
        return field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    public String getField4() {
        return field4;
    }

    public void setField4(String field4) {
        this.field4 = field4;
    }

    public String getField5() {
        return field5;
    }

    public void setField5(String field5) {
        this.field5 = field5;
    }

    @Override
    public String toString() {
        return "AduitRecord{" +
                "aduitId=" + aduitId +
                ", aduitWorkNo='" + aduitWorkNo + '\'' +
                ", workLeader='" + workLeader + '\'' +
                ", testers='" + testers + '\'' +
                ", createTime='" + createTime + '\'' +
                ", field1='" + field1 + '\'' +
                ", field2='" + field2 + '\'' +
                ", field3='" + field3 + '\'' +
                ", field4='" + field4 + '\'' +
                ", field5='" + field5 + '\'' +
                '}';
    }
}
