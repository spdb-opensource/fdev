package com.manager.ftms.entity;

public class WorkOrder {

    //工单编号
    private String workOrderNo;
    //对应主任务编号
    private String mainTaskNo;
    //对应主任务名称
    private String mainTaskName;
    //测试人员
    private String testers;
    //所属阶段 0未分配  1开发中 2sit 3uat 4已投产 5sit uat并行 6 uat(有风险 紧急上) 7 uat提测   8 无效
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
	
}
