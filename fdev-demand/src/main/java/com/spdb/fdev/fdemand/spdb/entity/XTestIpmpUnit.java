package com.spdb.fdev.fdemand.spdb.entity;

import com.spdb.fdev.fdemand.base.dict.Dict;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

@Component
@Document(collection = Dict.XTEST_IPMP_UNIT)
public class XTestIpmpUnit {

    private String id;

    @ApiModelProperty(value = "IPMP实施单元编号")
    private String implUnitNum;

    @ApiModelProperty(value = "实际提交用户测试日期")
    private String actualTestStartDate;

    @ApiModelProperty(value = "实际用户测试完成日期")
    private String actualTestFinishDate;

    @ApiModelProperty(value = "云测试平台更新时间")
    private String testUpdateTime;

    @ApiModelProperty(value = "同步时间")
    private String syncTime;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "失败原因")
    private String msg;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImplUnitNum() {
        return implUnitNum;
    }

    public void setImplUnitNum(String implUnitNum) {
        this.implUnitNum = implUnitNum;
    }

    public String getActualTestStartDate() {
        return actualTestStartDate;
    }

    public void setActualTestStartDate(String actualTestStartDate) {
        this.actualTestStartDate = actualTestStartDate;
    }

    public String getActualTestFinishDate() {
        return actualTestFinishDate;
    }

    public void setActualTestFinishDate(String actualTestFinishDate) {
        this.actualTestFinishDate = actualTestFinishDate;
    }

    public String getTestUpdateTime() {
        return testUpdateTime;
    }

    public void setTestUpdateTime(String testUpdateTime) {
        this.testUpdateTime = testUpdateTime;
    }

    public String getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(String syncTime) {
        this.syncTime = syncTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
