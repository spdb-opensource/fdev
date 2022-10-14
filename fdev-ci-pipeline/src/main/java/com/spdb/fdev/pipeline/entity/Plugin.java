package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Document(collection = "plugin")
@ApiModel(value = "插件")
public class Plugin {

    @Field("pluginCode")
    @Indexed(unique = true)
    @ApiModelProperty(value = "插件ID")
    private String pluginCode;

    @Field("nameId")
    @ApiModelProperty(value = "插件系列ID")
    private String nameId;

    @Field("pluginName")
    @ApiModelProperty(value = "插件中文名称")
    private String pluginName;

    @Field("pluginNameEn")
    @ApiModelProperty(value = "插件英文名称")
    private String pluginNameEn;

    @Field("pluginDesc")
    @ApiModelProperty(value = "插件描述")
    private String pluginDesc;

    @Field("execution")
    @ApiModelProperty(value = "执行命令入口相关")
    private Map<String,Object> execution;

    @Field("params")
    @ApiModelProperty(value = "插件输入字段定义,里面有boolean类型")
    private List<Map<String, Object>> params;

    @Field("output")
    @ApiModelProperty(value = "插件输出字段定义")
    private String output;

    @Field("entityTemplateList")
    @ApiModelProperty(value = "实体模板列表")
    private List<Map> entityTemplateList;

    @Field("version")
    @ApiModelProperty(value = "插件版本用户输入")
    private String version;

    @Field("createTime")
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @Field("author")
    @ApiModelProperty(value = "作者")
    private Author author;

//    @Field("type")
//    @ApiModelProperty(value = "插件类型'0'-系统插件，'1'-自定义插件")
//    private String type;
//    自定义插件区分改为用作者来区分，系统插件的作者为system，自定义则为用户

    @Field("open")
    @ApiModelProperty(value = "插件可见范围true-公开，false-私有")
    private boolean open;

    @Field("releaseLog")
    @ApiModelProperty(value = "提交描述")
    private String releaseLog;

    @Field("status")
    @ApiModelProperty(value = "插件状态")
    private String status;

    @Field("script")
    @ApiModelProperty(value = "脚本内容")
    private Map script;

//    @Field("changeVersion")
//    @ApiModelProperty(value = "变更版本号后端自增")
//    private String changeVersion;

    @Field("category")
    private Category category;        //分类名称

    @Field("artifacts")
    private Map<String, Object> artifacts;       //构建物信息

    @Field("updateTimes")
    private String updateTimes;             //变更次数

    @Field("markDownUrl")
    @ApiModelProperty(value = "markDown的文档地址")
    private String markDownUrl;

    @Field("imageUrl")
    @ApiModelProperty(value = "上传图片的地址")
    private String imageUrl;

    @Field("pluginLabel")
    @ApiModelProperty(value = "插件标签")
    private List<String> pluginLabel;

    @Field("devLanguage")
    @ApiModelProperty(value = "插件开发语言")
    private String devLanguage;

    @Field("platform")
    @ApiModelProperty(value = "系统运行平台")
    private List<String> platform;

    @Field("addParamsFlag")
    @ApiModelProperty(value = "true代表该插件是有脚本或者配置文件的,false代表没有")
    private Boolean addParamsFlag;

    @Field("resultDisplayFlag")
    @ApiModelProperty(value = "true代表该插件需要展示输出结果,false代表不需要,默认存量不需要")
    private Boolean resultDisplayFlag = false;

    public Plugin() {
    }


    public Plugin(String pluginCode, String nameId, String pluginName, String pluginNameEn, String pluginDesc, Map<String, Object> execution, List<Map<String, Object>> params, String output, List<Map> entityTemplateList, String version, String createTime, Author author, boolean open, String releaseLog, String status, Map script, Category category, Map<String, Object> artifacts, String updateTimes, String markDownUrl, String imageUrl, List<String> pluginLabel, String devLanguage, List<String> platform, Boolean addParamsFlag) {
        this.pluginCode = pluginCode;
        this.nameId = nameId;
        this.pluginName = pluginName;
        this.pluginNameEn = pluginNameEn;
        this.pluginDesc = pluginDesc;
        this.execution = execution;
        this.params = params;
        this.output = output;
        this.entityTemplateList = entityTemplateList;
        this.version = version;
        this.createTime = createTime;
        this.author = author;
        this.open = open;
        this.releaseLog = releaseLog;
        this.status = status;
        this.script = script;
        this.category = category;
        this.artifacts = artifacts;
        this.updateTimes = updateTimes;
        this.markDownUrl = markDownUrl;
        this.imageUrl = imageUrl;
        this.pluginLabel = pluginLabel;
        this.devLanguage = devLanguage;
        this.platform = platform;
        this.addParamsFlag = addParamsFlag;
    }

