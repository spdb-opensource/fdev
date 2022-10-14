package com.fdev.database.spdb.service;

import java.util.*;

public interface GitlabService {

    Map<String, Object> compareBranch(Integer projectId, String sourceBranch, String mergeBranch) throws Exception;

    void gitOperation(String name_en, String git, String nas_apps_path, String branch);

    List<Map<String, Object>> findCommitDiff(Integer projectId, String commitId) throws Exception;
}
