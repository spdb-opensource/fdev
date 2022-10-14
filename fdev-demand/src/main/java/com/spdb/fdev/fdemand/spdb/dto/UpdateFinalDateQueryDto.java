package com.spdb.fdev.fdemand.spdb.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class UpdateFinalDateQueryDto extends PageDto {

    @ApiModelProperty(value = "需求名称/编号")
    private String oaContactNameNo;

    @ApiModelProperty(value = "需求牵头小组集合")
    private List<String> demandLeaderGroups;

    @ApiModelProperty(value = "申请人")
    private List<String> applyUserId;

    @ApiModelProperty(value = "审批人")
    private List<String> operateUserId;

    @ApiModelProperty(value = "批量审批状态： undetermined 待审批，同意：agree ，拒绝： disagree")
    private List<String> operateStatus;

    private String status;

    List<String> ids;

    private String operateUser;
}
