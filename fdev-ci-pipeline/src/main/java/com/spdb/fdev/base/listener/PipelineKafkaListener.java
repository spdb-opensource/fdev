package com.spdb.fdev.base.listener;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.pipeline.service.IPipelineService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PipelineKafkaListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    IPipelineService pipelineService;

    @org.springframework.kafka.annotation.KafkaListener(topics = {"#{'${pushTopicName}'}"}, groupId = "#{'${pushGroupId}'}")        //fdev-pipeline
    public void receivePushMessage(ConsumerRecord<?, ?> consumerRecord) throws Exception{
        byte[] bytes = (byte[]) consumerRecord.value();
        String params = new String(bytes);
        JSONObject message = JSONObject.parseObject(params);
        Map map = JSONObject.toJavaObject(message, Map.class);
        pipelineService.webhookPipeline(map);
    }

    @org.springframework.kafka.annotation.KafkaListener(topics = {"#{'${tagPushTopicName}'}"}, groupId = "#{'${tagPushGroupId}'}")
    public void receiveTagPushMessage(ConsumerRecord<?, ?> consumerRecord) throws Exception{
        byte[] bytes = (byte[]) consumerRecord.value();
        String params = new String(bytes);
        JSONObject message = JSONObject.parseObject(params);
        Map map = JSONObject.toJavaObject(message, Map.class);
        pipelineService.webhookPipeline(map);
    }
}
