package com.spdb.fdev.fdemand.spdb.entity; 

import com.spdb.fdev.fdemand.base.dict.Dict;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Document(collection = Dict.DEMAND_BASEINFO)
public class DemandBaseInfo {

    @Id
    private ObjectId _id;
    @Field("id")
    private String id;

    /**
     * 需求类型
     */
    @Field("demand_type")
    private String demand_type;

    /**
     * 正常需求状态
     */
    @Field("demand_status_normal")
    private Integer demand_status_normal;

    /**
     * 特殊需求状态
     */
    @Field("demand_status_special")
    private Integer demand_status_special;

    /**
            *   需求编号
     */
    @Field("oa_contact_no")
    private String oa_contact_no;
    
    /**
     * OA联系单编号
     */
    private String oa_real_no;

    /**
     * OA联系单名称
     */
    @Field("oa_contact_name")
    private String oa_contact_name;

    /**
     * OA收件日期
     */
    private String oa_receive_date;

    /**
     * 需求提出部门
     */
    private String propose_demand_dept;

    /**
     * 需求联系人
     */
    private String propose_demand_user;

    /**
     * 规划联系人
     */
    private String plan_user;

    /**
     * 需求说明书名称
     * 对应科技需求的需求名称
     */
    @Field("demand_instruction")
    private String demand_instruction;

    /**
     * 需求计划编号
     * 对应科技需求的计划编号
     */
    private String demand_plan_no;

    /**
     * 需求计划名称
     */
    private String demand_plan_name;

    /**
     * 需求背景
     */
    private String demand_background;

    /**
     * 前期沟通情况
     */
    private String former_communication;

    /**
     * 需求是否可行
     */
    private String demand_available;

    /**
     * 需求评估方式
     */
    private String demand_assess_way;

    /**
     * 需求备案编号
     */
    private String demand_record_no;

    /**
     * 未来是否纳入后评估
     */
    private String future_assess;

    /**
     * 评审人
     */
    private String review_user;

    /**
     * 需求方期望投产日期
     */
    private String respect_product_date;

    /**
     * 需求可行性评估意见
     */
    private String available_assess_idea;

    /**
     * 是否涉及UI审核
     */
    @Field("ui_verify")
    private Boolean ui_verify;

    /**
     * 是否通过审核
     */
    @Field("is_verify")
    private Boolean is_verify;

    /**
     * 是否实施单元牵头人
     */
    @Transient
    private Boolean is_ipmp_unit_leader;

    /**
     * 设计稿审核人
     */
    @Field("ui_verify_user")
    private String ui_verify_user;

    /**
     * 设计稿审核状态,不涉及：noRelate、未上传：uploadNot、已上传：uploaded、待审核：auditWait、审核中：auditIn、审核通过：auditPass、审核不通过：auditPassNot、审核完成：completedAudit
     */
    @Field("design_status")
    private String design_status;

    /**
     * 需求说明
     */
    @Field("demand_desc")
    private String demand_desc;

    /**
     * 紧急需求标识 0=新增紧急业务需求 1=同步业务需求
     */
    @Field("demand_flag")
    private Integer demand_flag;

    /**
     * 需求书编号
     */
    @Field("demand_no")
    private String demand_no;

    /**
     * 实施单元跟踪人
     */
    @Field("impl_track_user")
    private Set<String> impl_track_user;

    /**
     * 需求牵头人是否编辑 true=编辑 false=未编辑
     */
    @Field("leader_update_flag")
    private Boolean leader_update_flag;

    /**
     * 代码审核工单编号
     */
    @Field("code_order_no")
    private Set<String> code_order_no;

    /**
     * 需求标签
     */
    @Field("demand_label")
    private Set<String> demand_label;

    /**
     * 需求标签 详细信息
     */
    @Transient
    private List<Map<String, Object>> demand_label_info;

    public List<Map<String, Object>> getDemand_label_info() {
        return demand_label_info;
    }

