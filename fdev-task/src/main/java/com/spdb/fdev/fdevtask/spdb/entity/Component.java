package com.spdb.fdev.fdevtask.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @program:fdev-process
 * @author: xxx
 * @createTime: 2021-01-22 16:56
 */

@Document(collection = "component")
@ApiModel(value = "组建")
public class Component {
    private static final long serialVersionUID = 8654331831270032342L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value ="活动id")
    private String id;

    @Field("name")
    @ApiModelProperty(value ="活动名称")
    private String name;

    @Field("className")
    @ApiModelProperty(value ="执行的类名")
    private String className;

    @Field("inputFieldList")
    @ApiModelProperty(value = "输入")
    private InputFieldList inputFieldList;

    @Field("outputFieldList")
    @ApiModelProperty(value = "输出")
    private OutputFieldList outputFieldList;

    @Field("feComponentList")
    @ApiModelProperty(value = "前端组件列表")
    private FeComponentList feComponentList;

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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public InputFieldList getInputFieldList() {
        return inputFieldList;
    }

    public void setInputFieldList(InputFieldList inputFieldList) {
        this.inputFieldList = inputFieldList;
    }

    public OutputFieldList getOutputFieldList() {
        return outputFieldList;
    }

    public void setOutputFieldList(OutputFieldList outputFieldList) {
        this.outputFieldList = outputFieldList;
    }

    public FeComponentList getFeComponentList() {
        return feComponentList;
    }

    public void setFeComponentList(FeComponentList feComponentList) {
        this.feComponentList = feComponentList;
    }
}
