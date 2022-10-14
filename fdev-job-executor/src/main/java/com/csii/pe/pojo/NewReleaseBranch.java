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

import java.util.Date;


@Component
@Document(collection = "new_release_branch")
@CompoundIndexes({
        @CompoundIndex(name = "release_index1", def = "{'application_id': 1, 'release_branch': 1}", unique = true)
})
public class NewReleaseBranch {

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("application_id")
    private String application_id;

    @Field("release_branch")
    private String release_branch;

    @Field("gitlab_project_id")
    private Integer gitlab_project_id;

    @Field("already_clean")
    private Integer already_clean;

    @Field("create_time")
    private String create_time;


    public NewReleaseBranch() {
    }

    public NewReleaseBranch(String application_id, String release_branch, Integer gitlab_project_id, Integer already_clean) {
        this.application_id = application_id;
        this.release_branch = release_branch;
        this.gitlab_project_id = gitlab_project_id;
        this.already_clean = already_clean;
        this.create_time = CommonUtils.sdf.format(new Date());
    }

    public String getApplication_id() {
        return application_id;
    }

    public void setApplication_id(String application_id) {
        this.application_id = application_id;
    }

    public String getRelease_branch() {
        return release_branch;
    }

    public void setRelease_branch(String release_branch) {
        this.release_branch = release_branch;
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

    @Override
    public String toString() {
        return "NewReleaseBranch{" +
                "application_id='" + application_id + '\'' +
                ", release_branch='" + release_branch + '\'' +
                ", gitlab_project_id=" + gitlab_project_id +
                ", already_clean=" + already_clean +
                ", create_time='" + create_time + '\'' +
                '}';
    }
}
