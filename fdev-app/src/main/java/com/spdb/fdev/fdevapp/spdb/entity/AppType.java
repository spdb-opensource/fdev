package com.spdb.fdev.fdevapp.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: luotao
 * Date: 2019/2/28
 * Time: 下午2:01
 * To change this template use File | Settings | File Templates.
 */
@Document(collection = "app_type")
@ApiModel(value="应用类型")
public class AppType implements Serializable{
    @Id
    private ObjectId _id;

    @Field("id")
    private String id;

    @Field("name")
    private String name;

    @Field("desc")
    private String desc;

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
}
