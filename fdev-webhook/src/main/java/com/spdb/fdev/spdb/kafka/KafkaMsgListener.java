package com.spdb.fdev.spdb.kafka;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.spdb.service.SonarService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaMsgListener {

    @Autowired
    private SonarService sonarService;

    @KafkaListener(topics = {"${kafka.runner.topic}"}, groupId = "fdev-webhook")
    public void receive(ConsumerRecord<?, ?> consumerRecord) {
        byte[] bytes = (byte[]) consumerRecord.value();
        String params = new String(bytes);
        Map<String, Object> parse = JSON.parseObject(params, Map.class);
        sonarService.getSonarMsg(parse);
    }

}
