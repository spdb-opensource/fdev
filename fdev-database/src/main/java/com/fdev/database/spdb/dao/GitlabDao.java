package com.fdev.database.spdb.dao;

import java.util.*;

public interface GitlabDao {

    Map<String, Object> compareBranch(Integer projectId, String sourceBranch, String mergeBranch) throws Exception;

    List<Map<String, Object>> findCommitDiff(Integer projectId, String commitId) throws Exception;
}
