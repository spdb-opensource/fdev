package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
public class Step {

    @Field("name")
    private String name;

    @Field("desc")
    private String desc;

    @Field("pluginInfo")
    private PluginInfo pluginInfo;


    @Field("warning")
    private Boolean warning;    //true有必填参数未输入；false代表没有必填参数未输入

    @Field("deleteFlag")
//    @ApiModelProperty("true代表可以删除；false代表不可以删除")
    private Boolean deleteFlag = true;    //存量数据没有deleteFlag，默认为null

    public Step(String name, PluginInfo pluginInfo) {
        this.name = name;
        this.pluginInfo = pluginInfo;
    }

    public Step() {
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

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    public void setPluginInfo(PluginInfo pluginInfo) {
        this.pluginInfo = pluginInfo;
    }

    public Boolean getWarning() {
        return warning;
    }

    public void setWarning(Boolean warning) {
        this.warning = warning;
    }

    public boolean getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(boolean deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    @Override
    public String toString() {
        return "Step{" +
                "name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", pluginInfo=" + pluginInfo +
                ", warning=" + warning +
                ", deleteFlag=" + deleteFlag +
                '}';
    }
}
