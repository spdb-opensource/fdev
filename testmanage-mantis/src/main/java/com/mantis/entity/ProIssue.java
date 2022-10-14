package com.mantis.entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProIssue {
	
	private String id;
	
	private String requirement_name;//需求名称
	
	private String occurred_time;//发生时间
	
	private String issue_type;//问题类型
	
	private String is_trigger_issue;//是否造成生产问题
	
	private String issue_level;//生产问题级别
	
	private String task_no;//任务编号
	
	private String module;//所属板块
	
	private String problem_phenomenon;//问题现象/问题描述

	private String release_node;//投产窗口
	
	private String issue_reason;//问题原因
	
	private String influence_area;//影响范围
	
	private String dev_responsible;//开发责任人
	
	private String audit_responsible;//审核责任人
	
	private String test_responsible;//内测责任人
	
	private String task_responsible;//牵头任务责任人
	
	private String discover_stage;//发现阶段
	
	private String improvement_measures;//改进措施
	
	private String reviewer_comment;//评审意见
	
	private String reviewer_status;//评审状态
	
	private String deal_status;//处理状态

	private String orfanizer; //编写人

	private String is_uat_replication;//能否uat复现

	private String is_rel_replication;//能否rel复现

	private String is_involve_urgency;//是否涉及紧急需求

	private String is_gray_replication;//能否灰度复现

	private String backlog_schedule;//待办事项
	
	private String backlog_schedule_reviewer;//待办事项负责人
	
	private String backlog_schedule_complete_time;//待办事项完成时间
	
	private String backlog_schedule_current_completion;//待办事项当前完成情况
	
	private String backlog_schedule_complete_percentage;//待办事项完成百分比
	
	private String remark;//备注
	
	private String work_no;//工单编号
	
	private String backlog_schedule_2;//待办事项2
	
	private String backlog_schedule_reviewer_2;//待办事项负责人2
	
	private String backlog_schedule_complete_time_2;//待办事项完成时间
	
	private String backlog_schedule_current_completion_2;//待办事项当前完成情况
	
	private String backlog_schedule_complete_percentage_2;//待办事项完成百分比
	
	private String backlog_schedule_3;//待办事项2
	
	private String backlog_schedule_reviewer_3;//待办事项负责人2
	
	private String backlog_schedule_complete_time_3;//待办事项完成时间
	
	private String backlog_schedule_current_completion_3;//待办事项当前完成情况
	
	private String backlog_schedule_complete_percentage_3;//待办事项完成百分比
	
	private List<Map<String,String>> backlog_schedule_list;//待办事项列表
	
	private String responsible; //问责人
	
	private String responsibility_type; //问责类型
	
	private String responsibility_content; //问责内容
	
	private String responsible_2; //问责人
	
	private String responsibility_type_2; //问责类型
	
	private String responsibility_content_2; //问责内容
	
	private String responsible_3; //问责人
	
	private String responsibility_type_3; //问责类型
	
	private String responsibility_content_3; //问责内容

	private Set<String> companys;
	
	private List<Map<String,String>> responsible_list; //问责列表

	private String first_occurred_time;//首次发生时间

	private String location_time;//定位时间

	private String repair_time;//修复时间

	private String reviewer_time;//评审时间

	private String reviewer;//评审人

	private String emergency_process;//应急过程

	private String emergency_responsible;//应急负责人
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRequirement_name() {
		return requirement_name;
	}

	public void setRequirement_name(String requirement_name) {
		this.requirement_name = requirement_name;
	}

	public String getOccurred_time() {
		return occurred_time;
	}

	public void setOccurred_time(String occurred_time) {
		this.occurred_time = occurred_time;
	}

	public String getIssue_type() {
		return issue_type;
	}

	public void setIssue_type(String issue_type) {
		this.issue_type = issue_type;
	}

	public String getIs_trigger_issue() {
		return is_trigger_issue;
	}

	public void setIs_trigger_issue(String is_trigger_issue) {
		this.is_trigger_issue = is_trigger_issue;
	}

	public String getIssue_level() {
		return issue_level;
	}

	public void setIssue_level(String issue_level) {
		this.issue_level = issue_level;
	}

	public String getTask_no() {
		return task_no;
	}

	public void setTask_no(String task_no) {
		this.task_no = task_no;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getProblem_phenomenon() {
		return problem_phenomenon;
	}

	public void setProblem_phenomenon(String problem_phenomenon) {
		this.problem_phenomenon = problem_phenomenon;
	}
		

	public String getIssue_reason() {
		return issue_reason;
	}

	public void setIssue_reason(String issue_reason) {
		this.issue_reason = issue_reason;
	}

	public String getInfluence_area() {
		return influence_area;
	}

	public void setInfluence_area(String influence_area) {
		this.influence_area = influence_area;
	}

	public String getDev_responsible() {
		return dev_responsible;
	}

	public void setDev_responsible(String dev_responsible) {
		this.dev_responsible = dev_responsible;
	}

	public String getAudit_responsible() {
		return audit_responsible;
	}

	public void setAudit_responsible(String audit_responsible) {
		this.audit_responsible = audit_responsible;
	}

	public String getTest_responsible() {
		return test_responsible;
	}

	public void setTest_responsible(String test_responsible) {
		this.test_responsible = test_responsible;
	}

	public String getTask_responsible() {
		return task_responsible;
	}

	public void setTask_responsible(String task_responsible) {
		this.task_responsible = task_responsible;
	}

	public String getDiscover_stage() {
		return discover_stage;
	}

	public void setDiscover_stage(String discover_stage) {
		this.discover_stage = discover_stage;
	}

	public String getImprovement_measures() {
		return improvement_measures;
	}

	public void setImprovement_measures(String improvement_measures) {
		this.improvement_measures = improvement_measures;
	}

	public String getReviewer_comment() {
		return reviewer_comment;
	}

	public void setReviewer_comment(String reviewer_comment) {
		this.reviewer_comment = reviewer_comment;
	}

	public String getReviewer_status() {
		return reviewer_status;
	}

	public void setReviewer_status(String reviewer_status) {
		this.reviewer_status = reviewer_status;
	}

	public String getDeal_status() {
		return deal_status;
	}

	public void setDeal_status(String deal_status) {
		this.deal_status = deal_status;
	}

	public String getBacklog_schedule() {
		return backlog_schedule;
	}

	public void setBacklog_schedule(String backlog_schedule) {
		this.backlog_schedule = backlog_schedule;
	}

	public String getBacklog_schedule_reviewer() {
		return backlog_schedule_reviewer;
	}

	public void setBacklog_schedule_reviewer(String backlog_schedule_reviewer) {
		this.backlog_schedule_reviewer = backlog_schedule_reviewer;
	}

	public String getBacklog_schedule_complete_time() {
		return backlog_schedule_complete_time;
	}

	public void setBacklog_schedule_complete_time(String backlog_schedule_complete_time) {
		this.backlog_schedule_complete_time = backlog_schedule_complete_time;
	}

	public String getBacklog_schedule_current_completion() {
		return backlog_schedule_current_completion;
	}

	public void setBacklog_schedule_current_completion(String backlog_schedule_current_completion) {
		this.backlog_schedule_current_completion = backlog_schedule_current_completion;
	}

	public String getBacklog_schedule_complete_percentage() {
		return backlog_schedule_complete_percentage;
	}

	public void setBacklog_schedule_complete_percentage(String backlog_schedule_complete_percentage) {
		this.backlog_schedule_complete_percentage = backlog_schedule_complete_percentage;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getWork_no() {
		return work_no;
	}

	public void setWork_no(String work_no) {
		this.work_no = work_no;
	}

	public String getBacklog_schedule_2() {
		return backlog_schedule_2;
	}

	public void setBacklog_schedule_2(String backlog_schedule_2) {
		this.backlog_schedule_2 = backlog_schedule_2;
	}

	public String getBacklog_schedule_reviewer_2() {
		return backlog_schedule_reviewer_2;
	}

	public void setBacklog_schedule_reviewer_2(String backlog_schedule_reviewer_2) {
		this.backlog_schedule_reviewer_2 = backlog_schedule_reviewer_2;
	}

	public String getBacklog_schedule_complete_time_2() {
		return backlog_schedule_complete_time_2;
	}

	public void setBacklog_schedule_complete_time_2(String backlog_schedule_complete_time_2) {
		this.backlog_schedule_complete_time_2 = backlog_schedule_complete_time_2;
	}

	public String getBacklog_schedule_current_completion_2() {
		return backlog_schedule_current_completion_2;
	}

	public void setBacklog_schedule_current_completion_2(String backlog_schedule_current_completion_2) {
		this.backlog_schedule_current_completion_2 = backlog_schedule_current_completion_2;
	}

	public String getBacklog_schedule_complete_percentage_2() {
		return backlog_schedule_complete_percentage_2;
	}

	public void setBacklog_schedule_complete_percentage_2(String backlog_schedule_complete_percentage_2) {
		this.backlog_schedule_complete_percentage_2 = backlog_schedule_complete_percentage_2;
	}

	public String getBacklog_schedule_3() {
		return backlog_schedule_3;
	}

	public void setBacklog_schedule_3(String backlog_schedule_3) {
		this.backlog_schedule_3 = backlog_schedule_3;
	}

	public String getBacklog_schedule_reviewer_3() {
		return backlog_schedule_reviewer_3;
	}

	public void setBacklog_schedule_reviewer_3(String backlog_schedule_reviewer_3) {
		this.backlog_schedule_reviewer_3 = backlog_schedule_reviewer_3;
	}

	public String getBacklog_schedule_complete_time_3() {
		return backlog_schedule_complete_time_3;
	}

	public void setBacklog_schedule_complete_time_3(String backlog_schedule_complete_time_3) {
		this.backlog_schedule_complete_time_3 = backlog_schedule_complete_time_3;
	}

	public String getBacklog_schedule_current_completion_3() {
		return backlog_schedule_current_completion_3;
	}

	public void setBacklog_schedule_current_completion_3(String backlog_schedule_current_completion_3) {
		this.backlog_schedule_current_completion_3 = backlog_schedule_current_completion_3;
	}

	public String getBacklog_schedule_complete_percentage_3() {
		return backlog_schedule_complete_percentage_3;
	}

	public void setBacklog_schedule_complete_percentage_3(String backlog_schedule_complete_percentage_3) {
		this.backlog_schedule_complete_percentage_3 = backlog_schedule_complete_percentage_3;
	}

	public String getResponsible() {
		return responsible;
	}

	public void setResponsible(String responsible) {
		this.responsible = responsible;
	}

	public String getResponsibility_type() {
		return responsibility_type;
	}

	public void setResponsibility_type(String responsibility_type) {
		this.responsibility_type = responsibility_type;
	}

	public String getResponsibility_content() {
		return responsibility_content;
	}

	public void setResponsibility_content(String responsibility_content) {
		this.responsibility_content = responsibility_content;
	}

	public String getResponsible_2() {
		return responsible_2;
	}

	public void setResponsible_2(String responsible_2) {
		this.responsible_2 = responsible_2;
	}

	public String getResponsibility_type_2() {
		return responsibility_type_2;
	}

	public void setResponsibility_type_2(String responsibility_type_2) {
		this.responsibility_type_2 = responsibility_type_2;
	}

	public String getResponsibility_content_2() {
		return responsibility_content_2;
	}

	public void setResponsibility_content_2(String responsibility_content_2) {
		this.responsibility_content_2 = responsibility_content_2;
	}

	public String getResponsible_3() {
		return responsible_3;
	}

	public void setResponsible_3(String responsible_3) {
		this.responsible_3 = responsible_3;
	}

	public String getResponsibility_type_3() {
		return responsibility_type_3;
	}

	public void setResponsibility_type_3(String responsibility_type_3) {
		this.responsibility_type_3 = responsibility_type_3;
	}

	public String getResponsibility_content_3() {
		return responsibility_content_3;
	}

	public void setResponsibility_content_3(String responsibility_content_3) {
		this.responsibility_content_3 = responsibility_content_3;
	}

	public List<Map<String, String>> getBacklog_schedule_list() {
		return backlog_schedule_list;
	}

	public void setBacklog_schedule_list(List<Map<String, String>> backlog_schedule_list) {
		this.backlog_schedule_list = backlog_schedule_list;
	}

	public List<Map<String, String>> getResponsible_list() {
		return responsible_list;
	}

	public void setResponsible_list(List<Map<String, String>> responsible_list) {
		this.responsible_list = responsible_list;
	}

	public String getOrfanizer() {
		return orfanizer;
	}

	public void setOrfanizer(String orfanizer) {
		this.orfanizer = orfanizer;
	}

	public String getIs_uat_replication() {
		return is_uat_replication;
	}

	public void setIs_uat_replication(String is_uat_replication) {
		this.is_uat_replication = is_uat_replication;
	}

	public String getIs_rel_replication() {
		return is_rel_replication;
	}

	public void setIs_rel_replication(String is_rel_replication) {
		this.is_rel_replication = is_rel_replication;
	}

	public String getIs_involve_urgency() {
		return is_involve_urgency;
	}

	public void setIs_involve_urgency(String is_involve_urgency) {
		this.is_involve_urgency = is_involve_urgency;
	}

	public String getIs_gray_replication() {
		return is_gray_replication;
	}

	public void setIs_gray_replication(String is_gray_replication) {
		this.is_gray_replication = is_gray_replication;
	}

	public String getRelease_node() {
		return release_node;
	}

	public void setRelease_node(String release_node) {
		this.release_node = release_node;
	}

	public Set<String> getCompanys() {
		return companys;
	}

	public void setCompanys(Set<String> companys) {
		this.companys = companys;
	}

	public String getFirst_occurred_time() {
		return first_occurred_time;
	}

	public void setFirst_occurred_time(String first_occurred_time) {
		this.first_occurred_time = first_occurred_time;
	}

	public String getLocation_time() {
		return location_time;
	}

	public void setLocation_time(String location_time) {
		this.location_time = location_time;
	}

	public String getRepair_time() {
		return repair_time;
	}

	public void setRepair_time(String repair_time) {
		this.repair_time = repair_time;
	}

	public String getReviewer_time() {
		return reviewer_time;
	}

	public void setReviewer_time(String reviewer_time) {
		this.reviewer_time = reviewer_time;
	}

	public String getReviewer() {
		return reviewer;
	}

	public void setReviewer(String reviewer) {
		this.reviewer = reviewer;
	}

	public String getEmergency_process() {
		return emergency_process;
	}

	public void setEmergency_process(String emergency_process) {
		this.emergency_process = emergency_process;
	}

	public String getEmergency_responsible() {
		return emergency_responsible;
	}

	public void setEmergency_responsible(String emergency_responsible) {
		this.emergency_responsible = emergency_responsible;
	}
}
