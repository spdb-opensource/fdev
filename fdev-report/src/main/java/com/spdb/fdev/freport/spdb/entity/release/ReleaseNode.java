package com.spdb.fdev.freport.spdb.entity.release;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Document(collection = "release_nodes")
@ApiModel(value = "投产窗口表")
@Data
public class ReleaseNode {

    @Field("release_date")
    @ApiModelProperty(value = "投产日期")
    private String releaseDate;

    @Field("release_node_name")
    @ApiModelProperty(value = "投产点名称")
    @Indexed(unique = true)
    private String releaseNodeName;

    @Field("create_user")
    @ApiModelProperty(value = "投产窗口创建人员id")
    private String createUser;

    @Field("create_user_name_en")
    @ApiModelProperty(value = "投产窗口创建人员英文名")
    private String createUserNameEn;

    @Field("create_user_name_cn")
    @ApiModelProperty(value = "投产窗口创建人员中文名")
    private String createUserNameCn;

    @Field("owner_groupId")
    @ApiModelProperty(value = "所属小组Id")
    private String ownerGroupId;

    @Field("owner_group_name")
    @ApiModelProperty(value = "所属小组名")
    private String ownerGroupName;

    @Field("release_manager")
    @ApiModelProperty(value = "投产管理员")
    private String releaseManager;

    @Field("release_manager_id")
    @ApiModelProperty(value = "投产管理员")
    private String releaseManagerId;

    @Field("release_manager_name_cn")
    @NotEmpty(message = "投产管理员不能为空")
    @ApiModelProperty(value = "投产管理员")
    private String releaseManagerNameCn;

    @Field("release_spdb_manager")
    @ApiModelProperty(value = "投产行内管理员")
    private String releaseSpdbManager;

    @Field("release_spdb_manager_id")
    @ApiModelProperty(value = "投产行内管理员id")
    private String releaseSpdbManagerId;

    @Field("release_spdb_manager_name_cn")
    @ApiModelProperty(value = "投产行内管理员中文名")
    private String releaseSpdbManagerNameCn;

    @Field("node_status")
    @ApiModelProperty(value = "窗口状态,1 - created,0-deserted")
    private String nodeStatus;

    @Field("type")
    @ApiModelProperty(value = "窗口类型,0 - 投产大窗口 1 - 微服务窗口，2 - 原生窗口，3 - 试运行窗口")
    private String type;


    @Field("release_contact")
    @ApiModelProperty(value = "投产大窗口牵头人")
    private List<String> releaseContact;
}
