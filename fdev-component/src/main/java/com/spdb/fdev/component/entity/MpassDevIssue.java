package com.spdb.fdev.component.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "mpass_dev_issue")
public class MpassDevIssue {

    @Id
    @JsonIgnore
    private ObjectId _id;
    @Field("id")
    private String id;

    /**
     * 关联组件字段
     */
    @Field("component_id")
    private String component_id;

    /**
     * 关联issue
     */
    @Field("issue_id")
    private String issue_id;

    /**
     * 开发人员
     */
    @Field("assignee")
    private String assignee;

    /**
     * 开发分支
     */
    @Field("feature_branch")
    private String feature_branch;

    /**
     * 计划SIT日期
     */
    @Field("sit_date")
    private String sit_date;

    /**
     * 计划UAT日期
     */
    @Field("uat_date")
    private String uat_date;

    /**
     * 计划完成日期
     */
    @Field("due_date")
    private String due_date;

    /**
     * 当前阶段
     */
    @Field("stage")
    private String stage;

    /**
     * 创建时间
     */
    @Field("create_date")
    private String create_date;

    /**
     * 描述
     */
    @Field("desc")
    private String desc;

    public String getComponent_id() {
        return component_id;
    }

    public void setComponent_id(String component_id) {
        this.component_id = component_id;
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

    public String getIssue_id() {
        return issue_id;
    }

    public void setIssue_id(String issue_id) {
        this.issue_id = issue_id;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getFeature_branch() {
        return feature_branch;
    }

    public void setFeature_branch(String feature_branch) {
        this.feature_branch = feature_branch;
    }

    public String getDue_date() {
        return due_date;
    }

    public void setDue_date(String due_date) {
        this.due_date = due_date;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSit_date() {
        return sit_date;
    }

    public void setSit_date(String sit_date) {
        this.sit_date = sit_date;
    }

    public String getUat_date() {
        return uat_date;
    }

    public void setUat_date(String uat_date) {
        this.uat_date = uat_date;
    }
}
