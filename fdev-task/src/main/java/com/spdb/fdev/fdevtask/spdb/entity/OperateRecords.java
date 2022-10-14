package com.spdb.fdev.fdevtask.spdb.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.SerializableString;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * 任务操作记录
 */
@Document("operate_record")
public class OperateRecords  implements Serializable {
    private static final long serialVersionUID = 8654331831270042342L;
    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty("任务id")
    private String id;

    @Field("operateTime")
    @ApiModelProperty("操作时间")
    private String operateTime;

    @Field("operateName")
    @ApiModelProperty("操作用户名字")
    private String operateName;

    @Field("operateUserId")
    @ApiModelProperty("操作用户id")
    private String operateUserId;
    /**
     * 1 上线确认书按钮
     */
    @Field("type")
    @ApiModelProperty("记录操作种类")
    private Integer type;

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

    public String getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(String operateTime) {
        this.operateTime = operateTime;
    }

    public String getOperateName() {
        return operateName;
    }

    public void setOperateName(String operateName) {
        this.operateName = operateName;
    }

    public String getOperateUserId() {
        return operateUserId;
    }

    public void setOperateUserId(String operateUserId) {
        this.operateUserId = operateUserId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
