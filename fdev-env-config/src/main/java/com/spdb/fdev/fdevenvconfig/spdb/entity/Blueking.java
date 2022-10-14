package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Map;

/**
 * @Author: lisy26
 * @date: 2021/1/6 19:34
 * @ClassName: Blueking
 * @Description: 蓝鲸平台相关信息
 */
@Document(collection = "blueking")
@ApiModel(value = "蓝鲸平台信息")
public class Blueking implements Serializable {

    private static final long serialVersionUID = 8637314009320661611L;

    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;

    @Field("entity_type")
    @ApiModelProperty(value = "蓝鲸实体类型")
    private String entityType;

    @Field("data")
    @ApiModelProperty(value = "蓝鲸保存字段")
    private Map<String, Object> data;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Map getData() {
        return data;
    }

    public void setData(Map data) {
        this.data = data;
    }

    public Blueking() {
    }

    public Blueking(String entityType, Map data) {
        this.entityType = entityType;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Blueking{" +
                "entityType='" + entityType + '\'' +
                ", data=" + data +
                '}';
    }
}
