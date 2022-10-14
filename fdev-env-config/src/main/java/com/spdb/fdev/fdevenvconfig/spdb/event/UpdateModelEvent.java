package com.spdb.fdev.fdevenvconfig.spdb.event;

import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import org.springframework.context.ApplicationEvent;

/**
 * create by Idea
 *
 * @Author aric
 * @Date 2020/1/6 13:45
 * @Version 1.0
 */
public class UpdateModelEvent extends ApplicationEvent {

    /**
     * 修改后的数据
     */
    private Model before;

    /**
     * 修改前的数据
     */
    private Model after;

    /**
     * 操作用户的id
     */
    private String userId;

    public UpdateModelEvent(Object source, Model before, Model after, String userId) {
        super(source);
        this.before = before;
        this.after = after;
        this.userId = userId;
    }

    public Model getBefore() {
        return before;
    }

    public void setBefore(Model before) {
        this.before = before;
    }

    public Model getAfter() {
        return after;
    }

    public void setAfter(Model after) {
        this.after = after;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
