package com.spdb.fdev.component.entity;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "component_application")
@CompoundIndexes({
        @CompoundIndex(name = "component_application_namex", def = "{'application_id': 1, 'component_id': 1}", unique = true)
})
public class ComponentApplication implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7925913217550659616L;

    @Id
    private ObjectId _id;
    @Field("id")
    private String id;
    @Field("application_id")
    private String application_id;
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

    public String getApplication_id() {
        return application_id;
    }

    public void setApplication_id(String application_id) {
        this.application_id = application_id;
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
