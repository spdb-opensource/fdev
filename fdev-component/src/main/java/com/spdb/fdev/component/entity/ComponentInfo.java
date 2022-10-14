package com.spdb.fdev.component.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "component_info")
public class ComponentInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3531171824936784020L;

    @Id
    @JsonIgnore
    private ObjectId _id;
    @Field("id")
    private String id;
    @Field("name_en")
    private String name_en;
    @Field("name_cn")
    private String name_cn;
    @Field("desc")
    private String desc;
    @Field("gitlab_url")
    private String gitlab_url;
    @Field("gitlab_id")
    private String gitlab_id;
    @Field("groupId")
    private String groupId;
    @Field("artifactId")
    private String artifactId;
    @Field("parentId")
    private String parentId;
    @Field("manager_id")
    private HashSet<Map<String, String>> manager_id;
    @Field("type")
    private String type;
    @Field("root_dir")
    private String root_dir;
    @Field("jdk_version")
    private String jdk_version;
    @Field("wiki_url")
    private String wiki_url;
    @Field("source")
    private String source;
    @Field("group")
    private String group;

    @Field("isTest")
    private String isTest;            //"0"标识不涉及内测，"1"标识涉及内测

    @Field("sonar_scan_switch")
    private String sonar_scan_switch;

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public HashSet<Map<String, String>> getManager_id() {
        return manager_id;
    }

    public void setManager_id(HashSet<Map<String, String>> manager_id) {
        this.manager_id = manager_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoot_dir() {
        return root_dir;
    }

    public void setRoot_dir(String root_dir) {
        this.root_dir = root_dir;
    }

    public String getJdk_version() {
        return jdk_version;
    }

    public void setJdk_version(String jdk_version) {
        this.jdk_version = jdk_version;
    }

    public String getWiki_url() {
        return wiki_url;
    }

    public void setWiki_url(String wiki_url) {
        this.wiki_url = wiki_url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup_id(String group_id) {
        this.group = group;
    }

    public String getIsTest() {
        return isTest;
    }

    public void setIsTest(String isTest) {
        this.isTest = isTest;
    }

    public String getSonar_scan_switch() {
        return sonar_scan_switch;
    }

    public void setSonar_scan_switch(String sonar_scan_switch) {
        this.sonar_scan_switch = sonar_scan_switch;
    }
}