    public void setDemand_label_info(List<Map<String, Object>> demand_label_info) {
        this.demand_label_info = demand_label_info;
    }

    public Set<String> getDemand_label() {
        return demand_label;
    }

    public void setDemand_label(Set<String> demand_label) {
        this.demand_label = demand_label;
    }

    public Set<String> getCode_order_no() {
        return code_order_no;
    }

    public void setCode_order_no(Set<String> code_order_no) {
        this.code_order_no = code_order_no;
    }

    public Boolean getLeader_update_flag() {
        return leader_update_flag;
    }

    public void setLeader_update_flag(Boolean leader_update_flag) {
        this.leader_update_flag = leader_update_flag;
    }

    public String getDemand_no() {
        return demand_no;
    }

    public void setDemand_no(String demand_no) {
        this.demand_no = demand_no;
    }

    public Set<String> getImpl_track_user() {
        return impl_track_user;
    }

    public void setImpl_track_user(Set<String> impl_track_user) {
        this.impl_track_user = impl_track_user;
    }

    /**
     * 实施团队可行性评估补充意见
     */
    @Field("extra_idea")
    private String extra_idea;

    /**
     * 需求创建人
     */
    @Field("demand_create_user")
    private String demand_create_user;

    /**
     * 需求创建人全部信息
     */
    @Field("demand_create_user_all")
    private UserInfo demand_create_user_all;

    /**
     * 需求创建时间
     */
    @Field("demand_create_time")
    private String demand_create_time;

    /**
     * 需求评估时间
     */
    @Field("demand_assess_date")
    private String demand_assess_date;

    /**
     * 需求删除时间
     */
    private String demand_delete_time;

    /**
     * 需求暂缓时间
     */
    private String demand_defer_time;

    /**
     * 需求恢复时间
     */
    private String demand_recover_time;

    /**
     * 需求总牵头
     */
    @Field("demand_leader_group")
    private String demand_leader_group;

    private String leader_group_status;
    /**
     * 需求总牵头小组中文名
     */
    @Field("demand_leader_group_cn")
    private String demand_leader_group_cn;

    /**
     * 需求总牵头负责人
     */
    @Field("demand_leader")
    private HashSet<String> demand_leader;

    /**
     * 需求总牵头负责人信息
     */
    @Field("demand_leader_all")
    private List<UserInfo> demand_leader_all;

    /**
     * 优先级
     */
    @Field("priority")
    private String priority;

    /**
     * 受理日期
     */
    @Field("accept_date")
    private String accept_date;

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
     * 计划提交业测日期
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
     * 实际提交业测日期
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
     * 涉及板块
     */
    @Field("relate_part")
    private HashSet<String> relate_part;

    /**
     * 涉及板块详情
     */
    @Field("relate_part_detail")
    private ArrayList<RelatePartDetail> relate_part_detail;

    /**
     * 是否撤销
     */
    @Field("is_canceled")
    private Boolean is_canceled;

    /**
     * 设计还原审核存储信息
     */
    @Field("designMap")
    private Map<String, List<Map<String, String>>> designMap;
    /**
     * UI上传者
     */
    @Field("uploader")
    private String uploader;

    /**
     * 第一次UI上传者
     */
    @Field("firstUploader")
    private String firstUploader;

    /**
     * 审核意见
     */
    @Field("designRemark")
    private String designRemark;


    /**
     * 提测重点
     */
    @Field("designDoc")
    private List<DesignDoc> designDoc;

    /**
     * 是否原需求模块迁移数据，老数据设置为1，新数据不存
     */
    @Field("isTransferRqrmnt")
    private String isTransferRqrmnt;
    
    /**
     * 需求编辑也ui审核框是否可编辑
     * true 可以修改 false 不可修改
     */
    private Boolean ui_status;

