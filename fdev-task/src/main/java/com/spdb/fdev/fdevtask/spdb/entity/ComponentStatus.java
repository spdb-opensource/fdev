package com.spdb.fdev.fdevtask.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * @program: fdev-process
 * @description:
 * @author: c-gaoys
 * @create: 2021-02-01 11:04
 **/
@ApiModel(value="组件执行状态")
public class ComponentStatus {

    @ApiModelProperty("源状态id")
    private String sourceId;

    @ApiModelProperty("目标状态id")
    private String destId;

    @ApiModelProperty("{id:组件id,result:当前组件执行结果,exe_status:组件状态0:未开始, 1:执行中, 2:成功 3:失败}")
    private List<Map<String,String>> componentResultAndStatus;

    public ComponentStatus() {
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getDestId() {
        return destId;
    }

    public void setDestId(String destId) {
        this.destId = destId;
    }

    public List<Map<String, String>> getComponentResult() {
        return componentResultAndStatus;
    }

    public void setComponentResult(List<Map<String, String>> componentResult) {
        this.componentResultAndStatus = componentResult;
    }
}
