package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Document(collection = "errors")
@ApiModel(value = "系统内部错误日志表")
public class ErrorCollection implements Serializable {

    private static final long serialVersionUID = -9103540139030021652L;

    @Id
    @ApiModelProperty(value = "ID")
    private ObjectId _id;

    @Field("id")
    @Indexed(unique = true)
    private String id;

    @Field("status")
    @ApiModelProperty(value = "状态：0：未处理 1：已处理")
    private String status;

    @Field("type")
    @ApiModelProperty(value = "类型：1：任务进入REL状态异常")
    private String type;

    @Field("link")
    @ApiModelProperty(value = "链接")
    private String link;

    @Field("exception")
    @ApiModelProperty(value = "异常信息")
    private String exception;

    @Field("description")
    @ApiModelProperty(value = "异常描述")
    private String description;

    @Field("create_time")
    @ApiModelProperty(value = "添加时间")
    private String create_time;

    public ErrorCollection() {
        super();
    }

    public ErrorCollection(ObjectId _id, String id, String status, String type, String link, String exception,
                           String description, String create_time) {
        this._id = _id;
        this.id = id;
        this.status = status;
        this.type = type;
        this.link = link;
        this.exception = exception;
        this.description = description;
        this.create_time = create_time;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }
}
