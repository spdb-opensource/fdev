package com.spdb.fdev.spdb.entity;

public class EntityUser {

	 /**
     * 用户id
     */
    private String userId;

	/**
     * 用户英文名
     */
    private String nameEn;
    /**
     * 用户中文名
     */
    private String nameCn;

    public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
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

	public EntityUser() {
	}

	public EntityUser(String userId, String nameEn, String nameCn) {
		this.userId = userId;
		this.nameEn = nameEn;
		this.nameCn = nameCn;
	}
}
