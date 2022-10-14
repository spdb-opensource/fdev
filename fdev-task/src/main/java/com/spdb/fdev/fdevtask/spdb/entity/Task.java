package com.spdb.fdev.fdevtask.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fdevtask.base.annotation.FiledTransConvert;
import com.spdb.fdev.fdevtask.base.enums.ComfirmReleaseEnum;
import com.spdb.fdev.fdevtask.base.enums.MountStatusEnum;
import com.spdb.fdev.fdevtask.base.enums.PriorityEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

/**
 * @program: fdev-process
 * @description:任务
 * @author: c-jiangl2
 * @create: 2021-01-22 16:33
 **/

@Document(collection = "process_task")
@ApiModel(value = "流程任务")
public class Task {

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "任务ID")
    private String id;

    @Field("name")
    @ApiModelProperty(value = "任务名称")
    @FiledTransConvert(rename = "任务名称")
    private String name;

    @Field("desc")
    @ApiModelProperty(value = "任务描述")
    @FiledTransConvert(rename = "任务描述")
    private String desc;

    @Field("status")
    @ApiModelProperty(value = "实际任务状态值")
    @FiledTransConvert(rename = "实际任务状态值")
    private String status;

    @Field("priority")
    @ApiModelProperty(value = "任务优先级")
    @FiledTransConvert(rename = "任务优先级",enumConverter = PriorityEnum.class)
    private Integer priority;

    @Field("parentId")
    @ApiModelProperty(value = "关联父级任务id")
    @FiledTransConvert(rename = "关联父级任务id")
    private String parentId;

    @Field("tagList")
    @ApiModelProperty(value = "任务标签")
    @FiledTransConvert(rename = "任务标签")
    private String[] tagList;

    @Field("requirementId")
    @ApiModelProperty(value = "需求id")
    @FiledTransConvert(rename = "需求id")
    private String requirementId;

    @Field("relatedApplication")
    @ApiModelProperty(value = "关联应用")
    @FiledTransConvert(rename = "关联应用")
    private String relatedApplication;

    @Field("implUnitId")
    @ApiModelProperty(value = "研发单元id")
    @FiledTransConvert(rename = "研发单元")
    private String implUnitId;

    @Field("planStartDate")
    @ApiModelProperty(value = "计划启动时间")
    @FiledTransConvert(rename = "计划启动时间")
    private String planStartDate;

    @Field("planEndDate")
    @ApiModelProperty(value = "计划结束时间")
    @FiledTransConvert(rename = "计划结束时间")
    private String planEndDate;

    @Field("startDate")
    @ApiModelProperty(value = "实际启动时间")
    @FiledTransConvert(rename = "实际启动时间")
    private String startDate;

    @Field("endDate")
    @ApiModelProperty(value = "实际结束时间")
    @FiledTransConvert(rename = "实际结束时间")
    private String endDate;

    @Field("createUserId")
    @ApiModelProperty(value = "创建人id")
    private String createUserId;

    @Field("createTime")
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @Field("updateTime")
    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    @Field("assigneeList")
    @ApiModelProperty(value = "受理人列表")
    private String[] assigneeList;

    @Field("teamId")
    @ApiModelProperty(value = "团队id")
    private String teamId;

    @Field("newDoc")
    @ApiModelProperty(value = "新文档数据")
    private List<Map<String,String>> newDoc;

    @Field("branchName")
    @ApiModelProperty(value = "分支名称")
    @FiledTransConvert(rename = "分支名称")
    private String branchName;

    @Field("branchType")
    @ApiModelProperty(value = "0-新建 1-关联")
    @FiledTransConvert(rename = "分支类型")
    private Integer branchType;

    @Field("functionPoint")
    @ApiModelProperty(value = "功能点")
    @FiledTransConvert(rename = "功能点")
    private String functionPoint;

    @Field("workload")
    @ApiModelProperty(value = "工作量")
    @FiledTransConvert(rename = "工作量")
    private String workload;

    @Field("requirementPlan")
    @ApiModelProperty(value = "需求计划")
    @FiledTransConvert(rename = "需求计划")
    private String requirementPlan;

    @Field("delete")
    @ApiModelProperty(value = "0-未删除 1-删除")
    private Integer delete;

    @Field("assigneeGroupId")
    @ApiModelProperty(value = "执行人员小组id")
    private String assigneeGroupId;

    @Field("versionId")
    @ApiModelProperty(value = "版本id")
    @FiledTransConvert(rename = "挂载版本")
    private String versionId;

    @Field("mountStatus")
    @ApiModelProperty(value = "挂载状态")
    @FiledTransConvert(rename = "挂载状态" ,enumConverter = MountStatusEnum.class)
    private Integer mountStatus;

    @Field("commitSha")
    @ApiModelProperty(value = "任务关联分支时，源分支最新版本commitSha")
    private String commitSha;

    @Field("confirmRelease")
    @ApiModelProperty(value = "确认发布 0-未确认 1-确认发布")
    @FiledTransConvert(rename = "发布状态" , enumConverter = ComfirmReleaseEnum.class)
    private String confirmRelease;

    @Field("processId")
    @ApiModelProperty(value = "流程id")
    private String processId;

    @Field("mountSameFBConfirm")
    @ApiModelProperty(value = "挂载版本相同开发分支确认 0-未确认 1-确认")
    @FiledTransConvert(rename = "挂载版本相同开发分支确认", enumConverter = ComfirmReleaseEnum.class)
    private String mountSameFBConfirm;

    @Field("bkInfo")
    @ApiModelProperty(value = "蓝鲸信息")
    private Map<String,Object> bkInfo;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public Task() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String[] getTagList() {
        return tagList;
    }

    public void setTagList(String[] tagList) {
        this.tagList = tagList;
    }

    public String getRelatedApplication() {
        return relatedApplication;
    }

    public void setRelatedApplication(String relatedApplication) {
        this.relatedApplication = relatedApplication;
    }

    public String getImplUnitId() {
        return implUnitId;
    }

    public void setImplUnitId(String implUnitId) {
        this.implUnitId = implUnitId;
    }

    public String getPlanStartDate() {
        return planStartDate;
    }

    public void setPlanStartDate(String planStartDate) {
        this.planStartDate = planStartDate;
    }

    public String getPlanEndDate() {
        return planEndDate;
    }

    public void setPlanEndDate(String planEndDate) {
        this.planEndDate = planEndDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String[] getAssigneeList() {
        return assigneeList;
    }

    public void setAssigneeList(String[] assigneeList) {
        this.assigneeList = assigneeList;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public List<Map<String, String>> getNewDoc() {
        return newDoc;
    }

    public void setNewDoc(List<Map<String, String>> newDoc) {
        this.newDoc = newDoc;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(String requirementId) {
        this.requirementId = requirementId;
    }

    public String getFunctionPoint() {
        return functionPoint;
    }

    public void setFunctionPoint(String functionPoint) {
        this.functionPoint = functionPoint;
    }

    public String getWorkload() {
        return workload;
    }

    public void setWorkload(String workload) {
        this.workload = workload;
    }

    public String getRequirementPlan() {
        return requirementPlan;
    }

    public void setRequirementPlan(String requirementPlan) {
        this.requirementPlan = requirementPlan;
    }

    public Integer getDelete() {
        return delete;
    }

    public void setDelete(Integer delete) {
        this.delete = delete;
    }

    public String getAssigneeGroupId() {
        return assigneeGroupId;
    }

    public void setAssigneeGroupId(String assigneeGroupId) {
        this.assigneeGroupId = assigneeGroupId;
    }

    public Integer getBranchType() {
        return branchType;
    }

    public void setBranchType(Integer branchType) {
        this.branchType = branchType;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public Integer getMountStatus() {
        return mountStatus;
    }

    public void setMountStatus(Integer mountStatus) {
        this.mountStatus = mountStatus;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public String getConfirmRelease() {
        return confirmRelease;
    }

    public void setConfirmRelease(String confirmRelease) {
        this.confirmRelease = confirmRelease;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getMountSameFBConfirm() {
        return mountSameFBConfirm;
    }

    public void setMountSameFBConfirm(String mountSameFBConfirm) {
        this.mountSameFBConfirm = mountSameFBConfirm;
    }

    public Map getBkInfo() {
        return bkInfo;
    }

    public void setBkInfo(Map bkInfo) {
        this.bkInfo = bkInfo;
    }
}
