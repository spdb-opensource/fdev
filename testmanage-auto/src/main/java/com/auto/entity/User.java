package com.auto.entity;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 登陆用户实体
 */
@Component
public class User implements Serializable {

	/** 登陆用户ID **/
    private Integer userId;

	/** 登陆用户名 **/
    private String userName;

	/** 登陆密码 **/
    private String queryPwd;

	/** 是否被删除 **/
    private String deleted;

	/** 记录创建时间 **/
    private String createTime;

	/** 记录修改时间 **/
    private String modifyTime;

	/** 最后操作人 **/
    private String lastOpr;

	/**
	 * @return the userId
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the queryPwd
	 */
	public String getQueryPwd() {
		return queryPwd;
	}

	/**
	 * @param queryPwd the queryPwd to set
	 */
	public void setQueryPwd(String queryPwd) {
		this.queryPwd = queryPwd;
	}

	/**
	 * @return the deleted
	 */
	public String getDeleted() {
		return deleted;
	}

	/**
	 * @param deleted the deleted to set
	 */
	public void setDeleted(String deleted) {
		this.deleted = deleted;
	}

	/**
	 * @return the createTime
	 */
	public String getCreateTime() {
		return createTime;
	}

	/**
	 * @param createTime the createTime to set
	 */
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	/**
	 * @return the modifyTime
	 */
	public String getModifyTime() {
		return modifyTime;
	}

	/**
	 * @param modifyTime the modifyTime to set
	 */
	public void setModifyTime(String modifyTime) {
		this.modifyTime = modifyTime;
	}

	/**
	 * @return the lastOpr
	 */
	public String getLastOpr() {
		return lastOpr;
	}

	/**
	 * @param lastOpr the lastOpr to set
	 */
	public void setLastOpr(String lastOpr) {
		this.lastOpr = lastOpr;
	}

	public User(){
		
	}
	
	public User(String userName, String queryPwd, String deleted,
			String createTime, String modifyTime, String lastOpr) {
		this.userName = userName;
		this.queryPwd = queryPwd;
		this.deleted = deleted;
		this.createTime = createTime;
		this.modifyTime = modifyTime;
		this.lastOpr = lastOpr;
	}

    
}