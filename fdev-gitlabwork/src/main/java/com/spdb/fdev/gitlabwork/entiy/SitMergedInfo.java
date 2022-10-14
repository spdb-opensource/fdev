package com.spdb.fdev.gitlabwork.entiy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document("sitmerged")
public class SitMergedInfo implements Serializable {

    private static final long serialVersionUID = 2150437191823504126L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field
    public String id;

    @Field
    public String appName;

    @Field
    public String gitUrl;

    @Field
    public String sitBranch;

    @Field
    public String mergeDate;

    @Field
    public String sourceBranch;

    @Field
    public String mergeId;

    public SitMergedInfo() {
    }

    public SitMergedInfo(String id, String appName, String gitUrl, String sitBranch, String mergeDate, String sourceBranch, String mergeId) {
        this.id = id;
        this.appName = appName;
        this.gitUrl = gitUrl;
        this.sitBranch = sitBranch;
        this.mergeDate = mergeDate;
        this.sourceBranch = sourceBranch;
        this.mergeId = mergeId;
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

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getSitBranch() {
        return sitBranch;
    }

    public void setSitBranch(String sitBranch) {
        this.sitBranch = sitBranch;
    }

    public String getMergeDate() {
        return mergeDate;
    }

    public void setMergeDate(String mergeDate) {
        this.mergeDate = mergeDate;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public String getMergeId() {
        return mergeId;
    }

    public void setMergeId(String mergeId) {
        this.mergeId = mergeId;
    }
}
