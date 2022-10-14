package com.spdb.fdev.component.service;

import java.util.HashSet;
import java.util.Map;

public interface ICommonInfoService {
    Map addMembersForApp(HashSet<Map<String, String>> managers, String id, String role) throws Exception;

    Map addMembersForApp(String userId, String id, String role) throws Exception;

    void continueIntegration(String projectId, String projectname, String yaml);

    void updateIntegration(String gitlaburl, String projectname, String yaml) throws Exception;

    boolean isJoinCompare(String latestVersion);

    Boolean compareVersion(String[] versionList, String[] version);
}
