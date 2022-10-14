package com.spdb.fdev.codeReview.spdb.entity;

import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author zhanghp4
 */
public class BaseEntity {
    @Id
    private ObjectId _id;

    @Field("create_user")
    @ApiModelProperty(value = "创建人")
    private String createUser;

    @Field("create_time")
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @Field("update_user")
    @ApiModelProperty(value = "更新人")
    private String updateUser;

    @Field("update_time")
    @ApiModelProperty(value = "更新时间")
    private String updateTime;


    @Field("delete_user")
    @ApiModelProperty(value = "删除人")
    private String deleteUser;

    @Field("delete_time")
    @ApiModelProperty(value = "删除时间")
    private String deleteTime;

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getDeleteUser() {
        return deleteUser;
    }

    public void setDeleteUser(String deleteUser) {
        this.deleteUser = deleteUser;
    }

    public String getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(String deleteTime) {
        this.deleteTime = deleteTime;
    }
}
