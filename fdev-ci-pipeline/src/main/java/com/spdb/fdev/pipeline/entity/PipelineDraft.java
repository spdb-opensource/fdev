package com.spdb.fdev.pipeline.entity;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Document(collection = "pipeline_draft")
public class PipelineDraft {

    @Field("id")
    private String id;

    @Field("name")
    private String name;

    @Field("desc")
    private String desc;

    @Field("authorId")
    @Indexed(unique = true)
    private String authorId;

    @Field("updateTime")
    private String updateTime;

    @Field("bindProject")
    private BindProject bindProject;

    @Field("stages")
    private List<Stage> stages;

    @Field("triggerMode")
    private List<String> triggerMode;

    @Field("envInfo")
    private EnvInfo envInfo;

    @Field("pipelineOrTemplate")
    private String pipelineOrTemplate;

    @Field("label")
    private List<String> label;

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<String> getTriggerMode() {
        return triggerMode;
    }

    public void setTriggerMode(List<String> triggerMode) {
        this.triggerMode = triggerMode;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public BindProject getBindProject() {
        return bindProject;
    }

    public void setBindProject(BindProject bindProject) {
        this.bindProject = bindProject;
    }

    public List<Stage> getStages() {
        return stages;
    }

    public void setStages(List<Stage> stages) {
        this.stages = stages;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public EnvInfo getEnvInfo() {
        return envInfo;
    }

    public void setEnvInfo(EnvInfo envInfo) {
        this.envInfo = envInfo;
    }

    public String getPipelineOrTemplate() {
        return pipelineOrTemplate;
    }

    public void setPipelineOrTemplate(String pipelineOrTemplate) {
        this.pipelineOrTemplate = pipelineOrTemplate;
    }

    public List<String> getLabel() {
        return label;
    }

    public void setLabel(List<String> label) {
        this.label = label;
    }
}
