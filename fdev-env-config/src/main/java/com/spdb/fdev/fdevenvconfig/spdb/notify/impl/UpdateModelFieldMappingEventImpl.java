package com.spdb.fdev.fdevenvconfig.spdb.notify.impl;

import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.entity.ModelEnv;
import com.spdb.fdev.fdevenvconfig.spdb.event.DeleteModelFieldMappingEvent;
import com.spdb.fdev.fdevenvconfig.spdb.event.UpdateModelFieldMappingEvent;
import com.spdb.fdev.fdevenvconfig.spdb.notify.INotifyEventStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * create by Idea
 * 更新实体属性事件
 *
 * @Author aric
 * @Date 2020/1/6 14:11
 * @Version 1.0
 */
@Component("UpdateModelFieldMapping")
public class UpdateModelFieldMappingEventImpl implements INotifyEventStrategy {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 更新实体环境映射值 通知
     *
     * @return
     */
    @Override
    public void doNotify(Map<String, Object> parse) {
        String userId = (String) parse.get(Dict.USER_ID);
        String applyId = (String) parse.get(Dict.APPLY_ID);
        ModelEnv before = (ModelEnv) parse.get(Dict.BEFORE);
        Object object = parse.get(Dict.AFTER);
        if (object == null) {
            DeleteModelFieldMappingEvent deleteModelFieldMappingEvent = new DeleteModelFieldMappingEvent(this, before, null, userId);
            applicationContext.publishEvent(deleteModelFieldMappingEvent);
        } else {
            ModelEnv after = (ModelEnv) object;
            String envNameEn = (String) parse.get(Dict.ENV_NAME_EN);
            String modelNameEn = (String) parse.get(Dict.MODEL_NAME_EN);
            // 获得环境中文名
            Map<String, Object> env = (Map<String, Object>) parse.get(Dict.ENV);
            String envNameCn = (String) env.get(Dict.NAME_CN);
            Map<String, Object> model = (Map<String, Object>) parse.get(Dict.MODEL);
            List<Map<String, Object>> envKeyList = (List<Map<String, Object>>) model.get(Dict.ENV_KEY);
            // 获得实体中文名
            String modelNameCn = (String) model.get(Dict.NAME_CN);
            String modify_reason = (String) parse.get(Dict.MODIFY_REASON);
            String apply_username = (String) parse.get(Dict.APPLY_USERNAME);
            UpdateModelFieldMappingEvent updateModelFieldMappingEvent = new UpdateModelFieldMappingEvent(this, before, after, envNameEn, envNameCn, modelNameEn, modelNameCn, envKeyList, userId, applyId, modify_reason, apply_username);
            applicationContext.publishEvent(updateModelFieldMappingEvent);
        }
    }
}
