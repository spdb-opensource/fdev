package com.spdb.fdev.fdevenvconfig.spdb.event;

import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelEnv;
import org.springframework.context.ApplicationEvent;

/**
 * create by Idea
 *
 * @Author aric
 * @Date 2020/1/6 13:45
 * @Version 1.0
 */
public class DeleteModelFieldMappingEvent extends ApplicationEvent {

    /**
     * 修改后的数据
     */
    private ModelEnv before;

    /**
     * 修改前的数据
     */
    private ModelEnv after;

    /**
     * 操作用户的id
     */
    private String userId;


    public DeleteModelFieldMappingEvent(Object source, ModelEnv before, ModelEnv after, String userId) {
        super(source);
        this.before = before;
        this.after = after;
        this.userId = userId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
