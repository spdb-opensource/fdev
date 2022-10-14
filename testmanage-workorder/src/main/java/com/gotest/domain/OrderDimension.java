package com.gotest.domain;

/**
 * 工单维度 返回的数据结构
 */
public class OrderDimension {
    //工单编号
    private String workNo;
    //主任务名称
    private String mainTaskName;
    //工单状态
    private String workStage;
    //执行总数
    private Integer caseExecute;
    //未执行总数
    private Integer caseNoExecute;
    //通过总数
    private Integer casePass;
    //失败总数
    private Integer caseFailure;
    //阻塞总数
    private Integer caseBlock;
    //有效缺陷总数
    private Integer caseMantis;
    // 案例总数
    private Integer caseCount;
    // 测试人员
    private String testers;
    // 开发责任人
    private String developer;
    // 小组名称
    private String groupName;
    // 测试进度(当前执行成功案例数/总案例数)
    private double percentage;
    // 业务需求问题
    private Integer rqrNum;
    // 需规问题
    private Integer rqrRuleNum;
    // 功能实现不完整
    private Integer funcLackNum;
    // 功能实现错误
    private Integer funcErrNum;
    // 历史遗留问题
    private Integer historyNum;
    // 优化建议
    private Integer optimizeNum;
    // 后台问题
    private Integer backNum;
    // 打包问题
    private Integer packageNum;
    // 兼容性异常
    private Integer compatibilityNum;

    private String groupId;

    private String orderType;

    public Integer getCaseMantis() {
        if(null == caseMantis)
            return 0;
        return caseMantis;
    }

    public void setCaseMantis(Integer caseMantis) {
        this.caseMantis = caseMantis;
    }

    public String getWorkNo() {
        return workNo;
    }

    public void setWorkNo(String workNo) {
        this.workNo = workNo;
    }

    public String getMainTaskName() {
        return mainTaskName;
    }

    public void setMainTaskName(String mainTaskName) {
        this.mainTaskName = mainTaskName;
    }

    public String getWorkStage() {
        return workStage;
    }

    public void setWorkStage(String workStage) {
        this.workStage = workStage;
    }

    public Integer getCaseExecute() {
        return caseExecute;
    }

    public void setCaseExecute(Integer caseExecute) {
        this.caseExecute = caseExecute;
    }

    public Integer getCaseNoExecute() {
        return caseNoExecute;
    }

    public void setCaseNoExecute(Integer caseNoExecute) {
        this.caseNoExecute = caseNoExecute;
    }

    public Integer getCasePass() {
        return casePass;
    }

    public void setCasePass(Integer casePass) {
        this.casePass = casePass;
    }

    public Integer getCaseFailure() {
        return caseFailure;
    }

    public void setCaseFailure(Integer caseFailure) {
        this.caseFailure = caseFailure;
    }

    public Integer getCaseBlock() {
        return caseBlock;
    }

    public void setCaseBlock(Integer caseBlock) {
        this.caseBlock = caseBlock;
    }

    public String getTesters() {
        return testers;
    }

    public void setTesters(String testers) {
        this.testers = testers;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Integer getCaseCount() {
        return caseCount;
    }

    public void setCaseCount(Integer caseCount) {
        this.caseCount = caseCount;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public Integer getRqrNum() {
        if(null == rqrNum)
            return 0;
        return rqrNum;
    }

    public void setRqrNum(Integer rqrNum) {
        this.rqrNum = rqrNum;
    }

    public Integer getRqrRuleNum() {
        if(null == rqrRuleNum)
            return 0;
        return rqrRuleNum;
    }

    public void setRqrRuleNum(Integer rqrRuleNum) {
        this.rqrRuleNum = rqrRuleNum;
    }

    public Integer getFuncLackNum() {
        if(null == funcLackNum)
            return 0;
        return funcLackNum;
    }

    public void setFuncLackNum(Integer funcLackNum) {
        this.funcLackNum = funcLackNum;
    }

    public Integer getFuncErrNum() {
        if(null == funcErrNum)
            return 0;
        return funcErrNum;
    }

    public void setFuncErrNum(Integer funcErrNum) {
        this.funcErrNum = funcErrNum;
    }

    public Integer getHistoryNum() {
        if(null == historyNum)
            return 0;
        return historyNum;
    }

    public void setHistoryNum(Integer historyNum) {
        this.historyNum = historyNum;
    }

    public Integer getOptimizeNum() {
        if(null == optimizeNum)
            return 0;
        return optimizeNum;
    }

    public void setOptimizeNum(Integer optimizeNum) {
        this.optimizeNum = optimizeNum;
    }

    public Integer getBackNum() {
        if(null == backNum)
            return 0;
        return backNum;
    }

    public void setBackNum(Integer backNum) {
        this.backNum = backNum;
    }

    public Integer getPackageNum() {
        if(null == packageNum)
            return 0;
        return packageNum;
    }

    public void setPackageNum(Integer packageNum) {
        this.packageNum = packageNum;
    }

    public Integer getCompatibilityNum() {
        return compatibilityNum;
    }

    public void setCompatibilityNum(Integer compatibilityNum) {
        this.compatibilityNum = compatibilityNum;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    @Override
    public String toString() {
        return "OrderDimension{" +
                "workNo='" + workNo + '\'' +
                ", mainTaskName='" + mainTaskName + '\'' +
                ", workStage='" + workStage + '\'' +
                ", caseExecute=" + caseExecute +
                ", caseNoExecute=" + caseNoExecute +
                ", casePass=" + casePass +
                ", caseFailure=" + caseFailure +
                ", caseBlock=" + caseBlock +
                ", caseMantis=" + caseMantis +
                '}';
    }
}
