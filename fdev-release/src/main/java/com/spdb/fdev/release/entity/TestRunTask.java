package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Document(collection = "testrun_task")
@ApiModel(value = "试运行任务")
@CompoundIndexes({
        @CompoundIndex(name = "project_merge_idx", def = "{'gitlab_project_id': 1, 'merge_request_id': 1}", unique = true)
})
public class TestRunTask implements Serializable {

    @Id
    @ApiModelProperty(value = "ID")
    private ObjectId _id;

    @Field("id")
    @NotEmpty(message = "编号不能为空")
    @ApiModelProperty(value = "编号")
    private String id;

    @Field("task_id")
    @NotEmpty(message = "任务编号不能为空")
    @ApiModelProperty(value = "任务编号")
    private String task_id;

    @Field("task_name")
    @ApiModelProperty(value = "任务名称")
    private String task_name;

    @Field("task_branch")
    @ApiModelProperty(value = "任务所在分支名称")
    private String task_branch;

    @Field("merge_request_id")
    @ApiModelProperty(value = "合并分支编号")
    private String merge_request_id;

    @Field("status")
    @ApiModelProperty(value = "分支合并状态 0：未合并 1：已合并")
    private String status;

    @Field("testrun_id")
    @ApiModelProperty(value = "试运行编号")
    private String testrun_id;

    @Field("gitlab_project_id")
    @ApiModelProperty(value = "gitlab项目id")
    private Integer gitlab_project_id;

    @Field("create_time")
    @ApiModelProperty(value="创建时间")
    private String create_time;

    @Field("update_time")
    @ApiModelProperty(value="修改时间")
    private String update_time;

    public TestRunTask() {
        super();
    }

    public TestRunTask(String testrun_id) {
        this.testrun_id = testrun_id;
    }

    public TestRunTask(Integer gitlab_project_id, String merge_request_id) {
        this.gitlab_project_id = gitlab_project_id;
        this.merge_request_id = merge_request_id;
    }

    public TestRunTask(Integer gitlab_project_id, String merge_request_id, String status) {
        this.gitlab_project_id = gitlab_project_id;
        this.merge_request_id = merge_request_id;
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

    public String getTask_branch() {
        return task_branch;
    }

    public void setTask_branch(String task_branch) {
        this.task_branch = task_branch;
    }

    public String getMerge_request_id() {
        return merge_request_id;
    }

    public void setMerge_request_id(String merge_request_id) {
        this.merge_request_id = merge_request_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTestrun_id() {
        return testrun_id;
    }

    public void setTestrun_id(String testrun_id) {
        this.testrun_id = testrun_id;
    }

    public Integer getGitlab_project_id() {
        return gitlab_project_id;
    }

    public void setGitlab_project_id(Integer gitlab_project_id) {
        this.gitlab_project_id = gitlab_project_id;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }
}
