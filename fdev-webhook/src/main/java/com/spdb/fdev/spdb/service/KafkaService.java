package com.spdb.fdev.spdb.service;

import java.util.Map;

public interface KafkaService {

    void sendData(String topic, byte[] data, Map<String, Object> logMap);

}
