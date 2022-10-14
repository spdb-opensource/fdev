package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

/**
 * 记录流水实体
 */
@Document(collection = "history-details")
public class HistoryDetails {

    @Id
    @JsonIgnore
    private ObjectId _id;

    // 流水id
    @Field("id")
    private String id;

    //操作员
    @Field("opno")
    private String opno;

    private String username;

    // 实体英文名
    @Field("modelName")
    private String modelName;

    // 更新的属性
    @Field("fields")
    private List<Map<String, Object>> fields;

    // 修改前的数据
    @Field("before")
    private Object before;

    // 修改后的数据
    @Field("after")
    private Object after;

    // 邮件通知状态
    @Field("status")
    private String status;

    // 操作类型
    @Field("type")
    private String type;

    // 创建时间
    @Field("ctime")
    private String ctime;

    // 环境英文名
    @Field("env")
    private String env;

    // 申请流水号
    @Field(Dict.APPLY_ID)
    private String applyId;

    public HistoryDetails() {
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

    public String getOpno() {
        return opno;
    }

    public void setOpno(String opno) {
        this.opno = opno;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<Map<String, Object>> getFields() {
        return fields;
    }

    public void setFields(List<Map<String, Object>> fields) {
        this.fields = fields;
    }

    public Object getBefore() {
        return before;
    }

    public void setBefore(Object before) {
        this.before = before;
    }

    public Object getAfter() {
        return after;
    }

    public void setAfter(Object after) {
        this.after = after;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }
}