    /**
     * 可发送邮件标识，yes为可发送邮件，no为不可发送邮件
     */
    @Field("assess_flag")
    private String assess_flag;

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
     * 判断是否可以新增实施单元
     * true 新增 false 不可新增
     */
    private Boolean addImplFlag;

/**
     * 是否存在实施单元
     * true 存在 false 不存在
     */
    private Boolean havaImpl;

    /**
     * 科技类型
     */
    private String tech_type;

    /**
     * 其他备注
     */
    private String tech_type_desc;

    /**
     * 需求属性
     */
    //预研advancedResearch（预备研发，通常为探索性、调研性、试错性等科技工作）；重点keyPoint（部门内外部督办类，战略类等开发需求）；常规routine（日常需求）
    private String demand_property;

    @Field("checkCount")
    @ApiModelProperty(value = "ui设计还原审核轮数")
    @Transient
    private String checkCount;

    @Field("currentStage")
    @ApiModelProperty(value = "当前阶段")
    @Transient
    private String currentStage;

    @Field("currentStageTime")
    @ApiModelProperty(value = "当前阶段开始时间")
    @Transient
    private String currentStageTime;

    @Field("finshFlag")
    @ApiModelProperty(value = "完成情况")
    @Transient
    private String finshFlag;

    private Boolean delayFlag;//需求延期标志。true延期

    public String getTech_type_desc() {
        return tech_type_desc;
    }

    public void setTech_type_desc(String tech_type_desc) {
        this.tech_type_desc = tech_type_desc;
    }

    public String getTech_type() {
        return tech_type;
    }

