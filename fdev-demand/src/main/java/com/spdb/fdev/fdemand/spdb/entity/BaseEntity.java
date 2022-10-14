package com.spdb.fdev.fdemand.spdb.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

/**
 * @author zhanghp4
 */
public class BaseEntity {
    @Id
    private ObjectId _id;

    @Field("create_user")
    @ApiModelProperty(value = "创建人")
    private String create_user;
    private UserInfo create_user_info;

    @Field("create_time")
    @ApiModelProperty(value = "创建时间")
    private String create_time;

    @Field("update_user")
    @ApiModelProperty(value = "更新人")
    private String update_user;

    @Field("update_time")
    @ApiModelProperty(value = "更新时间")
    private String update_time;


    @Field("delete_user")
    @ApiModelProperty(value = "删除人")
    private String delete_user;

    @Field("delete_time")
    @ApiModelProperty(value = "删除时间")
    private String delete_time;

    public String getCreate_user() {
        return create_user;
    }

    public void setCreate_user(String create_user) {
        this.create_user = create_user;
    }

    public UserInfo getCreate_user_info() {
        return create_user_info;
    }

    public void setCreate_user_info(UserInfo create_user_info) {
        this.create_user_info = create_user_info;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_user() {
        return update_user;
    }

    public void setUpdate_user(String update_user) {
        this.update_user = update_user;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getDelete_user() {
        return delete_user;
    }

    public void setDelete_user(String delete_user) {
        this.delete_user = delete_user;
    }

    public String getDelete_time() {
        return delete_time;
    }

    public void setDelete_time(String delete_time) {
        this.delete_time = delete_time;
    }

}
