package com.auto.entity;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 元素字典实体
 */
@Component
public class ElementDic implements Serializable {

	/** 元素字典ID **/
    private Integer elementDicId;

	/** 元素方法**/
    private String elementMethod;

	/** 元素参数 **/
    private String elementParam;

	/** 方法简述 **/
    private String methodDesc;

	/** 是否被删除 **/
    private String deleted;

	/** 记录创建时间 **/
    private String createTime;

	/** 记录修改时间 **/
    private String modifyTime;

	/** 最后操作人 **/
    private String lastOpr;

    public ElementDic(){}
    
	public ElementDic(String elementMethod, String elementParam,
			String methodDesc, String deleted, String createTime,
			String modifyTime, String lastOpr) {
		this.elementMethod = elementMethod;
		this.elementParam = elementParam;
		this.methodDesc = methodDesc;
		this.deleted = deleted;
		this.createTime = createTime;
		this.modifyTime = modifyTime;
		this.lastOpr = lastOpr;
	}

	/**
	 * @return the elementDicId
	 */
	public Integer getElementDicId() {
		return elementDicId;
	}

	/**
	 * @param elementDicId the elementDicId to set
	 */
	public void setElementDicId(Integer elementDicId) {
		this.elementDicId = elementDicId;
	}

	/**
	 * @return the elementMethod
	 */
	public String getElementMethod() {
		return elementMethod;
	}

	/**
	 * @param elementMethod the elementMethod to set
	 */
	public void setElementMethod(String elementMethod) {
		this.elementMethod = elementMethod;
	}

	/**
	 * @return the elementParam
	 */
	public String getElementParam() {
		return elementParam;
	}

	/**
	 * @param elementParam the elementParam to set
	 */
	public void setElementParam(String elementParam) {
		this.elementParam = elementParam;
	}

	/**
	 * @return the methodDesc
	 */
	public String getMethodDesc() {
		return methodDesc;
	}

	/**
	 * @param methodDesc the methodDesc to set
	 */
	public void setMethodDesc(String methodDesc) {
		this.methodDesc = methodDesc;
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

	

    
}