    public void setTech_type(String tech_type) {
        this.tech_type = tech_type;
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

    public String getDemand_type() {
        return demand_type;
    }

    public void setDemand_type(String demand_type) {
        this.demand_type = demand_type;
    }

    public Integer getDemand_status_normal() {
        return demand_status_normal;
    }

    public void setDemand_status_normal(Integer demand_status_normal) {
        this.demand_status_normal = demand_status_normal;
    }

    public Integer getDemand_status_special() {
        return demand_status_special;
    }

    public void setDemand_status_special(Integer demand_status_special) {
        this.demand_status_special = demand_status_special;
    }

    public String getOa_contact_no() {
        return oa_contact_no;
    }

    public void setOa_contact_no(String oa_contact_no) {
        this.oa_contact_no = oa_contact_no;
    }

    public String getOa_real_no() {
		return oa_real_no;
	}

	public void setOa_real_no(String oa_real_no) {
		this.oa_real_no = oa_real_no;
	}

	public String getOa_contact_name() {
        return oa_contact_name;
    }

    public void setOa_contact_name(String oa_contact_name) {
        this.oa_contact_name = oa_contact_name;
    }

    public String getOa_receive_date() {
        return oa_receive_date;
    }

    public void setOa_receive_date(String oa_receive_date) {
        this.oa_receive_date = oa_receive_date;
    }

    public String getPropose_demand_dept() {
        return propose_demand_dept;
    }

    public void setPropose_demand_dept(String propose_demand_dept) {
        this.propose_demand_dept = propose_demand_dept;
    }

    public String getPropose_demand_user() {
        return propose_demand_user;
    }

    public void setPropose_demand_user(String propose_demand_user) {
        this.propose_demand_user = propose_demand_user;
    }

    public String getPlan_user() {
        return plan_user;
    }

    public void setPlan_user(String plan_user) {
        this.plan_user = plan_user;
    }

    public String getDemand_instruction() {
        return demand_instruction;
    }

    public void setDemand_instruction(String demand_instruction) {
        this.demand_instruction = demand_instruction;
    }

    public String getDemand_plan_no() {
        return demand_plan_no;
    }

    public void setDemand_plan_no(String demand_plan_no) {
        this.demand_plan_no = demand_plan_no;
    }

    public String getDemand_plan_name() {
        return demand_plan_name;
    }

    public void setDemand_plan_name(String demand_plan_name) {
        this.demand_plan_name = demand_plan_name;
    }

    public String getDemand_background() {
        return demand_background;
    }

    public void setDemand_background(String demand_background) {
        this.demand_background = demand_background;
    }

    public String getFormer_communication() {
        return former_communication;
    }

    public void setFormer_communication(String former_communication) {
        this.former_communication = former_communication;
    }

    public String getDemand_available() {
        return demand_available;
    }

    public void setDemand_available(String demand_available) {
        this.demand_available = demand_available;
    }

    public String getDemand_assess_way() {
        return demand_assess_way;
    }

    public void setDemand_assess_way(String demand_assess_way) {
        this.demand_assess_way = demand_assess_way;
    }

    public String getDemand_record_no() {
        return demand_record_no;
    }

    public void setDemand_record_no(String demand_record_no) {
        this.demand_record_no = demand_record_no;
    }

    public String getFuture_assess() {
        return future_assess;
    }

    public void setFuture_assess(String future_assess) {
        this.future_assess = future_assess;
    }

    public String getReview_user() {
        return review_user;
    }

    public void setReview_user(String review_user) {
        this.review_user = review_user;
    }

    public String getRespect_product_date() {
        return respect_product_date;
    }

    public void setRespect_product_date(String respect_product_date) {
        this.respect_product_date = respect_product_date;
    }

    public String getAvailable_assess_idea() {
        return available_assess_idea;
    }

    public void setAvailable_assess_idea(String available_assess_idea) {
        this.available_assess_idea = available_assess_idea;
    }

    public Boolean isUi_verify() {
        return ui_verify;
    }

    public void setUi_verify(Boolean ui_verify) {
        this.ui_verify = ui_verify;
    }

    public Boolean isIs_verify() {
        return is_verify;
    }

    public void setIs_verify(Boolean is_verify) {
        this.is_verify = is_verify;
    }

    public String getUi_verify_user() {
        return ui_verify_user;
    }

    public Boolean getUi_verify() { return ui_verify; }

    public void setUi_verify_user(String ui_verify_user) {
        this.ui_verify_user = ui_verify_user;
    }

    public String getDesign_status() {
        return design_status;
    }

    public void setDesign_status(String design_status) {
        this.design_status = design_status;
    }

    public String getDemand_desc() {
        return demand_desc;
    }

    public void setDemand_desc(String demand_desc) {
        this.demand_desc = demand_desc;
    }

    public Integer getDemand_flag() { return demand_flag; }

    public void setDemand_flag(Integer demand_flag) { this.demand_flag = demand_flag; }

    public String getExtra_idea() {
        return extra_idea;
    }

    public void setExtra_idea(String extra_idea) {
        this.extra_idea = extra_idea;
    }

    public String getDemand_create_user() {
        return demand_create_user;
    }

    public void setDemand_create_user(String demand_create_user) {
        this.demand_create_user = demand_create_user;
    }

    public String getDemand_create_time() {
        return demand_create_time;
    }

    public void setDemand_create_time(String demand_create_time) {
        this.demand_create_time = demand_create_time;
    }

    public String getDemand_delete_time() {
        return demand_delete_time;
    }

    public void setDemand_delete_time(String demand_delete_time) {
        this.demand_delete_time = demand_delete_time;
    }

    public String getDemand_defer_time() {
        return demand_defer_time;
    }

    public void setDemand_defer_time(String demand_defer_time) {
        this.demand_defer_time = demand_defer_time;
    }

    public String getDemand_recover_time() {
        return demand_recover_time;
    }

    public void setDemand_recover_time(String demand_recover_time) {
        this.demand_recover_time = demand_recover_time;
    }

    public String getDemand_leader_group() {
        return demand_leader_group;
    }

    public void setDemand_leader_group(String demand_leader_group) {
        this.demand_leader_group = demand_leader_group;
    }

    public HashSet<String> getDemand_leader() {
        return demand_leader;
    }

    public void setDemand_leader(HashSet<String> demand_leader) {
        this.demand_leader = demand_leader;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAccept_date() {
        return accept_date;
    }

    public void setAccept_date(String accept_date) {
        this.accept_date = accept_date;
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

    public String getReal_product_date() {
        return real_product_date;
    }

    public void setReal_product_date(String real_product_date) {
        this.real_product_date = real_product_date;
    }

    public HashSet<String> getRelate_part() {
        return relate_part;
    }

    public void setRelate_part(HashSet<String> relate_part) {
        this.relate_part = relate_part;
    }

    public ArrayList<RelatePartDetail> getRelate_part_detail() {
        return relate_part_detail;
    }

    public void setRelate_part_detail(ArrayList<RelatePartDetail> relate_part_detail) {
        this.relate_part_detail = relate_part_detail;
    }

    public Boolean isIs_canceled() {
        return is_canceled;
    }

    public void setIs_canceled(Boolean is_canceled) {
        this.is_canceled = is_canceled;
    }

    public UserInfo getDemand_create_user_all() {
        return demand_create_user_all;
    }

    public void setDemand_create_user_all(UserInfo demand_create_user_all) {
        this.demand_create_user_all = demand_create_user_all;
    }

    public String getDemand_leader_group_cn() {
        return demand_leader_group_cn;
    }

    public void setDemand_leader_group_cn(String demand_leader_group_cn) {
        this.demand_leader_group_cn = demand_leader_group_cn;
    }

    public List<UserInfo> getDemand_leader_all() {
        return demand_leader_all;
    }

    public void setDemand_leader_all(List<UserInfo> demand_leader_all) {
        this.demand_leader_all = demand_leader_all;
    }

    public Map<String, List<Map<String, String>>> getDesignMap() {
        return designMap;
    }

    public void setDesignMap(Map<String, List<Map<String, String>>> designMap) {
        this.designMap = designMap;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public String getFirstUploader() {
        return firstUploader;
    }

    public void setFirstUploader(String firstUploader) {
        this.firstUploader = firstUploader;
    }

    public String getDesignRemark() {
        return designRemark;
    }

    public void setDesignRemark(String designRemark) {
        this.designRemark = designRemark;
    }

    public List<DesignDoc> getDesignDoc() {
        return designDoc;
    }

    public void setDesignDoc(List<DesignDoc> designDoc) {
        this.designDoc = designDoc;
    }

    public String getLeader_group_status() {
        return leader_group_status;
    }

    public void setLeader_group_status(String leader_group_status) {
        this.leader_group_status = leader_group_status;
    }

    public String getIsTransferRqrmnt() {
        return isTransferRqrmnt;
    }

    public void setIsTransferRqrmnt(String isTransferRqrmnt) {
        this.isTransferRqrmnt = isTransferRqrmnt;
    }

	public Boolean getUi_status() {
		return ui_status;
	}

	public void setUi_status(Boolean ui_status) {
		this.ui_status = ui_status;
	}
    public String getDemand_assess_date() {
        return demand_assess_date;
    }

    public void setDemand_assess_date(String demand_assess_date) {
        this.demand_assess_date = demand_assess_date;
    }

    public String getAssess_flag() {
        return assess_flag;
    }

    public void setAssess_flag(String assess_flag) {
        this.assess_flag = assess_flag;
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
    
    
    public Boolean getAddImplFlag() {
  		return addImplFlag;
  	}

  	public void setAddImplFlag(Boolean addImplFlag) {
  		this.addImplFlag = addImplFlag;
  	}

	public Boolean getHavaImpl() {
		return havaImpl;
	}

	public void setHavaImpl(Boolean havaImpl) {
		this.havaImpl = havaImpl;
	}

    public Boolean getIs_ipmp_unit_leader() { return is_ipmp_unit_leader; }

    public void setIs_ipmp_unit_leader(Boolean is_ipmp_unit_leader) { this.is_ipmp_unit_leader = is_ipmp_unit_leader; }

    public String getCheckCount() {
        return checkCount;
    }

    public void setCheckCount(String checkCount) {
        this.checkCount = checkCount;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public String getCurrentStageTime() {
        return currentStageTime;
    }

    public void setCurrentStageTime(String currentStageTime) {
        this.currentStageTime = currentStageTime;
    }

    public String getFinshFlag() {
        return finshFlag;
    }

    public void setFinshFlag(String finshFlag) {
        this.finshFlag = finshFlag;
    }

    public String getDemand_property() {
        return demand_property;
    }

    public void setDemand_property(String demand_property) {
        this.demand_property = demand_property;
    }

    public Boolean getDelayFlag() {
        return delayFlag;
    }

    public void setDelayFlag(Boolean delayFlag) {
        this.delayFlag = delayFlag;
    }

    public DemandBaseInfo(ObjectId _id, String id, String demand_type, Integer demand_status_normal, Integer demand_status_special, String oa_contact_no, String oa_contact_name, String oa_receive_date, String demand_instruction, Boolean ui_verify, Boolean is_verify, Boolean is_ipmp_unit_leader, String ui_verify_user, String design_status, String demand_desc, Integer demand_flag, String demand_no, Set<String> impl_track_user, String demand_create_user, UserInfo demand_create_user_all, String demand_create_time, String demand_assess_date, String demand_leader_group, String leader_group_status, String demand_leader_group_cn, HashSet<String> demand_leader, List<UserInfo> demand_leader_all, String priority, String plan_start_date, String plan_inner_test_date, String plan_test_date, String plan_test_finish_date, String plan_product_date, String real_start_date, String real_inner_test_date, String real_test_date, String real_test_finish_date, String real_product_date, HashSet<String> relate_part, ArrayList<RelatePartDetail> relate_part_detail, Boolean is_canceled, Map<String, List<Map<String, String>>> designMap, String uploader, String firstUploader, String designRemark, List<DesignDoc> designDoc, String isTransferRqrmnt, Boolean ui_status, String assess_flag, Boolean addImplFlag, Boolean havaImpl, Double dept_workload, Double company_workload) {
        this._id = _id;
        this.id = id;
        this.demand_type = demand_type;
        this.demand_status_normal = demand_status_normal;
        this.demand_status_special = demand_status_special;
        this.oa_contact_no = oa_contact_no;
        this.oa_contact_name = oa_contact_name;
        this.oa_receive_date = oa_receive_date;
        this.demand_instruction = demand_instruction;
        this.ui_verify = ui_verify;
        this.is_verify = is_verify;
        this.is_ipmp_unit_leader = is_ipmp_unit_leader;
        this.ui_verify_user = ui_verify_user;
        this.design_status = design_status;
        this.demand_desc = demand_desc;
        this.demand_flag = demand_flag;
        this.demand_no = demand_no;
        this.impl_track_user = impl_track_user;
        this.demand_create_user = demand_create_user;
        this.demand_create_user_all = demand_create_user_all;
        this.demand_create_time = demand_create_time;
        this.demand_assess_date = demand_assess_date;
        this.demand_leader_group = demand_leader_group;
        this.leader_group_status = leader_group_status;
        this.demand_leader_group_cn = demand_leader_group_cn;
        this.demand_leader = demand_leader;
        this.demand_leader_all = demand_leader_all;
        this.priority = priority;
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
        this.relate_part = relate_part;
        this.relate_part_detail = relate_part_detail;
        this.is_canceled = is_canceled;
        this.designMap = designMap;
        this.uploader = uploader;
        this.firstUploader = firstUploader;
        this.designRemark = designRemark;
        this.designDoc = designDoc;
        this.isTransferRqrmnt = isTransferRqrmnt;
        this.ui_status = ui_status;
        this.assess_flag = assess_flag;
        this.addImplFlag = addImplFlag;
        this.havaImpl = havaImpl;
        this.dept_workload = dept_workload;
        this.company_workload = company_workload;
    }

    public DemandBaseInfo() {
    }
}