package com.spdb.fdev.spdb.service;

public interface ITaskService {

    void updateTaskSonarTime(String application_name, String branch_name, String time) throws Exception;

    void updateTaskSonarId(String application_name, String branch_name, String sonar_id) throws Exception;

}
