package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * create by Idea
 *
 * @Author aric
 * @Date 2020/1/6 14:49
 * @Version 1.0
 */
@Document(collection = "app-config-map")
@ApiModel("应用与配置实体关系")
@CompoundIndexes({
        @CompoundIndex(name = "app_branch", def = "{'appId': 1, 'branch': 1}")
})
public class AppConfigMapping {

    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @Indexed(unique = true)
    @ApiModelProperty(value = "id")
    private String id;

    @Field("appId")
    @ApiModelProperty(value = "应用 id")
    private Integer appId;

    @Field("branch")
    @ApiModelProperty(value = "分支")
    private String branch;

    @Field("modelField")
    @ApiModelProperty("应用使用到的实体属性信息")
    private List<ModelField> modelField;

    // 标识是否为node应用的config    1：是   非1：否
    @Field(Dict.NODE)
    private String node;

    @Field("ctime")
    @ApiModelProperty(value = "创建时间")
    private String ctime;

    @Field("utime")
    @ApiModelProperty(value = "更新时间")
    private String utime;

    public AppConfigMapping() {
    }

    public AppConfigMapping(Integer appId, String branch, String node) {
        this.appId = appId;
        this.branch = branch;
        this.node = node;
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

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public List<ModelField> getModelField() {
        return modelField;
    }

    public void setModelField(List<ModelField> modelField) {
        this.modelField = modelField;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
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

}
