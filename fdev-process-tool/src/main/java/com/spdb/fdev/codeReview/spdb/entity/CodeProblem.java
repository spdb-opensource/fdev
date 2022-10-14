package com.spdb.fdev.codeReview.spdb.entity;

import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;
import zipkin2.Call;

/**
 * @Author liux81
 * @DATE 2021/11/9
 */
@Component
@Document("code_problem")
public class CodeProblem extends BaseEntity {
    @Field("id")
    private String id;

    @Field("meeting_id")
    @ApiModelProperty("会议id")
    private String meetingId;

    @Field("problem")
    @ApiModelProperty("问题描述")
    private String problem;

    @Field("problem_type")
    @ApiModelProperty("问题类型")
    private String problemType;//issue缺陷/advice建议/risk风险

    @Field("item_type")
    @ApiModelProperty("问题项类型key")
    private String itemType;

    @Field("problem_num")
    @ApiModelProperty("问题次数")
    private Integer problemNum;

    @Field("fix_flag")
    @ApiModelProperty("是否已修复")
    private String fixFlag;//fixed已修复/notFixed未修复

    @Field("fix_date")
    @ApiModelProperty("修复日期")
    private String fixDate;

    @Field("remark")
    @ApiModelProperty("备注")
    private String remark;

    private String updateButton;//编辑按钮，0亮，1置灰（工单终态下不可编辑）
    private String deleteButton;//删除按钮，0亮，1置灰（工单终态下不可删除）
    private String ItemTypeValue;//问题项内容
    private String orderNo;//工单编号
    private String demandName;//需求名称
    private String meetingTime;//会议时间
    private String applyTime;//申请时间


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
}
