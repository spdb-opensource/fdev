package com.spdb.fdev.codeReview.spdb.dto;

import java.util.Set;

/**
 * @Author liux81
 * @DATE 2022/1/13
 */
public class ProblemDto {
    private String id;
    private String orderId;//工单id
    private String meetingId;//会议id
    private String demandId;//需求id
    private String problem;//问题描述
    private String problemType;//问题类型。issue缺陷/advice建议/risk风险
    private String itemType;//问题项类型key
    private Integer problemNum;//问题次数
    private String fixFlag;//是否已修复。fixed已修复/notFixed未修复
    private String fixDate;//修复日期
    private String createTime;//创建时间
    private String remark;//备注
    private String ItemTypeValue;//问题项内容
    private String orderNo;//工单编号
    private Integer orderStatus;//工单状态
    private String demandName;//需求名称
    private String meetingTime;//会议时间
    private String applyTime;//申请时间
    private String leaderGroup;//牵头小组
    private String leaderGroupCn;//牵头小组中文名
    private String leader;//牵头人
    private String leaderName;//牵头人姓名
    private String auditFinishDate;//审核完成日期
    private Set<String> auditeUsers;//审核人id集合
    private String auditorUsersCn;//审核人姓名字符串（以、分隔）

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDemandId() {
        return demandId;
    }

    public void setDemandId(String demandId) {
        this.demandId = demandId;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getProblemType() {
        return problemType;
    }

    public void setProblemType(String problemType) {
        this.problemType = problemType;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public Integer getProblemNum() {
        return problemNum;
    }

    public void setProblemNum(Integer problemNum) {
        this.problemNum = problemNum;
    }

    public String getFixFlag() {
        return fixFlag;
    }

    public void setFixFlag(String fixFlag) {
        this.fixFlag = fixFlag;
    }

    public String getFixDate() {
        return fixDate;
    }

    public void setFixDate(String fixDate) {
        this.fixDate = fixDate;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getItemTypeValue() {
        return ItemTypeValue;
    }

    public void setItemTypeValue(String itemTypeValue) {
        ItemTypeValue = itemTypeValue;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getDemandName() {
        return demandName;
    }

    public void setDemandName(String demandName) {
        this.demandName = demandName;
    }

    public String getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(String meetingTime) {
        this.meetingTime = meetingTime;
    }

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }

    public String getLeaderGroup() {
        return leaderGroup;
    }

    public void setLeaderGroup(String leaderGroup) {
        this.leaderGroup = leaderGroup;
    }

    public String getLeaderGroupCn() {
        return leaderGroupCn;
    }

    public void setLeaderGroupCn(String leaderGroupCn) {
        this.leaderGroupCn = leaderGroupCn;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getAuditFinishDate() {
        return auditFinishDate;
    }

    public void setAuditFinishDate(String auditFinishDate) {
        this.auditFinishDate = auditFinishDate;
    }

    public Set<String> getAuditeUsers() {
        return auditeUsers;
    }

    public void setAuditeUsers(Set<String> auditeUsers) {
        this.auditeUsers = auditeUsers;
    }

    public String getAuditorUsersCn() {
        return auditorUsersCn;
    }

    public void setAuditorUsersCn(String auditorUsersCn) {
        this.auditorUsersCn = auditorUsersCn;
    }
}
