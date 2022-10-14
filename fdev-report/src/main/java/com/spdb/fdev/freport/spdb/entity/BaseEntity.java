package com.spdb.fdev.freport.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@EqualsAndHashCode(callSuper = true)
@Data
public class BaseEntity extends SimpleEntity{

    @Field("creator")
    @ApiModelProperty(value = "创建人")
    private String creator;

    @Field("create_time")
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @Field("update_time")
    @ApiModelProperty(value = "更新时间")
    private String updateTime;

}
