package com.spdb.fdev.fdevapp.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Document(collection = "app-entity")
@ApiModel(value = "应用实体")
public class AppEntity implements Serializable {

    private static final long serialVersionUID = 2940763111462288021L;
    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;
    @Field("id")
    @ApiModelProperty(value = "id")
    private String id;
    @Field("gitlab_project_id")
    @ApiModelProperty(value = "gitlab项目ID")
    private Integer gitlab_project_id;
    @Field("name_en")
    @ApiModelProperty(value = "应用英文名称")
    private String name_en;
    @Field("name_zh")
    @ApiModelProperty(value = "应用中文名称")
    private String name_zh;
    @Field("desc")
    @ApiModelProperty(value = "应用描述")
    private String desc;
    @Field("group")
    @ApiModelProperty(value = "应用所属组")
    private String group;
    @Field("system")
    @ApiModelProperty(value = "应用所属系统")
    private String system;
    @Field("service_system")
    @ApiModelProperty(value = "应用所属业务域")
    private String service_system;
    @Field("domain")
    @ApiModelProperty(value = "应用所属域")
    private String domain;
    @Field("type_id")
    @ApiModelProperty(value = "应用类型ID")
    private String type_id;
    @Field("type_name")
    @ApiModelProperty(value = "应用类型名称")
    private String type_name;
    @Field("git")
    @ApiModelProperty(value = "应用git地址")
    private String git;
    @Field("status")
    @ApiModelProperty(value = "应用状态")
    private String status;
    @Field("createtime")
    @ApiModelProperty(value = "应用创建时间")
    private String createtime;

    @Field("spdb_managers")
    @ApiModelProperty(value = "行内负责人")
    private Set<Map<String, String>> spdb_managers;
    @Field("dev_managers")
    @ApiModelProperty(value = "厂商负责人")
    private Set<Map<String, String>> dev_managers;
    @Field("tasks_id")
    @ApiModelProperty(value = "任务信息ID")
    private HashSet<String> tasks_id;

    @Field("archetype_id")
    @ApiModelProperty(value = "应用骨架信息")
    private String archetype_id;

    @Field("gitlabci_id")
    @ApiModelProperty(value = "持续集成模板信息")
    private String gitlabci_id;

    @Field("sit")
    @ApiModelProperty(value = "SIT分支信息")
    private HashSet<EnvBranch> sit;

    @Field("network")
    @ApiModelProperty(value = "部署网段")
    private String network;

    @Field("archetype_version")
    @ApiModelProperty(value = "应用骨架版本")
    private String archetype_version;
    @Field("def_uat")
    @ApiModelProperty(value = "默认uat环境")
    private String def_uat;

    @Field("def_rel")
    @ApiModelProperty(value = "默认rel环境")
    private String def_rel;

    @Field("pipeline_schedule_switch")
    @ApiModelProperty(value = "自动部署定时任务开关")
    private String pipeline_schedule_switch;// "0"表示关闭自动部署任务 "1"开启自动部署任务

    @Field("label")
    @ApiModelProperty(value = "应用标签")
    private HashSet<String> label;

    @Field("new_add_sign")
    @ApiModelProperty(value = "新增应用标识")
    private String new_add_sign;          // "0"表示是新增应用，"1"表示非新增应用，"2"表示录入的已有应用

    @Field("sonar_scan_switch")
    @ApiModelProperty(value = "sonar扫描开关")
    private String sonar_scan_switch;

    @Transient
    @ApiModelProperty(value = "文件编码")
    private String fileEncoding;

    @Field("isTest")
    @ApiModelProperty(value = "是否涉及内测")
    private String isTest;                  //"0"标识不涉及内测，"1"标识涉及内测

    @Transient
    @ApiModelProperty(value = "是否是互联网条线")
    private String isInternetSystem;                //1为代表是要校验三段式，0为代表不需要校验三段式

    @Field("appCiType")
    @ApiModelProperty(value = "应用持续集成类型")
    private String appCiType;           //应用持续集成类型：fdev-ci/gitlab-ci 


    @Field("caas_status")
    @ApiModelProperty(value = "CaaS平台是否被使用")  //“1”-"被使用"， “0”-不被使用
    private String caas_status;


    @Field("scc_status")
    @ApiModelProperty(value = "SCC平台是否被使用")  //“1”-"被使用"， “0”-不被使用
    private String scc_status;

    public AppEntity() {
    }

    public AppEntity(String id, Integer gitlab_project_id, String name_en, String name_zh, String desc, String group, String system, String service_system, String domain, String type_id, String type_name, String git, String status, String createtime, Set<Map<String, String>> spdb_managers, Set<Map<String, String>> dev_managers, HashSet<String> tasks_id, String archetype_id, String gitlabci_id, HashSet<EnvBranch> sit, String network, String archetype_version, String def_uat, String def_rel, String pipeline_schedule_switch, HashSet<String> label, String new_add_sign, String sonar_scan_switch, String fileEncoding, String isTest, String isInternetSystem, String appCiType) {
        this.id = id;
        this.gitlab_project_id = gitlab_project_id;
        this.name_en = name_en;
        this.name_zh = name_zh;
        this.desc = desc;
        this.group = group;
        this.system = system;
        this.service_system = service_system;
        this.domain = domain;
        this.type_id = type_id;
        this.type_name = type_name;
        this.git = git;
        this.status = status;
        this.createtime = createtime;
        this.spdb_managers = spdb_managers;
        this.dev_managers = dev_managers;
        this.tasks_id = tasks_id;
        this.archetype_id = archetype_id;
        this.gitlabci_id = gitlabci_id;
        this.sit = sit;
        this.network = network;
        this.archetype_version = archetype_version;
        this.def_uat = def_uat;
        this.def_rel = def_rel;
        this.pipeline_schedule_switch = pipeline_schedule_switch;
        this.label = label;
        this.new_add_sign = new_add_sign;
        this.sonar_scan_switch = sonar_scan_switch;
        this.fileEncoding = fileEncoding;
        this.isTest = isTest;
        this.isInternetSystem = isInternetSystem;
        this.appCiType = appCiType;
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

    public Integer getGitlab_project_id() {
        return gitlab_project_id;
    }

    public void setGitlab_project_id(Integer gitlab_project_id) {
        this.gitlab_project_id = gitlab_project_id;
    }

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
    }

