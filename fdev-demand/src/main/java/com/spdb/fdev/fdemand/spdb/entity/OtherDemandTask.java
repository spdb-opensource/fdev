package com.spdb.fdev.fdemand.spdb.entity;

import com.spdb.fdev.fdemand.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Document(collection = Dict.OTHER_DEMAND_TASK)
public class OtherDemandTask {


    @Id
    private ObjectId _id;

    @Field("id")
    private String id;

    /** 任务编号 */
    @Indexed(unique = true)
    private String taskNum;

    /** 需求ID */
    private String demandId;

    /** 任务名称 */
    private String taskName;

    /** 任务类型 */
    private String taskType;

    /** 任务分类 */
    private String taskClassify;

    /** 项目/任务集 */
    private String prjNum;

    /** 项目/任务集名称 */
    private String planPrjName;

    /** 牵头单位 */
    private String headerUnitName;

    /** 牵头团队 */
    private String headerTeamName;

    /** 牵头小组 */
    private String leaderGroup;

    /** 牵头小组中文名 */
    private String leaderGroupName;

    /** 实施状态 notStart=未开始 going=进行中 done=已完成 delete=删除 */
    private String status;

    /** 任务负责人ID */
    private String taskLeaderId;

    /** 任务负责人中文姓名 */
    private String taskLeaderName;

    /** 厂商负责人ID */
    private String firmLeaderId;

    /** 厂商负责人中文姓名 */
    private String firmLeaderName;

    /** 计划启动日期 */
    private String planStartDate;

    /** 计划完成日期 */
    private String planDoneDate;

    /** 实际启动日期 */
    private String actualStartDate;

    /** 实际完成日期 */
    private String actualDoneDate;

    /** 创建时间 */
    private String createTime;

    /** 当前用户是否有权限编辑 权限校验 需求管理员 需求牵头人 属于自己的任务负责人、厂商负责人 */
    @Transient
    private Boolean isUpdate;

    /** 当前用户是否有权限删除 权限校验 需求牵头人*/
    @Transient
    private String isDelete;

    /** 行内预期工作量 */
    private Double expectOwnWorkload;

    /** 公司预期工作量 */
    private Double expectOutWorkload;

    public Double getExpectOwnWorkload() {
        return expectOwnWorkload;
    }

    public void setExpectOwnWorkload(Double expectOwnWorkload) {
        this.expectOwnWorkload = expectOwnWorkload;
    }

    public Double getExpectOutWorkload() {
        return expectOutWorkload;
    }

    public void setExpectOutWorkload(Double expectOutWorkload) {
        this.expectOutWorkload = expectOutWorkload;
    }

    public String getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(String isDelete) {
        this.isDelete = isDelete;
    }

    public Boolean getIsUpdate() {
        return isUpdate;
    }

    public void setIsUpdate(Boolean isUpdate) {
        this.isUpdate = isUpdate;
    }

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

    public String getDemandId() {
        return demandId;
    }

    public void setDemandId(String demandId) {
        this.demandId = demandId;
    }

    public String getPrjNum() {
        return prjNum;
    }

    public void setPrjNum(String prjNum) {
        this.prjNum = prjNum;
    }

    public String getPlanPrjName() {
        return planPrjName;
    }

    public void setPlanPrjName(String planPrjName) {
        this.planPrjName = planPrjName;
    }

    public String getTaskNum() {
        return taskNum;
    }

    public void setTaskNum(String taskNum) {
        this.taskNum = taskNum;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTaskClassify() {
        return taskClassify;
    }

    public void setTaskClassify(String taskClassify) {
        this.taskClassify = taskClassify;
    }

    public String getHeaderUnitName() {
        return headerUnitName;
    }

    public void setHeaderUnitName(String headerUnitName) {
        this.headerUnitName = headerUnitName;
    }

    public String getHeaderTeamName() {
        return headerTeamName;
    }

    public void setHeaderTeamName(String headerTeamName) {
        this.headerTeamName = headerTeamName;
    }

    public String getLeaderGroup() {
        return leaderGroup;
    }

    public void setLeaderGroup(String leaderGroup) {
        this.leaderGroup = leaderGroup;
    }

    public String getLeaderGroupName() {
        return leaderGroupName;
    }

    public void setLeaderGroupName(String leaderGroupName) {
        this.leaderGroupName = leaderGroupName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTaskLeaderId() {
        return taskLeaderId;
    }

    public void setTaskLeaderId(String taskLeaderId) {
        this.taskLeaderId = taskLeaderId;
    }

    public String getTaskLeaderName() {
        return taskLeaderName;
    }

    public void setTaskLeaderName(String taskLeaderName) {
        this.taskLeaderName = taskLeaderName;
    }

    public String getFirmLeaderId() {
        return firmLeaderId;
    }

    public void setFirmLeaderId(String firmLeaderId) {
        this.firmLeaderId = firmLeaderId;
    }

    public String getFirmLeaderName() {
        return firmLeaderName;
    }

    public void setFirmLeaderName(String firmLeaderName) {
        this.firmLeaderName = firmLeaderName;
    }

    public String getPlanStartDate() {
        return planStartDate;
    }

    public void setPlanStartDate(String planStartDate) {
        this.planStartDate = planStartDate;
    }

    public String getPlanDoneDate() {
        return planDoneDate;
    }

    public void setPlanDoneDate(String planDoneDate) {
        this.planDoneDate = planDoneDate;
    }

    public String getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(String actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public String getActualDoneDate() {
        return actualDoneDate;
    }

    public void setActualDoneDate(String actualDoneDate) {
        this.actualDoneDate = actualDoneDate;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
