package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class JobExePlugin implements Serializable {

    private static final long serialVersionUID = 2918846622577537424L;

    @Field("plugin_code")
    @ApiModelProperty(value = "插件ID")
    private String pluginCode;

    @Field("plugin_name")
    private String pluginName;//插件名

    private String stepName;//插件名

    @Field("plugin_desc")
    private String pluginDesc;//插件描述

    private String version; //插件版本

    private long timestamp; //执行时间


    private Map<String,Object> execution; //执行命令入口相关

    @Field("author")
    private Map author; //作者

    private String type;

    private boolean isPublic; //是否公开

    private List<Map<String, String>> input; //固定入参

    private List<Entity> entity; //实体模板

    private List<Map<String, String>> output; //出参

    private String pluginStartTime; //开始时间

    private String pluginEndTime;  //结束时间

    private String pluginCostTime;  //耗时

    private String status; //状态

    private List artifacts; //产物

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginDesc() {
        return pluginDesc;
    }

    public void setPluginDesc(String pluginDesc) {
        this.pluginDesc = pluginDesc;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getExecution() {
        return execution;
    }

    public void setExecution(Map<String, Object> execution) {
        this.execution = execution;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public List<Map<String, String>> getInput() {
        return input;
    }

    public void setInput(List<Map<String, String>> input) {
        this.input = input;
    }

    public List<Map<String, String>> getOutput() {
        return output;
    }

    public void setOutput(List<Map<String, String>> output) {
        this.output = output;
    }

    public String getPluginStartTime() {
        return pluginStartTime;
    }

    public void setPluginStartTime(String pluginStartTime) {
        this.pluginStartTime = pluginStartTime;
    }

    public String getPluginEndTime() {
        return pluginEndTime;
    }

    public void setPluginEndTime(String pluginEndTime) {
        this.pluginEndTime = pluginEndTime;
    }

    public String getPluginCostTime() {
        return pluginCostTime;
    }

    public void setPluginCostTime(String pluginCostTime) {
        this.pluginCostTime = pluginCostTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Entity> getEntity() {
        return entity;
    }

    public void setEntity(List<Entity> entity) {
        this.entity = entity;
    }

    public Map getAuthor() {
        return author;
    }

    public void setAuthor(Map author) {
        this.author = author;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getPluginCode() {
        return pluginCode;
    }

    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }

    public List getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List artifacts) {
        this.artifacts = artifacts;
    }
}
