package com.spdb.fdev.fdemand.spdb.entity;

import com.spdb.fdev.fdemand.base.dict.Constants;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = Constants.IPMP_PROJECT)
public class IpmpProject {
    @Id
    private ObjectId _id;
    /**
     * 项目/任务集编号
     */
    @Field("project_no")
    private String project_no;
    /**
     * 项目/任务集名称
     */
    @Field("project_name")
    private String project_name;
    /**
     * 项目/任务集状态
     */
    @Field("project_status_name")
    private String project_status_name;

    /**
     * 项目类型 1-项目 2-任务集
     */
    @Field("project_type")
    private String project_type;

    public String getProject_type() {
        return project_type;
    }

    public void setProject_type(String project_type) {
        this.project_type = project_type;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getProject_no() {
        return project_no;
    }

    public void setProject_no(String project_no) {
        this.project_no = project_no;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getProject_status_name() {
        return project_status_name;
    }

    public void setProject_status_name(String project_status_name) {
        this.project_status_name = project_status_name;
    }
}
