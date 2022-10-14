package com.spdb.fdev.fdemand.spdb.entity;

import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
@Document(collection = Dict.FDEV_IMPLEMENT_UNIT)
public class FdevImplementUnit {

    @Id
    private ObjectId _id;
    @Field("id")
    private String id;

    /**
     * 需求id，关联需求表
     */
    @Field("demand_id")
    public String demand_id;

    /**
     * 需求类型
     */
    @Field("demand_type")
    private String demand_type;

    /**
     * 研发单元标识，区分老的实施单元与新的研发单元
     */
    @Field("is_new")
    private boolean is_new;

    /**
     * 研发单元编号
     */
    @Indexed(unique = true)
    @Field("fdev_implement_unit_no")
    private String fdev_implement_unit_no;

    /**
     * 批次编号
     */
    @Field("batch_no")
    private String batch_no;

    /**
     * 任务集编号
     */
    @Field("task_no")
    private String task_no;

    /**
     * 任务集名称
     */
    @Field("task_name")
    private String task_name;

    /**
     * IPMP实施单元编号
     */
    @Field("ipmp_implement_unit_no")
    private String ipmp_implement_unit_no;

    /**
     * 其他需求任务编号
     */
    @Field("other_demand_task_num")
    private String other_demand_task_num;

    /**
     * 研发单元内容
     */
    @Field("implement_unit_content")
    private String implement_unit_content;

    /**
     * 正常研发单元状态
     */
    @Field("implement_unit_status_normal")
    private Integer implement_unit_status_normal;

    /**
     * 特殊研发单元状态
     */
    @Field("implement_unit_status_special")
    private Integer implement_unit_status_special;

    /**
     * 实施牵头单位
     */
    @Field("implement_lead_dept")
    private String implement_lead_dept;

    /**
     * 实施牵头团队
     */
    @Field("implement_lead_team")
    private String implement_lead_team;

    /**
     * 研发单元牵头人
     */
    @Field("implement_leader")
    private HashSet<String> implement_leader;

    /**
     * 研发牵头人信息
     */
    @Field("implement_leader_all")
    private List<UserInfo> implement_leader_all;

    /**
     * 研发单元牵头人域账号
     */
    @Field("implement_leader_account")
    private HashSet<String> implement_leader_account;

    /**
     * 所属小组id
     */
    @Field("group")
    private String group;

    /**
     * 所属小组中文
     */
    @Transient
    private String group_cn;

    /**
     * 拟纳入项目名称
     */
    @Field("project_name")
    private String project_name;

    /**
     * 项目编号
     */
    @Field("project_no")
    private String project_no;

    /**
     * 计划启动日期
     */
    @Field("plan_start_date")
    private String plan_start_date;

    /**
     * 计划提交内测日期
     */
    @Field("plan_inner_test_date")
    private String plan_inner_test_date;

    /**
     * 计划提交用户测试日期
     */
    @Field("plan_test_date")
    private String plan_test_date;

    /**
     * 计划用户测试完成日期
     */
    @Field("plan_test_finish_date")
    private String plan_test_finish_date;

    /**
     * 计划投产日期
     */
    @Field("plan_product_date")
    private String plan_product_date;

    /**
     * 实际启动日期
     */
    @Field("real_start_date")
    private String real_start_date;

    /**
     * 实际提交内测日期
     */
    @Field("real_inner_test_date")
    private String real_inner_test_date;

    /**
     * 实际提交用户测试日期
     */
    @Field("real_test_date")
    private String real_test_date;

    /**
     * 实际用户测试完成日期
     */
    @Field("real_test_finish_date")
    private String real_test_finish_date;

    /**
     * 实际投产日期
     */
    @Field("real_product_date")
    private String real_product_date;

    /**
     * 涉及系统名称
     */
    @Field("relate_system_name")
    private String relate_system_name;

    /**
     * 预期我部人员工作
     */
    @Field("dept_workload")
    private Double dept_workload;

    /**
     * 预期公司人员工作量
     */
    @Field("company_workload")
    private Double company_workload;

    /**
     * 是否涉及UI审核
     */
    @Field("ui_verify")
    private boolean ui_verify;

    /**
     * 备注
     */
    @Field("remark")
    private String remark;

    /**
     * 研发单元创建人
     */
    @Field("create_user")
    private String create_user;

    /**
     * 研发单元创建人信息
     */
    @Field("create_user_all")
    private UserInfo create_user_all;

