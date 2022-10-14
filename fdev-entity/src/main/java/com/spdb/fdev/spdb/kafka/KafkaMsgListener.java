package com.spdb.fdev.spdb.kafka;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.spdb.service.IEntityService;
import com.spdb.fdev.spdb.service.IWebhookService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaMsgListener {

    @Autowired
    private IWebhookService webhookService;
    @Autowired
    private IEntityService entityService;

    @KafkaListener(topics = {"${kafka.push.topic}"}, groupId = "fdev-entity")
    public void receive(ConsumerRecord<?, ?> consumerRecord) throws Exception {
        byte[] bytes = (byte[]) consumerRecord.value();
        String params = new String(bytes);
        Map<String, Object> parse = JSON.parseObject(params, Map.class);
        webhookService.saveAppConfigMap(parse);
    }
    
    //@KafkaListener(topics = {"${delete.app.topic}"}, groupId = "fdev-entity")
    public void deleteService(ConsumerRecord<?, ?> consumerRecord) throws Exception {
        byte[] bytes = (byte[]) consumerRecord.value();
        String params = new String(bytes);
        Map<String, Object> parse = JSON.parseObject(params, Map.class);
        entityService.deleteConfigDependency(parse);
    }

}
