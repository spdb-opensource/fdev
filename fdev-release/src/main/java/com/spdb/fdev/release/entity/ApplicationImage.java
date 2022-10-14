package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "application_image")
@ApiModel(value = "推送镜像记录表")
public class ApplicationImage {

    @Id
    @ApiModelProperty(value = "ID")
    private ObjectId _id;

    @Field("id")
    @Indexed(unique = true)
    private String id;

    @Field("application_id")
    @ApiModelProperty(value = "应用id")
    private String application_id;

    @Field("app_name_en")
    @ApiModelProperty(value = "应用英文名")
    private String app_name_en;

    @Field("prod_id")
    @ApiModelProperty(value = "变更id")
    private String prod_id;

    @Field("version")
    @ApiModelProperty(value = "变更版本号")
    private String version;

    @Field("release_node_name")
    @ApiModelProperty(value = "投产窗口名称")
    private String release_node_name;

    @Field("namaspace")
    @ApiModelProperty(value = "镜像空间名称")
    private String namaspace;

    @Field("image_uri")
    @ApiModelProperty(value = "推送镜像名称")
    private String image_uri;

    @Field("status")
    @ApiModelProperty(value = "推送镜像状态0：未开始推送 1：推送中 2：推送成功 3：推送失败")
    private String status;

    @Field("log_path")
    @ApiModelProperty(value = "日志保存地址")
    private String log_path;

    @Field("push_image_log")
    @ApiModelProperty(value = "镜像推送日志")
    private String push_image_log;

    @Field("create_time")
    @ApiModelProperty(value="创建时间")
    private String create_time;

    @Field("update_time")
    @ApiModelProperty(value="修改时间")
    private String update_time;

    @Field("deploy_type")
    @ApiModelProperty(value="部署平台")
    private String deploy_type;

    public ApplicationImage() {
        super();
    }

    public ApplicationImage(ObjectId _id, String id, String application_id, String app_name_en, String prod_id, String version, String release_node_name, String namaspace, String image_uri, String status, String log_path, String push_image_log, String create_time, String update_time,String deploy_type) {
        this._id = _id;
        this.id = id;
        this.application_id = application_id;
        this.app_name_en = app_name_en;
        this.prod_id = prod_id;
        this.version = version;
        this.release_node_name = release_node_name;
        this.namaspace = namaspace;
        this.image_uri = image_uri;
        this.status = status;
        this.log_path = log_path;
        this.push_image_log = push_image_log;
        this.create_time = create_time;
        this.update_time = update_time;
        this.deploy_type = deploy_type;
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

    public String getApplication_id() {
        return application_id;
    }

    public void setApplication_id(String application_id) {
        this.application_id = application_id;
    }

    public String getApp_name_en() {
        return app_name_en;
    }

    public void setApp_name_en(String app_name_en) {
        this.app_name_en = app_name_en;
    }

    public String getProd_id() {
        return prod_id;
    }

    public void setProd_id(String prod_id) {
        this.prod_id = prod_id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRelease_node_name() {
        return release_node_name;
    }

    public void setRelease_node_name(String release_node_name) {
        this.release_node_name = release_node_name;
    }

    public String getNamaspace() {
        return namaspace;
    }

    public void setNamaspace(String namaspace) {
        this.namaspace = namaspace;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLog_path() {
        return log_path;
    }

    public void setLog_path(String log_path) {
        this.log_path = log_path;
    }

    public String getPush_image_log() {
        return push_image_log;
    }

    public void setPush_image_log(String push_image_log) {
        this.push_image_log = push_image_log;
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

    public String getDeploy_type() {
        return deploy_type;
    }

    public void setDeploy_type(String deploy_type) {
        this.deploy_type = deploy_type;
    }
}
