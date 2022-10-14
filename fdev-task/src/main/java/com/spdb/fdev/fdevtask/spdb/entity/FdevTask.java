package com.spdb.fdev.fdevtask.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Document(collection = "task")
@ApiModel(value = "任务")
public class FdevTask implements Serializable {

    private static final long serialVersionUID = -5627804709275451840L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "任务ID")
    @Indexed(unique = true)
    private String id;

    @Field("name")
    @ApiModelProperty(value = "任务名称")
    private String name;

    @Field("redmine_id")
    @ApiModelProperty(value = "redmine需求编号")
    private String redmine_id;

    @Field("desc")
    @ApiModelProperty(value = "任务描述")
    private String desc;

    @Field("spdb_master")
    @ApiModelProperty(value = "行内任务负责人")
    private String[] spdb_master;

    @Field("master")
    @ApiModelProperty(value = "任务负责人")
    private String[] master;

    @Field("group")
    @ApiModelProperty(value = "任务所属小组")
    private String group;

    @Field("project_id")
    @ApiModelProperty(value = "任务所属应用ID")
    private String project_id;

    @Field("project_name")
    @ApiModelProperty(value = "任务所属应用名称")
    private String project_name;

    @Field("plan_start_time")
    @ApiModelProperty(value = "计划启动日期")
    private String plan_start_time;

    @Field("start_time")
    @ApiModelProperty(value = "启动日期")
    private String start_time;

    @Field("plan_inner_test_time")
    @ApiModelProperty(value = "计划内测启动日期")
    private String plan_inner_test_time;

    @Field("start_inner_test_time")
    @ApiModelProperty(value = "内测启动日期")
    private String start_inner_test_time;

    @Field("uat_merge_time")
    @ApiModelProperty(value = "合并代码到UAT日期")
    private String uat_merge_time;

    @Field("rel_merge_time")
    @ApiModelProperty(value = "合并代码到REL日期")
    private String rel_merge_time;

    @Field("plan_uat_test_start_time")
    @ApiModelProperty(value = "计划uat测试启动日期")
    private String plan_uat_test_start_time;

    @Field("plan_uat_test_stop_time")
    @ApiModelProperty(value = "计划uat测试完成日期")
    private String plan_uat_test_stop_time;

    @Field("start_uat_test_time")
    @ApiModelProperty(value = "uat测试启动日期")
    private String start_uat_test_time;

    @Field("stop_uat_test_time")
    @ApiModelProperty(value = "uat测试完成日期")
    private String stop_uat_test_time;

    @Field("plan_rel_test_time")
    @ApiModelProperty(value = "计划rel测试启动日期")
    private String plan_rel_test_time;

    @Field("start_rel_test_time")
    @ApiModelProperty(value = "rel测试启动日期")
    private String start_rel_test_time;

    @Field("plan_fire_time")
    @ApiModelProperty(value = "计划投产日期")
    private String plan_fire_time;

    @Field("fire_time")
    @ApiModelProperty(value = "投产日期")
    private String fire_time;

    @Field("stage")
    @ApiModelProperty(value = "当前阶段")
    private String stage;

    @Field("old_stage")
    @ApiModelProperty(value = "撤销研发单元前的状态")
    private String old_stage;

    @Field("feature_branch")
    @ApiModelProperty(value = "对应分支")
    private String feature_branch;

    @Field("sit_merge_id")
    @ApiModelProperty(value = "Sit的mergeID")
    private String sit_merge_id;

    @Field("uat_merge_id")
    @ApiModelProperty(value = "Uat的mergeID")
    private String uat_merge_id;

    @Field("developer")
    @ApiModelProperty(value = "开发人员")
    private String[] developer;

    @Field("tester")
    @ApiModelProperty(value = "测试人员")
    private String[] tester;

    @Field("doc")
    @ApiModelProperty(value = "上传文件临时路径id")
    private String[] doc;

    @Field("concern")
    @ApiModelProperty(value = "关注者")
    private String[] concern;

    @Field("review")
    @ApiModelProperty(value = "任务关联评估项")
    private TaskReview review;

    @Field("report")
    @ApiModelProperty(value = "任务报告")
    private TaskReport report;

    @Field("creator")
    @ApiModelProperty(value = "任务创建者")
    private String creator;

    @Field("uat_testObject")
    @ApiModelProperty(value = "uat测试对象")
    private String uat_testObject;

    @Field("rqrmnt_no")
    @ApiModelProperty(value = "需求id")
    @Indexed
    private String rqrmnt_no;

    @Field("tag")
    @ApiModelProperty(value = "任务标签")
    private String[] tag;


    @Field("uat_test_time")
    @ApiModelProperty(value = "进入业务测试时间")
    private String uat_test_time;

    @Field("folder_id")
    @ApiModelProperty(value = "开发截图文档文件夹id")
    private String folder_id;

    @Field("reviewer")
    @ApiModelProperty(value = "设计还原审核人")
    private String reviewer;

    @Field("review_status")
    @ApiModelProperty(value = "设计还原审核状态")
    private String review_status;

    @Field("uiVerifyReporter")
    @ApiModelProperty(value = "UI审核发起人")
    private String uiVerifyReporter;

    @Field("doc_uploader")
    @ApiModelProperty(value = "设计还原审核文档上传人列表")
    private Map<String, List<String>> doc_uploader;

    @Field("system_remould")
    @ApiModelProperty(value = "相关库表变更是否涉及通知数据仓库等关联供数系统配套改造")
    private String system_remould;


    @Field("impl_data")
    @ApiModelProperty(value = "是否涉及在库表关联应用暂停服务期间实施数据库变更操作")
    private String impl_data;


    @Field("reinforce")
    @ApiModelProperty(value = "加固标识")
    private String reinforce;

    @Field("test_merge_id")
    @ApiModelProperty(value = "试运行合并id")
    private String test_merge_id;

    @Field("sonarId")
    @ApiModelProperty(value = "sonar扫描")
    private String sonarId;

    @Field("scanTime")
    @ApiModelProperty(value = "sonar扫描")
    private String scanTime;

    @Field("difficulty_desc")
    @ApiModelProperty(value = "任务难度描述")
    private String difficulty_desc;

    @Field("designMap")
    @ApiModelProperty(value = "设计还原相关数据")
    private Map<String, List<Map<String,String>>> designMap;
    /**
     * 有无代码变更的状态
     */
    @Field("taskType")
    @ApiModelProperty(value = "任务类型 0:开发任务;1:无代码任务 2:日常任务")
    private Integer taskType;

    /**
     *  appJava-Java微服务
     *  appVue-Vue应用
     *  appIos-IOS应用
     *  appAndroid-Android应用
     *  appDocker-容器化项目
     *  appOldService-老版服务
     *  componentWeb-前端组件
     *  componentServer-后端组件
     *  archetypeWeb-前端骨架
     *  archetypeServer-后端骨架
     *  image-镜像
     */
    @Field("applicationType")
    @ApiModelProperty(value = "应用类型")
    private String applicationType;

    /**
     * 组件的预设版本号
     * 后端骨架的目标版本号
     */
    @Field("versionNum")
    @ApiModelProperty(value = "版本号")
    private String versionNum;

    /**
     * key: sit  ,uat  ,rel  (小写)
     */
    @Field("nocodeInfoMap")
    @ApiModelProperty(value = "无代码变更信息")
    private Map<String,NoCodeInfo>  nocodeInfoMap;

    @ApiModelProperty(value = "需求编号")
    private String demandNo;

    public Map<String, NoCodeInfo> getNocodeInfoMap() {
        return nocodeInfoMap;
    }

    public void setNocodeInfoMap(Map<String, NoCodeInfo> nocodeInfoMap) {
        this.nocodeInfoMap = nocodeInfoMap;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }



    @Field("newDoc")
    @ApiModelProperty(value = "新文档数据")
    private List<Map<String,String>> newDoc;

    public List<Map<String, String>> getNewDoc() {
        return newDoc;
    }

    public void setNewDoc(List<Map<String, String>> newDoc) {
        this.newDoc = newDoc;
    }



    @Field("designRemark")
    @ApiModelProperty(value = "设计还原备注")
    private String  designRemark;

    @Field("proWantWindow")
    @ApiModelProperty(value = "投产意向窗口")
    private String proWantWindow;
    //0,关闭,1开启
    @Field("confirmBtn")
    @ApiModelProperty(value = "上线确认书按钮")
    private String confirmBtn;

    @Field("testKeyNote")
    @ApiModelProperty(value = "提测重点")
    private String testKeyNote;
    /**
     * 任务特殊状态 0：正常任务  1：暂缓中、3：恢复完成
     */
    @Field("taskSpectialStatus")
    private Integer taskSpectialStatus;
    /**
     * 暂缓时间
     */
    @Field("deferTime")
    private String deferTime;
    /**
     * 恢复时间
     */
    @Field("recoverTime")
    private String recoverTime;

    /**
     * 上线确认书实际到达时间
     */
    @Field("confirmFileDate")
    @ApiModelProperty(value = "上线确认书实际到达时间")
    private String confirmFileDate;

    /**
     * fdev研发单元编号
     */
    @Field("fdev_implement_unit_no")
    @Indexed
    private String fdev_implement_unit_no;

    /**
     * jira故事id
     */
    @Field("storyId")
    private String storyId;

    /**
     * jira对应的子任务key
     */
    @Field("jiraKey")
    private String jiraKey;

    /**
     * 代码审核工单编号
     */
    @Field("code_order_no")
    private Set<String> code_order_no;

    public Set<String> getCode_order_no() {
        return code_order_no;
    }

    public void setCode_order_no(Set<String> code_order_no) {
        this.code_order_no = code_order_no;
    }

    public String getUiVerifyReporter() {
        return uiVerifyReporter;
    }

    public void setUiVerifyReporter(String uiVerifyReporter) {
        this.uiVerifyReporter = uiVerifyReporter;
    }

    public String getFdev_implement_unit_no() {
        return fdev_implement_unit_no;
    }

    public void setFdev_implement_unit_no(String fdev_implement_unit_no) {
        this.fdev_implement_unit_no = fdev_implement_unit_no;
    }

    public Integer getTaskSpectialStatus() {
        return taskSpectialStatus;
    }

    public void setTaskSpectialStatus(Integer taskSpectialStatus) {
        this.taskSpectialStatus = taskSpectialStatus;
    }

    public String getDeferTime() {
        return deferTime;
    }

    public void setDeferTime(String deferTime) {
        this.deferTime = deferTime;
    }

    @Field("DesignDoc")
    @ApiModelProperty(value = "提测重点")
    private List<DesignDoc> designDoc;

    public String getRecoverTime() {
        return recoverTime;
    }

    public void setRecoverTime(String recoverTime) {
        this.recoverTime = recoverTime;
    }

    public String getConfirmBtn() {
        return confirmBtn;
    }

    public void setConfirmBtn(String confirmBtn) {
        this.confirmBtn = confirmBtn;
    }

    public String getTestKeyNote() {
        return testKeyNote;
    }

    public void setTestKeyNote(String testKeyNote) {
        this.testKeyNote = testKeyNote;
    }

    public String getProWantWindow() {
        return proWantWindow;
    }

    public void setProWantWindow(String proWantWindow) {
        this.proWantWindow = proWantWindow;
    }

    public String getDesignRemark() {
        return designRemark;
    }

    public void setDesignRemark(String designRemark) {
        this.designRemark = designRemark;
    }
    public Map<String, List<Map<String, String>>> getDesignMap() {
        return designMap;
    }
    public void setDesignMap(Map<String, List<Map<String, String>>> designMap) {
        this.designMap = designMap;
    }





    public String getScanTime() {
        return scanTime;
    }

    public void setScanTime(String scanTime) {
        this.scanTime = scanTime;
    }

    @Field("modify_reason")
    @ApiModelProperty(value = "任务难度修改原因")
    private String modify_reason;

    @Field("simple_demand")
    @ApiModelProperty(value = "是否简单需求任务 0-是（不走内测） 1-否(走内测)")
    private String simpleDemand;

    @Field("skip_flag")
    @ApiModelProperty(value = "按钮标识 1-跳过功能测试按钮 0-提交功能测试按钮")
    private String skipFlag;

    public String getSimpleDemand() {
        return simpleDemand;
    }

    public void setSimpleDemand(String simpleDemand) {
        this.simpleDemand = simpleDemand;
    }

    public String getSkipFlag() {
        return skipFlag;
    }

    public void setSkipFlag(String skipFlag) {
        this.skipFlag = skipFlag;
    }

    public FdevTask() {
    }


    public FdevTask(FdevTaskBuilder build) {
        this.id = build.id;
        this._id = build._id;
        this.name = build.name;
        this.redmine_id = build.redmine_id;
        this.desc = build.desc;
        this.spdb_master = build.spdb_master;
        this.master = build.master;
        this.group = build.group;
        this.project_id = build.project_id;
        this.start_time = build.start_time;
        this.start_inner_test_time = build.start_inner_test_time;
        this.start_uat_test_time = build.start_uat_test_time;
        this.start_rel_test_time = build.start_rel_test_time;
        this.plan_fire_time = build.plan_fire_time;
        this.fire_time = build.fire_time;
        this.stage = build.stage;
        this.feature_branch = build.feature_branch;
        this.review = build.review;
        this.report = build.report;
        this.plan_start_time = build.plan_start_time;
        this.plan_inner_test_time = build.plan_inner_test_time;
        this.uat_merge_time = build.uat_merge_time;
        this.rel_merge_time = build.rel_merge_time;
        this.plan_uat_test_start_time = build.plan_uat_test_start_time;
        this.plan_uat_test_stop_time = build.plan_uat_test_stop_time;
        this.stop_uat_test_time = build.stop_uat_test_time;
        this.plan_rel_test_time = build.plan_rel_test_time;
        this.uat_merge_id = build.uat_merge_id;
        this.sit_merge_id = build.sit_merge_id;
        this.developer = build.developer;
        this.tester = build.tester;
        this.concern = build.concern;
        this.project_name = build.project_name;
        this.creator = build.creator;
        this.uat_testObject = build.uat_testObject;
        this.rqrmnt_no = build.rqrmnt_no;
        this.tag = build.tag;
        this.uat_test_time = build.uat_test_time;
        this.folder_id = build.folder_id;
        this.reviewer = build.reviewer;
        this.review_status = build.review_status;
        this.doc_uploader = build.doc_uploader;
        this.system_remould = build.system_remould;
        this.impl_data = build.impl_data;
        this.reinforce = build.reinforce;
        this.test_merge_id=build.test_merge_id;
        this.sonarId=build.sonarId;
        this.scanTime=build.scanTime;
        this.taskType=build.taskType;
        this.nocodeInfoMap = build.nocodeInfoMap;
        this.fdev_implement_unit_no = build.fdev_implement_unit_no;

    }

    public String getSonarId() {
        return sonarId;
    }

    public void setSonarId(String sonarId) {
        this.sonarId = sonarId;
    }


    public static class FdevTaskBuilder {

        private String id;

        public FdevTaskBuilder id(String id) {
            this.id = id;
            return this;
        }
        private String sonarId;
        private String scanTime;

        public FdevTaskBuilder sonarId(String sonarId) {
            this.sonarId = sonarId;
            return this;
        }
        public FdevTaskBuilder scanTime(String scanTime) {
            this.scanTime = scanTime;
            return this;
        }

        private ObjectId _id;

        public FdevTaskBuilder _id(ObjectId _id) {
            this._id = _id;
            return this;
        }

        private String name;

        public FdevTaskBuilder name(String name) {
            this.name = name;
            return this;
        }

        private String redmine_id;

        public FdevTaskBuilder redmine_id(String redmine_id) {
            this.redmine_id = redmine_id;
            return this;
        }

        private String desc;

        public FdevTaskBuilder desc(String desc) {
            this.desc = desc;
            return this;
        }

        private String[] spdb_master;

        public FdevTaskBuilder spdbMaster(String[] spdb_master) {
            this.spdb_master = spdb_master;
            return this;
        }

        private String[] master;

        public FdevTaskBuilder master(String[] master) {
            this.master = master;
            return this;
        }

        private String group;

        public FdevTaskBuilder group(String group) {
            this.group = group;
            return this;
        }


        private String project_id;

        public FdevTaskBuilder project_id(String project_id) {
            this.project_id = project_id;
            return this;
        }

        private String start_time;

        public FdevTaskBuilder start_time(String start_time) {
            this.start_time = start_time;
            return this;
        }

        private String start_inner_test_time;

        public FdevTaskBuilder start_inner_test_time(String start_inner_test_time) {
            this.start_inner_test_time = start_inner_test_time;
            return this;
        }

        private String start_uat_test_time;

        public FdevTaskBuilder start_uat_test_time(String start_uat_test_time) {
            this.start_uat_test_time = start_uat_test_time;
            return this;
        }

        private String start_rel_test_time;

        public FdevTaskBuilder start_rel_test_time(String start_rel_test_time) {
            this.start_rel_test_time = start_rel_test_time;
            return this;
        }

        private String plan_fire_time;

        public FdevTaskBuilder plan_fire_time(String plan_fire_time) {
            this.plan_fire_time = plan_fire_time;
            return this;
        }

        private String fire_time;

        public FdevTaskBuilder fire_time(String fire_time) {
            this.fire_time = fire_time;
            return this;
        }

        private String stage;

        public FdevTaskBuilder stage(String stage) {
            this.stage = stage;
            return this;
        }

        private String feature_branch;

        public FdevTaskBuilder feature_branch(String feature_branch) {
            this.feature_branch = feature_branch;
            return this;
        }


        private TaskReview review;

        public FdevTaskBuilder review(TaskReview review) {
            this.review = review;
            return this;
        }

        private TaskReport report;

        public FdevTaskBuilder report(TaskReport report) {
            this.report = report;
            return this;
        }

        private String plan_start_time;

        public FdevTaskBuilder plan_start_time(String plan_start_time) {
            this.plan_start_time = plan_start_time;
            return this;
        }

        private String plan_inner_test_time;

        public FdevTaskBuilder plan_inner_test_time(String plan_inner_test_time) {
            this.plan_inner_test_time = plan_inner_test_time;
            return this;
        }

        private String plan_rel_test_time;

        public FdevTaskBuilder plan_rel_test_time(String plan_rel_test_time) {
            this.plan_rel_test_time = plan_rel_test_time;
            return this;
        }

        private String sit_merge_id;

        public FdevTaskBuilder sit_merge_id(String sit_merge_id) {
            this.sit_merge_id = sit_merge_id;
            return this;
        }

        private String uat_merge_id;

        public FdevTaskBuilder uat_merge_id(String uat_merge_id) {
            this.uat_merge_id = uat_merge_id;
            return this;
        }

        private String rel_merge_time;

        public FdevTaskBuilder rel_merge_time(String rel_merge_time) {
            this.rel_merge_time = rel_merge_time;
            return this;
        }

        private String plan_uat_test_start_time;

        public FdevTaskBuilder plan_uat_test_start_time(String plan_uat_test_start_time) {
            this.plan_uat_test_start_time = plan_uat_test_start_time;
            return this;
        }

        private String uat_merge_time;

        public FdevTaskBuilder uat_merge_time(String uat_merge_time) {
            this.uat_merge_time = uat_merge_time;
            return this;
        }

        private String plan_uat_test_stop_time;

        public FdevTaskBuilder plan_uat_test_stop_time(String plan_uat_test_stop_time) {
            this.plan_uat_test_stop_time = plan_uat_test_stop_time;
            return this;
        }

        private String stop_uat_test_time;

        public FdevTaskBuilder stop_uat_test_time(String stop_uat_test_time) {
            this.stop_uat_test_time = stop_uat_test_time;
            return this;
        }

        private String[] developer;

        public FdevTaskBuilder developer(String[] developer) {
            this.developer = developer;
            return this;
        }

        private String[] tester;

        public FdevTaskBuilder tester(String[] tester) {
            this.tester = tester;
            return this;
        }

        private String[] concern;

        public FdevTaskBuilder concern(String[] concern) {
            this.concern = concern;
            return this;
        }

        private String project_name;

        public FdevTaskBuilder project_name(String project_name) {
            this.project_name = project_name;
            return this;
        }

        private String creator;

        public FdevTaskBuilder creator(String creator) {
            this.creator = creator;
            return this;
        }

        private String uat_testObject;

        public FdevTaskBuilder uat_testObject(String uat_testObject) {
            this.uat_testObject = uat_testObject;
            return this;
        }

        private String rqrmnt_no;

        public FdevTaskBuilder rqrmnt_no(String rqrmnt_no) {
            this.rqrmnt_no = rqrmnt_no;
            return this;
        }

        private String[] tag;

        public FdevTaskBuilder tag(String[] tag) {
            this.tag = tag;
            return this;
        }


        private String uat_test_time;

        public FdevTaskBuilder uat_test_time(String uat_test_time) {
            this.uat_test_time = uat_test_time;
            return this;
        }

        private String folder_id;

        public FdevTaskBuilder folder_id(String folder_id) {
            this.folder_id = folder_id;
            return this;
        }

        private String reviewer;

        public FdevTaskBuilder reviewer(String reviewer) {
            this.reviewer = reviewer;
            return this;
        }

        private String review_status;

        public FdevTaskBuilder review_status(String review_status) {
            this.review_status = review_status;
            return this;
        }

        private Map<String, List<String>> doc_uploader;

        public FdevTaskBuilder doc_uploader(Map<String, List<String>> doc_uploader) {
            this.doc_uploader = doc_uploader;
            return this;
        }

        private String system_remould;

        public FdevTaskBuilder system_remould(String system_remould) {
            this.system_remould = system_remould;
            return this;
        }

        private String system_remould_notify;

        public FdevTaskBuilder system_remould_notify(String system_remould_notify) {
            this.system_remould_notify = system_remould_notify;
            return this;
        }

        private String impl_data;

        public FdevTaskBuilder impl_data(String impl_data) {
            this.impl_data = impl_data;
            return this;
        }

        private String db_rele_app;

        public FdevTaskBuilder db_rele_app(String db_rele_app) {
            this.db_rele_app = db_rele_app;
            return this;
        }



        private String reinforce;

        public FdevTaskBuilder reinforce(String reinforce) {
            this.reinforce = reinforce;
            return this;
        }

        private String test_merge_id;

        public FdevTaskBuilder test_merge_id(String test_merge_id) {
            this.test_merge_id = test_merge_id;
            return this;
        }

        private Integer taskType;

        public FdevTaskBuilder taskType(Integer taskType) {
            this.taskType = taskType;
            return this;
        }

        private Map<String,NoCodeInfo>  nocodeInfoMap;

        public FdevTaskBuilder nocodeInfoMap(Map<String,NoCodeInfo>  nocodeInfoMa) {
            this.nocodeInfoMap = nocodeInfoMap;
            return this;
        }

        private String fdev_implement_unit_no;

        public FdevTaskBuilder unitNo(String fdev_implement_unit_no) {
            this.fdev_implement_unit_no = fdev_implement_unit_no;
            return this;
        }

        public FdevTask init() {
            return new FdevTask(this);
        }
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String[] getMaster() {
        return master;
    }

    public void setMaster(String[] master) {
        this.master = master;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }


    public String getRedmine_id() {
        return redmine_id;
    }


    public void setRedmine_id(String redmine_id) {
        this.redmine_id = redmine_id;
    }


    public String[] getSpdb_master() {
        return spdb_master;
    }


    public void setSpdb_master(String[] spdb_master) {
        this.spdb_master = spdb_master;
    }


    public String getProject_id() {
        return project_id;
    }


    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getProject_name() {
        return project_name;
    }


    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }


    public String getPlan_start_time() {
        return plan_start_time;
    }


    public void setPlan_start_time(String plan_start_time) {
        this.plan_start_time = plan_start_time;
    }


    public String getStart_time() {
        return start_time;
    }


    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }


    public String getPlan_inner_test_time() {
        return plan_inner_test_time;
    }


    public void setPlan_inner_test_time(String plan_inner_test_time) {
        this.plan_inner_test_time = plan_inner_test_time;
    }


    public String getStart_inner_test_time() {
        return start_inner_test_time;
    }


    public void setStart_inner_test_time(String start_inner_test_time) {
        this.start_inner_test_time = start_inner_test_time;
    }

    public String getUat_merge_time() {
        return uat_merge_time;
    }

    public void setUat_merge_time(String uat_merge_time) {
        this.uat_merge_time = uat_merge_time;
    }

    public String getRel_merge_time() {
        return rel_merge_time;
    }

    public void setRel_merge_time(String rel_merge_time) {
        this.rel_merge_time = rel_merge_time;
    }

    public String getPlan_uat_test_start_time() {
        return plan_uat_test_start_time;
    }


    public void setPlan_uat_test_start_time(String plan_uat_test_start_time) {
        this.plan_uat_test_start_time = plan_uat_test_start_time;
    }


    public String getPlan_uat_test_stop_time() {
        return plan_uat_test_stop_time;
    }


    public void setPlan_uat_test_stop_time(String plan_uat_test_stop_time) {
        this.plan_uat_test_stop_time = plan_uat_test_stop_time;
    }


    public String getStart_uat_test_time() {
        return start_uat_test_time;
    }


    public void setStart_uat_test_time(String start_uat_test_time) {
        this.start_uat_test_time = start_uat_test_time;
    }


    public String getStop_uat_test_time() {
        return stop_uat_test_time;
    }


    public void setStop_uat_test_time(String stop_uat_test_time) {
        this.stop_uat_test_time = stop_uat_test_time;
    }


    public String getPlan_rel_test_time() {
        return plan_rel_test_time;
    }


    public void setPlan_rel_test_time(String plan_rel_test_time) {
        this.plan_rel_test_time = plan_rel_test_time;
    }


    public String getStart_rel_test_time() {
        return start_rel_test_time;
    }


    public void setStart_rel_test_time(String start_rel_test_time) {
        this.start_rel_test_time = start_rel_test_time;
    }


    public String getPlan_fire_time() {
        return plan_fire_time;
    }


    public void setPlan_fire_time(String plan_fire_time) {
        this.plan_fire_time = plan_fire_time;
    }


    public String getFire_time() {
        return fire_time;
    }


    public void setFire_time(String fire_time) {
        this.fire_time = fire_time;
    }


    public String getStage() {
        return stage;
    }


    public void setStage(String stage) {
        this.stage = stage;
    }


    public String getFeature_branch() {
        return feature_branch;
    }


    public void setFeature_branch(String feature_branch) {
        this.feature_branch = feature_branch;
    }


    public String getSit_merge_id() {
        return sit_merge_id;
    }


    public void setSit_merge_id(String sit_merge_id) {
        this.sit_merge_id = sit_merge_id;
    }


    public String getUat_merge_id() {
        return uat_merge_id;
    }


    public void setUat_merge_id(String uat_merge_id) {
        this.uat_merge_id = uat_merge_id;
    }


    public String[] getDeveloper() {
        return developer;
    }


    public void setDeveloper(String[] developer) {
        this.developer = developer;
    }


    public String[] getTester() {
        return tester;
    }


    public void setTester(String[] tester) {
        this.tester = tester;
    }


    public String[] getDoc() {
        return doc;
    }


    public void setDoc(String[] doc) {
        this.doc = doc;
    }


    public String[] getConcern() {
        return concern;
    }


    public void setConcern(String[] concern) {
        this.concern = concern;
    }


    public TaskReview getReview() {
        return review;
    }


    public void setReview(TaskReview review) {
        this.review = review;
    }


    public TaskReport getReport() {
        return report;
    }


    public void setReport(TaskReport report) {
        this.report = report;
    }


    public String getCreator() {
        return creator;
    }


    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getUat_testObject() {
        return uat_testObject;
    }

    public void setUat_testObject(String uat_testObject) {
        this.uat_testObject = uat_testObject;
    }

    public String getRqrmnt_no() {
        return rqrmnt_no;
    }

    public void setRqrmnt_no(String rqrmnt_no) {
        this.rqrmnt_no = rqrmnt_no;
    }

    public String[] getTag() {
        return tag;
    }

    public void setTag(String[] tag) {
        this.tag = tag;
    }

    public void setUat_test_time(String uat_test_time) {
        this.uat_test_time = uat_test_time;
    }

    public String getUat_test_time() {
        return uat_test_time;
    }

    public String getFolder_id() {
        return folder_id;
    }

    public void setFolder_id(String folder_id) {
        this.folder_id = folder_id;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getReview_status() {
        return review_status;
    }

    public void setReview_status(String review_status) {
        this.review_status = review_status;
    }

    public Map<String, List<String>> getDoc_uploader() {
        return doc_uploader;
    }

    public void setDoc_uploader(Map<String, List<String>> doc_uploader) {
        this.doc_uploader = doc_uploader;
    }

    public String getSystem_remould() {
        return system_remould;
    }

    public void setSystem_remould(String system_remould) {
        this.system_remould = system_remould;
    }

    public String getImpl_data() {
        return impl_data;
    }

    public void setImpl_data(String impl_data) {
        this.impl_data = impl_data;
    }

    public String getReinforce() {
        return reinforce;
    }

    public void setReinforce(String reinforce) {
        this.reinforce = reinforce;
    }

    public String getTest_merge_id() {
        return test_merge_id;
    }

    public void setTest_merge_id(String test_merge_id) {
        this.test_merge_id = test_merge_id;
    }

    public String getDifficulty_desc() {
        return difficulty_desc;
    }

    public void setDifficulty_desc(String difficulty_desc) {
        this.difficulty_desc = difficulty_desc;
    }

    public String getModify_reason() {
        return modify_reason;
    }

    public void setModify_reason(String modify_reason) {
        this.modify_reason = modify_reason;
    }

    public List<DesignDoc> getDesignDoc() {
        return designDoc;
    }

    public void setDesignDoc(List<DesignDoc> designDoc) {
        this.designDoc = designDoc;
    }

    public String getConfirmFileDate() {
        return confirmFileDate;
    }

    public void setConfirmFileDate(String confirmFileDate) {
        this.confirmFileDate = confirmFileDate;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getJiraKey() {
        return jiraKey;
    }

    public void setJiraKey(String jiraKey) {
        this.jiraKey = jiraKey;
    }

    public String getOld_stage() {
        return old_stage;
    }

    public void setOld_stage(String old_stage) {
        this.old_stage = old_stage;
    }

    public String getDemandNo() {
        return demandNo;
    }

    public void setDemandNo(String demandNo) {
        this.demandNo = demandNo;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(String versionNum) {
        this.versionNum = versionNum;
    }

}
