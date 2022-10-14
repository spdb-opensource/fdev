package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "release_batch_record")
@ApiModel(value = "投产模块批次记录")

public class ReleaseBatchRecord {

    @Id
    private ObjectId _id;

    @Field("batch_id")
    private String batch_id;

    @Field("release_node_name")
    private String release_node_name;

    @Field("application_id")
    private String application_id;

    @Field("modify_reason")
    private String modify_reason;

    @Field("user_name_en")
    private String user_name_en;

    @Field("user_name_cn")
    private String user_name_cn;

    @Field("create_time")
    private String create_time;

    @Field("update_time")
    @ApiModelProperty(value="修改时间")
    private String update_time;

    public String getBatch_id() {
        return batch_id;
    }

    public void setBatch_id(String batch_id) {
        this.batch_id = batch_id;
    }

    public String getRelease_node_name() {
        return release_node_name;
    }

    public void setRelease_node_name(String release_node_name) {
        this.release_node_name = release_node_name;
    }

    public String getModify_reason() {
        return modify_reason;
    }

    public void setModify_reason(String modify_reason) {
        this.modify_reason = modify_reason;
    }

    public String getUser_name_en() {
        return user_name_en;
    }

    public void setUser_name_en(String user_name_en) {
        this.user_name_en = user_name_en;
    }

    public String getUser_name_cn() {
        return user_name_cn;
    }

    public void setUser_name_cn(String user_name_cn) {
        this.user_name_cn = user_name_cn;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getApplication_id() {
        return application_id;
    }

    public void setApplication_id(String application_id) {
        this.application_id = application_id;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }
}