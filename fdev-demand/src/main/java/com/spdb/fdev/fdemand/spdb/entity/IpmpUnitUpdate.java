package com.spdb.fdev.fdemand.spdb.entity;

import com.spdb.fdev.fdemand.base.dict.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = Dict.IPMPUNIT_UPDATE)
@ApiModel(value = "实施单元同步时间")
public class IpmpUnitUpdate {

    @Id
    private ObjectId _id;

    /**
     * id
     */
    @Field("id")
    private String id;

    @ApiModelProperty(value = "增量参数1")
    private String updateDate;

    @ApiModelProperty(value = "增量参数2")
    private String updateTextDate;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

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

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getUpdateTextDate() {
        return updateTextDate;
    }

    public void setUpdateTextDate(String updateTextDate) {
        this.updateTextDate = updateTextDate;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
