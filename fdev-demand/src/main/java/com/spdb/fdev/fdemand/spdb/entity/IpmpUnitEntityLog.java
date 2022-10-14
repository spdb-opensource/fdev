package com.spdb.fdev.fdemand.spdb.entity;

import com.spdb.fdev.fdemand.base.dict.Dict;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Document(collection = Dict.IPMP_UNIT_ENTITY_LOG)
public class IpmpUnitEntityLog {

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

    @ApiModelProperty(value = "操作类型")// syncNew = 同步新增 syncUpdate = 同步修改 ,fdevUpdate = 页面修改
    private String updateType;

    @ApiModelProperty(value = "实施单元内容")
    private Map<String,Object> ipmpUnitInfo;

    @ApiModelProperty(value = "变动字段列表")
    private List<String> updateFieldInfo;

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

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public Map<String,Object> getIpmpUnitInfo() {
        return ipmpUnitInfo;
    }

    public void setIpmpUnitInfo(Map<String,Object> ipmpUnitInfo) {
        this.ipmpUnitInfo = ipmpUnitInfo;
    }

    public List<String> getUpdateFieldInfo() {
        return updateFieldInfo;
    }

    public void setUpdateFieldInfo(List<String> updateFieldInfo) {
        this.updateFieldInfo = updateFieldInfo;
    }
}
