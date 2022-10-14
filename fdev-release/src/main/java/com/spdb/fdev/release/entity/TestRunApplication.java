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

@Document(collection = "testrun_applications")
@ApiModel(value = "试运行应用")
@CompoundIndexes({
        @CompoundIndex(name = "project_branch_idx", def = "{'gitlab_project_id': 1, 'testrun_branch': 1}", unique = true)
})
public class TestRunApplication implements Serializable {

    @Id
    @ApiModelProperty(value = "ID")
    private ObjectId _id;

    @Field("id")
    @NotEmpty(message = "编号不能为空")
    @ApiModelProperty(value = "编号")
    private String id;

    @Field("application_id")
    @NotEmpty(message = "应用编号不能为空")
    @ApiModelProperty(value = "应用编号")
    private String application_id;

    @Field("release_node_name")
    @NotEmpty(message = "投产点名称不能为空")
    @ApiModelProperty(value = "投产点名称")
    private String release_node_name;

    @Field("testrun_branch")
    @ApiModelProperty(value = "试运行分支")
    private String testrun_branch;

    @Field("transition_branch")
    @ApiModelProperty(value = "试运行前过渡分支")
    private String transition_branch;

    @Field("testrun_url")
    @ApiModelProperty(value = "试运行包地址")
    private String testrun_url;

    @Field("gitlab_project_id")
    @ApiModelProperty(value = "gitlab项目id")
    private Integer gitlab_project_id;

    @Field("create_time")
    @ApiModelProperty(value="创建时间")
    private String create_time;

    @Field("update_time")
    @ApiModelProperty(value="修改时间")
    private String update_time;

    public TestRunApplication() {
        super();
    }

    public TestRunApplication(String application_id, String release_node_name) {
        this.application_id = application_id;
        this.release_node_name = release_node_name;
    }

    public TestRunApplication(Integer gitlab_project_id, String testrun_branch, String testrun_url) {
        this.gitlab_project_id = gitlab_project_id;
        this.testrun_branch = testrun_branch;
        this.testrun_url = testrun_url;
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

    public String getApplication_id() {
        return application_id;
    }

    public void setApplication_id(String application_id) {
        this.application_id = application_id;
    }

    public String getRelease_node_name() {
        return release_node_name;
    }

    public void setRelease_node_name(String release_node_name) {
        this.release_node_name = release_node_name;
    }

    public String getTestrun_branch() {
        return testrun_branch;
    }

    public void setTestrun_branch(String testrun_branch) {
        this.testrun_branch = testrun_branch;
    }

    public String getTransition_branch() {
        return transition_branch;
    }

    public void setTransition_branch(String transition_branch) {
        this.transition_branch = transition_branch;
    }

    public String getTestrun_url() {
        return testrun_url;
    }

    public void setTestrun_url(String testrun_url) {
        this.testrun_url = testrun_url;
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
