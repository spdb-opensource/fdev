package com.spdb.fdev.freport.spdb.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.freport.spdb.entity.SimpleEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@EqualsAndHashCode(callSuper = true)
@Document(collection = "company")
@ApiModel(value = "公司")
@Data
public class Company extends SimpleEntity {

    @Field("name")
    @ApiModelProperty(value = "公司名称")
    private String name;

    @JsonIgnore
    @Field("status")
    @ApiModelProperty(value = "公司状态, 0是废弃, 1是使用")
    private String status;

}

