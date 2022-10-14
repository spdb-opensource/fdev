package com.spdb.fdev.component.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Document(collection = "baseimage_issue")
public class BaseImageIssue implements Serializable {
    @Id
    @JsonIgnore
    private ObjectId _id;
    @Field("id")
    private String id;
    /**
     * 镜像名称
     */
    @Field("name")
    private String name;

    /**
     * 标题
     */
    @Field("title")
    private String title;

    /**
     * 需求描述
     */
    @Field("desc")
    private String desc;

    /**
     * 开发分支名称
     */
    @Field("branch")
    private String branch;

    /**
     * 开发人员
     */
    @Field("assignee")
    private String assignee;

    /**
     * 开发阶段
     */
    @Field("stage")
    private String stage;

    /**
     * 创建时间
     */
    @Field("create_date")
    private String create_date;

    /**
     * 计划完成日期
     */
    @Field("due_date")
    private String due_date;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
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

    public String getDue_date() {
        return due_date;
    }

    public void setDue_date(String due_date) {
        this.due_date = due_date;
    }
}
