package com.auto.entity;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 元素字典实体
 */
@Component
public class Element implements Serializable {

	/** 元素ID **/
    private Integer elementId;

	/** 元素类型**/
    private String elementType;

	/** 元素内容 **/
    private String elementContent;

	/** 方法名称 **/
    private String elementName;

	/** 方法目录 **/
    private String elementDir;

	/** 是否被删除 **/
    private String deleted;

	/** 记录创建时间 **/
    private String createTime;

	/** 记录修改时间 **/
    private String modifyTime;

	/** 最后操作人 **/
    private String lastOpr;

    public Element(){}

	public Element(String elementType, String elementContent,
			String elementName, String elementDir, String deleted,
			String createTime, String modifyTime, String lastOpr) {
		super();
		this.elementType = elementType;
		this.elementContent = elementContent;
		this.elementName = elementName;
		this.elementDir = elementDir;
		this.deleted = deleted;
		this.createTime = createTime;
		this.modifyTime = modifyTime;
		this.lastOpr = lastOpr;
	}

	/**
	 * @return the elementId
	 */
	public Integer getElementId() {
		return elementId;
	}

	/**
	 * @param elementId the elementId to set
	 */
	public void setElementId(Integer elementId) {
		this.elementId = elementId;
	}

	/**
	 * @return the elementType
	 */
	public String getElementType() {
		return elementType;
	}

	/**
	 * @param elementType the elementType to set
	 */
	public void setElementType(String elementType) {
		this.elementType = elementType;
	}

	/**
	 * @return the elementContent
	 */
	public String getElementContent() {
		return elementContent;
	}

	/**
	 * @param elementContent the elementContent to set
	 */
	public void setElementContent(String elementContent) {
		this.elementContent = elementContent;
	}

	/**
	 * @return the elementName
	 */
	public String getElementName() {
		return elementName;
	}

	/**
	 * @param elementName the elementName to set
	 */
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	/**
	 * @return the elementDir
	 */
	public String getElementDir() {
		return elementDir;
	}

	/**
	 * @param elementDir the elementDir to set
	 */
	public void setElementDir(String elementDir) {
		this.elementDir = elementDir;
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