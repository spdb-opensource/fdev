package com.fdev.docmanage.entity;

import io.swagger.annotations.ApiModel;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "userInfo")
@ApiModel(value="用户操作文件的信息")
public class UserInfo {
	 //主键 id
    @Id
    private ObjectId _id;
	//操作类型（wps、minio）
	private String type;
	//上传阶段(仅针对涉及还原审核，上传阶段区分)
	private String uploadStage;
    //用户中文名
    private String user_name_cn;
    //用户英文名
    private String user_name_en;
    //具体的操作（创建文件夹  文件上传   文件修改  文件删除  文件编辑）
    private String operation;
    //要操作的文件名称
    private String fileName;
    //要操作的文件id
    private String fileId;
    //要操作的文件的父id（即文件夹id）
    private String parentId;
	//操作时间
    private LocalDateTime operation_date;
    
    public String get_id() {
        return _id.toString();
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

    public void set_id(Object _id) {
        if (_id instanceof ObjectId) {
            this._id = (ObjectId) _id;
        } else if (_id instanceof String) {
            this._id = new ObjectId((String) _id);
        }
    }

	public String getUploadStage() {
		return uploadStage;
	}

	public void setUploadStage(String uploadStage) {
		this.uploadStage = uploadStage;
	}

    public String getUser_name_cn() {
		return user_name_cn;
	}

	public void setUser_name_cn(String user_name_cn) {
		this.user_name_cn = user_name_cn;
	}

	public String getUser_name_en() {
		return user_name_en;
	}

	public void setUser_name_en(String user_name_en) {
		this.user_name_en = user_name_en;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

    public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getOperation_date() {
		return operation_date.toString();
	}

	public void setOperation_date(LocalDateTime operation_date) {
		this.operation_date = operation_date;
	}

	//存数据时，初始化_id
    public void initId() {
        ObjectId temp = new ObjectId();
        this.set_id(temp);
    }

	@Override
	public String toString() {
		return "UserInfo{" +
				"_id=" + _id +
				", type='" + type + '\'' +
				", uploadStage='" + uploadStage + '\'' +
				", user_name_cn='" + user_name_cn + '\'' +
				", user_name_en='" + user_name_en + '\'' +
				", operation='" + operation + '\'' +
				", fileName='" + fileName + '\'' +
				", fileId='" + fileId + '\'' +
				", parentId='" + parentId + '\'' +
				", operation_date=" + operation_date +
				'}';
	}

}
