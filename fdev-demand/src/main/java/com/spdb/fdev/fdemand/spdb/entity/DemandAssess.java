package com.spdb.fdev.fdemand.spdb.entity;

import com.spdb.fdev.fdemand.base.dict.Dict;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

/**
 * @author zhanghp4
 */
@Component
@Document(collection = Dict.DEMAND_ASSESS)
public class DemandAssess extends BaseEntity {

    @Field("id")
    private String id;

    @Field("demand_type")
    @ApiModelProperty(value = "需求类型")
    private String demand_type;

    @Field("demand_status")
    @ApiModelProperty(value = "分析状态，分析中:1，分析完成:2，暂缓中：3，撤销:9")
    private Integer demand_status;

    @ApiModelProperty(value = "分析状态，分析中:1，分析完成:2，撤销:9")
    private String demand_status_name;

    @Field("start_assess_date")
    @ApiModelProperty(value = "起始评估日期")
    private String start_assess_date;

    @Field("end_assess_date")
    @ApiModelProperty(value = "完成评估日期")
    private String end_assess_date;

    @Indexed
    @Field("oa_contact_no")
    @ApiModelProperty(value = "需求编号")
    private String oa_contact_no;

    @Field("oa_contact_name")
    @ApiModelProperty(value = "需求名称")
    private String oa_contact_name;

    @Field("demand_leader_group")
    @ApiModelProperty(value = "需求牵头小组")
    private String demand_leader_group;
    private String demand_leader_group_cn;

    @Field("demand_leader")
    @ApiModelProperty(value = "需求牵头人")
    private HashSet<String> demand_leader;
    private HashSet<UserInfo> demand_leader_info;

    @Field("priority")
    @ApiModelProperty(value = "优先级")
    private String priority;

    @Field("overdue_type")
    @ApiModelProperty(value = "超期分类")
    private String overdue_type;
    private String overdue_type_cn;

    @Field("assess_present")
    @ApiModelProperty(value = "评估现状")
    private String assess_present;

    @Field("assess_days")
    @ApiModelProperty(value = "评估天数")
    private Integer assess_days;

    private String demand_id;

    @ApiModelProperty(value = "操作情况：不展示：noshow，置灰提示：tip，可展示：show")
    private String operate_flag;

    @ApiModelProperty(value = "confluence状态 - 可配置 - Dict.confState")
    private String conf_state;
    private String conf_state_cn;

    @ApiModelProperty(value = "定稿日期")
    private String final_date;

    @Field("conf_url")
    @ApiModelProperty(value = "conf地址")
    private String conf_url;

    @Field("access_times")
    @ApiModelProperty(value = "评估时间段")
    private List<String> access_times;

    @Field("confirm_from")
    @ApiModelProperty(value = "确认分析完成来源:ipmp同步;manual手动")
    private String confirm_from;

    @ApiModelProperty(value = "是否拥有手动确认权限")
    private Boolean confirmStatus;

    @Field("approve_time_list")
    @ApiModelProperty(value = "定稿日期修改成功时间列表")
    private List<String> approve_time_list;

    @ApiModelProperty(value = "修改定稿日期状态，0代表未修改，1代表已修改过一次，2代表无法修改")
    private Integer final_date_status;

    @ApiModelProperty(value = "如果需要审批，则提交申请原因")
    private String apply_reason;

    public Integer getDemand_status() {
        return demand_status;
    }

    public void setDemand_status(Integer demand_status) {
        this.demand_status = demand_status;
    }

    public String getStart_assess_date() {
        return start_assess_date;
    }

    public void setStart_assess_date(String start_assess_date) {
        this.start_assess_date = start_assess_date;
    }

    public String getEnd_assess_date() {
        return end_assess_date;
    }

    public void setEnd_assess_date(String end_assess_date) {
        this.end_assess_date = end_assess_date;
    }

    public String getOa_contact_no() {
        return oa_contact_no;
    }

    public void setOa_contact_no(String oa_contact_no) {
        this.oa_contact_no = oa_contact_no;
    }

    public String getOa_contact_name() {
        return oa_contact_name;
    }

    public void setOa_contact_name(String oa_contact_name) {
        this.oa_contact_name = oa_contact_name;
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

    public String getOverdue_type() {
        return overdue_type;
    }

    public void setOverdue_type(String overdue_type) {
        this.overdue_type = overdue_type;
    }

    public String getAssess_present() {
        return assess_present;
    }

    public void setAssess_present(String assess_present) {
        this.assess_present = assess_present;
    }

    public String getDemand_type() {
        return demand_type;
    }

    public void setDemand_type(String demand_type) {
        this.demand_type = demand_type;
    }

    public String getDemand_id() {
        return demand_id;
    }

    public void setDemand_id(String demand_id) {
        this.demand_id = demand_id;
    }

    public String getDemand_leader_group_cn() {
        return demand_leader_group_cn;
    }

    public void setDemand_leader_group_cn(String demand_leader_group_cn) {
        this.demand_leader_group_cn = demand_leader_group_cn;
    }

    public HashSet<UserInfo> getDemand_leader_info() {
        return demand_leader_info;
    }

    public void setDemand_leader_info(HashSet<UserInfo> demand_leader_info) {
        this.demand_leader_info = demand_leader_info;
    }

    public String getOperate_flag() {
        return operate_flag;
    }

    public void setOperate_flag(String operate_flag) {
        this.operate_flag = operate_flag;
    }

    public Integer getAssess_days() {
        return assess_days;
    }

    public void setAssess_days(Integer assess_days) {
        this.assess_days = assess_days;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDemand_status_name() {
        return demand_status_name;
    }

    public void setDemand_status_name(String demand_status_name) {
        this.demand_status_name = demand_status_name;
    }

    public String getConf_state() {
        return conf_state;
    }

    public void setConf_state(String conf_state) {
        this.conf_state = conf_state;
    }

    public String getFinal_date() {
        return final_date;
    }

    public void setFinal_date(String final_date) {
        this.final_date = final_date;
    }

    public String getConf_url() {
        return conf_url;
    }

    public void setConf_url(String conf_url) {
        this.conf_url = conf_url;
    }

    public String getOverdue_type_cn() {
        return overdue_type_cn;
    }

    public void setOverdue_type_cn(String overdue_type_cn) {
        this.overdue_type_cn = overdue_type_cn;
    }

    public String getConf_state_cn() {
        return conf_state_cn;
    }

    public void setConf_state_cn(String conf_state_cn) {
        this.conf_state_cn = conf_state_cn;
    }

    public List<String> getAccess_times() {
        return access_times;
    }

    public void setAccess_times(List<String> access_times) {
        this.access_times = access_times;
    }

    public Boolean getConfirmStatus() {
        return confirmStatus;
    }

    public void setConfirmStatus(Boolean confirmStatus) {
        this.confirmStatus = confirmStatus;
    }

    public String getConfirm_from() {
        return confirm_from;
    }

    public void setConfirm_from(String confirm_from) {
        this.confirm_from = confirm_from;
    }


    public List<String> getApprove_time_list() {
        return approve_time_list;
    }

    public void setApprove_time_list(List<String> approve_time_list) {
        this.approve_time_list = approve_time_list;
    }

    public String getApply_reason() {
        return apply_reason;
    }

    public void setApply_reason(String apply_reason) {
        this.apply_reason = apply_reason;
    }


    public Integer getFinal_date_status() {
        return final_date_status;
    }

    public void setFinal_date_status(Integer final_date_status) {
        this.final_date_status = final_date_status;
    }


}
