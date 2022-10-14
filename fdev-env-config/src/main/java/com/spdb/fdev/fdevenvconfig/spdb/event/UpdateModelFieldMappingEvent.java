package com.spdb.fdev.fdevenvconfig.spdb.event;

import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelEnv;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Map;

/**
 * create by Idea
 *
 * @Author aric
 * @Date 2020/1/6 13:45
 * @Version 1.0
 */
public class UpdateModelFieldMappingEvent extends ApplicationEvent {

    /**
     * 修改后的数据
     */
    private ModelEnv before;

    /**
     * 修改前的数据
     */
    private ModelEnv after;

    /**
     * 环境英文名
     */
    private String envNameEn;

    /**
     * 环境中文名
     */
    private String envNameCn;

    /**
     * 实体英文名
     */
    private String modelNameEn;

    /**
     * 实体中文名
     */
    private String modelNameCn;

    /**
     * 实体属性列表
     */
    List<Map<String, Object>> envKeyList;

    /**
     * 操作用户的id
     */
    private String userId;

    /**
     * 申请流水号
     */
    private String applyId;
    /**
     * 修改原因
     */
    private String modifyReason;
    /**
     * 申请修改人
     */
    private String apply_username;

    public UpdateModelFieldMappingEvent(Object source, ModelEnv before, ModelEnv after, String envNameEn, String envNameCn, String modelNameEn, String modelNameCn, List<Map<String, Object>> envKeyList, String userId, String applyId, String modifyReason, String apply_username) {
        super(source);
        this.before = before;
        this.after = after;
        this.envNameEn = envNameEn;
        this.envNameCn = envNameCn;
        this.modelNameEn = modelNameEn;
        this.modelNameCn = modelNameCn;
        this.envKeyList = envKeyList;
        this.userId = userId;
        this.applyId = applyId;
        this.modifyReason = modifyReason;
        this.apply_username = apply_username;
    }

    public ModelEnv getBefore() {
        return before;
    }

    public void setBefore(ModelEnv before) {
        this.before = before;
    }

    public ModelEnv getAfter() {
        return after;
    }

    public void setAfter(ModelEnv after) {
        this.after = after;
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

    public List<Map<String, Object>> getEnvKeyList() {
        return envKeyList;
    }

    public void setEnvKeyList(List<Map<String, Object>> envKeyList) {
        this.envKeyList = envKeyList;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    public String getModifyReason() {
        return modifyReason;
    }

    public void setModifyReason(String modifyReason) {
        this.modifyReason = modifyReason;
    }

    public String getApply_username() {
        return apply_username;
    }

    public void setApply_username(String apply_username) {
        this.apply_username = apply_username;
    }
}