    public String getMarkDownUrl() {
        return markDownUrl;
    }

    public void setMarkDownUrl(String markDownUrl) {
        this.markDownUrl = markDownUrl;
    }

    public String getPluginCode() {
        return pluginCode;
    }

    public void setPluginCode(String pluginCode) {
        this.pluginCode = pluginCode;
    }

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

    public Map<String, Object> getExecution() {
        return execution;
    }

    public void setExecution(Map<String, Object> execution) {
        this.execution = execution;
    }


    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCreateTime() { return createTime; }

    public void setCreateTime(String createTime) {  this.createTime = createTime; }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }


    public boolean isOpen() { return open;}

    public void setOpen(boolean open) { this.open = open;}

    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    public String getReleaseLog() {
        return releaseLog;
    }

    public void setReleaseLog(String releaseLog) {
        this.releaseLog = releaseLog;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



    public List<Map> getEntityTemplateList() {
        return entityTemplateList;
    }

    public void setEntityTemplateList(List<Map> entityTemplateList) {
        this.entityTemplateList = entityTemplateList;
    }

    public Map getScript() {
        return script;
    }

    public void setScript(Map script) {
        this.script = script;
    }

    public Map<String, Object> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Map<String, Object> artifacts) {
        this.artifacts = artifacts;
    }

    public String getUpdateTimes() {
        return updateTimes;
    }

    public void setUpdateTimes(String updateTimes) {
        this.updateTimes = updateTimes;
    }

    public List<Map<String, Object>> getParams() {
        return params;
    }

    public void setParams(List<Map<String, Object>> params) {
        this.params = params;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getPluginLabel() {
        return pluginLabel;
    }

    public void setPluginLabel(List<String> pluginLabel) {
        this.pluginLabel = pluginLabel;
    }

    public String getDevLanguage() {
        return devLanguage;
    }

    public void setDevLanguage(String devLanguage) {
        this.devLanguage = devLanguage;
    }

    public List<String> getPlatform() {
        return platform;
    }

    public void setPlatform(List<String> platform) {
        this.platform = platform;
    }

    public Boolean getAddParamsFlag() {
        return addParamsFlag;
    }

    public void setAddParamsFlag(Boolean addParamsFlag) {
        this.addParamsFlag = addParamsFlag;
    }

    public String getPluginNameEn() {
        return pluginNameEn;
    }

    public void setPluginNameEn(String pluginNameEn) {
        this.pluginNameEn = pluginNameEn;
    }

    public Boolean getResultDisplayFlag() {
        return resultDisplayFlag;
    }

    public void setResultDisplayFlag(Boolean resultDisplayFlag) {
        this.resultDisplayFlag = resultDisplayFlag;
    }

    @Override
    public String toString() {
        return "Plugin{" +
                "pluginCode='" + pluginCode + '\'' +
                ", nameId='" + nameId + '\'' +
                ", pluginName='" + pluginName + '\'' +
                ", pluginNameEn='" + pluginNameEn + '\'' +
                ", pluginDesc='" + pluginDesc + '\'' +
                ", execution=" + execution +
                ", params=" + params +
                ", output='" + output + '\'' +
                ", entityTemplateList=" + entityTemplateList +
                ", version='" + version + '\'' +
                ", createTime='" + createTime + '\'' +
                ", author=" + author +
                ", open=" + open +
                ", releaseLog='" + releaseLog + '\'' +
                ", status='" + status + '\'' +
                ", script=" + script +
                ", category=" + category +
                ", artifacts=" + artifacts +
                ", updateTimes='" + updateTimes + '\'' +
                ", markDownUrl='" + markDownUrl + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", pluginLabel=" + pluginLabel +
                ", devLanguage='" + devLanguage + '\'' +
                ", platform=" + platform +
                ", addParamsFlag=" + addParamsFlag +
                ", resultDisplayFlag=" + resultDisplayFlag +
                '}';
    }
}
