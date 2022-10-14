package com.spdb.fdev.fdemand.spdb.entity;

import com.spdb.fdev.fdemand.base.dict.Dict;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Document(collection = Dict.FDEV_UNIT_APPROVE)
public class FdevUnitApprove {

    @Id
    private ObjectId _id;

    @Field("id")
    private String id;

    @ApiModelProperty(value = "研发单元编号")
    private String fdevUnitNo;

    @ApiModelProperty(value = "研发单元内容")
    @Transient
    private String fdevUnitName;

    @ApiModelProperty(value = "需求ID")
    private String demandId;

    @ApiModelProperty(value = "需求编号")
    @Transient
    private String demandNo;

    @Transient
    @ApiModelProperty(value = "需求名称")
    private String demandName;

    @Transient
    @ApiModelProperty(value = "研发单元状态")
    private Integer fdevUnitState;

    @Transient
    @ApiModelProperty(value = "研发单元特殊状态")
    private Integer fdevUnitSpecialState;

    @Transient
    @ApiModelProperty(value = "牵头人id")
    private List<String> fdevUnitLeaderId;

    @Transient
    @ApiModelProperty(value = "牵头人信息")
    private List<UserInfo> fdevUnitLeaderInfo;

    @Transient
    @ApiModelProperty(value = "计划启动日期")
    private String planStartDate;

    @Transient
    @ApiModelProperty(value = "计划提交内测日期")
    private String planInnerTestDate;

    @Transient
    @ApiModelProperty(value = "计划提交用户测试日期")
    private String planTestDate;

    @Transient
    @ApiModelProperty(value = "计划用户测试完成日期")
    private String planTestFinishDate;

    @Transient
    @ApiModelProperty(value = "计划投产日期")
    private String planProductDate;

    @ApiModelProperty(value = "审批类型")// devApprove=开发审批 overdueApprove=超期审批 dev&overdue=开发审批&超期审批
    private String approveType;

    @ApiModelProperty(value = "超期类别")
    private String overdueType;

    @ApiModelProperty(value = "申请原因")
    private String overdueReason;

    @ApiModelProperty(value = "审批状态") /*wait=待审批，pass=通过，reject=拒绝*/
    private String approveState;

    @Transient
    @ApiModelProperty(value = "所属小组id")
    private String groupId;

    @Transient
    @ApiModelProperty(value = "所属小组名称")
    private String groupName;

    @ApiModelProperty(value = "条线id")
    private String sectionId;

    @ApiModelProperty(value = "条线名称")
    @Transient
    private String sectionName;

    @ApiModelProperty(value = "申请人id")
    private String applicantId;

    @ApiModelProperty(value = "申请人名称")
    @Transient
    private String applicantName;

    @ApiModelProperty(value = "审批人id")
    private String approverId;

    @ApiModelProperty(value = "审批人名称")
    @Transient
    private String approverName;

    @ApiModelProperty(value = "申请审批时间")
    private String applyTime;

    @ApiModelProperty(value = "审批时间")
    private String approveTime;

    @ApiModelProperty(value = "审批说明")
    private String approveReason;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApproveType() {
        return approveType;
    }

    public void setApproveType(String approveType) {
        this.approveType = approveType;
    }

    public Integer getFdevUnitSpecialState() {
        return fdevUnitSpecialState;
    }

    public void setFdevUnitSpecialState(Integer fdevUnitSpecialState) {
        this.fdevUnitSpecialState = fdevUnitSpecialState;
    }

    public String getFdevUnitName() {
        return fdevUnitName;
    }

    public void setFdevUnitName(String fdevUnitName) {
        this.fdevUnitName = fdevUnitName;
    }

    public String getDemandNo() {
        return demandNo;
    }

    public void setDemandNo(String demandNo) {
        this.demandNo = demandNo;
    }

    public String getDemandName() {
        return demandName;
    }

    public void setDemandName(String demandName) {
        this.demandName = demandName;
    }

    public Integer getFdevUnitState() {
        return fdevUnitState;
    }

    public void setFdevUnitState(Integer fdevUnitState) {
        this.fdevUnitState = fdevUnitState;
    }

    public List<String> getFdevUnitLeaderId() {
        return fdevUnitLeaderId;
    }

    public void setFdevUnitLeaderId(List<String> fdevUnitLeaderId) {
        this.fdevUnitLeaderId = fdevUnitLeaderId;
    }

    public List<UserInfo> getFdevUnitLeaderInfo() {
        return fdevUnitLeaderInfo;
    }

    public void setFdevUnitLeaderInfo(List<UserInfo> fdevUnitLeaderInfo) {
        this.fdevUnitLeaderInfo = fdevUnitLeaderInfo;
    }

    public String getPlanStartDate() {
        return planStartDate;
    }

    public void setPlanStartDate(String planStartDate) {
        this.planStartDate = planStartDate;
    }

    public String getPlanInnerTestDate() {
        return planInnerTestDate;
    }

    public void setPlanInnerTestDate(String planInnerTestDate) {
        this.planInnerTestDate = planInnerTestDate;
    }

    public String getPlanTestDate() {
        return planTestDate;
    }

    public void setPlanTestDate(String planTestDate) {
        this.planTestDate = planTestDate;
    }

    public String getPlanTestFinishDate() {
        return planTestFinishDate;
    }

    public void setPlanTestFinishDate(String planTestFinishDate) {
        this.planTestFinishDate = planTestFinishDate;
    }

    public String getPlanProductDate() {
        return planProductDate;
    }

    public void setPlanProductDate(String planProductDate) {
        this.planProductDate = planProductDate;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getFdevUnitNo() {
        return fdevUnitNo;
    }

    public void setFdevUnitNo(String fdevUnitNo) {
        this.fdevUnitNo = fdevUnitNo;
    }

    public String getDemandId() {
        return demandId;
    }

    public void setDemandId(String demandId) {
        this.demandId = demandId;
    }

    public String getOverdueType() {
        return overdueType;
    }

    public void setOverdueType(String overdueType) {
        this.overdueType = overdueType;
    }

    public String getOverdueReason() {
        return overdueReason;
    }

    public void setOverdueReason(String overdueReason) {
        this.overdueReason = overdueReason;
    }

    public String getApproveState() {
        return approveState;
    }

    public void setApproveState(String approveState) {
        this.approveState = approveState;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }

    public String getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(String approveTime) {
        this.approveTime = approveTime;
    }

    public String getApproveReason() {
        return approveReason;
    }

    public void setApproveReason(String approveReason) {
        this.approveReason = approveReason;
    }
}
