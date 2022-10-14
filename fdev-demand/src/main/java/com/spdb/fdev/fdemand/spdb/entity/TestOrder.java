package com.spdb.fdev.fdemand.spdb.entity;

import com.spdb.fdev.fdemand.base.dict.Dict;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Document(collection = Dict.TEST_ORDER)
public class TestOrder {

    @Id
    private ObjectId _id;

    @Field("id")
    private String id;

    //FDEV-CLOUD-年-月-日-最大值-PRO
    @ApiModelProperty(value = "提测单编号")
    private String test_order;

    //已创建、已提测、已归档、已撤销 create、test、file、deleted
    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "小组id")
    private String group;

    @Transient
    @ApiModelProperty(value = "小组中文名")
    private String group_cn;

    @ApiModelProperty(value = "需求id")
    private String demand_id;

    @ApiModelProperty(value = "需求类型")
    private String demand_type;

    @Transient
    @ApiModelProperty(value = "需求名称")
    private String oa_contact_name;

    @Transient
    @ApiModelProperty(value = "需求编号")
    private String oa_contact_no;

    @ApiModelProperty(value = "IPMP实施单元")
    private String impl_unit_num;

    @ApiModelProperty(value = "研发单元编号")
    private String fdev_implement_unit_no;

    @Transient
    @ApiModelProperty(value = "研发单元内容")
    private String implement_unit_content;

    @ApiModelProperty(value = "计划提交业测日期")
    private String plan_test_start_date;

    @ApiModelProperty(value = "业务人员邮箱")
    private String business_email;

    @ApiModelProperty(value = "开发人员")
    private String developer;

    @ApiModelProperty(value = "测试内容")
    private String test_content;

    @ApiModelProperty(value = "需求涉及的应用名称")
    private String app_name;

    @ApiModelProperty(value = "系统名称")
    private String systemName;

    @ApiModelProperty(value = "单元测试情况")
    private String unit_test_result;

    @ApiModelProperty(value = "内测通过情况")
    private String inner_test_result;

    //yes、no
    @ApiModelProperty(value = "是否涉及交易接口改动")
    private String trans_interface_change;

    //yes、no
    @ApiModelProperty(value = "是否涉及数据库改动")
    private String database_change;

    //yes、no
    @ApiModelProperty(value = "是否涉及回归测试")
    private String regress_test;

    @ApiModelProperty(value = "具体回归测试范围")
    private String regress_test_range;

    @ApiModelProperty(value = "涉及关联系统同步改造")
    private String system;

    //yes、no
    @ApiModelProperty(value = "是否涉及客户端更新")
    private String client_change;

    @ApiModelProperty(value = "客户端下载地址(明确具体的测试包)")
    private String client_download;

    @ApiModelProperty(value = "测试环境")
    private String test_environment;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "测试经理用户信息类")
    private List<UserInfo> test_manager_info;

    @ApiModelProperty(value = "提测邮件通知抄送人员id")
    private List<String> test_cc_user_ids;

    @Transient
    @ApiModelProperty(value = "提测邮件通知抄送人员")
    private List<UserInfo> test_cc_user_info;

    @ApiModelProperty(value = "测试日报抄送人员id")
    private List<String> daily_cc_user_ids;

    @Transient
    @ApiModelProperty(value = "测试日报抄送人员")
    private List<UserInfo> daily_cc_user_info;

    @ApiModelProperty(value = "提测用户id")
    private String test_user_id;

    @Transient
    @ApiModelProperty(value = "提测用户信息")
    private UserInfo test_user_info;

    @ApiModelProperty(value = "创建人员id")
    private String create_user_id;

    @Transient
    @ApiModelProperty(value = "创建人员信息")
    private UserInfo create_user_info;

    @ApiModelProperty(value = "修改人员id")
    private String update_user_id;

    @Transient
    @ApiModelProperty(value = "修改人员信息")
    private UserInfo update_user_info;

    @ApiModelProperty(value = "创建时间")//yyyy-MM-dd HH:mm:ss
    private String create_time;

    @ApiModelProperty(value = "提交时间")
    private String submit_time;

    @ApiModelProperty(value = "撤销时间")
    private String deleted_time;

    @ApiModelProperty(value = "归档时间")
    private String file_time;

    @ApiModelProperty(value = "修改时间")
    private String update_time;

    @Transient
    @ApiModelProperty(value = "编辑标识")
    private Map<String,Object> update_flag;

    @Transient
    @ApiModelProperty(value = "提交标识")
    private Map<String,Object> submit_flag;

    @Transient
    @ApiModelProperty(value = "删除标识")
    private Map<String,Object> delete_flag;

    @Transient
    @ApiModelProperty(value = "文件标识")
    private Map<String,Object> file_flag;

    @Transient
    @ApiModelProperty(value = "工单编号")
    private String workNo;

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getWorkNo() {
        return workNo;
    }

    public void setWorkNo(String workNo) {
        this.workNo = workNo;
    }

    public Map<String, Object> getFile_flag() {
        return file_flag;
    }

    public void setFile_flag(Map<String, Object> file_flag) {
        this.file_flag = file_flag;
    }

    public String getImplement_unit_content() {
        return implement_unit_content;
    }

    public void setImplement_unit_content(String implement_unit_content) {
        this.implement_unit_content = implement_unit_content;
    }

    public String getDemand_type() {
        return demand_type;
    }

    public void setDemand_type(String demand_type) {
        this.demand_type = demand_type;
    }

    public Map<String, Object> getUpdate_flag() {
        return update_flag;
    }

    public void setUpdate_flag(Map<String, Object> update_flag) {
        this.update_flag = update_flag;
    }

    public Map<String, Object> getSubmit_flag() {
        return submit_flag;
    }

    public void setSubmit_flag(Map<String, Object> submit_flag) {
        this.submit_flag = submit_flag;
    }

    public Map<String, Object> getDelete_flag() {
        return delete_flag;
    }

    public void setDelete_flag(Map<String, Object> delete_flag) {
        this.delete_flag = delete_flag;
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

    public String getTest_order() {
        return test_order;
    }

    public void setTest_order(String test_order) {
        this.test_order = test_order;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup_cn() {
        return group_cn;
    }

    public void setGroup_cn(String group_cn) {
        this.group_cn = group_cn;
    }

    public String getDemand_id() {
        return demand_id;
    }

    public void setDemand_id(String demand_id) {
        this.demand_id = demand_id;
    }

    public String getOa_contact_name() {
        return oa_contact_name;
    }

    public void setOa_contact_name(String oa_contact_name) {
        this.oa_contact_name = oa_contact_name;
    }

    public String getOa_contact_no() {
        return oa_contact_no;
    }

    public void setOa_contact_no(String oa_contact_no) {
        this.oa_contact_no = oa_contact_no;
    }

    public String getImpl_unit_num() {
        return impl_unit_num;
    }

    public void setImpl_unit_num(String impl_unit_num) {
        this.impl_unit_num = impl_unit_num;
    }

    public String getFdev_implement_unit_no() {
        return fdev_implement_unit_no;
    }

    public void setFdev_implement_unit_no(String fdev_implement_unit_no) {
        this.fdev_implement_unit_no = fdev_implement_unit_no;
    }

    public String getPlan_test_start_date() {
        return plan_test_start_date;
    }

    public void setPlan_test_start_date(String plan_test_start_date) {
        this.plan_test_start_date = plan_test_start_date;
    }

    public String getBusiness_email() {
        return business_email;
    }

    public void setBusiness_email(String business_email) {
        this.business_email = business_email;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getTest_content() {
        return test_content;
    }

    public void setTest_content(String test_content) {
        this.test_content = test_content;
    }

    public String getApp_name() {
        return app_name;
    }

    public void setApp_name(String app_name) {
        this.app_name = app_name;
    }

    public String getUnit_test_result() {
        return unit_test_result;
    }

    public void setUnit_test_result(String unit_test_result) {
        this.unit_test_result = unit_test_result;
    }

    public String getInner_test_result() {
        return inner_test_result;
    }

    public void setInner_test_result(String inner_test_result) {
        this.inner_test_result = inner_test_result;
    }

    public String getTrans_interface_change() {
        return trans_interface_change;
    }

    public void setTrans_interface_change(String trans_interface_change) {
        this.trans_interface_change = trans_interface_change;
    }

    public String getDatabase_change() {
        return database_change;
    }

    public void setDatabase_change(String database_change) {
        this.database_change = database_change;
    }

    public String getRegress_test() {
        return regress_test;
    }

    public void setRegress_test(String regress_test) {
        this.regress_test = regress_test;
    }

    public String getRegress_test_range() {
        return regress_test_range;
    }

    public void setRegress_test_range(String regress_test_range) {
        this.regress_test_range = regress_test_range;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getClient_change() {
        return client_change;
    }

    public void setClient_change(String client_change) {
        this.client_change = client_change;
    }

    public String getClient_download() {
        return client_download;
    }

    public void setClient_download(String client_download) {
        this.client_download = client_download;
    }

    public String getTest_environment() {
        return test_environment;
    }

    public void setTest_environment(String test_environment) {
        this.test_environment = test_environment;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<UserInfo> getTest_manager_info() {
        return test_manager_info;
    }

    public void setTest_manager_info(List<UserInfo> test_manager_info) {
        this.test_manager_info = test_manager_info;
    }

    public List<String> getTest_cc_user_ids() {
        return test_cc_user_ids;
    }

    public void setTest_cc_user_ids(List<String> test_cc_user_ids) {
        this.test_cc_user_ids = test_cc_user_ids;
    }

    public List<UserInfo> getTest_cc_user_info() {
        return test_cc_user_info;
    }

    public void setTest_cc_user_info(List<UserInfo> test_cc_user_info) {
        this.test_cc_user_info = test_cc_user_info;
    }

    public List<String> getDaily_cc_user_ids() {
        return daily_cc_user_ids;
    }

    public void setDaily_cc_user_ids(List<String> daily_cc_user_ids) {
        this.daily_cc_user_ids = daily_cc_user_ids;
    }

    public List<UserInfo> getDaily_cc_user_info() {
        return daily_cc_user_info;
    }

    public void setDaily_cc_user_info(List<UserInfo> daily_cc_user_info) {
        this.daily_cc_user_info = daily_cc_user_info;
    }

    public String getTest_user_id() {
        return test_user_id;
    }

    public void setTest_user_id(String test_user_id) {
        this.test_user_id = test_user_id;
    }

    public UserInfo getTest_user_info() {
        return test_user_info;
    }

    public void setTest_user_info(UserInfo test_user_info) {
        this.test_user_info = test_user_info;
    }

    public String getCreate_user_id() {
        return create_user_id;
    }

    public void setCreate_user_id(String create_user_id) {
        this.create_user_id = create_user_id;
    }

    public UserInfo getCreate_user_info() {
        return create_user_info;
    }

    public void setCreate_user_info(UserInfo create_user_info) {
        this.create_user_info = create_user_info;
    }

    public String getUpdate_user_id() {
        return update_user_id;
    }

    public void setUpdate_user_id(String update_user_id) {
        this.update_user_id = update_user_id;
    }

    public UserInfo getUpdate_user_info() {
        return update_user_info;
    }

    public void setUpdate_user_info(UserInfo update_user_info) {
        this.update_user_info = update_user_info;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getSubmit_time() {
        return submit_time;
    }

    public void setSubmit_time(String submit_time) {
        this.submit_time = submit_time;
    }

    public String getDeleted_time() {
        return deleted_time;
    }

    public void setDeleted_time(String deleted_time) {
        this.deleted_time = deleted_time;
    }

    public String getFile_time() {
        return file_time;
    }

    public void setFile_time(String file_time) {
        this.file_time = file_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }
}
