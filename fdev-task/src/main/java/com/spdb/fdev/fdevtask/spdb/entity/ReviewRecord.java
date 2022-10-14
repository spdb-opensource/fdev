package com.spdb.fdev.fdevtask.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document("review_record")
public class ReviewRecord {

    @Id
    @JsonIgnore
    public ObjectId _id;
    @Field("id")
    public String id;
    public String taskName;
    public String taskId;
    public String appName;
    public String appId;
    public List<ReviewUser> master;
    public String applicant;
    public String applicantName;
    public List<ReviewUser> reviewers;
    public String group;
    public String reviewIdea;
    public String groupName;
    public String reviewType;
    public String reason;
    public String reviewStatus;
    public String createDate;
    public String status;
    public String firstReviewer;
    public String secondReviewer;
    public String dbType;
    public String stock;

    public String getFirstReviewer() {
        return firstReviewer;
    }

    public void setFirstReviewer(String firstReviewer) {
        this.firstReviewer = firstReviewer;
    }

    public String getSecondReviewer() {
        return secondReviewer;
    }

    public void setSecondReviewer(String secondReviewer) {
        this.secondReviewer = secondReviewer;
    }

    public ReviewRecord pushReviewer(String name, String id) {
        ReviewUser user = new ReviewUser(name, id);
        if (!CommonUtils.isNullOrEmpty(reviewers)) {
            if (this.firstReviewer != null && this.firstReviewer.equals(this.secondReviewer)) {
                String fname = this.firstReviewer.split(";").length > 1 ?this.firstReviewer.split(";")[1] : "";
//                String sname = this.secondReviewer.split(";").length > 1 ?this.secondReviewer.split(";")[1] : "";
//                throw new FdevException(ErrorConstants.REVIEW_REVIEWERS_ERROR,new String[]{fname});
            }
        }
        int size = reviewers.size();
        for (int i = size - 1; i >= 0; i--) {
            ReviewUser tmp = reviewers.get(i);
            if (user.equals(tmp)) {
                reviewers.remove(i);
            }
        }
        this.reviewers.add(user);
        return this;
    }

    public ReviewRecord() {
    }

    public ReviewRecord(ObjectId _id, String id, String taskName, String taskId, String appName, String appId,
                        List<ReviewUser> master, String applicant, String applicantName, List<ReviewUser> reviewers, String group,
                        String reviewIdea, String groupName, String reviewType, String reason, String reviewStatus, String createDate, String status) {
        this._id = _id;
        this.id = id;
        this.taskName = taskName;
        this.taskId = taskId;
        this.appName = appName;
        this.appId = appId;
        this.master = master;
        this.applicant = applicant;
        this.applicantName = applicantName;
        this.reviewers = reviewers;
        this.group = group;
        this.reviewIdea = reviewIdea;
        this.groupName = groupName;
        this.reviewType = reviewType;
        this.reason = reason;
        this.reviewStatus = reviewStatus;
        this.createDate = createDate;
        this.status = status;
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

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public List getMaster() {
        return master;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public List getReviewers() {
        return reviewers;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getReviewIdea() {
        return reviewIdea;
    }

    public void setReviewIdea(String reviewIdea) {
        this.reviewIdea = reviewIdea;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getReviewType() {
        return reviewType;
    }

    public void setReviewType(String reviewType) {
        this.reviewType = reviewType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setMaster(List<ReviewUser> master) {
        this.master = master;
    }

    public void setReviewers(List<ReviewUser> reviewers) {
        this.reviewers = reviewers;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getStock(){
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }
}
