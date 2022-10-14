package com.spdb.fdev.fdevenvconfig.spdb.kafka;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.fdevenvconfig.spdb.service.WebhookService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaMsgListener {

    @Autowired
    private WebhookService webhookService;

    @KafkaListener(topics = {"${kafka.push.topic}"}, groupId = "fdev-env-config")
    public void receive(ConsumerRecord<?, ?> consumerRecord) {
        byte[] bytes = (byte[]) consumerRecord.value();
        String params = new String(bytes);
        Map<String, Object> parse = JSON.parseObject(params, Map.class);
        webhookService.saveNodeAppConfigMap(parse);
        webhookService.saveAppConfigMap(parse);
    }

}
