package com.manager.ftms.util;

public class TestcaseAuditQueryObject {
	private Integer pageNum; // 当前页,默认为第1页
	private Integer pagesize; // 页面尺寸,默认为5条
	private String testcaseStatus; // 案例状态
	private Integer testcaseFuncId; // 功能模块id
	private String testcaseNo; // 案例编号
	private String testcaseName; // 案例名称
	private String testcasePeople; // 案例编写人
	private String work_no; // 工单编号

	public Integer getStart() {
		return (pageNum - 1) * pagesize;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getPagesize() {
		return pagesize;
	}

	public void setPageSize(int pagesize) {
		this.pagesize = pagesize;
	}

	public String getTestcaseStatus() {
		return testcaseStatus;
	}

	public void setTestcaseStatus(String testcaseStatus) {
		this.testcaseStatus = testcaseStatus;
	}

	public Integer getTestcaseFuncId() {
		return testcaseFuncId;
	}

	public void setTestcaseFuncId(Integer testcaseFuncId) {
		this.testcaseFuncId = testcaseFuncId;
	}

	public String getTestcaseNo() {
		return testcaseNo;
	}

	public void setTestcaseNo(String testcaseNo) {
		this.testcaseNo = testcaseNo;
	}

	public String getTestcaseName() {
		return testcaseName;
	}

	public void setTestcaseName(String testcaseName) {
		this.testcaseName = testcaseName;
	}

	public String getTestcasePeople() {
		return testcasePeople;
	}

	public void setTestcasePeople(String testcasePeople) {
		this.testcasePeople = testcasePeople;
	}

	public String getWork_no() {
		return work_no;
	}

	public void setWork_no(String work_no) {
		this.work_no = work_no;
	}

}
