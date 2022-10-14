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
@Document(collection = "archetype_info")
public class ArchetypeInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1130629400954138973L;
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
    @Field("manager_id")
    private HashSet<Map<String, String>> manager_id;
    @Field("group")
    private String group;
    @Field("type")
    private String type;
    @Field("wiki_url")
    private String wiki_url;
    @Field("encoding")
    private String encoding;
    @Field("application_path")
    private String application_path;



    @Field("isTest")
    private String isTest;            //"0"标识不涉及内测，"1"标识涉及内测

    @Field("sonar_scan_switch")
    private String sonar_scan_switch;
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

    public HashSet<Map<String, String>> getManager_id() {
        return manager_id;
    }

    public void setManager_id(HashSet<Map<String, String>> manager_id) {
        this.manager_id = manager_id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWiki_url() {
        return wiki_url;
    }

    public void setWiki_url(String wiki_url) {
        this.wiki_url = wiki_url;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getApplication_path() {
        return application_path;
    }

    public void setApplication_path(String application_path) {
        this.application_path = application_path;
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

    public String getSonar_scan_switch() {
        return sonar_scan_switch;
    }

    public void setSonar_scan_switch(String sonar_scan_switch) {
        this.sonar_scan_switch = sonar_scan_switch;
    }
}
