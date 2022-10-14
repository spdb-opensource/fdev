package com.spdb.fdev.gitlabwork.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * IGitlabCommitService
 *
 * @blame Android Team
 */
public interface IGitlabCommitService {

    Map<String, Object> addCommitInfoList(String since, String until) throws Exception;

    void findOneBranchCommitList(int projectId, List<Map<String, Object>> branchList, Map<String, Object> workInfoMap, List<Map<String, Object>> list, String since, String until) throws IOException;

    void findCommitInfo(int projectId, List<Map<String, Object>> oneBranchCommitList, List<Map<String, Object>> list, Map<String, Object> workInfoMap) throws IOException;
}
