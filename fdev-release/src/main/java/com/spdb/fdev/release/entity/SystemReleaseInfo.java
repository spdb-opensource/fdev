package com.spdb.fdev.release.entity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Document(collection = "system_release_info")
@ApiModel(value = "各系统投产信息表")
public class SystemReleaseInfo implements Serializable {

	private static final long serialVersionUID = -5865317568352828598L;

	
	@Field("system_id")
	@Indexed(unique = true)
	@ApiModelProperty(value = "所属系统id")
	private String system_id;
	
	@Field("resource_giturl")
	@ApiModelProperty(value = "resource项目gitlab仓库地址")
	private String resource_giturl;
	
	@Field("resource_projectid")
	@ApiModelProperty(value = "resource项目projectid")
	private String resource_projectid;

	@Field("config_type")
	@ApiModelProperty(value = "配置文件类型")
	private String config_type;


	public String getResource_projectid() {
		return resource_projectid;
	}

	public void setResource_projectid(String resource_projectid) {
		this.resource_projectid = resource_projectid;
	}

	public void setResource_giturl(String resource_giturl) {
		this.resource_giturl = resource_giturl;
	}

	public String getSystem_id() {
		return system_id;
	}

	public void setSystem_id(String system_id) {
		this.system_id = system_id;
	}

	public String getResource_giturl() {
		return resource_giturl;
	}

	public String getConfig_type() {
		return config_type;
	}

	public void setConfig_type(String config_type) {
		this.config_type = config_type;
	}
	
}




