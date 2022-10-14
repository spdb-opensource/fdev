package com.spdb.fdev.fuser.spdb.entity.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * @author 李尚林 on 2020/7/23
 * @c-lisl1
 **/
@Document(collection = "stuckPoint")
@ApiModel(value="卡点")
public class StuckPoint implements Serializable {

    private static final long serialVersionUID = -1526925873059692866L;

    @Id
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value="卡点ID")
    private String id;

    @Field("interface_name")
    @ApiModelProperty(value="接口名称")
    private String interface_name;//接口名称

    @Field("interface_desc")
    @ApiModelProperty(value="接口功能描述")
    private String interface_desc;//接口功能描述

    @Field("p_user_name")
    @ApiModelProperty(value="卡点用户名名称")
    private String p_user_name;//卡点用户名名称

    @Field("p_user_id")
    @ApiModelProperty(value="卡点用户ID")
    private String p_user_id;//卡点用户ID

    @Field("p_user_group")
    @ApiModelProperty(value="用户小组")
    private String p_user_group;//用户小组

    @Field("op_time")
    @ApiModelProperty(value="操作时间")
    private String op_time;//操作时间

    @Field("interface_data")
    @ApiModelProperty(value="接口数据")
    private String interface_data;//接口数据

    @Field("p_back_field")
    @ApiModelProperty(value="备用字段")
    private String p_back_field;//备用字段

    public StuckPoint() {
    }

    public StuckPoint(String id, String interface_name, String interface_desc, String p_user_name, String p_user_id, String p_user_group, String op_time, String interface_data, String p_back_field) {
        this.id = id;
        this.interface_name = interface_name;
        this.interface_desc = interface_desc;
        this.p_user_name = p_user_name;
        this.p_user_id = p_user_id;
        this.p_user_group = p_user_group;
        this.op_time = op_time;
        this.interface_data = interface_data;
        this.p_back_field = p_back_field;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInterface_name() {
        return interface_name;
    }

    public void setInterface_name(String interface_name) {
        this.interface_name = interface_name;
    }

    public String getInterface_desc() {
        return interface_desc;
    }

    public void setInterface_desc(String interface_desc) {
        this.interface_desc = interface_desc;
    }

    public String getP_user_name() {
        return p_user_name;
    }

    public void setP_user_name(String p_user_name) {
        this.p_user_name = p_user_name;
    }

    public String getP_user_id() {
        return p_user_id;
    }

    public void setP_user_id(String p_user_id) {
        this.p_user_id = p_user_id;
    }

    public String getP_user_group() {
        return p_user_group;
    }

    public void setP_user_group(String p_user_group) {
        this.p_user_group = p_user_group;
    }

    public String getOp_time() {
        return op_time;
    }

    public void setOp_time(String op_time) {
        this.op_time = op_time;
    }

    public String getInterface_data() {
        return interface_data;
    }

    public void setInterface_data(String interface_data) {
        this.interface_data = interface_data;
    }

    public String getP_back_field() {
        return p_back_field;
    }

    public void setP_back_field(String p_back_field) {
        this.p_back_field = p_back_field;
    }

    @Override
    public String toString() {
        return "StuckPoint{" +
                "id='" + id + '\'' +
                ", interface_name='" + interface_name + '\'' +
                ", interface_desc='" + interface_desc + '\'' +
                ", p_user_name='" + p_user_name + '\'' +
                ", p_user_id='" + p_user_id + '\'' +
                ", p_user_group='" + p_user_group + '\'' +
                ", op_time='" + op_time + '\'' +
                ", interface_data='" + interface_data + '\'' +
                ", p_back_field='" + p_back_field + '\'' +
                '}';
    }

    //存数据时，初始化_id和id
    public void initId() {
        ObjectId temp = new ObjectId();
        this._id = temp;
        this.id = temp.toString();
    }
}
