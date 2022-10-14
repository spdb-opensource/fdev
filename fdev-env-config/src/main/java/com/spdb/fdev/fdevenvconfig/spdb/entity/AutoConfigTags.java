package com.spdb.fdev.fdevenvconfig.spdb.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document(collection = "auto_config_tags")
public class AutoConfigTags implements Serializable {

    private static final long serialVersionUID = -9060208741092845764L;

    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "tag的Id")
    private String id;

    @Field("ctime")
    @ApiModelProperty(value = "创建时间")
    private String ctime;

    @Field("gitlabId")
    @ApiModelProperty(value = "gitlabId")
    private Integer gitlabId;

    @Field("config_gitlabid")
    @ApiModelProperty(value = "外部配置参数项目的gitlabId")
    private Integer configGitlabId;

    @Field("tag_name")
    @ApiModelProperty(value = "tag名称")
    private String tag_name;

    @Field("pipeline_id")
    @ApiModelProperty(value = "pipeline_id")
    private String pipeline_id;

    @Field("tagInfo")
    @ApiModelProperty(value = "tag信息")
    private List<Map<String, Object>> tagInfo;

    public AutoConfigTags() {
    }

    public Integer getConfigGitlabId() {
        return configGitlabId;
    }

    public void setConfigGitlabId(Integer configGitlabId) {
        this.configGitlabId = configGitlabId;
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

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public Integer getGitlab_id() {
        return gitlabId;
    }

    public void setGitlab_id(Integer gitlabId) {
        this.gitlabId = gitlabId;
    }

    public String getTag_name() {
        return tag_name;
    }

    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }

    public String getPipeline_id() {
        return pipeline_id;
    }

    public void setPipeline_id(String pipeline_id) {
        this.pipeline_id = pipeline_id;
    }

    public List<Map<String, Object>> getTagInfo() {
        return tagInfo;
    }

    public void setTagInfo(List<Map<String, Object>> tagInfo) {
        this.tagInfo = tagInfo;
    }

    @Override
    public String toString() {
        return "AutoConfigTags{" +
                "_id=" + _id +
                ", id='" + id + '\'' +
                ", ctime='" + ctime + '\'' +
                ", gitlab_id=" + gitlabId +
                ", tag_name='" + tag_name + '\'' +
                ", pipeline_id='" + pipeline_id + '\'' +
                ", tagInfo='" + tagInfo + '\'' +
                '}';
    }
}