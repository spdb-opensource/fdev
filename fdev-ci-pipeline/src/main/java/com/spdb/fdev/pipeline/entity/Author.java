package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@ApiModel(value = "创建人信息")
public class Author implements Serializable {

    private static final long serialVersionUID = -3344809078720864016L;

    @Field("id")
    @ApiModelProperty(value = "用户ID")
    private String id;

    @Field("nameCn")
    @ApiModelProperty(value = "用户中文名")
    private String nameCn;

    @Field("nameEn")
    @ApiModelProperty(value = "用户英文名")
    private String nameEn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id='" + id + '\'' +
                ", nameCn='" + nameCn + '\'' +
                ", nameEn='" + nameEn + '\'' +
                '}';
    }
}
