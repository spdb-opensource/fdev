package com.spdb.fdev.codeReview.spdb.entity;

import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.Set;


/**
 * @Author liux81
 * @DATE 2021/11/9
 */
@Component
@Document("code_meeting")
public class CodeMeeting extends BaseEntity{

    @Field("id")
    private String id;

    @Field("order_id")
    @ApiModelProperty("工单id")
    private String orderId;

    @Field("address")
    @ApiModelProperty("会议地点")
    private String address;

    @Field("meeting_type")
    @ApiModelProperty("会议类型")
    private String meetingType;//会议类型。firstCheck初审/recheck复审

    @Field("audite_users")
    @ApiModelProperty("审核人id集合")
    private Set<String> auditeUsers;

    @Field("meeting_time")
    @ApiModelProperty("会议时间")
    private String meetingTime;


    private String updateButton;//0亮，1置灰（仅代码审核角色可编辑），2置灰（工单终态不可编辑）

    private String deleteButton;//0亮，1置灰（仅代码审核角色可删除），2置灰（工单终态下不可删除会议），3置灰（会议下存在问题，不可删除）

    private String auditorUsersCn;//审核人姓名展示字符串，如A、B、C

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(String meetingType) {
        this.meetingType = meetingType;
    }

    public Set<String> getAuditeUsers() {
        return auditeUsers;
    }

    public void setAuditeUsers(Set<String> auditeUsers) {
        this.auditeUsers = auditeUsers;
    }

    public String getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(String meetingTime) {
        this.meetingTime = meetingTime;
    }

    public String getUpdateButton() {
        return updateButton;
    }

    public void setUpdateButton(String updateButton) {
        this.updateButton = updateButton;
    }

    public String getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(String deleteButton) {
        this.deleteButton = deleteButton;
    }

    public String getAuditorUsersCn() {
        return auditorUsersCn;
    }

    public void setAuditorUsersCn(String auditorUsersCn) {
        this.auditorUsersCn = auditorUsersCn;
    }
}
