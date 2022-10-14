package com.spdb.fdev.spdb.entity;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class EntityField {

    @ApiModelProperty(value = "实体英文名")
    private String entityNameEn;

    @ApiModelProperty(value = "字段列表")
    private Set<Object> fields;

    public EntityField() {
    }
    
	public EntityField(String key, Set<Object> value) {
		this.entityNameEn = key ;
		this.fields = value ;
	}

	public String getEntityNameEn() {
		return entityNameEn;
	}

	public void setEntityNameEn(String entityNameEn) {
		this.entityNameEn = entityNameEn;
	}

	public Set getFields() {
		return fields;
	}

	public void setFields(Set fields) {
		this.fields = fields;
	}
}
