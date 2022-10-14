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

@Component(Dict.PIPELINE_HOOK)
@RefreshScope
public class PipelineEventStrategyImpl implements WebhookEventStrategy {

    @Value("${kafka.pipeline.topic}")
    private String kafkaPipelineTopic;
    @Autowired
    private KafkaService kafkaService;

    @Override
    public Object eventExecutor(Map<String, Object> parse) {
        Map<String, Object> projectInfo = (Map<String, Object>) parse.get(Dict.PROJECT);
        Integer projectId = (Integer) projectInfo.get(Dict.ID);
        Map<String, Object> attributes = (Map<String, Object>) parse.get(Dict.OBJECT_ATTRIBUTES);
        // pipeline id
        Integer pipelineId = (Integer) attributes.get(Dict.ID);
        // 触发pipeline的分支
        String pipelineBranch = (String) attributes.get(Dict.REF);
        // pipeline状态
        String status = (String) attributes.get(Dict.STATUS);
        // 发送数据
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(Dict.PROJECT_ID, projectId);
        logMap.put(Dict.PIPELINE_ID, pipelineId);
        logMap.put(Dict.PIPELINE_BRANCH, pipelineBranch);
        logMap.put(Dict.STATUS, status);
        kafkaService.sendData(kafkaPipelineTopic, JSON.toJSONString(parse).getBytes(), logMap);
        return null;
    }

}
