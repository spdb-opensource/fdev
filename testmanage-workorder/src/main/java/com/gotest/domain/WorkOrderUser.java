package com.gotest.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 该类作为返回前端的工单展示中文名
 *	将testers,groupleader,workManager,field2转换成user对象返回前端
 *		其余和workOrder类一样
 */
public class WorkOrderUser {
	
	//工单编号
    private String workOrderNo;
    //对应主任务编号
    private String mainTaskNo;
    //对应主任务名称
    private String mainTaskName;
    //测试人员
    private List<Map> testers;
    //所属阶段 0未分配  1开发中 2sit 3uat 4已投产 5sit uat并行
    private String stage;
    //实施单元
    private String unit;
    //计划Sit测试时间
    private String planSitDate;
    //计划Uat时间
    private String planUatDate;
    //计划投产时间
    private String planProDate;
    //工单标识  //0测试平台新建 1fdev
    private String workOrderFlag;
    //分配工单管理员
    private Map workManager;
    //分配到的组长
    private List<Map> groupLeader;
    //工单备注
    private String remark;

    //创建时间
    private Long createTime;
    //备用字段
    private String field1;
    private List<Map> field2;  //审批人
    private String field3;  //时间戳
    private String groupId;  //小组id
    private String field5;  //案例数
	private String sitFlag; //工单内测标识("1"已内测"0"未内测)
    private String uatSubmitDate;
	private String riskDescription;
	private String hasCaseFlag;//工单是否有案例标志
	private String groupName; //小组名
	private String hasTaskFlag;//工单是否有子任务标志
	private String imageLink; //案例截图链接
	private String fdevGroupName;//fdev组名
	private String fdevGroupId;//fdev组id
	private String mantisFlag;//工单是否包含缺陷标志
	private String demandNo; //需求编号
	private String demandName;//需求名称
	private String orderType;//工单类型

	public String getHasTaskFlag() {return hasTaskFlag; }
	public void setHasTaskFlag(String hasTaskFlag) { this.hasTaskFlag = hasTaskFlag; }
	public String getWorkOrderNo() {
		return workOrderNo;
	}
	public void setWorkOrderNo(String workOrderNo) {
		this.workOrderNo = workOrderNo;
	}
	public String getMainTaskNo() {
		return mainTaskNo;
	}
	public void setMainTaskNo(String mainTaskNo) {
		this.mainTaskNo = mainTaskNo;
	}
	public String getMainTaskName() {
		return mainTaskName;
	}
	public void setMainTaskName(String mainTaskName) {
		this.mainTaskName = mainTaskName;
	}
	public List<Map> getTesters() {
		if(testers == null)
			testers = new ArrayList<Map>();
		return testers;
	}
	public void setTesters(List<Map> testers) {
		this.testers = testers;
	}
	public String getStage() {
		return stage;
	}
	public void setStage(String stage) {
		this.stage = stage;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getPlanSitDate() {
		return planSitDate;
	}
	public void setPlanSitDate(String planSitDate) {
		this.planSitDate = planSitDate;
	}
	public String getPlanUatDate() {
		return planUatDate;
	}
	public void setPlanUatDate(String planUatDate) {
		this.planUatDate = planUatDate;
	}
	public String getPlanProDate() {
		return planProDate;
	}
	public void setPlanProDate(String planProDate) {
		this.planProDate = planProDate;
	}
	public String getWorkOrderFlag() {
		return workOrderFlag;
	}
	public void setWorkOrderFlag(String workOrderFlag) {
		this.workOrderFlag = workOrderFlag;
	}
	public Map getWorkManager() {
		return workManager;
	}
	public void setWorkManager(Map workManager) {
		this.workManager = workManager;
	}
	public List<Map> getGroupLeader() {
		if(groupLeader == null)
			groupLeader = new ArrayList<Map>();
		return groupLeader;
	}
	public void setGroupLeader(List<Map> groupLeader) {
		this.groupLeader = groupLeader;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	public String getField1() {
		return field1;
	}
	public void setField1(String field1) {
		this.field1 = field1;
	}
	public List<Map> getField2() {
		if(field2 == null)
			field2 = new ArrayList<Map>();
		return field2;
	}
	public void setField2(List<Map> field2) {
		this.field2 = field2;
	}
	public String getField3() {
		return field3;
	}
	public void setField3(String field3) {
		this.field3 = field3;
	}
    
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getField5() {
		return field5;
	}
	public void setField5(String field5) {
		this.field5 = field5;
	}
	public String getUatSubmitDate() {
		return uatSubmitDate;
	}
	public void setUatSubmitDate(String uatSubmitDate) {
		this.uatSubmitDate = uatSubmitDate;
	}
	public String getSitFlag() {
		return sitFlag;
	}
	public void setSitFlag(String sitFlag) {
		this.sitFlag = sitFlag;
	}

	public String getRiskDescription() {
		return riskDescription;
	}

	public void setRiskDescription(String riskDescription) {
		this.riskDescription = riskDescription;
	}

	public String getHasCaseFlag() {
		return hasCaseFlag;
	}

	public void setHasCaseFlag(String hasCaseFlag) {
		this.hasCaseFlag = hasCaseFlag;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getImageLink() { return imageLink; }

	public void setImageLink(String imageLink) { this.imageLink = imageLink; }

	public String getFdevGroupName() { return fdevGroupName; }

	public void setFdevGroupName(String fdevGroupName) { this.fdevGroupName = fdevGroupName; }

	public String getFdevGroupId() { return fdevGroupId; }

	public void setFdevGroupId(String fdevGroupId) { this.fdevGroupId = fdevGroupId; }

	public String getMantisFlag() {
		return mantisFlag;
	}

	public void setMantisFlag(String mantisFlag) {
		this.mantisFlag = mantisFlag;
	}

	public String getDemandNo() {
		return demandNo;
	}

	public void setDemandNo(String demandNo) {
		this.demandNo = demandNo;
	}

	public String getDemandName() {
		return demandName;
	}

	public void setDemandName(String demandName) {
		this.demandName = demandName;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
}
