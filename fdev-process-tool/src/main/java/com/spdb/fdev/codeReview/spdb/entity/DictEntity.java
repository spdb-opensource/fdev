package com.spdb.fdev.codeReview.spdb.entity;

import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

/**
 * @Author liux81
 * @DATE 2021/11/9
 */
@Component
@Document("code_dict")
public class DictEntity extends BaseEntity{

    @Field("id")
    private String id;

    @Field("type")
    @ApiModelProperty("字典类型")
    private String type;//problemItem问题项/fileType文件类型

    @Field("type_name")
    @ApiModelProperty("字典类型中文名")
    private String typeName;

    @Field("key")
    @ApiModelProperty("字典key")
    private String key;

    @Field("value")
    @ApiModelProperty("字典value")
    private String value;

    @Field("sort")
    @ApiModelProperty("排序编号")
    private Integer sort;

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

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
