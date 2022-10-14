package com.auto.entity;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 用例详情实体
 */
@Component
public class CaseDetail implements Serializable {

    private Integer detailId;

    private String testcaseNo;

    private String stepNo;

    private String moduleId;

    private String elementType;

    private String assertId;

    private String elementId;

    private String elementData;

    private String exeTimes;

    private String deleted;

    private String createTime;

    private String modifyTime;

    private String lastOpr;

    public Integer getDetailId() {
        return detailId;
    }

    public void setDetailId(Integer detailId) {
        this.detailId = detailId;
    }

    public String getTestcaseNo() {
        return testcaseNo;
    }

    public void setTestcaseNo(String testcaseNo) {
        this.testcaseNo = testcaseNo;
    }

    public String getStepNo() {
        return stepNo;
    }

    public void setStepNo(String stepNo) {
        this.stepNo = stepNo;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String getAssertId() {
        return assertId;
    }

    public void setAssertId(String assertId) {
        this.assertId = assertId;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
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

    public CaseDetail() {
    }

    public CaseDetail(String testcaseNo, String stepNo, String moduleId, String elementType, String assertId, String elementId, String elementData, String exeTimes, String deleted, String createTime, String modifyTime, String lastOpr) {
        this.testcaseNo = testcaseNo;
        this.stepNo = stepNo;
        this.moduleId = moduleId;
        this.elementType = elementType;
        this.assertId = assertId;
        this.elementId = elementId;
        this.elementData = elementData;
        this.exeTimes = exeTimes;
        this.deleted = deleted;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
        this.lastOpr = lastOpr;
    }
}