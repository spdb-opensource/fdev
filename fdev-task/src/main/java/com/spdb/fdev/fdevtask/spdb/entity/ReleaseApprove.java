package com.spdb.fdev.fdevtask.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @Author liux81
 * @DATE 2021/12/29
 */
@Document(collection = "release_approve")
@ApiModel(value = "合并release申请")
public class ReleaseApprove {

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @Indexed
    private String id;

    @Field("task_id")
    private String task_id;

    @Field("applicant")
    private String applicant;//申请人id

    @Field("auditor")
    private String auditor;//审核人id

    @Field("apply_time")
    private String apply_time;//申请时间

    @Field("audit_time")
    private String audit_time;//审核时间

    @Field("apply_desc")
    private String apply_desc;//申请描述 合并说明

    @Field("result_desc")
    private String result_desc;//结果描述

    @Field("status")
    private Integer status;//审核状态.0待审批、1通过、2拒绝

    @Field("source_branch")
    private String source_branch;//源分支

    @Field("target_branch")
    private String target_branch;//目标分支

    @Field("delete")
    private String delete;//del表示已删除

    /*需求功能问题 demandIssue
    删除注释 annotation
    修改代码冲突 updateClash
    代码审核修改 codeCheckUpdate
    兼容性修复 compatibilityRepair
    改挂投产窗口 updateProWindow
    环境问题 envIssue
    其他  other
    */
    @Field("merge_reason")
    private String merge_reason;//合并原因

    @Field("env")
    private String env;//合并环境 uat sit

    public String getMerge_reason() {
        return merge_reason;
    }

    public void setMerge_reason(String merge_reason) {
        this.merge_reason = merge_reason;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
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

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public String getAuditor() {
        return auditor;
    }

    public void setAuditor(String auditor) {
        this.auditor = auditor;
    }

    public String getApply_time() {
        return apply_time;
    }

    public void setApply_time(String apply_time) {
        this.apply_time = apply_time;
    }

    public String getAudit_time() {
        return audit_time;
    }

    public void setAudit_time(String audit_time) {
        this.audit_time = audit_time;
    }

    public String getApply_desc() {
        return apply_desc;
    }

    public void setApply_desc(String apply_desc) {
        this.apply_desc = apply_desc;
    }

    public String getResult_desc() {
        return result_desc;
    }

    public void setResult_desc(String result_desc) {
        this.result_desc = result_desc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSource_branch() {
        return source_branch;
    }

    public void setSource_branch(String source_branch) {
        this.source_branch = source_branch;
    }

    public String getTarget_branch() {
        return target_branch;
    }

    public void setTarget_branch(String target_branch) {
        this.target_branch = target_branch;
    }

    public String getDelete() {
        return delete;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }
}
