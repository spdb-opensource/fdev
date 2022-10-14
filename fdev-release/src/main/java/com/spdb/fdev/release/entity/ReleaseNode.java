package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Document(collection = "release_nodes")
@ApiModel(value = "投产管理")
public class ReleaseNode implements Serializable {

    private static final long serialVersionUID = -5175297197143066658L;
    @Id
    @ApiModelProperty(value = "ID")
    private ObjectId _id;

    @Field("release_date")
    @ApiModelProperty(value = "投产日期")
    @NotEmpty(message = "投产日期不能为空")
    private String release_date;

    @Field("release_node_name")
    @NotEmpty(message = "投产点名称不能为空")
    @ApiModelProperty(value = "投产点名称")
    @Indexed(unique = true)
    private String release_node_name;

    @Field("create_user")
    @ApiModelProperty(value = "投产窗口创建人员id")
    private String create_user;

    @Field("create_user_name_en")
    @ApiModelProperty(value = "投产窗口创建人员英文名")
    private String create_user_name_en;

    @Field("create_user_name_cn")
    @ApiModelProperty(value = "投产窗口创建人员中文名")
    private String create_user_name_cn;

    @Field("owner_groupId")
    @NotEmpty(message = "所属小组Id不能为空")
    @ApiModelProperty(value = "所属小组Id")
    private String owner_groupId;

    @Field("release_manager")
    @NotEmpty(message = "投产管理员英文名不能为空")
    @ApiModelProperty(value = "投产管理员")
    private String release_manager;

    @Field("release_manager_id")
    @NotEmpty(message = "投产管理员id不能为空")
    @ApiModelProperty(value = "投产管理员")
    private String release_manager_id;

    @Field("release_manager_name_cn")
    @NotEmpty(message = "投产管理员不能为空")
    @ApiModelProperty(value = "投产管理员")
    private String release_manager_name_cn;

    @Field("release_spdb_manager")
    @ApiModelProperty(value = "投产行内管理员")
    private String release_spdb_manager;

    @Field("release_spdb_manager_id")
    @ApiModelProperty(value = "投产行内管理员id")
    private String release_spdb_manager_id;

    @Field("release_spdb_manager_name_cn")
    @ApiModelProperty(value = "投产行内管理员中文名")
    private String release_spdb_manager_name_cn;

    @Field("node_status")
    @ApiModelProperty(value = "窗口状态,1 - created,0-deserted")
    private String node_status;

    @Field("type")
    @ApiModelProperty(value = "窗口类型,0 - 投产大窗口 1 - 微服务窗口，2 - 原生窗口，3 - 试运行窗口")
    private String type;

    @Field("create_time")
    @ApiModelProperty(value = "创建时间")
    private String create_time;

    @Field("update_time")
    @ApiModelProperty(value = "更新时间")
    private String update_time;

    @Field("release_contact")
    @ApiModelProperty(value = "投产大窗口牵头人")
    private List<String> release_contact;

    public List<String> getRelease_contact() {
        return release_contact;
    }

    public void setRelease_contact(List<String> release_contact) {
        this.release_contact = release_contact;
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

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getRelease_node_name() {
        return release_node_name;
    }

    public void setRelease_node_name(String release_node_name) {
        this.release_node_name = release_node_name;
    }

    public String getCreate_user() {
        return create_user;
    }

    public void setCreate_user(String create_user) {
        this.create_user = create_user;
    }

    public String getCreate_user_name_en() {
        return create_user_name_en;
    }

    public void setCreate_user_name_en(String create_user_name_en) {
        this.create_user_name_en = create_user_name_en;
    }

    public String getCreate_user_name_cn() {
        return create_user_name_cn;
    }

    public void setCreate_user_name_cn(String create_user_name_cn) {
        this.create_user_name_cn = create_user_name_cn;
    }

    public String getOwner_groupId() {
        return owner_groupId;
    }

    public void setOwner_groupId(String owner_groupId) {
        this.owner_groupId = owner_groupId;
    }

    public String getRelease_manager() {
        return release_manager;
    }

    public void setRelease_manager(String release_manager) {
        this.release_manager = release_manager;
    }

    public String getRelease_manager_id() {
        return release_manager_id;
    }

    public void setRelease_manager_id(String release_manager_id) {
        this.release_manager_id = release_manager_id;
    }

    public String getRelease_manager_name_cn() {
        return release_manager_name_cn;
    }

    public void setRelease_manager_name_cn(String release_manager_name_cn) {
        this.release_manager_name_cn = release_manager_name_cn;
    }

    public String getRelease_spdb_manager() {
        return release_spdb_manager;
    }

    public void setRelease_spdb_manager(String release_spdb_manager) {
        this.release_spdb_manager = release_spdb_manager;
    }

    public String getRelease_spdb_manager_id() {
        return release_spdb_manager_id;
    }

    public void setRelease_spdb_manager_id(String release_spdb_manager_id) {
        this.release_spdb_manager_id = release_spdb_manager_id;
    }

    public String getRelease_spdb_manager_name_cn() {
        return release_spdb_manager_name_cn;
    }

    public void setRelease_spdb_manager_name_cn(String release_spdb_manager_name_cn) {
        this.release_spdb_manager_name_cn = release_spdb_manager_name_cn;
    }

    public String getNode_status() {
        return node_status;
    }

    public void setNode_status(String node_status) {
        this.node_status = node_status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
