package com.gotest.domain;

/**
 * 安全测试提测时上传的交易清单，对应security_test_trans表
 */
public class SecurityTestTrans {
    /**
     * 主键id
     */
    private Integer id;
    /**
     * 提测信息表id外键
     */
    private Integer submitSitId;
    /**
     * 序号
     */
    private String transIndex;
    /**
     * 交易名称
     */
    private String transName;
    /**
     * 交易描述
     */
    private String transDesc;
    /**
     * 功能菜单
     */
    private String functionMenu;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSubmitSitId() {
        return submitSitId;
    }

    public void setSubmitSitId(Integer submitSitId) {
        this.submitSitId = submitSitId;
    }

    public String getTransIndex() {
        return transIndex;
    }

    public void setTransIndex(String transIndex) {
        this.transIndex = transIndex;
    }

    public String getTransName() {
        return transName;
    }

    public void setTransName(String transName) {
        this.transName = transName;
    }

    public String getTransDesc() {
        return transDesc;
    }

    public void setTransDesc(String transDesc) {
        this.transDesc = transDesc;
    }

    public String getFunctionMenu() {
        return functionMenu;
    }

    public void setFunctionMenu(String functionMenu) {
        this.functionMenu = functionMenu;
    }
}
