package com.auto.entity;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 组件实体
 */
@Component
public class Module implements Serializable {

    private Integer moduleId;

    private String moduleNo;

    private String moduleGroup;

    private String moduleName;

    private String moduleNameCn;

    private String deleted;

    private String createTime;

    private String modifyTime;

    private String lastOpr;


    public Integer getModuleId() {
        return moduleId;
    }

    public void setModuleId(Integer moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleNo() {
        return moduleNo;
    }

    public void setModuleNo(String moduleNo) {
        this.moduleNo = moduleNo;
    }

    public String getModuleGroup() {
        return moduleGroup;
    }

    public void setModuleGroup(String moduleGroup) {
        this.moduleGroup = moduleGroup;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleNameCn() {
        return moduleNameCn;
    }

    public void setModuleNameCn(String moduleNameCn) {
        this.moduleNameCn = moduleNameCn;
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

    public Module() {
    }
}