package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document(collection = "app_own_field")
public class AppOwnField implements Serializable {

    private static final long serialVersionUID = -4686506582246541504L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field(Dict.ID)
    private String id;

    @Field(Dict.APPID)
    private String app_Id;

    @Field(Dict.APP_NAME)
    private String app_name;

    @Field(Dict.ENV_ID)
    private String env_id;

    @Field(Dict.ENV_NAME)
    private String env_name;

    @Field(Dict.MODELFIELD_VALUE)
    private List<Map> modelFleld_value;

    @Field(Dict.CREATE_TIME)
    private String createTime;

    @Field(Dict.UPDATE_TIME)
    private String uadateTime;


    public AppOwnField() {
    }

    public AppOwnField(String id, String app_Id, String app_name, String env_id, String env_name,
                       List<Map> modelFleld_value, String createTime, String uadateTime) {

        this.id = id;
        this.app_Id = app_Id;
        this.app_name = app_name;
        this.env_id = env_id;
        this.env_name = env_name;
        this.modelFleld_value = modelFleld_value;
        this.createTime = createTime;
        this.uadateTime = uadateTime;
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

    public String getApp_Id() {
        return app_Id;
    }

    public void setApp_Id(String app_Id) {
        this.app_Id = app_Id;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getEnv_id() {
        return env_id;
    }

    public void setEnv_id(String env_id) {
        this.env_id = env_id;
    }

    public String getEnv_name() {
        return env_name;
    }

    public void setEnv_name(String env_name) {
        this.env_name = env_name;
    }

    public List<Map> getModelFleld_value() {
        return modelFleld_value;
    }

    public void setModelFleld_value(List<Map> modelFleld_value) {
        this.modelFleld_value = modelFleld_value;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUadateTime() {
        return uadateTime;
    }

    public void setUadateTime(String uadateTime) {
        this.uadateTime = uadateTime;
    }

    @Override
    public String toString() {
        return "AppOwnField{" +
                "_id=" + _id +
                ", id='" + id + '\'' +
                ", app_Id='" + app_Id + '\'' +
                ", app_name='" + app_name + '\'' +
                ", env_id='" + env_id + '\'' +
                ", env_name='" + env_name + '\'' +
                ", modelFleld_value=" + modelFleld_value +
                ", createTime='" + createTime + '\'' +
                ", uadateTime='" + uadateTime + '\'' +
                '}';
    }
}
