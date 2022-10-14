package com.spdb.fdev.fdevtask.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @program: fdev-task
 * @description:实例存值
 * @author: c-jiangl2
 * @create: 2021-02-01 09:19
 **/
@Document(collection = "model_ext")
@ApiModel(value = "模型存值")
public class ModelExt {

    private static final long serialVersionUID = 8654331831270032342L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field
    @ApiModelProperty(value ="id")
    private String id;

    @Field("model")
    @ApiModelProperty(value ="模型类型")
    private String model;

    @Field("value")
    @ApiModelProperty(value ="值")
    private Object value;

    @Field("modelId")
    @ApiModelProperty(value ="模型id")
    private String modelId;

    @Field("fieldId")
    @ApiModelProperty(value ="字段id")
    private String fieldId;

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

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }
}
