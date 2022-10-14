package com.manager.ftms.entity;

public class WeekReport extends Report{
	private String groupName;//组名称
	private String workNo;//工单编号
	private String mainTaskNo;//工单编号
	private String mainTaskName;//工单名称
	private String workStage;//工单状态
	private String workFlag; //工单所属 1：fdev，0测试平台自己新建
	private String workManager;//工单负责人
	private String testers;//测试人员
	private String aduitDemo;//代码审核人

	
	public String getWorkFlag() {
		return workFlag;
	}

	public void setWorkFlag(String workFlag) {
		this.workFlag = workFlag;
	}

	public String getMainTaskNo() {
		return mainTaskNo;
	}

	public void setMainTaskNo(String mainTaskNo) {
		this.mainTaskNo = mainTaskNo;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
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

	public String getWorkManager() {
		return workManager;
	}

	public void setWorkManager(String workManager) {
		this.workManager = workManager;
	}

	public String getTesters() {
		return testers;
	}

	public void setTesters(String testers) {
		this.testers = testers;
	}

	public String getAduitDemo() {
		return aduitDemo;
	}

	public void setAduitDemo(String aduitDemo) {
		this.aduitDemo = aduitDemo;
	}

	@Override
	public String toString() {
		return "WeekReport [groupName=" + groupName + ", workNo=" + workNo + ", mainTaskNo=" + mainTaskNo
				+ ", mainTaskName=" + mainTaskName + ", workStage=" + workStage + ", workFlag=" + workFlag
				+ ", workManager=" + workManager + ", testers=" + testers + ", aduitDemo=" + aduitDemo
				+ ", testcaseNum=" + testcaseNum + ", testcaseIds=" + testcaseIds + ", executeNum=" + executeNum
				+ ", developer=" + developer + ", mantisNum=" + mantisNum + ", mantisSource=" + mantisSource
				+ ", requireNum=" + requireNum + ", requireRoleNum=" + requireRoleNum + ", funcMantisNum="
				+ funcMantisNum + ", errorNum=" + errorNum + ", historyNum=" + historyNum + ", adviseNum=" + adviseNum
				+ ", javaNum=" + javaNum + ", packNum=" + packNum + ", dataNum=" + dataNum + ", enNum=" + enNum
				+ ", otherNum=" + otherNum + "]";
	}


	/*@Override
	public String toString() {
		return "WeekReport{" +
				"groupName='" + groupName + '\'' +
				", workNo='" + workNo + '\'' +
				", mainTaskNo='" + mainTaskNo + '\'' +
				", mainTaskName='" + mainTaskName + '\'' +
				", workStage='" + workStage + '\'' +
				", workFlag='" + workFlag + '\'' +
				", workManager='" + workManager + '\'' +
				", testers='" + testers + '\'' +
				", aduitDemo='" + aduitDemo + '\'' +
				", testcaseNum=" + testcaseNum +
				", testcaseIds='" + testcaseIds + '\'' +
				", executeNum=" + executeNum +
				", developer='" + developer + '\'' +
				", mantisNum=" + mantisNum +
				", mantisSource='" + mantisSource + '\'' +
				", requireNum=" + requireNum +
				", requireRoleNum=" + requireRoleNum +
				", funcMantisNum=" + funcMantisNum +
				", errorNum=" + errorNum +
				", historyNum=" + historyNum +
				", adviseNum=" + adviseNum +
				", javaNum=" + javaNum +
				", packNum=" + packNum +
				", dataNum=" + dataNum +
				", enNum=" + enNum +
				", otherNum=" + otherNum +
				'}';
	}*/
}
