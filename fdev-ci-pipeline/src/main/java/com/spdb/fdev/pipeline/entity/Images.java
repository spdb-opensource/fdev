package com.spdb.fdev.pipeline.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.base.dict.Dict;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Document(collection = "images")
public class Images implements Serializable {
    @JsonIgnore
    @Field("_id")
    private ObjectId _id;

    @Field("imageId")
    private String id;

    @Field("name")
    @ApiModelProperty("镜像名称")
    private String name;

    @Field("desc")
    @ApiModelProperty("镜像描述")
    private String desc;

    @Field("path")
    @ApiModelProperty("镜像地址")
    private String path;

    @Field("visibleRange")
    @ApiModelProperty("可见范围有三种：private、public、group")
    private String visibleRange;

    @Field("groupId")
    @ApiModelProperty("用户组id")
    private String groupId;

    @Field("status")
    @ApiModelProperty("是否可用")
    private String status;

    @Field("author")
    @ApiModelProperty("镜像创建者")
    private Author author;

    public Images(String id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
    }

    public Images() {
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVisibleRange() {
        return visibleRange;
    }

    public void setVisibleRange(String visibleRange) {
        this.visibleRange = visibleRange;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    @Override
    public String toString() {
        return "Images{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", path='" + path + '\'' +
                ", visibleRange='" + visibleRange + '\'' +
                ", groupId='" + groupId + '\'' +
                ", status='" + status + '\'' +
                ", author=" + author +
                '}';
    }
}
