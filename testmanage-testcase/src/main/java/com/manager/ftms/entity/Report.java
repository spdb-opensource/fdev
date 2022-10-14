package com.manager.ftms.entity;

public class Report {

    public Integer testcaseNum;//案例总数
    public String testcaseIds;//案例编号
    public Integer executeNum;//执行数

    public String developer;//开发人员
    public Integer mantisNum;//缺陷数
    public String mantisSource;//缺陷来源
    //有效缺陷分类统计
    public Integer requireNum = 0;//业务需求问题数
    public Integer requireRoleNum = 0;//需规问题数
    public Integer funcMantisNum = 0;//功能实现不完整数
    public Integer errorNum = 0;//功能实现错误数
    public Integer historyNum = 0;//历史遗留问题数
    public Integer adviseNum = 0;//优化建议数
    public Integer javaNum = 0;//后台问题数
    public Integer packNum = 0;//打包问题数
    //无效缺陷统计
    public Integer dataNum = 0;//数据问题数
    public Integer enNum = 0;//环境问题数
    public Integer otherNum = 0;//其他原因数

    public Integer getTestcaseNum() {
        if (null == testcaseNum)
            testcaseNum = 0;
        return testcaseNum;
    }

    public void setTestcaseNum(Integer testcaseNum) {
        this.testcaseNum = testcaseNum;
    }

    public String getTestcaseIds() {
        return testcaseIds;
    }

    public void setTestcaseIds(String testcaseIds) {
        this.testcaseIds = testcaseIds;
    }

    public Integer getExecuteNum() {
        if (null == executeNum)
            executeNum = 0;
        return executeNum;
    }

    public void setExecuteNum(Integer executeNum) {
        this.executeNum = executeNum;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public Integer getMantisNum() {
        if (null == mantisNum)
            mantisNum = 0;
        return mantisNum;
    }

    public void setMantisNum(Integer mantisNum) {
        this.mantisNum = mantisNum;
    }

    public String getMantisSource() {
        return mantisSource;
    }

    public void setMantisSource(String mantisSource) {
        this.mantisSource = mantisSource;
    }

    public Integer getRequireNum() {
        return requireNum;
    }

    public void setRequireNum(Integer requireNum) {
        this.requireNum = requireNum;
    }

    public Integer getRequireRoleNum() {
        return requireRoleNum;
    }

    public void setRequireRoleNum(Integer requireRoleNum) {
        this.requireRoleNum = requireRoleNum;
    }

    public Integer getFuncMantisNum() {
        return funcMantisNum;
    }

    public void setFuncMantisNum(Integer funcMantisNum) {
        this.funcMantisNum = funcMantisNum;
    }

    public Integer getErrorNum() {
        return errorNum;
    }

    public void setErrorNum(Integer errorNum) {
        this.errorNum = errorNum;
    }

    public Integer getHistoryNum() {
        return historyNum;
    }

    public void setHistoryNum(Integer historyNum) {
        this.historyNum = historyNum;
    }

    public Integer getAdviseNum() {
        return adviseNum;
    }

    public void setAdviseNum(Integer adviseNum) {
        this.adviseNum = adviseNum;
    }

    public Integer getJavaNum() {
        return javaNum;
    }

    public void setJavaNum(Integer javaNum) {
        this.javaNum = javaNum;
    }

    public Integer getPackNum() {
        return packNum;
    }

    public void setPackNum(Integer packNum) {
        this.packNum = packNum;
    }

    public Integer getDataNum() {
        return dataNum;
    }

    public void setDataNum(Integer dataNum) {
        this.dataNum = dataNum;
    }

    public Integer getEnNum() {
        return enNum;
    }

    public void setEnNum(Integer enNum) {
        this.enNum = enNum;
    }

    public Integer getOtherNum() {
        return otherNum;
    }

    public void setOtherNum(Integer otherNum) {
        this.otherNum = otherNum;
    }

}
