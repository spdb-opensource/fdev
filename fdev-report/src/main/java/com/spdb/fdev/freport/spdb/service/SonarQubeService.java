package com.spdb.fdev.freport.spdb.service;

import com.spdb.fdev.freport.spdb.entity.sonar.SonarProjectRank;

import java.util.List;

public interface SonarQubeService {

    List<SonarProjectRank> querySonarProjectRank() throws Exception;

    void cacheSonarProject() throws Exception;
}
