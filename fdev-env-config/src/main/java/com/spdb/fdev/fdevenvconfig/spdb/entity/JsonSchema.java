package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * 实体高级属性
 */
@Document("json_schema")
public class JsonSchema implements Serializable {

    private static final long serialVersionUID = -1317596494260553835L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    // id
    @Field(Dict.ID)
    private String id;

    // 属性英文名
    @Field(Dict.TITLE)
    private String title;

    // 描述
    @Field(Dict.DESCRIPTION)
    private String description;

    // 标识是否为公用模板 1-是， 0-否
    @Field(Dict.IS_TEMPLATE)
    private Integer template;

    @Field(Dict.DATA_TYPE)
    private String dataType;

    @Field(Dict.JSON_SCHEMA)
    private String jsonSchema;

    // 操作人id
    @Field(Constants.OPNO)
    private String opno;

    // 状态 1-未删除，0-删除
    @Field(Dict.STATUS)
    private Integer status;

    // 创建时间
    @Field(Dict.CREATE_TIME)
    private String createTime;

    // 更新时间
    @Field(Dict.UPDATE_TIME)
    private String updateTime;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTemplate() {
        return template;
    }

    public void setTemplate(Integer template) {
        this.template = template;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getJsonSchema() {
        return jsonSchema;
    }

    public void setJsonSchema(String jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    public String getOpno() {
        return opno;
    }

    public void setOpno(String opno) {
        this.opno = opno;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

}
