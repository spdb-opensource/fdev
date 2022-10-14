package com.csii.pe.pojo;

import com.csii.pe.spdb.common.util.CommonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;

@Component
@Document(collection = "feature_branch")
@CompoundIndexes({
        @CompoundIndex(name = "feature_index1", def = "{'application_id': 1, 'task_id': 1,'feature_branch': 1}", unique = true)
})
public class FeatureBranch implements Serializable {

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("release_node_name")
    private String release_node_name;

    @Field("application_id")
    private String application_id;

    @Field("task_id")
    private String task_id;

    @Field("feature_branch")
    private String feature_branch;

    @Field("gitlab_project_id")
    private Integer gitlab_project_id;

    @Field("already_clean")
    private Integer already_clean;

    @Field("create_time")
    private String create_time;


    public FeatureBranch() {

    }

    public FeatureBranch(String release_node_name, String application_id, String task_id, String feature_branch, Integer gitlab_project_id, Integer already_clean) {
        this.release_node_name = release_node_name;
        this.application_id = application_id;
        this.task_id = task_id;
        this.feature_branch = feature_branch;
        this.gitlab_project_id = gitlab_project_id;
        this.already_clean = already_clean;
        this.create_time = CommonUtils.sdf.format(new Date());
    }

    @Override
    public String toString() {
        return "FeatureBranch{release_node_name='" + release_node_name + '\'' +
                ", application_id='" + application_id + '\'' +
                ", task_id='" + task_id + '\'' +
                ", feature_branch='" + feature_branch + '\'' +
                ", gitlab_project_id=" + gitlab_project_id +
                ", already_clean=" + already_clean +
                ", create_time=" + create_time +
                '}';
    }

    public String getRelease_node_name() {
        return release_node_name;
    }

    public void setRelease_node_name(String release_node_name) {
        this.release_node_name = release_node_name;
    }

    public String getApplication_id() {
        return application_id;
    }

    public void setApplication_id(String application_id) {
        this.application_id = application_id;
    }

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public String getFeature_branch() {
        return feature_branch;
    }

    public void setFeature_branch(String feature_branch) {
        this.feature_branch = feature_branch;
    }

    public Integer getGitlab_project_id() {
        return gitlab_project_id;
    }

    public void setGitlab_project_id(Integer gitlab_project_id) {
        this.gitlab_project_id = gitlab_project_id;
    }

    public Integer getAlready_clean() {
        return already_clean;
    }

    public void setAlready_clean(Integer already_clean) {
        this.already_clean = already_clean;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }
}