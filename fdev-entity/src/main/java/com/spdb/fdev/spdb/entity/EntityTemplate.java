package com.spdb.fdev.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @ClassName EntityTemplate
 * @DESCRIPTION 实体模板表
 * @Author xxx
 * @Date 2021/5/7 15:38
 * @Version 1.0
 */
@Component
@Document(collection = "entity_template")
@ApiModel(value = "实体模板表")
public class EntityTemplate {
    @Id
    @ApiModelProperty(value = "ID")
    private ObjectId _id;

    @Field("id")
    @Indexed(unique = true)
    private String id;

    @ApiModelProperty(value = "实体模板英文名")
    private String nameEn;

    @ApiModelProperty(value = "实体模板中文名")
    private String nameCn;

    @ApiModelProperty(value = "属性集合")
    private List<Map<String, Object>> properties;

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

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }

    public List<Map<String, Object>> getProperties() {
        return properties;
    }

    public void setProperties(List<Map<String, Object>> properties) {
        this.properties = properties;
    }
}
