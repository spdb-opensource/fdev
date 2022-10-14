package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

/**
 * @author dafeng
 * @date 2021/2/23 16:14
 */
@Component
@ApiModel(value = "push基础的参数")
public class PushParams {

    @Field("branchType")
    @ApiModelProperty(value = "分支类型")
    private String branchType;

    @Field("branchName")
    @ApiModelProperty(value = "分支名或正则")
    private String branchName;

    public String getBranchType() {
        return branchType;
    }

    public void setBranchType(String branchType) {
        this.branchType = branchType;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    @Override
    public String toString() {
        return "PushParams{" +
                "branchType='" + branchType + '\'' +
                ", branchName='" + branchName + '\'' +
                '}';
    }
}
