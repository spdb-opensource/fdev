package com.spdb.fdev.component.entity;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "component_archetype")
public class ComponentArchetype implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5166468077706206564L;
    @Id
    private ObjectId _id;
    @Field("id")
    private String id;
    @Field("archetype_id")
    private String archetype_id;
    @Field("archetype_version")
    private String archetype_version;
    @Field("component_id")
    private String component_id;
    @Field("component_version")
    private String component_version;
    @Field("isLatest")
    private String isLatest;
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

    public String getComponent_id() {
        return component_id;
    }

    public void setComponent_id(String component_id) {
        this.component_id = component_id;
    }

    public String getComponent_version() {
        return component_version;
    }

    public void setComponent_version(String component_version) {
        this.component_version = component_version;
    }

    public String getIsLatest() {
        return isLatest;
    }

    public void setIsLatest(String isLatest) {
        this.isLatest = isLatest;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }
}
