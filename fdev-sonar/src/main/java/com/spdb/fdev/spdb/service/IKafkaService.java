package com.spdb.fdev.spdb.service;

import java.util.Map;

public interface IKafkaService {

    void sendData(String topic, byte[] data, Map<String, Object> logMap);

}
