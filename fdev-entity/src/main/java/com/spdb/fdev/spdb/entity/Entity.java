package com.spdb.fdev.spdb.entity;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import com.spdb.fdev.base.dict.Dict;

import io.swagger.annotations.ApiModelProperty;

@Component
@Document(collection = Dict.ENTITY)
public class Entity {

	@Id
    private ObjectId _id;

    @Field("id")
    private String id;

    @Indexed(unique = false)
    @ApiModelProperty(value = "实体英文名")
    private String nameEn;

    @ApiModelProperty(value = "实体中文名")
    private String nameCn;

    @ApiModelProperty(value = "实体创建人")
    private EntityUser createUser;
    
    @Transient
	@ApiModelProperty(value = "实体创建人ID")
    private String createUserId;

    @Transient
    @ApiModelProperty(value = "实体创建人姓名")
    private String createUserName;

	@ApiModelProperty(value = "实体创建时间")
    private String createTime;

    @ApiModelProperty(value = "最新修改人")
    private EntityUser updateUser;
    
    @Transient
    @ApiModelProperty(value = "最新修改人id")
    private String updateUserId;
    
	@Transient
    @ApiModelProperty(value = "最新修改人姓名")
    private String updateUserName;

    @ApiModelProperty(value = "最新修改时间")
    private String updateTime;

    @ApiModelProperty(value = "实体模板")
    private String templateId;
    
    @Transient
    @ApiModelProperty(value = "实体模板名称")
    private String templateName;
    
    @ApiModelProperty(value = "实体字段列表")
    private List<Map<String, Object>> properties;
    
    @ApiModelProperty(value = "实体字段值")
    private Map<String, Object> propertiesValue;

	@ApiModelProperty(value = "逻辑删除标记")
	private String delete;

    public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	
	public String getCreateUserName() {
		return createUserName;
	}

	public void setCreateUserName(String createUserName) {
		this.createUserName = createUserName;
	}

	public String getUpdateUserName() {
		return updateUserName;
	}

	public void setUpdateUserName(String updateUserName) {
		this.updateUserName = updateUserName;
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

	public String getNameEn() {
		return nameEn;
	}

	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}

	public String getNameCn() {
		return nameCn;
	}

	public void setNameCn(String nameCn) {
		this.nameCn = nameCn;
	}

	public String getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getUpdateUserId() {
		return updateUserId;
	}

	public void setUpdateUserId(String updateUserId) {
		this.updateUserId = updateUserId;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public List<Map<String, Object>> getProperties() {
		return properties;
	}

	public void setProperties(List<Map<String, Object>> properties) {
		this.properties = properties;
	}

	public Map<String, Object> getPropertiesValue() {
		return propertiesValue;
	}

	public void setPropertiesValue(Map<String, Object> propertiesValue) {
		this.propertiesValue = propertiesValue;
	}

    public EntityUser getCreateUser() {
		return createUser;
	}

	public void setCreateUser(EntityUser createUser) {
		this.createUser = createUser;
	}

	public EntityUser getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(EntityUser updateUser) {
		this.updateUser = updateUser;
	}

	public String getDelete() {
		return delete;
	}

	public void setDelete(String delete) {
		this.delete = delete;
	}
}
