package com.spdb.fdev.fdevapp.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "system")
@ApiModel(value = "应用所属系统")
public class AppSystem {

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "系统id")
    private String id;

    @Field("name")
    @ApiModelProperty(value = "系统中文名")
    private String name;


    public AppSystem() {
    }

    public AppSystem(String id, String name) {
        this.id = id;
        this.name = name;
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


    @Override
    public String toString() {
        return "AppSystem{" +
                "_id=" + _id +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
