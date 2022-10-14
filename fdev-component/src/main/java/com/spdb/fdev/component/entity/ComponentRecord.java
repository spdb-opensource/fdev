package com.spdb.fdev.component.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "component_record")
public class ComponentRecord implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3774399655878722006L;
    @Id
    @JsonIgnore
    private ObjectId _id;
    @Field("id")
    private String id;
    @Field("component_id")
    private String component_id;
    @Field("version")
    private String version;
    @Field("date")
    private String date;
    @Field("update_user")
    private String update_user;
    @Field("release_log")
    private String release_log;
    @Field("jdk_version")
    private String jdk_version;
    @Field("type")
    private String type;
    @Field("packagetype")
    private String packagetype;
    @Field("pipid")
    private String pipid;
    @Field("branch")
    private String branch;
    @Field("issue_id")
    private String issue_id;

    public ComponentRecord() {
    }

    public ComponentRecord(String component_id, String version, String date) {
        this.component_id = component_id;
        this.version = version;
        this.date = date;
    }

    public ComponentRecord(String component_id, String version, String date, String update_user, String packagetype, String type) {
        this.component_id = component_id;
        this.version = version;
        this.date = date;
        this.update_user = update_user;
        this.packagetype = packagetype;
        this.type = type;
    }


    public ComponentRecord(String component_id, String version, String date, String update_user, String release_log, String jdk_version, String packagetype) {
        this.component_id = component_id;
        this.version = version;
        this.date = date;
        this.update_user = update_user;
        this.release_log = release_log;
        this.jdk_version = jdk_version;
        this.packagetype = packagetype;
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

    public String getComponent_id() {
        return component_id;
    }

    public void setComponent_id(String component_id) {
        this.component_id = component_id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUpdate_user() {
        return update_user;
    }

    public void setUpdate_user(String update_user) {
        this.update_user = update_user;
    }

    public String getRelease_log() {
        return release_log;
    }

    public void setRelease_log(String release_log) {
        this.release_log = release_log;
    }

    public String getJdk_version() {
        return jdk_version;
    }

    public void setJdk_version(String jdk_version) {
        this.jdk_version = jdk_version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPackagetype() {
        return packagetype;
    }

    public void setPackagetype(String packagetype) {
        this.packagetype = packagetype;
    }

    public String getPipid() {
        return pipid;
    }

    public void setPipid(String pipid) {
        this.pipid = pipid;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getIssue_id() {
        return issue_id;
    }

    public void setIssue_id(String issue_id) {
        this.issue_id = issue_id;
    }
}
