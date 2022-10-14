package com.spdb.fdev.gitlabwork.entiy;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document("mergedInfo")
public class MergedInfo implements Serializable {

    private static final long serialVersionUID = 3322546145521486233L;

    @Id
    private ObjectId _id;

    @Field("id")
    private String id;

    @Field("appName")
    private String appName;     //应用名

    @Field("appId")
    private String appId;       //fdev应用id

    @Field("creator")
    private Map<String, Object> creator;        //创建人

    @Field("handler")
    private Map<String, Object> handler;        //处理人

    @Field("sourceBranch")
    private String sourceBranch;//源分支

    @Field("targetBranch")
    private String targetBranch;//目标分支

    @Field("createTime")
    private String createTime;  //创建时间

    @Field("handleTime")
    private String handleTime;  //处理时间

    @Field("groupId")
    private String groupId;       //小组

    @Field("conflict_sha")
    private String conflict_sha;//是否存在冲突

    @Field("commitsId")
    private List<String> commitsId;//涉及到的commits


    public MergedInfo() {
    }

    public MergedInfo(String id, String appName, String appId, Map creator, Map handler, String sourceBranch, String targetBranch, String createTime, String handleTime, String groupId, String conflict_sha, List<String> commitsId) {
        this.id = id;
        this.appName = appName;
        this.appId = appId;
        this.creator = creator;
        this.handler = handler;
        this.sourceBranch = sourceBranch;
        this.targetBranch = targetBranch;
        this.createTime = createTime;
        this.handleTime = handleTime;
        this.groupId = groupId;
        this.conflict_sha = conflict_sha;
        this.commitsId = commitsId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Map getCreator() {
        return creator;
    }

    public void setCreator(Map creator) {
        this.creator = creator;
    }

    public Map getHandler() {
        return handler;
    }

    public void setHandler(Map handler) {
        this.handler = handler;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(String handleTime) {
        this.handleTime = handleTime;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getConflict_sha() {
        return conflict_sha;
    }

    public void setConflict_sha(String conflict_sha) {
        this.conflict_sha = conflict_sha;
    }

    public List<String> getCommitsId() {
        return commitsId;
    }

    public void setCommitsId(List<String> commitsId) {
        this.commitsId = commitsId;
    }

    @Override
    public String toString() {
        return "MergedInfo{" +
                "appName='" + appName + '\'' +
                ", appId='" + appId + '\'' +
                ", creator=" + creator +
                ", handler=" + handler +
                ", sourceBranch='" + sourceBranch + '\'' +
                ", targetBranch='" + targetBranch + '\'' +
                ", createTime='" + createTime + '\'' +
                ", handleTime='" + handleTime + '\'' +
                ", groupId='" + groupId + '\'' +
                ", conflict_sha='" + conflict_sha + '\'' +
                ", commitsId=" + commitsId +
                '}';
    }

    //存数据时，初始化_id和id
    public void initId() {
        ObjectId temp = new ObjectId();
        this._id = temp;
        this.id = temp.toString();
    }
}