    /**
     * 研发单元创建时间
     */
    @Field("create_time")
    private String create_time;

    /**
     * 研发单元暂缓时间
     */
    @Field("defer_time")
    private String defer_time;

    /**
     * 研发单元恢复时间
     */
    @Field("recover_time")
    private String recover_time;

    /**
     * 研发单元删除时间
     */
    @Field("delete_time")
    private String delete_time;

    /**
     * 是否撤销
     */
    @Field("is_canceled")
    private boolean is_canceled;

    /**
     * 需求联系单名称（任务查询返回可用实施单元增加字段，不入库）
     *
     * @return
     */
    @Transient
    private String oa_contact_name;

    /**
     * 需求联系单编号（任务查询返回可用实施单元增加字段，不入库）
     *
     * @return
     */
    @Transient
    private String oa_contact_no;

    /**
     * 是否原需求模块迁移数据，设置为1
     */
    @Field("isTransferRqrmnt")
    private String isTransferRqrmnt;
    /**
     * ipmp任务集的组id
     */
    @Field("ipmp_group_id")
    @Transient
    private String ipmpGroupId;
    
    /**
     * 实施单元是否可删除标识
     * 0 可以删除
     * 1 该需求处于特殊状态 不可删除
     * 2 该实施单元下存在任务不可删除
     * 3 该需求只有需求牵头人可删除
     * 4 用户无权限删除
     */
    @Transient
    private Integer del_flag;

    /**
     * 研发单元是否可编辑标志及不可删除的提示话术
     */
    @Transient
    private Map update_flag;

    /**
     * 研发单元是否审批标志及话术
     */
    @Transient
    private Map approveFlag;

    public Map getApproveFlag() {
        return approveFlag;
    }

    public void setApproveFlag(Map approveFlag) {
        this.approveFlag = approveFlag;
    }

    /**
     * 研发单元对应的实施单元牵头人域账号
     */
    @Transient
    private List<String> ipmp_unit_leader;

    /**
     * 研发单元所属小组的评估人id集合
     */
    @Transient
    private List<String> access_user;

    /**
     * 关联实施单元状态是否为(已投产、撤销、暂缓、暂存),true标识是
     *
     */
    @Transient
    private Boolean is_fourStatus;

    /**
     * 挂载按钮标记
     */
    @Transient
    private Boolean mount_flag;

	/**
     * 实施单元新增标识：null或者0说明是新增的，大于0说明是补充的
     */
    @Field("add_flag")
    private Integer add_flag;

    /**
     * 所属实施单元编号是否已同步到fdev，同步了则可点击链接跳转
     * 1：已同步，有链接
     * 0：未同步，无链接
     */
    private Integer have_link;

    /**
     * 需求名称
     */
    @Transient
    private String demand_name;
    /**
     * 需求编号
     */
    @Transient
    private String demand_no;
    /**
     * 审批类型  devApprove=开发审批 overdueApprove=超期审批 dev&overdue=开发审批&超期审批
     */
    @Field("approveType")
    private String approveType;

    /**
     * 超期类别
     */
    @Field("overdueType")
    private String overdueType;
    /**
     * 申请原因
     */
    @Field("overdueReason")
    private String overdueReason;
    /**
     * 审批状态 noSubmit=未提交 wait=待审批，pass=通过，  reject=拒绝
     */
    @Field("approveState")
    private String approveState;
    /**
     * 审批人id
     */
    @Field("approverId")
    private String approverId;
    /**
     * 审批人姓名
     */
    @Field("approverName")
    private String approverName;

    /**
     * 审批说明
     */
    @Field("approveReason")
    private String approveReason;

    /**
     * 提测单新增选择时话术
     */
    @Transient
    private String testOrderFlag;

    /**
     * 延期标志
     */
    @Transient
    private Boolean delayFlag;

    public String getApproveType() {
        return approveType;
    }

    public void setApproveType(String approveType) {
        this.approveType = approveType;
    }

    public String getTestOrderFlag() {
        return testOrderFlag;
    }

    public void setTestOrderFlag(String testOrderFlag) {
        this.testOrderFlag = testOrderFlag;
    }

    public String getApproveReason() {
        return approveReason;
    }

    public void setApproveReason(String approveReason) {
        this.approveReason = approveReason;
    }

    public String getOverdueType() {
        return overdueType;
    }

