package com.fdev.notify.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fdev.notify.util.NotifyUtil;
import com.spdb.fdev.common.util.Util;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(collection = "message")
public class Message implements Serializable {
    @JsonIgnore
    @Id
    private ObjectId _id;

    @Field("id")
    private String id;

    @Field("content")
    private String content;

    @Field("type")
    private String type;

    @Field("target")
    private String target;

    @Field("hyperlink")
    private String hyperlink;

    @Field("state")
    private String state;

    @Field("create_time")
    private String create_time;

    @Field("expiry_time")
    private String expiry_time;

    @Field("desc")
    private String desc;

    @Transient
    private String isNew;//是否为新公告，yes\no


    public Message() {
    }

    public Message(String content, String expiry_time) {
        this.id = new ObjectId().toString();
        this.content = content;
        this.expiry_time = expiry_time;
        this.create_time = Util.formatDate(Util.STANDARDDATEPATTERN);
    }

    public Message(String content, String type, String target, String desc) {
        this.id = new ObjectId().toString();
        this.content = content;
        this.type = type;
        this.target = target;
        this.desc = desc;
        this.state = "0";
        this.create_time = Util.formatDate(Util.STANDARDDATEPATTERN);
    }

    public Message(String content, String type, String target, String hyperlink, String desc) {
        this.id = new ObjectId().toString();
        this.content = content;
        this.type = type;
        this.target = target;
        this.hyperlink = hyperlink;
        this.desc = desc;
        this.state = "0";
        this.create_time = Util.formatDate(Util.STANDARDDATEPATTERN);
    }

    @Override
    public String toString() {
        String json = "{}";
        try {
            json = NotifyUtil.objectToJson(this);
        } catch (JsonProcessingException e) {
            return "{}";
        }
        return json;
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

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getHyperlink() {
        return hyperlink;
    }

    public void setHyperlink(String hyperlink) {
        this.hyperlink = hyperlink;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getExpiry_time() {
        return expiry_time;
    }

    public void setExpiry_time(String expiry_time) {
        this.expiry_time = expiry_time;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIsNew() {
        return isNew;
    }

    public void setIsNew(String isNew) {
        this.isNew = isNew;
    }
}
