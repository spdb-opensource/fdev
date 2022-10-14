package com.spdb.fdev.component.entity;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Document(collection = "release_node")
public class ReleaseNode {
    @Id
    private ObjectId _id;

    @Field("id")
    private String id;

    @Field("app_id")
    private String app_id;

    @Field("release_node_name")
    private String release_node_name;

    @Field("tag_list")
    private List<String> tag_list;

    public ReleaseNode() {

    }

    public ReleaseNode(String id, String app_id, String release_node_name, List<String> tag_list) {
        this.id = id;
        this.app_id = app_id;
        this.release_node_name = release_node_name;
        this.tag_list = tag_list;
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

    public String getRelease_node_name() {
        return release_node_name;
    }

    public void setRelease_node_name(String release_node_name) {
        this.release_node_name = release_node_name;
    }

    public List<String> getTag_list() {
        return tag_list;
    }

    public void setTag_list(List<String> tag_list) {
        this.tag_list = tag_list;
    }


}
