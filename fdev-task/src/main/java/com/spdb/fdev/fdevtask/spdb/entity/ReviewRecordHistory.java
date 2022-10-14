package com.spdb.fdev.fdevtask.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Map;

@Document("review_record_history")
public class ReviewRecordHistory implements Serializable {
    private static final long serialVersionUID = 8557731831270010690L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    private String id;
    //详情页上列展示基本信息：任务名、所属组、（文档清单）、发起人、审核人、审核类型（ 数据库变更）、审核状态。
    @Field("task_id")
    @ApiModelProperty(value = "任务ID")
    private String task_id;

    @Field("task_name")
    @ApiModelProperty(value = "任务名")
    private String task_name;

    @Field("group")
    private String group;

    @Field("doc")
    private String doc;

    @Field("initiator")
    private Map initiator;

    @Field("auditor")
    private Map auditor;

    @Field("review_type")
    private String review_type;

    @Field("review_status")
    private String review_status;

    @Field("db_type")
    private String db_type;

    @Field("status")
    private String status;

    @Field("review_time")
    private String review_time;

    @Field("init_auditor")
    private Map init_auditor;

    @Field("opno")
    private Map opno;

    public ReviewRecordHistory() {
    }

    public ReviewRecordHistory(ObjectId _id, String id, String task_id, String task_name, String group, String doc, Map initiator, Map auditor, String review_type, String review_status, String db_type, String status, String review_time, Map init_auditor, Map opno) {
        this._id = _id;
        this.id = id;
        this.task_id = task_id;
        this.task_name = task_name;
        this.group = group;
        this.doc = doc;
        this.initiator = initiator;
        this.auditor = auditor;
        this.review_type = review_type;
        this.review_status = review_status;
        this.db_type = db_type;
        this.status = status;
        this.review_time = review_time;
        this.init_auditor = init_auditor;
        this.opno = opno;
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

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public String getTask_name() {
        return task_name;
    }

    public void setTask_name(String task_name) {
        this.task_name = task_name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public Map getInitiator() {
        return initiator;
    }

    public void setInitiator(Map initiator) {
        this.initiator = initiator;
    }

    public Map getAuditor() {
        return auditor;
    }

    public void setAuditor(Map auditor) {
        this.auditor = auditor;
    }

    public String getReview_type() {
        return review_type;
    }

    public void setReview_type(String review_type) {
        this.review_type = review_type;
    }

    public String getReview_status() {
        return review_status;
    }

    public void setReview_status(String review_status) {
        this.review_status = review_status;
    }

    public String getDb_type() {
        return db_type;
    }

    public void setDb_type(String db_type) {
        this.db_type = db_type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReview_time() {
        return review_time;
    }

    public void setReview_time(String review_time) {
        this.review_time = review_time;
    }

    public Map getInit_auditor() {
        return init_auditor;
    }

    public void setInit_auditor(Map init_auditor) {
        this.init_auditor = init_auditor;
    }

    public Map getOpno() {
        return opno;
    }

    public void setOpno(Map opno) {
        this.opno = opno;
    }

    @Override
    public String toString() {
        return "ReviewRecordHistory{" +
                "_id=" + _id +
                ", id='" + id + '\'' +
                ", task_id='" + task_id + '\'' +
                ", task_name='" + task_name + '\'' +
                ", group='" + group + '\'' +
                ", doc='" + doc + '\'' +
                ", initiator=" + initiator +
                ", auditor=" + auditor +
                ", review_type='" + review_type + '\'' +
                ", review_status='" + review_status + '\'' +
                ", db_type='" + db_type + '\'' +
                ", status='" + status + '\'' +
                ", review_time='" + review_time + '\'' +
                ", init_auditor=" + init_auditor +
                ", opno=" + opno +
                '}';
    }
}
