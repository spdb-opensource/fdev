package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ComponentRecord;
import com.spdb.fdev.component.entity.MpassDevIssue;
import com.spdb.fdev.component.entity.MpassReleaseIssue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IMpassRelaseIssueService {
    String getLasterVersion(String npmName, String npmGroup);

    List<String> getVersion(String npmName, String npmGroup);

    List query(MpassReleaseIssue mpassReleaseIssue) throws Exception;

    MpassReleaseIssue queryMpassReleaseIssueDetail(String id) throws Exception;

    MpassReleaseIssue save(MpassReleaseIssue mpassReleaseIssue) throws Exception;

    MpassReleaseIssue update(MpassReleaseIssue mpassReleaseIssue) throws Exception;

    void delete(MpassReleaseIssue mpassReleaseIssue) throws Exception;

    Map defaultBranchAndVersion(String component_id, String issue_type) throws Exception;

    List query(MpassDevIssue mpassDevIssue) throws Exception;

    MpassDevIssue save(MpassDevIssue mpassDevIssue) throws Exception;

    MpassDevIssue update(MpassDevIssue mpassDevIssue) throws Exception;

    void createComponentRecord(Map hashMap) throws Exception;

    String getNextAlphaVersion(Map hashMap) throws Exception;

    String getNextAlphaorBetaVersion(Map hashMap) throws Exception;

    void devPackage(Map param) throws Exception;

    void changeStage(Map<String, String> map) throws Exception;

    boolean canPackage(Map param) throws Exception;

    String getRootDir(Map param);

    void pipiCallBack(String gitlab_url, String gitlabId, String ref) throws Exception;

    void releasePackage(Map param) throws Exception;

    void mergedCallBack(Integer iid, Integer project_id) throws Exception;

    MpassDevIssue queryMpassDevIssueDetail(String id) throws Exception;

    List<ComponentRecord> queryMpassIssueRecord(Map param) throws Exception;

    void destroyIssue(Map param) throws Exception;

    List<MpassReleaseIssue> queryTransgerReleaseIssue(String component_id,String feature_branch);

    void devIssueTransger(Map<String, String> param) throws Exception;

    List<HashMap<String, Object>> queryQrmntsData(Map requestParam);

    List<Map> queryIssueDelay(Map requestParam) throws Exception;

    void sendDevNotify(String nameCn, MpassReleaseIssue mpassReleaseIssue, MpassDevIssue mpassDevIssue) throws Exception;

    String getComponentAlphaVersion(Map<String, String> hashMap) throws Exception;

    String queryJdkByGitlabUrl(Map<String, String> hashMap) throws Exception;

    void createMutiComponentRecord(Map param) throws Exception;

    void mergedSitAndReleaseCallBack(String state, Integer iid, Integer project_id, String target_branch) throws Exception;

    void mergedMasterCallBack(String state, Integer iid, Integer project_id, String target_branch) throws Exception;

    void addComponentRecord(Map param) throws Exception;
}
