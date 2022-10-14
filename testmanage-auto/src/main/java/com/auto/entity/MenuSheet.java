package com.auto.entity;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 模块菜单实体
 */
@Component
public class MenuSheet implements Serializable {

    private Integer menuSheetId;

    private String menuName;

    private String menuNo;

    private String secondaryMenu;

    private String secondaryMenuNo;

    private String thirdMenu;

    private String thirdMenuNo;

    private String deleted;

    private String createTime;

    private String modifyTime;

    private String lastOpr;

    public Integer getMenuSheetId() {
        return menuSheetId;
    }

    public void setMenuSheetId(Integer menuSheetId) {
        this.menuSheetId = menuSheetId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getMenuNo() {
        return menuNo;
    }

    public void setMenuNo(String menuNo) {
        this.menuNo = menuNo;
    }

    public String getSecondaryMenu() {
        return secondaryMenu;
    }

    public void setSecondaryMenu(String secondaryMenu) {
        this.secondaryMenu = secondaryMenu;
    }

    public String getSecondaryMenuNo() {
        return secondaryMenuNo;
    }

    public void setSecondaryMenuNo(String secondaryMenuNo) {
        this.secondaryMenuNo = secondaryMenuNo;
    }

    public String getThirdMenu() {
        return thirdMenu;
    }

    public void setThirdMenu(String thirdMenu) {
        this.thirdMenu = thirdMenu;
    }

    public String getThirdMenuNo() {
        return thirdMenuNo;
    }

    public void setThirdMenuNo(String thirdMenuNo) {
        this.thirdMenuNo = thirdMenuNo;
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

    public MenuSheet() {
    }

    public MenuSheet(String menuName, String menuNo, String secondaryMenu, String secondaryMenuNo,
                     String thirdMenu, String thirdMenuNo, String deleted, String createTime,
                     String modifyTime, String lastOpr) {
        this.menuName = menuName;
        this.menuNo = menuNo;
        this.secondaryMenu = secondaryMenu;
        this.secondaryMenuNo = secondaryMenuNo;
        this.thirdMenu = thirdMenu;
        this.thirdMenuNo = thirdMenuNo;
        this.deleted = deleted;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
        this.lastOpr = lastOpr;
    }
}