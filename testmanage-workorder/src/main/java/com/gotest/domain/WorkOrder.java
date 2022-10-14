package com.gotest.domain;

import java.util.List;
import java.util.Map;

public class WorkOrder {

    //工单编号
    private String workOrderNo;
    //对应主任务编号
    private String mainTaskNo;
    //对应主任务名称,工单名
    private String mainTaskName;
    //测试人员
    private String testers;
    //所属阶段 0未分配  1开发中 2sit 3uat 4已投产 5sit uat并行 6 uat(有风险 紧急上) 7 uat提测   8 无效  9 分包测试  10分包测试（含风险） 11已废弃 12-安全测试（内测完成） 13-安全测试（含风险） 14-安全测试（不涉及）
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
    private String workManager;
    //分配到的组长
    private String groupLeader;
    //工单备注
    private String remark;

    //创建时间
    private Long createTime;
    //UAT提测日期
    private Long uatSubmitDate;
    //备用字段
    private String field1;
    private String field2;  //审批人
    private String field3;  //时间戳
    private String groupId;  //组id
    private String field5;  //案例数
    private String riskDescription;
    private String sitFlag;
    private String approvalFlag; //审批标志  0-未审批  1-已审批
    private String fdevGroupId;//fdev组id
    private String fstSitDate;//首次提交sit测试时间
    private String fnlSitDate;//最新一次提交sit测试时间
    private String fnlRollbackDate;//最新一次打回时间
    private String imageLink;//案例截图链接
    private String fdevGroupName;//fdev组名
    private String demandNo;//需求编号
    private String demandName;//需求名称
    private List<Map> mapList; //计划集合
    private String fdevNew;//重构fdev标记1为重构
    private String demandId;//需求id
    private String orderType;//工单类型,function-功能测试工单,security-安全测试工单

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

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

    public String getTesters() {
        return testers;
    }

    public void setTesters(String testers) {
        this.testers = testers;
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

    public String getWorkManager() {
        return workManager;
    }

    public void setWorkManager(String workManager) {
        this.workManager = workManager;
    }

    public String getGroupLeader() {
        return groupLeader;
    }

    public void setGroupLeader(String groupLeader) {
        this.groupLeader = groupLeader;
    }

    public String getMainTaskName() {
        return mainTaskName;
    }

    public void setMainTaskName(String mainTaskName) {
        this.mainTaskName = mainTaskName;
    }

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public String getField3() {
        return field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    public String getField5() {
        return field5;
    }

    public void setField5(String field5) {
        this.field5 = field5;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


	public Long getUatSubmitDate() {
		return uatSubmitDate;
	}

	public void setUatSubmitDate(Long uatSubmitDate) {
		this.uatSubmitDate = uatSubmitDate;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

    public String getRiskDescription() {
        return riskDescription;
    }

    public void setRiskDescription(String riskDescription) {
        this.riskDescription = riskDescription;
    }

    public String getSitFlag() {
        return sitFlag;
    }

    public void setSitFlag(String sitFlag) {
        this.sitFlag = sitFlag;
    }

    public String getApprovalFlag() {
        return approvalFlag;
    }

    public void setApprovalFlag(String approvalFlag) {
        this.approvalFlag = approvalFlag;
    }

    public String getFdevGroupId() {
        return fdevGroupId;
    }

    public void setFdevGroupId(String fdevGroupId) {
        this.fdevGroupId = fdevGroupId;
    }

    public String getFstSitDate() {
        return fstSitDate;
    }

    public void setFstSitDate(String fstSitDate) {
        this.fstSitDate = fstSitDate;
    }

    public String getFnlSitDate() {
        return fnlSitDate;
    }

    public void setFnlSitDate(String fnlSitDate) {
        this.fnlSitDate = fnlSitDate;
    }

    public String getFnlRollbackDate() {
        return fnlRollbackDate;
    }

    public void setFnlRollbackDate(String fnlRollbackDate) {
        this.fnlRollbackDate = fnlRollbackDate;
    }

    public String getImageLink() { return imageLink; }

    public void setImageLink(String imageLink) { this.imageLink = imageLink; }

    public String getFdevGroupName() { return fdevGroupName; }

    public void setFdevGroupName(String fdevGroupName) { this.fdevGroupName = fdevGroupName; }

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

    public List<Map> getMapList() {
        return mapList;
    }

    public void setMapList(List<Map> mapList) {
        this.mapList = mapList;
    }

    public String getFdevNew() { return fdevNew; }

    public void setFdevNew(String fdevNew) { this.fdevNew = fdevNew; }

    public String getDemandId() { return demandId; }

    public void setDemandId(String demandId) { this.demandId = demandId; }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
}
