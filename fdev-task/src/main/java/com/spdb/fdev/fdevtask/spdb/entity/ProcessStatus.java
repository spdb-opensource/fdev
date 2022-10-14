package com.spdb.fdev.fdevtask.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * @program: fdev-process
 * @description:
 * @author: c-gaoys
 * @create: 2021-01-22 16:33
 **/
@Document(collection = "process_status")
@ApiModel(value = "状态")
public class ProcessStatus {
    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "状态ID")
    private String id;

    @Field("name")
    @ApiModelProperty(value = "状态名称")
    private String name;

    @Field("statusType")
    @ApiModelProperty(value = "类型（0-起始、1-中间、2-结束）")
    private int statusType;

    @Field("inList")
    @ApiModelProperty(value = "源flow")
    private List<InFlow> inList;

    @Field("outList")
    @ApiModelProperty(value = "目标flow")
    private List<FlowOut> outList;

    @Field("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    @Field("createUserId")
    @ApiModelProperty(value = "创建人id")
    private String createUserId;

    @Field("createTime")
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @Field("updateTime")
    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    public ProcessStatus() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatusType() {
        return statusType;
    }

    public void setStatusType(int statusType) {
        this.statusType = statusType;
    }

    public List<InFlow> getInList() {
        return inList;
    }

    public void setInList(List<InFlow> inList) {
        this.inList = inList;
    }

    public List<FlowOut> getOutList() {
        return outList;
    }

    public void setOutList(List<FlowOut> outList) {
        this.outList = outList;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
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
