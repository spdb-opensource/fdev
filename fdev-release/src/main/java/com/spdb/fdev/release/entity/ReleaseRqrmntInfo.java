package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Document(collection = "release_rqrmnt_info")
@ApiModel(value = "投产任务")
public class ReleaseRqrmntInfo {

    @Id
    private ObjectId _id;

    @Field("id")
    private String id;

    /**
     * 组id
     */
    @Field("group_id")
    private String group_id;

    /**
     * 组名称
     */
    private String group_name;

    /**
     * 投产日期
     */
    @Field("release_date")
    private String release_date;

    /**
     * 需求id
     */
    @Field("rqrmnt_id")
    private String rqrmnt_id;

    /**
     * 需求编号
     */
    @Field("rqrmnt_no")
    private String rqrmnt_no;

    /**
     * 需求名称
     */
    @Field("rqrmnt_name")
    private String rqrmnt_name;

    /**
     * 需求行内负责人
     */
    private List<Map> rqrmnt_spdb_manager;

    /**
     * 投产确认书情况
     */    //0 未到达  1 已到达
    private String release_confirm_doc;

    /**
     * 整包提测
     */
    @Field("package_submit_test")  //0 未打包  // 1未提测   // 2已提测
    private String package_submit_test;

    /**
     * 准生产提测
     */
    @Field("rel_test") //0 未打包  // 1未提测   // 2已提测
    private String rel_test;

    /**
     * 任务id列表
     */
    @Field("task_ids")
    private Set<String> task_ids;

    /**
     * 需求类型
     */
    @Field("type")
    private String type;

    /**
     * 新增应用标识
     */
    @Field("new_add_sign")
    private String new_add_sign;

    /**
     *  涉及整包提请安全测试标志
     */
    private String security;

    /**
     * 重点测试
     */
    private String testKeyNote;

    /**
     * 业务联系人
     */
    private String rqrmntContact;

    /**
     * 需规地址
     */
    private List doc;
    /*
     * 公共配置文件
     */
    @Field("commonProfile")
    private String commonProfile;
    /**
     * 涉及数据库
     */
    @Field("dataBaseAlter")
    private String dataBaseAlter;
    /**
     * 涉及系统
     */
    @Field("otherSystem")
    private String otherSystem;
    /**
     * 特殊情况
     */
    @Field("specialCase")
    private String specialCase;

    @Field("create_time")
    @ApiModelProperty(value="创建时间")
    private String create_time;

    @Field("update_time")
    @ApiModelProperty(value="修改时间")
    private String update_time;
    /**
     * 标签
     * 业务需求：
     * （1）涉及处对处流程需求判断条件：t-7日期0点前未在业务需求打开“上线确认书情况”开关；
     * （2）涉及总对总流程判断条件：t-3日期0点之前未在业务需求任务详情页面打开“上线确认书情况”开关；
     * （3）不予投产判断条件：t-2日期9点之后打开“上线确认书情况开关”。（T-3邮件通知）
     * （4）日常流程需求判断条件：T-7日期之前业务需求打开“上线确认书情况开关”
     *  科技需求：
     * （1）涉及条线经理审批判断条件：t-4日期至t-1日期完成测试的需求；（基于内测完成时间）
     * （2）涉及冯总审批判断条件：t当日完成测试的需求需要走总对总流程审批。（基于内测完成时间）
     * （3）日常流程需求判断条件：T-4日期之前内测完成
     */
    private String tag;

    /**
     * 最大任务状态
     */
    private String maxTaskStatus;
    /**
     * 下载标识
     */
    @Field("export_flag")
    @ApiModelProperty(value="导出标识")
    private String exportFlag;
    /**
     * 实施单元
     */
    @Field("ipmpUnit")
    @ApiModelProperty(value="实施单元 ipmp实施单元编号/FDEV编号")
    private String ipmpUnit;

    /**
     * 科技类型
     */
    @Field("technology_type")
    @ApiModelProperty(value="科技类型")
    private String technology_type;

    /**
     * 上线确认书实际到达时间
     */
    @Field("confirmFileDate")
    @ApiModelProperty(value = "上线确认书实际到达时间")
    private String confirmFileDate;
    
