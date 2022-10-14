package com.spdb.fdev.component.listener;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.component.entity.ComponentApplication;
import com.spdb.fdev.component.service.IApplicationArchetypeService;
import com.spdb.fdev.component.service.IArchetypeRecordService;
import com.spdb.fdev.component.service.IComponentApplicationService;
import com.spdb.fdev.component.service.IImageApplicationService;
import com.spdb.fdev.component.strategy.webhook.WebhookContext;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaMsgListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaMsgListener.class);
    @Autowired
    private IArchetypeRecordService archetypeRecordService;
    @Autowired
    private IComponentApplicationService iComponentApplicationService;
    @Autowired
    private IImageApplicationService imageApplicationService;
    @Autowired
    private IApplicationArchetypeService applicationArchetypeService;
    @Autowired
    private WebhookContext webhookContext;

    @KafkaListener(topics = {"${delete.app.topic}"}, groupId = "${gruopId}")
    public void receiveAppInfo(ConsumerRecord<?, ?> consumerRecord) throws Exception {
        byte[] bytes = (byte[]) consumerRecord.value();
        String params = new String(bytes);
        Map<String, Object> parse = JSON.parseObject(params, Map.class);
        String applicationId = (String) parse.get(Dict.ID);
        ComponentApplication componentApplication = new ComponentApplication();
        componentApplication.setApplication_id(applicationId);
        iComponentApplicationService.deleteAllByApplicationId(componentApplication);
        imageApplicationService.deleteAllByApplicationId(applicationId);
        applicationArchetypeService.deleteByAppId(applicationId);
    }

    @KafkaListener(topics = {"${kafka.merge.topic}"}, groupId = "${gruopId}")
    public void mergeWebHook(ConsumerRecord<?, ?> consumerRecord) throws Exception {
        byte[] bytes = (byte[]) consumerRecord.value();
        String params = new String(bytes);
        Map<String, Object> parse = JSON.parseObject(params, Map.class);
        String webhookToken = (String) parse.get(Dict.X_GITLAB_TOKEN);
        if (webhookToken == null) return;
        String event = "merge_" + webhookToken;
        webhookContext.executor(event, parse);

    }

    @KafkaListener(topics = {"${kafka.pipeline.topic}"}, groupId = "${gruopId}")
    public void pipeWebHook(ConsumerRecord<?, ?> consumerRecord) throws Exception {
        byte[] bytes = (byte[]) consumerRecord.value();
        String params = new String(bytes);
        Map<String, Object> parse = JSON.parseObject(params, Map.class);
        String webhookToken = (String) parse.get(Dict.X_GITLAB_TOKEN);
        if (webhookToken == null) return;
        String event = "pipeline_" + webhookToken;
        webhookContext.executor(event, parse);
    }
}
