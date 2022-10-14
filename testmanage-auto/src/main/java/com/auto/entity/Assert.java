package com.auto.entity;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 断言实体
 */
@Component
public class Assert implements Serializable {

    private Integer assertId;

    private String label;

    private String assertData1;
    private String assertData2;
    private String assertData3;
    private String assertData4;
    private String assertData5;
    private String assertData6;
    private String assertData7;
    private String assertData8;
    private String assertData9;
    private String assertData10;

    private String deleted;

    private String createTime;

    private String modifyTime;

    private String lastOpr;

    public Integer getAssertId() {
        return assertId;
    }

    public void setAssertId(Integer assertId) {
        this.assertId = assertId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAssertData1() {
        return assertData1;
    }

    public void setAssertData1(String assertData1) {
        this.assertData1 = assertData1;
    }

    public String getAssertData2() {
        return assertData2;
    }

    public void setAssertData2(String assertData2) {
        this.assertData2 = assertData2;
    }

    public String getAssertData3() {
        return assertData3;
    }

    public void setAssertData3(String assertData3) {
        this.assertData3 = assertData3;
    }

    public String getAssertData4() {
        return assertData4;
    }

    public void setAssertData4(String assertData4) {
        this.assertData4 = assertData4;
    }

    public String getAssertData5() {
        return assertData5;
    }

    public void setAssertData5(String assertData5) {
        this.assertData5 = assertData5;
    }

    public String getAssertData6() {
        return assertData6;
    }

    public void setAssertData6(String assertData6) {
        this.assertData6 = assertData6;
    }

    public String getAssertData7() {
        return assertData7;
    }

    public void setAssertData7(String assertData7) {
        this.assertData7 = assertData7;
    }

    public String getAssertData8() {
        return assertData8;
    }

    public void setAssertData8(String assertData8) {
        this.assertData8 = assertData8;
    }

    public String getAssertData9() {
        return assertData9;
    }

    public void setAssertData9(String assertData9) {
        this.assertData9 = assertData9;
    }

    public String getAssertData10() {
        return assertData10;
    }

    public void setAssertData10(String assertData10) {
        this.assertData10 = assertData10;
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

    public Assert() {
    }
}