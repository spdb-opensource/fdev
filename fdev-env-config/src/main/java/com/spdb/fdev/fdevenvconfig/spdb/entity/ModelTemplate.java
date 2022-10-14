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
 * @Author: lisy26
 * @date: 2020/11/13 13:43
 * @ClassName ModelTemplate
 * @Description 实体模板类
 **/
@Document(collection = "model_template")
@ApiModel(value = "实体模板")
public class ModelTemplate implements Serializable {

    //代码序列化
    private static final long serialVersionUID = 8637314009320661611L;

    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "实体模板id")
    private String id;

    @Indexed(unique = true)
    @Field("name_en")
    @ApiModelProperty(value = "实体模板英文名")
    private String nameEn;

    @Indexed(unique = true)
    @Field("name_cn")
    @ApiModelProperty(value = "实体模板中文名")
    private String nameCn;

    @Field("env_key")
    @ApiModelProperty(value = "实体模板变量key")
    private List<Object> envKey;

    @Field("desc")
    @ApiModelProperty(value = "描述")
    private String desc;

    @Field("status")
    @ApiModelProperty(value = "有效状态")
    private Integer status;

    @Field("ctime")
    @ApiModelProperty(value = "创建时间")
    private String ctime;

    @Field("utime")
    @ApiModelProperty(value = "修改时间")
    private String utime;

    @Field("opno")
    @ApiModelProperty(value = "操作员")
    private String opno;

    public ModelTemplate() {
    }

    public ModelTemplate(ObjectId _id, String id, String nameEn, String nameCn, List<Object> envKey, String desc, Integer status, String ctime, String utime, String opno) {
        super();
        this._id = _id;
        this.id = id;
        this.nameEn = nameEn;
        this.nameCn = nameCn;
        this.envKey = envKey;
        this.desc = desc;
        this.status = status;
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

    public List<Object> getEnvKey() {
        return envKey;
    }

    public void setEnvKey(List<Object> envKey) {
        this.envKey = envKey;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
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
        return "ModelTemplate{" +
                "_id=" + _id +
                ", id='" + id + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", nameCn='" + nameCn + '\'' +
                ", envKey=" + envKey +
                ", desc='" + desc + '\'' +
                ", status=" + status +
                ", ctime='" + ctime + '\'' +
                ", utime='" + utime + '\'' +
                ", opno='" + opno + '\'' +
                '}';
    }
}
