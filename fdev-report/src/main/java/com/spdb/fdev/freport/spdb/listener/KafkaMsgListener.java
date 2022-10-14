package com.spdb.fdev.freport.spdb.listener;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.freport.base.dict.GitlabDict;
import com.spdb.fdev.freport.spdb.dto.gitlab.WebHooksDto;
import com.spdb.fdev.freport.spdb.service.GitlabService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaMsgListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GitlabService gitlabService;

    @KafkaListener(topics = {"${kafka.merge.topic}"}, groupId = "${kafka.group.id}")
    public void receiveMerge(ConsumerRecord<?, ?> consumerRecord) throws Exception {
        String value = (String) consumerRecord.value();
        logger.info("kafka data:{}", value);
        WebHooksDto webHooksDto = JSONObject.parseObject(value, WebHooksDto.class);
        if (GitlabDict.MERGE_REQUEST.equals(webHooksDto.getEvent_type())) {
            gitlabService.dealWebHooksMerge(webHooksDto);
        }
    }
}
