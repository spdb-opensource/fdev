package com.spdb.fdev.fdevtask.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @program:fdev-process
 * @author: xxx
 * @createTime: 2021-01-22 16:55
 */
@Document(collection = "field")
@ApiModel(value = "域模型")
public class Field {

    private static final long serialVersionUID = 8654331831270032342L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @org.springframework.data.mongodb.core.mapping.Field
    @ApiModelProperty(value ="域id")
    private String id;

    @org.springframework.data.mongodb.core.mapping.Field("name")
    @ApiModelProperty(value ="字段名称")
    private String name;

    @org.springframework.data.mongodb.core.mapping.Field("model")
    @ApiModelProperty(value ="模型类型")
    private String model;

    @org.springframework.data.mongodb.core.mapping.Field("key")
    @ApiModelProperty(value ="字段名")
    private String key;

    @org.springframework.data.mongodb.core.mapping.Field("isRequired")
    @ApiModelProperty(value ="是否必输")
    private Boolean isRequired;

    @org.springframework.data.mongodb.core.mapping.Field("lable")
    @ApiModelProperty(value ="字段类型 1：系统 2：自定义")
    private String lable;

    @org.springframework.data.mongodb.core.mapping.Field("remark")
    @ApiModelProperty(value ="备注")
    private String remark;

    @org.springframework.data.mongodb.core.mapping.Field("updateTime")
    @ApiModelProperty(value ="更新时间")
    private String updateTime;

    @org.springframework.data.mongodb.core.mapping.Field("type")
    @ApiModelProperty(value ="字段类型 1：文本框 2：复选框 等")
    private String type;

    @org.springframework.data.mongodb.core.mapping.Field("enum")
    @ApiModelProperty(value ="可选值")
    private EnumData enumData;

    @org.springframework.data.mongodb.core.mapping.Field("isModify")
    @ApiModelProperty(value ="是否可以更改")
    private Boolean isModify;

    @org.springframework.data.mongodb.core.mapping.Field("defaultValue")
    @ApiModelProperty(value ="默认值")
    private Boolean defaultValue;

    @org.springframework.data.mongodb.core.mapping.Field("teamId")
    @ApiModelProperty(value ="团队id")
    private String teamId;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean required) {
        isRequired = required;
    }

    public String getLable() {
        return lable;
    }

    public void setLable(String lable) {
        this.lable = lable;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EnumData getEnumData() {
        return enumData;
    }

    public void setEnumData(EnumData enumData) {
        this.enumData = enumData;
    }

    public Boolean getIsModify() {
        return isModify;
    }

    public void setIsModify(Boolean modify) {
        isModify = modify;
    }

    public Boolean getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
}
