package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.HashSet;

@Document(collection = "app-model-rela")
@ApiModel(value = "应用实体关联")
public class AppModel implements Serializable {
    private static final long serialVersionUID = -3817793707400528425L;

    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "应用实体关联id")
    private String id;

    @Field("app_id")
    @ApiModelProperty(value = "应用id")
    private String app_id;

    @Field("model_id")
    @ApiModelProperty(value = "实体id")
    private HashSet model_id;

    public AppModel() {

    }

    public AppModel(ObjectId _id, String id, String app_id, HashSet model_id) {
        super();
        this._id = _id;
        this.id = id;
        this.app_id = app_id;
        this.model_id = model_id;
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

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public HashSet getModel_id() {
        return model_id;
    }

    public void setModel_id(HashSet model_id) {
        this.model_id = model_id;
    }

    @Override
    public String toString() {
        return "AppModel [_id=" + _id + ", id=" + id + ", app_id=" + app_id + ", model_id=" + model_id + "]";
    }


}
