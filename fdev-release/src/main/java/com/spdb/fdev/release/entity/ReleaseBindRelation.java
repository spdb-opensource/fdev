package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "release_bind")
@ApiModel(value = "投产模块绑定应用")
public class ReleaseBindRelation {

    @Id
    private ObjectId _id;

    @Field("release_node_name")
    private String release_node_name;

    @Field("applications")
    private List<String> applications;

    @Field("batch_id")
    private String batch_id;

    @Field("create_time")
    @ApiModelProperty(value="创建时间")
    private String create_time;

    @Field("update_time")
    @ApiModelProperty(value="修改时间")
    private String update_time;

    public String getRelease_node_name() {
        return release_node_name;
    }

    public void setRelease_node_name(String release_node_name) {
        this.release_node_name = release_node_name;
    }

    public List<String> getApplications() {
        return applications;
    }

    public void setApplications(List<String> applications) {
        this.applications = applications;
    }

    public ObjectId get_id() { return _id; }

    public void set_id(ObjectId _id) { this._id = _id; }

    public String getBatch_id() { return batch_id; }

    public void setBatch_id(String batch_id) { this.batch_id = batch_id; }

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
}