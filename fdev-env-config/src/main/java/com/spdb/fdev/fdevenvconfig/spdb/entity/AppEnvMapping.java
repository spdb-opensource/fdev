package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

@Document(collection = "app-env-map")
@ApiModel(value = "应用环境关联")
public class AppEnvMapping implements Serializable {

    private static final long serialVersionUID = 5879486337073106263L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    private String id;

    @Field(Dict.APPID)
    private String app_id;

    @Field(Dict.APP_NAME)
    private String app_name;

    @Field(Dict.ENV_ID)
    private String env_id;

    @Field(Dict.SCC_SIT_ID)
    private String scc_sit_id;

    @Field(Dict.SCC_UAT_ID)
    private String scc_uat_id;

    @Field(Dict.SCC_REL_ID)
    private String scc_rel_id;

    @Field(Dict.SCC_PRO_ID)
    private String scc_pro_id;

    @Field(Dict.ENV_NAME)
    private String env_name;


    @Field(Dict.CREATE_TIME)
    private String createTime;

    @Field(Dict.UPDATE_TIME)
    private String updateTime;


    @Field(Dict.TAGS)
    private List<String> tags;

    @Field(Dict.NETWORK)
    private String network;

    @Field("opno")
    private String opno;


    public AppEnvMapping() {
    }

    public String getScc_sit_id() {
        return scc_sit_id;
    }

    public void setScc_sit_id(String scc_sit_id) {
        this.scc_sit_id = scc_sit_id;
    }

    public String getScc_uat_id() {
        return scc_uat_id;
    }

    public void setScc_uat_id(String scc_uat_id) {
        this.scc_uat_id = scc_uat_id;
    }

    public String getScc_rel_id() {
        return scc_rel_id;
    }

    public void setScc_rel_id(String scc_rel_id) {
        this.scc_rel_id = scc_rel_id;
    }

    public String getScc_pro_id() {
        return scc_pro_id;
    }

    public void setScc_pro_id(String scc_pro_id) {
        this.scc_pro_id = scc_pro_id;
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

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getEnv_id() {
        return env_id;
    }

    public void setEnv_id(String env_id) {
        this.env_id = env_id;
    }

    public String getEnv_name() {
        return env_name;
    }

    public void setEnv_name(String env_name) {
        this.env_name = env_name;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getOpno() {
        return opno;
    }

    public void setOpno(String opno) {
        this.opno = opno;
    }

    @Override
    public String toString() {
        return "AppEnvMapping{" +
                "id='" + id + '\'' +
                ", app_id='" + app_id + '\'' +
                ", app_name='" + app_name + '\'' +
                ", env_id='" + env_id + '\'' +
                ", scc_sit_id='" + scc_sit_id + '\'' +
                ", scc_uat_id='" + scc_uat_id + '\'' +
                ", scc_rel_id='" + scc_rel_id + '\'' +
                ", scc_pro_id='" + scc_pro_id + '\'' +
                ", env_name='" + env_name + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", tags=" + tags +
                ", network='" + network + '\'' +
                ", opno='" + opno + '\'' +
                '}';
    }
}
