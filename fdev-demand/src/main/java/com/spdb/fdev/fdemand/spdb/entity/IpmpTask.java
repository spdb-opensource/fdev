package com.spdb.fdev.fdemand.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fdemand.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Document(collection = Dict.IPMP_TASK)
public class IpmpTask {

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    private String id;

    /**
     * 任务集编号
     */
    @Field("task_no")
    private String taskNo;

    /**
     * 任务集名称
     */
    @Field("task_name")
    private String taskName;

    /**
     * 所属小组id
     */
    @Field("group_id")
    private String groupId;
    /**
     * 所属小组名称
     */
    @Field("group_name")
    private String groupName;

    /**
     * 实施单元列表
     */
    @Field("implement_unit_list")
    private List<IpmpImplementUnit> implement_unit_list=new ArrayList<>();


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

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<IpmpImplementUnit> getIpmpImplementUnitList() {
        return implement_unit_list;
    }

    public void setIpmpImplementUnitList(List<IpmpImplementUnit> ipmpImplementUnitList) {
        this.implement_unit_list = ipmpImplementUnitList;
    }
}