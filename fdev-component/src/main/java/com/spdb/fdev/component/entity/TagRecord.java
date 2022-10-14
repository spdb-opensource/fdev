package com.spdb.fdev.component.entity;


import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.*;

@Document(collection = "tag_record")
public class TagRecord implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7237120063382059189L;

    @Id
    private ObjectId _id;
    @Field("id")
    private String id;

    @Field("component_id")
    private String component_id;

    @Field("archetype_id")
    private String archetype_id;

    @Field("image_id")
    private String image_id;

    @Field("gitlab_id")
    private String gitlab_id;

    @Field("merge_request_id")
    private String merge_request_id;

    @Field("target_version")
    private String target_version;





    @Field("variables")
    private List<Map<String, String>> variables;

    @Field("branch")
    private String branch;

    @Field("release_log")
    private String release_log;

    @Field("update_user")
    private String update_user;

    @Field("jdk")
    private String jdk;

    @Field("devBranch")
    private String devBranch;

    @Field("name")
    private String name;

    @Field("meta_data")
    private HashMap<String, String> meta_data;

    @Field("trial_apps")
    private HashSet<String> trial_apps;

    @Field("issue_id")
    private String issue_id;


    @Field("release_node_name")
    private String release_node_name;


    public TagRecord(String component_id, String gitlab_id, String merge_request_id, String target_version, List<Map<String, String>> variables, String branch, String release_log, String update_user, String jdk) {
        this.component_id = component_id;
        this.gitlab_id = gitlab_id;
        this.merge_request_id = merge_request_id;
        this.target_version = target_version;
        this.variables = variables;
        this.branch = branch;
        this.release_log = release_log;
        this.update_user = update_user;
        this.jdk = jdk;
    }

    public TagRecord(String archetype_id, String gitlab_id, String merge_request_id, String target_version, List<Map<String, String>> variables, String branch, String release_log, String update_user) {
        this.archetype_id = archetype_id;
        this.gitlab_id = gitlab_id;
        this.merge_request_id = merge_request_id;
        this.target_version = target_version;
        this.variables = variables;
        this.branch = branch;
        this.release_log = release_log;
        this.update_user = update_user;
    }

    public TagRecord(String name, String gitlab_id, String merge_request_id, List<Map<String, String>> variables, String devBranch, String branch, String release_log, HashMap<String, String> meta_data, HashSet<String> trial_apps, String update_user) {
        this.name = name;
        this.gitlab_id = gitlab_id;
        this.merge_request_id = merge_request_id;
        this.variables = variables;
        this.devBranch = devBranch;
        this.branch = branch;
        this.release_log = release_log;
        this.meta_data = meta_data;
        this.trial_apps = trial_apps;
        this.update_user = update_user;
    }

    public TagRecord() {
    }


    public String getRelease_node_name() {
        return release_node_name;
    }

    public void setRelease_node_name(String release_node_name) {
        this.release_node_name = release_node_name;
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

    public String getComponent_id() {
        return component_id;
    }

    public void setComponent_id(String component_id) {
        this.component_id = component_id;
    }

    public String getArchetype_id() {
        return archetype_id;
    }

    public void setArchetype_id(String archetype_id) {
        this.archetype_id = archetype_id;
    }

    public String getGitlab_id() {
        return gitlab_id;
    }

    public void setGitlab_id(String gitlab_id) {
        this.gitlab_id = gitlab_id;
    }

    public String getMerge_request_id() {
        return merge_request_id;
    }

    public void setMerge_request_id(String merge_request_id) {
        this.merge_request_id = merge_request_id;
    }

    public String getTarget_version() {
        return target_version;
    }

    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public void setTarget_version(String target_version) {
        this.target_version = target_version;
    }

    public List<Map<String, String>> getVariables() {
        return variables;
    }

    public void setVariables(List<Map<String, String>> variables) {
        this.variables = variables;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getRelease_log() {
        return release_log;
    }

    public void setRelease_log(String release_log) {
        this.release_log = release_log;
    }

    public String getUpdate_user() {
        return update_user;
    }

    public void setUpdate_user(String update_user) {
        this.update_user = update_user;
    }

    public String getJdk() {
        return jdk;
    }

    public void setJdk(String jdk) {
        this.jdk = jdk;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, String> getMeta_data() {
        return meta_data;
    }

    public void setMeta_data(HashMap<String, String> meta_data) {
        this.meta_data = meta_data;
    }

    public HashSet<String> getTrial_apps() {
        return trial_apps;
    }

    public void setTrial_apps(HashSet<String> trial_apps) {
        this.trial_apps = trial_apps;
    }

    public String getDevBranch() {
        return devBranch;
    }

    public void setDevBranch(String devBranch) {
        this.devBranch = devBranch;
    }

    public String getIssue_id() {
        return issue_id;
    }

    public void setIssue_id(String issue_id) {
        this.issue_id = issue_id;
    }
}
