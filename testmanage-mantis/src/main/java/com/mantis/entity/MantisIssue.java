package com.mantis.entity;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class MantisIssue implements Serializable {

    private Integer id;

    private String summary;//摘要

    private String description;//描述

    private Integer project_id;//项目id

    private String project_name;//项目名称

    /**
     * mantis: '10:新建,20:反馈,30:认可,40:已确认,50:已分配,80:已解决,90:已关闭';
     * 页面显示: '10:新建,20:拒绝,30:确认拒绝,40:延迟修复,50:打开,80:已修复,90:关闭';
     */
    private String status;//缺陷状态

    private String handler;//分派处理人

    private String handler_id;//分派处理人id

    private String handler_en_name;//分派处理人英文名

    private String reporter;//报告人

    private String date_submitted;//缺陷创建时间

    /**
     * 新功能：feature,10
     * 细节：trivial,20
     * 文字：text,30
     * 小调整：tweak,40
     * 小错误：minor,50
     * 很严重：major,60
     * 崩溃：crash,70
     * 宕机：block,80
     */
    private String severity;//严重性
    /**
     * 无：none,10
     * 低：low,20
     * 中：normal,30
     * 高：high,40
     * 紧急：urgent,50
     * 非常紧急：immediate,60
     */
    private String priority;//优先级

    /**
     * 以下为mantis自定义字段
     */

    private String stage;//归属阶段  id= 3

    private String reason;//开发原因分析

    private String flaw_source;//缺陷来源

    private String system_version;//系统版本

    private String system_name;//系统名称

    private String developer;//开发人员

    private String plan_fix_date;//遗留缺陷预计修复时间

    private String flaw_type;//缺陷类型

    private String function_module;//功能模块

    private String redmine_id;//实施单元编号

    private String workNo;//工单号

    private String reporter_en_name;

    private String planlist_testcase_id;

    private String main_task_no;//任务编号

    private String developer_cn;//开发人员中文名

    private String task_name;//任务名称

    private String task_id;//任务id

    private String fdev_group_name;//所属fdev小组

    private String app_name;//所属应用英文名

    private String reopen_reason;//重新打开原因

    private Integer open_num;//打开次数

    private String solve_time;//解决时长

    private String auditFlag;//审核标志,0-待审核,1-已审核

    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    public String getSummary() {
        return summary;
    }


    public void setSummary(String summary) {
        this.summary = summary;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public Integer getProject_id() {
        return project_id;
    }


    public void setProject_id(Integer project_id) {
        this.project_id = project_id;
    }


    public String getProject_name() {
        return project_name;
    }


    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }


    public String getHandler() {
        return handler;
    }


    public void setHandler(String handler) {
        this.handler = handler;
    }


    public String getSeverity() {
        return severity;
    }


    public void setSeverity(String severity) {
        this.severity = severity;
    }


    public String getPriority() {
        return priority;
    }


    public void setPriority(String priority) {
        this.priority = priority;
    }


    public String getStage() {
        return stage;
    }


    public void setStage(String stage) {
        this.stage = stage;
    }


    public String getReason() {
        return reason;
    }


    public void setReason(String reason) {
        this.reason = reason;
    }


    public String getFlaw_source() {
        return flaw_source;
    }


    public void setFlaw_source(String flaw_source) {
        this.flaw_source = flaw_source;
    }


    public String getSystem_version() {
        return system_version;
    }


    public void setSystem_version(String system_version) {
        this.system_version = system_version;
    }


    public String getSystem_name() {
        return system_name;
    }


    public void setSystem_name(String system_name) {
        this.system_name = system_name;
    }


    public String getDeveloper() {
        return developer;
    }


    public void setDeveloper(String developer) {
        this.developer = developer;
    }


    public String getPlan_fix_date() {
        return plan_fix_date;
    }


    public void setPlan_fix_date(String plan_fix_date) {
        this.plan_fix_date = plan_fix_date;
    }


    public String getFlaw_type() {
        return flaw_type;
    }


    public void setFlaw_type(String flaw_type) {
        this.flaw_type = flaw_type;
    }


    public String getFunction_module() {
        return function_module;
    }


    public void setFunction_module(String function_module) {
        this.function_module = function_module;
    }


    public String getRedmine_id() {
        return redmine_id;
    }


    public void setRedmine_id(String redmine_id) {
        this.redmine_id = redmine_id;
    }


    public String getWorkNo() {
        return workNo;
    }


    public void setWorkNo(String workNo) {
        this.workNo = workNo;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public String getReporter() {
        return reporter;
    }


    public void setReporter(String reporter) {
        this.reporter = reporter;
    }


    public String getDate_submitted() {
        return date_submitted;
    }


    public void setDate_submitted(String date_submitted) {
        this.date_submitted = date_submitted;
    }


    public String getHandler_id() {
        return handler_id;
    }


    public void setHandler_id(String handler_id) {
        this.handler_id = handler_id;
    }


    public String getHandler_en_name() {
        return handler_en_name;
    }

    public void setHandler_en_name(String handler_en_name) {
        this.handler_en_name = handler_en_name;
    }

    public String getReporter_en_name() {
        return reporter_en_name;
    }

    public void setReporter_en_name(String reporter_en_name) {
        this.reporter_en_name = reporter_en_name;
    }

    public String getPlanlist_testcase_id() {
        return planlist_testcase_id;
    }

    public void setPlanlist_testcase_id(String planlist_testcase_id) {
        this.planlist_testcase_id = planlist_testcase_id;
    }

    public String getMain_task_no() {
        return main_task_no;
    }

    public void setMain_task_no(String main_task_no) {
        this.main_task_no = main_task_no;
    }

    public String getDeveloper_cn() {
        return developer_cn;
    }

    public void setDeveloper_cn(String developer_cn) {
        this.developer_cn = developer_cn;
    }

    public String getTask_name() {
        return task_name;
    }

    public void setTask_name(String task_name) {
        this.task_name = task_name;
    }

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public String getFdev_group_name() {
        return fdev_group_name;
    }

    public void setFdev_group_name(String fdev_group_name) {
        this.fdev_group_name = fdev_group_name;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getReopen_reason() {
        return reopen_reason;
    }

    public void setReopen_reason(String reopen_reason) {
        this.reopen_reason = reopen_reason;
    }

    public Integer getOpen_num() {
        return open_num;
    }

    public void setOpen_num(Integer open_num) {
        this.open_num = open_num;
    }

    public String getSolve_time() {
        return solve_time;
    }

    public void setSolve_time(String solve_time) {
        this.solve_time = solve_time;
    }

    public String getAuditFlag() {
        return auditFlag;
    }

    public void setAuditFlag(String auditFlag) {
        this.auditFlag = auditFlag;
    }
}