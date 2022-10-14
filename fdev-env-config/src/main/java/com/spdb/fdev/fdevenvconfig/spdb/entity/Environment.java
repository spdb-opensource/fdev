package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

/**
 * @author xxx
 * @date 2019/7/5 13:12
 */
@Document(collection = "environment")
@ApiModel(value = "环境")
public class Environment implements Serializable {

    private static final long serialVersionUID = 510436608574122117L;

    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "环境id")
    private String id;

    @Indexed(unique = true)
    @Field("name_en")
    @ApiModelProperty(value = "环境英文")
    private String name_en;

    @Indexed(unique = true)
    @Field("name_cn")
    @ApiModelProperty(value = "环境中文")
    private String name_cn;

    @Field("labels")
    @ApiModelProperty(value = "环境标签")
    private List<String> labels;

    @Field("desc")
    @ApiModelProperty(value = "环境描述")
    private String desc;

    @Field("status")
    @ApiModelProperty(value = "有效状态")
    private String status;

    @Field("ctime")
    @ApiModelProperty(value = "创建时间")
    private String ctime;

    @Field("utime")
    @ApiModelProperty(value = "修改时间")
    private String utime;

    @Field("opno")
    @ApiModelProperty(value = "操作员")
    private String opno;

    public Environment() {
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

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
    }

    public String getName_cn() {
        return name_cn;
    }

    public void setName_cn(String name_cn) {
        this.name_cn = name_cn;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        return "Environment{" +
                "_id=" + _id +
                ", id='" + id + '\'' +
                ", name_en='" + name_en + '\'' +
                ", name_cn='" + name_cn + '\'' +
                ", labels='" + labels + '\'' +
                ", desc='" + desc + '\'' +
                ", status='" + status + '\'' +
                ", ctime='" + ctime + '\'' +
                ", utime='" + utime + '\'' +
                ", opno='" + opno + '\'' +
                '}';
    }
}
