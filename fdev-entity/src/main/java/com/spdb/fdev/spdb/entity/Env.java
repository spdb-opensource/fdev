package com.spdb.fdev.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

/**
 * @ClassName Env
 * @DESCRIPTION 环境表
 * @Author xxx
 * @Date 2021/5/7 15:37
 * @Version 1.0
 */
@Component
@Document(collection = "env")
@ApiModel(value = "环境表")
public class Env {

    @Id
    @ApiModelProperty(value = "ID")
    private ObjectId _id;

    @Field("id")
    @Indexed(unique = true)
    private String id;

    @Indexed(unique = true)
    @ApiModelProperty(value = "环境")
    private String nameEn;

    @ApiModelProperty(value = "环境中文名")
    private String nameCn;

    @Indexed(unique = false)
    @ApiModelProperty(value = "类型")
    private String type;


    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
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

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
