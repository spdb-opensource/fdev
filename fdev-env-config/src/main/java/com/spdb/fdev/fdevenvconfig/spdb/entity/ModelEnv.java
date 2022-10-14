package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Map;

/**
 * @author xxx
 * @date 2019/7/5 13:15
 */
@Document(collection = "model-env")
@ApiModel(value = "实体环境值")
@CompoundIndexes({
        @CompoundIndex(name = "env_model_id", def = "{'env_id':1,'model_id':1}")
})
public class ModelEnv implements Serializable {

    private static final long serialVersionUID = -6109360094571509654L;

    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "实体环境id")
    private String id;

    @Field("env_id")
    @ApiModelProperty(value = "环境id")
    private String env_id;

    @Field("model_id")
    @ApiModelProperty(value = "实体id")
    private String model_id;

    @Field("desc")
    @ApiModelProperty(value = "实体环境描述")
    private String desc;

    @Field("variables")
    @ApiModelProperty(value = "实体key对应的值")
    private Map<String, Object> variables;

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
    @Field("version")
    @ApiModelProperty(value = "版本")
    private String version;

    public ModelEnv() {
    }

    public ModelEnv(ObjectId _id, String id, String env_id, String model_id, String desc, Map<String, Object> variables, String status, String ctime, String utime, String opno) {
        this._id = _id;
        this.id = id;
        this.env_id = env_id;
        this.model_id = model_id;
        this.desc = desc;
        this.variables = variables;
        this.status = status;
        this.ctime = ctime;
        this.utime = utime;
        this.opno = opno;
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

    public String getEnv_id() {
        return env_id;
    }

    public void setEnv_id(String env_id) {
        this.env_id = env_id;
    }

    public String getModel_id() {
        return model_id;
    }

    public void setModel_id(String model_id) {
        this.model_id = model_id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ModelEnv{" +
                "_id=" + _id +
                ", id='" + id + '\'' +
                ", env_id='" + env_id + '\'' +
                ", model_id='" + model_id + '\'' +
                ", desc='" + desc + '\'' +
                ", variables=" + variables +
                ", status='" + status + '\'' +
                ", ctime='" + ctime + '\'' +
                ", utime='" + utime + '\'' +
                ", opno='" + opno + '\'' +
                '}';
    }
}
