package com.spdb.fdev.component.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Document(collection = "component_record_history_log")
public class ComponentRecordHistoryLog implements Serializable {

    private static final long serialVersionUID = -2774399655891422011L;
    @Id
    @JsonIgnore
    private ObjectId _id;
    @Field("id")
    private String id;
    @Field("component_id")
    private String component_id;
    @Field("version")
    private String version;
    @Field("date")
    private String date;
    @Field("update_user")
    private String update_user;
    @Field("content")
    private String content;
    @Field("type")
    private String type;

    public ComponentRecordHistoryLog() {
    }

    public ComponentRecordHistoryLog(String component_id, String version, String date
            , String update_user, String content, String type) {
        this.component_id = component_id;
        this.version = version;
        this.date = date;
        this.update_user = update_user;
        this.content = content;
        this.type = type;
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

    public String getComponent_id() {
        return component_id;
    }

    public void setComponent_id(String component_id) {
        this.component_id = component_id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUpdate_user() {
        return update_user;
    }

    public void setUpdate_user(String update_user) {
        this.update_user = update_user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
