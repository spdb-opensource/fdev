package com.spdb.fdev.release.service.impl;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.release.service.IKafkaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
public class KafkaServiceImpl implements IKafkaService {

    private static Logger logger = LoggerFactory.getLogger(KafkaServiceImpl.class);

    @Value("${fdev.media.topic}")
    private String kafkaTopic;

    @Autowired
    private KafkaTemplate<byte[], byte[]> kafkaTemplate;

    @Override
    public void sendData(String trigger, String projectName, String data) {
        logger.info("发送日志:"+data);
        ListenableFuture<SendResult<byte[], byte[]>> send = kafkaTemplate.send(kafkaTopic, data.getBytes());
        send.addCallback(sendResult -> logger.info(">>>>>>>{}-{}发送成功！", trigger, projectName),
                ex -> logger.error(">>>>>>>{}-{}发送失败:{}", trigger, projectName, ex));
    }


}
