package com.spdb.fdev.fdevapp.spdb.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;
import java.util.Map;

@Document(collection = "app-vip-channel")
@ApiModel(value = "绿色通道实体")
public class AppVipChannel {

    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;
    @Field("id")
    @ApiModelProperty(value = "id")
    private String id;
    @Field("trigger_time")
    @ApiModelProperty(value = "trigger_time")
    private Long trigger_time;
    @Field("app")
    @ApiModelProperty(value = "app")
    private Map<String, Object> app;
    @Field("ref")
    @ApiModelProperty(value = "ref")
    private String ref;
    @Field("status")
    @ApiModelProperty(value = "status")
    private String status;
    @Field("triggerer")
    @ApiModelProperty(value = "triggerer")
    private Map<String, Object> triggerer;
    @Field("commit")
    @ApiModelProperty(value = "commit")
    private Map<String, Object> commit;
    @Field("jobs")
    @ApiModelProperty(value = "jobs")
    private List<Map<String, Object>> jobs;
    @Field("duration")
    @ApiModelProperty(value = "duration")
    private Long duration;
    @Field("finished_at")
    @ApiModelProperty(value = "finished_at")
    private Long finished_at;
    @Field("variables")
    @ApiModelProperty(value = "环境变量")
    private Map<String, Object> variables;

    public AppVipChannel() {
    }

    public AppVipChannel(String id, Long trigger_time, Map<String, Object> app, String ref, String status, Map<String, Object> triggerer, Map<String, Object> commit, List<Map<String, Object>> jobs, Long duration, Long finished_at, Map<String, Object> variables) {
        this.id = id;
        this.trigger_time = trigger_time;
        this.app = app;
        this.ref = ref;
        this.status = status;
        this.triggerer = triggerer;
        this.commit = commit;
        this.jobs = jobs;
        this.duration = duration;
        this.finished_at = finished_at;
        this.variables = variables;
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

    public Long getTrigger_time() {
        return trigger_time;
    }

    public void setTrigger_time(Long trigger_time) {
        this.trigger_time = trigger_time;
    }

    public Map<String, Object> getApp() {
        return app;
    }

    public void setApp(Map<String, Object> app) {
        this.app = app;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getTriggerer() {
        return triggerer;
    }

    public void setTriggerer(Map<String, Object> triggerer) {
        this.triggerer = triggerer;
    }

    public Map<String, Object> getCommit() {
        return commit;
    }

    public void setCommit(Map<String, Object> commit) {
        this.commit = commit;
    }

    public List<Map<String, Object>> getJobs() {
        return jobs;
    }

    public void setJobs(List<Map<String, Object>> jobs) {
        this.jobs = jobs;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getFinished_at() {
        return finished_at;
    }

    public void setFinished_at(Long finished_at) {
        this.finished_at = finished_at;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return "AppVipChannel{" +
                "id='" + id + '\'' +
                ", trigger_time='" + trigger_time + '\'' +
                ", app=" + app +
                ", ref='" + ref + '\'' +
                ", status='" + status + '\'' +
                ", triggerer=" + triggerer +
                ", commit=" + commit +
                ", jobs=" + jobs +
                ", duration='" + duration + '\'' +
                ", finished_at='" + finished_at + '\'' +
                ", variables=" + variables +
                '}';
    }
}
