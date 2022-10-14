package com.auto.entity;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 组件详情实体
 */
@Component
public class ModuleDetail implements Serializable {

    private Integer moduleDetailId;

    private String moduleId;

    private String elementStepNo;

    private String elementId;

    private String elementType;

    private String elementData;

    private String exeTimes;

    private String deleted;

    private String createTime;

    private String modifyTime;

    private String lastOpr;

    private String moduleName;

    private String elementName;

    public Integer getModuleDetailId() {
        return moduleDetailId;
    }

    public void setModuleDetailId(Integer moduleDetailId) {
        this.moduleDetailId = moduleDetailId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getElementStepNo() {
        return elementStepNo;
    }

    public void setElementStepNo(String elementStepNo) {
        this.elementStepNo = elementStepNo;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String getElementData() {
        return elementData;
    }

    public void setElementData(String elementData) {
        this.elementData = elementData;
    }

    public String getExeTimes() {
        return exeTimes;
    }

    public void setExeTimes(String exeTimes) {
        this.exeTimes = exeTimes;
    }

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getLastOpr() {
        return lastOpr;
    }

    public void setLastOpr(String lastOpr) {
        this.lastOpr = lastOpr;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public ModuleDetail() {
    }
}