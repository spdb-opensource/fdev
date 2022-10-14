package com.manager.ftms.entity;

import java.io.Serializable;

public class Testcase implements Serializable {
	private static final long serialVersionUID = 1L;
	private String testcaseNo;// 案例编号
	private String testcaseName;// 案例名称
	private String testcaseStatus;// 案例审核状态 待审核，审批通过，待生效
	private String testcaseType;// 案例类型 页面 功能 流程 连接 接口
	private String testcasePriority;// 案例优先级 高 中 低
	private String testcasePre;// 案例前置条件
	private String testcaseNature;// 案例性质 反案例2 正案例1
	private String funcationPoint;// 功能点
	private String testcaseDescribe;// 案例描述
	private String expectedResult;// 案例预期结果
	private String remark;// 备注
	private String testcaseVersion;// 案例版本号
	private String testcasePeople;// 案例编写人
	private String testcaseDate;// 案例创建时间
	private Integer testcaseFuncId;// 功能id
	private String systemName;// 系统名称
	private Integer systemId;// 系统名称
	private String testcaseFuncName;// 功能名称
	private String field1;
	private String field2;
	private String field3;
	private String field4;
	private String field5;
	private int planlistTestcaseId;
	private int planId;
	private String testcaseExecuteResult; // 执行结果
	private String testcaseExecuteDate;
	private String userName;
	private String necessaryFlag;// 必测标识
	private String createOpr;//案例创建人
	private String orderNum;//案例顺序

	public String getTestcaseNo() {
		return testcaseNo;
	}

	public void setTestcaseNo(String testcaseNo) {
		this.testcaseNo = testcaseNo == null ? null : testcaseNo.trim();
	}

	public String getTestcaseName() {
		return testcaseName;
	}

	public void setTestcaseName(String testcaseName) {
		this.testcaseName = testcaseName == null ? null : testcaseName.trim();
	}

	public String getTestcaseStatus() {
		return testcaseStatus;
	}

	public void setTestcaseStatus(String testcaseStatus) {
		this.testcaseStatus = testcaseStatus == null ? null : testcaseStatus.trim();
	}

	public String getTestcaseType() {
		return testcaseType;
	}

	public void setTestcaseType(String testcaseType) {
		this.testcaseType = testcaseType == null ? null : testcaseType.trim();
	}

	public String getTestcasePriority() {
		return testcasePriority;
	}

	public void setTestcasePriority(String testcasePriority) {
		this.testcasePriority = testcasePriority == null ? null : testcasePriority.trim();
	}

	public String getTestcasePre() {
		return testcasePre;
	}

	public void setTestcasePre(String testcasePre) {
		this.testcasePre = testcasePre == null ? null : testcasePre.trim();
	}

	public String getTestcaseNature() {
		return testcaseNature;
	}

	public void setTestcaseNature(String testcaseNature) {
		this.testcaseNature = testcaseNature == null ? null : testcaseNature.trim();
	}

	public String getFuncationPoint() {
		return funcationPoint;
	}

	public void setFuncationPoint(String funcationPoint) {
		this.funcationPoint = funcationPoint == null ? null : funcationPoint.trim();
	}

	public String getTestcaseDescribe() {
		return testcaseDescribe;
	}

	public void setTestcaseDescribe(String testcaseDescribe) {
		this.testcaseDescribe = testcaseDescribe == null ? null : testcaseDescribe.trim();
	}

	public String getExpectedResult() {
		return expectedResult;
	}

	public void setExpectedResult(String expectedResult) {
		this.expectedResult = expectedResult;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark == null ? null : remark.trim();
	}

	public String getTestcaseVersion() {
		return testcaseVersion;
	}

	public void setTestcaseVersion(String testcaseVersion) {
		this.testcaseVersion = testcaseVersion == null ? null : testcaseVersion.trim();
	}

	public String getTestcasePeople() {
		return testcasePeople;
	}

	public void setTestcasePeople(String testcasePeople) {
		this.testcasePeople = testcasePeople == null ? null : testcasePeople.trim();
	}

