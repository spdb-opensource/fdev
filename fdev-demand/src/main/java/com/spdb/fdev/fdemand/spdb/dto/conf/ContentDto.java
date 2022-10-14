package com.spdb.fdev.fdemand.spdb.dto.conf;

public class ContentDto {

    private String id;//"id": "150549895",

    private String type;//"type": "page",

    private String status;//"status": "current",

    private String title;//"title": "甜橘APP新设备登录优化相关需求"

    private String state;//非接口字段，这里是便于数据组装

    private Links _links;//非接口字段，这里是便于数据组装

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Links get_links() {
        return _links;
    }

    public void set_links(Links _links) {
        this._links = _links;
    }
}
