package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document(collection = "labels")
public class Labels implements Serializable {

    private static final long serialVersionUID = -9117313748496690326L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    private String id;

    @Field(Dict.LABELS)
    private Map labels;

    public Labels() {
    }

    public Labels(ObjectId _id, String id, Map labels) {
        this._id = _id;
        this.id = id;
        this.labels = labels;
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

    public Map getLabels() {
        return labels;
    }

    public void setLabels(Map labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "Labels{" +
                "_id=" + _id +
                ", id='" + id + '\'' +
                ", labels=" + labels +
                '}';
    }
}
