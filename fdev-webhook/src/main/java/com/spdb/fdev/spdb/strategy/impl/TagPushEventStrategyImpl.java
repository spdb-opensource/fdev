package com.spdb.fdev.spdb.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.spdb.service.KafkaService;
import com.spdb.fdev.spdb.strategy.WebhookEventStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Hxy
 * @Date: 2021/1/10 4:20 下午
 */
@Component(Dict.TAG_PUSH_HOOK)
@RefreshScope
public class TagPushEventStrategyImpl implements WebhookEventStrategy {

    @Value("${kafka.tag.push.topic}")
    private String kafkaTagPushTopic;
    @Autowired
    private KafkaService kafkaService;

    @Override
    public Object eventExecutor(Map<String, Object> parse) {
        // 触发push的Project ID
        Integer projectId = (Integer) parse.get(Dict.PROJECT_ID);
        // 触发push的分支
        String pushBranch = (String) parse.get(Dict.REF);
        // 发送数据
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(Dict.PROJECT_ID, projectId);
        logMap.put(Dict.PUSH_BRANCH, pushBranch);
        kafkaService.sendData(kafkaTagPushTopic, JSON.toJSONString(parse).getBytes(), logMap);
        return null;
    }
}
