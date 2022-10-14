package com.spdb.fdev.fdemand.spdb.entity;
import org.springframework.data.mongodb.core.mapping.Field;

public class UserInfo {
    /**
     * 用户id
     */
	@Field("id")
    private String id;

    /**
     * 用户中文名
     */
    private String user_name_cn;

    /**
     * 用户英文名
     */
    private String user_name_en;

	/**
	 * 邮箱
	 */
	private String user_email;

	public String getUser_email() {
		return user_email;
	}

	public void setUser_email(String user_email) {
		this.user_email = user_email;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
    
}
