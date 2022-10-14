package com.spdb.fdev.component.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;

@Component
@Document(collection = "mpass_archetype")
public class MpassArchetype {

    @Id
    @JsonIgnore
    private ObjectId _id;
    @Field("id")
    private String id;
    /**
     * 骨架英文名
     */
    @Field("name_en")
    private String name_en;
    /**
     * 骨架中文名
     */
    @Field("name_cn")
    private String name_cn;
    /**
     * 骨架管理员
     */
    @Field("manager")
    private HashSet<Map<String, String>> manager;

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
     * 骨架描述
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


    @Field("archetype_type")
    private String archetype_type;

    public String getArchetype_type() {
        return archetype_type;
    }

    public void setArchetype_type(String archetype_type) {
        this.archetype_type = archetype_type;
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

    public String getIsTest() {
        return isTest;
    }

    public void setIsTest(String isTest) {
        this.isTest = isTest;
    }

}
