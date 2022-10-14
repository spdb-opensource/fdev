package com.spdb.fdev.component.entity;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "image_archetype")
public class ImageArchetype {

    @Id
    private ObjectId _id;
    @Field("id")
    private String id;
    @Field("archetype_id")
    private String archetype_id;
    @Field("archetype_version")
    private String archetype_version;
    @Field("image_name")
    private String image_name;
    @Field("image_tag")
    private String image_tag;
    @Field("update_time")
    private String update_time;

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

    public String getArchetype_id() {
        return archetype_id;
    }

    public void setArchetype_id(String archetype_id) {
        this.archetype_id = archetype_id;
    }

    public String getArchetype_version() {
        return archetype_version;
    }

    public void setArchetype_version(String archetype_version) {
        this.archetype_version = archetype_version;
    }

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public String getImage_tag() {
        return image_tag;
    }

    public void setImage_tag(String image_tag) {
        this.image_tag = image_tag;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }
}
