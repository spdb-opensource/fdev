package com.spdb.fdev.spdb.entity;

import com.spdb.fdev.base.dict.Dict;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Document(collection = Dict.SERVICE_CONFIG)
public class ServiceConfig {

	@Id
    private ObjectId _id;

    @ApiModelProperty(value = "应用GitId")
    private String serviceGitId;

    @ApiModelProperty(value = "分支")
    private String branch;

    @ApiModelProperty(value = "应用使用到的实体属性信息")
    private List<EntityField> entityField;

    @ApiModelProperty(value = "创建时间")
	private String createTime;

	@ApiModelProperty(value = "更新时间")
	private String updateTime;
	
	@ApiModelProperty(value = "逻辑删除")
	private String delete; // yes=删除

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public String getDelete() {
		return delete;
	}

	public void setDelete(String delete) {
		this.delete = delete;
	}

	public String getServiceGitId() {
		return serviceGitId;
	}

	public void setServiceGitId(String serviceGitId) {
		this.serviceGitId = serviceGitId;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public List<EntityField> getEntityField() {
		return entityField;
	}

	public void setEntityField(List<EntityField> entityField) {
		this.entityField = entityField;
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
}
