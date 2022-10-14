package com.spdb.fdev.component.entity;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "component_issue")
public class ComponentIssue implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1865863043762006232L;

    @Id
    private ObjectId _id;
    @Field("id")
    private String id;
    @Field("component_id")
    private String component_id;
    @Field("title")
    private String title;
    @Field("desc")
    private String desc;
    @Field("assignee")
    private String assignee;
    @Field("feature_branch")
    private String feature_branch;
    @Field("target_version")
    private String target_version;
    @Field("stage")
    private String stage;
    @Field("due_date")
    private String due_date;
    @Field("create_date")
    private String create_date;

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public String getTarget_version() {
        return target_version;
    }

    public void setTarget_version(String target_version) {
        this.target_version = target_version;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getDue_date() {
        return due_date;
    }

    public void setDue_date(String due_date) {
        this.due_date = due_date;
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
