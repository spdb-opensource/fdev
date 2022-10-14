package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * @author dafeng
 * @date 2021/2/26 10:45
 */
public class PipelineVo {

    private String name;

    private String desc;

    private List<Stage> stages;

    private BindProject bindProject;

    private TriggerRules triggerRules;       //触发规则

    @ApiModelProperty("已关注该流水线的用户集合")
    private List<String> collected;


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

    public TriggerRules getTriggerRules() {
        return triggerRules;
    }

    public void setTriggerRules(TriggerRules triggerRules) {
        this.triggerRules = triggerRules;
    }

    public List<String> getCollected() {
        return collected;
    }

    public void setCollected(List<String> collected) {
        this.collected = collected;
    }

}
