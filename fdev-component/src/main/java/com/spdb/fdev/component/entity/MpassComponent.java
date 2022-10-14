package com.spdb.fdev.component.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Map;

@Component
@Document(collection = "mpass_component")
public class MpassComponent {

    @Id
    @JsonIgnore
    private ObjectId _id;
    @Field("id")
    private String id;
    /**
     * 组件英文名
     */
    @Field("name_en")
    private String name_en;
    /**
     * 组件中文名
     */
    @Field("name_cn")
    private String name_cn;
    /**
     * 组件管理员
     */
    @Field("manager")
    private HashSet<Map<String, String>> manager;
    /**
     * npm坐标name
     */
    @Field("npm_name")
    private String npm_name;
    /**
     * 技术栈
     */
    @Field("skillstack")
    private String skillstack;
    /**
     * 业务领域
     */
    @Field("businessarea")
    private String businessarea;
    /**
     * npm坐标group
     */
    @Field("npm_group")
    private String npm_group;
    /**
     * 组件来源，0：组内维护(自研) 1：第三方(开源)
     */
    @Field("source")
    private String source;
    /**
     * 组件类型，0：业务组件 1：服务组件 2: UI组件 3: 金融组件 4: 插件
     */
    @Field("type")
    private String type;
    /**
     * group 所属小组
     */
    @Field("group")
    private String group;

    /**
     * gitlab地址
     */
    @Field("gitlab_url")
    private String gitlab_url;
    /**
     * gitlab项目id
     */
    @Field("gitlab_id")
    private String gitlab_id;
    /**
     * 组件根路径
     */
    @Field("root_dir")
    private String root_dir;
    /**
     * 组件描述
     */
    @Field("desc")
    private String desc;


    @Field("isTest")
    private String isTest;            //"0"标识不涉及内测，"1"标识涉及内测

    /**
     * 创建时间
     */
    @Field("create_date")
    private String create_date;

    @Field("component_type")
    private String component_type;


    public String getComponent_type() {
        return component_type;
    }

    public void setComponent_type(String component_type) {
        this.component_type = component_type;
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

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
    }

    public String getName_cn() {
        return name_cn;
    }

    public void setName_cn(String name_cn) {
        this.name_cn = name_cn;
    }

    public HashSet<Map<String, String>> getManager() {
        return manager;
    }

    public void setManager(HashSet<Map<String, String>> manager) {
        this.manager = manager;
    }

    public String getNpm_name() {
        return npm_name;
    }

    public void setNpm_name(String npm_name) {
        this.npm_name = npm_name;
    }

    public String getNpm_group() {
        return npm_group;
    }

    public void setNpm_group(String npm_group) {
        this.npm_group = npm_group;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGitlab_url() {
        return gitlab_url;
    }

    public void setGitlab_url(String gitlab_url) {
        this.gitlab_url = gitlab_url;
    }

    public String getGitlab_id() {
        return gitlab_id;
    }

    public void setGitlab_id(String gitlab_id) {
        this.gitlab_id = gitlab_id;
    }

    public String getRoot_dir() {
        return root_dir;
    }

    public void setRoot_dir(String root_dir) {
        this.root_dir = root_dir;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getSkillstack() {
        return skillstack;
    }

    public void setSkillstack(String skillstack) {
        this.skillstack = skillstack;
    }

    public String getBusinessarea() {
        return businessarea;
    }

    public void setBusinessarea(String businessarea) {
        this.businessarea = businessarea;
    }

    public String getIsTest() {
        return isTest;
    }

    public void setIsTest(String isTest) {
        this.isTest = isTest;
    }

}
