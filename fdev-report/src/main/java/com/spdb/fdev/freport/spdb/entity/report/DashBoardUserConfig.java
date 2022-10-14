package com.spdb.fdev.freport.spdb.entity.report;

import com.spdb.fdev.freport.spdb.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;


@Document(collection = "dashboard_user_config")
@ApiModel(value="用户看板配置表")
@CompoundIndex(name = "user_config_idx", def = "{'user_id': 1, 'group_id': 1}" , unique = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class DashBoardUserConfig extends BaseEntity {

	@Field("user_id")
	@ApiModelProperty(value="用户ID")
	private String userId;

//	@Field("team_id")
//	@ApiModelProperty(value="团队ID")
//	private String teamId;

	@Field("group_id")
	@ApiModelProperty(value="小组ID")
	private String groupId;

	@Field("configs")
	@ApiModelProperty(value="看板配置参数")
	private List<Map<String, Object>> configs;

}
