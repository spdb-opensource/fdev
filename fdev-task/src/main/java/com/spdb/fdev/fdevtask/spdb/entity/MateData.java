package com.spdb.fdev.fdevtask.spdb.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document("mate_data")
public class MateData {
    private static final long serialVersionUID = 8654331831270032342L;
    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty("id")
    private String id;

    @Field("type")
    @ApiModelProperty(value = "类型")
    private String type;

    @Field("listMate")
    @ApiModelProperty(value = "下拉选项")
    private List<String> listMate;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getListMate() {
        return listMate;
    }

    public void setListMate(List<String> listMate) {
        this.listMate = listMate;
    }
    @Override
    public String toString() {
        return "MateData{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", listMate=" + listMate +
                '}';
    }

}
