package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document(collection = "app-deploy-map")
@ApiModel(value = "应用实体关联")
public class AppDeployMapping implements Serializable {

    private static final long serialVersionUID = -3817793707400528425L;

    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "应用实体关联id")
    private String id;

    @Field("gitlabId")
    @Indexed(unique = true)
    @ApiModelProperty(value = "gitlabId")
    private Integer gitlabId;

    @Field("config_gitlabid")
    @ApiModelProperty(value = "外部配置参数项目的gitlabId")
    private Integer configGitlabId;

    @Field(Dict.VARIABLES)
    @ApiModelProperty(value = "应用与部署实体对应关系")
    private List<Map> variables;

    @Field("ctime")
    @ApiModelProperty(value = "创建时间")
    private String ctime;

    @Field("utime")
    @ApiModelProperty(value = "更新时间")
    private String utime;

    @Field("opno")
    @ApiModelProperty(value = "操作员")
    private String opno;

    @Field(Dict.MODELSET)
    private String modelSet;

    private List<Map<String, Object>> tagInfo;


    @Field(Dict.SCCMODELSET)
    @ApiModelProperty(value = "scc实体组id")
    private String scc_modeSet;

    @Field(Dict.SCCVARIABLES)
    @ApiModelProperty(value = "应用与scc部署实体对应关系")
    private List<Map> scc_variables;


    public AppDeployMapping() {
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

    public Integer getGitlabId() {
        return gitlabId;
    }

    public void setGitlabId(Integer gitlabId) {
        this.gitlabId = gitlabId;
    }

    public List<Map> getVariables() {
        return variables;
    }

    public void setVariables(List<Map> variables) {
        this.variables = variables;
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

    public Integer getConfigGitlabId() {
        return configGitlabId;
    }

    public void setConfigGitlabId(Integer configGitlabId) {
        this.configGitlabId = configGitlabId;
    }

    public String getModelSet() {
        return modelSet;
    }

    public void setModelSet(String modelSet) {
        this.modelSet = modelSet;
    }

    public List<Map<String, Object>> getTagInfo() {
        return tagInfo;
    }

    public void setTagInfo(List<Map<String, Object>> tagInfo) {
        this.tagInfo = tagInfo;
    }

    public String getScc_modeSet() { return scc_modeSet; }

    public void setScc_modeSet(String scc_modeSet) { this.scc_modeSet = scc_modeSet;}

    public List<Map> getScc_variables() { return scc_variables; }

    public void setScc_variables(List<Map> scc_variables) { this.scc_variables = scc_variables; }

    @Override
    public String toString() {
        return "AppDeployMapping{" +
                "_id=" + _id +
                ", id='" + id + '\'' +
                ", gitlabId=" + gitlabId +
                ", configGitlabId=" + configGitlabId +
                ", variables=" + variables +
                ", ctime='" + ctime + '\'' +
                ", utime='" + utime + '\'' +
                ", opno='" + opno + '\'' +
                ", modelSet='" + modelSet + '\'' +
                ", tagInfo=" + tagInfo +
                '}';
    }
}
