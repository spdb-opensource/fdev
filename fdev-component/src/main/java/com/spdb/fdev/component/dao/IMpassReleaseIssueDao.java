package com.spdb.fdev.component.dao;

import com.spdb.fdev.component.entity.MpassReleaseIssue;

import java.util.List;
import java.util.Map;

public interface IMpassReleaseIssueDao {

    List query(MpassReleaseIssue mpassReleaseIssue) throws Exception;

    MpassReleaseIssue save(MpassReleaseIssue mpassReleaseIssue) throws Exception;

    MpassReleaseIssue update(MpassReleaseIssue mpassReleaseIssue) throws Exception;

    void delete(MpassReleaseIssue mpassReleaseIssue) throws Exception;

    MpassReleaseIssue queryById(String id) throws Exception;

    MpassReleaseIssue queryByComponentAndBranch(String component_id, String branch) throws Exception;

    MpassReleaseIssue queryByBranch(String branch) throws Exception;

    List<MpassReleaseIssue> queryByComponentId(String componentId);

    List<Map>  queryQrmntsData(String userId);

    List<Map> queryIssueDelay(Map params);

    List<MpassReleaseIssue> queryLatestVersion(String componentId, String releaseNodeName);

    List<MpassReleaseIssue> queryAllReleaseIssues(String componentId);
}
