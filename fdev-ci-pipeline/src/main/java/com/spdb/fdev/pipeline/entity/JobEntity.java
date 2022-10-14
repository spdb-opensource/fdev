package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModelProperty;
import org.checkerframework.common.aliasing.qual.Unique;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;

/**
 *
 * job 任务的实体
 *
 */
@Document("jobEntity")
public class JobEntity {

    @Unique
    @Field("jobId")
    private String jobId;       //任务id

    @Field("jobName")
    private String name;     //任务名称

    @Field("jobDesc")
    private String desc;        //任务描述

    @Field("nameId")
    private String nameId;      //任务的不变id

    @Field("runnerClusterId")
    private String runnerClusterId;  //构建集群ID

    @Field("image")
    private Images image;  //工作镜像

    @Field("author")
    private Author author;      //作者

    @Field("status")
    private String status;      //状态  0废弃，1可用

    @Field("steps")
    private List<Step> steps;         //任务步骤

    @Field("category")
    private Category category;  //任务分类

    @Field("createTime")
    private String createTime;  //创建时间

    @Field("updateTime")
    private String updateTime;  //更新时间

    @Field("version")
    private String version;  //版本

    @Field("visibleRange")
    private String visibleRange;  //可见范围有三种：private、public、group

    @Field("groupId")
    private String groupId;  //用户组id

    @ApiModelProperty("是否可编辑0：否1：是")
    @Transient
    private String canEdit;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    public String getRunnerClusterId() {
        return runnerClusterId;
    }

    public void setRunnerClusterId(String runnerClusterId) {
        this.runnerClusterId = runnerClusterId;
    }

    public Images getImage() {
        return image;
    }

    public void setImage(Images image) {
        this.image = image;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVisibleRange() {
        return visibleRange;
    }

    public void setVisibleRange(String visibleRange) {
        this.visibleRange = visibleRange;
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
