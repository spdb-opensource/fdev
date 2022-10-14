package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ApiModel(value = "实体或实体模板")
public class Entity {

    @Field("id")
    @ApiModelProperty(value = "实体或实体模板ID")
    private String id;

    @Field("nameEn")
    @ApiModelProperty(value = "实体或实体模板英文名")
    private String nameEn;

    @Field("nameCn")
    @ApiModelProperty(value = "实体或实体模板中文名")
    private String nameCn;

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
}
