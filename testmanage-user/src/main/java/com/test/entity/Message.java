package com.test.entity;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.test.utils.MyUtils;
import com.test.utils.NotifyUtil;

public class Message implements Serializable {

    private String id;

    private String content;

    private String type;

    private String target;

    private String linkUri;

    private String state;

    private String create_time;

    private String expiry_time;


    public Message() {
    }

    public Message(String content, String expiry_time) {
        this.content = content;
        this.expiry_time = expiry_time;
        this.create_time = MyUtils.formatDate(MyUtils.STANDARDDATEPATTERN);
    }

    public Message(String content, String type, String target) {
        this.content = content;
        this.type = type;
        this.target = target;
        this.create_time = MyUtils.formatDate(MyUtils.STANDARDDATEPATTERN);
    }

    public Message(String content, String type, String target, String hyperlink) {
        this.content = content;
        this.type = type;
        this.target = target;
        this.state = "0";
        this.create_time = MyUtils.formatDate(MyUtils.STANDARDDATEPATTERN);
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

    public String getLinkUri() {
        return linkUri;
    }

    public void setLinkUri(String linkUri) {
        this.linkUri = linkUri;
    }
}
