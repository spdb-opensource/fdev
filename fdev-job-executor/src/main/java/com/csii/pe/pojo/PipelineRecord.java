package com.csii.pe.pojo;

import com.csii.pe.spdb.common.dict.Dict;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Set;

/**
 * 记录当天pipeline成功过的应用及分支，定时批量部署自动化测试环境
 *
 * @author xxx
 * @date 2020/5/18 13:46
 */
@Document(collection = "pipeline_record")
public class PipelineRecord implements Serializable {

    private static final long serialVersionUID = -1257241722623037897L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field(Dict.ID)
    private String id;

    // GitLab Project ID
    @Field(Dict.PROJECT_ID)
    private Integer projectId;

    @Field(Dict.PROJECT_NAME)
    private String projectName;

    @Field(Dict.BRANCH)
    private String branch;

    @Field(Dict.PIPELINE_IDS)
    private Set<Integer> pipelineIds;

    @Field(Dict.CREATE_TIME)
    private String createTime;

    @Field(Dict.UPDATE_TIME)
    private String updateTime;

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

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Set<Integer> getPipelineIds() {
        return pipelineIds;
    }

    public void setPipelineIds(Set<Integer> pipelineIds) {
        this.pipelineIds = pipelineIds;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
