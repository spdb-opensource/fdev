package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document("runnerCluster")
public class RunnerCluster implements Serializable {        //runner集群

    @Field("runnerClusterId")
    private String runnerClusterId;         //runner集群的id

    @Field("runnerClusterName")
    private String runnerClusterName;       //runner集群的中文名

    @Field("nameId")
    private String nameId;                  //唯一id（更新不会更新的id）

    @Field("runnerClusterNameEn")
    private String runnerClusterNameEn;      //runner集群的英文名

    @Field("author")
    private Author author;                    //用户

    @Field("platform")
    private String platform;                //runner集群的平台

    @Field("runnerList")
    private List<Map<String, Object>> runnerList;            //存放runner的id和name

    @Field("status")
    private String status;                  //1使用，0废弃

    @Field("createTime")
    private String createTime;

    @Field("updateTime")
    private String updateTime;

    @Field("isVisible")
    private Boolean isVisible;              //是否可见

    @Field("active")
    private Boolean active;                 //是否处于工作中  true 工作中，false 没在工作

    @Field("runnerNum")
    private Integer runnerNum;

    @Field("groupId")
    private String groupId;      //组id

    @ApiModelProperty("是否可编辑0：否1：是")
    @Transient
    private String canEdit;

    public String getRunnerClusterId() {
        return runnerClusterId;
    }

    public void setRunnerClusterId(String runnerClusterId) {
        this.runnerClusterId = runnerClusterId;
    }

    public String getRunnerClusterName() {
        return runnerClusterName;
    }

    public void setRunnerClusterName(String runnerClusterName) {
        this.runnerClusterName = runnerClusterName;
    }

    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    public String getRunnerClusterNameEn() {
        return runnerClusterNameEn;
    }

    public void setRunnerClusterNameEn(String runnerClusterNameEn) {
        this.runnerClusterNameEn = runnerClusterNameEn;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public List getRunnerList() {
        return runnerList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setRunnerList(List<Map<String, Object>> runnerList) {
        this.runnerList = runnerList;
    }

    public Boolean getVisible() {
        return isVisible;
    }

    public void setVisible(Boolean visible) {
        isVisible = visible;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getRunnerNum() {
        return runnerNum;
    }

    public void setRunnerNum(Integer runnerNum) {
        this.runnerNum = runnerNum;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(String canEdit) {
        this.canEdit = canEdit;
    }
}
