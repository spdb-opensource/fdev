package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModelProperty;
import net.minidev.json.annotate.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "pipeline_template")
public class PipelineTemplate {

    @Id
    @JsonIgnore
    private ObjectId objectId;

    @Field("id")
    private String id;

    @Field("nameId")
    private String nameId;

    @Field("status")
    private String status;

    @Field("name")
    private String name;

    @Field("desc")
    private String desc;

    @Field("author")
    private Author author;

    @Field("version")
    private String version;

    @Field("updateTime")
    private String updateTime;

    @Field("stages")
    private List<Stage> stages;

    @Field("category")
    private Category category;

    @ApiModelProperty("是否可编辑0：否1：是")
    @Transient
    private String canEdit;

    @Field("visibleRange")
    @ApiModelProperty("可见范围有三种：private、public、group")
    private String visibleRange;

    @Field("groupId")
    @ApiModelProperty("用户组id")
    private String groupId;

    @Field("fixedModeFlag")
    @ApiModelProperty(value = "true代表固定模式,false代表自由模式,默认存量都是自由模式")
    private Boolean fixedModeFlag = false;

    public PipelineTemplate() {
    }

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public List<Stage> getStages() {
        return stages;
    }

    public void setStages(List<Stage> stages) {
        this.stages = stages;
    }

    public String getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(String canEdit) {
        this.canEdit = canEdit;
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

    public Boolean getFixedModeFlag() {
        return fixedModeFlag;
    }

    public void setFixedModeFlag(Boolean fixedModeFlag) {
        this.fixedModeFlag = fixedModeFlag;
    }
}
