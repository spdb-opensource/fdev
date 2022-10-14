package com.spdb.fdev.component.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spdb.fdev.component.entity.MpassDevIssue;

import java.util.List;

public interface IMpassDevIssueDao {

    List query(MpassDevIssue mpassDevIssue) throws Exception;

    MpassDevIssue save(MpassDevIssue mpassDevIssue) throws Exception;

    MpassDevIssue update(MpassDevIssue mpassDevIssue) throws JsonProcessingException;

    void delete(MpassDevIssue mpassDevIssue) throws Exception;

    MpassDevIssue queryById(String id) throws Exception;

    MpassDevIssue queryByComIdAndIssueIdBranch(String componentId, String issueId, String branch);

    List<MpassDevIssue> queryByIssueId(String issueId);
}
