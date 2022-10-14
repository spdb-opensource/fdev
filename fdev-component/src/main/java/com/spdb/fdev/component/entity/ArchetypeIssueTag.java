package com.spdb.fdev.component.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

/**
 * mpass骨架打包后，存入一个当前的tag作为备份
 */
@Component
@Document(collection = "archetype_issue_tag")
public class ArchetypeIssueTag {
    @Id
    @JsonIgnore
    private ObjectId _id;
    @Field("id")
    private String id;
    /**
     * 关联骨架id
     */
    @Field("archetype_id")
    private String archetype_id;
    /**
     * 关联优化需求id
     */
    @Field("issue_id")
    private String issue_id;

    /**
     * tag
     */
    @Field("tag")
    private String tag;

    /**
     * 更新人员
     */
    @Field("update_user")
    private String update_user;

    /**
     * 发布日志
     */
    @Field("release_log")
    private String release_log;

    /**
     * 创建时间
     */
    @Field("date")
    private String date;

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

    public String getArchetype_id() {
        return archetype_id;
    }

    public void setArchetype_id(String archetype_id) {
        this.archetype_id = archetype_id;
    }

    public String getIssue_id() {
        return issue_id;
    }

    public void setIssue_id(String issue_id) {
        this.issue_id = issue_id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUpdate_user() {
        return update_user;
    }

    public void setUpdate_user(String update_user) {
        this.update_user = update_user;
    }

    public String getRelease_log() {
        return release_log;
    }

    public void setRelease_log(String release_log) {
        this.release_log = release_log;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
