package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
@Document(collection = "automation_env")
@ApiModel(value = "fdev环境名称对应表")
public class AutomationEnv implements Serializable {

    private static final long serialVersionUID = 6957099078066190901L;

    @Id
    @ApiModelProperty(value = "ID")
    private ObjectId _id;

    @Field("id")
    @Indexed(unique = true)
    private String id;

    @Field("env_name")
    @Indexed(unique = true)
    @ApiModelProperty(value = "自动化发布环境")
    private String env_name;

    @Field("fdev_env_name")
    @ApiModelProperty(value = "对应fdev环境")
    private Map<String, Object> fdev_env_name;

    @Field("scc_fdev_env_name")
    @ApiModelProperty(value = "对应fdev环境")
    private Map<String, Object> scc_fdev_env_name;

    @Field("scc_namespace")
    @ApiModelProperty(value = "scc对应租户")
    private Map<String, Object> scc_namespace;

    @Field("description")
    @ApiModelProperty(value = "描述")
    private String description;

    @Field("create_time")
    @ApiModelProperty(value = "创建时间")
    private String create_time;

    @Field("update_time")
    @ApiModelProperty(value = "修改时间")
    private String update_time;

    @Field("platform")
    @ApiModelProperty(value = "部署平台")
    private List<String> platform;

    public AutomationEnv() {
        super();
    }

    public AutomationEnv(String env_name) {
        this.env_name = env_name;
    }

    public AutomationEnv(String id, String env_name, Map<String, Object> fdev_env_name, Map<String, Object> scc_fdev_env_name, String description, List<String> platform) {
        this.id = id;
        this.env_name = env_name;
        this.fdev_env_name = fdev_env_name;
        this.scc_fdev_env_name = scc_fdev_env_name;
        this.description = description;
        this.platform = platform;
    }

    public AutomationEnv(ObjectId _id, String id, String env_name, Map<String, Object> fdev_env_name, Map<String, Object> scc_fdev_env_name, String description, List<String> platform) {
        this._id = _id;
        this.id = id;
        this.env_name = env_name;
        this.fdev_env_name = fdev_env_name;
        this.scc_fdev_env_name = scc_fdev_env_name;
        this.description = description;
        this.platform = platform;
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

    public String getEnv_name() {
        return env_name;
    }

    public void setEnv_name(String env_name) {
        this.env_name = env_name;
    }

    public Map<String, Object> getFdev_env_name() {
        return fdev_env_name;
    }

    public void setFdev_env_name(Map<String, Object> fdev_env_name) {
        this.fdev_env_name = fdev_env_name;
    }

    public Map<String, Object> getScc_fdev_env_name() {
        return scc_fdev_env_name;
    }

    public void setScc_fdev_env_name(Map<String, Object> scc_fdev_env_name) {
        this.scc_fdev_env_name = scc_fdev_env_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public List<String> getPlatform() {
        return platform;
    }

    public void setPlatform(List<String> platform) {
        this.platform = platform;
    }

    public Map<String, Object> getScc_namespace() {
        return scc_namespace;
    }

    public void setScc_namespace(Map<String, Object> scc_namespace) {
        this.scc_namespace = scc_namespace;
    }
}
