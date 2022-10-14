package com.spdb.fdev.component.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

@Component
@Document(collection = "baseimage_record")
@CompoundIndexes({
        @CompoundIndex(name = "baseimage_record_namex", def = "{'name': 1, 'image_tag': 1}", unique = true)
})
public class BaseImageRecord implements Serializable {

    @Id
    @JsonIgnore
    private ObjectId _id;
    @Field("id")
    private String id;
    /**
     * 基础镜像名称（英文镜像名称）
     */
    @Field("name")
    private String name;
    /**
     * 分支名称
     */
    @Field("branch")
    private String branch;
    /**
     * 镜像标签，即版本号
     */
    @Field("image_tag")
    private String image_tag;
    /**
     * 阶段，dev-开发测试、trial-试用期、release-正式推广、invalid-已失效
     */
    @Field("stage")
    private String stage;
    /**
     * 试用应用列表
     */
    @Field("trial_apps")
    private HashSet<String> trial_apps;
    /**
     * 更新人员姓名
     */
    @Field("update_user")
    private String update_user;
    /**
     * 更新时间
     */
    @Field("update_time")
    private String update_time;
    /**
     * 更新日志
     */
    @Field("release_log")
    private String release_log;
    /**
     * 重要元数据(Map对象)
     */
    @Field("meta_data")
    private HashMap<String, String> meta_data;

    /**
     * 持续集成状态
     */
    @Field("packagetype")
    private String packagetype;

    public BaseImageRecord() {

    }

    public BaseImageRecord(String name, String branch, String image_tag, String stage, HashSet<String> trial_apps, String release_log, String update_user, String update_time, String packagetype, HashMap<String, String> meta_data) {
        this.name = name;
        this.branch = branch;
        this.image_tag = image_tag;
        this.stage = stage;
        this.trial_apps = trial_apps;
        this.release_log = release_log;
        this.update_user = update_user;
        this.update_time = update_time;
        this.packagetype = packagetype;
        this.meta_data = meta_data;
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

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getImage_tag() {
        return image_tag;
    }

    public void setImage_tag(String image_tag) {
        this.image_tag = image_tag;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public HashSet<String> getTrial_apps() {
        return trial_apps;
    }

    public void setTrial_apps(HashSet<String> trial_apps) {
        this.trial_apps = trial_apps;
    }

    public String getUpdate_user() {
        return update_user;
    }

    public void setUpdate_user(String update_user) {
        this.update_user = update_user;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getRelease_log() {
        return release_log;
    }

    public void setRelease_log(String release_log) {
        this.release_log = release_log;
    }

    public HashMap<String, String> getMeta_data() {
        return meta_data;
    }

    public void setMeta_data(HashMap<String, String> meta_data) {
        this.meta_data = meta_data;
    }

    public String getPackagetype() {
        return packagetype;
    }

    public void setPackagetype(String packagetype) {
        this.packagetype = packagetype;
    }
}
