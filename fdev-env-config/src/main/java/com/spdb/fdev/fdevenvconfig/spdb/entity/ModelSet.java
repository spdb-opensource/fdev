package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

/**
 * 实体组
 */
@Document("model_set")
public class ModelSet implements Serializable {

    private static final long serialVersionUID = 5793889173433371792L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    // id
    @Field(Dict.ID)
    private String id;

    // 实体组中文名
    @Field(Constants.NAME_CN)
    private String nameCn;

    // 实体组模板
    @Field(Dict.TEMPLATE)
    private String template;

    // 实体Id
    @Field(Dict.MODELS)
    private List<String> models;

    // 操作人id
    @Field(Constants.OPNO)
    private String opno;

    // 状态 1-未删除，0-删除
    @Field(Dict.STATUS)
    private Integer status;

    // 创建时间
    @Field(Dict.CREATE_TIME)
    private String createTime;

    // 更新时间
    @Field(Dict.UPDATE_TIME)
    private String updateTime;

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

    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public List<String> getModels() {
        return models;
    }

    public void setModels(List<String> models) {
        this.models = models;
    }

    public String getOpno() {
        return opno;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setOpno(String opno) {
        this.opno = opno;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

}
