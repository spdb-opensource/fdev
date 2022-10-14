package com.spdb.fdev.pipeline.entity;

import com.spdb.fdev.base.utils.CommonUtils;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

public class PluginInfo {

    @Transient
    //private String id;

    @Field("input")
    private List<Map<String, String>> input;

    @Field("artifacts")
    @ApiModelProperty(value = "接收前段传来的产物")
    private Map<String,Object> artifacts;

    @Field("pluginCode")
    @ApiModelProperty(value = "插件ID")
    private String pluginCode;

    @Field("name")
    @ApiModelProperty(value = "插件名称")
    private String name;

    @Field("desc")
    @ApiModelProperty(value = "插件描述")
    private String desc;


    @Field("entityTemplateParams")
    @ApiModelProperty(value = "实体模板列表参数")
    private List<Map> entityTemplateParams;

    @Field("params")
    @ApiModelProperty(value = "手动输入参数")
    private List<Map<String,Object>> params;

    @Transient
    @ApiModelProperty(value = "页面的脚本命令")
    private String scriptCmd;

    @Transient
    @ApiModelProperty(value = "false代表页面脚本命令未修改，true代表脚本命令已修改")
    private Boolean scriptUpdateFlag;

    @Field("script")
    @ApiModelProperty(value = "脚本的mino名称和地址")
    private Map<String,String> script;

    @Transient
    @ApiModelProperty(value = "插件是否有效，true代表有效，false代表无效")
    private Boolean validFlag;

    @Field("addParams")
    @ApiModelProperty(value = "构建参数")
    private List<Map> addParams;

    public PluginInfo() {
    }

    public List<Map<String, String>> getInput() {
        return input;
    }

    public void setInput(List<Map<String, String>> input) {
        this.input = input;
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

    public String getScriptCmd() {
        return scriptCmd;
    }

    public void setScriptCmd(String scriptCmd) {
        this.scriptCmd = scriptCmd;
    }

    public Boolean getScriptUpdateFlag() {
        return scriptUpdateFlag;
    }

    public void setScriptUpdateFlag(Boolean scriptUpdateFlag) {
        this.scriptUpdateFlag = scriptUpdateFlag;
    }


    public String getPluginCode() {
        return pluginCode;
    }

    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }


    public Map<String, String> getScript() {
        return script;
    }

    public void setScript(Map<String, String> script) {
        this.script = script;
    }

    public List<Map<String, Object>> getParams() {
        return params;
    }

    public void setParams(List<Map<String, Object>> params) {
        this.params = params;
    }

    public List<Map> getEntityTemplateParams() {
        return entityTemplateParams;
    }

    public void setEntityTemplateParams(List<Map> entityTemplateParams) {
        this.entityTemplateParams = entityTemplateParams;
    }

    public Map<String, Object> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Map<String, Object> artifacts) {
        this.artifacts = artifacts;
    }

    public Boolean getValidFlag() {
        return validFlag;
    }

    public void setValidFlag(Boolean validFlag) {
        this.validFlag = validFlag;
    }

    public List<Map> getAddParams() {
        return addParams;
    }

    public void setAddParams(List<Map> addParams) {
        this.addParams = addParams;
    }

    @Override
    public String toString() {
        return "PluginInfo{" +
                "input=" + input +
                ", artifacts=" + artifacts +
                ", pluginCode='" + pluginCode + '\'' +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", entityTemplateParams=" + entityTemplateParams +
                ", params=" + params +
                ", scriptCmd='" + scriptCmd + '\'' +
                ", scriptUpdateFlag=" + scriptUpdateFlag +
                ", script=" + script +
                ", validFlag=" + validFlag +
                ", addParams=" + addParams +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        try {
            return CommonUtils.checkoutAllFields(this, obj);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }
}
