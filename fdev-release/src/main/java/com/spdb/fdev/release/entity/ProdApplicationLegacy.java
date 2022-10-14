package com.spdb.fdev.release.entity;

import java.io.Serializable;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
/**
 * 非fdev应用变更
 */
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "prod_application_legacy")
@CompoundIndexes({
	@CompoundIndex(name = "prod_application_legacy_idx", def = "{'prod_id': 1, 'app_name_en': 1}" , unique = true)
})
public class ProdApplicationLegacy implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8232197632396045758L;
	
	@Field("prod_id")
	private String prod_id;
	
	@Field("app_name_en")
	private String app_name_en;
	
	@Field("pro_image_uri")
	private String pro_image_uri;
	
	public String getProd_id() {
		return prod_id;
	}
	public void setProd_id(String prod_id) {
		this.prod_id = prod_id;
	}
	public String getApp_name_en() {
		return app_name_en;
	}
	public void setApp_name_en(String app_name_en) {
		this.app_name_en = app_name_en;
	}
	public String getPro_image_uri() {
		return pro_image_uri;
	}
	public void setPro_image_uri(String pro_image_uri) {
		this.pro_image_uri = pro_image_uri;
	}
}
