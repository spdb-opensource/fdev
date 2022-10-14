package com.spdb.fdev.codeReview.spdb.entity;

import com.spdb.fdev.codeReview.spdb.dto.LogContent;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author liux81
 * @DATE 2021/11/9
 */
@Component
@Document("code_log")
public class CodeLog {

    @Id
    private ObjectId _id;

    @Field("id")
    private String id;

    @Field("order_id")
    @ApiModelProperty("")
    private String orderId;

    @Field("operate_user")
    @ApiModelProperty("")
    private String operateUser;

    @Field("operate_time")
    @ApiModelProperty("")
    private String operateTime;

    @Field("content")
    @ApiModelProperty("")
    private List<LogContent> content;

    private List<String> fieldList;//日志记录的字段列表

    private List oldValueList;//旧值列表

    private List newValueList;//新值列表

    private String operateUserName;

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

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOperateUser() {
        return operateUser;
    }

    public void setOperateUser(String operateUser) {
        this.operateUser = operateUser;
    }

    public String getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(String operateTime) {
        this.operateTime = operateTime;
    }

    public List<LogContent> getContent() {
        return content;
    }

    public void setContent(List<LogContent> content) {
        this.content = content;
    }

    public List<String> getFieldList() {
        return fieldList;
    }

    public void setFieldList(List<String> fieldList) {
        this.fieldList = fieldList;
    }

    public List getOldValueList() {
        return oldValueList;
    }

    public void setOldValueList(List oldValueList) {
        this.oldValueList = oldValueList;
    }

    public List getNewValueList() {
        return newValueList;
    }

    public void setNewValueList(List newValueList) {
        this.newValueList = newValueList;
    }

    public String getOperateUserName() {
        return operateUserName;
    }

    public void setOperateUserName(String operateUserName) {
        this.operateUserName = operateUserName;
    }
}
