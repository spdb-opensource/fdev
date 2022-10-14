package com.gotest.domain;

import java.io.Serializable;

/**
 * 计划-案例关系表
 */
public class PlanlistTestcaseRelation implements Serializable {

	private static final long serialVersionUID = 1L;

	// 关系表id
	private Integer planlistTestcaseId;
	// 案例编号
	private String testcaseNo;
	// 计划id
	private Integer planId;
	// 工单号
	private String workNo;
	// 案例执行结果
	private String testcaseExecuteResult;
	// 案例结果更新日期
	private String testcaseExecuteDate;
	// 首次执行人
	private String fstOpr;
	// 最后更新人
	private String fnlOpr;
	// 首次执行时间
	private String fstTm;
	// 关系挂载时间
	private String createTm;
	// 点击执行数
	private Integer exeNum;
	// 执行失败数
	private Integer failExeNum;
	// 执行阻塞数
	private Integer blockExeNum;

	private String createOpr;//案例创建人

	public Integer getExeNum() {
		return exeNum;
	}

	public void setExeNum(Integer exeNum) {
		this.exeNum = exeNum;
	}

	public String getCreateTm() {
		return createTm;
	}

	public void setCreateTm(String createTm) {
		this.createTm = createTm;
	}

	public Integer getPlanlistTestcaseId() {
		return planlistTestcaseId;
	}

	public void setPlanlistTestcaseId(Integer planlistTestcaseId) {
		this.planlistTestcaseId = planlistTestcaseId;
	}

	public String getTestcaseNo() {
		return testcaseNo;
	}

	public void setTestcaseNo(String testcaseNo) {
		this.testcaseNo = testcaseNo;
	}

	public Integer getPlanId() {
		return planId;
	}

	public void setPlanId(Integer planId) {
		this.planId = planId;
	}

	public String getWorkNo() {
		return workNo;
	}

	public void setWorkNo(String workNo) {
		this.workNo = workNo;
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

	public String getFstOpr() {
		return fstOpr;
	}

	public void setFstOpr(String fstOpr) {
		this.fstOpr = fstOpr;
	}

	public String getFnlOpr() {
		return fnlOpr;
	}

	public void setFnlOpr(String fnlOpr) {
		this.fnlOpr = fnlOpr;
	}

	public String getFstTm() {
		return fstTm;
	}

	public void setFstTm(String fstTm) {
		this.fstTm = fstTm;
	}


	public String getCreateOpr() {
		return createOpr;
	}

	public void setCreateOpr(String createOpr) {
		this.createOpr = createOpr;
	}

	public Integer getFailExeNum() {
		return failExeNum;
	}

	public void setFailExeNum(Integer failExeNum) {
		this.failExeNum = failExeNum;
	}

	public Integer getBlockExeNum() {
		return blockExeNum;
	}

	public void setBlockExeNum(Integer blockExeNum) {
		this.blockExeNum = blockExeNum;
	}

	@Override
	public String toString() {

		return "PlanlistTestcaseRelation{" +
				"planlistTestcaseId=" + planlistTestcaseId +
				", testcaseNo='" + testcaseNo + '\'' +
				", planId=" + planId +
				", workNo='" + workNo + '\'' +
				", testcaseExecuteResult='" + testcaseExecuteResult + '\'' +
				", testcaseExecuteDate='" + testcaseExecuteDate + '\'' +
				", fstOpr='" + fstOpr + '\'' +
				", fnlOpr='" + fnlOpr + '\'' +
				", fstTm='" + fstTm + '\'' +
				", createTm='" + createTm + '\'' +
				", exeNum=" + exeNum +
				'}';
	}

}