package com.spdb.fdev.component.service;

import com.spdb.fdev.component.entity.ComponentRecord;
import com.spdb.fdev.component.entity.MpassDevIssue;
import com.spdb.fdev.component.entity.MpassReleaseIssue;

import java.util.List;
import java.util.Map;

public interface IComponentReleaseIssueService {
    MpassReleaseIssue save(MpassReleaseIssue mpassReleaseIssue) throws Exception;

    MpassReleaseIssue update(MpassReleaseIssue mpassReleaseIssue) throws Exception;

    MpassDevIssue save(MpassDevIssue mpassDevIssue) throws Exception;

    boolean canPackage(Map param) throws Exception;

    void devPackage(Map param) throws Exception;

    void destroyIssue(Map param) throws Exception;

    void releasePackage(Map param) throws Exception;

    List<ComponentRecord> queryMpassIssueRecord(Map param) throws Exception;
}
