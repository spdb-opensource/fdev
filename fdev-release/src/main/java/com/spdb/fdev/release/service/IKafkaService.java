package com.spdb.fdev.release.service;

public interface IKafkaService {

    void sendData(String trigger, String projectName, String data);

}