    /**
     * 上线确认书实际到达时间
     */
    @Field("confirmFileDateList")
    @ApiModelProperty(value = "上线确认书实际到达时间集合")
    private List<String> confirmFileDateList;

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

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getRqrmnt_no() {
        return rqrmnt_no;
    }

    public void setRqrmnt_no(String rqrmnt_no) {
        this.rqrmnt_no = rqrmnt_no;
    }

    public String getRqrmnt_name() {
        return rqrmnt_name;
    }

    public void setRqrmnt_name(String rqrmnt_name) {
        this.rqrmnt_name = rqrmnt_name;
    }

    public List<Map> getRqrmnt_spdb_manager() {
        return rqrmnt_spdb_manager;
    }

    public void setRqrmnt_spdb_manager(List<Map> rqrmnt_spdb_manager) {
        this.rqrmnt_spdb_manager = rqrmnt_spdb_manager;
    }

    public String getRelease_confirm_doc() {
        return release_confirm_doc;
    }

    public void setRelease_confirm_doc(String release_confirm_doc) {
        this.release_confirm_doc = release_confirm_doc;
    }

    public String getPackage_submit_test() {
        return package_submit_test;
    }

    public void setPackage_submit_test(String package_submit_test) {
        this.package_submit_test = package_submit_test;
    }

    public String getRel_test() {
        return rel_test;
    }

    public void setRel_test(String rel_test) {
        this.rel_test = rel_test;
    }

    public Set<String> getTask_ids() {
        return task_ids;
    }

    public void setTask_ids(Set<String> task_ids) {
        this.task_ids = task_ids;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMaxTaskStatus() {
        return maxTaskStatus;
    }

    public void setMaxTaskStatus(String maxTaskStatus) {
        this.maxTaskStatus = maxTaskStatus;
    }

    public String getRqrmnt_id() {
        return rqrmnt_id;
    }

    public void setRqrmnt_id(String rqrmnt_id) {
        this.rqrmnt_id = rqrmnt_id;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getTestKeyNote() {
        return testKeyNote;
    }

    public void setTestKeyNote(String testKeyNote) {
        this.testKeyNote = testKeyNote;
    }

    public String getRqrmntContact() {
        return rqrmntContact;
    }

    public void setRqrmntContact(String rqrmntContact) {
        this.rqrmntContact = rqrmntContact;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public List getDoc() {
        return doc;
    }

    public void setDoc(List doc) {
        this.doc = doc;
    }

    public String getNew_add_sign() {
        return new_add_sign;
    }

    public void setNew_add_sign(String new_add_sign) {
        this.new_add_sign = new_add_sign;
    }

    public String getCommonProfile() {
        return commonProfile;
    }

    public void setCommonProfile(String commonProfile) {
        this.commonProfile = commonProfile;
    }

    public String getDataBaseAlter() {
        return dataBaseAlter;
    }

    public void setDataBaseAlter(String dataBaseAlter) {
        this.dataBaseAlter = dataBaseAlter;
    }

    public String getOtherSystem() {
        return otherSystem;
    }

    public void setOtherSystem(String otherSystem) {
        this.otherSystem = otherSystem;
    }

    public String getSpecialCase() {
        return specialCase;
    }

    public void setSpecialCase(String specialCase) {
        this.specialCase = specialCase;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getExportFlag() {
        return exportFlag;
    }

    public void setExportFlag(String exportFlag) {
        this.exportFlag = exportFlag;
    }

    public String getIpmpUnit() {
        return ipmpUnit;
    }

    public void setIpmpUnit(String ipmpUnit) {
        this.ipmpUnit = ipmpUnit;
    }

    public String getTechnology_type() {
        return technology_type;
    }

    public void setTechnology_type(String technology_type) {
        this.technology_type = technology_type;
    }

	public String getConfirmFileDate() {
		return confirmFileDate;
	}

	public void setConfirmFileDate(String confirmFileDate) {
		this.confirmFileDate = confirmFileDate;
	}

	public List<String> getConfirmFileDateList() {
		return confirmFileDateList;
	}

	public void setConfirmFileDateList(List<String> confirmFileDateList) {
		this.confirmFileDateList = confirmFileDateList;
	}
	
}
