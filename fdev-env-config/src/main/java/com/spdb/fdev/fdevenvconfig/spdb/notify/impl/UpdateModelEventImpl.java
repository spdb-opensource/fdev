package com.spdb.fdev.fdevenvconfig.spdb.notify.impl;

import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Model;
import com.spdb.fdev.fdevenvconfig.spdb.event.DeleteModelEvent;
import com.spdb.fdev.fdevenvconfig.spdb.event.UpdateModelEvent;
import com.spdb.fdev.fdevenvconfig.spdb.notify.INotifyEventStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * create by Idea
 * <p>
 * 更新实体事件
 *
 * @Author aric
 * @Date 2020/1/6 14:09
 * @Version 1.0
 */
@Component("UpdateModel")
public class UpdateModelEventImpl implements INotifyEventStrategy {
    @Resource
    private ApplicationContext applicationContext;

    /**
     * 更新实体修改 通知
     *
     * @return
     */
    @Override
    public void doNotify(Map<String, Object> parse) {
        String userId = (String) parse.get(Dict.USER_ID);
        Model before = (Model) parse.get(Dict.BEFORE);
        Object object = parse.get(Dict.AFTER);
        // after为空，则为删除实体
        if (object == null) {
            DeleteModelEvent deleteModelEvent = new DeleteModelEvent(this, before, null, userId);
            applicationContext.publishEvent(deleteModelEvent);
        } else {
            Model after = (Model) object;
            UpdateModelEvent updateModelEvent = new UpdateModelEvent(this, before, after, userId);
            applicationContext.publishEvent(updateModelEvent);
        }
    }
}
