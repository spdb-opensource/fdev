package com.spdb.fdev.freport.spdb.entity.report;

import com.spdb.fdev.freport.spdb.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@ApiModel(value = "仪表盘提示")
@Document(collection = "dashboard_tips")
@EqualsAndHashCode(callSuper = true)
@Data
public class DashboardTips extends BaseEntity {

    @Field("code")
    @Indexed(unique = true)
    @ApiModelProperty(value = "编码")
    private String code;

    @Field("name")
    @ApiModelProperty(value = "业务名称")
    private String name;

    @Field("content")
    @ApiModelProperty(value = "提示内容")
    private String content;

}