    public void setOverdueType(String overdueType) {
        this.overdueType = overdueType;
    }

    public String getOverdueReason() {
        return overdueReason;
    }

    public void setOverdueReason(String overdueReason) {
        this.overdueReason = overdueReason;
    }

    public String getApproveState() {
        return approveState;
    }

    public void setApproveState(String approveState) {
        this.approveState = approveState;
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getDemand_name() {
        return demand_name;
    }

    public void setDemand_name(String demand_name) {
        this.demand_name = demand_name;
    }

    public String getDemand_no() {
        return demand_no;
    }

    public void setDemand_no(String demand_no) {
        this.demand_no = demand_no;
    }


    public String getOther_demand_task_num() {
        return other_demand_task_num;
    }

    public void setOther_demand_task_num(String other_demand_task_num) {
        this.other_demand_task_num = other_demand_task_num;
    }

    public ObjectId get_id() {
        return _id;
    }


	public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDemand_id() {
        return demand_id;
    }

    public void setDemand_id(String demand_id) {
        this.demand_id = demand_id;
    }

    public String getDemand_type() {
        return demand_type;
    }

    public void setDemand_type(String demand_type) {
        this.demand_type = demand_type;
    }

    public String getFdev_implement_unit_no() {
        return fdev_implement_unit_no;
    }

    public void setFdev_implement_unit_no(String fdev_implement_unit_no) {
        this.fdev_implement_unit_no = fdev_implement_unit_no;
    }

    public String getBatch_no() {
        return batch_no;
    }

    public void setBatch_no(String batch_no) {
        this.batch_no = batch_no;
    }

    public String getTask_no() {
        return task_no;
    }

    public void setTask_no(String task_no) {
        this.task_no = task_no;
    }

    public String getTask_name() {
        return task_name;
    }

    public void setTask_name(String task_name) {
        this.task_name = task_name;
    }

    public String getIpmp_implement_unit_no() {
        return ipmp_implement_unit_no;
    }

    public void setIpmp_implement_unit_no(String ipmp_implement_unit_no) {
        this.ipmp_implement_unit_no = ipmp_implement_unit_no;
    }

    public String getImplement_unit_content() {
        return implement_unit_content;
    }

    public void setImplement_unit_content(String implement_unit_content) {
        this.implement_unit_content = implement_unit_content;
    }

    public Integer getImplement_unit_status_normal() {
        return implement_unit_status_normal;
    }

    public void setImplement_unit_status_normal(Integer implement_unit_status_normal) {
        this.implement_unit_status_normal = implement_unit_status_normal;
    }

    public Integer getImplement_unit_status_special() {
        return implement_unit_status_special;
    }

    public void setImplement_unit_status_special(Integer implement_unit_status_special) {
        this.implement_unit_status_special = implement_unit_status_special;
    }

    public String getImplement_lead_dept() {
        return implement_lead_dept;
    }

    public void setImplement_lead_dept(String implement_lead_dept) {
        this.implement_lead_dept = implement_lead_dept;
    }

    public String getImplement_lead_team() {
        return implement_lead_team;
    }

    public void setImplement_lead_team(String implement_lead_team) {
        this.implement_lead_team = implement_lead_team;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public HashSet<String> getImplement_leader() {
        return implement_leader;
    }

    public void setImplement_leader(HashSet<String> implement_leader) {
        this.implement_leader = implement_leader;
    }

    public HashSet<String> getImplement_leader_account() {
        return implement_leader_account;
    }

    public void setImplement_leader_account(HashSet<String> implement_leader_account) {
        this.implement_leader_account = implement_leader_account;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getProject_no() {
        return project_no;
    }

    public void setProject_no(String project_no) {
        this.project_no = project_no;
    }

    public String getPlan_start_date() {
        return plan_start_date;
    }

    public void setPlan_start_date(String plan_start_date) {
        this.plan_start_date = plan_start_date;
    }

    public String getPlan_inner_test_date() {
        return plan_inner_test_date;
    }

    public void setPlan_inner_test_date(String plan_inner_test_date) {
        this.plan_inner_test_date = plan_inner_test_date;
    }

    public String getPlan_test_date() {
        return plan_test_date;
    }

    public void setPlan_test_date(String plan_test_date) {
        this.plan_test_date = plan_test_date;
    }

    public String getPlan_test_finish_date() {
        return plan_test_finish_date;
    }

    public void setPlan_test_finish_date(String plan_test_finish_date) {
        this.plan_test_finish_date = plan_test_finish_date;
    }

    public String getPlan_product_date() {
        return plan_product_date;
    }

    public void setPlan_product_date(String plan_product_date) {
        this.plan_product_date = plan_product_date;
    }

    public String getReal_start_date() {
        return real_start_date;
    }

    public void setReal_start_date(String real_start_date) {
        this.real_start_date = real_start_date;
    }

    public String getReal_inner_test_date() {
        return real_inner_test_date;
    }

    public void setReal_inner_test_date(String real_inner_test_date) {
        this.real_inner_test_date = real_inner_test_date;
    }

    public String getReal_test_date() {
        return real_test_date;
    }

    public void setReal_test_date(String real_test_date) {
        this.real_test_date = real_test_date;
    }

    public String getReal_test_finish_date() {
        return real_test_finish_date;
    }

    public void setReal_test_finish_date(String real_test_finish_date) {
        this.real_test_finish_date = real_test_finish_date;
    }

    public void setIs_new(boolean is_new) {
        this.is_new = is_new;
    }

    public boolean getIs_new() {
        return is_new;
    }
    public String getReal_product_date() {
        return real_product_date;
    }

    public void setReal_product_date(String real_product_date) {
        this.real_product_date = real_product_date;
    }

    public String getRelate_system_name() {
        return relate_system_name;
    }

    public void setRelate_system_name(String relate_system_name) {
        this.relate_system_name = relate_system_name;
    }

    public Double getDept_workload() {
        return dept_workload;
    }

    public void setDept_workload(Double dept_workload) {
        this.dept_workload = dept_workload;
    }

    public Double getCompany_workload() {
        return company_workload;
    }

    public void setCompany_workload(Double company_workload) {
        this.company_workload = company_workload;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreate_user() {
        return create_user;
    }

    public void setCreate_user(String create_user) {
        this.create_user = create_user;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getDefer_time() {
        return defer_time;
    }

    public void setDefer_time(String defer_time) {
        this.defer_time = defer_time;
    }

    public String getRecover_time() {
        return recover_time;
    }

    public void setRecover_time(String recover_time) {
        this.recover_time = recover_time;
    }

    public String getDelete_time() {
        return delete_time;
    }

    public void setDelete_time(String delete_time) {
        this.delete_time = delete_time;
    }

    public boolean isIs_canceled() {
        return is_canceled;
    }

    public void setIs_canceled(boolean is_canceled) {
        this.is_canceled = is_canceled;
    }

    public List<UserInfo> getImplement_leader_all() {
    	if(CommonUtils.isNullOrEmpty(implement_leader_all)) {
    		return new ArrayList();
    	}
        return implement_leader_all;
    }

    public void setImplement_leader_all(List<UserInfo> implement_leader_all) {
        this.implement_leader_all = implement_leader_all;
    }

    public String getGroup_cn() {
        return group_cn;
    }

    public void setGroup_cn(String group_cn) {
        this.group_cn = group_cn;
    }

    public UserInfo getCreate_user_all() {
        return create_user_all;
    }

    public void setCreate_user_all(UserInfo create_user_all) {
        this.create_user_all = create_user_all;
    }

    public String getOa_contact_name() {
        return oa_contact_name;
    }

    public void setOa_contact_name(String oa_contact_name) {
        this.oa_contact_name = oa_contact_name;
    }

    public boolean isUi_verify() {
        return ui_verify;
    }

    public void setUi_verify(boolean ui_verify) {
        this.ui_verify = ui_verify;
    }

    public String getIsTransferRqrmnt() {
        return isTransferRqrmnt;
    }

    public void setIsTransferRqrmnt(String isTransferRqrmnt) {
        this.isTransferRqrmnt = isTransferRqrmnt;
    }

    public String getOa_contact_no() {
        return oa_contact_no;
    }

    public void setOa_contact_no(String oa_contact_no) {
        this.oa_contact_no = oa_contact_no;
    }

    public String getIpmpGroupId() {
        return ipmpGroupId;
    }

    public void setIpmpGroupId(String ipmpGroupId) {
        this.ipmpGroupId = ipmpGroupId;
    }


    public Integer getAdd_flag() {
        return add_flag;
    }

    public void setAdd_flag(Integer add_flag) {
        this.add_flag = add_flag;
    }
	public Integer getDel_flag() {
		return del_flag;
	}


	public void setDel_flag(Integer del_flag) {
		this.del_flag = del_flag;
	}

    public boolean isIs_new() {
        return is_new;
    }

    public List<String> getIpmp_unit_leader() {
        return ipmp_unit_leader;
    }

    public void setIpmp_unit_leader(List<String> ipmp_unit_leader) {
        this.ipmp_unit_leader = ipmp_unit_leader;
    }

    public List<String> getAccess_user() {
        return access_user;
    }

    public void setAccess_user(List<String> access_user) {
        this.access_user = access_user;
    }

    public Boolean isIs_fourStatus() {
        return is_fourStatus;
    }

    public void setIs_fourStatus(Boolean is_fourStatus) {
        this.is_fourStatus = is_fourStatus;
    }

    public Map getUpdate_flag() {
        return update_flag;
    }

    public void setUpdate_flag(Map update_flag) {
        this.update_flag = update_flag;
    }

    public Boolean isMount_flag() {
        return mount_flag;
    }

    public void setMount_flag(Boolean mount_flag) {
        this.mount_flag = mount_flag;
    }

    public Integer getHave_link() {
        return have_link;
    }

    public void setHave_link(Integer have_link) {
        this.have_link = have_link;
    }

    public Boolean getDelayFlag() {
        return delayFlag;
    }

    public void setDelayFlag(Boolean delayFlag) {
        this.delayFlag = delayFlag;
    }

    public FdevImplementUnit() {
    }

    public FdevImplementUnit(String id,String ipmpGroupId, String demand_id, String demand_type, String fdev_implement_unit_no, String batch_no, String task_no, String task_name, String ipmp_implement_unit_no, String implement_unit_content, Integer implement_unit_status_normal, Integer implement_unit_status_special, String implement_lead_dept, String implement_lead_team, HashSet<String> implement_leader, List<UserInfo> implement_leader_all, HashSet<String> implement_leader_account, String group, String project_name, String project_no, String plan_start_date, String plan_inner_test_date, String plan_test_date, String plan_test_finish_date, String plan_product_date, String real_start_date, String real_inner_test_date, String real_test_date, String real_test_finish_date, String real_product_date, String relate_system_name, Double dept_workload, Double company_workload, boolean ui_verify, String remark, String create_user, String create_time, String defer_time, String recover_time, String delete_time, boolean is_canceled, String isTransferRqrmnt, Map update_flag, List<String> access_user, Integer add_flag) {
        this.id = id;
        this.demand_id = demand_id;
        this.demand_type = demand_type;
        this.fdev_implement_unit_no = fdev_implement_unit_no;
        this.batch_no = batch_no;
        this.task_no = task_no;
        this.task_name = task_name;
        this.ipmp_implement_unit_no = ipmp_implement_unit_no;
        this.implement_unit_content = implement_unit_content;
        this.implement_unit_status_normal = implement_unit_status_normal;
        this.implement_unit_status_special = implement_unit_status_special;
        this.implement_lead_dept = implement_lead_dept;
        this.implement_lead_team = implement_lead_team;
        this.implement_leader = implement_leader;
        this.implement_leader_all = implement_leader_all;
        this.implement_leader_account = implement_leader_account;
        this.group = group;
        this.project_name = project_name;
        this.project_no = project_no;
        this.plan_start_date = plan_start_date;
        this.plan_inner_test_date = plan_inner_test_date;
        this.plan_test_date = plan_test_date;
        this.plan_test_finish_date = plan_test_finish_date;
        this.plan_product_date = plan_product_date;
        this.real_start_date = real_start_date;
        this.real_inner_test_date = real_inner_test_date;
        this.real_test_date = real_test_date;
        this.real_test_finish_date = real_test_finish_date;
        this.real_product_date = real_product_date;
        this.relate_system_name = relate_system_name;
        this.dept_workload = dept_workload;
        this.company_workload = company_workload;
        this.ui_verify = ui_verify;
        this.remark = remark;
        this.create_user = create_user;
        this.create_time = create_time;
        this.defer_time = defer_time;
        this.recover_time = recover_time;
        this.delete_time = delete_time;
        this.is_canceled = is_canceled;
        this.isTransferRqrmnt = isTransferRqrmnt;
        this.update_flag = update_flag;
        this.access_user = access_user;
        this.add_flag = add_flag;
        this.ipmpGroupId = ipmpGroupId;
    }
}
