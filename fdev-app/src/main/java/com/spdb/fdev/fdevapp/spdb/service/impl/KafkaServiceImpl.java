package com.spdb.fdev.fdevapp.spdb.service.impl;

import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.service.IKafkaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
@RefreshScope
public class KafkaServiceImpl implements IKafkaService {

    private static Logger logger = LoggerFactory.getLogger(KafkaServiceImpl.class);

    @Value("${kafka.send.topic}")
    private String kafkaTopic;

    @Autowired
    private KafkaTemplate<byte[], byte[]> kafkaTemplate;

    /**
     * 加入topic，根据传入的topic来发送kafka，若为空或null，则用默认值
     *
     * @param trigger
     * @param projectName
     * @param data
     * @param topic
     */
    @Override
    public void sendData(String trigger, String projectName, String data, String topic) {
        if (CommonUtils.isNullOrEmpty(topic))
            topic = kafkaTopic;
        ListenableFuture<SendResult<byte[], byte[]>> send = kafkaTemplate.send(topic, data.getBytes());
        send.addCallback(sendResult -> logger.info(">>>>>>>{}-{}发送成功！{}", trigger, projectName, data),
                ex -> logger.error(">>>>>>>{}-{}发送失败:{}", trigger, projectName, ex));
    }

}
