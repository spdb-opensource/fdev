package com.spdb.fdev.fdevapp.spdb.service;

public interface IKafkaService {

    void sendData(String trigger, String projectName, String data, String topic);

}
