package com.spdb.fdev.codeReview.spdb.entity;

import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

/**
 * @Author liux81
 * @DATE 2021/11/9
 */
@Component
@Document("code_file")
public class CodeFile extends BaseEntity{

    @Field("id")
    private String id;

    @Field("order_id")
    @ApiModelProperty("工单id")
    private String orderId;

    @Field("file_name")
    @ApiModelProperty("文件名")
    private String fileName;

    @Field("file_type")
    @ApiModelProperty("文件类型")
    //存对应字典表的key
    private String fileType;//需求规格说明书、代码审核表、原型图、需求说明书、接口文件、数据库文件。需规和代码审核表为必传

    @Field("file_path")
    @ApiModelProperty("文件路径")
    private String filePath;

    @Field("upload_user")
    @ApiModelProperty("上传者id")
    private String uploadUser;

    @Field("upload_time")
    @ApiModelProperty("上传时间")
    private String uploadTime;

    private String deleteButton;

    private String uploadUserName;

    private String fileTypeName;//文件类型名称

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUploadUser() {
        return uploadUser;
    }

    public void setUploadUser(String uploadUser) {
        this.uploadUser = uploadUser;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(String deleteButton) {
        this.deleteButton = deleteButton;
    }

    public String getUploadUserName() {
        return uploadUserName;
    }

    public void setUploadUserName(String uploadUserName) {
        this.uploadUserName = uploadUserName;
    }

    public String getFileTypeName() {
        return fileTypeName;
    }

    public void setFileTypeName(String fileTypeName) {
        this.fileTypeName = fileTypeName;
    }
}
