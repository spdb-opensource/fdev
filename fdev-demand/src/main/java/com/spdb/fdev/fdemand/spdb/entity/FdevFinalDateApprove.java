package com.spdb.fdev.fdemand.spdb.entity;

import com.spdb.fdev.fdemand.base.dict.Dict;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = Dict.FDEV_FINAL_DATE_APPROVE)
public class FdevFinalDateApprove {

    @Id
    private ObjectId _id;

    @Field("id")
    private String id;

    @Field("access_id")
    @ApiModelProperty(value = "评估id")
    private String access_id;

    @Indexed
    @Field("oa_contact_no")
    @ApiModelProperty(value = "需求编号")
    private String oa_contact_no;

    @ApiModelProperty(value = "需求名称")
    private String oa_contact_name;

    @ApiModelProperty(value = "需求牵头小组")
    private String demand_leader_group;

    private String demand_leader_group_cn;

    @Field("apply_reason")
    @ApiModelProperty(value = "申请审批原因")
    private String apply_reason;

    @Field("apply_user_id")
    @ApiModelProperty(value = "申请人")
    private String apply_user_id;
    private String apply_user;

    @Field("section_id")
    @ApiModelProperty(value = "条线id")
    private String section_id;
    private String section_id_cn;

    @Field("apply_update_time")
    @ApiModelProperty(value = "申请定稿日期修改时间")
    private String apply_update_time;

    @Field("create_time")
    @ApiModelProperty(value = "申请时间")
    private String create_time;

    @Field("operate_user_id")
    @ApiModelProperty(value = "审批人")
    private String operate_user_id;
    private String operate_user;

    @Field("operate_time")
    @ApiModelProperty(value = "审批时间")
    private String operate_time;

    @Field("operate_status")
    @ApiModelProperty(value = "审批状态： undetermined 待审批，同意：agree ，拒绝： disagree")
    private String operate_status;
    private String operate_status_cn;

    @Field("state")
    @ApiModelProperty(value = "说明：同意不用写任何内容，拒绝则必须写说明内容")
    private String state;

}
