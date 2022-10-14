package com.spdb.fdev.component.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;

@Component
@Document(collection = "mpass_release_issue")
public class MpassReleaseIssue {

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
     * title
     */
    @Field("title")
    private String title;

    /**
     * 版本发布管理员
     */
    @Field("manager")
    private HashSet<Map<String, String>> manager;

    /**
     * 优化需求类型
     * （0：功能新增，1：bug修复）
     */
    @Field("issue_type")
    private String issue_type;

    /**
     * 目标分支
     */
    @Field("feature_branch")
    private String feature_branch;

    /**
     * 预设版本
     */
    @Field("predict_version")
    private String predict_version;

    /**
     * 计划完成日期
     */
    @Field("due_date")
    private String due_date;

    /**
     * 创建时间
     */
    @Field("create_date")
    private String create_date;

    /**
     * 需求描述
     */
    @Field("desc")
    private String desc;


    @Field("release_node_name")
    private String release_node_name;

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

    public String getComponent_id() {
        return component_id;
    }

    public void setComponent_id(String component_id) {
        this.component_id = component_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public HashSet<Map<String, String>> getManager() {
        return manager;
    }

    public void setManager(HashSet<Map<String, String>> manager) {
        this.manager = manager;
    }

    public String getIssue_type() {
        return issue_type;
    }

    public void setIssue_type(String issue_type) {
        this.issue_type = issue_type;
    }

    public String getFeature_branch() {
        return feature_branch;
    }

    public void setFeature_branch(String feature_branch) {
        this.feature_branch = feature_branch;
    }

    public String getPredict_version() {
        return predict_version;
    }

    public void setPredict_version(String predict_version) {
        this.predict_version = predict_version;
    }

    public String getDue_date() {
        return due_date;
    }

    public void setDue_date(String due_date) {
        this.due_date = due_date;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getRelease_node_name() {
        return release_node_name;
    }

    public void setRelease_node_name(String release_node_name) {
        this.release_node_name = release_node_name;
    }
}
