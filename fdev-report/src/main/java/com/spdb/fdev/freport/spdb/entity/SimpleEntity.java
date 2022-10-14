package com.spdb.fdev.freport.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class SimpleEntity {

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    private String id;

    public void init() {
        _id = new ObjectId();
        id = _id.toString();
    }
}
