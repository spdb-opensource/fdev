package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document(collection = "release_applications")
@ApiModel(value = "投产模块应用")
@CompoundIndexes({
        @CompoundIndex(name = "rls_app_idx", def = "{'release_node_name': 1, 'application_id': 1}",unique = true)
})
public class ReleaseApplication implements Serializable {

    private static final long serialVersionUID = 8969805366582997662L;

    @Id
    @ApiModelProperty(value = "ID")
    private ObjectId _id;

    @Field("application_id")
    @ApiModelProperty(value = "应用编号")
    private String application_id;

    @Field("release_branch")
    @ApiModelProperty(value = "UAT测试分支gitlab地址")
    private String release_branch;

    @Field("release_node_name")
    @ApiModelProperty(value = "投产点名称")
    private String release_node_name;


    @Field("gray_temp_id")
    @ApiModelProperty(value = "灰度窗口使用模板id")
    private String gray_temp_id;

    @Field("proc_temp_id")
    @ApiModelProperty(value = "生产窗口使用模板id")
    private String proc_temp_id;

    @Field("uat_env_name")
    @ApiModelProperty(value = "UAT测试环境")
    private String uat_env_name;

    @Field("rel_env_name")
    @NotEmpty(message = "REL测试环境不能为空")
    @ApiModelProperty(value = "REL测试环境")
    private String rel_env_name;

    @Field("last_release_tag")
    @ApiModelProperty(value = "上次打包并投产的分支名称")
    private String last_release_tag;

    @Field("devops_tag")
    @ApiModelProperty(value = "本窗口下最后一次生成镜像的分支名称")
    private String devops_tag;

    @Field("fdev_config_changed")
    @ApiModelProperty(value = "配置文件是否有变化")
    private boolean fdev_config_changed;

    @Field("fdev_config_confirm")
    @ApiModelProperty(value = "配置文件变化审核状态")
    private String fdev_config_confirm;

    @Field("compare_url")
    @ApiModelProperty(value = "配置文件变化git比对地址")
    private String compare_url;

    @Field("fake_flag")
    @ApiModelProperty(value = "是否创建fake分支 0：不创建  1：创建")
    private String fake_flag;

    @Field("fake_image_name")
    @ApiModelProperty(value = "fake分支基础镜像名称")
    private String fake_image_name;

    @Field("fake_image_version")
    @ApiModelProperty(value = "fake分支基础镜像版本")
    private String fake_image_version;

    @Field("new_add_sign")
    @ApiModelProperty(value = "新增应用标识，1：新增应用，0：非新增")
    private String new_add_sign;

    @Field("create_time")
    @ApiModelProperty(value="创建时间")
    private String create_time;

    @Field("update_time")
    @ApiModelProperty(value="修改时间")
    private String update_time;

    @Field("path")
    @ApiModelProperty(value="包路径")
    private Map<String, String> path;

    @Field("static_resource")
    @ApiModelProperty(value="静态资源包路径")
    private Map<String, String> static_resource;

    @Field("app_sql")
    @ApiModelProperty(value="sql内容")
    private Map<String, Object> app_sql;

    public ReleaseApplication() {
        super();
    }

    public ReleaseApplication(String release_node_name, String application_id) {
        this.release_node_name = release_node_name;
        this.application_id = application_id;
    }

    public String getUat_env_name() {
        return uat_env_name;
    }

    public void setUat_env_name(String uat_env_name) {
        this.uat_env_name = uat_env_name;
    }

    public String getRel_env_name() {
        return rel_env_name;
    }

    public void setRel_env_name(String rel_env_name) {
        this.rel_env_name = rel_env_name;
    }

    public String getApplication_id() {
        return application_id;
    }

    public void setApplication_id(String application_id) {
        this.application_id = application_id;
    }

    public String getRelease_branch() {
        return release_branch;
    }

    public void setRelease_branch(String release_branch) {
        this.release_branch = release_branch;
    }

    public String getRelease_node_name() {
        return release_node_name;
    }

    public void setRelease_node_name(String release_node_name) {
        this.release_node_name = release_node_name;
    }

    public String getGray_temp_id() {
        return gray_temp_id;
    }

    public void setGray_temp_id(String gray_temp_id) {
        this.gray_temp_id = gray_temp_id;
    }

    public String getProc_temp_id() {
        return proc_temp_id;
    }

    public void setProc_temp_id(String proc_temp_id) {
        this.proc_temp_id = proc_temp_id;
    }

    public String getLast_release_tag() {
        return last_release_tag;
    }

    public void setLast_release_tag(String last_release_tag) {
        this.last_release_tag = last_release_tag;
    }

    public boolean isFdev_config_changed() {
        return fdev_config_changed;
    }

    public String getDevops_tag() {
        return devops_tag;
    }

    public void setDevops_tag(String devops_tag) {
        this.devops_tag = devops_tag;
    }

    public void setFdev_config_changed(boolean fdev_config_changed) {
        this.fdev_config_changed = fdev_config_changed;
    }

    public String getFdev_config_confirm() {
        return fdev_config_confirm;
    }

    public void setFdev_config_confirm(String fdev_config_confirm) {
        this.fdev_config_confirm = fdev_config_confirm;
    }

    public String getCompare_url() {
        return compare_url;
    }

    public void setCompare_url(String compare_url) {
        this.compare_url = compare_url;
    }

    public String getFake_flag() {
        return fake_flag;
    }

    public void setFake_flag(String fake_flag) {
        this.fake_flag = fake_flag;
    }

    public String getFake_image_name() {
        return fake_image_name;
    }

    public void setFake_image_name(String fake_image_name) {
        this.fake_image_name = fake_image_name;
    }

    public String getFake_image_version() {
        return fake_image_version;
    }

    public void setFake_image_version(String fake_image_version) {
        this.fake_image_version = fake_image_version;
    }

    public String getNew_add_sign() {
        return new_add_sign;
    }

    public void setNew_add_sign(String new_add_sign) {
        this.new_add_sign = new_add_sign;
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

    public Map<String, String> getPath() {
        return path;
    }

    public void setPath(Map<String, String> path) {
        this.path = path;
    }

    public Map<String, String> getStatic_resource() {
        return static_resource;
    }

    public void setStatic_resource(Map<String, String> static_resource) {
        this.static_resource = static_resource;
    }

    public Map<String, Object> getApp_sql() {
        return app_sql;
    }

    public void setApp_sql(Map<String, Object> app_sql) {
        this.app_sql = app_sql;
    }
}