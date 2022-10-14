package com.spdb.fdev.pipeline.entity;

import com.spdb.fdev.base.utils.CommonUtils;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

// fileEdit插件的参数实体
@Document("yamlConfig")
public class YamlConfig implements Serializable {

    @Field("configId")
    private String configId;

    @Field("configName")
    private String configName;

    @Field("configTemplateId")
    private String configTemplateId;          //源模版的id，当为public系统模版的时候，该值为空

    @Field("configDesc")
    private String configDesc;

    @Field("type")
    private String type;            //public 是系统的模版   private是流水线自己用的

    @Field("env")
    private String env;             //环境名  public 的模版不会有这个值

    @Field("envsFlag")
    private Boolean envsFlag;       //多选环境的flag   true 为需要多选， false 即不需要多选

    @Field("envList")
    private List envList;          //若envsFlag为true 即该值不为空

    @Field("params")
    private List<YamlConfigParam> params;       //参数列表

    @Field("status")
    private String status;       //状态：0-没有使用，1-有使用

    public YamlConfig() {
    }

    public String paramsInfo(){
        return "{" +
                "configName=" + configName +
                ", configDesc=" + configDesc +
                ", configTemplateId=" + configTemplateId +
                ", type=" + type +
                ", envsFlag=" + envsFlag +
                ", env=" + env +
                ", envList=" + envList +
                ", params=" + params.toString() +
                '}';
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigDesc() {
        return configDesc;
    }

    public void setConfigDesc(String configDesc) {
        this.configDesc = configDesc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public List<YamlConfigParam> getParams() {
        return params;
    }

    public void setParams(List<YamlConfigParam> params) {
        this.params = params;
    }

    public String getConfigTemplateId() {
        return configTemplateId;
    }

    public void setConfigTemplateId(String configTemplateId) {
        this.configTemplateId = configTemplateId;
    }

    public Boolean getEnvsFlag() {
        return envsFlag;
    }

    public void setEnvsFlag(Boolean envsFlag) {
        this.envsFlag = envsFlag;
    }

    public List getEnvList() {
        return envList;
    }

    public void setEnvList(List envList) {
        this.envList = envList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
