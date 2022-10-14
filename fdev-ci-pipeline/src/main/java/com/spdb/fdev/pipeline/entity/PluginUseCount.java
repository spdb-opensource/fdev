package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "pluginUseCount")
@ApiModel(value = "插件使用统计")
public class PluginUseCount {

    @Field("pluginCode")
    @ApiModelProperty(value = "插件ID")
    private String pluginCode;

    @Field("nameId")
    @ApiModelProperty(value = "插件系列ID")
    private String nameId;

    @Field("bindProject")
    @ApiModelProperty(value = "应用")
    private BindProject bindProject;

    @Field("useCount")
    @ApiModelProperty(value = "使用次数")
    private String useCount;

    public PluginUseCount() {
    }

    public PluginUseCount(String pluginCode, String nameId, BindProject bindProject, String useCount) {
        this.pluginCode = pluginCode;
        this.nameId = nameId;
        this.bindProject = bindProject;
        this.useCount = useCount;
    }

    public String getPluginCode() {
        return pluginCode;
    }

    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }

    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    public BindProject getBindProject() {
        return bindProject;
    }

    public void setBindProject(BindProject bindProject) {
        this.bindProject = bindProject;
    }

    public String getUseCount() {
        return useCount;
    }

    public void setUseCount(String useCount) {
        this.useCount = useCount;
    }
}
