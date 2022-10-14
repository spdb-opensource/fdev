package com.spdb.fdev.fdemand.spdb.entity;

import com.spdb.fdev.fdemand.base.dict.Dict;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Document(collection = Dict.IPMP_UNIT_OPERATE_LOG)
public class IpmpUnitOperateLog {

    @Id
    private ObjectId _id;

    @Field("id")
    private String id;

    @ApiModelProperty(value = "IPMP实施单元编号")
    private String implUnitNum;

    @ApiModelProperty(value = "更新人ID")
    private String updateUserId;

    @ApiModelProperty(value = "更新人姓名")
    private String updateUserName;

    @ApiModelProperty(value = "更新时间")
    private String updateTime;

    @ApiModelProperty(value = "IPMP接口名")
    private String interfaceName;

    @ApiModelProperty(value = "更新报文")
    private Map updateMap;

    @ApiModelProperty(value = "结果状态")
    private String resultStatus;

    @ApiModelProperty(value = "错误编码")
    private String errorCode;

    @ApiModelProperty(value = "返回信息")
    private String message;

    @ApiModelProperty(value = "返回信息response")
    private Map response;

    public Map getResponse() {
        return response;
    }

    public void setResponse(Map response) {
        this.response = response;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

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

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public Map getUpdateMap() {
        return updateMap;
    }

    public void setUpdateMap(Map updateMap) {
        this.updateMap = updateMap;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