	public String getTestcaseDate() {
		return testcaseDate;
	}

	public void setTestcaseDate(String testcaseDate) {
		this.testcaseDate = testcaseDate;
	}

	public Integer getTestcaseFuncId() {
		return testcaseFuncId;
	}

	public void setTestcaseFuncId(Integer testcaseFuncId) {
		this.testcaseFuncId = testcaseFuncId;
	}

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1 == null ? null : field1.trim();
	}

	public String getField2() {
		return field2;
	}

	public void setField2(String field2) {
		this.field2 = field2 == null ? null : field2.trim();
	}

	public String getField3() {
		return field3;
	}

	public void setField3(String field3) {
		this.field3 = field3 == null ? null : field3.trim();
	}

	public String getField4() {
		return field4;
	}

	public void setField4(String field4) {
		this.field4 = field4 == null ? null : field4.trim();
	}

	public String getField5() {
		return field5;
	}

	public void setField5(String field5) {
		this.field5 = field5 == null ? null : field5.trim();
	}

	public int getPlanlistTestcaseId() {
		return planlistTestcaseId;
	}

	public void setPlanlistTestcaseId(int planlistTestcaseId) {
		this.planlistTestcaseId = planlistTestcaseId;
	}

	public int getPlanId() {
		return planId;
	}

	public void setPlanId(int planId) {
		this.planId = planId;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

	public String getTestcaseFuncName() {
		return testcaseFuncName;
	}

	public void setTestcaseFuncName(String testcaseFuncName) {
		this.testcaseFuncName = testcaseFuncName;
	}

	public String getTestcaseExecuteResult() {
		return testcaseExecuteResult;
	}

	public void setTestcaseExecuteResult(String testcaseExecuteResult) {
		this.testcaseExecuteResult = testcaseExecuteResult;
	}

	public String getTestcaseExecuteDate() {
		return testcaseExecuteDate;
	}

	public void setTestcaseExecuteDate(String testcaseExecuteDate) {
		this.testcaseExecuteDate = testcaseExecuteDate;
	}

	public Integer getSystemId() {
		return systemId;
	}

	public void setSystemId(Integer systemId) {
		this.systemId = systemId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getNecessaryFlag() {
		return necessaryFlag;
	}

	public void setNecessaryFlag(String necessaryFlag) {
		this.necessaryFlag = necessaryFlag;
	}

	public String getCreateOpr() {
		return createOpr;
	}

	public void setCreateOpr(String createOpr) {
		this.createOpr = createOpr;
	}

	public String getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(String orderNum) {
		this.orderNum = orderNum;
	}

	@Override
	public String toString() {
		return "Testcase [testcaseNo=" + testcaseNo + ", testcaseName=" + testcaseName + ", testcaseStatus="
				+ testcaseStatus + ", testcaseType=" + testcaseType + ", testcasePriority=" + testcasePriority
				+ ", testcasePre=" + testcasePre + ", testcaseNature=" + testcaseNature + ", funcationPoint="
				+ funcationPoint + ", testcaseDescribe=" + testcaseDescribe + ", expectedResult=" + expectedResult
				+ ", remark=" + remark + ", testcaseVersion=" + testcaseVersion + ", testcasePeople=" + testcasePeople
				+ ", testcaseDate=" + testcaseDate + ", testcaseFuncId=" + testcaseFuncId + ", systemName=" + systemName
				+ ", systemId=" + systemId + ", testcaseFuncName=" + testcaseFuncName + ", field1=" + field1
				+ ", field2=" + field2 + ", field3=" + field3 + ", field4=" + field4 + ", field5=" + field5
				+ ", planlistTestcaseId=" + planlistTestcaseId + ", planId=" + planId + ", testcaseExecuteResult="
				+ testcaseExecuteResult + ", testcaseExecuteDate=" + testcaseExecuteDate + ", userName=" + userName
				+ ", orderNum=" + orderNum+ "]";
	}

}
