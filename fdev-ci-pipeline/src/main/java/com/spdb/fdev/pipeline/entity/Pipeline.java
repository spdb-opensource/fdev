package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;
import java.util.Map;

@Document(collection = "pipeline")
public class Pipeline {

    @Field("pipelineId")
    @Indexed(unique = true)
    private String id;

    //等于最初版本的pipline_id
    @Field("nameId")
    private String nameId;

    //最新的版本为1，旧版本的为0，弃用删除的为0
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

    //开始创建时时间和更新时间一致
    @Field("createTime")
    private String createTime;

    @Field("updateTime")
    private String updateTime;

    @Field("stages")
    private List<Stage> stages;

    @Field("bindProject")
    private BindProject bindProject;

    @ApiModelProperty("已关注该流水线的用户集合")
    @Field("collected")
    private List<String> collected;

    @Field("triggerRules")
    private TriggerRules triggerRules;       //触发规则

    @Transient
    @ApiModelProperty("是否被当前用户收藏（0：否 1：是）")
    private String collectStatus;

    @Transient
    @ApiModelProperty("实例创建人")
    private String user;

    @ApiModelProperty("实例创建时间")
    @Field("buildTime")
    private String buildTime;

    @Transient
    @ApiModelProperty("是否可以编辑")
    private boolean updateRight;

    @Transient
    @ApiModelProperty("是否收藏")
    private Boolean collectedOrNot;

//    @Field("groupLineId")
//    private String groupLineId;             //绑定应用的条线id，老版fdev没有该信息，不隔离数据

    @Transient
    @ApiModelProperty("push触发保存提交记录")
    private String commitTitle;

    @Transient
    @ApiModelProperty("push触发保存提交记录Id")
    private String commitId;

    @Transient
    @ApiModelProperty("手动触发时运行变量")
    private List<Map> runVariables;

    @Field("fixedModeFlag")
    @ApiModelProperty(value = "true代表固定模式,false代表自由模式,默认存量都是自由模式")
    private Boolean fixedModeFlag = false;

    @Field("pipelineTemplateNameId")
    @ApiModelProperty(value = "流水线模板nameId")
    private String pipelineTemplateNameId;

    @Field("pipelineTemplateId")
    @ApiModelProperty(value = "流水线模板Id")
    private String pipelineTemplateId;

    public Pipeline() {
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

    public List<Stage> getStages() {
        return stages;
    }

    public void setStages(List<Stage> stages) {
        this.stages = stages;
    }

    public BindProject getBindProject() {
        return bindProject;
    }

    public void setBindProject(BindProject bindProject) {
        this.bindProject = bindProject;
    }

    public List<String> getCollected() {
        return collected;
    }

    public void setCollected(List<String> collected) {
        this.collected = collected;
    }

    public TriggerRules getTriggerRules() {
        return triggerRules;
    }

    public void setTriggerRules(TriggerRules triggerRules) {
        this.triggerRules = triggerRules;
    }

    public String getCollectStatus() {
        return collectStatus;
    }

    public void setCollectStatus(String collectStatus) {
        this.collectStatus = collectStatus;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

    public boolean isUpdateRight() {
        return updateRight;
    }

    public void setUpdateRight(boolean updateRight) {
        this.updateRight = updateRight;
    }

//    public String getGroupLineId() {
//        return groupLineId;
//    }
//
//    public void setGroupLineId(String groupLineId) {
//        this.groupLineId = groupLineId;
//    }
    public String getCommitTitle() {
        return commitTitle;
    }

    public void setCommitTitle(String commitTitle) {
        this.commitTitle = commitTitle;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public List<Map> getRunVariables() {
        return runVariables;
    }

    public void setRunVariables(List<Map> runVariables) {
        this.runVariables = runVariables;
    }

    public Boolean getFixedModeFlag() {
        return fixedModeFlag;
    }

    public void setFixedModeFlag(Boolean fixedModeFlag) {
        this.fixedModeFlag = fixedModeFlag;
    }

    public String getPipelineTemplateNameId() {
        return pipelineTemplateNameId;
    }

    public void setPipelineTemplateNameId(String pipelineTemplateNameId) {
        this.pipelineTemplateNameId = pipelineTemplateNameId;
    }

    public String getPipelineTemplateId() {
        return pipelineTemplateId;
    }

    public void setPipelineTemplateId(String pipelineTemplateId) {
        this.pipelineTemplateId = pipelineTemplateId;
    }

    public Boolean getCollectedOrNot() {
        return collectedOrNot;
    }

    public void setCollectedOrNot(Boolean collectedOrNot) {
        this.collectedOrNot = collectedOrNot;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "Pipeline{" +
                "id='" + id + '\'' +
                ", nameId='" + nameId + '\'' +
                ", status='" + status + '\'' +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", author=" + author +
                ", version='" + version + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", stages=" + stages +
                ", bindProject=" + bindProject +
                ", collected=" + collected +
                ", triggerRules=" + triggerRules +
                ", collectStatus='" + collectStatus + '\'' +
                ", user='" + user + '\'' +
                ", buildTime='" + buildTime + '\'' +
                ", updateRight=" + updateRight +
                ", collectedOrNot=" + collectedOrNot +
//                ", groupLineId='" + groupLineId + '\'' +
                ", commitTitle='" + commitTitle + '\'' +
                ", commitId='" + commitId + '\'' +
                ", runVariables=" + runVariables +
                ", fixedModeFlag=" + fixedModeFlag +
                ", pipelineTemplateNameId='" + pipelineTemplateNameId + '\'' +
                ", pipelineTemplateId='" + pipelineTemplateId + '\'' +
                '}';
    }
}
