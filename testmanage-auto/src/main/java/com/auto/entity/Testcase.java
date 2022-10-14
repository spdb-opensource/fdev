package com.auto.entity;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 用例实体
 */
@Component
public class Testcase implements Serializable {

    private Integer testcaseNo;

    private String dataId;

    private String userId;

    private String funcPoint;

    private String testcaseName;

    private String precondition;

    private String testcaseDesc;

    private String expectedResult;

    private String validFlag;

    private String menuSheetId;

    private String assertId;

    private String deleted;

    private String createTime;

    private String modifyTime;

    private String lastOpr;

    public Integer getTestcaseNo() { return testcaseNo; }

    public void setTestcaseNo(Integer testcaseNo) { this.testcaseNo = testcaseNo; }

    public String getDataId() { return dataId; }

    public void setDataId(String dataId) { this.dataId = dataId; }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getFuncPoint() { return funcPoint; }

    public void setFuncPoint(String funcPoint) { this.funcPoint = funcPoint; }

    public String getTestcaseName() { return testcaseName; }

    public void setTestcaseName(String testcaseName) { this.testcaseName = testcaseName; }

    public String getPrecondition() { return precondition; }

    public void setPrecondition(String precondition) { this.precondition = precondition; }

    public String getTestcaseDesc() { return testcaseDesc; }

    public void setTestcaseDesc(String testcaseDesc) { this.testcaseDesc = testcaseDesc; }

    public String getExpectedResult() { return expectedResult; }

    public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }

    public String getValidFlag() { return validFlag; }

    public void setValidFlag(String validFlag) { this.validFlag = validFlag; }

    public String getMenuSheetId() { return menuSheetId; }

    public void setMenuSheetId(String menuSheetId) { this.menuSheetId = menuSheetId; }

    public String getAssertId() { return assertId; }

    public void setAssertId(String assertId) { this.assertId = assertId; }

    public String getDeleted() { return deleted; }

    public void setDeleted(String deleted) { this.deleted = deleted; }

    public String getCreateTime() { return createTime; }

    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getModifyTime() { return modifyTime; }

    public void setModifyTime(String modifyTime) { this.modifyTime = modifyTime; }

    public String getLastOpr() { return lastOpr; }

    public void setLastOpr(String lastOpr) { this.lastOpr = lastOpr; }

    public Testcase() {
    }

    public Testcase(String dataId, String userId, String funcPoint, String testcaseName,
                    String precondition, String testcaseDesc, String expectedResult, String validFlag,
                    String menuSheetId, String assertId, String deleted, String createTime,
                    String modifyTime, String lastOpr) {
        this.dataId = dataId;
        this.userId = userId;
        this.funcPoint = funcPoint;
        this.testcaseName = testcaseName;
        this.precondition = precondition;
        this.testcaseDesc = testcaseDesc;
        this.expectedResult = expectedResult;
        this.validFlag = validFlag;
        this.menuSheetId = menuSheetId;
        this.assertId = assertId;
        this.deleted = deleted;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
        this.lastOpr = lastOpr;
    }
}