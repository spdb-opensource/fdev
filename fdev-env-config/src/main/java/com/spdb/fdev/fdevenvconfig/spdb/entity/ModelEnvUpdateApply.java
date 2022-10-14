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
import java.util.Map;

/**
 * 实体与环境映射修改申请
 */
@Document("model_env_update_apply")
public class ModelEnvUpdateApply implements Serializable {

    private static final long serialVersionUID = -5006413032474350606L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    // id/申请流水号
    @Field(Dict.ID)
    private String id;

    // 实体id
    @Field(Constants.MODEL_ID)
    private String modelId;

    // 实体英文名
    @Field(Dict.MODEL_NAME_EN)
    private String modelNameEn;

    // 实体中文名
    @Field(Dict.MODEL_NAME_CN)
    private String modelNameCn;

    // 环境id
    @Field(Constants.ENV_ID)
    private String envId;

    // 环境英文名
    @Field(Dict.ENV_NAME_EN)
    private String envNameEn;

    // 环境中文名
    @Field(Dict.ENV_NAME_CN)
    private String envNameCn;

    // 更新的实体与环境映射id
    @Field(Dict.MODEL_ENV_ID)
    private String modelEnvId;

    // 描述
    @Field(Constants.DESC)
    private String desc;

    // 更新的属性
    @Field(Dict.VARIABLES)
    private List<Map<String, Object>> variables;

    // 更新的类型：insert-新增，update-更新
    @Field(Dict.TYPE)
    private String type;

    // 更新的状态：checking-核对中，finished-核对完成，overtime-超期未核对，cancel-取消
    @Field(Dict.STATUS)
    private String status;

    // 申请人邮箱帐号
    @Field(Dict.APPLY_EMAIL)
    private String applyEmail;

    // 申请人中文名
    @Field(Dict.APPLY_USERNAME)
    private String applyUsername;

    // 申请人id
    @Field(Dict.APPLY_USER_ID)
    private String applyUserId;

    // 申请应用英文名
    @Field(Dict.APP_NAME_EN)
    private String appNameEn;

    // 核对时间
    @Field(Dict.CHECK_TIME)
    private String checkTime;

    // 操作的环境配置管理员
    @Field(Dict.ENV_MANAGER)
    private String envManager;

    // 申请时间/创建时间
    @Field(Dict.CREATE_TIME)
    private String createTime;

    // 更新时间
    @Field(Dict.UPDATE_TIME)
    private String updateTime;

    //修改原因
    @Field(Dict.MODIFY_REASON)
    private String modify_reason;

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

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getModelEnvId() {
        return modelEnvId;
    }

    public void setModelEnvId(String modelEnvId) {
        this.modelEnvId = modelEnvId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getModelNameEn() {
        return modelNameEn;
    }

    public void setModelNameEn(String modelNameEn) {
        this.modelNameEn = modelNameEn;
    }

    public String getModelNameCn() {
        return modelNameCn;
    }

    public void setModelNameCn(String modelNameCn) {
        this.modelNameCn = modelNameCn;
    }

    public String getEnvNameEn() {
        return envNameEn;
    }

    public void setEnvNameEn(String envNameEn) {
        this.envNameEn = envNameEn;
    }

    public String getEnvNameCn() {
        return envNameCn;
    }

    public void setEnvNameCn(String envNameCn) {
        this.envNameCn = envNameCn;
    }

    public String getApplyUsername() {
        return applyUsername;
    }

    public void setApplyUsername(String applyUsername) {
        this.applyUsername = applyUsername;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getApplyUserId() {
        return applyUserId;
    }

    public void setApplyUserId(String applyUserId) {
        this.applyUserId = applyUserId;
    }

    public List<Map<String, Object>> getVariables() {
        return variables;
    }

    public void setVariables(List<Map<String, Object>> variables) {
        this.variables = variables;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApplyEmail() {
        return applyEmail;
    }

    public void setApplyEmail(String applyEmail) {
        this.applyEmail = applyEmail;
    }

    public String getAppNameEn() {
        return appNameEn;
    }

    public void setAppNameEn(String appNameEn) {
        this.appNameEn = appNameEn;
    }

    public String getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(String checkTime) {
        this.checkTime = checkTime;
    }

    public String getEnvManager() {
        return envManager;
    }

    public void setEnvManager(String envManager) {
        this.envManager = envManager;
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

    public String getModify_reason() {
        return modify_reason;
    }

    public void setModify_reason(String modify_reason) {
        this.modify_reason = modify_reason;
    }
}
