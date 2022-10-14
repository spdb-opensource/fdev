package com.spdb.fdev.fuser.spdb.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;
import java.util.Set;

/**
 * @Author liux81
 * @DATE 2021/12/7
 */
@Document(collection = "interface_register")
@ApiModel(value = "接口权限登记表")
public class InterfaceRegister {
    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "记录ID")
    private String id;

    @Field("interface_path")
    @ApiModelProperty(value = "接口路径")
    private String interfacePath;

    @Field("role_ids")
    @ApiModelProperty(value = "角色id集合")
    private Set<String> roleIds;

    @Field("function_desc")
    @ApiModelProperty(value = "接口功能描述")
    private String functionDesc;

    @Field("status")
    @ApiModelProperty(value = "状态")
    private String status;//状态，using使用中，deleted已废弃

    @Field("create_user")
    @ApiModelProperty(value = "创建人")
    private String createUser;

    @Field("update_user")
    @ApiModelProperty(value = "更新人")
    private String updateUser;

    @Field("create_time")
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @Field("update_time")
    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    private Set<Map<String, Object>> roles;//角色信息集合

    private Integer updateButton;//编辑按钮，0可点击，1当前用户权限不足，2不展示

    private Integer deleteButton;//编辑按钮，0可点击，1当前用户权限不足，2不展示

    private Integer recoverButton;//编辑按钮，0可点击，1当前用户权限不足，2不展示

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

    public String getInterfacePath() {
        return interfacePath;
    }

    public void setInterfacePath(String interfacePath) {
        this.interfacePath = interfacePath;
    }

    public Set<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<String> roleIds) {
        this.roleIds = roleIds;
    }

    public String getFunctionDesc() {
        return functionDesc;
    }

    public void setFunctionDesc(String functionDesc) {
        this.functionDesc = functionDesc;
    }

    public Set<Map<String, Object>> getRoles() {
        return roles;
    }

    public void setRoles(Set<Map<String, Object>> roles) {
        this.roles = roles;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getUpdateButton() {
        return updateButton;
    }

    public void setUpdateButton(Integer updateButton) {
        this.updateButton = updateButton;
    }

    public Integer getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(Integer deleteButton) {
        this.deleteButton = deleteButton;
    }

    public Integer getRecoverButton() {
        return recoverButton;
    }

    public void setRecoverButton(Integer recoverButton) {
        this.recoverButton = recoverButton;
    }
}
