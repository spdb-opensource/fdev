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
import java.util.Map;

@Component
@Document(collection = Dict.HISTORY_DETAILS)
public class HistoryDetails {

	@Id
    private ObjectId _id;

    @Field("id")
    private String id;

    @ApiModelProperty(value = "更新人ID")
    private String updateUserId;

    @ApiModelProperty(value = "更新人姓名")
    private String updateUserName;

    @ApiModelProperty(value = "实体id")
    private String entityId;

    @ApiModelProperty(value = "实体英文名")
	private String entityNameEn;

	@ApiModelProperty(value = "实体中文名")
	private String entityNameCn;
	
	@ApiModelProperty(value = "操作类型")
	private String operateType;
	
	@ApiModelProperty(value = "字段列表")
    private List<Map<String, Object>> fields;

    @ApiModelProperty(value = "修改前的数据")
	private Map<String, Object> before;

	@ApiModelProperty(value = "修改后的数据")
	private Map<String, Object> after;

	@ApiModelProperty(value = "更新时间")
    private String updateTime;

    @ApiModelProperty(value = "环境英文名")
	private String envName;

	@ApiModelProperty(value = "描述")
	private String desc;

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

	public String getUpdateUserId() {
		return updateUserId;
	}

	public void setUpdateUserId(String updateUserId) {
		this.updateUserId = updateUserId;
	}

	public String getUpdateUserName() {
		return updateUserName;
	}

	public void setUpdateUserName(String updateUserName) {
		this.updateUserName = updateUserName;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getEntityNameEn() {
		return entityNameEn;
	}

	public void setEntityNameEn(String entityNameEn) {
		this.entityNameEn = entityNameEn;
	}

	public String getEntityNameCn() {
		return entityNameCn;
	}

	public void setEntityNameCn(String entityNameCn) {
		this.entityNameCn = entityNameCn;
	}

	public String getOperateType() {
		return operateType;
	}

	public void setOperateType(String operateType) {
		this.operateType = operateType;
	}

	public List<Map<String, Object>> getFields() {
		return fields;
	}

	public void setFields(List<Map<String, Object>> fields) {
		this.fields = fields;
	}

	public Map<String, Object> getBefore() {
		return before;
	}

	public void setBefore(Map<String, Object> before) {
		this.before = before;
	}

	public Map<String, Object> getAfter() {
		return after;
	}

	public void setAfter(Map<String, Object> after) {
		this.after = after;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
