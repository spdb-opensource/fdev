package com.spdb.fdev.component.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "baseimage_info")
public class BaseImageInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1594874720326088867L;

    @Id
    @JsonIgnore
    private ObjectId _id;
    @Field("id")
    private String id;
    /**
     * 基础镜像名称（英文镜像名称）
     */
    @Indexed(unique = true)
    @Field("name")
    private String name;

    /**
     * 基础镜像名称（中文镜像名称）
     */
    @Field("name_cn")
    private String name_cn;

    /**
     * 描述信息
     */
    @Field("description")
    private String description;
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
     * 管理员
     */
    @Field("manager")
    private HashSet<Map<String, String>> manager;
    /**
     * 类型
     */
    @Field("type")
    private String type;
    /**
     * 目标环境
     */
    @Field("target_env")
    private String target_env;
    /**
     * wiki地址
     */
    @Field("wiki")
    private String wiki;

    /**
     * 重要元数据(Map对象)
     */
    @Field("meta_data_declare")
    private HashMap<String, String> meta_data_declare;


    @Field("isTest")
    private String isTest;            //"0"标识不涉及内测，"1"标识涉及内测


    /**
     * 创建时间
     */
    @Field("create_date")
    private String create_date;

    @Field("group")
    private String group;


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

    public String getName_cn() {
        return name_cn;
    }

    public void setName_cn(String name_cn) {
        this.name_cn = name_cn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public HashSet<Map<String, String>> getManager() {
        return manager;
    }

    public void setManager(HashSet<Map<String, String>> manager) {
        this.manager = manager;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget_env() {
        return target_env;
    }

    public void setTarget_env(String target_env) {
        this.target_env = target_env;
    }

    public String getWiki() {
        return wiki;
    }

    public void setWiki(String wiki) {
        this.wiki = wiki;
    }

    public HashMap<String, String> getMeta_data_declare() {
        return meta_data_declare;
    }

    public void setMeta_data_declare(HashMap<String, String> meta_data_declare) {
        this.meta_data_declare = meta_data_declare;
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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }


}
