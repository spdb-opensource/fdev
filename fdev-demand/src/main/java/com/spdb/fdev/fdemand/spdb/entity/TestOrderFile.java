package com.spdb.fdev.fdemand.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fdemand.base.dict.Dict;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = Dict.TEST_ORDER_FILE)
public class TestOrderFile {

    @Id
    private ObjectId _id;

    @Field("id")
    private String id;

    @ApiModelProperty(value = "提测单id")
    private String test_order_id;

    @ApiModelProperty(value = "上传文件名称")
    private String file_name;

    @ApiModelProperty(value = "文件路径")
    private String file_path;

    @ApiModelProperty(value = "文件类型")
    private String file_type;

    @ApiModelProperty(value = "文件大小")
    private long file_size;

    @ApiModelProperty(value = "上传时间")
    private String upload_time;

    @ApiModelProperty(value = "上传人员id")
    private String upload_user_id;

    @Transient
    @ApiModelProperty(value = "上传人员")
    private UserInfo upload_user_info;

    public long getFile_size() {
        return file_size;
    }

    public void setFile_size(long file_size) {
        this.file_size = file_size;
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

    public String getTest_order_id() {
        return test_order_id;
    }

    public void setTest_order_id(String test_order_id) {
        this.test_order_id = test_order_id;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public String getUpload_time() {
        return upload_time;
    }

    public void setUpload_time(String upload_time) {
        this.upload_time = upload_time;
    }

    public String getUpload_user_id() {
        return upload_user_id;
    }

    public void setUpload_user_id(String upload_user_id) {
        this.upload_user_id = upload_user_id;
    }

    public UserInfo getUpload_user_info() {
        return upload_user_info;
    }

    public void setUpload_user_info(UserInfo upload_user_info) {
        this.upload_user_info = upload_user_info;
    }
}
