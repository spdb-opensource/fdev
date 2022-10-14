package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.spdb.service.KafkaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Map;

@Service
@RefreshScope
public class KafkaServiceImpl implements KafkaService {

    private static Logger logger = LoggerFactory.getLogger(KafkaServiceImpl.class);

    @Autowired
    private KafkaTemplate<byte[], byte[]> kafkaTemplate;

    @Override
    public void sendData(String topic, byte[] data, Map<String, Object> logMap) {
        ListenableFuture<SendResult<byte[], byte[]>> send = kafkaTemplate.send(topic, data);
        send.addCallback((sendResult -> logger.info("topic:{}发送成功,project id:{},iid:{},target branch:{},merge state:{},push branch:{},pipeline id:{},pipeline branch:{},pipeline status:{}",
                topic, logMap.get(Dict.PROJECT_ID), logMap.get(Dict.IID), logMap.get(Dict.TARGET_BRANCH), logMap.get(Dict.STATE), logMap.get(Dict.PUSH_BRANCH), logMap.get(Dict.PIPELINE_ID), logMap.get(Dict.PIPELINE_BRANCH), logMap.get(Dict.STATUS))),
                ex -> logger.error("topic:{}发送失败,project id:{},iid:{},target branch:{},merge state:{},push branch:{},pipeline id:{},pipeline branch:{},pipeline status:{},失败信息:{}",
                        topic, logMap.get(Dict.PROJECT_ID), logMap.get(Dict.IID), logMap.get(Dict.TARGET_BRANCH), logMap.get(Dict.STATE), logMap.get(Dict.PUSH_BRANCH), logMap.get(Dict.PIPELINE_ID), logMap.get(Dict.PIPELINE_BRANCH), logMap.get(Dict.STATUS), ex));
    }

}
