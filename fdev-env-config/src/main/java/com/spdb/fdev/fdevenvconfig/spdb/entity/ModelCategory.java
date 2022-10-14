package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

@Document(collection = "model-category")
@ApiModel(value = "实体类型常量")
public class ModelCategory implements Serializable {

    private static final long serialVersionUID = -5358260789019534369L;

    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "实体类型常量id")
    private String id;

    @Field("category")
    @ApiModelProperty(value = "实体类型常量分类")
    private List<Object> category;

    @Field("ctime")
    @ApiModelProperty(value = "创建时间")
    private String ctime;

    @Field("utime")
    @ApiModelProperty(value = "修改时间")
    private String utime;

    @Field("opno")
    @ApiModelProperty(value = "操作员")
    private String opno;

    public ModelCategory() {

    }

    public ModelCategory(ObjectId _id, String id, List<Object> category, String ctime, String utime,
                         String opno) {
        super();
        this._id = _id;
        this.id = id;
        this.category = category;
        this.ctime = ctime;
        this.utime = utime;
        this.opno = opno;
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

    public List<Object> getCategory() {
        return category;
    }

    public void setCategory(List<Object> category) {
        this.category = category;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getUtime() {
        return utime;
    }

    public void setUtime(String utime) {
        this.utime = utime;
    }

    public String getOpno() {
        return opno;
    }

    public void setOpno(String opno) {
        this.opno = opno;
    }

    @Override
    public String toString() {
        return "ModelCategory [_id=" + _id + ", id=" + id + ", category=" + category + ", ctime="
                + ctime + ", utime=" + utime + ", opno=" + opno + "]";
    }

}
