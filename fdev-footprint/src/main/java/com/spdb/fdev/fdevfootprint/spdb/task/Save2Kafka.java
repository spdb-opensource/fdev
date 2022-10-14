package com.spdb.fdev.fdevfootprint.spdb.task;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class Save2Kafka extends BaseTask {
    protected Logger logger = LoggerFactory.getLogger(Save2Kafka.class);

    public Save2Kafka() {

    }

    public Save2Kafka(String message) {
        super(message);
    }

    @Autowired
    KafkaTemplate<byte[], byte[]> kafkaTemplate;

    @Override
    public void run() {
        String value = (String) this.getMessage();
        ListenableFuture<SendResult<byte[], byte[]>> send = kafkaTemplate.send(super.getTopicName(), value.getBytes());

        send.addCallback(new ListenableFutureCallback<SendResult<byte[], byte[]>>() {
            @Override
            public void onSuccess(SendResult<byte[], byte[]> result) {
                RecordMetadata rm = result.getRecordMetadata();
                logger.info("@@@kafka message@@@ topic:" + rm.topic() + " partition:" + rm.partition() + " offset:" + rm.offset());
            }

            @Override
            public void onFailure(Throwable ex) {
                logger.error(ex.getMessage());
            }
        });
    }

}