    public String getName_zh() {
        return name_zh;
    }

    public void setName_zh(String name_zh) {
        this.name_zh = name_zh;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }

    public String getType_name() {
        return type_name;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

    public String getGit() {
        return git;
    }

    public void setGit(String git) {
        this.git = git;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }


    public HashSet<String> getTasks_id() {
        return tasks_id;
    }

    public void setTasks_id(HashSet<String> tasks_id) {
        this.tasks_id = tasks_id;
    }

    public String getArchetype_id() {
        return archetype_id;
    }

    public void setArchetype_id(String archetype_id) {
        this.archetype_id = archetype_id;
    }

    public String getGitlabci_id() {
        return gitlabci_id;
    }

    public void setGitlabci_id(String gitlabci_id) {
        this.gitlabci_id = gitlabci_id;
    }

    public HashSet<EnvBranch> getSit() {
        return sit;
    }

    public void setSit(HashSet<EnvBranch> sit) {
        this.sit = sit;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getArchetype_version() {
        return archetype_version;
    }

    public void setArchetype_version(String archetype_version) {
        this.archetype_version = archetype_version;
    }

    public String getDef_uat() {
        return def_uat;
    }

    public void setDef_uat(String def_uat) {
        this.def_uat = def_uat;
    }

    public String getDef_rel() {
        return def_rel;
    }

    public void setDef_rel(String def_rel) {
        this.def_rel = def_rel;
    }

    public String getPipeline_schedule_switch() {
        return pipeline_schedule_switch;
    }

    public void setPipeline_schedule_switch(String pipeline_schedule_switch) {
        this.pipeline_schedule_switch = pipeline_schedule_switch;
    }

    public String getNew_add_sign() {
        return new_add_sign;
    }

    public void setNew_add_sign(String new_add_sign) {
        this.new_add_sign = new_add_sign;
    }

    public HashSet<String> getLabel() {
        return label;
    }

    public void setLabel(HashSet<String> label) {
        this.label = label;
    }

    public String getSonar_scan_switch() {
        return sonar_scan_switch;
    }

    public void setSonar_scan_switch(String sonar_scan_switch) {
        this.sonar_scan_switch = sonar_scan_switch;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getFileEncoding() {
        return fileEncoding;
    }

    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    public String getService_system() {
        return service_system;
    }

    public void setService_system(String service_system) {
        this.service_system = service_system;
    }

    public String getIsInternetSystem() {
        return isInternetSystem;
    }

    public void setIsInternetSystem(String isInternetSystem) {
        this.isInternetSystem = isInternetSystem;
    }

    public String getIsTest() {
        return isTest;
    }

    public void setIsTest(String isTest) {
        this.isTest = isTest;
    }

    public String getAppCiType() {
        return appCiType;
    }

    public void setAppCiType(String appCiType) {
        this.appCiType = appCiType;
    }

    public String getCaas_status() { return caas_status; }

    public void setCaas_status(String caas_status) { this.caas_status = caas_status; }

    public String getScc_status() { return scc_status; }

    public void setScc_status(String scc_status) { this.scc_status = scc_status; }

    public Set<Map<String, String>> getSpdb_managers() {
        return spdb_managers;
    }

    public void setSpdb_managers(Set<Map<String, String>> spdb_managers) {
        this.spdb_managers = spdb_managers;
    }

    public Set<Map<String, String>> getDev_managers() {
        return dev_managers;
    }

    public void setDev_managers(Set<Map<String, String>> dev_managers) {
        this.dev_managers = dev_managers;
    }

    @Override
    public String toString() {
        return "AppEntity{" +
                "_id=" + _id +
                ", id='" + id + '\'' +
                ", gitlab_project_id=" + gitlab_project_id +
                ", name_en='" + name_en + '\'' +
                ", name_zh='" + name_zh + '\'' +
                ", desc='" + desc + '\'' +
                ", group='" + group + '\'' +
                ", system='" + system + '\'' +
                ", service_system='" + service_system + '\'' +
                ", domain='" + domain + '\'' +
                ", type_id='" + type_id + '\'' +
                ", type_name='" + type_name + '\'' +
                ", git='" + git + '\'' +
                ", status='" + status + '\'' +
                ", createtime='" + createtime + '\'' +
                ", spdb_managers=" + spdb_managers +
                ", dev_managers=" + dev_managers +
                ", tasks_id=" + tasks_id +
                ", archetype_id='" + archetype_id + '\'' +
                ", gitlabci_id='" + gitlabci_id + '\'' +
                ", sit=" + sit +
                ", network='" + network + '\'' +
                ", archetype_version='" + archetype_version + '\'' +
                ", def_uat='" + def_uat + '\'' +
                ", def_rel='" + def_rel + '\'' +
                ", pipeline_schedule_switch='" + pipeline_schedule_switch + '\'' +
                ", label=" + label +
                ", new_add_sign='" + new_add_sign + '\'' +
                ", sonar_scan_switch='" + sonar_scan_switch + '\'' +
                ", fileEncoding='" + fileEncoding + '\'' +
                '}';
    }
}
