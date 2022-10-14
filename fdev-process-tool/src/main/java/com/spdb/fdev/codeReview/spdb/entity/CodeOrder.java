package com.spdb.fdev.codeReview.spdb.entity;

import com.spdb.fdev.codeReview.spdb.dto.ApplyInfo;
import com.spdb.fdev.codeReview.spdb.dto.TaskEntity;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author liux81
 * @DATE 2021/11/9
 */
@Component
@Document("code_order")
public class CodeOrder extends BaseEntity{

    @Field("id")
    private String id;

    @Field("order_no")
    @ApiModelProperty("工单编号")
    private String orderNo;

    @Field("order_status")
    @ApiModelProperty("工单状态")
    private Integer orderStatus;//1待审核、2审核中、3需线下复审、4需会议复审、5初审通过（终态）、6线下复审通过（终态）、7会议复审通过（终态）、8拒绝（终态）

    @Field("systems")
    @ApiModelProperty("涉及系统id集合")
    private Set<String> systems;//由任务带出

    @Field("leader_group")
    @ApiModelProperty("牵头小组id")
    private String leaderGroup;//牵头人对应的第三层级的小组数据

    @Field("leader")
    @ApiModelProperty("牵头人id")
    private String leader;//默认取需求牵头人的第一个，用户可更改

    @Field("plan_product_date")
    @ApiModelProperty("计划投产日期")
    private String planProductDate;

    @Field("real_product_date")
    @ApiModelProperty("实际投产日期")
    private String realProductDate;

    @Field("audit_content")
    @ApiModelProperty("审核内容")
    private String auditContent;

    @Field("audit_result")
    @ApiModelProperty("审核结论")
    private String auditResult;

    @Field("audit_finish_time")
    @ApiModelProperty("审核完成时间")
    private String auditFinishTime;

    @Field("expect_audit_date")
    @ApiModelProperty("期望审核时间")
    private String expectAuditDate;

    @Field("product_problem")
    @ApiModelProperty("生产问题描述")
    private String productProblem;

    @Field("demand_id")
    @ApiModelProperty("对应需求id")
    private String demandId;

    @Field("task_ids")
    @ApiModelProperty("对应任务id集合")
    private Set<String> taskIds;

    @Field("apply_time")
    @ApiModelProperty("申请时间")
    private String applyTime;//yyyy-MM-dd hh:MM:ss

    @Field("audite_users")
    @ApiModelProperty("审核人id集合")
    private Set<String> auditeUsers;

    @Field("applyInfo")
    @ApiModelProperty("申请信息")
    private List<ApplyInfo> applyInfo;

    private String updateButton;//0可编辑部分字段，1可编辑全部字段，2仅可编辑生产问题描述，3不可编辑（终态下非代码审核角色不可编辑）

    private String deleteButton;//删除按钮，0亮，1工单终态下不可删除，2工单下有会议记录不可删除

    private String leaderGroupCn;//牵头组中文名

    private String leaderName;//牵头人姓名

    private String createUserNameCn;//创建人姓名

    private String systemNames;//涉及系统名称，用“，”隔开

    private String demandName;//需求编号_需求名称

    private List<TaskEntity> tasksInfo;//任务信息集合

    private Set<String> emailTo;//邮件通知对象的userId集合

    private String emailToNameCn;//邮件通知对象中文名拼接字符串

    private String applyRecheckButton;//申请复审按钮

    private String recheckRemindButton;//复审提醒按钮

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Set<String> getSystems() {
        return systems;
    }

    public void setSystems(Set<String> systems) {
        this.systems = systems;
    }

    public Set<String> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(Set<String> taskIds) {
        this.taskIds = taskIds;
    }

    public String getLeaderGroup() {
        return leaderGroup;
    }

    public void setLeaderGroup(String leaderGroup) {
        this.leaderGroup = leaderGroup;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getPlanProductDate() {
        return planProductDate;
    }

    public void setPlanProductDate(String planProductDate) {
        this.planProductDate = planProductDate;
    }

    public String getRealProductDate() {
        return realProductDate;
    }

    public void setRealProductDate(String realProductDate) {
        this.realProductDate = realProductDate;
    }

    public String getAuditContent() {
        return auditContent;
    }

    public void setAuditContent(String auditContent) {
        this.auditContent = auditContent;
    }

    public String getAuditResult() {
        return auditResult;
    }

    public void setAuditResult(String auditResult) {
        this.auditResult = auditResult;
    }

    public String getAuditFinishTime() {
        return auditFinishTime;
    }

    public void setAuditFinishTime(String auditFinishTime) {
        this.auditFinishTime = auditFinishTime;
    }

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }

    public String getProductProblem() {
        return productProblem;
    }

    public void setProductProblem(String productProblem) {
        this.productProblem = productProblem;
    }

    public String getDemandId() {
        return demandId;
    }

    public void setDemandId(String demandId) {
        this.demandId = demandId;
    }

    public String getUpdateButton() {
        return updateButton;
    }

    public void setUpdateButton(String updateButton) {
        this.updateButton = updateButton;
    }

    public String getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(String deleteButton) {
        this.deleteButton = deleteButton;
    }

    public String getLeaderGroupCn() {
        return leaderGroupCn;
    }

    public void setLeaderGroupCn(String leaderGroupCn) {
        this.leaderGroupCn = leaderGroupCn;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getSystemNames() {
        return systemNames;
    }

    public void setSystemNames(String systemNames) {
        this.systemNames = systemNames;
    }

    public String getDemandName() {
        return demandName;
    }

    public void setDemandName(String demandName) {
        this.demandName = demandName;
    }

    public List<TaskEntity> getTasksInfo() {
        return tasksInfo;
    }

    public void setTasksInfo(List<TaskEntity> tasksInfo) {
        this.tasksInfo = tasksInfo;
    }

    public String getCreateUserNameCn() {
        return createUserNameCn;
    }

    public void setCreateUserNameCn(String createUserNameCn) {
        this.createUserNameCn = createUserNameCn;
    }

    public Set<String> getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(Set<String> emailTo) {
        this.emailTo = emailTo;
    }

    public String getExpectAuditDate() {
        return expectAuditDate;
    }

    public void setExpectAuditDate(String expectAuditDate) {
        this.expectAuditDate = expectAuditDate;
    }

    public Set<String> getAuditeUsers() {
        return auditeUsers;
    }

    public void setAuditeUsers(Set<String> auditeUsers) {
        this.auditeUsers = auditeUsers;
    }

    public String getEmailToNameCn() {
        return emailToNameCn;
    }

    public void setEmailToNameCn(String emailToNameCn) {
        this.emailToNameCn = emailToNameCn;
    }

    public List<ApplyInfo> getApplyInfo() {
        return applyInfo;
    }

    public void setApplyInfo(List<ApplyInfo> applyInfo) {
        this.applyInfo = applyInfo;
    }

    public String getApplyRecheckButton() {
        return applyRecheckButton;
    }

    public void setApplyRecheckButton(String applyRecheckButton) {
        this.applyRecheckButton = applyRecheckButton;
    }

    public String getRecheckRemindButton() {
        return recheckRemindButton;
    }

    public void setRecheckRemindButton(String recheckRemindButton) {
        this.recheckRemindButton = recheckRemindButton;
    }
}
