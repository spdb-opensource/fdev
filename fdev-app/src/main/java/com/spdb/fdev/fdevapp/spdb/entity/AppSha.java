package com.spdb.fdev.fdevapp.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.LinkedHashSet;

/**
 * @author xxx
 * @date 2019/7/1 17:10
 */
@Document(collection = "app-sha")
@ApiModel(value = "分支Sha")
public class AppSha {
    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;
    @Field("id")
    @ApiModelProperty(value = "id")
    private String id;
    @Indexed(unique = true)
    @ApiModelProperty(value = "gitlab项目ID")
    private Integer gitlab_id;
    @Field("sha")
    @ApiModelProperty(value = "分支sha值")
    private LinkedHashSet<String> sha;
    @Field("branch")
    @ApiModelProperty(value = "应用分支名")
    private String branch;
    @Field("ctime")
    private String ctime;
    @Field("utime")
    private String utime;

    public AppSha() {
    }

    public AppSha(ObjectId _id, String id, Integer gitlab_id, LinkedHashSet sha, String branch, String ctime, String utime) {
        this._id = _id;
        this.id = id;
        this.gitlab_id = gitlab_id;
        this.sha = sha;
        this.branch = branch;
        this.ctime = ctime;
        this.utime = utime;
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

    public Integer getGitlab_id() {
        return gitlab_id;
    }

    public void setGitlab_id(Integer gitlab_id) {
        this.gitlab_id = gitlab_id;
    }

    public LinkedHashSet getSha() {
        return sha;
    }

    public void setSha(LinkedHashSet sha) {
        this.sha = sha;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getUtime() {
        return utime;
    }

    public void setUtime(String utime) {
        this.utime = utime;
    }

    @Override
    public String toString() {
        return "AppSha{" +
                "_id=" + _id +
                ", id='" + id + '\'' +
                ", gitlab_id=" + gitlab_id +
                ", sha=" + sha +
                ", branch='" + branch + '\'' +
                ", ctime='" + ctime + '\'' +
                ", utime='" + utime + '\'' +
                '}';
    }
}
