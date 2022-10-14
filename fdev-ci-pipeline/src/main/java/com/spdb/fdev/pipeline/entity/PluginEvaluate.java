package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("pluginEvaluate")
public class PluginEvaluate {

    @Field("pluginEvaluateId")
    private String pluginEvaluateId;

    @Field("nameId")
    @ApiModelProperty("插件唯一标识")
    private String nameId;

    @Field("userDifficulty")
    @ApiModelProperty("操作复杂度评分")
    private Integer operationDifficulty;

    @Field("robustness")
    @ApiModelProperty("插件健壮性评分")
    private Integer robustness;

    @Field("logClarity")
    @ApiModelProperty("插件日志清晰度评分")
    private Integer logClarity;

    @Field("average")
    @ApiModelProperty("三种评分的平均分,相当于综合评分")
    private Double average;

    @Field("desc")
    @ApiModelProperty("用户评价描述")
    private String desc;

    @Field("author")
    @ApiModelProperty("评价的用户")
    private Author author;

    public String getPluginEvaluateId() {
        return pluginEvaluateId;
    }

    public void setPluginEvaluateId(String pluginEvaluateId) {
        this.pluginEvaluateId = pluginEvaluateId;
    }

    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    public Integer getOperationDifficulty() {
        return operationDifficulty;
    }

    public void setOperationDifficulty(Integer operationDifficulty) {
        this.operationDifficulty = operationDifficulty;
    }

    public Integer getRobustness() {
        return robustness;
    }

    public void setRobustness(Integer robustness) {
        this.robustness = robustness;
    }

    public Integer getLogClarity() {
        return logClarity;
    }

    public void setLogClarity(Integer logClarity) {
        this.logClarity = logClarity;
    }

    public Double getAverage() {
        return average;
    }

    public void setAverage(Double average) {
        this.average = average;
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
}
